/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLIndex.Column;

public class IndexColumnTableModelTest extends TestCase {

    IndexColumnTableModel tm;
    Column indexCol;
    protected void setUp() throws Exception {
        super.setUp();
        SQLTable t = new SQLTable(null,true);
        SQLColumn col = new SQLColumn(t,"col1",1,0,0);
        t.addColumn(col);
        SQLIndex i = new SQLIndex("name",true,"a", "BTREE","");
        t.getIndicesFolder().addChild(i);
        i.addIndexColumn(col, AscendDescend.DESCENDING);
        i.addChild(i.new Column("expression",AscendDescend.DESCENDING));
        indexCol = i.getChild(0);
        tm = new IndexColumnTableModel(i,t);
        
    }

    public void testIsCellEditable() {
        assertTrue(tm.isCellEditable(0, 0));
        assertTrue(tm.isCellEditable(1, 1));
        assertTrue(tm.isCellEditable(0, 2));
    }

    public void testGetColumnClassInt() {
        assertEquals(String.class,tm.getColumnClass(0));
        assertEquals(SQLColumn.class,tm.getColumnClass(1));
        assertEquals(Boolean.class,tm.getColumnClass(2));
        assertEquals(Boolean.class,tm.getColumnClass(3));
    }

    public void testGetColumnCount() {
        assertEquals(4,tm.getColumnCount());
    }

    
    public void testGetRowCount() {
        assertEquals(2,tm.getRowCount());
    }

    public void testGetValueAt() {
        assertEquals("col1", tm.getValueAt(0,0));
        assertEquals(indexCol.getColumn(), tm.getValueAt(0,1));
        assertEquals(SQLIndex.AscendDescend.DESCENDING,tm.getValueAt(0, 2));
    }

}
