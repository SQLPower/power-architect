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
import java.util.Map;

import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Implements the quirks required for successful DDL generation that targets
 * HSQLDB versions 1.8 and newer.
 * <p>
 * Sorry about the class name.. we kind of got painted into a corner with
 * our naming convention, and by the time HSQLDB support came along, it was
 * too late to change it.
 */
public class HSQLDBDDLGenerator extends GenericDDLGenerator {
    
    public static final String GENERATOR_VERSION = "$Revision: 2933 $";
    
    private final String REGEX_CRLF = "(\r\n|\n\r|\r|\n)";

    public HSQLDBDDLGenerator() throws SQLException {
        super();
    }
    
    @Override
    public String getName() {
        return "HSQLDB";
    }
 
    @Override
    public String getCatalogTerm() {
        return null;
    }

    @Override
    public String getSchemaTerm() {
        return "Schema";
    }

    @Override
    public String columnType(SQLColumn c) {
        if (c.isAutoIncrement()) {
            return "IDENTITY";
        } else {
            return super.columnType(c);
        }
    }
    
    @Override
    protected String getPlatformName() {
        return "HSQLDB";
    }
    
    @Override
    public String getDeferrabilityClause(SQLRelationship r) {
        if (supportsDeferrabilityPolicy(r)) {
            return "";
        } else {
            throw new UnsupportedOperationException(getName() + " does not support " + 
                    r.getName() + "'s deferrability policy (" + r.getDeferrability() + ").");
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
    protected void createTypeMap() throws SQLException {
        typeMap = new HashMap<Integer, GenericTypeDescriptor>();

        typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 1000, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("LONGVARBINARY", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("BOOLEAN", Types.BOOLEAN, 1, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("LONGVARCHAR", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("LONGVARBINARY", Types.LONGVARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("LONGVARCHAR", Types.LONGVARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
    }
    
    @Override
    public boolean supportsUpdateAction(SQLRelationship r) {
        return r.getUpdateRule() != UpdateDeleteRule.RESTRICT;
    }
    
    @Override
    public String getUpdateActionClause(SQLRelationship r) {
        if (r.getUpdateRule() == UpdateDeleteRule.RESTRICT) {
            throw new IllegalArgumentException("Unsupported update action: " + r.getUpdateRule());
        } else {
            return super.getUpdateActionClause(r);
        }
    }
    
    @Override
    public boolean supportsDeleteAction(SQLRelationship r) {
        return r.getDeleteRule() != UpdateDeleteRule.RESTRICT;
    }
    
    @Override
    public String getDeleteActionClause(SQLRelationship r) {
        if (r.getDeleteRule() == UpdateDeleteRule.RESTRICT) {
            throw new IllegalArgumentException("Unsupported update action: " + r.getDeleteRule());
        } else {
            return super.getDeleteActionClause(r);
        }
    }
    
    @Override
    public String toString() {
        return "SQL Power HSQLDB DDL Generator " + GENERATOR_VERSION;
    }

    /**
     * HSQLDB (as of version 1.8.0.10) does not appear to support remarks. So
     * addComment() simply prints out a SQL comment containing the remarks from
     * the user.
     * 
     * @param t
     *            The table to add the comments/remarks to
     * @param includeColumns
     *            If true, then remarks for the columns defined in the given
     *            table will also be printed
     */
    @Override
    public void addComment(SQLTable t, boolean includeColumns) {
        if (t.getRemarks() != null && t.getRemarks().trim().length() > 0) {
            print("\n-- Comment for table [" + t.getPhysicalName() + "]: ");
            print(t.getRemarks().replaceAll(REGEX_CRLF, "\n-- "));
            endStatement(StatementType.COMMENT, t);

            if (includeColumns) {
                addColumnComments(t);
            }
        }
    }
    
   /**
     * HSQLDB (as of version 1.8.0.10) does not appear to support remarks. So
     * addComment() simply prints out a SQL comment containing the remarks from
     * the user.
     * 
     * @param c
     *            The {@link SQLColumn} to add the comment/remarks to
     */
    @Override
    public void addComment(SQLColumn c) {
        if (c.getRemarks() == null || c.getRemarks().trim().length() == 0) return;

        print("\n-- Comment for column [");
        print(c.getName());
        print("]: ");
        print(c.getRemarks().replaceAll(REGEX_CRLF, "\n-- "));
        endStatement(StatementType.COMMENT, c);
    }

	/**
	 * Generate the SQL to rename a column.
	 * <br/>
	 * The default implementation works for PostgreSQL, Oracle
	 * @param oldTable
	 * @param newTable
	 */
	public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		print("\nALTER TABLE ");
		print(toQualifiedName(oldCol.getParent()));
		print(" ALTER COLUMN ");
		print(createPhysicalName(colNameMap, oldCol));
        print(" RENAME TO ");
		print(createPhysicalName(colNameMap, newCol));
		endStatement(StatementType.ALTER, newCol);
    }

}
