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
import java.util.HashMap;

import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.Deferrability;
import ca.sqlpower.architect.SQLRelationship.UpdateDeleteRule;

// TODO: override to_identifier routine to ensure identifier names are legal
// and unique for DB2.  See the Oracle, SQL Server, and Postgres DDL generators
// for hints on how it should be written.  Development of this stuff has been
// deferred until we have an architect customer who wants to use it with DB2.

public class DB2DDLGenerator extends GenericDDLGenerator {

    public static final String GENERATOR_VERSION = "$Revision$";

    public DB2DDLGenerator() throws SQLException {
		super();
	}

    public String getName() {
        return "IBM DB2";
    }

    @Override
	public void writeHeader() {
		println("-- Created by SQLPower DB2 DDL Generator "+GENERATOR_VERSION+" --");
	}

    @Override
	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();

		typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BLOB", Types.BINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("DECIMAL", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 254, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 10, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 31, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 53, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 53, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("BLOB", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("CLOB", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("DECIMAL", Types.NUMERIC, 31, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("FLOAT", Types.REAL, 31, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 8, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 26, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("SMALLINT", Types.TINYINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("BLOB", Types.VARBINARY, 2147483647, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 32672, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

	/**
	 * No catalogs in DB2.
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
    
    @Override
    public boolean supportsDeleteAction(SQLRelationship r) {
        return r.getDeleteRule() != UpdateDeleteRule.SET_DEFAULT;
    }

    @Override
    public boolean supportsUpdateAction(SQLRelationship r) {
        return (r.getUpdateRule() == UpdateDeleteRule.RESTRICT)
            || (r.getUpdateRule() == UpdateDeleteRule.NO_ACTION);
    }
    
    /**
     * DB2 does not support a deferrability clause on FK constraints, but its
     * default behaviour is like DEFERRABLE on other platforms. So this method
     * says DEFERRABLE is supported, and the other options are not.
     */
    @Override
    public boolean supportsDeferrabilityPolicy(SQLRelationship r) {
        return r.getDeferrability() == Deferrability.NOT_DEFERRABLE;
    }
    
    @Override
    public String getDeferrabilityClause(SQLRelationship r) {
        if (!supportsDeferrabilityPolicy(r)) {
            throw new IllegalArgumentException("DB2 does not support deferrability option: " + r.getDeferrability());
        } else {
            return "";
        }
    }
}
