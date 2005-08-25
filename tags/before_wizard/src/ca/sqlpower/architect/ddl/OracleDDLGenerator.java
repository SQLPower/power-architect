package ca.sqlpower.architect.ddl;

import java.sql.*;
import java.util.*;
import java.util.regex.*;
import org.apache.log4j.Logger;             

public class OracleDDLGenerator extends GenericDDLGenerator {
	public static final String GENERATOR_VERSION = "$Revision$";

	private static final Logger logger = Logger.getLogger(OracleDDLGenerator.class);

	private static HashSet reservedWords;
	
	static {
		reservedWords = new HashSet();
		reservedWords.add("ACCESS");
		reservedWords.add("ADD");
		reservedWords.add("ALL");
		reservedWords.add("ALTER");
		reservedWords.add("AND");
		reservedWords.add("ANY");
		reservedWords.add("ARRAYLEN");
		reservedWords.add("AS");
		reservedWords.add("ASC");
		reservedWords.add("AUDIT");
		reservedWords.add("BETWEEN");
		reservedWords.add("BY");
		reservedWords.add("CHAR");
		reservedWords.add("CHECK");
		reservedWords.add("CLUSTER");
		reservedWords.add("COLUMN");
		reservedWords.add("COMMENT");
		reservedWords.add("COMPRESS");
		reservedWords.add("CONNECT");
		reservedWords.add("CREATE");
		reservedWords.add("CURRENT");
		reservedWords.add("DATE");
		reservedWords.add("DECIMAL");
		reservedWords.add("DEFAULT");
		reservedWords.add("DELETE");
		reservedWords.add("DESC");
		reservedWords.add("DISTINCT");
		reservedWords.add("DROP");
		reservedWords.add("ELSE");
		reservedWords.add("EXCLUSIVE");
		reservedWords.add("EXISTS");
		reservedWords.add("FILE");
		reservedWords.add("FLOAT");
		reservedWords.add("FOR");
		reservedWords.add("FROM");
		reservedWords.add("GRANT");
		reservedWords.add("GROUP");
		reservedWords.add("HAVING");
		reservedWords.add("IDENTIFIED");
		reservedWords.add("IMMEDIATE");
		reservedWords.add("IN");
		reservedWords.add("INCREMENT");
		reservedWords.add("INDEX");
		reservedWords.add("INITIAL");
		reservedWords.add("INSERT");
		reservedWords.add("INTEGER");
		reservedWords.add("INTERSECT");
		reservedWords.add("INTO");
		reservedWords.add("IS");
		reservedWords.add("LEVEL");
		reservedWords.add("LIKE");
		reservedWords.add("LOCK");
		reservedWords.add("LONG");
		reservedWords.add("MAXEXTENTS");
		reservedWords.add("MINUS");
		reservedWords.add("MODE");
		reservedWords.add("MODIFY");
		reservedWords.add("NOAUDIT");
		reservedWords.add("NOCOMPRESS");
		reservedWords.add("NOT");
		reservedWords.add("NOTFOUND");
		reservedWords.add("NOWAIT");
		reservedWords.add("NULL");
		reservedWords.add("NUMBER");
		reservedWords.add("OF");
		reservedWords.add("OFFLINE");
		reservedWords.add("ON");
		reservedWords.add("ONLINE");
		reservedWords.add("OPTION");
		reservedWords.add("OR");
		reservedWords.add("ORDER");
		reservedWords.add("PCTFREE");
		reservedWords.add("PRIOR");
		reservedWords.add("PRIVILEGES");
		reservedWords.add("PUBLIC");
		reservedWords.add("RAW");
		reservedWords.add("RENAME");
		reservedWords.add("RESOURCE");
		reservedWords.add("REVOKE");
		reservedWords.add("ROW");
		reservedWords.add("ROWID");
		reservedWords.add("ROWLABEL");
		reservedWords.add("ROWNUM");
		reservedWords.add("ROWS");
		reservedWords.add("START");
		reservedWords.add("SELECT");
		reservedWords.add("SESSION");
		reservedWords.add("SET");
		reservedWords.add("SHARE");
		reservedWords.add("SIZE");
		reservedWords.add("SMALLINT");
		reservedWords.add("SQLBUF");
		reservedWords.add("SUCCESSFUL");
		reservedWords.add("SYNONYM");
		reservedWords.add("SYSDATE");
		reservedWords.add("TABLE");
		reservedWords.add("THEN");
		reservedWords.add("TO");
		reservedWords.add("TRIGGER");
		reservedWords.add("UID");
		reservedWords.add("UNION");
		reservedWords.add("UNIQUE");
		reservedWords.add("UPDATE");
		reservedWords.add("USER");
		reservedWords.add("VALIDATE");
		reservedWords.add("VALUES");
		reservedWords.add("VARCHAR");
		reservedWords.add("VARCHAR2");
		reservedWords.add("VIEW");
		reservedWords.add("WHENEVER");
		reservedWords.add("WHERE");
		reservedWords.add("WITH");		
	}

	private static boolean isReservedWord(String word) {
		return reservedWords.contains(word.toUpperCase());
	}
		
	public void writeHeader() {
		println("-- Created by SQLPower Oracle 8i/9i DDL Generator "+GENERATOR_VERSION+" --");
	}

	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();
		
		typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("NUMBER", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("RAW", Types.BINARY, 2000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("NUMBER", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 2000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("NUMBER", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("NUMBER", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("NUMBER", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("NUMBER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("LONG RAW", Types.LONGVARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("VARCHAR2", Types.LONGVARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("NUMBER", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("NUMBER", Types.SMALLINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("DATE", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("DATE", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("NUMBER", Types.TINYINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("LONG RAW", Types.VARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR2", Types.VARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

	/**
	 * Turn a logical identifier into a legal identifier (physical name) for Oracle 8i/9i.  
     * Also, upcase the identifier for consistency.  
     * 
     * <p>Uses a deterministic method to generate tie-breaking numbers when there is a namespace 
     * conflict.  If you pass null as the physical name, it will use just the logical name when 
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal     
     * identifier which will be used with the logical name to generate a another hash.
     * 
     * <p>Oracle Rules:
     * <ul>
     *  <li> no spaces
     *  <li> 30 character limit
     *  <li> identifiers must begin with a letter (the offending chunk of characters is moved to the 
     *       back of the new physical identifier)
     *  <li> can't be an oracle reserved word
     *  <li> can only be comprised of letters, numbers, and underscores (XXX: does not play well with regex chars like ^ and |)
     * </ul>
	 */
	public String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		if (logger.isDebugEnabled()) logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toUpperCase();
		if (logger.isDebugEnabled()) logger.debug("after replace of spaces: " + ident);
		// make sure first character is alpha
 		Pattern p = Pattern.compile("^[^a-zA-Z]+");
 		Matcher m = p.matcher(ident);
		if (m.find()) {
			// just add something alpha to the front for now
			ident = "X" + ident;
			if (logger.isDebugEnabled()) logger.debug("identifiers must start with letter, appending X: " + ident);
		}
		// see if it's a reserved word, and add something alpha to front if it is...
		if (isReservedWord(ident)) {
			ident = "X" + ident;
			if (logger.isDebugEnabled()) logger.debug("identifier was reserved word, appending X: " + ident);
		}

		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_]", "_");

		// first time through
		if (physicalName == null) {
			// length is ok
            if (ident.length() < 31) {
				return ident;
			} else {
				// length is too big
				if (logger.isDebugEnabled()) logger.debug("truncating identifier: " + ident);
				String base = ident.substring(0,27);
				int tiebreaker = ((ident.hashCode() % 1000) + 1000) % 1000;
				if (logger.isDebugEnabled()) logger.debug("new identifier: " + base + tiebreaker);
				return (base + tiebreaker);
			}
		} else {
			// back for more, which means that we had a 
            // namespace conflict.  Hack the ident down
            // to size if it's too big, and then generate 
            // a hash tiebreaker using the ident and the 
            // current value of physicalName
			if (logger.isDebugEnabled()) logger.debug("physical idenfier is not unique, regenerating: " + physicalName);
			String base = ident;
			if (ident.length() > 27) {
				base = ident.substring(0, 27);
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
	 * Returns null because Oracle doesn't have catalogs.
	 */
	public String getCatalogTerm() {
		return null;
	}

	/**
	 * Returns the string "Schema".
	 */
	public String getSchemaTerm() {
		return "Schema";
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
}
