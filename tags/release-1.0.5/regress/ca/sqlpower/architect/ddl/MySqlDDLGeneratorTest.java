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

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.PropertyType;

import java.sql.Types;
import java.util.List;

public class MySqlDDLGeneratorTest extends TestCase {

	public void testGenerateComment() throws Exception {
		MySqlDDLGenerator ddl = new MySqlDDLGenerator();
		SQLTable tbl = new SQLTable();
		tbl.initFolders(true);
		tbl.setPhysicalName("test_table");
		tbl.setRemarks("Test single ' quotes");
		SQLColumn id = new SQLColumn(tbl, "id", Types.INTEGER, 1, 0);
		UserDefinedSQLType idUpstreamType = new UserDefinedSQLType();
		idUpstreamType.setName("INTEGER");
        idUpstreamType.setType(Types.INTEGER);
        idUpstreamType.setPrecision("MySQL", 1);
        idUpstreamType.setPrecisionType("MySQL", PropertyType.VARIABLE);
		idUpstreamType.setScaleType("MySQL", PropertyType.NOT_APPLICABLE);
        id.getUserDefinedSQLType().setUpstreamType(idUpstreamType);
		id.setRemarks("The row's primary key");
		tbl.addColumn(id);
		SQLColumn name = new SQLColumn(tbl, "name", Types.VARCHAR, 50, 0);
		UserDefinedSQLType nameUpstreamType = new UserDefinedSQLType();
		nameUpstreamType.setName("VARCHAR");
        nameUpstreamType.setType(Types.VARCHAR);
        nameUpstreamType.setPrecision("MySQL", 50);
        nameUpstreamType.setPrecisionType("MySQL", PropertyType.VARIABLE);
		nameUpstreamType.setScaleType("MySQL", PropertyType.NOT_APPLICABLE);
        name.getUserDefinedSQLType().setUpstreamType(nameUpstreamType);
		name.setRemarks("The person's name");
		tbl.addColumn(name);
		ddl.addTable(tbl);

		// the first statement is the CREATE table
		// second, third and fourth statements should be the COMMENT ON ... statements
		List<DDLStatement> stmts = ddl.getDdlStatements();
		assertEquals(4, stmts.size());
		
		String sql = stmts.get(1).getSQLText().trim();	
		
		assertEquals("ALTER TABLE test_table COMMENT 'Test single '' quotes'", sql);

		sql = stmts.get(2).getSQLText().trim();
		assertEquals("ALTER TABLE test_table MODIFY COLUMN id INTEGER(1) COMMENT 'The row''s primary key'", sql);
		
		sql = stmts.get(3).getSQLText().trim();
        assertEquals("ALTER TABLE test_table MODIFY COLUMN name VARCHAR(50) COMMENT 'The person''s name'", sql);
	}
}
