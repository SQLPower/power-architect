package ca.sqlpower.architect.ddl;

import java.sql.*;
import java.util.*;

public class DB2DDLGenerator extends GenericDDLGenerator {
	public static final String GENERATOR_VERSION = "$Revision$";

	public DB2DDLGenerator() {
	}

	public void writeHeader() {
		println("-- Created by SQLPower DB2 DDL Generator "+GENERATOR_VERSION+" --");
	}

	protected void createTypeMap() throws SQLException {
		typeMap = new HashMap();
		
		typeMap.put(new Integer(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.BINARY), new GenericTypeDescriptor("BLOB", Types.BINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BIT), new GenericTypeDescriptor("DECIMAL", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.BLOB), new GenericTypeDescriptor("BLOB", Types.BLOB, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 254, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.CLOB), new GenericTypeDescriptor("CLOB", Types.CLOB, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 10, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 31, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 53, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 53, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 10, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.LONGVARBINARY), new GenericTypeDescriptor("BLOB", Types.LONGVARBINARY, 2147483647, "0x", null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.LONGVARCHAR), new GenericTypeDescriptor("CLOB", Types.LONGVARCHAR, 2147483647, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.NUMERIC), new GenericTypeDescriptor("DECIMAL", Types.NUMERIC, 31, null, null, DatabaseMetaData.columnNullable, true, true));
		typeMap.put(new Integer(Types.REAL), new GenericTypeDescriptor("FLOAT", Types.REAL, 31, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 8, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 26, "'", "'", DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.TINYINT), new GenericTypeDescriptor("SMALLINT", Types.TINYINT, 5, null, null, DatabaseMetaData.columnNullable, false, false));
		typeMap.put(new Integer(Types.VARBINARY), new GenericTypeDescriptor("BLOB", Types.VARBINARY, 2147483647, null, null, DatabaseMetaData.columnNullable, true, false));
		typeMap.put(new Integer(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 32672, "'", "'", DatabaseMetaData.columnNullable, true, false));
	}

	/**
	 * Returns the string "Database".
	 */
	public String getCatalogTerm() {
		return "Database";
	}

	/**
	 * Returns the string "Schema".
	 */
	public String getSchemaTerm() {
		return "Schema";
	}
}
