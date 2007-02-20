package ca.sqlpower.architect.ddl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLIndex.IndexType;

public class MySqlDDLGenerator extends GenericDDLGenerator {

    public MySqlDDLGenerator() throws SQLException {
        super();
    }
    
    public static final String GENERATOR_VERSION = "$Revision$";

    private static final Logger logger = Logger.getLogger(MySqlDDLGenerator.class);

    private static HashSet reservedWords;
    
    static {
        reservedWords = new HashSet();
        reservedWords.add("ADD");
        reservedWords.add("ALL");
        reservedWords.add("ALTER");
        reservedWords.add("ANALYZE");
        reservedWords.add("AND");
        reservedWords.add("AS");
        reservedWords.add("ASC");
        reservedWords.add("ASENSITIVE");
        reservedWords.add("BEFORE");
        reservedWords.add("BETWEEN");
        reservedWords.add("BIGINT");
        reservedWords.add("BINARY");
        reservedWords.add("BLOB");
        reservedWords.add("BOTH");
        reservedWords.add("BY");
        reservedWords.add("CALL");
        reservedWords.add("CASCADE");
        reservedWords.add("CASE");
        reservedWords.add("CHANGE");
        reservedWords.add("CHAR");
        reservedWords.add("CHARACTER");
        reservedWords.add("CHECK");
        reservedWords.add("COLLATE");
        reservedWords.add("COLUMN");
        reservedWords.add("CONDITION");
        reservedWords.add("CONNECTION");
        reservedWords.add("CONSTRAINT");
        reservedWords.add("CONTINUE");
        reservedWords.add("CONVERT");
        reservedWords.add("CREATE");
        reservedWords.add("CROSS");
        reservedWords.add("CURRENT_DATE");
        reservedWords.add("CURRENT_TIME");
        reservedWords.add("CURRENT_TIMESTAMP");
        reservedWords.add("CURRENT_USER");
        reservedWords.add("CURSOR");
        reservedWords.add("DATABASE");
        reservedWords.add("DATABASES");
        reservedWords.add("DAY_HOUR");
        reservedWords.add("DAY_MICROSECOND");
        reservedWords.add("DAY_MINUTE");
        reservedWords.add("DAY_SECOND");
        reservedWords.add("DEC");
        reservedWords.add("DECIMAL");
        reservedWords.add("DECLARE");
        reservedWords.add("DEFAULT");
        reservedWords.add("DELAYED");
        reservedWords.add("DELETE");
        reservedWords.add("DESC");
        reservedWords.add("DESCRIBE");
        reservedWords.add("DETERMINISTIC");
        reservedWords.add("DISTINCT");
        reservedWords.add("DISTINCTROW");
        reservedWords.add("DIV");
        reservedWords.add("DOUBLE");
        reservedWords.add("DROP");
        reservedWords.add("DUAL");
        reservedWords.add("EACH");
        reservedWords.add("ELSE");
        reservedWords.add("ELSEIF");
        reservedWords.add("ENCLOSED");
        reservedWords.add("ESCAPED");
        reservedWords.add("EXISTS");
        reservedWords.add("EXIT");
        reservedWords.add("EXPLAIN");
        reservedWords.add("FETCH");
        reservedWords.add("FLOAT");
        reservedWords.add("FLOAT4");
        reservedWords.add("FLOAT8");
        reservedWords.add("FOR");
        reservedWords.add("FORCE");
        reservedWords.add("FOREIGN");
        reservedWords.add("FROM");
        reservedWords.add("FULLTEXT");
        reservedWords.add("GRANT");
        reservedWords.add("GROUP");
        reservedWords.add("HAVING");
        reservedWords.add("HIGH_PRIORITY");
        reservedWords.add("HOUR_MICROSECOND");
        reservedWords.add("HOUR_MINUTE");
        reservedWords.add("HOUR_SECOND");
        reservedWords.add("IF");
        reservedWords.add("IGNORE");
        reservedWords.add("IN");
        reservedWords.add("INDEX");
        reservedWords.add("INFILE");
        reservedWords.add("INNER");
        reservedWords.add("INOUT");
        reservedWords.add("INSENSITIVE");
        reservedWords.add("INSERT");
        reservedWords.add("INT");
        reservedWords.add("INT1");
        reservedWords.add("INT2");
        reservedWords.add("INT3");
        reservedWords.add("INT4");
        reservedWords.add("INT8");
        reservedWords.add("INTEGER");
        reservedWords.add("INTERVAL");
        reservedWords.add("INTO");
        reservedWords.add("IS");
        reservedWords.add("ITERATE");
        reservedWords.add("JOIN");
        reservedWords.add("KEY");
        reservedWords.add("KEYS");
        reservedWords.add("KILL");
        reservedWords.add("LEADING");
        reservedWords.add("LEAVE");
        reservedWords.add("LEFT");
        reservedWords.add("LIKE");
        reservedWords.add("LIMIT");
        reservedWords.add("LINES");
        reservedWords.add("LOAD");
        reservedWords.add("LOCALTIME");
        reservedWords.add("LOCALTIMESTAMP");
        reservedWords.add("LOCK");
        reservedWords.add("LONG");
        reservedWords.add("LONGBLOB");
        reservedWords.add("LONGTEXT");
        reservedWords.add("LOOP");
        reservedWords.add("LOW_PRIORITY");
        reservedWords.add("MATCH");
        reservedWords.add("MEDIUMBLOB");
        reservedWords.add("MEDIUMINT");
        reservedWords.add("MEDIUMTEXT");
        reservedWords.add("MIDDLEINT");
        reservedWords.add("MINUTE_MICROSECOND");
        reservedWords.add("MINUTE_SECOND");
        reservedWords.add("MOD");
        reservedWords.add("MODIFIES");
        reservedWords.add("NATURAL");
        reservedWords.add("NO_WRITE_TO_BINLOG");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("NUMERIC");
        reservedWords.add("ON");
        reservedWords.add("OPTIMIZE");
        reservedWords.add("OPTION");
        reservedWords.add("OPTIONALLY");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("OUT");
        reservedWords.add("OUTER");
        reservedWords.add("OUTFILE");
        reservedWords.add("PRECISION");
        reservedWords.add("PRIMARY");
        reservedWords.add("PROCEDURE");
        reservedWords.add("PURGE");
        reservedWords.add("RAID0");
        reservedWords.add("READ");
        reservedWords.add("READS");
        reservedWords.add("REAL");
        reservedWords.add("REFERENCES");
        reservedWords.add("REGEXP");
        reservedWords.add("RELEASE");
        reservedWords.add("RENAME");
        reservedWords.add("REPEAT");
        reservedWords.add("REPLACE");
        reservedWords.add("REQUIRE");
        reservedWords.add("RESTRICT");
        reservedWords.add("RETURN");
        reservedWords.add("REVOKE");
        reservedWords.add("RIGHT");
        reservedWords.add("RLIKE");
        reservedWords.add("SCHEMA");
        reservedWords.add("SCHEMAS");
        reservedWords.add("SECOND_MICROSECOND");
        reservedWords.add("SELECT");
        reservedWords.add("SENSITIVE");
        reservedWords.add("SEPARATOR");
        reservedWords.add("SET");
        reservedWords.add("SHOW");
        reservedWords.add("SMALLINT");
        reservedWords.add("SONAME");
        reservedWords.add("SPATIAL");
        reservedWords.add("SPECIFIC");
        reservedWords.add("SQL");
        reservedWords.add("SQL_BIG_RESULT");
        reservedWords.add("SQL_CALC_FOUND_ROWS");
        reservedWords.add("SQL_SMALL_RESULT");
        reservedWords.add("SQLEXCEPTION");
        reservedWords.add("SQLSTATE");
        reservedWords.add("SQLWARNING");
        reservedWords.add("SSL");
        reservedWords.add("STARTING");
        reservedWords.add("STRAIGHT_JOIN");
        reservedWords.add("TABLE");
        reservedWords.add("TERMINATED");
        reservedWords.add("THEN");
        reservedWords.add("TINYBLOB");
        reservedWords.add("TINYINT");
        reservedWords.add("TINYTEXT");
        reservedWords.add("TO");
        reservedWords.add("TRAILING");
        reservedWords.add("TRIGGER");
        reservedWords.add("UNDO");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("UNLOCK");
        reservedWords.add("UNSIGNED");
        reservedWords.add("UPDATE");
        reservedWords.add("USAGE");
        reservedWords.add("USE");
        reservedWords.add("USING");
        reservedWords.add("UTC_DATE");
        reservedWords.add("UTC_TIME");
        reservedWords.add("UTC_TIMESTAMP");
        reservedWords.add("VALUES");
        reservedWords.add("VARBINARY");
        reservedWords.add("VARCHAR");
        reservedWords.add("VARCHARACTER");
        reservedWords.add("VARYING");
        reservedWords.add("WHEN");
        reservedWords.add("WHERE");
        reservedWords.add("WHILE");
        reservedWords.add("WITH");
        reservedWords.add("WRITE");
        reservedWords.add("X509");
        reservedWords.add("XOR");
        reservedWords.add("YEAR_MONTH");
        reservedWords.add("ZEROFILL");

    }
  

    @Override
    protected void createTypeMap() throws SQLException {
        typeMap = new HashMap();
        
        typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("TINYINT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("LONGBLOB", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 2000, "'", "'", DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("LONGTEXT", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE PRECISION", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("DOUBLE PRECISION", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("INT", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("DOUBLE PRECISION", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("DATETIME", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 65535, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("VARBINARY", Types.LONGVARBINARY, 65535, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 65535, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("VARCHAR", Types.LONGVARCHAR, 65535, "'", "'", DatabaseMetaData.columnNullable, true, false));
        typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 65535, "'", "'", DatabaseMetaData.columnNullable, true, false));
    }
    
    /**
     * Subroutine for toIdentifier().  Probably a generally useful feature that we
     * should pull up to the GenericDDLGenerator.
     */
    public boolean isReservedWord(String word) {
        return reservedWords.contains(word.toUpperCase());
    }
    
    /**
     * create index ddl in mySql syntax
     */
    @Override
    public void addIndex(SQLIndex index) throws ArchitectException {
        if (index.getType() == IndexType.STATISTIC )
            return;
        
        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
        print("INDEX ");
        print(index.getName());
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
