package ca.sqlpower.architect;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

public class SQLTable extends SQLObject implements SQLObjectListener {

	private static Logger logger = Logger.getLogger(SQLTable.class);

	protected SQLObject parent;
	protected String tableName;
	protected String remarks;
	protected String objectType;
	protected String primaryKeyName;
	
	/**
	 * A List of SQLColumn objects which make up all the columns of
	 * this table.
	 */
	protected Folder columnsFolder;

	/**
	 * A List of SQLRelationship objects describing keys that this
	 * table exports.  This SQLTable is the "pkTable" in its exported
	 * keys.
	 */
	protected Folder exportedKeysFolder;

	/**
	 * A List of SQLRelationship objects describing keys that this
	 * table imports.  This SQLTable is the "fkTable" in its imported
	 * keys.
	 */
	protected Folder importedKeysFolder;

	public SQLTable(SQLObject parent, String name, String remarks, String objectType) {
		logger.debug("NEW TABLE "+name+"@"+hashCode());
		this.parent = parent;
		this.tableName = name;
		this.remarks = remarks;
		this.objectType = objectType;

		this.children = new ArrayList();
		initFolders(false);

		/* we listen to the importedKeysFolder because this is how we
		 * know to remove FK columns when their owning relationship is
		 * removed. */
		importedKeysFolder.addSQLObjectListener(this);
	}
	
	/**
	 * Creates a new SQLTable with parent as its parent and a null
	 * schema and catalog.  The table will contain the three default
	 * folders: "Columns" "Exported Keys" and "Imported Keys".
	 */
	public SQLTable(SQLDatabase parent) {
		this(parent, "", "", "TABLE");
	}

	/**
	 * Creates a new SQLTable with no children, no parent, and all
	 * properties set to their defaults.
	 */
	public SQLTable() {
		//columnsPopulated = true;
		//relationshipsPopulated = true;
		children = new ArrayList();
	}

	/**
	 * If you create a table from scratch using the no-args
	 * constructor, you should call this to create the standard set of
	 * Folder objects under this table.  The regular constructor does
	 * it automatically.
	 *
	 * @param populated The initial value to give the folders'
	 * populated status.  When loading from a file, this should be true;
	 * if lazy loading from a database, it should be false.
	 */
	public void initFolders(boolean populated) {
		addChild(new Folder(Folder.COLUMNS, populated));
		addChild(new Folder(Folder.EXPORTED_KEYS, populated));
		addChild(new Folder(Folder.IMPORTED_KEYS, populated));
	}

	protected static void addTablesToDatabase(SQLDatabase addTo) 
		throws SQLException, ArchitectException {
		HashMap catalogs = new HashMap();
		HashMap schemas = new HashMap();
		synchronized (addTo) {
			Connection con = addTo.getConnection();
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet mdTables = null;
			try {
				mdTables = dbmd.getTables(null,
										  null,
										  "%",
										  new String[] {"TABLE", "VIEW"});
				while (mdTables.next()) {
					SQLObject tableParent = addTo;

					String catName = mdTables.getString(1);
					SQLCatalog cat = null;

					if (catName != null) {
						cat = (SQLCatalog) catalogs.get(catName);
						if (cat == null) {
							cat = new SQLCatalog(addTo, catName);
							cat.setNativeTerm(dbmd.getCatalogTerm());
							logger.debug("Set catalog term to "+cat.getNativeTerm());
							addTo.children.add(cat);
							catalogs.put(catName, cat);
						}
						tableParent = cat;
					}

					String schName = mdTables.getString(2);
					SQLSchema schema = null;
					if (schName != null) {
						schema = (SQLSchema) schemas.get(catName+"."+schName);
						if (schema == null) {
							if (cat == null) {
								schema = new SQLSchema(addTo, schName);
								addTo.children.add(schema);
							} else {
								schema = new SQLSchema(cat, schName);
								cat.children.add(schema);
							}
							schema.setNativeTerm(dbmd.getSchemaTerm());
							logger.debug("Set schema term to "+schema.getNativeTerm());
							schemas.put(catName+"."+schName, schema);
						}
						tableParent = schema;
					}

					tableParent.children.add(new SQLTable(tableParent,
														  mdTables.getString(3),
														  mdTables.getString(5),
														  mdTables.getString(4)
														  ));
				}
			} finally {
				if (mdTables != null) mdTables.close();
			}
		}
	}

	public static SQLTable getDerivedInstance(SQLTable source, SQLDatabase parent)
		throws ArchitectException {
		source.populateColumns();
		source.populateRelationships();
		SQLTable t = new SQLTable(parent);
		t.columnsFolder.populated = true;
		t.importedKeysFolder.populated = true;
		t.exportedKeysFolder.populated = true;
		t.tableName = source.tableName;
		t.remarks = source.remarks;
		t.primaryKeyName = source.getName()+"_pk";
		t.inherit(source);
		parent.addChild(t);
		return t;
	}
	
	protected synchronized void populateColumns() throws ArchitectException {
		if (columnsFolder.isPopulated()) return;
		if (columnsFolder.children.size() > 0) throw new IllegalStateException("Can't populate table because it already contains columns");
		try {
			SQLColumn.addColumnsToTable(this,
										getCatalogName(),
										getSchemaName(),
										tableName);
		} catch (SQLException e) {
			throw new ArchitectException("Failed to populate columns of table "+getName(), e);
		} finally {
			columnsFolder.populated = true;
			Collections.sort(columnsFolder.children, new SQLColumn.SortByPKSeq());
			normalizePrimaryKey();
			int newSize = columnsFolder.children.size();
			int[] changedIndices = new int[newSize];
			for (int i = 0; i < newSize; i++) {
				changedIndices[i] = i;
			}
			columnsFolder.fireDbChildrenInserted(changedIndices, columnsFolder.children);
		}
	}
	
	/**
	 * Populates all the imported key relationships.  This has the
	 * side effect of populating the exported key side of the
	 * relationships for the exporting tables.
	 */
	public synchronized void populateRelationships() throws ArchitectException {
		if (!columnsFolder.isPopulated()) throw new IllegalStateException("Table must be populated before relationships are added");
		if (importedKeysFolder.isPopulated()) return;
		int oldSize = importedKeysFolder.children.size();

		/* this must come before
		 * SQLRelationship.addImportedRelationshipsToTable because
		 * addImportedRelationshipsToTable causes SQLObjectEvents to be fired,
		 * which in turn could cause infinite recursion when listeners
		 * query the size of the relationships folder.
		 */
		importedKeysFolder.populated = true;
		try {
			SQLRelationship.addImportedRelationshipsToTable(this);
		} finally {
			int newSize = importedKeysFolder.children.size();
			if (newSize > oldSize) {
				int[] changedIndices = new int[newSize - oldSize];
				for (int i = 0, n = newSize - oldSize; i < n; i++) {
					changedIndices[i] = oldSize + i;
				}
				try {
					importedKeysFolder.fireDbChildrenInserted
						(changedIndices,
						 importedKeysFolder.children.subList(oldSize, newSize));
				} catch (IndexOutOfBoundsException ex) {
					logger.error("Index out of bounds while adding imported keys to table "
								 +getName()+" where oldSize="+oldSize+"; newSize="+newSize
								 +"; imported keys="+importedKeysFolder.children);
				}
			}
		}
	}

	/**
	 * Convenience method for getImportedKeys.addChild(r).
	 */
	public void addImportedKey(SQLRelationship r) {
		importedKeysFolder.addChild(r);
	}

	/**
	 * Convenience method for getImportedKeys.removeChild(r).
	 */
	public void removeImportedKey(SQLRelationship r) {
		importedKeysFolder.removeChild(r);
	}

	/**
	 * Convenience method for getExportedKeys.addChild(r).
	 */
	public void addExportedKey(SQLRelationship r) {
		exportedKeysFolder.addChild(r);
	}

	/**
	 * Convenience method for getExportedKeys.removeChild(r).
	 */
	public void removeExportedKey(SQLRelationship r) {
		exportedKeysFolder.removeChild(r);
	}

	/**
	 * Counts the number of columns in the primary key of this table.
	 */
	public int pkSize() {
		int size = 0;
		Iterator it = columnsFolder.children.iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			if (c.getPrimaryKeySeq() != null) {
				size++;
			} else {
				break;
				}
		}
		return size;
	}

	/**
	 * Adds all the columns of the given source table to the end of
	 * this table's column list.
	 */
	public void inherit(SQLTable source) throws ArchitectException {
		inherit(columnsFolder.children.size(), source);
	}

	/**
	 * Inserts all the columns of the given source table into this
	 * table at position <code>pos</code>.
	 *
	 * <p>If this table currently has no columns, then the source's
	 * primary key will remain intact (and this table will become an
	 * identical copy of source).  If not, and if the insertion
	 * position <= this.pkSize(), then all source columns will be
	 * added to this table's primary key.  Otherwise, no source
	 * columns will be added to this table's primary key.
	 */
	public void inherit(int pos, SQLTable source) throws ArchitectException {
		SQLColumn c;

		boolean addToPK;
		int pkSize = pkSize();
		int sourceSize = source.columnsFolder.children.size();
		int originalSize = columnsFolder.children.size();
		if (originalSize == 0 || pos < pkSize) {
			addToPK = true;
			normalizePrimaryKey();
			for (int i = pos; i < pkSize; i++) {
				((SQLColumn) columnsFolder.children.get(i)).primaryKeySeq = new Integer(i + sourceSize);
			}
		} else {
			addToPK = false;
		}

		Iterator it = source.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn child = (SQLColumn) it.next();
			c = SQLColumn.getDerivedInstance(child, this);
			if (originalSize > 0) {
				if (addToPK) {
					c.primaryKeySeq = new Integer(pos);
				} else {
					c.primaryKeySeq = null;
				}
			}
			columnsFolder.addChild(pos, c);
			pos += 1;
		}
	}

	public void inherit(int pos, SQLColumn sourceCol) {
		boolean addToPK;
		int pkSize = pkSize();
		if (pos < pkSize) {
			addToPK = true;
			normalizePrimaryKey();
			for (int i = pos; i < pkSize; i++) {
				((SQLColumn) columnsFolder.children.get(i)).primaryKeySeq = new Integer(i + 1);
			}
		} else {
			addToPK = false;
		}

		SQLColumn c = SQLColumn.getDerivedInstance(sourceCol, this);
		if (addToPK) {
			c.primaryKeySeq = new Integer(pos);
		} else {
			c.primaryKeySeq = null;
		}
		columnsFolder.addChild(pos, c);
	}

	public SQLColumn getColumn(int idx) throws ArchitectException {
		return (SQLColumn) columnsFolder.getChild(idx);
	}

	/**
	 * Populates this table then searches for the named column.
	 */
	public SQLColumn getColumnByName(String colName) throws ArchitectException {
		return getColumnByName(colName, true);
	}
	
	/**
	 * Searches for the named table.
	 *
	 * @param populate If true, this table will retrieve its column
	 * list from the database; otherwise it just searches the current
	 * list.
	 */
	public SQLColumn getColumnByName(String colName, boolean populate) throws ArchitectException {
		if (populate && !columnsFolder.isPopulated()) populateColumns();
		logger.debug("Looking for column "+colName+" in "+children);
		Iterator it = columnsFolder.children.iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getColumnName().equalsIgnoreCase(colName)) {
				logger.debug("FOUND");
				return col;
			}
		}
		logger.debug("NOT FOUND");
		return null;
	}

	public void addColumn(SQLColumn col) {
		addColumn(columnsFolder.children.size(), col);
	}

	public void addColumn(int pos, SQLColumn col) {
		boolean addToPK = false;
		int pkSize = pkSize();
		try {
			if (getColumns().size() > 0 && pos <= pkSize) {
				addToPK = true;
				normalizePrimaryKey();
				for (int i = pos; i < pkSize; i++) {
					((SQLColumn) columnsFolder.children.get(i)).primaryKeySeq = new Integer(i + 1);
				}
			}
		} catch (ArchitectException e) {
			logger.warn("Unexpected ArchitectException in addColumn", e);
		}

		col.setParent(null);
		if (addToPK) {
			col.nullable = DatabaseMetaData.columnNoNulls;
			col.primaryKeySeq = new Integer(pos);
		} else {
			col.primaryKeySeq = null;
		}
		columnsFolder.addChild(pos, col);
	}

	public void addChild(SQLObject child) {
		addChild(children.size(), child);
	}
	
	/**
	 * Connects up the columnsFolder, exportedKeysFolder, and
	 * importedKeysFolder pointers to the children at indices 0, 1,
	 * and 2 respectively.
	 */
	public void addChild(int index, SQLObject child) {
		if (child instanceof Folder) {
			if (children.size() == 0) {
				columnsFolder = (Folder) child;
			} else if (children.size() == 1) {
				exportedKeysFolder = (Folder) child;
			} else if (children.size() == 2) {
				importedKeysFolder = (Folder) child;

				/* we listen to the importedKeysFolder because this is how we
				 * know to remove FK columns when their owning relationship is
				 * removed. */
				importedKeysFolder.addSQLObjectListener(this);
			} else {
				throw new UnsupportedOperationException("Can't add a 4th folder to SQLTable");
			}
		} else {
			throw new UnsupportedOperationException("You can only add Folders to SQLTable");
		}
		super.addChild(index, child);
	}

	/**
	 * Calls {@link #removeColumn(SQLColumn)} with the appropriate argument.
	 */
	public void removeColumn(int index) throws LockedColumnException {
		removeColumn((SQLColumn) columnsFolder.children.get(index));
	}

	/**
	 * Removes the given column if it is in this table.  If you want
	 * to change a column's, index, use the {@link
	 * #changeColumnIndex(int,int)} method because it does not throw
	 * LockedColumnException.
	 *
	 * @throws LockedColumnException if the given column belongs to an
	 * imported or exported key.
	 */
	public void removeColumn(SQLColumn col) throws LockedColumnException {
// 		List keys = keysOfColumn(col);
// 		if (keys.isEmpty()) {
// 			columnsFolder.removeChild(col);
// 			normalizePrimaryKey();
// 		} else {
// 			throw new LockedColumnException("This column can't be removed because it belongs to the "+keys.get(0)+" relationship");
// 		}
		columnsFolder.removeChild(col);
		normalizePrimaryKey();
	}

	/**
	 * Moves the column at index <code>oldIdx</code> to index
	 * <code>newIdx</code>.  This may cause the moved column to become
	 * part of the primary key (or to be removed from the primary
	 * key).
	 *
	 * @param oldIdx the present index of the column.
	 * @param newIdx the index that the column will have when this
	 * method returns.
	 */
	public void changeColumnIndex(int oldIdx, int newIdx) throws ArchitectException {
		// this old way didn't cause any events
// 		SQLColumn col = (SQLColumn) columnsFolder.children.remove(oldIdx);
// 		columnsFolder.children.add(newIdx, col);
// 		if (newIdx == 0
// 			|| ((SQLColumn) columnsFolder.children.get(newIdx-1)).primaryKeySeq != null) {
// 			col.primaryKeySeq = new Integer(1); // will get sane value when normalized
// 		}
// 		normalizePrimaryKey();

		SQLColumn col = (SQLColumn) columnsFolder.getChild(oldIdx);
		removeColumn(oldIdx);
		addColumn(newIdx, col);
	}

	/**
	 * Sets the primaryKeySeq on each child column currently in the
	 * primary key to its index in this table.
	 */
	public void normalizePrimaryKey() {
		if (columnsFolder.children.isEmpty()) return;
		boolean donePk = false;
		int i = 0;
		Iterator it = columnsFolder.children.iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getPrimaryKeySeq() == null) donePk = true;
			if (!donePk) {
				col.primaryKeySeq = new Integer(i);
			} else {
				col.primaryKeySeq = null;
			}
			i++;
		}
	}
	
	public List keysOfColumn(SQLColumn col) {
		LinkedList keys = new LinkedList();
		Iterator it = exportedKeysFolder.children.iterator();
		while (it.hasNext()) {
			SQLRelationship r = (SQLRelationship) it.next();
			if (r.containsPkColumn(col)) {
				keys.add(r);
			}
		}
		it = importedKeysFolder.children.iterator();
		while (it.hasNext()) {
			SQLRelationship r = (SQLRelationship) it.next();
			if (r.containsFkColumn(col)) {
				keys.add(r);
			}
		}
		return keys;
	}

	public String toString() {
		return getShortDisplayName();
	}

	// ---------------------- SQLObject support ------------------------

	public SQLObject getParent() {
		return parent;
	}

	protected void setParent(SQLObject newParent) {
		parent = newParent;
	}

	public String getName() {
		return tableName;
	}

	/**
	 * The table's name.
	 */
	public String getShortDisplayName() {
		SQLSchema schema = getSchema();
		if (schema != null) {
			return schema.getSchemaName()+"."+tableName+" ("+objectType+")";
		} else {
			if (objectType != null) {
				return tableName+" ("+objectType+")";
			} else {
				return tableName;
			}
		}
	}

	/**
	 * Just calls populateColumns and populateRelationships.
	 */
	public void populate() throws ArchitectException {
// 		populateColumns();
// 		populateRelationships();
	}
	
	public boolean isPopulated() {
		return true;
// 		if (columnsFolder == null
// 			|| importedKeysFolder == null
// 			|| exportedKeysFolder == null) {
// 			return false;
// 		} else {
// 			return columnsFolder.isPopulated()
// 				&& importedKeysFolder.isPopulated()
// 				&& exportedKeysFolder.isPopulated();
// 		}
	}

	/**
	 * Returns true (tables have several folders as children).
	 */
	public boolean allowsChildren() {
		return true;
	}

	public void removeDependencies() {
		Iterator it = importedKeysFolder.children.iterator();
		while (it.hasNext()) {
			SQLRelationship r = (SQLRelationship) it.next();
			r.getPkTable().removeExportedKey(r);
			logger.debug(r);
		}

		it = exportedKeysFolder.children.iterator();
		while (it.hasNext()) {
			SQLRelationship r = (SQLRelationship) it.next();
			r.getFkTable().removeImportedKey(r);
			logger.debug(r);
		}
	}

	/**
	 * The Folder class is a SQLObject that holds a SQLTable's child
	 * folders (columns and relationships).
	 */
	public static class Folder extends SQLObject {
		protected boolean populated;
		protected int type;
		protected String name;
		protected SQLTable parent;

		public static final int COLUMNS = 1;
		public static final int IMPORTED_KEYS = 2;
		public static final int EXPORTED_KEYS = 3;

		public Folder(int type, boolean populated) {
			this.populated = populated;
			this.type = type;
			this.children = new ArrayList();
			if (type == COLUMNS) {
				name = "Columns";
			} else if (type == IMPORTED_KEYS) {
				name = "Imported Keys";
			} else if (type == EXPORTED_KEYS) {
				name = "Exported Keys";
			} else {
				throw new IllegalArgumentException("Unknown folder type: "+type);
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String n) {
			name = n;
		}

		public SQLObject getParent() {
			return parent;
		}

		/**
		 * Sets the parent reference in this folder.
		 * 
		 * @throws ClassCastException if newParent is not an instance of SQLTable.
		 */
		protected void setParent(SQLObject newParentTable) {
			parent = (SQLTable) newParentTable;
		}

		public void populate() throws ArchitectException {
			try {
				if (type == COLUMNS) {
					parent.populateColumns();
				} else if (type == IMPORTED_KEYS) {
					parent.populateColumns();
					parent.populateRelationships();
				} else if (type == EXPORTED_KEYS) {
					// XXX: not implemented yet
				} else {
					throw new IllegalArgumentException("Unknown folder type: "+type);
				}
			} finally {
				populated = true;
			}
		}

		public boolean isPopulated() {
			return populated;
		}

		public String getShortDisplayName() {
			return name;
		}

		public boolean allowsChildren() {
			return true;
		}

		public String toString() {
			return name;
		}

		/**
		 * Returns the type code of this folder.
		 *
		 * @return One of COLUMNS, IMPORTED_KEYS, or EXPORTED_KEYS.
		 */
		public int getType() {
			return type;
		}
	}
	
	// -------------------- SQL Object Listener Support ----------------------
	public void dbChildrenInserted(SQLObjectEvent e) {
		// XXX: when we implement shared FK columns, we should either insert a new column or increase an existing column's FK link count when a new imported SQLRelationship is added to this table.
		// XXX: at that time, we can remove the FK insertion code from the swingui.Relationship constructor
	}

	/**
	 * Removes unreferenced FK columns from this table when FK
	 * relationships are removed from this table.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		logger.debug("got dbChildrenRemoved event from "+e.getSource());
		if (e.getSource() == importedKeysFolder) {
			try {
				SQLObject[] removedChildren = e.getChildren();
				for (int i = 0; i < removedChildren.length; i++) {
					SQLRelationship rel = (SQLRelationship) removedChildren[i];
					Iterator mappings = rel.getChildren().iterator();
					while (mappings.hasNext()) {
						SQLRelationship.ColumnMapping cmap = (SQLRelationship.ColumnMapping) mappings.next();
						// XXX: when we can make multiple relationships share an FK column, we should check for other FKs using this column before dropping it
						removeColumn(cmap.getFkColumn());
					}
				}
			} catch (ArchitectException ex) {
				logger.error("Couldn't remove orphaned FK column", ex);
			}
		}
	}

	public void dbObjectChanged(SQLObjectEvent e) {
	}

	public void dbStructureChanged(SQLObjectEvent e) {
	}

	// ------------------ Accessors and mutators below this line ------------------------

	/**
	 * Walks up the SQLObject containment hierarchy and returns the
	 * first SQLDatabase object encountered.  If this SQLTable has no
	 * SQLDatabase ancestors, the return value is null.
	 *
	 * @return the value of parentDatabase
	 */
	public SQLDatabase getParentDatabase()  {
		SQLObject o = this.parent;
		while (o != null && ! (o instanceof SQLDatabase)) {
			o = o.getParent();
		}
		return (SQLDatabase) o;
	}

	/**
	 * @return An empty string if the catalog for this table is null;
	 * otherwise, getCatalog().getCatalogName().
	 */
	public String getCatalogName() {
		SQLCatalog catalog = getCatalog();
		if (catalog == null) {
			return "";
		} else {
			return catalog.getCatalogName();
		}
	}

	public SQLCatalog getCatalog()  {
		SQLObject o = this.parent;
		while (o != null && ! (o instanceof SQLCatalog)) {
			o = o.getParent();
		}
		return (SQLCatalog) o;
	}

	/**
	 * @return An empty string if the schema for this table is null;
	 * otherwise, schema.getSchemaName().
	 */
	public String getSchemaName() {
		SQLSchema schema = getSchema();
		if (schema == null) {
			return "";
		} else {
			return schema.getSchemaName();
		}
	}

	public SQLSchema getSchema()  {
		SQLObject o = this.parent;
		while (o != null && ! (o instanceof SQLSchema)) {
			o = o.getParent();
		}
		return (SQLSchema) o;
	}

	public Folder getColumnsFolder() {
		return columnsFolder;
	}

	/**
	 * Gets the value of tableName
	 *
	 * @return the value of tableName
	 */
	public String getTableName()  {
		return this.tableName;
	}

	/**
	 * Sets the table name, and also modifies the primary key name if
	 * it was previously null or set to the default of
	 * "oldTableName_pk".
	 *
	 * @param argTableName The new table name.  NULL is not allowed.
	 */
	public void setTableName(String argTableName) {
		String oldTableName = tableName;
		if ( ! argTableName.equals(tableName) ) {
			this.tableName = argTableName;
			fireDbObjectChanged("tableName");
		}
		if (primaryKeyName == null
			|| primaryKeyName.equals("")
			|| primaryKeyName.equals(oldTableName+"_pk")) {
			setPrimaryKeyName(tableName+"_pk");
		}
	}

	/**
	 * Gets the value of remarks
	 *
	 * @return the value of remarks
	 */
	public String getRemarks()  {
		return this.remarks;
	}

	/**
	 * Sets the value of remarks
	 *
	 * @param argRemarks Value to assign to this.remarks
	 */
	public void setRemarks(String argRemarks) {
		this.remarks = argRemarks;
		fireDbObjectChanged("remarks");
	}

	/**
	 * Gets the value of columns
	 *
	 * @return the value of columns
	 */
	public synchronized List getColumns() throws ArchitectException {
		populateColumns();
		return columnsFolder.getChildren();
	}

	/**
	 * Gets the value of importedKeys
	 *
	 * @return the value of importedKeys
	 */
	public List getImportedKeys() throws ArchitectException {
		return this.importedKeysFolder.getChildren();
	}

	/**
	 * Gets the value of exportedKeys
	 *
	 * @return the value of exportedKeys
	 */
	public List getExportedKeys() throws ArchitectException {
		return this.exportedKeysFolder.getChildren();
	}

	/**
	 * Gets the value of columnsPopulated
	 *
	 * @return the value of columnsPopulated
	 */
	public boolean isColumnsPopulated()  {
		return columnsFolder.isPopulated();
	}

	/**
	 * Allows an outsider to tell this SQLTable that its columns list
	 * is already populated (true) or still needs to be populated from
	 * the source database (false).  Users of the class should not
	 * normally call this method, but the load method of
	 * SwingUIProject needs to call this.
	 *
	 * @param argColumnsPopulated Value to assign to this.columnsPopulated
	public void setColumnsPopulated(boolean argColumnsPopulated) {
		this.columnsPopulated = argColumnsPopulated;
	}
	 */

	/**
	 * Gets the value of relationshipsPopulated
	 *
	 * @return the value of relationshipsPopulated
	 */
	public boolean isRelationshipsPopulated()  {
		return importedKeysFolder.isPopulated() && exportedKeysFolder.isPopulated();
	}

	/**
	 * Allows an outsider to tell this SQLTable that its relationships
	 * list is already populated (true) or still needs to be populated
	 * from the source database (false).  Users of the class should
	 * not normally call this method, but the load method of
	 * SwingUIProject needs to call this.
	 *
	 * @param argRelationshipsPopulated Value to assign to this.relationshipsPopulated
	public void setRelationshipsPopulated(boolean argRelationshipsPopulated) {
		this.relationshipsPopulated = argRelationshipsPopulated;
	}
	 */

	/**
	 * Gets the value of primaryKeyName
	 *
	 * @return the value of primaryKeyName
	 */
	public String getPrimaryKeyName()  {
		return this.primaryKeyName;
	}

	/**
	 * Sets the value of primaryKeyName
	 *
	 * @param argPrimaryKeyName Value to assign to this.primaryKeyName
	 */
	public void setPrimaryKeyName(String argPrimaryKeyName) {
		this.primaryKeyName = argPrimaryKeyName;
		fireDbObjectChanged("primaryKeyName");
	}
	
	
	/**
	 * Gets the value of objectType
	 *
	 * @return the value of objectType
	 */
	public String getObjectType()  {
		return this.objectType;
	}

	/**
	 * Sets the value of objectType
	 *
	 * @param argObjectType Value to assign to this.objectType
	 */
	public void setObjectType(String argObjectType) {
		this.objectType = argObjectType;
		fireDbObjectChanged("objectType");
	}

}
