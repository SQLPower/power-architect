/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLSequence;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.Monitorable;

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
public class ConflictResolver implements Monitorable {
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
        private List<Conflict> dependants;
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
            this.dependants = new ArrayList<Conflict>();
        }
        
        public String getQualifiedName() {
            return DDLUtils.toQualifiedName(catalog, schema, name);
        }
        
        /**
         * Adds the dependant imported and exported key relationships which must
         * be dropped before this conflict can be dropped. 
         * @param dbmd The database metadata to consult.
         */
        private void addTableDependants(DatabaseMetaData dbmd) throws SQLException {
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
                
                ddlg.setTargetCatalog(c.getCatalog());
                ddlg.setTargetSchema(c.getSchema());
                c.setSqlDropStatement(
                        ddlg.makeDropForeignKeySQL(rs.getString("FKTABLE_NAME"), c.getName()));
                dependants.add(c);
            }
        }
        
        public String toString() {
            return getType()+" "+getQualifiedName();
        }
        public void setDependants(List<Conflict> v) {
            this.dependants = v;
        }
        public List<Conflict> getDependants() {
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
         * A mediocre hash function for combining the same fields that the equals compares.
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
    
    private SQLDatabase targetDatabase;
    private List<DDLStatement> ddlStatements;
    private List<Conflict> conflicts;
    private String lastSQLStatement;
    private DDLGenerator ddlg;
    private int monitorableProgress;
	private boolean doingFindConflicting;
    private boolean findConflictingFinished;
	private boolean doingDropConflicting;
	private boolean dropConflictingStarted;
	private boolean dropConflictingFinished;
	
    /**
     * Creates a new ConflictResolver.  You should call findConflicting() after you get
     * this new object.
     */
    public ConflictResolver(SQLDatabase target, DDLGenerator ddlg, List<DDLStatement> ddlStatements) {
    	this.targetDatabase = target;
        this.ddlg = ddlg;
        this.ddlStatements = ddlStatements;
    }

    public void aboutToCallDropConflicting() {
    		dropConflictingStarted = true;
    }
    
    /**
     * Searches for objects in the database pointed to by con that would
     * conflict with the execution of any of the given DDL statements.
     * @throws SQLException
     * @throws SQLObjectException 
     */
    public void findConflicting() throws SQLException, SQLObjectException {
   		doingFindConflicting = true;
   		Connection con = null;
   		try {
   			conflicts = new ArrayList<Conflict>();
   			monitorableProgress = 0;
   			
   			if (logger.isDebugEnabled()) {
   				logger.debug("About to find conflicting objects for DDL Script: "+ddlStatements);
   			}
   			
   			con = targetDatabase.getConnection();
   			DatabaseMetaData dbmd = con.getMetaData();
   			
   			Iterator<DDLStatement> it = ddlStatements.iterator();
   			while (it.hasNext()) {
   			    DDLStatement ddlStmt = (DDLStatement) it.next();
   			    monitorableProgress += 1;
   			    if (ddlStmt.getType() != DDLStatement.StatementType.CREATE) continue;
   			    SQLObject so = ddlStmt.getObject();
   			    Class<? extends SQLObject> clazz = so.getClass();
   			    
   			    if (clazz.equals(SQLTable.class)) {
   			        SQLTable t = (SQLTable) so;
   			        String cat = ddlStmt.getTargetCatalog();
   			        String sch = ddlStmt.getTargetSchema();
   			        if (logger.isDebugEnabled()) {
   			            logger.debug("Finding conflicts for TABLE '" + cat + "'.'"
   			                    + sch + "'.'" + t.getPhysicalName() + "'");
   			        }
   			        
   			        ResultSet rs = dbmd.getTables(
   			                ddlg.toIdentifier(cat),
   			                ddlg.toIdentifier(sch),
   			                ddlg.toIdentifier(t.getPhysicalName()),
   			                null);
   			        while (rs.next()) {
   			            Conflict c = new Conflict(
   			                    rs.getString("TABLE_TYPE"),
   			                    rs.getString("TABLE_CAT"),
   			                    rs.getString("TABLE_SCHEM"),
   			                    rs.getString("TABLE_NAME"));
   			            ddlg.setTargetCatalog(c.getCatalog());
   			            ddlg.setTargetSchema(c.getSchema());
   			            c.setSqlDropStatement(ddlg.makeDropTableSQL(c.getName()));
   			            c.addTableDependants(dbmd);
   			            conflicts.add(c);
   			        }
   			        rs.close();
   			        
   				} else if (clazz.equals(SQLRelationship.class)) {
   					logger.error("Relationship conflicts are not supported yet!");
                } else if (clazz.equals(SQLIndex.class)) {
                    logger.error("Index conflicts not supported.");
                } else if (clazz.equals(SQLSequence.class)) {
                    logger.error("Sequence conflicts not supported.");
   				} else {
   					throw new IllegalArgumentException(
   							"Unknown subclass of SQLObject: " + clazz.getName());
   				}
   			}
   			
   			if (logger.isDebugEnabled()) {
   				logger.debug("Found conflicts: " + conflicts);
   			}
   		} finally {    			
   			findConflictingFinished = true;
   			doingFindConflicting = false;
   			try {
   				if (con != null) con.close();
   			} catch (SQLException ex) {
   				logger.error("Couldn't close connection");
   			}
   		}
    }

    /**
     * Drops the conflicting objects which findConflicting() found in the target database.
     * 
     * @throws SQLException
     * @throws SQLObjectException 
     */
    public void dropConflicting() throws SQLException, SQLObjectException {
    		if (conflicts == null) {
    			throw new IllegalStateException("You have to call findConflicting() before dropConflicting()");
    		}
    		monitorableProgress = 0;
    		dropConflictingStarted = true;
    		doingDropConflicting = true;
    		Iterator<Conflict> it = conflicts.iterator();
    		Connection con = null;
    		Statement stmt = null;
    		try {
    			con = targetDatabase.getConnection();
    			stmt = con.createStatement();
    			Set<Conflict> alreadyDropped = new HashSet<Conflict>();
    			while (it.hasNext()) {
    				Conflict c = (Conflict) it.next();
    				monitorableProgress++;
    				dropConflict(c, stmt, alreadyDropped);
    			}
    		} finally {
    			dropConflictingFinished = true;
    			doingDropConflicting = false;
    			try {
    				if (stmt != null) stmt.close();
    			} catch (SQLException ex) {
    				logger.error("Couldn't close statement", ex);
    			}
    			try {
    				if (con != null) con.close();
    			} catch (SQLException ex) {
    				logger.error("Couldn't close connection", ex);
    			}
    		}
    }
    
    /**
     * Recursively drops the given conflict and all its dependencies.  This is a 
     * subroutine of the public dropConflicting() method.
     * @param c
     * @param stmt
     */
    private void dropConflict(Conflict c, Statement stmt, Set<Conflict> alreadyDropped) throws SQLException {
        
        Iterator<Conflict> it = c.getDependants().iterator();
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
        Iterator<Conflict> it = conflicts.iterator();
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
        Iterator<Conflict> it = c.getDependants().iterator();
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

    // ========== Monitorable interface ===========
 
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.Monitorable#getProgress()
	 */
	public int getProgress() {
		return monitorableProgress;
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.Monitorable#getJobSize()
	 */
	public Integer getJobSize() {
		if (doingFindConflicting) {
			if (ddlStatements == null) return null;
			else return new Integer(ddlStatements.size());
		} else if (doingDropConflicting) {
			if (conflicts == null) return null;
			else return new Integer(conflicts.size());
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.Monitorable#isFinished()
	 */
	public boolean isFinished() {
		if (doingDropConflicting || doingFindConflicting) return false;
		else if (dropConflictingStarted) return dropConflictingFinished;
		else return findConflictingFinished;
	}
	
	/**
	 * Not implemented.  Throws a RuntimeException when called.
	 */
	public void setCancelled(boolean cancelled) {
		throw new RuntimeException("The Conflict Resolver can't be cancelled");
	}

    /**
     * Not implemented. Always returns false.
     */
    public boolean isCancelled() {
        return false;
    }
	
	public String getMessage () {
		return null;
	}
	/**
	 * @return Returns the hasStarted.
	 */
	public boolean hasStarted() {
		return (doingDropConflicting || doingFindConflicting);
	}
}

	
