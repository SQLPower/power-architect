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

public class Oracle8DDLGenerator extends OracleDDLGenerator {

    public static final String GENERATOR_VERSION = "$Revision: 2409 $";
    
    public Oracle8DDLGenerator() throws SQLException {
        super();
    }
    
    public String getName() {
        return "Oracle 8i";
    }

    @Override
    protected String getPlatformName() {
        return "Oracle 8i";
    }
    
    @Override
    public void writeHeader() {
        println("-- Created by SQLPower Oracle 8i DDL Generator "+GENERATOR_VERSION+" --");
    }
    
    @Override
    protected void createTypeMap() throws SQLException {
        typeMap = new HashMap<Integer, GenericTypeDescriptor>();

        typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("NUMBER", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("RAW", Types.BINARY, 2000, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("NUMBER", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 1, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 2000, "'", "'", DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("NUMBER", Types.DECIMAL, 38, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("NUMBER", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("NUMBER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("LONG RAW", Types.LONGVARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMBER", Types.NUMERIC, 38, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("NUMBER", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("NUMBER", Types.SMALLINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("DATE", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("DATE", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("NUMBER", Types.TINYINT, 38, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("LONG", Types.LONGVARCHAR, 2000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("LONG RAW", Types.VARBINARY, 2000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR2", Types.VARCHAR, 4000, "'", "'", DatabaseMetaData.columnNullable, true, false));
    }

}
