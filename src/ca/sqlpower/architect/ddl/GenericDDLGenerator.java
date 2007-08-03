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
package ca.sqlpower.architect.ddl;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;
import ca.sqlpower.architect.profile.ProfileFunctionDescriptor;

public class GenericDDLGenerator implements DDLGenerator {

	public static final String GENERATOR_VERSION = "$Revision$";

	private static final Logger logger = Logger.getLogger(GenericDDLGenerator.class);

     /**
     * Check to see if the word word is on the list of reserved words for this database
     * @return
     */
    public boolean isReservedWord(String word){
        return false;
    }
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
     * XXX Consider changing this to a Set as it appears that the values
     * stored in the Map are never used.
	 */
	protected Map<String, SQLObject> topLevelNames;

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

    /**
     * A mapping from JDBC type code (Integer values) to
     * appliable profile functions (min,max,avg,sum,etc...)
     */
    protected Map<String, ProfileFunctionDescriptor> profileFunctionMap;



	public GenericDDLGenerator() throws SQLException {
		allowConnection = true;
		warnings = new ArrayList();
		ddlStatements = new ArrayList();
		ddl = new StringBuffer(500);
		println("");
		topLevelNames = new CaseInsensitiveHashMap();  // for tracking dup table/relationship names
		createTypeMap();
        createProfileFunctionMap();
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


	/**
     *  This is the main entry point from the rest of the application to generate DDL.
	 * @see ca.sqlpower.architect.ddl.DDLGenerator#generateDDLStatements(ca.sqlpower.architect.SQLDatabase)
	 */
	public final List<DDLStatement> generateDDLStatements(SQLDatabase source) throws SQLException, ArchitectException {
        warnings = new ArrayList();
		ddlStatements = new ArrayList<DDLStatement>();
		ddl = new StringBuffer(500);
        topLevelNames = new CaseInsensitiveHashMap();

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
				addTable(t);
                
				writePrimaryKey(t);
                for (SQLIndex index : (List<SQLIndex>)t.getIndicesFolder().getChildren()) {
                   if (index.isPrimaryKeyIndex()) continue;
                    addIndex(index);
                }
			}
			it = source.getChildren().iterator();
			while (it.hasNext()) {
				SQLTable t = (SQLTable) it.next();
				writeExportedRelationships(t);
			}

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
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject> ();
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
		colNameMap.clear();
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
    public String columnType(SQLColumn c) {
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

    /** Columnn type */
    public String getColumnDataTypeName(SQLColumn c) {
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
        GenericTypeDescriptor td = (GenericTypeDescriptor) typeMap.get(Integer.valueOf(c.getType()));
		if (td == null) {
		    td = (GenericTypeDescriptor) typeMap.get(getDefaultType());
		    if (td == null) {
		        throw new NullPointerException("Current type map does not have entry for default datatype!");
		    }
		    GenericTypeDescriptor oldType = new GenericTypeDescriptor
		    (c.getSourceDataTypeName(), c.getType(), c.getPrecision(),
		            null, null, c.getNullable(), false, false);
		    oldType.determineScaleAndPrecision();
		    warnings.add(
                    new TypeMapDDLWarning(c, "Unknown Target Type", oldType, td));
		}
        return td;
    }

	public void addTable(SQLTable t) throws SQLException, ArchitectException {
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
                createPhysicalPrimaryKeyName(t);
				println("");
				print("ALTER TABLE ");
				print( toQualifiedName(t) );
				print(" ADD CONSTRAINT ");
				print(t.getPrimaryKeyName());
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
			typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 2000, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
			typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("LONGVARBINARY", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("LONGVARCHAR", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
			typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 3, null, null, DatabaseMetaData.columnNullable, false, false));
			typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 8000, null, null, DatabaseMetaData.columnNullable, true, false));
			typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		}
		else
		{
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTypeInfo();
			while (rs.next()) {
				GenericTypeDescriptor td = new GenericTypeDescriptor(rs);
				typeMap.put(Integer.valueOf(td.getDataType()), td);
			}
			rs.close();
		}
	}

    protected void createProfileFunctionMap() {
        profileFunctionMap = new HashMap<String, ProfileFunctionDescriptor>();
        profileFunctionMap.put("BIGINT", new ProfileFunctionDescriptor("BIGINT", Types.BIGINT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("BINARY", new ProfileFunctionDescriptor("BINARY", Types.BINARY, true,false,false,false,true,true,true,true));
        profileFunctionMap.put("BIT", new ProfileFunctionDescriptor("BIT", Types.BIT, true,false,false,false,true,true,true,true));
        profileFunctionMap.put("BLOB", new ProfileFunctionDescriptor("BLOB", Types.BLOB, true,false,false,false,true,true,true,true));
        profileFunctionMap.put("CHAR", new ProfileFunctionDescriptor("CHAR", Types.CHAR, true,false,false,false,true,true,true,true));
        profileFunctionMap.put("CLOB", new ProfileFunctionDescriptor("CLOB", Types.CLOB, true,false,false,false,true,true,true,true));
        profileFunctionMap.put("DATE", new ProfileFunctionDescriptor("DATE", Types.DATE, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("DECIMAL", new ProfileFunctionDescriptor("DECIMAL", Types.DECIMAL, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("DOUBLE", new ProfileFunctionDescriptor("DOUBLE", Types.DOUBLE, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("FLOAT", new ProfileFunctionDescriptor("FLOAT", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("INTEGER", new ProfileFunctionDescriptor("INTEGER", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("LONGVARBINARY", new ProfileFunctionDescriptor("LONGVARBINARY", Types.LONGVARBINARY, false,false,false,false,true,true,true,true));
        profileFunctionMap.put("LONGVARCHAR", new ProfileFunctionDescriptor("LONGVARCHAR", Types.LONGVARCHAR, false,false,false,false,true,true,true,true));
        profileFunctionMap.put("NUMERIC", new ProfileFunctionDescriptor("NUMERIC", Types.NUMERIC, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("REAL", new ProfileFunctionDescriptor("REAL", Types.REAL, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("SMALLINT", new ProfileFunctionDescriptor("SMALLINT", Types.SMALLINT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("TIME", new ProfileFunctionDescriptor("TIME", Types.TIME, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("TIMESTAMP", new ProfileFunctionDescriptor("TIMESTAMP", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("TINYINT", new ProfileFunctionDescriptor("TINYINT", Types.TINYINT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("VARBINARY", new ProfileFunctionDescriptor("VARBINARY", Types.VARBINARY, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("VARCHAR", new ProfileFunctionDescriptor("VARCHAR", Types.VARCHAR, true,true,true,false,true,true,true,true));
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

    public Map<String, ProfileFunctionDescriptor> getProfileFunctionMap() {
        return this.profileFunctionMap;
    }
    public void setProfileFunctionMap(Map profileFunctionMap) {
        this.profileFunctionMap = profileFunctionMap;
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
	protected String createPhysicalName(Map<String, SQLObject> dupCheck, SQLObject so) {
		logger.debug("transform identifier source: " + so.getPhysicalName());
		so.setPhysicalName(toIdentifier(so.getName()));
        String physicalName = so.getPhysicalName();
        if(isReservedWord(physicalName)) {
            String renameTo = physicalName   + "2";
            warnings.add(new DuplicateNameDDLWarning(
                    String.format("%s is a reserved word", physicalName),
                    Arrays.asList(new SQLObject[] { so }),
                    String.format("Rename %s to %s", physicalName, renameTo),
                    so, renameTo));
            return physicalName;
        }

        int pointIndex = so.getPhysicalName().lastIndexOf('.');
        if (!so.getName().substring(pointIndex+1,pointIndex+2).matches("[a-zA-Z]")){
            String renameTo;
            if (so instanceof SQLTable) {
                renameTo = "table_" + so.getName();
            } else if (so instanceof SQLColumn) {
                renameTo = "column_" + so.getName();
            } else if (so instanceof SQLIndex) {
                renameTo = "index_" + so.getName();
            } else {
                renameTo = "X_" + so.getName();
            }
            warnings.add(new DuplicateNameDDLWarning(
                    String.format("Name %s starts with a non-alpha character", physicalName),
                    Arrays.asList(new SQLObject[] { so }),
                    String.format("Rename %s to %s", physicalName, renameTo),
                    so, renameTo));
            return physicalName;
        }

        logger.debug("transform identifier result: " + so.getPhysicalName());
        // XXX should change checkDupName(Map where, so.getPhysicalName(), so, "Duplicate Physical Name", so.getName());

        String physicalName2 = so.getPhysicalName();
        SQLObject object = dupCheck.get(physicalName2);
        if (object == null) {
			dupCheck.put(physicalName2, so);
        } else {
            String renameTo2 = physicalName2 + "2";
            String message;
            if (so instanceof SQLColumn) {
                message = String.format("Column name %s in table %s already in use", 
                        so.getName(), 
                        ((SQLColumn) so).getParentTable().getName());
            } else {
                message = String.format("Global name %s already in use", physicalName);
            }
                    warnings.add(new DuplicateNameDDLWarning(
                            message,
                            Arrays.asList(new SQLObject[] { so, object }),
                            String.format("Rename %s to %s", physicalName, renameTo2),
                            so, renameTo2));
		}

		return so.getPhysicalName();
	}

	/**
     * Generate, set, and return a physicalPrimaryKeyName which is just the
     * logical primary key name run through "toIdentifier()".
     * Before returning it, run it past checkDupName to check in and add
     * it to the topLevelNames Map.
	 * @throws ArchitectException 
     */

    private String createPhysicalPrimaryKeyName(SQLTable t) throws ArchitectException {
        String physName = toIdentifier(t.getPrimaryKeyName());
        t.setPhysicalPrimaryKeyName(physName);
        checkDupName(physName, t,
                "Duplicate Primary Key Name", physName);
        return physName;
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

		try {
            print("ALTER TABLE " + toQualifiedName(t.getName())
            	+ " DROP PRIMARY KEY " + t.getPrimaryKeyName());
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
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



    public String caseWhenNull(String expression, String then) {

        StringBuffer sql = new StringBuffer();
        sql.append("CASE WHEN ");
        sql.append(expression);
        sql.append(" IS NULL THEN ");
        sql.append(then);
        sql.append(" END");

        return sql.toString();
    }

    public String getStringLengthSQLFunctionName(String expression) {
        return "LENGTH("+expression+")";
    }

    public String getAverageSQLFunctionName(String expression) {
        return "AVG("+expression+")";
    }


    protected final void checkDupIndexname(SQLIndex index) {
        String name = index.getName();
        checkDupName(name, index.getParentTable(), "Index name is not unique", name);
    }

    /**
     * Check that given name is not already present in the top level namespace.
     * @param name The top level name to be checked.
     * @param obj The value to go with the name.
     * @param warning The text of the name change warning to generate.
     * @param name2 The name to go in the name change warning.
     */
    protected final void checkDupName(String name,
            SQLObject obj,
            String warning,
            String name2) {
        if (name.equals(name2)) {
            System.err.println("Error: checkDupName called with newname == oldname");
        }
        final SQLObject object = topLevelNames.get(name);
        if (object == null) {
            topLevelNames.put(name, obj);
        } else {
            warnings.add(
                    new DuplicateNameDDLWarning(
                            String.format("Global name %s already in use", name),
                            Arrays.asList(new SQLObject[] { obj, object }),
                            String.format("Rename %s to %s", name, name + "2"),
                            obj, name + "2"));
        }
    }
    
    /**
     * Adds a DDL statement to this generator that will create the
     * given index.
     *
     * @param index The specification of the index to create.  Note,
     * if the index type is STATISTIC, no DDL will be generated because
     * STATISTIC indices are just artificial JDBC constructs to describe
     * table statistics (you can't create or drop them).
     */
    public void addIndex(SQLIndex index) throws ArchitectException {
        if (index.getType() == IndexType.STATISTIC )
            return;

        checkDupIndexname(index);

        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }

        print("INDEX ");
        print(toQualifiedName(index.getName()));
        print("\n ON ");
        print(toQualifiedName(index.getParentTable()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : (List<SQLIndex.Column>) index.getChildren()) {
            if (!first) print(", ");
            print(c.getName());
            print(c.isAscending() ? " ASC" : "");
            print(c.isDescending() ? " DESC" : "");
            first = false;
        }
        print(" )");
        endStatement(DDLStatement.StatementType.CREATE, index);
    }


}
