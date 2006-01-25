package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;
import java.sql.*;
import java.util.*;
import java.io.File;
import org.apache.log4j.Logger;

public class GenericDDLGenerator implements DDLGenerator {

	public static final String GENERATOR_VERSION = "$Revision$";

	private static final Logger logger = Logger.getLogger(GenericDDLGenerator.class);

	/**
	 * This property says whether or not the user will allow us to
	 * connect to the target system in order to determine database
	 * meta-data.  This generic base class will fail if
	 * allowConnection == false.
	 */
	protected boolean allowConnection;

	/**
	 * This is the user's selected file for saving the generated DDL.
	 * This class does not actually write DDL to disk, but you should
	 * use this file when you save the generated DDL string.
	 */
	protected File file;

	/**
	 * This is where each DDL statement gets accumulated while it is
	 * being generated.
	 */
	private StringBuffer ddl;

	/**
	 * Complete DDL statements (of type DDLStatement) are accumulated in this list.
	 */
	private List ddlStatements;

	/**
	 * This is initialized to the System line.separator property.
	 */
	protected static final String EOL = System.getProperty("line.separator");

	/**
	 * A mapping from JDBC type code (Integer values) to
	 * GenericTypeDescriptor objects which describe that data type.
	 */
	protected Map typeMap;

	/**
	 * This variable will be a live, non-null connection to the target
	 * database (set up by writeDDL) if allowConnection is true.
	 */
	protected Connection con;

	/**
	 * As table and relationship creation statements are generated,
	 * their SQL identifiers are stored in this map (key is name,
	 * value is object having that name).  Warnings are created when
	 * multiple objects in this top-level scope use the same name.
	 */
	protected Map topLevelNames;

	/**
	 * This list contains 0 or more {@link NameChangeWarning} objects.  It is
	 * populated as statements are added to the
	 * <code>ddlStatements</code> list.  If non-empty, this list of
	 * warnings should be presented to the user before the generated
	 * DDL is saved or executed (to give them a chance to fix the
	 * warnings and try again).
	 */
	protected List warnings;

	/**
	 * The name of the catalog in the target database that the
	 * generated DDL statements should create the objects in.  Not all
	 * databases have catalogs; subclasses of GenericDDLGenerator which
	 * target catalogless platforms should set this value to
	 * <code>null</code> as well as override {@link #getCatalogTerm()}
	 * to return <code>null</code>.
	 */
	protected String targetCatalog;

	/**
	 * The name of the schema in the target database that the
	 * generated DDL statements should create the objects in.  Not all
	 * databases have schemas; subclasses of GenericDDLGenerator which
	 * target schemaless platforms should set this value to
	 * <code>null</code> as well as override {@link #getSchemaTerm()}
	 * to return <code>null</code>.
	 */
	protected String targetSchema;

	public GenericDDLGenerator() {
		allowConnection = true;
	}

	public StringBuffer generateDDL(SQLDatabase source) throws SQLException, ArchitectException {
		List statements = generateDDLStatements(source);

		ddl = new StringBuffer(4000);
		writeHeader();
		writeCreateDB(source);
		writeDDLTransactionBegin();

		Iterator it = statements.iterator();
		while (it.hasNext()) {
			DDLStatement ddlStmt = (DDLStatement) it.next();
			ddl.append(ddlStmt.getSQLText());
			writeStatementTerminator();
		}
		
		writeDDLTransactionEnd();
		return ddl;
	}

	public List generateDDLStatements(SQLDatabase source) throws SQLException, ArchitectException {
		warnings = new ArrayList();
		ddlStatements = new ArrayList();
		ddl = new StringBuffer(500);
		topLevelNames = new HashMap();  // for tracking dup table/relationship names

		if (allowConnection) {
			con = source.getConnection();
		} else {
			con = null;
		}
		
		createTypeMap();

		Iterator it = source.getChildren().iterator();
		while (it.hasNext()) {
			SQLTable t = (SQLTable) it.next();
			writeTable(t);
			writePrimaryKey(t);
		}
		it = source.getChildren().iterator();
		while (it.hasNext()) {
			SQLTable t = (SQLTable) it.next();
			writeExportedRelationships(t);
		}

		return ddlStatements;
	}

	/**
	 * Stores all the ddl since the last call to endStatement as a SQL
	 * statement. You have to call this at the end of each statement.
	 *
	 * @param type the type of statement
	 * @param sqlObject the object to which the statement pertains
	 */
	public final void endStatement(DDLStatement.StatementType type, SQLObject sqlObject) {
		if (logger.isInfoEnabled()) {
			logger.info("endStatement: " + ddl.toString());
		}
		ddlStatements.add(new DDLStatement(sqlObject, type, ddl.toString(), getTargetCatalog(), getTargetSchema()));
		ddl = new StringBuffer(500);
	}

	public void writeHeader() {
		println("-- Created by SQLPower Generic DDL Generator "+GENERATOR_VERSION+" --");
	}

	/**
	 * Prints a single semicolon character (no newline).  If your
	 * database needs something else, override this method.
	 */
	public void writeStatementTerminator() {
		print(";");
	}

	/**
	 * Does nothing.  If your target system supports transactional
	 * DDL, override this method and print the appropriate statement.
	 */
	public void writeDDLTransactionBegin() {
        // not supported in generic case
	}

	/**
	 * Does nothing.  If your target system supports transactional
	 * DDL, override this method and print the appropriate statement.
	 */
	public void writeDDLTransactionEnd() {
        // not supported in generic case
	}

	public void writeCreateDB(SQLDatabase db) {
		println("-- Would Create Database "+db.getName()+" here. --");
	}

	public void writeTable(SQLTable t) throws SQLException, ArchitectException {
		Map colNameMap = new HashMap();  // for detecting duplicate column names
		// generate a new physical name if necessary
		getPhysicalName(topLevelNames,t); // also adds generated physical name to the map
		print("\nCREATE TABLE ");
		printQualified(t.getPhysicalName());
		println(" (");
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			// generate a new physical name if necessary
			getPhysicalName(colNameMap,c); // also adds generated physical name to the map
			GenericTypeDescriptor td = (GenericTypeDescriptor) typeMap.get(new Integer(c.getType()));
			if (td == null) {
				td = (GenericTypeDescriptor) typeMap.get(new Integer(Types.VARCHAR)); //better be non-null!
				GenericTypeDescriptor oldType = new GenericTypeDescriptor
					(c.getSourceDataTypeName(), c.getType(), c.getPrecision(),
					 null, null, c.getNullable(), false, false);
				oldType.determineScaleAndPrecision();
				warnings.add(new TypeMapWarning(c, "Unknown Target Type", oldType, td));
			}
			if (!firstCol) println(",");
			print("                ");
			print(c.getPhysicalName());
			print(" ");
			print(td.getName());
			if (td.getHasPrecision()) {
				print("("+c.getPrecision());
				if (td.getHasScale()) {
					print(","+c.getScale());
				}
				print(")");
			}

			if (c.isDefinitelyNullable()) {
				if (! td.isNullable()) {
					throw new UnsupportedOperationException
						("The data type "+td.getName()+" is not nullable on the target database platform.");
				}
				print(" NULL");
			} else {
				print(" NOT NULL");
			}

			// XXX: default values handled only in ETL?

			firstCol = false;
		}
		println("");
		print(")");
		endStatement(DDLStatement.StatementType.CREATE, t);
		println("");
	}
	
	protected void writePrimaryKey(SQLTable t) throws ArchitectException {
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getPrimaryKeySeq() == null) break;
			if (firstCol) {
				// generate a unique primary key name
                getPhysicalPrimaryKeyName(topLevelNames,t);
				//
				println("");
				print("ALTER TABLE ");
				printQualified(t.getPhysicalName());
				print(" ADD CONSTRAINT ");
				print(t.getPhysicalPrimaryKeyName());
				println("");
				print("PRIMARY KEY (");
				firstCol = false;
			} else {
				print(", ");
			}
			print(col.getPhysicalName());
		}
		if (!firstCol) {
			print(")");
			endStatement(DDLStatement.StatementType.ADD_PK, t);
			println("");
		}
	}

	protected void writeExportedRelationships(SQLTable t) throws ArchitectException {
		Iterator it = t.getExportedKeys().iterator();
		while (it.hasNext()) {
			SQLRelationship rel = (SQLRelationship) it.next();
			// geneate a physical name for this relationship
			getPhysicalName(topLevelNames,rel); 
			//
			println("");
			print("ALTER TABLE ");
			// this works because all the tables have had their physical names generated already...
			printQualified(rel.getFkTable().getPhysicalName());
			print(" ADD CONSTRAINT ");
			print(rel.getPhysicalName());
			println("");
			print("FOREIGN KEY (");
			StringBuffer pkCols = new StringBuffer();
			StringBuffer fkCols = new StringBuffer();
			boolean firstCol = true;
			Iterator mappings = rel.getChildren().iterator();
			while (mappings.hasNext()) {
				SQLRelationship.ColumnMapping cmap = (SQLRelationship.ColumnMapping) mappings.next();
				if (!firstCol) {
					pkCols.append(", ");
					fkCols.append(", ");
				}
				// 
				append(pkCols, cmap.getPkColumn().getPhysicalName());
				append(fkCols, cmap.getFkColumn().getPhysicalName());
				firstCol = false;
			}
			print(fkCols.toString());
			println(")");
			print("REFERENCES ");
			printQualified(rel.getPkTable().getPhysicalName());
			print(" (");
			print(pkCols.toString());
			print(")");
			endStatement(DDLStatement.StatementType.ADD_FK, t);
			println("");
		}
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData.  Subclasses for specific DB platforms will be
	 * able to override this implementation with one that uses a
	 * static, pre-defined type map.
	 */
	protected void createTypeMap() throws SQLException {
		if (con == null || !allowConnection) {
			throw new UnsupportedOperationException("Can't create a type map without DatabaseMetaData");
		}
		typeMap = new HashMap();
		DatabaseMetaData dbmd = con.getMetaData();
		ResultSet rs = dbmd.getTypeInfo();
		while (rs.next()) {
			GenericTypeDescriptor td = new GenericTypeDescriptor(rs);
			typeMap.put(new Integer(td.getDataType()), td);
		}
		rs.close();
	}

	protected void println(String text) {
		ddl.append(text).append(EOL);
	}

	protected void print(String text) {
		ddl.append(text);
	}

	/**
	 * Appends the qualified name (with targetCatalog and
	 * targetSchema, if present) to <code>ddl</code>, the internal
	 * StringBuffer that accumulates the results of DDL generation.
	 *
	 * <p>Names are qualified with
	 * <code>catalog.schema.object_name</code> "dot" notation.  If
	 * your generator subclass is for a database that doesn't use dot
	 * notation, override this method and do it differently.
	 */
	// NOT NEEDED ANYMORE?
	protected void printQualifiedIdentifier(String text) {	
		if (getTargetCatalog() != null && getTargetCatalog().length() > 0) {
			ddl.append(getTargetCatalog()).append('.');
		}
		if (getTargetSchema() != null && getTargetSchema().length() > 0) {
			ddl.append(getTargetSchema()).append('.');
		}
		appendIdentifier(ddl, text);
	}

	protected void printQualified(String text) {	
		if (getTargetCatalog() != null && getTargetCatalog().length() > 0) {
			ddl.append(getTargetCatalog()).append('.');
		}
		if (getTargetSchema() != null && getTargetSchema().length() > 0) {
			ddl.append(getTargetSchema()).append('.');
		}
		print(text);
	}

	/**
	 * Calls {@link #appendIdentifier} on <code>ddl</code>, the
	 * internal StringBuffer that accumulates the results of DDL
	 * generation.  It should never be necessary to override this
	 * method, because changes to appendIdentifier will always affect
	 * this method's behaviour.
	 */

	// NOT NEEDED ANYMORE?
	protected final void printIdentifier(String text) {
		appendIdentifier(ddl, text);
	}

	/**
	 * Converts <code>text</code> to a SQL identifier by calling
	 * {@link #toIdentifier} and appends the result to <code>sb</code>
	 */
	// NOT NEEDED ANYMORE?
	protected void appendIdentifier(StringBuffer sb, String text) {
		sb.append(toIdentifier(text));		
	}
	protected void append(StringBuffer sb, String text) {
		sb.append(text);
	}

	/**
	 * Converts space to underscore in <code>name</code> and returns
	 * the possibly-modified string.  This will not be completely
	 * sufficient because it leaves ".", "%", and lots of other
	 * non-alphanumeric characters alone. Subclasses might choose to
	 * quote and leave everything alone, or whatever.
	 */
	public String toIdentifier(String logicalName, String physicalName) {
        if (logicalName == null) return null;
		else return logicalName.replace(' ', '_');
	}

	public String toIdentifier(String name) {
        if (name == null) return null;
		else return name.replace(' ', '_');
	}


	// ---------------------- accessors and mutators ----------------------
	
	/**
	 * Gets the value of allowConnection
	 *
	 * @return the value of allowConnection
	 */
	public boolean getAllowConnection()  {
		return this.allowConnection;
	}

	/**
	 * Sets the value of allowConnection
	 *
	 * @param argAllowConnection Value to assign to this.allowConnection
	 */
	public void setAllowConnection(boolean argAllowConnection) {
		this.allowConnection = argAllowConnection;
	}

	/**
	 * Gets the value of file
	 *
	 * @return the value of file
	 */
	public File getFile()  {
		return this.file;
	}

	/**
	 * Sets the value of file
	 *
	 * @param argFile Value to assign to this.file
	 */
	public void setFile(File argFile) {
		this.file = argFile;
	}

	/**
	 * Gets the value of typeMap
	 *
	 * @return the value of typeMap
	 */
	public Map getTypeMap()  {
		return this.typeMap;
	}

	/**
	 * Sets the value of typeMap
	 *
	 * @param argTypeMap Value to assign to this.typeMap
	 */
	public void setTypeMap(Map argTypeMap) {
		this.typeMap = argTypeMap;
	}

	/**
	 * Gets the value of con
	 *
	 * @return the value of con
	 */
	public Connection getCon()  {
		return this.con;
	}

	/**
	 * Sets the value of con
	 *
	 * @param argCon Value to assign to this.con
	 */
	public void setCon(Connection argCon) {
		this.con = argCon;
	}

	/**
	 * Returns {@link #warnings}.
	 */
	public List getWarnings() {
		return warnings;
	}

	/**
	 * See {@link #targetCatalog}.
	 *
	 * @return the value of targetCatalog
	 */
	public String getTargetCatalog()  {
		return this.targetCatalog;
	}

	/**
	 * See {@link #targetCatalog}.
	 *
	 * @param argTargetCatalog Value to assign to this.targetCatalog
	 */
	public void setTargetCatalog(String argTargetCatalog) {
		this.targetCatalog = argTargetCatalog;
	}

	/**
	 * See {@link #targetSchema}.
	 *
	 * @return the value of targetSchema
	 */
	public String getTargetSchema()  {
		return this.targetSchema;
	}

	/**
	 * See {@link #targetSchema}.
	 *
	 * @param argTargetSchema Value to assign to this.targetSchema
	 */
	public void setTargetSchema(String argTargetSchema) {
		this.targetSchema = argTargetSchema;
	}

	/**
	 * The name that the target database gives to the JDBC idea of
	 * "catalog."  For Oracle, this would be null (no catalogs) and
	 * for SQL Server it would be "Database".
	 */
	public String getCatalogTerm() {
		return null;
	}

	/**
	 * The name that the target database gives to the JDBC idea of
	 * "schema."  For Oracle, this would be "Schema" and for SQL
	 * Server it would be "Owner".
	 */
	public String getSchemaTerm() {
		return null;
	}

	/**
     * Generate, set, and return a valid identifier for this SQLObject.
     */
	public String getPhysicalName(Map dupCheck, SQLObject so) {				
		boolean done = false;
		boolean firstTime = true;
		String oldName = so.getName();
		// loop until we manage to generate a unique physical name
		logger.debug("transform identifier source: " + so.getName());
		while (!done) {
			String temp = null;
			if (firstTime) {
				// naming is deterministic, so unconditionally regenerate
				firstTime = false;
				temp = toIdentifier(so.getName(),null); 
			} else {
				temp = toIdentifier(so.getName(),so.getPhysicalName());
			}
			logger.debug("transform identifier result: " + temp);
			so.setPhysicalName(temp);
			if (dupCheck.get(so.getPhysicalName()) == null) {
				done = true; // we managed to generate something unique
				warnings.add(new NameChangeWarning(so, "Duplicate Name Found", oldName)); //TODO make sure these appear in the warnings table, and make sure they're editable
				dupCheck.put(so.getPhysicalName(), so);
			}
		}				
		return so.getPhysicalName();
	}

	/**
     * Generate, set, and return a physicalPrimaryKeyName. 
     */
	public String getPhysicalPrimaryKeyName(Map dupCheck, SQLTable t) {		
		boolean done = false;
		boolean firstTime = true;
		// loop until we manage to generate a valid physical primary key name
		while (!done) {
			logger.debug("getting phsysical primary key name, logical="+t.getPrimaryKeyName()+",physical="+t.getPhysicalPrimaryKeyName());
			String temp = null;
			if (firstTime) {
				// naming is deterministic, so unconditionally regenerate
				firstTime = false;
				temp = toIdentifier(t.getPrimaryKeyName(),null);
			} else {
				temp = toIdentifier(t.getPrimaryKeyName(),t.getPhysicalPrimaryKeyName()); 
			}
			logger.debug("transform key identifier result: " + temp);
			t.setPhysicalPrimaryKeyName(temp);
			if (dupCheck.get(t.getPhysicalPrimaryKeyName()) == null) {
				done = true; // we managed to generate something valid and unique
				dupCheck.put(t.getPhysicalPrimaryKeyName(),t);
			}
		}				
		return t.getPhysicalPrimaryKeyName();
	}

    /**
     * Generates a standard <code>DROP TABLE $tablename</code> command.  Should work on most platforms. 
     */
    public String makeDropTableSQL(String catalog, String schema, String table) {
        return "DROP TABLE "+DDLUtils.toQualifiedName(catalog, schema, table);
    }

    /**
     * Generates a command for dropping a foreign key which works on some platforms.
     * The statement looks like <code>ALTER TABLE $fktable DROP FOREIGN KEY $fkname</code>.
     */
    public String makeDropForeignKeySQL(String fkCatalog, String fkSchema, String fkTable, String fkName) {
        return "ALTER TABLE "
            +DDLUtils.toQualifiedName(fkCatalog, fkSchema, fkTable)
            +" DROP FOREIGN KEY "
            +fkName;
    }
}
