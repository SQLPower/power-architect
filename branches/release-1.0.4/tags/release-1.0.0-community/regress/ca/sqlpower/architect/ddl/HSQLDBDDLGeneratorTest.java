/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import junit.framework.TestCase;

public class HSQLDBDDLGeneratorTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * HSQLDB as of version 1.8.0.10 does not appear to support SQL remarks, so
     * the DDL generator should just place remarks into SQL comments.
     * 
     * I could not find anything in the HSQLDB documentation that would suggest
     * that it supports SQL remarks, but if anyone finds such documentation
     * either now, or in future versions of HSQLDB, do let us know.
     * 
     * @throws Exception
     */
    public void testAddComments() throws Exception {
        HSQLDBDDLGenerator ddl = new HSQLDBDDLGenerator();
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
        // second, third and fourth statements should be the comments as SQL comments starting with --
        // as HSQLDB does not support comments on database objects
        List<DDLStatement> stmts = ddl.getDdlStatements();
        assertEquals(4, stmts.size());
        assertEquals("-- Comment for table [test_table]: Test single ' quotes", stmts.get(1).getSQLText().trim());
        assertEquals("-- Comment for column [id]: The row's primary key", stmts.get(2).getSQLText().trim());
        assertEquals("-- Comment for column [name]: The person's name", stmts.get(3).getSQLText().trim());
    }
}
