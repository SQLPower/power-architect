/*
 * Created on May 11, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.diff.ArchitectDiffException;

/**
 * The DDLGenerator interface is a generic API for turning a SQLObject
 * hierarchy into a series of SQL statements which create the corresponding
 * data model in a physical database.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface DDLGenerator {
    public abstract List generateDDLStatements(SQLDatabase source)
            throws SQLException, ArchitectException;
    
    public void dropColumn(SQLColumn c, SQLTable t) throws ArchitectDiffException;
    public void addColumn(SQLColumn c, SQLTable t) throws ArchitectDiffException;
    public void modifyColumn(SQLColumn c) throws ArchitectDiffException ;
    public void addRelationship(SQLRelationship r) throws ArchitectDiffException;
    public void dropRelationship(SQLRelationship r);

    /** 
     * Writes out the sql statements for droping a table using SQLTable t's name.
     * It then adds the statement to the DdlStatements list.
     */
    public void dropTable(SQLTable t);
    /** 
     * Writes out the sql statements for creating a table using SQLTable t as a 
     * template.  It then adds the statement to the DdlStatements list.
     */
    public void writeTable(SQLTable t) throws SQLException, ArchitectException;
    public List<DDLStatement> getDdlStatements();

    /**
     * Converts an arbitrary string (which may contain spaces, mixed case, 
     * punctuation, and so on) into a valid identifier in the target
     * database system.  The implementation should mangle the input string
     * to the minimum degree necessary to make the given string into a valid
     * identifier on the target system.
     */
    public abstract String toIdentifier(String name);

    /**
     * Creates and returns a DDL statement which will drop the given table.
     * 
     * @param catalog The catalog of the table to be dropped (NULL for no catalog).
     * @param schema The schema of the table to be dropped (NULL for no schema).
     * @param table The name of the table to be dropped.
     * @return A SQL statement which will drop the table. 
     */
    public abstract String makeDropTableSQL(String catalog, String schema, String table);

    /**
     * Creates and returns a DDL statement which will drop a foreign key relationship.
     * 
     * @param fkCatalog The catalog of the FK table whose relationship should be dropped.
     * @param fkSchema The schema of the FK table whose relationship should be dropped.
     * @param fkTable The name of the FK table whose relationship should be dropped.
     * @param fkName The name of the key to drop.
     * @return a SQL statement which will drop the key.
     */
    public abstract String makeDropForeignKeySQL(String fkCatalog, String fkSchema, String fkTable, String fkName);

    // ---------------------- accessors and mutators ----------------------

    /**
     * Tells the generator whether or not it can connect to the target 
     * database and ask for additional information during the generation
     * process. 
     */
    public abstract boolean getAllowConnection();

    /**
     * See {@link #getAllowConnection()}.
     */
    public abstract void setAllowConnection(boolean argAllowConnection);

    /**
     * Gets the value of typeMap
     *
     * @return the value of typeMap
     */
    public abstract Map getTypeMap();

    /**
     * Sets the value of typeMap
     *
     * @param argTypeMap Value to assign to this.typeMap
     */
    public abstract void setTypeMap(Map argTypeMap);

    /**
     * Gets the value of con
     *
     * @return the value of con
     */
    public abstract Connection getCon();

    /**
     * Sets the value of con
     *
     * @param argCon Value to assign to this.con
     */
    public abstract void setCon(Connection argCon);

    /**
     * Returns {@link #warnings}.
     */
    public abstract List getWarnings();

    /**
     * See {@link #targetCatalog}.
     *
     * @return the value of targetCatalog
     */
    public abstract String getTargetCatalog();

    /**
     * See {@link #targetCatalog}.
     *
     * @param argTargetCatalog Value to assign to this.targetCatalog
     */
    public abstract void setTargetCatalog(String argTargetCatalog);

    /**
     * See {@link #targetSchema}.
     *
     * @return the value of targetSchema
     */
    public abstract String getTargetSchema();

    /**
     * See {@link #targetSchema}.
     *
     * @param argTargetSchema Value to assign to this.targetSchema
     */
    public abstract void setTargetSchema(String argTargetSchema);

    /**
     * The name that the target database gives to the JDBC idea of
     * "catalog."  For Oracle, this would be null (no catalogs) and
     * for SQL Server it would be "Database".
     */
    public abstract String getCatalogTerm();

    /**
     * The name that the target database gives to the JDBC idea of
     * "schema."  For Oracle, this would be "Schema" and for SQL
     * Server it would be "Owner".
     */
    public abstract String getSchemaTerm();

	public abstract void dropPrimaryKey(SQLTable t, String primaryKeyName);

	public abstract void addPrimaryKey(SQLTable t, String primaryKeyName) throws ArchitectException;
}