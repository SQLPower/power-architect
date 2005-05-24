package ca.sqlpower.architect.ddl;

import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;
import java.util.regex.*;
import java.io.*;


public class PostgresDDLGenerator extends GenericDDLGenerator {
	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(PostgresDDLGenerator.class);

	private static ArrayList reservedWords;
	
	static {
		reservedWords = new ArrayList();		
		BufferedReader br = null;
		try {
			br = new BufferedReader (new FileReader("postgres_reserved_words.txt"));		
			while (br.ready()) {
				String s = br.readLine();
				if (s != null && s.length() > 0) {
					reservedWords.add(s);
				}
			}
		} catch (IOException ie) {
			logger.error("problem parsing reserved words file", ie);
		} finally { 
   			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ie2) {
				logger.error("problem closing reserved words file");
			}
		}
	}

	private static boolean isReservedWord(String word) {
		boolean found = false;
		Iterator it = reservedWords.iterator();
		while (!found && it.hasNext()) {
			String s = (String) it.next();
			if (word.toUpperCase().equals(s.toUpperCase())) {
				found = true;
			}
		}
		return found;
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
	 * Turn a logical identifier into a legal identifier (physical name) for this database.  
     * Also, upcase the identifier for consistency.  
     * 
     * Uses a deterministic method to generate tie-breaking numbers when there is a namespace 
     * conflict.  If you pass null as the physical name, it will use just the logical name when 
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal     
     * identifier which will be used with the logical name to generate a another hash.
     * 
     * Postgres 8.0 rules:
     * 
     * - no spaces
     * - 63 character limit
     * - identifiers must begin with a letter (one is added if needed)
     * - can't be a postgres reserved word 
     * - can only be comprised of letters, numbers, underscores, and $ 
     *
     * XXX: substring replacement routine does not play well with regex chars like ^ and | 
	 */
	public String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toUpperCase();
		logger.debug("after replace of spaces: " + ident);
		// make sure first character is alpha
 		Pattern p = Pattern.compile("^[^a-zA-Z]+");
 		Matcher m = p.matcher(ident);
		if (m.find()) {
			// just add something alpha to the front for now
			ident = "X" + ident;
			logger.debug("identifiers must start with letter, appending X: " + ident);
		}
		// see if it's a reserved word, and add something alpha to front if it is...
		if (isReservedWord(ident)) {
			ident = "X" + ident;
			logger.debug("identifier was reserved word, appending X: " + ident);
		}
		// replace anything that is not a letter, character, or underscore with an underscore...
		String tempString = ident;
		Pattern p2 = Pattern.compile("[^a-xA-Z0-9_$]");
		Matcher m2 = p2.matcher(ident);
		while (m2.find()) {
			tempString = tempString.replace(m2.group(),"_");						
		}
		// first time through
		if (physicalName == null) {
			// length is ok
            if (ident.length() < 129) {
				return ident;
			} else {
				// length is too big
				logger.debug("truncating identifier: " + ident);
				String base = ident.substring(0,125);
				int tiebreaker = ((ident.hashCode() % 1000) + 1000) % 1000;
				logger.debug("new identifier: " + base + tiebreaker);
				return (base + tiebreaker);
			}						
		} else {
			// back for more, which means that we probably 
            // had a namespace conflict.  Hack the ident down
            // to size if it's too big, and then generate 
            // a hash tiebreaker using the ident and the 
            // passed value physicalName
			logger.debug("physical identifier is not unique, regenerating: " + physicalName);
			String base = ident;
			if (ident.length() > 125) {
				base = ident.substring(0,125);
			}
			int tiebreaker = (((ident + physicalName).hashCode() % 1000) + 1000) % 1000;
			logger.debug("regenerated identifier is: " + (base + tiebreaker));
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
