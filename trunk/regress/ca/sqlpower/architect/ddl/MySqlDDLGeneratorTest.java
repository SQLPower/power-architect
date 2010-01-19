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
import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLTable;
import java.sql.Types;
import java.util.List;

public class MySqlDDLGeneratorTest extends TestCase {

	public void testGenerateComment() throws Exception {
		MySqlDDLGenerator ddl = new MySqlDDLGenerator();
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
		
		assertEquals("ALTER TABLE test_table COMMENT 'Test single '' quotes'", sql);

		sql = stmts.get(2).getSQLText().trim();
		assertEquals("ALTER TABLE test_table MODIFY COLUMN id INTEGER(0) COMMENT 'The row''s primary key'", sql);
		
		sql = stmts.get(3).getSQLText().trim();
        assertEquals("ALTER TABLE test_table MODIFY COLUMN name VARCHAR(50) COMMENT 'The person''s name'", sql);
	}
}
