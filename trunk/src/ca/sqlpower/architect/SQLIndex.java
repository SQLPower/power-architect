/*
 * Created September 28, 2006.
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable.Folder;

/**
 * The SQLIndex class represents an index on a table in a relational database.
 *
 * @author fuerth
 */
public class SQLIndex extends SQLObject {

    private static final Logger logger = Logger.getLogger(SQLIndex.class);

    /**
     * An enumeration of the types of indices that JDBC recognises.
     */
    public static enum IndexType {
        /**
         * Table statistics that are returned in conjuction with a table's index descriptions.
         */
        STATISTIC(DatabaseMetaData.tableIndexStatistic),

        /**
         * A clustered index.
         */
        CLUSTERED(DatabaseMetaData.tableIndexClustered),

        /**
         * A hashed index.
         */
        HASHED(DatabaseMetaData.tableIndexHashed),

        /**
         * An index that is not clustered or hashed, or table statisics.
         */
        OTHER(DatabaseMetaData.tableIndexOther);

        private short jdbcType;

        IndexType(short jdbcType) {
            this.jdbcType = jdbcType;
        }

        /**
         * Returns this index type's JDBC code (one of
         * DatabaseMetaData.tableIndexStatistic,
         * DatabaseMetaData.tableIndexClustered,
         * DatabaseMetaData.tableIndexHashed, or
         * DatabaseMetaData.tableIndexOther).
         */
        public short getJdbcType() {
            return jdbcType;
        }

        public static IndexType forJdbcType(short jdbcType) {
            if (jdbcType == DatabaseMetaData.tableIndexStatistic) return STATISTIC;
            if (jdbcType == DatabaseMetaData.tableIndexClustered) return CLUSTERED;
            if (jdbcType == DatabaseMetaData.tableIndexHashed) return HASHED;
            if (jdbcType == DatabaseMetaData.tableIndexOther) return OTHER;
            throw new IllegalArgumentException("Unknown JDBC index type code: " + jdbcType);
        }
    }

    /**
     * A simple placeholder for a column.  We're not using real SQLColumn instances here so that the
     * tree of SQLObjects can remain tree-like.  If we put the real SQLColumns in here, the columns
     * would appear in two places in the tree (here and under the table's columns folder)!
     */
    public class Column extends SQLObject {

        /**
         * Small class for reacting to changes in this index columns's
         * target SQLColumn (if it has one at all). 
         */
        private class TargetColumnListener implements SQLObjectListener {

            public void dbChildrenInserted(SQLObjectEvent e) {
                // won't happen to a column
            }

            public void dbChildrenRemoved(SQLObjectEvent e) {
                // won't happen to a column
            }

            /**
             * Updates the index column name to match the new value in this
             * event, if the event is a name change from the target SQLColumn.
             * The process of doing the update will cause the SQLIndex.Column
             * object to fire an event of its own. 
             */
            public void dbObjectChanged(SQLObjectEvent e) {
                if ("name".equals(e.getPropertyName())) {
                    setName((String) e.getNewValue());
                }
            }

            /**
             * Refires structure change events from the target SQLColumn.
             */
            public void dbStructureChanged(SQLObjectEvent e) {
                Column.this.fireDbStructureChanged();
            }
            
            @Override
            public String toString() {
                StringBuffer buf = new StringBuffer();
                buf.append(SQLIndex.this.getName());
                buf.append(".");
                buf.append(Column.this.getName());
                buf.append(".");
                buf.append("TargetColumnListener");
                buf.append(" isPrimarykey?");
                buf.append(SQLIndex.this.primaryKeyIndex);
                return buf.toString();
            }
        }
        
        /**
         * The column in the table that this index column represents. Might be
         * null if this index column represents an expression rather than a
         * single column value.
         */
        private SQLColumn column;

        /**
         * Indicates if this index applies to ascending values.
         */
        private boolean ascending;

        /**
         * Indicates if this index applies to descending values.
         */
        private boolean descending;

        /**
         * A proxy that refires certain events on the target column.
         * 
         * <p>It is the job of {@link #setColumn(SQLColumn)} to keep this
         * listener hooked up to the correct SQLColumn object (or completely
         * disconnected in the case that there is no target SQLColumn).
         */
        private final TargetColumnListener targetColumnListener = new TargetColumnListener();
        
        /**
         * Creates a Column object that corresponds to a particular SQLColumn.
         */
        public Column(SQLColumn col, boolean ascending, boolean descending) {
            this(col.getName(), ascending, descending);
            setColumn(col);
        }

        /**
         * Creates a Column object that does not correspond to a particular column
         * (such as an expression index).
         */
        public Column(String name, boolean ascending, boolean descending) {
            children = Collections.emptyList();
            setName(name);

            this.ascending = ascending;
            this.descending = descending;
        }

        public Column() {
            this((String) null, false, false);
        }

        @Override
        public boolean allowsChildren() {
            return false;
        }

        @Override
        public Class<? extends SQLObject> getChildType() {
            return null;
        }

        @Override
        public SQLObject getParent() {
            return SQLIndex.this;
        }

        @Override
        public String getShortDisplayName() {
            return getName();
        }

        @Override
        protected void populate() throws ArchitectException {
            // nothing to do
        }

        @Override
        public boolean isPopulated() {
            return true;
        }

        @Override
        protected void setParent(SQLObject parent) {
            if (parent != null && parent != SQLIndex.this) {
                throw new UnsupportedOperationException("You can't change an Index.Column's parent");
            }
        }

        public SQLColumn getColumn() {
            return column;
        }

        public void setColumn(SQLColumn column) {
            if (this.column != null) {
                this.column.removeSQLObjectListener(targetColumnListener);
            }
            SQLColumn oldValue = this.column;
            this.column = column;
            if (this.column != null) {
                this.column.addSQLObjectListener(targetColumnListener);
            }
            fireDbObjectChanged("column", oldValue, column);
        }

        public boolean isAscending() {
            return ascending;
        }

        public void setAscending(boolean ascending) {
            boolean oldValue = this.ascending;
            this.ascending = ascending;
            fireDbObjectChanged("ascending", oldValue, ascending);
        }

        public boolean isDescending() {
            return descending;
        }

        public void setDescending(boolean descending) {
            boolean oldValue = this.descending;
            this.descending = descending;
            fireDbObjectChanged("descending", oldValue, descending);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * The parent folder that owns this index object.
     */
    private SQLTable.Folder<SQLIndex> parent;

    /**
     * Flags whether or not this index enforces uniqueness.
     */
    private boolean unique;

    /**
     * The qualifier that must be used for referring to this index in the database.  This is
     * usually the name of the table the index belongs to (in the case of SQL Server), or null
     * (in the case of Oracle).
     */
    private String qualifier;

    /**
     * The type of this index.
     */
    private IndexType type;

    /**
     * The filter condition on this index, if any.  According to the ODBC programmer's reference,
     * this is probably a property of the index as a whole (as opposed to the individual index columns),
     * but it doesn't say that explicitly.  According to the JDBC spec, this could be anything at all.
     */
    private String filterCondition;

    private boolean primaryKeyIndex;

    public SQLIndex(String name, boolean unique, String qualifier, IndexType type, String filter) {
        this();
        setName(name);
        this.unique = unique;
        this.qualifier = qualifier;
        this.type = type;
        this.filterCondition = filter;
    }

    public SQLIndex() {
        children = new ArrayList();
        primaryKeyIndex = false;
    }
    
    /**
     * Copy constructor for a sql index
     * @param oldIndex
     * @throws ArchitectException
     */
    public SQLIndex(SQLIndex oldIndex) throws ArchitectException{
        this();
        setName(oldIndex.getName());
        unique = oldIndex.unique;
        parent = oldIndex.parent;
        populated = oldIndex.populated;
        type = oldIndex.type;
        filterCondition = oldIndex.filterCondition;
        qualifier = oldIndex.qualifier;
        for (Object c: oldIndex.getChildren()){
            Column oldCol = (Column) c;
            Column newCol = new Column();
            newCol.setAscending(oldCol.ascending);
            newCol.setDescending(oldCol.descending);
            newCol.column = oldCol.column;
            newCol.setName(oldCol.getName());
            addChild(newCol);
        }
    }

    /**
     * Indices are associated with one or more table columns.  The children of this index represent those columns,
     * and the order in which the index applies to them.
     */
    @Override
    public boolean allowsChildren() {
        return true;
    }

    @Override
    public Class<? extends SQLObject> getChildType() {
        return Column.class;
    }

    /**
     * Overriden to narrow return type.
     */
    @Override
    public Column getChild(int index) throws ArchitectException {
        return (Column) super.getChild(index);
    }
    
    /**
     * Returns the table folder that owns this index.
     */
    @Override
    public SQLTable.Folder<SQLIndex> getParent() {
        return parent;
    }

    /**
     * Returns this index's parent table, or null if this index
     * is not attached to a parent table.
     */
    public SQLTable getParentTable() {
        SQLTable.Folder<SQLIndex> parent = getParent();
        if (parent == null) {
            return null;
        } else {
            return parent.getParent();
        }
    }
    @Override
    public String getShortDisplayName() {
        return getName();
    }

    /**
     * Indices are populated when first created, so populate is a no-op.
     */
    @Override
    protected void populate() throws ArchitectException {
        // nothing to do
    }

    @Override
    public boolean isPopulated() {
        return true;
    }

    /**
     * Updates this index's parent reference.
     *
     *  @param parent must be a SQLTable.Folder instance.
     */
    @Override
    protected void setParent(SQLObject parent) {
        this.parent = (Folder<SQLIndex>) parent;
    }

    @Override
    protected SQLObject removeImpl(int index) {
        Column c = (Column) children.get(index);
        if (c.getColumn() != null) {
            c.getColumn().removeSQLObjectListener(c.targetColumnListener);
        }
        return super.removeImpl(index);
    }

    @Override
    protected void addChildImpl(int index, SQLObject newChild) throws ArchitectException {
        super.addChildImpl(index, newChild);
        Column c = (Column) newChild;
        if (c.getColumn() != null) {
            // this will be redundant in some cases, but the addSQLObjectListener method
            // checks for adding duplicate listeners and does nothing in that case
            c.getColumn().addSQLObjectListener(c.targetColumnListener);
        }
    }
    
    public String getFilterCondition() {
        return filterCondition;
    }

    public void setFilterCondition(String filterCondition) {
        String oldValue = this.filterCondition;
        this.filterCondition = filterCondition;
        fireDbObjectChanged("filterCondition", oldValue, filterCondition);
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        String oldValue = this.qualifier;
        this.qualifier = qualifier;
        fireDbObjectChanged("qualifier", oldValue, qualifier);
    }

    public IndexType getType() {
        return type;
    }

    public void setType(IndexType type) {
        IndexType oldValue = this.type;
        this.type = type;
        fireDbObjectChanged("type", oldValue, type);
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        boolean oldValue = this.unique;
        this.unique = unique;
        fireDbObjectChanged("unique", oldValue, unique);
    }

    public void setParent(SQLTable.Folder<SQLIndex> parent) {
        this.parent = parent;
    }

    /**
     * Mainly for use by SQLTable's populate method.  Does not cause
     * SQLObjectEvents to avoid infinite recursion, so you have to
     * generate them yourself at a safe time.
     */
    static void addIndicesToTable(SQLTable addTo,
                                  String catalog,
                                  String schema,
                                  String tableName)
        throws SQLException, ArchitectException {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = addTo.getParentDatabase().getConnection();
            DatabaseMetaData dbmd = con.getMetaData();
            
            String pkName = null;
            rs = dbmd.getPrimaryKeys(catalog, schema, tableName);
            while (rs.next()) {
                SQLColumn col = addTo.getColumnByName(rs.getString(4), false, true);
                //logger.debug(rs.getString(4));
                if (col != null ){
                    col.primaryKeySeq = new Integer(rs.getInt(5));
                    String pkNameCheck =rs.getString(6);
                    if (pkName == null) {
                        pkName = pkNameCheck;
                    } else if (!pkName.equals(pkNameCheck)) {
                        throw new IllegalStateException("The PK name has changed somehow while adding indices to table");
                    }
                } else {
                    throw new SQLException("Column "+rs.getString(4)+ " not found in "+addTo);
                }
            }
            rs.close();
            rs = null;
            
            
            logger.debug("SQLIndex.addIndicesToTable: catalog=" + catalog + 
                    "; schema=" + schema + 
                    "; tableName="+tableName +
                    "; primary key name=" + pkName);
            SQLIndex idx = null;
            rs = dbmd.getIndexInfo(catalog, schema, tableName, false, true);
            while (rs.next()) {
                /*
                 * DatabaseMetadata result set columns:
                 *
                1  TABLE_CAT String => table catalog (may be null)
                2  TABLE_SCHEM String => table schema (may be null)
                3  TABLE_NAME String => table name
                4  NON_UNIQUE boolean => Can index values be non-unique. false when TYPE is tableIndexStatistic
                5  INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is tableIndexStatistic
                6  INDEX_NAME String => index name; null when TYPE is tableIndexStatistic
                7  TYPE short => index type:
                      tableIndexStatistic - this identifies table statistics that are returned in conjuction with a table's index descriptions
                      tableIndexClustered - this is a clustered index
                      tableIndexHashed - this is a hashed index
                      tableIndexOther - this is some other style of index
                8  ORDINAL_POSITION short => column sequence number within index; zero when TYPE is tableIndexStatistic
                9  COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic
                10 ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
                11 CARDINALITY int => When TYPE is tableIndexStatistic, then this is the number of rows in the table; otherwise, it is the number of unique values in the index.
                12 PAGES int => When TYPE is tableIndexStatisic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index.
                13 FILTER_CONDITION String => Filter condition, if any. (may be null)
                 */
                boolean nonUnique = rs.getBoolean(4);
                String qualifier = rs.getString(5);
                String name = rs.getString(6);
                IndexType type = IndexType.forJdbcType(rs.getShort(7));
                int pos = rs.getInt(8);
                String colName = rs.getString(9);
                String ascDesc = rs.getString(10);
                boolean ascending = (ascDesc != null && ascDesc.equals("A"));
                boolean descending = (ascDesc != null && ascDesc.equals("D"));
                String filter = rs.getString(13);

                if (pos == 0) {
                    // this is just the table stats, not an index
                    continue;
                } else if (pos == 1) {
                    logger.debug("Found index "+name);
                    idx = new SQLIndex(name, !nonUnique, qualifier, type, filter);
                    addTo.getIndicesFolder().children.add(idx);
                    if (name.equals(pkName)) {
                        idx.setPrimaryKeyIndex(true);
                    }
                }

                logger.debug("Adding column "+colName+" to index "+idx.getName());

                Column col;
                if (addTo.getColumnByName(colName, false, true) != null) {
                    col = idx.new Column(addTo.getColumnByName(colName, false, true), ascending, descending);
                } else {
                    col = idx.new Column(colName, ascending, descending);  // probably an expression like "col1+col2"
                }

                idx.children.add(col); // direct access avoids possible recursive SQLObjectEvents
            }
            rs.close();
            rs = null;

            
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ex) {
                logger.error("Couldn't close result set", ex);
            }
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                logger.error("Couldn't close connection", ex);
            }
        }
    }

    public boolean isPrimaryKeyIndex() {
        return primaryKeyIndex;
    }
    
    /**
     * Updates whether this index is a primary key
     * 
     * set this index as primary key index and remove any old primary key
     * if isPrimaryKey is true.  Otherwise, sets primaryKeyIndex to false and 
     * removes it from its parent table.
     * 
     * @param isPrimaryKey
     */
    public void setPrimaryKeyIndex(boolean isPrimaryKey) throws ArchitectException {
        boolean oldValue = this.primaryKeyIndex;
        if (oldValue == isPrimaryKey) return;
        try {
            startCompoundEdit("Make index a Primary Key");
            if (isPrimaryKey) {
                SQLTable parentTable = getParentTable();
                if (parentTable != null) {
                    SQLIndex i = parentTable.getPrimaryKeyIndex();
                    if (i != null && i != this) {
                        i.setPrimaryKeyIndex(false);
                    }
                }
            }
            primaryKeyIndex = isPrimaryKey;
            fireDbObjectChanged("primaryKeyIndex", oldValue, isPrimaryKey);
        } finally {
            endCompoundEdit("Make index a Primary Key");
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public void addIndexColumn(SQLColumn col1, boolean ascending, boolean descending) throws ArchitectException {
        Column col = new Column(col1,ascending,descending);
        addChild(col);
    }

    public static SQLIndex getDerivedInstance(SQLIndex source, SQLTable parenTable) throws ArchitectException {
        
        SQLIndex index = new SQLIndex();
        index.setName(source.getName());
        index.setUnique(source.isUnique());
        index.setPopulated(source.isPopulated());
        index.setType(source.getType());
        index.setFilterCondition(source.getFilterCondition());
        index.setQualifier(source.getQualifier());
        index.setPrimaryKeyIndex(source.isPrimaryKeyIndex());
        
        for (Column column : (List<Column>)source.getChildren()) {
            SQLColumn sqlColumn = parenTable.getColumnByName(column.getColumn().getName());
            if ( sqlColumn == null ) {
                throw new ArchitectException("Can not derive instance, because coulmn " +
                        column.getColumn().getName() + "is not found in parent table [" +
                        parenTable.getName() + "]");
            }
            Column newColumn = index.new Column(sqlColumn,column.isAscending(),column.isDescending());
            index.addChild(newColumn);
        }
        return index;
    }




}