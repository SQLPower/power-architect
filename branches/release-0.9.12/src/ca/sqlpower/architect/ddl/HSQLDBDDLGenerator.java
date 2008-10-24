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

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.Deferrability;
import ca.sqlpower.architect.SQLRelationship.UpdateDeleteRule;

/**
 * Implements the quirks required for successful DDL generation that targets
 * HSQLDB versions 1.8 and newer.
 * <p>
 * Sorry about the class name.. we kind of got painted into a corner with
 * our naming convention, and by the time HSQLDB support came along, it was
 * too late to change it.
 */
public class HSQLDBDDLGenerator extends GenericDDLGenerator {
    
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
        typeMap = new HashMap();

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
}
