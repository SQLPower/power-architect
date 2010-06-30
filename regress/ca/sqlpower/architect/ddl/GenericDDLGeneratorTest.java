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

import java.sql.Types;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.PropertyType;

public class GenericDDLGeneratorTest extends TestCase {

	public void testGenerateComment() throws Exception {
		GenericDDLGenerator ddl = new GenericDDLGenerator();
		SQLTable tbl = new SQLTable();
		tbl.initFolders(true);
		tbl.setPhysicalName("test_table");
		tbl.setRemarks("Test single ' quotes");
		SQLColumn id = new SQLColumn(tbl, "id", Types.INTEGER, 0, 0);
		id.setRemarks("The row's primary key");
		tbl.addColumn(id);
		SQLColumn name = new SQLColumn(tbl, "name", Types.VARCHAR, 50, 0);
		name.setRemarks("The person's name");
		tbl.addColumn(name);
		ddl.addTable(tbl);

		// the first statement is the CREATE table
		// second, third and fourth statements should be the COMMENT ON ... statements
		List<DDLStatement> stmts = ddl.getDdlStatements();
		assertEquals(4, stmts.size());

		String sql = stmts.get(1).getSQLText().trim();
		assertEquals("COMMENT ON TABLE test_table IS 'Test single '' quotes'", sql);

		sql = stmts.get(2).getSQLText().trim();
		assertEquals("COMMENT ON COLUMN test_table.id IS 'The row''s primary key'", sql);

		sql = stmts.get(3).getSQLText().trim();
		assertEquals("COMMENT ON COLUMN test_table.name IS 'The person''s name'", sql);
	}

	public void testNewTypes() throws Exception {
		GenericDDLGenerator ddl = new GenericDDLGenerator(false);
		SQLTable tbl = new SQLTable();
		tbl.initFolders(true);
		tbl.setPhysicalName("test_table");
		
		SQLColumn col1 = new SQLColumn(tbl, "N_TEST", SQLType.NVARCHAR, "NVARCHAR", 1000, 0, 0, "",
		  null, false);
		col1.getUserDefinedSQLType().getDefaultPhysicalProperties().setScaleType(PropertyType.NOT_APPLICABLE);
		col1.getUserDefinedSQLType().getDefaultPhysicalProperties().setPrecisionType(PropertyType.CONSTANT);
		assertEquals("NVARCHAR(1000)", ddl.getColumnDataTypeName(col1));
		assertEquals("NVARCHAR(1000)", ddl.columnType(col1));

		SQLColumn col2 = new SQLColumn(tbl, "N_CHARTEST", SQLType.NCHAR, "NCHAR", 1000, 0, 0, "",
		  null, false);
		col2.getUserDefinedSQLType().getDefaultPhysicalProperties().setScaleType(PropertyType.NOT_APPLICABLE);
		col2.getUserDefinedSQLType().getDefaultPhysicalProperties().setPrecisionType(PropertyType.CONSTANT);
		assertEquals("NCHAR(1000)", ddl.getColumnDataTypeName(col2));
		assertEquals("NCHAR(1000)", ddl.columnType(col2));

		SQLColumn col3 = new SQLColumn(tbl, "N_CLOB_TEST", SQLType.NCLOB, "NCLOB", 1000, 0, 0, "",
		  null, false);
		col3.getUserDefinedSQLType().getDefaultPhysicalProperties().setPrecisionType(PropertyType.NOT_APPLICABLE);
		col3.getUserDefinedSQLType().getDefaultPhysicalProperties().setScaleType(PropertyType.NOT_APPLICABLE);
		assertEquals("NCLOB", ddl.getColumnDataTypeName(col3));
		assertEquals("NCLOB", ddl.columnType(col3));

	}
}
