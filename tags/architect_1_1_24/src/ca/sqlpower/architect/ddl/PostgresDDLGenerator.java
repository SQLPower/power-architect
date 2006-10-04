package ca.sqlpower.architect.ddl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileFunctionDescriptor;

/**
 * DDL Generator for Postgres 8.x (does not support e.g., ALTER COLUMN operations 7.[34]).
 */
public class PostgresDDLGenerator extends GenericDDLGenerator {
    
	public PostgresDDLGenerator() throws SQLException {
		super();
   	}

	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(PostgresDDLGenerator.class);

	private static HashSet reservedWords;
	
	static {
		reservedWords = new HashSet();
		reservedWords.add("ABORT");
		reservedWords.add("ABSOLUTE");
		reservedWords.add("ACTION");
		reservedWords.add("ADD");
		reservedWords.add("ALL");
		reservedWords.add("ALLOCATE");
		reservedWords.add("ALTER");
		reservedWords.add("ANALYZE");
		reservedWords.add("AND");
		reservedWords.add("ANY");
		reservedWords.add("ARE");
		reservedWords.add("AS");
		reservedWords.add("ASC");
		reservedWords.add("ASSERTION");
		reservedWords.add("AT");
		reservedWords.add("AUTHORIZATION");
		reservedWords.add("AVG");
		reservedWords.add("BEGIN");
		reservedWords.add("BETWEEN");
		reservedWords.add("BINARY");
		reservedWords.add("BIT");
		reservedWords.add("BIT_LENGTH");
		reservedWords.add("BOOLEAN");
		reservedWords.add("BOTH");
		reservedWords.add("BY");
		reservedWords.add("CASCADE");
		reservedWords.add("CASCADED");
		reservedWords.add("CASE");
		reservedWords.add("CAST");
		reservedWords.add("CATALOG");
		reservedWords.add("CHAR");
		reservedWords.add("CHARACTER");
		reservedWords.add("CHARACTER_LENGTH");
		reservedWords.add("CHAR_LENGTH");
		reservedWords.add("CHECK");
		reservedWords.add("CLOSE");
		reservedWords.add("CLUSTER");
		reservedWords.add("COALESCE");
		reservedWords.add("COLLATE");
		reservedWords.add("COLLATION");
		reservedWords.add("COLUMN");
		reservedWords.add("COMMIT");
		reservedWords.add("CONNECT");
		reservedWords.add("CONNECTION");
		reservedWords.add("CONSTRAINT");
		reservedWords.add("CONSTRAINTS");
		reservedWords.add("CONTINUE");
		reservedWords.add("CONVERT");
		reservedWords.add("COPY");
		reservedWords.add("CORRESPONDING");
		reservedWords.add("COUNT");
		reservedWords.add("CREATE");
		reservedWords.add("CROSS");
		reservedWords.add("CURRENT");
		reservedWords.add("CURRENT_DATE");
		reservedWords.add("CURRENT_SESSION");
		reservedWords.add("CURRENT_TIME");
		reservedWords.add("CURRENT_TIMESTAMP");
		reservedWords.add("CURRENT_USER");
		reservedWords.add("CURSOR");
		reservedWords.add("DATE");
		reservedWords.add("DATETIME");
		reservedWords.add("DAY");
		reservedWords.add("DEALLOCATE");
		reservedWords.add("DEC");
		reservedWords.add("DECIMAL");
		reservedWords.add("DECLARE");
		reservedWords.add("DEFAULT");
		reservedWords.add("DEFERRABLE");
		reservedWords.add("DEFERRED");
		reservedWords.add("DELETE");
		reservedWords.add("DESC");
		reservedWords.add("DESC");
		reservedWords.add("DESCRIBE");
		reservedWords.add("DESCRIPTOR");
		reservedWords.add("DIAGNOSTICS");
		reservedWords.add("DISCONNECT");
		reservedWords.add("DISTINCT");
		reservedWords.add("DO");
		reservedWords.add("DOMAIN");
		reservedWords.add("DOUBLE");
		reservedWords.add("DROP");
		reservedWords.add("ELSE");
		reservedWords.add("END");
		reservedWords.add("ESCAPE");
		reservedWords.add("EXCEPT");
		reservedWords.add("EXCEPTION");
		reservedWords.add("EXEC");
		reservedWords.add("EXECUTE");
		reservedWords.add("EXISTS");
		reservedWords.add("EXPLAIN");
		reservedWords.add("EXTEND");
		reservedWords.add("EXTERNAL");
		reservedWords.add("EXTRACT");
		reservedWords.add("EXTRACT");
		reservedWords.add("FALSE");
		reservedWords.add("FETCH");
		reservedWords.add("FIRST");
		reservedWords.add("FLOAT");
		reservedWords.add("FLOAT");
		reservedWords.add("FOR");
		reservedWords.add("FOR");
		reservedWords.add("FOREIGN");
		reservedWords.add("FOUND");
		reservedWords.add("FROM");
		reservedWords.add("FULL");
		reservedWords.add("FULL");
		reservedWords.add("GET");
		reservedWords.add("GLOBAL");
		reservedWords.add("GO");
		reservedWords.add("GOTO");
		reservedWords.add("GRANT");
		reservedWords.add("GROUP");
		reservedWords.add("HAVING");
		reservedWords.add("HOUR");
		reservedWords.add("IDENTITY");
		reservedWords.add("IMMEDIATE");
		reservedWords.add("IN");
		reservedWords.add("IN");
		reservedWords.add("INDICATOR");
		reservedWords.add("INITIALLY");
		reservedWords.add("INNER");
		reservedWords.add("INPUT");
		reservedWords.add("INSENSITIVE");
		reservedWords.add("INSERT");
		reservedWords.add("INT");
		reservedWords.add("INTEGER");
		reservedWords.add("INTERSECT");
		reservedWords.add("INTERVAL");
		reservedWords.add("INTO");
		reservedWords.add("IS");
		reservedWords.add("IS");
		reservedWords.add("ISOLATION");
		reservedWords.add("JOIN");
		reservedWords.add("JOIN");
		reservedWords.add("KEY");
		reservedWords.add("LANGUAGE");
		reservedWords.add("LAST");
		reservedWords.add("LEADING");
		reservedWords.add("LEFT");
		reservedWords.add("LEFT");
		reservedWords.add("LEVEL");
		reservedWords.add("LIKE");
		reservedWords.add("LIKE");
		reservedWords.add("LISTEN");
		reservedWords.add("LOAD");
		reservedWords.add("LOCAL");
		reservedWords.add("LOCAL");
		reservedWords.add("LOCK");
		reservedWords.add("LOWER");
		reservedWords.add("MATCH");
		reservedWords.add("MAX");
		reservedWords.add("MIN");
		reservedWords.add("MINUTE");
		reservedWords.add("MODULE");
		reservedWords.add("MONTH");
		reservedWords.add("MOVE");
		reservedWords.add("NAMES");
		reservedWords.add("NATIONAL");
		reservedWords.add("NATURAL");
		reservedWords.add("NATURAL");
		reservedWords.add("NCHAR");
		reservedWords.add("NCHAR");
		reservedWords.add("NEW");
		reservedWords.add("NEXT");
		reservedWords.add("NO");
		reservedWords.add("NONE");
		reservedWords.add("NOT");
		reservedWords.add("NOTIFY");
		reservedWords.add("NULL");
		reservedWords.add("NULL");
		reservedWords.add("NULLIF");
		reservedWords.add("NUMERIC");
		reservedWords.add("OCTET_LENGTH");
		reservedWords.add("OF");
		reservedWords.add("OFFSET");
		reservedWords.add("ON");
		reservedWords.add("ON");
		reservedWords.add("ONLY");
		reservedWords.add("OPEN");
		reservedWords.add("OPTION");
		reservedWords.add("OR");
		reservedWords.add("OR");
		reservedWords.add("ORDER");
		reservedWords.add("OUTER");
		reservedWords.add("OUTPUT");
		reservedWords.add("OVERLAPS");
		reservedWords.add("OVERLAPS");
		reservedWords.add("PARTIAL");
		reservedWords.add("PENDANT");
		reservedWords.add("POSITION");
		reservedWords.add("PRECISION");
		reservedWords.add("PREPARE");
		reservedWords.add("PRESERVE");
		reservedWords.add("PRIMARY");
		reservedWords.add("PRIOR");
		reservedWords.add("PRIVILEGES");
		reservedWords.add("PRIVILEGES");
		reservedWords.add("PROCEDURE");
		reservedWords.add("PUBLIC");
		reservedWords.add("PUBLIC");
		reservedWords.add("READ");
		reservedWords.add("REAL");
		reservedWords.add("REFERENCES");
		reservedWords.add("RELATIVE");
		reservedWords.add("RESET");
		reservedWords.add("RESTRICT");
		reservedWords.add("REVOKE");
		reservedWords.add("RIGHT");
		reservedWords.add("ROLLBACK");
		reservedWords.add("ROWS");
		reservedWords.add("SCHEMA");
		reservedWords.add("SCROLL");
		reservedWords.add("SECOND");
		reservedWords.add("SECTION");
		reservedWords.add("SELECT");
		reservedWords.add("SESSION");
		reservedWords.add("SESSION_USER");
		reservedWords.add("SET");
		reservedWords.add("SETOF");
		reservedWords.add("SHOW");
		reservedWords.add("SIZE");
		reservedWords.add("SMALLINT");
		reservedWords.add("SOME");
		reservedWords.add("SQL");
		reservedWords.add("SQLCODE");
		reservedWords.add("SQLERROR");
		reservedWords.add("SQLSTATE");
		reservedWords.add("SUBSTRING");
		reservedWords.add("SUM");
		reservedWords.add("SYSTEM_USER");
		reservedWords.add("TABLE");
		reservedWords.add("TEMPORARY");
		reservedWords.add("THEN");
		reservedWords.add("TIME");
		reservedWords.add("TIMESPAN");
		reservedWords.add("TIMESTAMP");
		reservedWords.add("TIMEZONE_HOUR");
		reservedWords.add("TIMEZONE_MINUTE");
		reservedWords.add("TO");
		reservedWords.add("TO");
		reservedWords.add("TRAILING");
		reservedWords.add("TRANSACTION");
		reservedWords.add("TRANSLATE");
		reservedWords.add("TRANSLATION");
		reservedWords.add("TRIGGER");
		reservedWords.add("TRIM");
		reservedWords.add("TRUE");
		reservedWords.add("UNION");
		reservedWords.add("UNION");
		reservedWords.add("UNIQUE");
		reservedWords.add("UNKNOWN");
		reservedWords.add("UNLISTEN");
		reservedWords.add("UNTIL");
		reservedWords.add("UPDATE");
		reservedWords.add("UPPER");
		reservedWords.add("USAGE");
		reservedWords.add("USER");
		reservedWords.add("USING");
		reservedWords.add("VACUUM");
		reservedWords.add("VALUE");
		reservedWords.add("VALUES");
		reservedWords.add("VARCHAR");
		reservedWords.add("VARYING");
		reservedWords.add("VERBOSE");
		reservedWords.add("VIEW");
		reservedWords.add("WHEN");
		reservedWords.add("WHENEVER");
		reservedWords.add("WHERE");
		reservedWords.add("WITH");
		reservedWords.add("WORK");
		reservedWords.add("WRITE");
		reservedWords.add("YEAR");
		reservedWords.add("ZONE");
	}

    @Override
	public boolean isReservedWord(String word) {
		return reservedWords.contains(word.toUpperCase());
	}

	@Override
	public void writeHeader() {
		println("-- Created by SQLPower PostgreSQL DDL Generator "+GENERATOR_VERSION+" --");
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData, but ignores nullability as reported by the
	 * driver's type map (because all types are reported as
	 * non-nullable).
	 */
	@Override
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();
		
		typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("NUMERIC", Types.BIGINT, 1000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("BYTEA", Types.BINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("BYTEA", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("TEXT", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("NUMERIC", Types.DECIMAL, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE PRECISION", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("REAL", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("BYTEA", Types.LONGVARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("TEXT", Types.LONGVARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("SMALLINT", Types.TINYINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("BYTEA", Types.VARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}
	
    @Override
    protected void createProfileFunctionMap() {
        profileFunctionMap = new HashMap();
        profileFunctionMap.put("varchar", new ProfileFunctionDescriptor("varchar", Types.VARCHAR, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("char", new ProfileFunctionDescriptor("char", Types.CHAR, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("name", new ProfileFunctionDescriptor("name", Types.CHAR, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("bpchar", new ProfileFunctionDescriptor("bpchar", Types.CHAR, true,true,true,false,true,true,true,true));
                
        profileFunctionMap.put("date", new ProfileFunctionDescriptor("date", Types.DATE, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("time", new ProfileFunctionDescriptor("time", Types.TIME, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("timestamp", new ProfileFunctionDescriptor("timestamp", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("timestamptz", new ProfileFunctionDescriptor("timestamptz", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("timetz", new ProfileFunctionDescriptor("timetz", Types.TIME, true,true,true,false,true,true,true,true));
        
        
        profileFunctionMap.put("float4", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("float8", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("int2", new ProfileFunctionDescriptor("int", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("int4", new ProfileFunctionDescriptor("int", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("int8", new ProfileFunctionDescriptor("int", Types.INTEGER, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("float4", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("float4", new ProfileFunctionDescriptor("float", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("numeric", new ProfileFunctionDescriptor("numeric", Types.FLOAT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("interval", new ProfileFunctionDescriptor("interval", Types.FLOAT, true,true,true,true,true,true,true,true));
        
        
        profileFunctionMap.put("bool", new ProfileFunctionDescriptor("bool", Types.BOOLEAN, true,false,false,false,false,false,false,true));
        profileFunctionMap.put("bit", new ProfileFunctionDescriptor("bit", Types.BIT, true,false,false,false,false,false,false,true));
        
        profileFunctionMap.put("bytea", new ProfileFunctionDescriptor("bytea", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("text", new ProfileFunctionDescriptor("text", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("oid", new ProfileFunctionDescriptor("oid", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("xid", new ProfileFunctionDescriptor("xid", Types.BLOB, false,false,false,false,false,false,false,true));
        profileFunctionMap.put("cid", new ProfileFunctionDescriptor("cid", Types.BLOB, false,false,false,false,false,false,false,true));

        /* add data type start with '_' like _int4 to the mapping */
        Map profileFunctionMap2 = new HashMap();
        for ( Map.Entry<String, ProfileFunctionDescriptor> entry : profileFunctionMap.entrySet() ) {
            String key = entry.getKey();
            ProfileFunctionDescriptor pfd = entry.getValue();
            profileFunctionMap2.put("_"+key, pfd);
        }
        profileFunctionMap.putAll(profileFunctionMap2);
    }

	/**
	 * Turns a logical identifier into a legal identifier (physical name) for PostgreSQL.  
     * Also, downcases the identifier for consistency.  
     * 
     * <p>Uses a deterministic method to generate tie-breaking numbers when there is a namespace 
     * conflict.  If you pass null as the physical name, it will use just the logical name when 
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal     
     * identifier which will be used with the logical name to generate a another hash.
     * 
     * <p>Postgres 8.0 rules:
     * <ul>
     *  <li> no spaces
     *  <li> 63 character limit
     *  <li> identifiers must begin with a letter (one is added if needed)
     *  <li> can't be a postgres reserved word 
     *  <li> can only be comprised of letters, numbers, underscores, and $ 
     * </ul> 
	 */
	private String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		if (logger.isDebugEnabled()) logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toLowerCase();
		if (logger.isDebugEnabled()) logger.debug("after replace of spaces: " + ident);
		

		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_$]", "_");

		// first time through
        // XXX clean this up
		if (physicalName == null) {
			// length is ok
            if (ident.length() <= 63) {
				return ident;
			} else {
				// length is too big
				if (logger.isDebugEnabled()) logger.debug("truncating identifier: " + ident);
				String base = ident.substring(0, 60);
				int tiebreaker = ((ident.hashCode() % 1000) + 1000) % 1000;
				if (logger.isDebugEnabled()) logger.debug("new identifier: " + base + tiebreaker);
				return (base + tiebreaker);
			}						
		} else {
			// back for more, which means that we probably 
            // had a namespace conflict.  Hack the ident down
            // to size if it's too big, and then generate 
            // a hash tiebreaker using the ident and the 
            // passed value physicalName
			if (logger.isDebugEnabled()) logger.debug("physical identifier is not unique, regenerating: " + physicalName);
			String base = ident;
			if (ident.length() > 63) {
				base = ident.substring(0, 60);
			}
			int tiebreaker = (((ident + physicalName).hashCode() % 1000) + 1000) % 1000;
			if (logger.isDebugEnabled()) logger.debug("regenerated identifier is: " + (base + tiebreaker));
			return (base + tiebreaker);
		}
	}	

    @Override
	public String toIdentifier(String name) {
		return toIdentifier(name,null);
	}
	
	/**
     * Generates a command for dropping a foreign key.
     * The statement looks like <code>ALTER TABLE ONLY $fktable DROP CONSTRAINT $fkname</code>.
     */
    @Override
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "\n ALTER TABLE ONLY "
            + toQualifiedName(fkTable)
            + " DROP CONSTRAINT "
            + fkName;
    }
    
    @Override
    public void modifyColumn(SQLColumn c) {
        Map colNameMap = new HashMap(); 
        SQLTable t = c.getParentTable();
        print("\n ALTER TABLE ONLY ");
        print( toQualifiedName(t) );
        print(" ALTER COLUMN ");
        
        // Column name
        String columnPhysName = createPhysicalName(colNameMap,c);
        print(columnPhysName);
        print(" TYPE ");       
        print(columnType(c));
        
        // Column nullability
        print(", ALTER COLUMN ");
        print(columnPhysName);
        print(" ");
        print(c.isDefinitelyNullable() ? "DROP" : "SET");
        print(" NOT NULL");

        endStatement(DDLStatement.StatementType.MODIFY, c);
        
    }
    
	/**
	 * Returns null, even though Postgres calls this "Database."  The reason is,
	 * you can't refer to objects in a different database than the default
	 * database for your current connection.  Also, the Postgres DatabaseMetaData
	 * always shows nulls for the catalog/database name of tables.
	 */
    @Override
	public String getCatalogTerm() {
		return null;
	}
	
	/**
	 * Returns "Schema".
	 */
    @Override
	public String getSchemaTerm() {
		return "Schema";
	}
	
	/**
	 * Returns the previously-set target schema name, or "public" if there is no
	 * current setting. Public is the Postgres default when no schema is
	 * specified.
	 */
    @Override
	public String getTargetSchema() {
		if (targetSchema != null) return targetSchema;
		else return "public";
	}
}
