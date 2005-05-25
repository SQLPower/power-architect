package ca.sqlpower.architect.ddl;

import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;
import java.io.*;


public class SQLServerDDLGenerator extends GenericDDLGenerator {
	public static final String GENERATOR_VERSION = "$Revision$";
	private static final Logger logger = Logger.getLogger(SQLServerDDLGenerator.class);

	private static ArrayList reservedWords;
	
	static {
		reservedWords = new ArrayList();
		BufferedReader br = null;
		try {
			br = new BufferedReader (new FileReader("sqlserver_reserved_words.txt"));		
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
		println("-- Created by SQLPower SQLServer 2000 DDL Generator "+GENERATOR_VERSION+" --");
	}
	
	public void writeDDLTransactionBegin() {
        // nothing needs to be done for beginning a transaction
	}

	/**
	 * Prints "GO" on its own line.
	 */
	public void writeDDLTransactionEnd() {
		println("GO");
	}

	/**
	 * Prints nothing because SS2k doesn't need DDL statement
	 * terminators.
	 */
	public void writeStatementTerminator() {
        // override to suppress
	}

	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();
		
		typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 2000, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("IMAGE", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("TEXT", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATETIME", Types.DATE, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("REAL", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("INT", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("IMAGE", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("TEXT", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("DATETIME", Types.TIME, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("DATETIME", Types.TIMESTAMP, 23, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 3, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 8000, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 8000, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

	/**
	 * Returns the string "Database".
	 */
	public String getCatalogTerm() {
		return "Database";
	}

	/**
	 * Returns the string "Owner".
	 */
	public String getSchemaTerm() {
		return "Owner";
	}

	/**
	 * Turns a logical identifier into a legal identifier (physical name) for SQL Server.
     * 
     * <p>Uses a deterministic method to generate tie-breaking numbers when there is a namespace 
     * conflict.  If you pass null as the physical name, it will use just the logical name when 
     * trying to come up with tie-breaking hashes for identifier names.  If the first attempt
     * at generating a unique name fails, subsequent calls should pass each new illegal     
     * identifier which will be used with the logical name to generate a another hash.
     * 
     * <p>SQL Server 7.0 Rules:
     * <ul>
     *  <li> no spaces
     *  <li> 128 character limit
     *  <li> can only be comprised of letters, numbers, underscores
     *  <li> can't be an sql server reserved word
     *  <li> can also use "@$#_" 
     * </ul>
     * 
     * <p>XXX: the illegal character replacement routine does not play well with regex chars like ^ and |
	 */
	public String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_');
		logger.debug("after replace of spaces: " + ident);
		// see if it's a reserved word, and add something alpha to front if it is...
		if (isReservedWord(ident)) {
			ident = "X" + ident;
			logger.debug("identifier was reserved word, appending X: " + ident);
		}

		// replace anything that is not a letter, character, or underscore with an underscore...
		ident = ident.replaceAll("[^a-zA-Z0-9_@$#]", "_");

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


}
