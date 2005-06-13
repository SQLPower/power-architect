package ca.sqlpower.architect.ddl;

import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;
import java.util.regex.*;

public class PostgresDDLGenerator extends GenericDDLGenerator {
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

	private static boolean isReservedWord(String word) {
		return reservedWords.contains(word.toUpperCase());
	}


	public void writeHeader() {
		println("-- Created by SQLPower PostgreSQL DDL Generator "+GENERATOR_VERSION+" --");
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData, but ignores nullability as reported by the
	 * driver's type map (because all types are reported as
	 * non-nullable).
	 */
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();
		
		typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("NUMERIC", Types.BIGINT, 1000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("BYTEA", Types.BINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("BYTEA", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("TEXT", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("NUMERIC", Types.DECIMAL, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE PRECISION", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("REAL", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("BYTEA", Types.LONGVARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("TEXT", Types.LONGVARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("SMALLINT", Types.TINYINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("BYTEA", Types.VARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
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
     * 
     * <p>XXX: substring replacement routine does not play well with regex chars like ^ and | 
	 */
	public String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		if (logger.isDebugEnabled()) logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toLowerCase();
		if (logger.isDebugEnabled()) logger.debug("after replace of spaces: " + ident);
		// make sure first character is alpha
 		Pattern p = Pattern.compile("^[^a-zA-Z]+");
 		Matcher m = p.matcher(ident);
		if (m.find()) {
			// just add something alpha to the front for now
			ident = "X" + ident;
			if (logger.isDebugEnabled()) logger.debug("identifiers must start with letter; prepending X: " + ident);
		}
		// see if it's a reserved word, and add something alpha to front if it is...
		if (isReservedWord(ident)) {
			ident = "X" + ident;
			if (logger.isDebugEnabled()) logger.debug("identifier was reserved word, prepending X: " + ident);
		}

		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_$]", "_");

		// first time through
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

	public String toIdentifier(String name) {
		return toIdentifier(name,null);
	}
	
	/**
     * Generates a command for dropping a foreign key on oracle.
     * The statement looks like <code>ALTER TABLE $fktable DROP CONSTRAINT $fkname</code>.
     */
    public String makeDropForeignKeySQL(String fkCatalog, String fkSchema, String fkTable, String fkName) {
        return "ALTER TABLE "
            +DDLUtils.toQualifiedName(fkCatalog, fkSchema, fkTable)
            +" DROP CONSTRAINT "
            +fkName;
    }
    
	/**
	 * Returns null, even though Postgres calls this "Database."  The reason is,
	 * you can't refer to objects in a different database than the default
	 * database for your current connection.  Also, the Postgres DatabaseMetaData
	 * always shows nulls for the catalog/database name of tables.
	 */
	public String getCatalogTerm() {
		return null;
	}
	
	/**
	 * Returns "Schema".
	 */
	public String getSchemaTerm() {
		return "Schema";
	}
	
	/**
	 * Returns the previously-set target schema name, or "public" if there is no
	 * current setting. Public is the Postgres default when no schema is
	 * specified.
	 */
	public String getTargetSchema() {
		if (targetSchema != null) return targetSchema;
		else return "public";
	}
}
