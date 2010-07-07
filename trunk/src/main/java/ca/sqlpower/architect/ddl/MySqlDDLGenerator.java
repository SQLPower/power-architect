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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLEnumeration;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTypePhysicalProperties.SQLTypeConstraint;

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

    /**
     * These words are the words reserved by MySQL and cannot be used as a name
     * for tables, columns, relationships, indices, etc.
     */
    public static final HashSet<String> RESERVED_WORDS;

    static {
        RESERVED_WORDS = new HashSet<String>();
        RESERVED_WORDS.add("ADD");
        RESERVED_WORDS.add("ALL");
        RESERVED_WORDS.add("ALTER");
        RESERVED_WORDS.add("ANALYZE");
        RESERVED_WORDS.add("AND");
        RESERVED_WORDS.add("AS");
        RESERVED_WORDS.add("ASC");
        RESERVED_WORDS.add("ASENSITIVE");
        RESERVED_WORDS.add("BEFORE");
        RESERVED_WORDS.add("BETWEEN");
        RESERVED_WORDS.add("BIGINT");
        RESERVED_WORDS.add("BINARY");
        RESERVED_WORDS.add("BLOB");
        RESERVED_WORDS.add("BOTH");
        RESERVED_WORDS.add("BY");
        RESERVED_WORDS.add("CALL");
        RESERVED_WORDS.add("CASCADE");
        RESERVED_WORDS.add("CASE");
        RESERVED_WORDS.add("CHANGE");
        RESERVED_WORDS.add("CHAR");
        RESERVED_WORDS.add("CHARACTER");
        RESERVED_WORDS.add("CHECK");
        RESERVED_WORDS.add("COLLATE");
        RESERVED_WORDS.add("COLUMN");
        RESERVED_WORDS.add("CONDITION");
        RESERVED_WORDS.add("CONNECTION");
        RESERVED_WORDS.add("CONSTRAINT");
        RESERVED_WORDS.add("CONTINUE");
        RESERVED_WORDS.add("CONVERT");
        RESERVED_WORDS.add("CREATE");
        RESERVED_WORDS.add("CROSS");
        RESERVED_WORDS.add("CURRENT_DATE");
        RESERVED_WORDS.add("CURRENT_TIME");
        RESERVED_WORDS.add("CURRENT_TIMESTAMP");
        RESERVED_WORDS.add("CURRENT_USER");
        RESERVED_WORDS.add("CURSOR");
        RESERVED_WORDS.add("DATABASE");
        RESERVED_WORDS.add("DATABASES");
        RESERVED_WORDS.add("DAY_HOUR");
        RESERVED_WORDS.add("DAY_MICROSECOND");
        RESERVED_WORDS.add("DAY_MINUTE");
        RESERVED_WORDS.add("DAY_SECOND");
        RESERVED_WORDS.add("DEC");
        RESERVED_WORDS.add("DECIMAL");
        RESERVED_WORDS.add("DECLARE");
        RESERVED_WORDS.add("DEFAULT");
        RESERVED_WORDS.add("DELAYED");
        RESERVED_WORDS.add("DELETE");
        RESERVED_WORDS.add("DESC");
        RESERVED_WORDS.add("DESCRIBE");
        RESERVED_WORDS.add("DETERMINISTIC");
        RESERVED_WORDS.add("DISTINCT");
        RESERVED_WORDS.add("DISTINCTROW");
        RESERVED_WORDS.add("DIV");
        RESERVED_WORDS.add("DOUBLE");
        RESERVED_WORDS.add("DROP");
        RESERVED_WORDS.add("DUAL");
        RESERVED_WORDS.add("EACH");
        RESERVED_WORDS.add("ELSE");
        RESERVED_WORDS.add("ELSEIF");
        RESERVED_WORDS.add("ENCLOSED");
        RESERVED_WORDS.add("ESCAPED");
        RESERVED_WORDS.add("EXISTS");
        RESERVED_WORDS.add("EXIT");
        RESERVED_WORDS.add("EXPLAIN");
        RESERVED_WORDS.add("FETCH");
        RESERVED_WORDS.add("FLOAT");
        RESERVED_WORDS.add("FLOAT4");
        RESERVED_WORDS.add("FLOAT8");
        RESERVED_WORDS.add("FOR");
        RESERVED_WORDS.add("FORCE");
        RESERVED_WORDS.add("FOREIGN");
        RESERVED_WORDS.add("FROM");
        RESERVED_WORDS.add("FULLTEXT");
        RESERVED_WORDS.add("GRANT");
        RESERVED_WORDS.add("GROUP");
        RESERVED_WORDS.add("HAVING");
        RESERVED_WORDS.add("HIGH_PRIORITY");
        RESERVED_WORDS.add("HOUR_MICROSECOND");
        RESERVED_WORDS.add("HOUR_MINUTE");
        RESERVED_WORDS.add("HOUR_SECOND");
        RESERVED_WORDS.add("IF");
        RESERVED_WORDS.add("IGNORE");
        RESERVED_WORDS.add("IN");
        RESERVED_WORDS.add("INDEX");
        RESERVED_WORDS.add("INFILE");
        RESERVED_WORDS.add("INNER");
        RESERVED_WORDS.add("INOUT");
        RESERVED_WORDS.add("INSENSITIVE");
        RESERVED_WORDS.add("INSERT");
        RESERVED_WORDS.add("INT");
        RESERVED_WORDS.add("INT1");
        RESERVED_WORDS.add("INT2");
        RESERVED_WORDS.add("INT3");
        RESERVED_WORDS.add("INT4");
        RESERVED_WORDS.add("INT8");
        RESERVED_WORDS.add("INTEGER");
        RESERVED_WORDS.add("INTERVAL");
        RESERVED_WORDS.add("INTO");
        RESERVED_WORDS.add("IS");
        RESERVED_WORDS.add("ITERATE");
        RESERVED_WORDS.add("JOIN");
        RESERVED_WORDS.add("KEY");
        RESERVED_WORDS.add("KEYS");
        RESERVED_WORDS.add("KILL");
        RESERVED_WORDS.add("LEADING");
        RESERVED_WORDS.add("LEAVE");
        RESERVED_WORDS.add("LEFT");
        RESERVED_WORDS.add("LIKE");
        RESERVED_WORDS.add("LIMIT");
        RESERVED_WORDS.add("LINES");
        RESERVED_WORDS.add("LOAD");
        RESERVED_WORDS.add("LOCALTIME");
        RESERVED_WORDS.add("LOCALTIMESTAMP");
        RESERVED_WORDS.add("LOCK");
        RESERVED_WORDS.add("LONG");
        RESERVED_WORDS.add("LONGBLOB");
        RESERVED_WORDS.add("LONGTEXT");
        RESERVED_WORDS.add("LOOP");
        RESERVED_WORDS.add("LOW_PRIORITY");
        RESERVED_WORDS.add("MATCH");
        RESERVED_WORDS.add("MEDIUMBLOB");
        RESERVED_WORDS.add("MEDIUMINT");
        RESERVED_WORDS.add("MEDIUMTEXT");
        RESERVED_WORDS.add("MIDDLEINT");
        RESERVED_WORDS.add("MINUTE_MICROSECOND");
        RESERVED_WORDS.add("MINUTE_SECOND");
        RESERVED_WORDS.add("MOD");
        RESERVED_WORDS.add("MODIFIES");
        RESERVED_WORDS.add("NATURAL");
        RESERVED_WORDS.add("NO_WRITE_TO_BINLOG");
        RESERVED_WORDS.add("NOT");
        RESERVED_WORDS.add("NULL");
        RESERVED_WORDS.add("NUMERIC");
        RESERVED_WORDS.add("ON");
        RESERVED_WORDS.add("OPTIMIZE");
        RESERVED_WORDS.add("OPTION");
        RESERVED_WORDS.add("OPTIONALLY");
        RESERVED_WORDS.add("OR");
        RESERVED_WORDS.add("ORDER");
        RESERVED_WORDS.add("OUT");
        RESERVED_WORDS.add("OUTER");
        RESERVED_WORDS.add("OUTFILE");
        RESERVED_WORDS.add("PRECISION");
        RESERVED_WORDS.add("PRIMARY");
        RESERVED_WORDS.add("PROCEDURE");
        RESERVED_WORDS.add("PURGE");
        RESERVED_WORDS.add("RAID0");
        RESERVED_WORDS.add("READ");
        RESERVED_WORDS.add("READS");
        RESERVED_WORDS.add("REAL");
        RESERVED_WORDS.add("REFERENCES");
        RESERVED_WORDS.add("REGEXP");
        RESERVED_WORDS.add("RELEASE");
        RESERVED_WORDS.add("RENAME");
        RESERVED_WORDS.add("REPEAT");
        RESERVED_WORDS.add("REPLACE");
        RESERVED_WORDS.add("REQUIRE");
        RESERVED_WORDS.add("RESTRICT");
        RESERVED_WORDS.add("RETURN");
        RESERVED_WORDS.add("REVOKE");
        RESERVED_WORDS.add("RIGHT");
        RESERVED_WORDS.add("RLIKE");
        RESERVED_WORDS.add("SCHEMA");
        RESERVED_WORDS.add("SCHEMAS");
        RESERVED_WORDS.add("SECOND_MICROSECOND");
        RESERVED_WORDS.add("SELECT");
        RESERVED_WORDS.add("SENSITIVE");
        RESERVED_WORDS.add("SEPARATOR");
        RESERVED_WORDS.add("SET");
        RESERVED_WORDS.add("SHOW");
        RESERVED_WORDS.add("SMALLINT");
        RESERVED_WORDS.add("SONAME");
        RESERVED_WORDS.add("SPATIAL");
        RESERVED_WORDS.add("SPECIFIC");
        RESERVED_WORDS.add("SQL");
        RESERVED_WORDS.add("SQL_BIG_RESULT");
        RESERVED_WORDS.add("SQL_CALC_FOUND_ROWS");
        RESERVED_WORDS.add("SQL_SMALL_RESULT");
        RESERVED_WORDS.add("SQLEXCEPTION");
        RESERVED_WORDS.add("SQLSTATE");
        RESERVED_WORDS.add("SQLWARNING");
        RESERVED_WORDS.add("SSL");
        RESERVED_WORDS.add("STARTING");
        RESERVED_WORDS.add("STRAIGHT_JOIN");
        RESERVED_WORDS.add("TABLE");
        RESERVED_WORDS.add("TERMINATED");
        RESERVED_WORDS.add("THEN");
        RESERVED_WORDS.add("TINYBLOB");
        RESERVED_WORDS.add("TINYINT");
        RESERVED_WORDS.add("TINYTEXT");
        RESERVED_WORDS.add("TO");
        RESERVED_WORDS.add("TRAILING");
        RESERVED_WORDS.add("TRIGGER");
        RESERVED_WORDS.add("UNDO");
        RESERVED_WORDS.add("UNION");
        RESERVED_WORDS.add("UNIQUE");
        RESERVED_WORDS.add("UNLOCK");
        RESERVED_WORDS.add("UNSIGNED");
        RESERVED_WORDS.add("UPDATE");
        RESERVED_WORDS.add("USAGE");
        RESERVED_WORDS.add("USE");
        RESERVED_WORDS.add("USING");
        RESERVED_WORDS.add("UTC_DATE");
        RESERVED_WORDS.add("UTC_TIME");
        RESERVED_WORDS.add("UTC_TIMESTAMP");
        RESERVED_WORDS.add("VALUES");
        RESERVED_WORDS.add("VARBINARY");
        RESERVED_WORDS.add("VARCHAR");
        RESERVED_WORDS.add("VARCHARACTER");
        RESERVED_WORDS.add("VARYING");
        RESERVED_WORDS.add("WHEN");
        RESERVED_WORDS.add("WHERE");
        RESERVED_WORDS.add("WHILE");
        RESERVED_WORDS.add("WITH");
        RESERVED_WORDS.add("WRITE");
        RESERVED_WORDS.add("X509");
        RESERVED_WORDS.add("XOR");
        RESERVED_WORDS.add("YEAR_MONTH");
        RESERVED_WORDS.add("ZEROFILL");

    }

    public String getName() {
        return "MySQL";
    }

    @Override
    protected void createTypeMap() throws SQLException {
        typeMap = new HashMap<Integer, GenericTypeDescriptor>();

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
        typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("DATETIME", Types.TIMESTAMP, 0, "'",
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
        return RESERVED_WORDS.contains(word.toUpperCase());
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
    public void renameTable(SQLTable oldTable, SQLTable newTable) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>(0);
        println("RENAME TABLE "
				+ createPhysicalName(colNameMap, oldTable)
				+ " TO "
				+ createPhysicalName(colNameMap, newTable));
        endStatement(StatementType.ALTER, newTable);
    }

	@Override
	public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
		Map<String, SQLObject> empty = new HashMap<String, SQLObject>(0);
		print("ALTER TABLE ");
		print(createPhysicalName(empty, oldCol.getParent()));
		print(" CHANGE ");

		Map<String, SQLObject> cols = new HashMap<String, SQLObject>();
		try {
			for (SQLColumn col : oldCol.getParent().getColumns()) {
				cols.put(col.getPhysicalName(), col);
			}
		} catch (SQLObjectException e) {
			// can't do anything...
		}
		print(createPhysicalName(cols, newCol));
		print(" ");
		print(columnDefinition(newCol, cols));
		endStatement(StatementType.ALTER, newCol);
	}

	@Override
	public void renameRelationship(SQLRelationship oldFK, SQLRelationship newFK) {
		println("/* Renaming foreign key " + oldFK.getPhysicalName() + " to " + newFK.getPhysicalName() + " */");
		dropRelationship(oldFK);
		addRelationship(newFK);
	}

	@Override
	public void renameIndex(SQLIndex oldIndex, SQLIndex newIndex) throws SQLObjectException {
		println("/* Renaming index " + oldIndex.getPhysicalName() + " to " + newIndex.getPhysicalName() + " */");
		dropIndex(oldIndex);
		addIndex(newIndex);
	}
	
    /**
     * Overridden because MySQL doesn't allow the naming of PK constraints. This
     * version's text begins with "PRIMARY KEY" and is otherwise the same as the
     * generic method it overrides.
     */
    @Override
    protected void writePKConstraintClause(SQLIndex pk) throws SQLObjectException {
        if (!pk.isPrimaryKeyIndex()) {
            throw new IllegalArgumentException("The given index is not a primary key");
        }
        createPhysicalName(topLevelNames, pk);
        print("PRIMARY KEY (");

        boolean firstCol = true;
        for (SQLIndex.Column col : pk.getChildren(SQLIndex.Column.class)) {
            if (!firstCol) print(", ");
            print(col.getPhysicalName());
            firstCol = false;
        }
        print(")");
    }

    @Override
    public void dropPrimaryKey(SQLTable t) {
        print("\nALTER TABLE " + toQualifiedName(t.getName()) + " DROP PRIMARY KEY");
        endStatement(StatementType.DROP, t);
    }
    
    public void dropRelationship(SQLRelationship r) {

        print("\nALTER TABLE ");

        print(toQualifiedName(r.getFkTable()));
        print(" DROP FOREIGN KEY ");
        print(r.getName());
        endStatement(StatementType.DROP, r);
    }

    /**
     * Adds support for the MySQL auto_increment feature.
     * Enumerations are considered as a type itself.
     */
    @Override
    public String columnType(SQLColumn c) {
        String type;
        if (c.getConstraintType() == SQLTypeConstraint.ENUM) {
            type = columnEnumeration(c, c.getEnumerations());
        } else {
            type = super.columnType(c);
        }
        
        if (c.isAutoIncrement()) {
            type += " AUTO_INCREMENT";
        }
        return type;
    }
    
    @Override
    protected String columnEnumeration(SQLColumn c, List<SQLEnumeration> enumeration) {
        if (enumeration == null || enumeration.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (SQLEnumeration e : enumeration) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getName());
        }
        
        return "ENUM(" + sb.toString() + ")";
    }
    
    @Override
    public boolean supportsCheckConstraint() {
        return false;
    }
    
    @Override
    public boolean supportsEnumeration() {
        return true;
    }

    @Override
    protected String getPlatformName() {
        return "MySQL";
    }
    
    @Override
    public void addIndex(SQLIndex index) throws SQLObjectException {

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
        print(toQualifiedName(index.getParent()));
        print("\n ( ");

        boolean first = true;
        for (SQLIndex.Column c : index.getChildren(SQLIndex.Column.class)) {
            if (!first)
                print(", ");
            if (c.getColumn() != null) {
                print(c.getColumn().getPhysicalName());
            } else {
                print(c.getName());
            }
            print(c.getAscendingOrDescending() == AscendDescend.ASCENDING ? " ASC" : "");
            print(c.getAscendingOrDescending() == AscendDescend.DESCENDING ? " DESC" : "");
            first = false;
        }
        print(" )");
        endStatement(StatementType.CREATE, index);
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
	public void addComment(SQLTable t, boolean includeColumns) {
	    if (t.getRemarks() != null && t.getRemarks().trim().length() > 0) {
        	print("\nALTER TABLE ");
        	print(toQualifiedName(t));
        	print(" COMMENT '");     		
        	print(t.getRemarks().replaceAll("'", "''"));
        	print("'");
        	endStatement(StatementType.ALTER, t);
	    }
        if (includeColumns) {
            addColumnComments(t);
        }
	}
	
	@Override
	public void addComment(SQLColumn c) {
	    if (c.getRemarks() != null && c.getRemarks().trim().length() > 0) {
	        print("\nALTER TABLE ");
	        print(toQualifiedName(c.getParent()));
	        print(" MODIFY COLUMN ");
	        print(c.getPhysicalName());
	        print(" ");
	        print(c.getTypeName());
	        print(" COMMENT '");
            print(c.getRemarks().replaceAll("'", "''"));
            print("'");
            endStatement(StatementType.ALTER, c);
	    }
	}

    @Override
    public void addColumn(SQLColumn c) {
        Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
        print("\nALTER TABLE ");
        print(toQualifiedName(c.getParent()));
        print(" ADD COLUMN ");
        print(columnDefinition(c,colNameMap));
        
        /* MySQL supports adding a column at a particular position
         * instead of always being the last column
         */
        try {
            int colPosition = c.getParent().getChildren().indexOf(c);
            if (colPosition == 0) {
                print(" FIRST");
            } else if (colPosition > 0) {
                SQLObject precedingColumn = c.getParent().getChild(colPosition - 1);
                print(" AFTER " + precedingColumn.getPhysicalName());
            } else {
                throw new IllegalStateException(
                        "Column " + c + " is not a child of its parent!" +
                        " Children are: " + c.getParent().getChildren());
            }
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
        
        endStatement(StatementType.CREATE, c);
    }
    
    @Override
    public void modifyColumn(SQLColumn c) {
        Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
        SQLTable t = c.getParent();
        print("\nALTER TABLE ");
        print(toQualifiedName(t));
        print(" MODIFY COLUMN ");
        print(columnDefinition(c, colNameMap));
        endStatement(StatementType.MODIFY, c);
    }
    
    @Override
    public boolean supportsRollback() {
        return false;
    }
}
