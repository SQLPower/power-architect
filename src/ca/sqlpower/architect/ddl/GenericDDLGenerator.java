package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;

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
	private List<DDLStatement> ddlStatements;

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

	public GenericDDLGenerator() throws SQLException {
		allowConnection = true;
		warnings = new ArrayList();
		ddlStatements = new ArrayList();
		ddl = new StringBuffer(500);
		println("");
		topLevelNames = new HashMap();  // for tracking dup table/relationship names
		createTypeMap();
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
			println(getStatementTerminator());
		}
		
		writeDDLTransactionEnd();
		return ddl;
	}

	
	public List<DDLStatement> generateDDLStatements(SQLDatabase source) throws SQLException, ArchitectException {
		warnings = new ArrayList();
		ddlStatements = new ArrayList<DDLStatement>();
		ddl = new StringBuffer(500);
		topLevelNames = new HashMap();  // for tracking dup table/relationship names

		try {
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
            
			// TODO add warnings for the originals of the existing duplicate name warnings
		} finally {
			try {
				if (con != null) con.close();
			} catch (SQLException ex) {
				logger.error("Couldn't close connection", ex);
			}
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
		
		ddlStatements.add(new DDLStatement(sqlObject, type, ddl.toString(), getStatementTerminator(), getTargetCatalog(), getTargetSchema()));
		ddl = new StringBuffer(500);
		println("");
	}

	public void writeHeader() {
		println("-- Created by SQLPower Generic DDL Generator "+GENERATOR_VERSION+" --");
	}

	/**
	 * Prints a single semicolon character (no newline).  If your
	 * database needs something else, override this method.
	 */
	public String getStatementTerminator() {
		return ";";
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

	public void dropRelationship(SQLRelationship r) {
		
		print("\n ALTER TABLE ");
		
		print( toQualifiedName(r.getFkTable()) );
		print(" DROP CONSTRAINT ");
		print(r.getName());
		endStatement(DDLStatement.StatementType.DROP, r);
	}
	
	public void addRelationship(SQLRelationship r) {
		
		print("\n ALTER TABLE ");
		print( toQualifiedName(r.getFkTable()) );
		print(" ADD CONSTRAINT ");
		print(r.getName());
		print(" FOREIGN KEY ( ");
		Map<String, SQLColumn> colNameMap = new HashMap<String, SQLColumn> (); 
		boolean firstColumn = true;

		for (ColumnMapping cm : r.getMappings()) {
			SQLColumn c = cm.getFkColumn();
			// make sure this is unique
			if (colNameMap.get(c.getName()) == null) {
				if (firstColumn) {
					firstColumn = false;
					print(createPhysicalName(colNameMap, c));
				} else {
					print(", " + createPhysicalName(colNameMap, c));
				}
				colNameMap.put(c.getName(), c);
			}
		}
		print(" ) REFERENCES ");
		print( toQualifiedName(r.getPkTable()) );
		print(" ( ");
		colNameMap = new HashMap<String, SQLColumn>();
		firstColumn = true;

		for (ColumnMapping cm : r.getMappings()) {

			SQLColumn c = cm.getPkColumn();
			// make sure this is unique
			if (colNameMap.get(c.getName()) == null) {
				if (firstColumn) {
					firstColumn = false;
					print(createPhysicalName(colNameMap, c));
				} else {
					print(", " + createPhysicalName(colNameMap, c));
				}
				colNameMap.put(c.getName(), c);
			}
		}

		print(" )");
		endStatement(DDLStatement.StatementType.CREATE, r);
		
	}
	
	public void addColumn(SQLColumn c) {
		Map colNameMap = new HashMap();  
		print("\n ALTER TABLE ");
		print(toQualifiedName(c.getParentTable()));
		print(" ADD COLUMN ");
		print(columnDefinition(c,colNameMap));
		endStatement(DDLStatement.StatementType.CREATE, c);
		
	}
	
	public void dropColumn(SQLColumn c) {
		Map colNameMap = new HashMap();  
		print("\n ALTER TABLE ");
		print(toQualifiedName(c.getParentTable()));
		print(" DROP COLUMN ");
		print(createPhysicalName(colNameMap,c));
		endStatement(DDLStatement.StatementType.DROP, c);
		
	}
	
	public void modifyColumn(SQLColumn c) {
		Map colNameMap = new HashMap(); 
		SQLTable t = c.getParentTable();
		print("\n ALTER TABLE ");
		print(toQualifiedName(t));
		print(" ALTER COLUMN ");
		print(columnDefinition(c,colNameMap));
		endStatement(DDLStatement.StatementType.MODIFY, c);
		
	}
	
	public void dropTable(SQLTable t) {
        print(makeDropTableSQL(t.getName()));
        endStatement(DDLStatement.StatementType.DROP, t);
    }
    
	protected String columnDefinition(SQLColumn c, Map colNameMap) {
        StringBuffer def = new StringBuffer();

        // Column name
        def.append(createPhysicalName(colNameMap, c));
        def.append(" ");

        def.append(columnType(c));
        def.append(" ");

        // Column nullability
        def.append(columnNullability(c));

        return def.toString();
    }

    protected String columnNullability(SQLColumn c) {
        GenericTypeDescriptor td = failsafeGetTypeDescriptor(c);       
        if (c.isDefinitelyNullable()) {
			if (! td.isNullable()) {
				throw new UnsupportedOperationException
					("The data type "+td.getName()+" is not nullable on the target database platform.");
			}
			return "NULL";
		} else {
			return "NOT NULL";
		}
    }

	/** Columnn type */
    protected String columnType(SQLColumn c) {
        StringBuffer def = new StringBuffer();
		GenericTypeDescriptor td = failsafeGetTypeDescriptor(c);       
		def.append(td.getName());
		if (td.getHasPrecision()) {
			def.append("("+c.getPrecision());
			if (td.getHasScale()) {
				def.append(","+c.getScale());
			}
			def.append(")");
		}
        return def.toString();
    }

    /**
     * Returns the type descriptor for the given column's type if that exists in this generator's typemap,
     * else returns the default type.
     */
    protected GenericTypeDescriptor failsafeGetTypeDescriptor(SQLColumn c) {
        GenericTypeDescriptor td = (GenericTypeDescriptor) typeMap.get(new Integer(c.getType()));
		if (td == null) {
		    td = (GenericTypeDescriptor) typeMap.get(getDefaultType());
		    if (td == null) {
		        throw new NullPointerException("Current type map does not have entry for default datatype!");
		    }
		    GenericTypeDescriptor oldType = new GenericTypeDescriptor
		    (c.getSourceDataTypeName(), c.getType(), c.getPrecision(),
		            null, null, c.getNullable(), false, false);
		    oldType.determineScaleAndPrecision();
		    warnings.add(new TypeMapWarning(c, "Unknown Target Type", oldType, td));
		}
        return td;
    }
    
	public void writeTable(SQLTable t) throws SQLException, ArchitectException {
		Map colNameMap = new HashMap();  // for detecting duplicate column names
		// generate a new physical name if necessary
		createPhysicalName(topLevelNames,t); // also adds generated physical name to the map
		print("\nCREATE TABLE ");
		print( toQualifiedName(t) );
		println(" (");
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			
			if (!firstCol) println(",");
			print("                ");

			print(columnDefinition(c,colNameMap));
			// XXX: default values handled only in ETL?

			firstCol = false;
		}
		println("");
		print(")");
		endStatement(DDLStatement.StatementType.CREATE, t);
		
	}
	
	
	/**
	 * Returns the default data type for this platform.  Normally, this can be VARCHAR,
	 * but if your platform doesn't have a varchar, override this method.
	 */
	protected Object getDefaultType() {
		return Types.VARCHAR;
	}

	protected void writePrimaryKey(SQLTable t) throws ArchitectException {
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getPrimaryKeySeq() == null) break;
			if (firstCol) {
				// generate a unique primary key name
                createPhysicalPrimaryKeyName(topLevelNames,t);
				//
				println("");
				print("ALTER TABLE ");
				print( toQualifiedName(t) );
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
			
		}
	}

	protected void writeExportedRelationships(SQLTable t) throws ArchitectException {
		Iterator it = t.getExportedKeys().iterator();
		while (it.hasNext()) {
			SQLRelationship rel = (SQLRelationship) it.next();
			// geneate a physical name for this relationship
			createPhysicalName(topLevelNames,rel); 
			//
			println("");
			print("ALTER TABLE ");
			// this works because all the tables have had their physical names generated already...
			print( toQualifiedName(rel.getFkTable()) );
			
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
				pkCols.append(cmap.getPkColumn().getPhysicalName());
				fkCols.append(cmap.getFkColumn().getPhysicalName());
				firstCol = false;
			}
			print(fkCols.toString());
			println(")");
			print("REFERENCES ");
			print( toQualifiedName(rel.getPkTable()) );
			print(" (");
			print(pkCols.toString());
			print(")");
			endStatement(DDLStatement.StatementType.ADD_FK, t);
			
		}
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData.  Subclasses for specific DB platforms will be
	 * able to override this implementation with one that uses a
	 * static, pre-defined type map.
	 */
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();
		if (con == null || !allowConnection) {
			// Add generic type map
			typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 2000, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
			typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("LONGVARBINARY", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("LONGVARCHAR", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
			typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 3, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 8000, null, null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		}
		else
		{
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTypeInfo();
			while (rs.next()) {
				GenericTypeDescriptor td = new GenericTypeDescriptor(rs);
				typeMap.put(new Integer(td.getDataType()), td);
			}
			rs.close();
		}
	}

	protected void println(String text) {
		ddl.append(text).append(EOL);
	}

	protected void print(String text) {
		ddl.append(text);
	}

	/**
	 * Converts space to underscore in <code>name</code> and returns
	 * the possibly-modified string.  This will not be completely
	 * sufficient because it leaves ".", "%", and lots of other
	 * non-alphanumeric characters alone. Subclasses might choose to
	 * quote and leave everything alone, or whatever.
	 */
	public String toIdentifier(String name) {
        if (name == null) return null;
		else return name.replace(' ', '_');
	}

	/**
     * Creates a fully-qualified table name from the given table's phyiscal name
     * and this DDL Generator's current target schema and catalog.
     * 
     * @param t The table whose name to qualify.  The parents of this table are
     * disregarded; only the DDL Generator's target schema and catalog matter.
     * @return A string of the form <tt>[catalog.][schema.]table</tt> (catalog and
     * schema are omitted if null).
	 */
	public String toQualifiedName(SQLTable t) {
		return toQualifiedName(t.getPhysicalName());
	}

    /**
     * Creates a fully-qualified table name from the given string (which
     * is the non-qualified table name) and this DDL Generator's current
     * target schema and catalog.
     * 
     * @param tname The table name to qualify. Must not contain the name separator
     * character (usually '.').
     * @return A string of the form <tt>[catalog.][schema.]table</tt> (catalog and
     * schema are omitted if null).
     */
    public String toQualifiedName(String tname) {
        String catalog = getTargetCatalog();
        String schema = getTargetSchema();
        
        return DDLUtils.toQualifiedName(catalog, schema, tname);
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
	 * @throws ArchitectException 
     */
	protected String createPhysicalName(Map dupCheck, SQLObject so) {
		logger.debug("transform identifier source: " + so.getPhysicalName());
		so.setPhysicalName(toIdentifier(so.getName()));
        logger.debug("transform identifier result: " + so.getPhysicalName());
		if (dupCheck.get(so.getPhysicalName()) == null) {
			dupCheck.put(so.getPhysicalName(), so);
		} else {
            warnings.add(new NameChangeWarning(so, "Duplicate Name", so.getName()));
		}
						
		return so.getPhysicalName();
	}

	/**
     * Generate, set, and return a physicalPrimaryKeyName. 
     */
	public String createPhysicalPrimaryKeyName(Map dupCheck, SQLTable t) {
	    logger.debug("getting physical primary key name, logical="+t.getPrimaryKeyName()+",physical="+t.getPhysicalPrimaryKeyName());
	    String temp = null;
	    temp = toIdentifier(t.getPrimaryKeyName());
	    logger.debug("transform key identifier result: " + temp);
	    t.setPhysicalPrimaryKeyName(temp);
	    if (dupCheck.get(t.getPhysicalPrimaryKeyName()) == null) {
	        dupCheck.put(t.getPhysicalPrimaryKeyName(),t);
	    } else {
            warnings.add(new NameChangeWarning(t, "Duplicate Primary Key Name", t.getPrimaryKeyName()));
        }
	    return t.getPhysicalPrimaryKeyName();
	}

    /**
     * Generates a standard <code>DROP TABLE $tablename</code> command.  Should work on most platforms. 
     */
    public String makeDropTableSQL(String table) {
        return "DROP TABLE "+toQualifiedName(table);
    }

    /**
     * Generates a command for dropping a foreign key which works on some platforms.
     * The statement looks like <code>ALTER TABLE $fktable DROP FOREIGN KEY $fkname</code>.
     */
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "ALTER TABLE "
            +toQualifiedName(fkTable)
            +" DROP FOREIGN KEY "
            +fkName;
    }

	public List<DDLStatement> getDdlStatements() {
		return ddlStatements;
	}

	public void dropPrimaryKey(SQLTable t) {

		print("ALTER TABLE " + toQualifiedName(t.getName())
			+ " DROP PRIMARY KEY " + t.getPrimaryKeyName());
		endStatement(DDLStatement.StatementType.DROP, t);
	}

	public void addPrimaryKey(SQLTable t) throws ArchitectException {
		Map colNameMap = new HashMap();  
		StringBuffer sqlStatement = new StringBuffer();
		boolean first = true;
		sqlStatement.append("ALTER TABLE "+ toQualifiedName(t.getName())
				+ " ADD PRIMARY KEY (");
		for (SQLColumn c : t.getColumns()) {
			if (c.isPrimaryKey()) {
				if (!first) {
					sqlStatement.append(",");
				}else{
					first =false;
				}
				
				sqlStatement.append(createPhysicalName(colNameMap,c));
			}
		}
		sqlStatement.append(")");
		if (!first)
		{
			print(sqlStatement.toString());
			endStatement(DDLStatement.StatementType.CREATE,t);
		}
	}

	
}
