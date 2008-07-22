/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.ddl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLRelationship.Deferrability;
import ca.sqlpower.architect.SQLRelationship.UpdateDeleteRule;

public class MySqlDDLGenerator extends GenericDDLGenerator {

    public MySqlDDLGenerator() throws SQLException {
        super();
    }

    public static final String GENERATOR_VERSION = "$Revision$";

    /**
     * Systemwide setting (controlled by a system property) that tells whether
     * or not this DDL generator will convert identifiers to lower case.
     */
    private static final boolean DOWNCASE_IDENTIFIERS = Boolean.parseBoolean(System.getProperty(
            "ca.sqlpower.architect.ddl.MySqlDDLGenerator.DOWNCASE_IDENTIFIERS", "true"));

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

    public String getName() {
        return "MySQL";
    }

    @Override
    protected void createTypeMap() throws SQLException {
        typeMap = new HashMap();

        typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null,
                DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("TINYINT", Types.BIT, 1, null, null,
                DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("LONGBLOB", Types.BLOB, 4000000000L, null,
                null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 2000, "'", "'",
                DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("LONGTEXT", Types.CLOB, 4000000000L, null,
                null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("BOOLEAN", Types.BOOLEAN, 1, null, null,
                DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'",
                DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 38, null, null,
                DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE PRECISION", Types.DOUBLE, 38,
                null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("DOUBLE PRECISION", Types.FLOAT, 38, null,
                null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INT", Types.INTEGER, 38, null, null,
                DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 38, null, null,
                DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("DOUBLE PRECISION", Types.REAL, 38, null,
                null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 38, null,
                null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'",
                DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 0, "'",
                "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 38, null, null,
                DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 65535, null, null,
                DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("VARBINARY", Types.LONGVARBINARY,
                65535, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 65535,
                null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("TEXT", Types.LONGVARCHAR, 65535,
                "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 65535, "'",
                "'", DatabaseMetaData.columnNullable, true, false));
    }

    @Override
    public String toIdentifier(String name) {
        if (name != null && DOWNCASE_IDENTIFIERS) {
            name = name.toLowerCase();
        }
        return super.toIdentifier(name);
    }

    /**
     * Subroutine for toIdentifier(). Probably a generally useful feature that
     * we should pull up to the GenericDDLGenerator.
     */
    public boolean isReservedWord(String word) {
        return reservedWords.contains(word.toUpperCase());
    }

    @Override
    public String getCatalogTerm() {
        return "Database";
    }

    @Override
    public String getSchemaTerm() {
        return null;
    }

    @Override
    protected void addPrimaryKeysToCreateTable(SQLTable t) throws ArchitectException {
        logger.debug("Adding Primary keys");

        Iterator it = t.getColumns().iterator();
        boolean firstCol = true;
        while (it.hasNext()) {
            SQLColumn col = (SQLColumn) it.next();
            if (col.getPrimaryKeySeq() == null)
                break;
            if (firstCol) {
                print(",\n                PRIMARY KEY (");
                firstCol = false;
            } else {
                print(", ");
            }
            print(col.getPhysicalName());
        }
        if (!firstCol) {
            print(")");
        }
    }

    public void dropRelationship(SQLRelationship r) {

        print("\n ALTER TABLE ");

        print(toQualifiedName(r.getFkTable()));
        print(" DROP FOREIGN KEY ");
        print(r.getName());
        endStatement(DDLStatement.StatementType.DROP, r);
    }

    /*
     * The primary key name in MySQL is completely ignored. Every primary key is
     * named PRIMARY so we don't have to do any checking of name conflicts for
     * the primary key. We don't even have to specify a name for the primary
     * key.
     * 
     * @see ca.sqlpower.architect.ddl.GenericDDLGenerator#writePrimaryKey(ca.sqlpower.architect.SQLTable)
     */
    @Override
    protected void writePrimaryKey(SQLTable t) throws ArchitectException {
        logger.debug("Writing the primary key of " + t.getName());
        boolean firstCol = true;
        Iterator it = t.getColumns().iterator();
        while (it.hasNext()) {
            SQLColumn col = (SQLColumn) it.next();
            if (col.getPrimaryKeySeq() == null)
                break;
            if (firstCol) {
                // generate a unique primary key name
                println("");
                print("ALTER TABLE ");
                print(toQualifiedName(t));
                print(" ADD CONSTRAINT ");
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

    /**
     * Adds support for the MySQL auto_increment feature.
     */
    @Override
    public String columnType(SQLColumn c) {
        String type = super.columnType(c);
        if (c.isAutoIncrement()) {
            type += " AUTO_INCREMENT";
        }
        return type;
    }

    @Override
    public void addIndex(SQLIndex index) throws ArchitectException {

        createPhysicalName(topLevelNames, index);

        println("");
        print("CREATE ");
        if (index.isUnique()) {
            print("UNIQUE ");
        }
        print("INDEX ");
        print(toIdentifier(index.getName()));
        if(index.getType() != null) {            
            print(" USING " + index.getType());
        }
        print("\n ON ");
        print(index.getParentTable().getName());
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : (List<SQLIndex.Column>) index.getChildren()) {
            if (!first)
                print(", ");
            print(c.getName());
            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
            first = false;
        }
        print(" )");
        endStatement(DDLStatement.StatementType.CREATE, index);
    }

    @Override
    public String getDeferrabilityClause(SQLRelationship r) {
        if (supportsDeferrabilityPolicy(r)) {
            return "";
        } else {
            throw new UnsupportedOperationException(getName() + " does not support " + r.getName() +
                    "'s deferrability policy (" + r.getDeferrability() + ").");
        }
    }

    @Override
    public boolean supportsDeferrabilityPolicy(SQLRelationship r) {
        if (!Arrays.asList(Deferrability.values()).contains(r.getDeferrability())) {
            throw new IllegalArgumentException("Unknown deferrability policy: " + r.getDeferrability());
        } else {
            return r.getDeferrability() == Deferrability.NOT_DEFERRABLE;
        }
    }
    
    @Override
    public boolean supportsDeleteAction(SQLRelationship r) {
        return r.getDeleteRule() != UpdateDeleteRule.SET_DEFAULT;
    }

    @Override
    public String getDeleteActionClause(SQLRelationship r) {
        if (r.getDeleteRule() == UpdateDeleteRule.SET_DEFAULT) {
            throw new IllegalArgumentException("Unsupported delete action: " + r.getDeleteRule());
        } else {
            return super.getDeleteActionClause(r);
        }
    }
    
    @Override
    public boolean supportsUpdateAction(SQLRelationship r) {
        return r.getUpdateRule() != UpdateDeleteRule.SET_DEFAULT;
    }

    @Override
    public String getUpdateActionClause(SQLRelationship r) {
        if (r.getUpdateRule() == UpdateDeleteRule.SET_DEFAULT) {
            throw new IllegalArgumentException("Unsupported update action: " + r.getUpdateRule());
        } else {
            return super.getUpdateActionClause(r);
        }
    }

    @Override
    public void modifyColumn(SQLColumn c) {
        Map colNameMap = new HashMap();
        SQLTable t = c.getParentTable();
        print("\n ALTER TABLE ");
        print(toQualifiedName(t));
        print(" MODIFY COLUMN ");
        print(columnDefinition(c, colNameMap));
        endStatement(DDLStatement.StatementType.MODIFY, c);
    }
    
    @Override
    public boolean supportsRollback() {
        return false;
    }
}
