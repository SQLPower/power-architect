/*
 * Created on May 10, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.*;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

/**
 * A ConflictResolver performs "seek" and "destroy" operations on objects in
 * existing databases whose names conflict with the execution of a DDL script.
 * 
 * <p>
 * To use a conflict resolver, just use the public constructor to point it at an
 * existing database and give it your DDL script. After that, you must call 
 * findConflicts() to set up the internal state of the object.  Finally, you can 
 * then examine the conflicts that were found, and choose to run the {@link
 * #dropConflicting()} method.
 * 
 * @author fuerth
 */
public class ConflictResolver {
    private static final Logger logger = Logger.getLogger(ConflictResolver.class);

    /**
     * A Conflict represents an existing database object which needs to be
     * removed for some operation to continue. 
     */
    public class Conflict {
        private String type;
        private String catalog;
        private String schema;
        private String name;
        private List dependants;
        private String sqlDropStatement;

        /**
         * @param type
         * @param catalog
         * @param schema
         * @param name
         */
        public Conflict(String type, String catalog, String schema, String name) {
            super();
            this.type = type;
            this.catalog = catalog;
            this.schema = schema;
            this.name = name;
            this.dependants = new ArrayList();
        }
        
        public String getQualifiedName() {
            return DDLUtils.toQualifiedName(catalog, schema, name);
        }
        
        /**
         * Adds the dependant imported and exported key relationships which must
         * be dropped before this conflict can be dropped. 
         */
        public void addTableDependants() throws SQLException {
            ResultSet ikrs = dbmd.getImportedKeys(getCatalog(), getSchema(), getName());
            addDependantsFromKeys(ikrs);
            ikrs.close();
            
            ikrs = dbmd.getExportedKeys(getCatalog(), getSchema(), getName());
            addDependantsFromKeys(ikrs);
            ikrs.close();
        }

        /**
         * A subrountine of addTableDependants().  It adds dependant objects from 
         * a DatabaseMetaData imported keys or exported keys resultset. 
         * @param rs
         * @throws SQLException
         */
        private void addDependantsFromKeys(ResultSet rs) throws SQLException {
            Conflict prev = null;
            while (rs.next()) {
                Conflict c = new Conflict("FOREIGN KEY",
                        rs.getString("FKTABLE_CAT"),
                        rs.getString("FKTABLE_SCHEM"),
                        rs.getString("FK_NAME"));

                // multi-column keys get multiple rows in this result set.  We need to skip 'em.
                if (c.equals(prev)) continue;
                prev = c;
                
                c.setSqlDropStatement(
                        ddlg.makeDropForeignKeySQL(c.getCatalog(), c.getSchema(),
                                                   rs.getString("FKTABLE_NAME"), c.getName()));
                dependants.add(c);
            }
        }
        
        public String toString() {
            return getType()+" "+getQualifiedName();
        }
        public void setDependants(List v) {
            this.dependants = v;
        }
        public List getDependants() {
            return dependants;
        }
        public String getName() {
            return name;
        }
        public String getType() {
            return type;
        }
        public String getCatalog() {
            return catalog;
        }
        public String getSchema() {
            return schema;
        }
        public String getSqlDropStatement() {
            return sqlDropStatement;
        }
        public void setSqlDropStatement(String sqlDropStatement) {
            this.sqlDropStatement = sqlDropStatement;
        }
        
        /**
         * Compares the type, catalog, schema, and name fields, and considers this Conflics
         * equal to the other Conflict if all those fields are themselves equal.
         */
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (! (obj instanceof Conflict)) return false;
            Conflict other = (Conflict) obj;
            return (ArchitectUtils.areEqual(this.type, other.type)
                    && ArchitectUtils.areEqual(this.catalog, other.catalog)
                    && ArchitectUtils.areEqual(this.schema, other.schema)
                    && ArchitectUtils.areEqual(this.name, other.name));
        }
        
        /**
         * A mediocre hash function for combining the same fields compared in the equals method.
         */
        public int hashCode() {
            int hash = 1;
            hash += 31*hash + (type == null ? 0 : type.hashCode());
            hash += 31*hash + (catalog == null ? 0 : catalog.hashCode());
            hash += 31*hash + (schema == null ? 0 : schema.hashCode());
            hash += 31*hash + (name == null ? 0 : name.hashCode());
            return hash;
        }
    }
    
    private Connection con;
    private DatabaseMetaData dbmd;
    private List ddlStatements;
    private List conflicts;
    private String lastSQLStatement;
    private DDLGenerator ddlg;
    
    /**
     * Creates a new ConflictResolver.  You should call findConflicting() after you get
     * this new object.
     * 
     * @param con
     * @param ddlStatements
     */
    public ConflictResolver(Connection con, DDLGenerator ddlg, List ddlStatements) throws SQLException {
        this.con = con;
        this.ddlg = ddlg;
        this.ddlStatements = ddlStatements;
        this.dbmd = con.getMetaData();
    }

    /**
     * Searches for objects in the database pointed to by con that would
     * conflict with the execution of any of the given DDL statements.
     * 
     * @return a list of object names that need to be removed before ddlStmt
     *         will succeed.
     */
    public void findConflicting() throws SQLException {
        conflicts = new ArrayList();
        
        if (logger.isDebugEnabled()) {
            logger.debug("About to find conflicting objects for DDL Script: "+ddlStatements);
        }

        Iterator it = ddlStatements.iterator();
        while (it.hasNext()) {
            DDLStatement ddlStmt = (DDLStatement) it.next();
            if (ddlStmt.getType() != DDLStatement.StatementType.CREATE) continue;
            SQLObject so = ddlStmt.getObject();
            Class clazz = so.getClass();
            if (clazz.equals(SQLTable.class)) {
                SQLTable t = (SQLTable) so;
                String cat = ddlStmt.getTargetCatalog();
                String sch = ddlStmt.getTargetSchema();
                if (logger.isDebugEnabled()) {
                    logger.debug("Finding conflicts for TABLE '" + cat + "'.'"
                            + sch + "'.'" + t.getName() + "'");
                }
                ResultSet rs = dbmd.getTables(
                        ddlg.toIdentifier(cat),
                        ddlg.toIdentifier(sch),
                        ddlg.toIdentifier(t.getName()),
                        null);
                while (rs.next()) {
                    Conflict c = new Conflict(
                            rs.getString("TABLE_TYPE"),
                            rs.getString("TABLE_CAT"),
                            rs.getString("TABLE_SCHEM"),
                            rs.getString("TABLE_NAME"));
                    c.setSqlDropStatement(ddlg.makeDropTableSQL(c.getCatalog(), c.getSchema(), c.getName()));
                    List dependants = new ArrayList();
                    c.addTableDependants();
                    conflicts.add(c);
                }
                rs.close();
            } else if (clazz.equals(SQLRelationship.class)) {
                logger.error("Relationship conflicts are not supported yet!");
            } else {
                throw new IllegalArgumentException(
                        "Unknown subclass of SQLObject: " + clazz.getName());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found conflicts: " + conflicts);
        }
    }

    /**
     * Drops the conflicting objects which findConflicting() found in the target database.
     * 
     * @throws SQLException
     */
    public void dropConflicting() throws SQLException {
        if (conflicts == null) {
            throw new IllegalStateException("You have to call findConflicting() before dropConflicting()");
        }
        Iterator it = conflicts.iterator();
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            Set alreadyDropped = new HashSet();
            while (it.hasNext()) {
                Conflict c = (Conflict) it.next();
                dropConflict(c, stmt, alreadyDropped);
            }
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    
    /**
     * Recursively drops the given conflict and all its dependencies.  This is a 
     * subroutine of the public dropConflicting() method.
     * @param c
     * @param stmt
     */
    private void dropConflict(Conflict c, Statement stmt, Set alreadyDropped) throws SQLException {
        
        Iterator it = c.getDependants().iterator();
        while (it.hasNext()) {
            Conflict c2 = (Conflict) it.next();
            dropConflict(c2, stmt, alreadyDropped);
        }

        if (!alreadyDropped.contains(c)) {
            alreadyDropped.add(c);
            
            if (logger.isDebugEnabled())
                logger.debug("Dropping conflict "+c+" with SQL: "+c.getSqlDropStatement());

            lastSQLStatement = c.getSqlDropStatement();
            stmt.executeUpdate(c.getSqlDropStatement());
        }
    }

    /**
     * @return True if and only if no conflicts were found.
     */
    public boolean isEmpty() {
        if (conflicts == null) {
            throw new IllegalStateException("You have to call findConflicting() before isEmpty()");
        }
        return conflicts.isEmpty();
    }

    /**
     * @return A multi-line String representation of the conflicting objects. 
     */
    public String toConflictTree() {
        StringBuffer tree = new StringBuffer();
        Iterator it = conflicts.iterator();
        while (it.hasNext()) {
            Conflict c = (Conflict) it.next();
            appendToConflictTree(tree, 1, c);
        }
        return tree.toString();
    }

    /**
     * The toConflictTree method uses this recursive subroutine.
     * 
     * @param tree The string buffer we're formatting the tree into.
     * @param indent How many places to indent the first line by.
     * @param c The conflict to format into the string buffer.
     */
    private void appendToConflictTree(StringBuffer tree, int indent, Conflict c) {
        for (int i = 0; i < indent; i++) {
            tree.append(" ");
        }
        tree.append(c.getType()).append(" ").append(c.getQualifiedName());
        tree.append("\n");
        Iterator it = c.getDependants().iterator();
        while (it.hasNext()) {
            appendToConflictTree(tree, indent+1, (Conflict) it.next());
        }
    }

    /**
     * @return The text of the SQL statement most recently attempted by this class. 
     */
    public String getLastSQLStatement() {
        return lastSQLStatement;
    }
}
