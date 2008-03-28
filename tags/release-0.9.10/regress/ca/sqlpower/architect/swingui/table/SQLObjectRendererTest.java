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
package ca.sqlpower.architect.swingui.table;

import javax.swing.JLabel;

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.table.BaseRendererTest;

public class SQLObjectRendererTest extends BaseRendererTest {

    /* Test rendering of SQLObjects  */
    public void test5() {
        SQLDatabase db = new SQLDatabase();
        db.setDataSource(new SPDataSource(new PlDotIni()));
        db.setName("MyName");

        // Test with SQLDatabase
        SQLObjectTableCellRenderer sqlRenderer = new SQLObjectTableCellRenderer();
        JLabel renderer = (JLabel) sqlRenderer.getTableCellRendererComponent(table, db, false, false, 0, 0);
        assertEquals("renderer formatted OK", "MyName", renderer.getText());

        // Test with SQLColumn
        SQLCatalog cat = new SQLCatalog();
        cat.setName("MyName2");
        renderer = (JLabel) sqlRenderer.getTableCellRendererComponent(table, cat, false, false, 0, 0);
        assertEquals("renderer formatted OK", "MyName2", renderer.getText());

        // Test for null
        renderer = (JLabel) sqlRenderer.getTableCellRendererComponent(table, null, false, false, 0, 0);
        assertEquals("renderer formatted OK", "null", renderer.getText());

    }
}
