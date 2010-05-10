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

package ca.sqlpower.architect.swingui;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.IndexColumnTable.IndexColumnTableModel;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLIndex.Column;

public class IndexColumnTableTest extends TestCase {

    /**
     * This will test the PopulateModel method on the IndexColumnTableModel
     * to verify the table does not throw an exception when an index is added
     * that has a column with no SQLColumn. This is related to bug 1526.
     */
    public void testTableModelPopulateAllowsNulls() throws Exception {
        SQLTable table = new SQLTable();
        table.initFolders(true);
        SQLIndex index = new SQLIndex();
        Column col = new Column("TestCol", AscendDescend.UNSPECIFIED);
        index.addChild(col);
        table.addIndex(index);
        SQLIndex copyIndex = new SQLIndex(index);
        IndexColumnTable ict = new IndexColumnTable(table, copyIndex, index);
        
        IndexColumnTableModel ictm = ict.new IndexColumnTableModel(copyIndex, table, index);
        
        assertEquals(1, ictm.getRowCount());
        assertEquals(1, index.getChildCount());
        
        System.out.println(ictm.getRowList());
        
        ict.finalizeIndex();
        
        System.out.println(ictm.getRowList());
        
        assertEquals(1, index.getChildCount());
        
        //Assert the column not the null SQLColumn in the column to come back.
        assertEquals(col, ictm.getValueAt(0, 1));
    }
}
