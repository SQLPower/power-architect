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
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLIndex.Column;

public class SQLTable extends SQLObject {

	private static Logger logger = Logger.getLogger(SQLTable.class);

	protected SQLObject parent;
	protected String remarks="";
	private String objectType;
	protected String physicalPrimaryKeyName;

	/**
	 * A List of SQLColumn objects which make up all the columns of
	 * this table.
	 */
	protected Folder<SQLColumn> columnsFolder;

	/**
	 * A List of SQLRelationship objects describing keys that this
	 * table exports.  This SQLTable is the "pkTable" in its exported
	 * keys.
	 */
	protected Folder<SQLRelationship> exportedKeysFolder;

	/**
	 * A container for SQLRelationship objects describing keys that this
	 * table imports.  This SQLTable is the "fkTable" in its imported
	 * keys.
	 */
	protected Folder<SQLRelationship> importedKeysFolder;

    /**
     * A container for SQLIndex objects that describe the various database indices
     * that exist on this table.
     */
    private Folder<SQLIndex> indicesFolder;

	public SQLTable(SQLObject parent, String name, String remarks, String objectType, boolean startPopulated) throws ArchitectException {
		logger.debug("NEW TABLE "+name+"@"+hashCode());
		initFolders(startPopulated);
		setup(parent, name, remarks, objectType);
	}

    /**
     * Sets up the values for the new Table
     */
    private void setup(SQLObject parent, String name, String remarks, String objectType) {
        this.parent = parent;
		super.setName(name);  // this.setName will try to be far to fancy at this point, and break stuff
		this.remarks = remarks;
		this.objectType = objectType;
		if (this.objectType == null) throw new NullPointerException();
    }

	/**
	 * Creates a new SQLTable with parent as its parent and a null
	 * schema and catalog.  The table will contain the four default
	 * folders: "Columns" "Exported Keys" "Imported Keys" and "Indices".
     *
     * @param startPopulated The initial setting of this table's folders' <tt>populated</tt> flags.
     * If this is set to false, the table will attempt to lazy-load the child folders.  Otherwise,
     * this table will not try to load its children from a database connection.
	 */
	public SQLTable(SQLDatabase parent, boolean startPopulated) throws ArchitectException {
		this(parent, "", "", "TABLE", startPopulated);
	}

	/**
	 * Creates a new SQLTable with no children, no parent, and all
	 * properties set to their defaults.   Note this should never
     * Initialize the folders.
	 *
	 * <p>This is mainly for code that needs to reconstruct a SQLTable
	 * from outside configuration info, such as the SwingUIProject.load() method.
	 * If you want to make SQLTable objects from scratch, consider using one
	 * of the other constructors, which initialise the state more thoroughly.
	 *
	 */
	public SQLTable() {
        this.children = new ArrayList();  // initFolders normally does this, but this constructor never calls it
        setup(null,null,null,"TABLE");
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
        this.children = new ArrayList();
		addChild(new Folder(Folder.COLUMNS, populated));
		addChild(new Folder(Folder.EXPORTED_KEYS, populated));
        addChild(new Folder(Folder.IMPORTED_KEYS, populated));
        addChild(new Folder(Folder.INDICES, populated));
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
        source.populateIndices();
		SQLTable t = new SQLTable(parent, true);
		t.setName(source.getName());
		t.remarks = source.remarks;

		t.setPhysicalName(source.getPhysicalName());
		t.physicalPrimaryKeyName = source.getPhysicalPrimaryKeyName();

		t.inherit(source);
        inheritIndices(source,t);
        
		parent.addChild(t);
		return t;
	}
    
    /**
     * inherit indices from the source table,
     * @param source
     * @param target    
     * @throws ArchitectException 
     */
    private static void inheritIndices(SQLTable source, SQLTable target) throws ArchitectException {
        for ( SQLIndex index : (List<SQLIndex>)source.getIndicesFolder().getChildren()) {
            SQLIndex index2 = SQLIndex.getDerivedInstance(index,target);
            target.addIndex(index2);
        }
    }

    /**
     * Populates the columns of this table from the database.  If successful, then the
     * indices will also be populated.
     * 
     * @throws ArchitectException
     */
	private synchronized void populateColumns() throws ArchitectException {
		if (columnsFolder.isPopulated()) return;
		if (columnsFolder.children.size() > 0) throw new IllegalStateException("Can't populate table because it already contains columns");

		logger.debug("column folder populate starting");

		try {
			SQLColumn.addColumnsToTable(this,
										getCatalogName(),
										getSchemaName(),
										getName());
		} catch (SQLException e) {
			throw new ArchitectException("Failed to populate columns of table "+getName(), e);
		} finally {
			columnsFolder.populated = true;
			int newSize = columnsFolder.children.size();
			int[] changedIndices = new int[newSize];
			for (int i = 0; i < newSize; i++) {
				changedIndices[i] = i;
			}
			columnsFolder.fireDbChildrenInserted(changedIndices, columnsFolder.children);
		}

		logger.debug("column folder populate finished");

        populateIndices();
	}

    /**
     * Retrieves all index information about this table from the source database
     * it is associated with.  If the index folder has already been populated, this
     * method returns immediately with no side effects.
     * 
     * <p>Note: It is essential that the columns folder of this table has been populated before calling
     * this method.
     * 
     * @throws IllegalStateException if the columns folder is not yet populated, or if the
     * index folder is both non-empty and non-populated
     */
    private synchronized void populateIndices() throws ArchitectException {
        if (indicesFolder.isPopulated()) return;
        if (indicesFolder.children.size() > 0) throw new IllegalStateException("Can't populate indices folder because it already contains children!");
        if (!columnsFolder.isPopulated()) throw new IllegalStateException("Columns folder must be populated");
        
        logger.debug("index folder populate starting");

        try {
            logger.debug("before addIndicesToTable");
            
            SQLIndex.addIndicesToTable(this,
                                      getCatalogName(),
                                      getSchemaName(),
                                      getName());
            
            logger.debug("found "+indicesFolder.children.size()+" indices.");
            boolean foundPKColumn = false;
            for (SQLColumn column : getColumns()) {
                if (column.getPrimaryKeySeq() != null) {
                    foundPKColumn = true;
                    break;
                }
            }
          
            indicesFolder.populated = true;
        } catch (SQLException e) {
            throw new ArchitectException("Failed to populate indices of table "+getName(), e);
        } finally {
            indicesFolder.populated = true;
            Collections.sort(columnsFolder.children, new SQLColumn.CompareByPKSeq());
            int newSize = indicesFolder.children.size();
            int[] changedIndices = new int[newSize];
            for (int i = 0; i < newSize; i++) {
                changedIndices[i] = i;
            }
            indicesFolder.fireDbChildrenInserted(changedIndices, indicesFolder.children);
        }
        normalizePrimaryKey();
        logger.debug("index folder populate finished");
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
	void addImportedKey(SQLRelationship r) throws ArchitectException {
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
	void addExportedKey(SQLRelationship r) throws ArchitectException {
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
	 * Populates this table then searches for the named column in a case-insensitive
     * manner.
	 */
	public SQLColumn getColumnByName(String colName) throws ArchitectException {
		return getColumnByName(colName, true, false);
	}

	/**
	 * Searches for the named column.
	 *
	 * @param populate If true, this table will retrieve its column
	 * list from the database; otherwise it just searches the current
	 * list.
	 */
	public SQLColumn getColumnByName(String colName, boolean populate, boolean caseSensitive)
        throws ArchitectException {
		if (populate) populateColumns();
		if (logger.isDebugEnabled()) {
            logger.debug("Looking for column "+colName+" in "+children);
        }
		/* if columnsFolder.children.iterator(); gets changed to getColumns().iterator()
		 * we get infinite recursion between populateColumns, getColumns,
		 * getColumnsByName and addColumnsToTable
		 */
		Iterator it = columnsFolder.children.iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
            if (caseSensitive) {
                if (col.getName().equals(colName)) {
                    logger.debug("FOUND");
                    return col;
                }
            } else {
                if (col.getName().equalsIgnoreCase(colName)) {
                    logger.debug("FOUND");
                    return col;
                }
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

		if (isMagicEnabled()) {
		    boolean addToPK = false;
		    int pkSize = getPkSize();
		    if (getColumns().size() > 0 && pos < pkSize) {
		        addToPK = true;
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
		}
		columnsFolder.addChild(pos, col);
	}

	/**
     * Adds the given SQLIndex object to this table's index folder.
	 */
    public void addIndex(SQLIndex idx) throws ArchitectException {
        getIndicesFolder().addChild(idx);
    }

	/**
	 * Connects up the columnsFolder, exportedKeysFolder,
	 * importedKeysFolder, and indicesFolder pointers to the
     * children at indices 0, 1, 2, and 3 respectively.
	 */
	protected void addChildImpl(int index, SQLObject child) throws ArchitectException {
		if (child instanceof Folder) {
			if (children.size() == 0) {
				columnsFolder = (Folder) child;
			} else if (children.size() == 1) {
				exportedKeysFolder = (Folder) child;
            } else if (children.size() == 2) {
                importedKeysFolder = (Folder) child;
            } else if (children.size() == 3) {
                indicesFolder = (Folder) child;
			} else {
				throw new UnsupportedOperationException("Can't add a 5th folder to SQLTable");
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
		if (!isMagicEnabled()) {
            columnsFolder.removeChild(col);
		} else {
		    // a column is only locked if it is an IMPORTed key--not if it is EXPORTed.
		    for (SQLRelationship r : getImportedKeys()) {
		        r.checkColumnLocked(col);
		    }
		    columnsFolder.removeChild(col);
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
        try {
            startCompoundEdit("Changing column index");
            SQLColumn col = (SQLColumn) columnsFolder.children.get(oldIdx);
            Integer oldPkSeq = col.primaryKeySeq;
            Integer interimPkSeq;
            if (putInPK) {
                interimPkSeq = new Integer(1); // will get sane value when normalized
                col.setNullable(DatabaseMetaData.columnNoNulls);
            } else {
                interimPkSeq = null;
            }
            col.primaryKeySeq = interimPkSeq;
            col.fireDbObjectChanged("primaryKeySeq", oldPkSeq, interimPkSeq);

            columnsFolder.children.remove(oldIdx);
            columnsFolder.fireDbChildRemoved(oldIdx, col);
            columnsFolder.children.add(newIdx, col);
            columnsFolder.fireDbChildInserted(newIdx, col);

            normalizePrimaryKey();
        } finally {
            endCompoundEdit("Changing column index");
        }
	}

	/**
	 * Sets the primaryKeySeq on each child column currently in the
	 * primary key to its index in this table.
     * 
     * Assumptions:  All child columns in the primary key are sequential starting
     *                      from child 0.
     *               All child columns that are in the primary key have a nnon-null primary key seq
     *               
	 * @throws ArchitectException
	 */
	public void normalizePrimaryKey() throws ArchitectException {
		try {
            startCompoundEdit("Normalizing Primary Key");
            if (getColumns().isEmpty()) return;
            
            if (getPrimaryKeyIndex() == null) {
                SQLIndex pkIndex = new SQLIndex(getName()+"_pk", true, null, SQLIndex.IndexType.CLUSTERED,null);
                addIndex(pkIndex);
                pkIndex.setPrimaryKeyIndex(true);
                logger.debug("new pkIndex.getChildCount()="+pkIndex.getChildCount());
            }
            
            boolean donePk = false;
            int i = 0;
            Iterator it = getColumns().iterator();
            while (it.hasNext()) {
                SQLColumn col = (SQLColumn) it.next();
                if (col.getPrimaryKeySeq() == null) donePk = true;
                Integer oldValue = col.getPrimaryKeySeq();
                Integer newValue;
                if (!donePk) {
                    newValue = new Integer(i);
                } else {
                    newValue = null;
                }
                col.primaryKeySeq = newValue;
                col.fireDbObjectChanged("primaryKeySeq", oldValue, newValue);
                i++;
            }
		} catch (ArchitectException e) {
		    logger.warn("Unexpected ArchitectException in normalizePrimaryKey "+ e);
		    throw e;
        } finally {
		    SQLIndex pkIndex = getPrimaryKeyIndex();
		    if (pkIndex != null ) {
                Map<SQLColumn, Column> oldColumnInstances = new HashMap<SQLColumn, Column>();
		        while (pkIndex.getChildCount() > 0) {
		            Column child = (Column) pkIndex.removeChild(0);
                    oldColumnInstances.put(child.getColumn(),child);
		        }
		        Iterator it = getColumns().iterator();
		        while (it.hasNext()) {
		            SQLColumn col = (SQLColumn) it.next();
		            if (col.getPrimaryKeySeq() == null) break;
                    if (oldColumnInstances.get(col) != null){
                        pkIndex.addChild(oldColumnInstances.get(col));
                    } else {
                        pkIndex.addIndexColumn(col,false,false);
                    }
		        }
		        if (pkIndex.getChildCount() == 0) {
		            getIndicesFolder().removeChild(pkIndex);

		        }
            }
		    endCompoundEdit("Normalizing Primary Key");
		}
        if (logger.isDebugEnabled()) {
            logger.debug("----Normalize Results----");
            for (SQLColumn col : getColumns()) {
                logger.debug("Column Name " + col.getName() + " Key Sequence" +col.getPrimaryKeySeq() );
            }
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

	//TODO XXX Sql object should be doing this when we add generics
	protected void setParent(SQLObject newParent) {
		logger.debug("Setting "+getName()+"'s parent to "+ newParent);
		if (parent == newParent) return;
		SQLObject oldVal = parent;
		parent = newParent;
		fireDbObjectChanged("parent",oldVal,parent);
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
	 * Since SQLTable is just a container for Folders, there is no special populate
     * step.  The various populate operations (columns, keys, indices) are triggered
     * by visiting the individual folders.
	 */
	public void populate() throws ArchitectException {
		// SQLTable: populate is a no-op
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
        public static final int INDICES = 4;

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
            } else if (type == INDICES) {
                name = "Indices";
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

		public SQLTable getParent() {
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
					Connection con = null;
					try {
						con = parent.getParentDatabase().getConnection();
						DatabaseMetaData dbmd = con.getMetaData();
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
						try {
							if (con != null) con.close();
						} catch (SQLException ex) {
							logger.warn("Couldn't close resultset", ex);
						}
					}
                } else if (type == INDICES) {
                    parent.populateColumns();
                    parent.populateIndices();
				} else {
					throw new IllegalArgumentException("Unknown folder type: "+type);
				}
			} finally {
				populated = true;
			}

			logger.debug("SQLTable.Folder["+getName()+"]: populate finished");

		}

		protected void addChildImpl(int index, SQLObject child) throws ArchitectException {
			logger.debug("[31mAdding child "+child.getName()+" to folder "+getName()+"[0m" );
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

	public Folder<SQLColumn> getColumnsFolder() {
		return columnsFolder;
	}

	public Folder<SQLRelationship> getImportedKeysFolder() {
		return importedKeysFolder;
	}

	public Folder<SQLRelationship> getExportedKeysFolder() {
		return exportedKeysFolder;
	}

    public Folder<SQLIndex> getIndicesFolder() {
        return indicesFolder;
    }

	/**
	 * Sets the table name, and also modifies the primary key name if
	 * it was previously null or set to the default of
	 * "oldTableName_pk".
	 *
	 * @param argName The new table name.  NULL is not allowed.
	 */
	public void setName(String argName) {

        // this method can be called very early in a SQLTable's life,
        // before its indices folder exists.  Therefore, we have to
        // be careful not to look up the primary key before one exists.

        if ( (!isMagicEnabled()) || (indicesFolder == null) ) {
            super.setName(argName);
        } else try {
            String oldName = getName();
            startCompoundEdit("Table Name Change");
            super.setName(argName);
            SQLIndex primaryKeyIndex = getPrimaryKeyIndex();
            if (argName != null &&
                primaryKeyIndex != null && 
               (getPrimaryKeyName() == null
                    || "".equals(getPrimaryKeyName())
                    || (oldName+"_pk").equals(getPrimaryKeyName())) ) {
                primaryKeyIndex.setName(getName()+"_pk");
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        } finally {
            endCompoundEdit("Ending table name compound edit");
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
		String oldRemarks = this.remarks;
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
     * Gets the value of exportedKeys by name
     *
     * @return the value of exportedKeys
     */
    public SQLRelationship getExportedKeyByName(String name) throws ArchitectException {
        return getExportedKeyByName(name,true);
    }

    /**
     * Gets the value of exportedKeys by name
     *
     * @return the value of exportedKeys
     */
    public SQLRelationship getExportedKeyByName(String name,
            boolean populate ) throws ArchitectException {
        if (populate) populateRelationships();
        logger.debug("Looking for Exported Key ["+name+"] in "+exportedKeysFolder.getChildren() );
        Iterator it = exportedKeysFolder.children.iterator();
        while (it.hasNext()) {
            SQLRelationship r = (SQLRelationship) it.next();
            if (r.getName().equalsIgnoreCase(name)) {
                logger.debug("FOUND");
                return r;
            }
        }
        logger.debug("NOT FOUND");
        return null;
    }

    /**
     * Gets a list of unique indices
     */
    public synchronized List<SQLIndex> getUniqueIndices() throws ArchitectException {
        populateColumns();
        populateIndices();
        List<SQLIndex> list = new ArrayList<SQLIndex>();
        Iterator it = getIndicesFolder().children.iterator();
        while (it.hasNext()) {
            SQLIndex idx = (SQLIndex) it.next();
            if (idx.isUnique() ) {
                list.add(idx);
            }
        }
        return list;
    }

    /**
     * Gets the value of index by name
     *
     * @return the value of index
     */
    public SQLIndex getIndexByName(String name) throws ArchitectException {
        return getIndexByName(name,true);
    }

    /**
     * Gets the value of index by name
     *
     * @return the value of index
     */
    public SQLIndex getIndexByName(String name,
            boolean populate ) throws ArchitectException {
        if (populate) {
            populateColumns();
            populateIndices();
        }
        logger.debug("Looking for Index ["+name+"] in "+getIndicesFolder().children);
        Iterator it = getIndicesFolder().children.iterator();
        while (it.hasNext()) {
            SQLIndex idx = (SQLIndex) it.next();
            if (idx.getName().equalsIgnoreCase(name)) {
                logger.debug("FOUND");
                return idx;
            }
        }
        logger.debug("NOT FOUND");
        return null;
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
	 * Gets the name of this table's Primary Key index if it has one, otherwise
     * returns null.
	 * @throws ArchitectException 
	 */
	public String getPrimaryKeyName() throws ArchitectException  {
        SQLIndex primaryKeyIndex = getPrimaryKeyIndex();
		return primaryKeyIndex == null ? null : primaryKeyIndex.getName();
	}
  
	/**
	 * Gets the value of physicalPrimaryKeyName
	 *
	 * @return the value of physicalPrimaryKeyName
	 * @throws ArchitectException 
	 */
	public String getPhysicalPrimaryKeyName() throws ArchitectException  {
        SQLIndex primaryKeyIndex = getPrimaryKeyIndex();
        return primaryKeyIndex == null ? null : primaryKeyIndex.getPhysicalName();
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
        if (this.objectType == null) throw new NullPointerException();
		fireDbObjectChanged("objectType",oldObjectType, argObjectType);
	}

    /**
     * Returns the primary key for this table, or null if none exists.
     * 
     * @throws ArchitectException
     */
    public SQLIndex getPrimaryKeyIndex() throws ArchitectException {
        for (SQLIndex i : (List<SQLIndex>)getIndicesFolder().getChildren()){
            if (i.isPrimaryKeyIndex()) return i;
        }
        return null;
    }

    /**
     * Retrieves all of the table names for the given catalog, schema
     * in the container's database using DatabaseMetaData.  This method
     * is a subroutine of the populate() methods in SQLDatabase, SQLCatalog,
     * and SQLSchema.
     * <p>
     * Important Note: This method adds the tables directly to the parent's
     * children list.  No SQLObjectEvents will be generated.  Calling code
     * has to do this at the appropriate time, when it's safe to do so. 
     * 
     * @param container The container that will be the direct parent of
     * all tables created by this method call.
     * @param dbmd The DatabaseMetaData for the parent database in question.
     * The fact that you have to pass it in is just an optimization: all
     * the places from which this method gets called already have an instance
     * of DatabaseMetaData ready to go.
     */
    static void addTablesToTableContainer(
            SQLObject container, DatabaseMetaData dbmd,
            String catalogName, String schemaName)
    throws ArchitectException, SQLException {
        ResultSet rs = null;
        try {
            rs = dbmd.getTables(catalogName,
                    schemaName,
                    "%",
                    new String[] {"TABLE", "VIEW"});

            while (rs.next()) {
                container.children.add(new SQLTable(container,
                        rs.getString(3),
                        rs.getString(5),
                        rs.getString(4),
                        false));
            }
            rs.close();
            rs = null;
        } finally {
            if (rs != null) rs.close();
        }
    }
}