package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.log4j.Logger;

import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class SQLTable extends SQLObject implements SQLObjectListener {

	private static Logger logger = Logger.getLogger(SQLTable.class);

	protected SQLObject parent;
	protected String remarks="";
	protected String objectType;
	protected String primaryKeyName;
	protected String physicalPrimaryKeyName;
	
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
	 * 
	 * @param startPopulated The initial setting of this table's folders' <tt>populated</tt> flags.
	 * If this is set to false, the table will attempt to lazy-load the child folders.  Otherwise,
	 * this table will not try to load its children from a database connection.
	 */
	protected Folder importedKeysFolder;
	
	public SQLTable(SQLObject parent, String name, String remarks, String objectType, boolean startPopulated) throws ArchitectException {
		logger.debug("NEW TABLE "+name+"@"+hashCode());
		this.parent = parent;
		setName(name);
		this.remarks = remarks;
		this.objectType = objectType;

		this.children = new ArrayList();
		initFolders(startPopulated);

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
	public SQLTable(SQLDatabase parent, boolean startPopulated) throws ArchitectException {
		this(parent, "", "", "TABLE", startPopulated);
	}

	/**
	 * Creates a new SQLTable with no children, no parent, and all
	 * properties set to their defaults.
	 * 
	 * <p>This is mainly for code that needs to reconstruct a SQLTable
	 * from outside configuration info, such as the SwingUIProject.load() method.
	 * If you want to make SQLTable objects from scratch, consider using one
	 * of the other constructors, which initialise the state more thoroughly.
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
	public void initFolders(boolean populated) throws ArchitectException {
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
			ResultSet rs = null;
			try {
				rs = dbmd.getTables(null,
								   null,
								   "%",
								   new String[] {"TABLE", "VIEW"});
				while (rs.next()) {
					SQLObject tableParent = addTo;

					String catName = rs.getString(1);
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

					String schName = rs.getString(2);
					SQLSchema schema = null;
					if (schName != null) {
						schema = (SQLSchema) schemas.get(catName+"."+schName);
						if (schema == null) {
							if (cat == null) {
								schema = new SQLSchema(addTo, schName, false);
								addTo.children.add(schema);
							} else {
								schema = new SQLSchema(cat, schName, false);
								cat.children.add(schema);
							}
							schema.setNativeTerm(dbmd.getSchemaTerm());
							logger.debug("Set schema term to "+schema.getNativeTerm());
							schemas.put(catName+"."+schName, schema);
						}
						tableParent = schema;
					}
														  
					tableParent.children.add(new SQLTable(tableParent,
														  rs.getString(3),
														  rs.getString(5),
														  rs.getString(4),
														  false));
				}
			} finally {
				if (rs != null) rs.close();
			}
		}
	}

	/**
	 * Creates a new SQLTable under the given parent database.  The new table will have
	 * all the same properties as the given source table.  
	 * 
	 * @param source The table to copy
	 * @param parent The database to insert the new table into
	 * @return The new table
	 * @throws ArchitectException if there are populate problems on source or parent
	 * Or if the parent has children of type other than SQLTable.
	 */
	public static SQLTable getDerivedInstance(SQLTable source, SQLDatabase parent)
		throws ArchitectException {
		source.populateColumns();
		source.populateRelationships();
		SQLTable t = new SQLTable(parent, true);
		t.setName(source.getName());
		t.remarks = source.remarks;
		
		t.setPhysicalName(source.getPhysicalName());
		t.primaryKeyName = source.getPrimaryKeyName();
		t.physicalPrimaryKeyName = source.getPhysicalPrimaryKeyName();
		
		t.inherit(source);
		parent.addChild(t);
		return t;
	}
	
	private synchronized void populateColumns() throws ArchitectException {
		if (columnsFolder.isPopulated()) return;
		if (columnsFolder.children.size() > 0) throw new IllegalStateException("Can't populate table because it already contains columns");

		logger.debug("SQLTable: column populate starting");

		try {
			SQLColumn.addColumnsToTable(this,
										getCatalogName(),
										getSchemaName(),
										 getName());
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
		
		logger.debug("SQLTable: column populate finished");

	}
	
	/**
	 * Populates all the imported key relationships.  This has the
	 * side effect of populating the exported key side of the
	 * relationships for the exporting tables.
	 */
	private synchronized void populateRelationships() throws ArchitectException {
		if (!columnsFolder.isPopulated()) throw new IllegalStateException("Table must be populated before relationships are added");
		if (importedKeysFolder.isPopulated()) return;
		
		logger.debug("SQLTable: relationship populate starting");

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
		
		logger.debug("SQLTable: relationship populate finished");

	}

	/**
	 * Convenience method for getImportedKeys.addChild(r).
	 */
	public void addImportedKey(SQLRelationship r) throws ArchitectException {
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
	public void addExportedKey(SQLRelationship r) throws ArchitectException {
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
	public int getPkSize() {
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
		if (source == this)
		{
			throw new ArchitectException("Cannot inherit from self");
		}

		boolean addToPK;
		int pkSize = getPkSize();
		int sourceSize = source.getColumns().size();
		int originalSize = getColumns().size();
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

	public void inherit(int pos, SQLColumn sourceCol, boolean addToPK) throws ArchitectException {
	    if (addToPK && pos > 0 && !getColumn(pos - 1).isPrimaryKey()) {
	        throw new IllegalArgumentException("Can't inherit new PK column below a non-PK column! Insert pos="+pos+"; addToPk="+addToPK);
	    }
		SQLColumn c = SQLColumn.getDerivedInstance(sourceCol, this);
		if (addToPK) {
			c.primaryKeySeq = new Integer(1);
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
		if (populate) populateColumns();
		logger.debug("Looking for column "+colName+" in "+children);
		/* if columnsFolder.children.iterator(); gets changed to getColumns().iterator() 
		 * we get infinite recursion between populateColumns, getColumns, 
		 * getColumnsByName and addColumnsToTable
		 */
		Iterator it = columnsFolder.children.iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getName().equalsIgnoreCase(colName)) {
				logger.debug("FOUND");
				return col;
			}
		}
		logger.debug("NOT FOUND");
		return null;
	}

	public int getColumnIndex(SQLColumn col) throws ArchitectException {
		logger.debug("Looking for column index of: " + col);
		
		Iterator it = getColumns().iterator();
		
		int colIdx = 0;
		while (it.hasNext()) {
			if (it.next() == col) {
				return colIdx;
			}
			colIdx++;
		}

		logger.debug("NOT FOUND");
		return -1;
	}

	public void addColumn(SQLColumn col) throws ArchitectException {
		addColumnImpl(columnsFolder.children.size(), col);
	}

	public void addColumn(int pos, SQLColumn col) throws ArchitectException {
		addColumnImpl(pos, col);
	}
	
	private void addColumnImpl(int pos, SQLColumn col) throws ArchitectException {
		if (getColumnIndex(col) != -1) {
			col.addReference();
			return;
		}
		boolean addToPK = false;
		int pkSize = getPkSize();
		if (getColumns().size() > 0 && pos < pkSize) {
			addToPK = true;
			normalizePrimaryKey();
			for (int i = pos; i < pkSize; i++) {
				((SQLColumn) getColumns().get(i)).primaryKeySeq = new Integer(i + 1);
			}
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

	/**
	 * Connects up the columnsFolder, exportedKeysFolder, and
	 * importedKeysFolder pointers to the children at indices 0, 1,
	 * and 2 respectively.
	 */
	protected void addChildImpl(int index, SQLObject child) throws ArchitectException {
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
		super.addChildImpl(index, child);
	}

	/**
	 * Calls {@link #removeColumn(SQLColumn)} with the appropriate argument.
	 * 
	 * @throws LockedColumnException If the column is "owned" by a relationship, and cannot
	 * be safely removed.
	 * @throws ArchitectException If something goes wrong accessing the table's foreign keys 
	 */
	public void removeColumn(int index) throws ArchitectException {
		removeColumn((SQLColumn) columnsFolder.children.get(index));
	}

	/**
	 * Removes the given column if it is in this table.  If you want
	 * to change a column's, index, use the {@link
	 * #changeColumnIndex(int,int)} method because it does not throw
	 * LockedColumnException.
	 * 
	 * <p>
	 * FIXME: This should be implemented by decreasing the column's reference count.
	 * (addColumn already does increase reference count when appropriate)
	 * Then, everything that manipulates reference counts directly can just use regular
	 * addColumn and removeColumn and magic will take care of the correct behaviour!
	 * 
	 * @throws LockedColumnException If the column is "owned" by a relationship, and cannot
	 * be safely removed.
	 * @throws ArchitectException If something goes wrong accessing the table's foreign keys 
	 */
	public void removeColumn(SQLColumn col) throws ArchitectException {
		
		// a column is only locked if it is an IMPORTed key--not if it is EXPORTed.
		SQLRelationship lockingRelationship = null;
		for (SQLRelationship r : getImportedKeys()) {
			for (SQLRelationship.ColumnMapping cm : r.getMappings()) {
				if (cm.getFkColumn() == col) {
					lockingRelationship = r;
					break;
				}
			}
		}
		
		if (lockingRelationship == null) {
			columnsFolder.removeChild(col);
			normalizePrimaryKey();
		} else {
			throw new LockedColumnException(lockingRelationship);
		}
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
	public void changeColumnIndex(int oldIdx, int newIdx, boolean putInPK) throws ArchitectException {
		// remove and add the column directly, then manually fire the event.
	    // This is necessary because the relationships prevent deletion of locked keys.
 		SQLColumn col = (SQLColumn) columnsFolder.children.remove(oldIdx);
 		columnsFolder.fireDbChildRemoved(oldIdx, col);
 		columnsFolder.children.add(newIdx, col);
 		if (putInPK) {
 			col.primaryKeySeq = new Integer(1); // will get sane value when normalized
 		} else {
 			col.primaryKeySeq = null;
 		}
 		normalizePrimaryKey();
 		columnsFolder.fireDbChildInserted(newIdx, col);
	}

	/**
	 * Sets the primaryKeySeq on each child column currently in the
	 * primary key to its index in this table.
	 */
	public void normalizePrimaryKey() {
		try {
			if (getColumns().isEmpty()) return;
		
			boolean donePk = false;
			int i = 0;
			Iterator it = getColumns().iterator();
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
		} catch (ArchitectException e) {
			logger.warn("Unexpected ArchitectException in normalizePrimaryKey "+e);
		}
	}
	
	public List<SQLRelationship> keysOfColumn(SQLColumn col) throws ArchitectException {
		LinkedList<SQLRelationship> keys = new LinkedList<SQLRelationship>();
		Iterator it = getExportedKeys().iterator();
		while (it.hasNext()) {
			SQLRelationship r = (SQLRelationship) it.next();
			if (r.containsPkColumn(col)) {
				keys.add(r);
			}
		}
		it = getExportedKeys().iterator();
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



	/**
	 * The table's name.
	 */
	public String getShortDisplayName() {
		SQLSchema schema = getSchema();
		if (schema != null) {
			return schema.getName()+"."+ getName()+" ("+objectType+")";
		} else {
			if (objectType != null) {
				return  getName()+" ("+objectType+")";
			} else {
				return  getName();
			}
		}
	}

	/**
	 * Just calls populateColumns and populateRelationships.
	 */
	public void populate() throws ArchitectException {
		logger.debug("SQLTable: populate is a no-op");
	}
	
	public boolean isPopulated() {
		return true;
	}

	/**
	 * Returns true (tables have several folders as children).
	 */
	public boolean allowsChildren() {
		return true;
	}

	public Class<? extends SQLObject> getChildType() {
		return Folder.class;
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
	public static class Folder<T extends SQLObject> extends SQLObject {
		protected int type;
		protected String name;
		protected SQLTable parent;

		public static final int COLUMNS = 1;
		public static final int IMPORTED_KEYS = 2;
		public static final int EXPORTED_KEYS = 3;

		public Folder(int type, boolean populated) {
			this.populated = populated;
			this.type = type;
			this.children = new ArrayList<T>();
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
			String oldName = name;
			name = n;
			fireDbObjectChanged("name", oldName, name);
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
			if (populated) return;

			logger.debug("SQLTable.Folder["+getName()+"]: populate starting");

			try {
				if (type == COLUMNS) {
					parent.populateColumns();
				} else if (type == IMPORTED_KEYS) {
					parent.populateColumns();
					parent.populateRelationships();
				} else if (type == EXPORTED_KEYS) {
					ResultSet rs = null;
					try {
						DatabaseMetaData dbmd = parent.getParentDatabase().getConnection().getMetaData();
						rs = dbmd.getExportedKeys(parent.getCatalogName(), parent.getSchemaName(), parent.getName());
						while (rs.next()) {
							if (rs.getInt(9) != 1) {
								// just another column mapping in a relationship we've already handled
								continue;
							}
							String cat = rs.getString(5);
							String sch = rs.getString(6);
							String tab = rs.getString(7);
							SQLTable fkTable = parent.getParentDatabase().getTableByName(cat, sch, tab);
							fkTable.populateColumns();
							fkTable.populateRelationships();
						}
					} catch (SQLException ex) {
						throw new ArchitectException("Couldn't locate related tables", ex);
					} finally {
						try {
							if (rs != null) rs.close();
						} catch (SQLException ex) {
							logger.warn("Couldn't close resultset", ex);
						}
					}
				} else {
					throw new IllegalArgumentException("Unknown folder type: "+type);
				}
			} finally {
				populated = true;
			}
			
			logger.debug("SQLTable.Folder["+getName()+"]: populate finished");

		}

		protected void addChildImpl(int index, SQLObject child) throws ArchitectException {
			logger.debug("[31mAdding child "+child.getName()+" to folder "+getName()+"[0m");
			super.addChildImpl(index, child);
		}

		public String getShortDisplayName() {
			return name;
		}

		public boolean allowsChildren() {
			return true;
		}

		public String toString() {
			if (parent == null) {
				return name+" folder (no parent)";
			} else {
				return name+" folder of "+parent.getName();
			}
		}

		/**
		 * Returns the type code of this folder.
		 *
		 * @return One of COLUMNS, IMPORTED_KEYS, or EXPORTED_KEYS.
		 */
		public int getType() {
			return type;
		}

		@Override
		public Class<? extends SQLObject> getChildType() {
			return SQLColumn.class;
		}
		
		/**
		 * Returns the table's secondary mode or false if there is no parent table
		 * 
		 */
		@Override
		public boolean isSecondaryChangeMode() {
			if (parent != null) {
				return parent.isSecondaryChangeMode();
			} else {
				return false;
			}
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
						cmap.getFkColumn().removeReference(); // might delete column							
					}
				}
			} catch (ArchitectException ex) {
				logger.error("Couldn't remove orphaned FK column", ex);
			}
		}
	}

	public void dbObjectChanged(SQLObjectEvent e) {
        // we don't care
	}

	public void dbStructureChanged(SQLObjectEvent e) {
        // we don't care
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
			return catalog.getName();
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
			return schema.getName();
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
	 * Sets the table name, and also modifies the primary key name if
	 * it was previously null or set to the default of
	 * "oldTableName_pk".
	 * 
	 * @param argName The new table name.  NULL is not allowed.
	 */
	public void setName(String argName) {
		String oldName =  getName();
		fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.PROPERTY_CHANGE_GROUP_START,"Starting table name compound edit"));
		super.setName(argName);
		if (primaryKeyName == null
			|| primaryKeyName.equals("")
			|| primaryKeyName.equals(oldName+"_pk")) {
			setPrimaryKeyName( getName()+"_pk");
		}
		fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.PROPERTY_CHANGE_GROUP_END,"Ending table name compound edit"));
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
		String oldRemarks =this.remarks;
		this.remarks = argRemarks;
		fireDbObjectChanged("remarks",oldRemarks,argRemarks);
	}

	/**
	 * Gets the value of columns
	 *
	 * @return the value of columns
	 */
	public synchronized List<SQLColumn> getColumns() throws ArchitectException {
		populateColumns();
		return columnsFolder.getChildren();
	}

	/**
	 * Gets the value of importedKeys
	 *
	 * @return the value of importedKeys
	 */
	public List<SQLRelationship> getImportedKeys() throws ArchitectException {
		return this.importedKeysFolder.getChildren();
	}

	/**
	 * Gets the value of exportedKeys
	 *
	 * @return the value of exportedKeys
	 */
	public List<SQLRelationship> getExportedKeys() throws ArchitectException {
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
		String oldPrimaryKeyName = this.primaryKeyName;
		this.primaryKeyName = argPrimaryKeyName;
		fireDbObjectChanged("primaryKeyName",oldPrimaryKeyName,argPrimaryKeyName);
	}

	/**
	 * Gets the value of physicalPrimaryKeyName
	 *
	 * @return the value of physicalPrimaryKeyName
	 */
	public String getPhysicalPrimaryKeyName()  {
		return this.physicalPrimaryKeyName;
	}

	/**
	 * Sets the value of physicalPrimaryKeyName
	 *
	 * @param argPhysicalPrimaryKeyName Value to assign to this.physicalPrimaryKeyName
	 */
	public void setPhysicalPrimaryKeyName(String argPhysicalPrimaryKeyName) {
		String oldPhysicalPrimaryKeyName = this.physicalPrimaryKeyName;
		this.physicalPrimaryKeyName = argPhysicalPrimaryKeyName;
		fireDbObjectChanged("physicalPrimaryKeyName",oldPhysicalPrimaryKeyName,argPhysicalPrimaryKeyName);
	}

	
	
	/**
	 * Gets the type of table this object represents (TABLE or VIEW).
	 *
	 * @return the value of objectType
	 */
	public String getObjectType()  {
		return this.objectType;
	}

	/**
	 * Sets the type of table this object represents (TABLE or VIEW).
	 *
	 * @param argObjectType Value to assign to this.objectType
	 */
	public void setObjectType(String argObjectType) {
		String oldObjectType = this.objectType;
		this.objectType = argObjectType;
		fireDbObjectChanged("objectType",oldObjectType, argObjectType);
	}

}
