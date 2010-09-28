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

import java.io.IOException;
import java.sql.DatabaseMetaData;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.event.SelectionEvent;

public class TestColumnEditPanel extends TestCase {
	SQLDatabase db;
	SQLTable table;
	SQLColumn col1;
	SQLColumn col2;
	SQLColumn col3;
	SQLColumn col4;
	SQLTable table2;
	ColumnEditPanel panel;
	
	protected void setUp() throws Exception {
		db = new SQLDatabase();
		table = new SQLTable(db,"Table1","remark1","Table",true);
		table2 = new SQLTable(db,"Table2","remark2","Table",true);
		db.addChild(0,table);
		col1 = new SQLColumn(null,"Column 1",1,2,3);
		col2 = new SQLColumn(null,"Column 2",2,3,4);
		col3 = new SQLColumn(null,"Column 3",1,2,3);
		col4 = new SQLColumn(null,"Column 4",1,2,3);	
		
		col2.setAutoIncrement(false);
		col2.setNullable(DatabaseMetaData.columnNoNulls);
		col2.setPrimaryKeySeq(0);
		table.addColumn(col1);
		table.addColumn(col2);
		table.addColumn(col3);
		table2.addColumn(col4);
		panel = new ColumnEditPanel(col2);
		
		super.setUp();
	}
 
	protected void tearDown() throws Exception {
		db = null;
		table = null;
		col1 = null;
		col2 = null;
		col3 = null;
		col4 = null;
		super.tearDown();
	}

	public void testEditColumn() throws ArchitectException {
	    assertEquals(2, table.getColumnIndex(col3));
	    assertEquals("The column we plan to edit should not be in PK",
                null, col3.getPrimaryKeySeq());

        panel.editColumn(col3);
        
		assertEquals("Wrong column name",col3.getName(),panel.getColName().getText());
		assertEquals("Wrong Precision",col3.getPrecision(),((Integer) (panel.getColPrec().getValue())).intValue());
		assertEquals("Wrong type",col3.getType(),((SQLType)(panel.getColType().getSelectedItem())).getType());
		assertEquals("Wrong Scale",col3.getScale(),((Integer) (panel.getColScale().getValue())).intValue());
		assertEquals(col3.isAutoIncrement(), panel.getColAutoInc().getModel().isSelected());
		assertEquals(col3.isPrimaryKey(), panel.getColInPK().getModel().isSelected());
		assertEquals(col3.getNullable() == DatabaseMetaData.columnNullable, panel.getColNullable().getModel().isSelected());
		assertEquals("None Specified",panel.getSourceDB().getText());
		assertEquals("None Specified",panel.getSourceTableCol().getText());

        panel.editColumn(col2);

        assertEquals("Wrong column name",col2.getName(),panel.getColName().getText());
        assertEquals("Wrong Precision",col2.getPrecision(),((Integer) (panel.getColPrec().getValue())).intValue());
        assertEquals("Wrong type",col2.getType(),((SQLType)(panel.getColType().getSelectedItem())).getType());
        assertEquals("Wrong Scale",col2.getScale(),((Integer) (panel.getColScale().getValue())).intValue());
        assertEquals(col2.isAutoIncrement(), panel.getColAutoInc().getModel().isSelected());
        assertEquals(col2.isPrimaryKey(), panel.getColInPK().getModel().isSelected());
        assertEquals(col2.getNullable() == DatabaseMetaData.columnNullable, panel.getColNullable().getModel().isSelected());
        assertEquals("None Specified",panel.getSourceDB().getText());
        assertEquals("None Specified",panel.getSourceTableCol().getText());
	}

	public void testApplyChanges() {

		panel.getColName().setText("CHANGED");
		panel.getColPrec().setValue(new Integer(1234));
		panel.getColType().setSelectedIndex(5);
		panel.getColScale().setValue(new Integer(5432));
		
		
		panel.getColInPK().getModel().setSelected(true);
		panel.getColAutoInc().getModel().setSelected(true);			
		panel.getColNullable().getModel().setSelected(true);
		
		panel.applyChanges();
		assertEquals("Panel check boxes borked",true,panel.getColAutoInc().getModel().isSelected());
		assertEquals("Wrong column name","CHANGED",col2.getName());
		assertEquals("Wrong Precision",1234,col2.getPrecision());
		assertEquals("Wrong type",1,col2.getType());
		assertEquals("Wrong Scale",5432,col2.getScale());
		assertTrue(col2.isAutoIncrement());
		assertTrue(col2.isPrimaryKey());
		assertTrue(col2.isDefinitelyNullable());
	}

	public void testDiscardChanges() {
		panel.getColName().setText("CHANGED");
		panel.getColPrec().setValue(new Integer(1234));
		panel.getColType().setSelectedIndex(5);
		panel.getColScale().setValue(new Integer(5432));
		panel.getColAutoInc().getModel().setSelected(true);	
		panel.getColInPK().getModel().setSelected(true);
		panel.getColNullable().getModel().setSelected(true);
		panel.discardChanges();
		
		assertEquals("Wrong column name","Column 2",col2.getName());
		assertEquals("Wrong Precision",3,col2.getPrecision());
		assertEquals("Wrong type",2,col2.getType());
		assertEquals("Wrong Scale",4,col2.getScale());
		assertFalse(col2.isAutoIncrement());
		assertFalse(col2.isPrimaryKey());
		assertFalse(col2.isDefinitelyNullable());
	}
	
	/** Tests for real problem (columns in pk were getting moved to bottom of PK after editing) */
	public void testPKColumnMoveRegression() throws ArchitectException{		
		SQLColumn c1 = new SQLColumn(table,"PKColumn 1",1,2,3);
		SQLColumn c2 = new SQLColumn(table,"PKColumn 2",1,2,3);
		table.addColumn(c1);
		table.addColumn(c2);
		c1.setPrimaryKeySeq(0);
		c2.setPrimaryKeySeq(1);
		assertEquals(5, table.getColumns().size());
        assertTrue(c1.isPrimaryKey());
        assertTrue(c1.isPrimaryKey());
		
        int previousIdx = table.getColumnIndex(table.getColumnByName("PKColumn 1"));
		ColumnEditPanel editPanel = new ColumnEditPanel(c1);
		editPanel.applyChanges();
		assertEquals(previousIdx, table.getColumnIndex(table.getColumnByName("PKColumn 1")));		
	}
    
    /**
     * This test makes sure that if a column is put into pk via
     * the ColumnEditPanel that after the modification, it is still
     * being selected.
     * @throws IOException 
     */
    public void testColumnStaysSelectedWhenMovedToPK() throws ArchitectException, IOException{        
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
        PlayPen pp = new PlayPen(session, db);        
        TablePane tp = new TablePane(table, pp);
        tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
        tp.selectColumn(table.getColumnIndex(col3));        
        ColumnEditPanel ce = new ColumnEditPanel(col3);        
        ce.getColInPK().setSelected(true);
        ce.applyChanges();
        assertEquals(table.getColumnIndex(col3), tp.getSelectedColumnIndex());
    }

}
