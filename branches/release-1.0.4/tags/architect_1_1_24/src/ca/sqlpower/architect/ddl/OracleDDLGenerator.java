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

public class OracleDDLGenerator extends GenericDDLGenerator {
    
	public OracleDDLGenerator() throws SQLException {
		super();
	}

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

    @Override
	public void writeHeader() {
		println("-- Created by SQLPower Oracle 8i/9i DDL Generator "+GENERATOR_VERSION+" --");
	}

    @Override
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
		typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("NUMBER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("LONG RAW", Types.LONGVARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("VARCHAR2", Types.LONGVARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("NUMBER", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("NUMBER", Types.SMALLINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("DATE", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("DATE", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("NUMBER", Types.TINYINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("LONG", Types.LONGVARCHAR, 2000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("LONG RAW", Types.VARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR2", Types.VARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

    @Override
    protected void createProfileFunctionMap() {
        profileFunctionMap = new HashMap();
        profileFunctionMap.put("BIT", new ProfileFunctionDescriptor("BIT", Types.BIT,                   true,true,true,false,true,true,true,true));
        profileFunctionMap.put("CHAR", new ProfileFunctionDescriptor("CHAR", Types.CHAR,                true,true,true,false,true,true,true,true));
        profileFunctionMap.put("BINARY", new ProfileFunctionDescriptor("BINARY", Types.BINARY,          true,true,true,false,true,true,true,true));
        profileFunctionMap.put("VARBINARY", new ProfileFunctionDescriptor("VARBINARY", Types.VARBINARY, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("RAW",       new ProfileFunctionDescriptor("VARBINARY", Types.VARBINARY, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("VARCHAR", new ProfileFunctionDescriptor("VARCHAR", Types.VARCHAR,       true,true,true,false,true,true,true,true));
        profileFunctionMap.put("VARCHAR2",new ProfileFunctionDescriptor("VARCHAR", Types.VARCHAR,       true,true,true,false,true,true,true,true));
        profileFunctionMap.put("NVARCHAR2",new ProfileFunctionDescriptor("NVARCHAR", Types.VARCHAR,     true,true,true,false,true,true,true,true));
        
        profileFunctionMap.put("BLOB", new ProfileFunctionDescriptor("BLOB", Types.BLOB, false,false,false,false,false,false,false,false));
        profileFunctionMap.put("CLOB", new ProfileFunctionDescriptor("CLOB", Types.CLOB, false,false,false,false,false,false,false,false));
        
        profileFunctionMap.put("BIGINT", new ProfileFunctionDescriptor("BIGINT", Types.BIGINT,       true,true,true,true,true,true,true,true));
        profileFunctionMap.put("DECIMAL", new ProfileFunctionDescriptor("DECIMAL", Types.DECIMAL,    true,true,true,true,true,true,true,true));
        profileFunctionMap.put("DOUBLE", new ProfileFunctionDescriptor("DOUBLE", Types.DOUBLE,       true,true,true,true,true,true,true,true));
        profileFunctionMap.put("FLOAT", new ProfileFunctionDescriptor("FLOAT", Types.FLOAT,          true,true,true,true,true,true,true,true));
        profileFunctionMap.put("INTEGER", new ProfileFunctionDescriptor("INTEGER", Types.INTEGER,    true,true,true,true,true,true,true,true));
        profileFunctionMap.put("NUMERIC", new ProfileFunctionDescriptor("NUMERIC", Types.NUMERIC,    true,true,true,true,true,true,true,true));
        profileFunctionMap.put("NUMBER",  new ProfileFunctionDescriptor("NUMERIC", Types.NUMERIC,    true,true,true,true,true,true,true,true));
        profileFunctionMap.put("REAL", new ProfileFunctionDescriptor("REAL", Types.REAL,             true,true,true,true,true,true,true,true));
        profileFunctionMap.put("SMALLINT", new ProfileFunctionDescriptor("SMALLINT", Types.SMALLINT, true,true,true,true,true,true,true,true));
        profileFunctionMap.put("TINYINT", new ProfileFunctionDescriptor("TINYINT", Types.TINYINT,    true,true,true,true,true,true,true,true));
        profileFunctionMap.put("INTERVALDS", new ProfileFunctionDescriptor("TINYINT", Types.TINYINT,    true,true,true,true,true,true,true,true));
        profileFunctionMap.put("INTERVALYM", new ProfileFunctionDescriptor("TINYINT", Types.TINYINT,    true,true,true,true,true,true,true,true));
        
        profileFunctionMap.put("TIME",      new ProfileFunctionDescriptor("TIME", Types.TIME,           true,true,true,false,true,true,true,true));
        profileFunctionMap.put("TIMESTAMP", new ProfileFunctionDescriptor("TIMESTAMP", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("TIMESTAMP WITH LOCAL TIME ZONE", new ProfileFunctionDescriptor("TIMESTAMP", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("TIMESTAMP WITH TIME ZONE", new ProfileFunctionDescriptor("TIMESTAMP", Types.TIMESTAMP, true,true,true,false,true,true,true,true));
        profileFunctionMap.put("DATE",      new ProfileFunctionDescriptor("DATE", Types.DATE,           true,true,true,false,true,true,true,true));
        
        profileFunctionMap.put("LONG",          new ProfileFunctionDescriptor("LONGVARCHAR", Types.LONGVARCHAR,     false,false,false,false,false,false,false,false));
        profileFunctionMap.put("LONG RAW",      new ProfileFunctionDescriptor("LONGVARBINARY", Types.LONGVARBINARY, false,false,false,false,false,false,false,false));
        profileFunctionMap.put("STRUCT",        new ProfileFunctionDescriptor("LONGVARBINARY", Types.LONGVARBINARY, false,false,false,false,false,false,false,false));
        profileFunctionMap.put("ARRAY",         new ProfileFunctionDescriptor("LONGVARBINARY", Types.LONGVARBINARY, false,false,false,false,false,false,false,false));
        profileFunctionMap.put("REF",           new ProfileFunctionDescriptor("LONGVARBINARY", Types.LONGVARBINARY, false,false,false,false,false,false,false,false));
        
        
        

    }

    
	/**
	 * Turns a logical identifier into a legal identifier (physical name) for Oracle 8i/9i.  
     * Also, upcases the identifier for consistency.  
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
	private String toIdentifier(String logicalName, String physicalName) {
		// replace spaces with underscores
		if (logicalName == null) return null;
		if (logger.isDebugEnabled()) logger.debug("getting physical name for: " + logicalName);
		String ident = logicalName.replace(' ','_').toUpperCase();
		if (logger.isDebugEnabled()) logger.debug("after replace of spaces: " + ident);
				
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
    
    /**
     * Subroutine for toIdentifier().  Probably a generally useful feature that we
     * should pull up to the GenericDDLGenerator.
     */
    public boolean isReservedWord(String word) {
        return reservedWords.contains(word.toUpperCase());
    }

    @Override
	public String toIdentifier(String name) {
		return toIdentifier(name,null);
	}

	/**
	 * Returns null because Oracle doesn't have catalogs.
	 */
    @Override
	public String getCatalogTerm() {
		return null;
	}

	/**
	 * Returns the string "Schema".
	 */
    @Override
	public String getSchemaTerm() {
		return "Schema";
	}

    /**
     * Generates a command for dropping a foreign key on oracle.
     * The statement looks like <code>ALTER TABLE $fktable DROP CONSTRAINT $fkname</code>.
     */
    @Override
    public String makeDropForeignKeySQL(String fkTable, String fkName) {
        return "ALTER TABLE "
            + toQualifiedName(fkTable)
            + " DROP CONSTRAINT "
            + fkName;
    }
    
    @Override
    public void modifyColumn(SQLColumn c) {
		Map colNameMap = new HashMap(); 
		SQLTable t = c.getParentTable();
		print("\n ALTER TABLE ");
		print(toQualifiedName(t.getPhysicalName()));
		print(" MODIFY ");
		print(columnDefinition(c,colNameMap));
		endStatement(DDLStatement.StatementType.MODIFY, c);
	}
    
    @Override
    public void addColumn(SQLColumn c) {
        Map colNameMap = new HashMap();  
        print("\n ALTER TABLE ");
        print(toQualifiedName(c.getParentTable()));
        print(" ADD ");
        print(columnDefinition(c,colNameMap));
        endStatement(DDLStatement.StatementType.CREATE, c);
    }
    
    @Override
    public String caseWhenNull(String expression, String then) {
        StringBuffer sql = new StringBuffer();
        sql.append("DECODE(");
        sql.append(expression);
        sql.append(",NULL,");
        sql.append(then);
        sql.append(")");
        return sql.toString();
    }
}
