/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
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
    private List ddlStatements;
    private List conflicts;
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
    public ConflictResolver(SQLDatabase target, DDLGenerator ddlg, List ddlStatements) {
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
     * 
     * @return a list of object names that need to be removed before ddlStmt
     *         will succeed.
     * @throws ArchitectException 
     */
    public void findConflicting() throws SQLException, ArchitectException {
   		doingFindConflicting = true;
   		Connection con = null;
   		try {
   			conflicts = new ArrayList();
   			monitorableProgress = 0;
   			
   			if (logger.isDebugEnabled()) {
   				logger.debug("About to find conflicting objects for DDL Script: "+ddlStatements);
   			}
   			
   			con = targetDatabase.getConnection();
   			DatabaseMetaData dbmd = con.getMetaData();
   			
   			Iterator it = ddlStatements.iterator();
   			while (it.hasNext()) {
   			    DDLStatement ddlStmt = (DDLStatement) it.next();
   			    monitorableProgress += 1;
   			    if (ddlStmt.getType() != DDLStatement.StatementType.CREATE) continue;
   			    SQLObject so = ddlStmt.getObject();
   			    Class clazz = so.getClass();
   			    
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
     * @throws ArchitectException 
     */
    public void dropConflicting() throws SQLException, ArchitectException {
    		if (conflicts == null) {
    			throw new IllegalStateException("You have to call findConflicting() before dropConflicting()");
    		}
    		monitorableProgress = 0;
    		dropConflictingStarted = true;
    		doingDropConflicting = true;
    		Iterator it = conflicts.iterator();
    		Connection con = null;
    		Statement stmt = null;
    		try {
    			con = targetDatabase.getConnection();
    			stmt = con.createStatement();
    			Set alreadyDropped = new HashSet();
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

	
