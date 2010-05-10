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

package ca.sqlpower.architect.olap;

import junit.framework.TestCase;
import ca.sqlpower.architect.olap.MondrianModel.Join;
import ca.sqlpower.architect.olap.MondrianModel.Table;

public class OLAPUtilTest extends TestCase {

    public void testGenerateSQLFromJoin1() {
        Join join = new Join();
        Table table1 = new Table();
        table1.setAlias("table1");
        table1.setName("Table_One");
        table1.setSchema("schema");
        join.setLeft(table1);
        join.setLeftKey("column1");
        
        Table table2 = new Table();
        table2.setAlias("table2");
        table2.setName("Table_Two");
        table2.setSchema("schema");
        join.setRight(table2);
        join.setRightKey("column2");

        String sql = OLAPUtil.generateSQLFromJoin(join);
        assertEquals("SELECT * FROM schema.Table_One table1 JOIN schema.Table_Two table2 ON Table_One.column1=Table_Two.column2", sql);
    }

    public void testGenerateSQLFromJoinWithNestedJoin() {
        Join join = new Join();
        Table table1 = new Table();
        table1.setAlias("table1");
        table1.setName("Table_One");
        table1.setSchema("schema");
        join.setLeft(table1);
        join.setLeftKey("column1");
        join.setLeftAlias("table1");
        
        Join nestedJoin = new Join();
        Table table2 = new Table();
        table2.setAlias("table2");
        table2.setName("Table_Two");
        table2.setSchema("schema");
        nestedJoin.setLeft(table2);
        nestedJoin.setLeftKey("column2");
        nestedJoin.setLeftAlias("table2");
        
        Table table3 = new Table();
        table3.setAlias("table3");
        table3.setName("Table_Three");
        table3.setSchema("schema");
        nestedJoin.setRight(table3);
        nestedJoin.setRightKey("column3");
        nestedJoin.setRightAlias("table3");
        
        join.setRight(nestedJoin);
        join.setRightKey("column2");
        join.setRightAlias("nestedJoin");

        String sql = OLAPUtil.generateSQLFromJoin(join);
        assertEquals("SELECT * FROM schema.Table_One table1 JOIN (SELECT * FROM schema.Table_Two table2 JOIN schema.Table_Three table3 ON table2.column2=table3.column3) AS nestedJoin ON table1.column1=nestedJoin.column2", sql);
    }
}
