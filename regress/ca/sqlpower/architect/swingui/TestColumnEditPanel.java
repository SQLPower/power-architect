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

import java.io.IOException;
import java.sql.DatabaseMetaData;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ColumnEditPanel.YesNoEnum;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

public class TestColumnEditPanel extends TestCase {
	SQLDatabase db;
	SQLTable table;
	SQLColumn col1;
	SQLColumn col2;
	SQLColumn col3;
	SQLColumn col4;
	SQLTable table2;
	ColumnEditPanel panel;
	ArchitectSwingSession session;
	
	protected void setUp() throws Exception {
		db = new SQLDatabase();
		table = new SQLTable(db,"Table1","remark1","Table",true);
		table2 = new SQLTable(db,"Table2","remark2","Table",true);
		db.addChild(table,0);
		col1 = new SQLColumn(null,"Column 1",1,2,3);
		col2 = new SQLColumn(null,"Column 2",2,3,4);
		col3 = new SQLColumn(null,"Column 3",1,2,3);
		col4 = new SQLColumn(null,"Column 4",1,2,3);	
		
		col2.setPhysicalName("Physical Name 2");
		col2.setAutoIncrement(false);
		col2.setNullable(DatabaseMetaData.columnNoNulls);
		table.addColumn(col1);
		table.addColumn(col2);
		table.addColumn(col3);
		table2.addColumn(col4);
		table.addToPK(col2);
		TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
		session = context.createSession();
		panel = new ColumnEditPanel(col2, session);
		
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

	public void testSetComponentsToColumnValues() throws SQLObjectException {
	    assertEquals(2, table.getColumnIndex(col3));
	    assertFalse("The column we plan to edit should not be in PK",
                col3.isPrimaryKey());

	    col3.getUserDefinedSQLType().setUpstreamType(session.getSQLTypes().get(0));
	    panel = new ColumnEditPanel(col3, session);
        
		assertEquals("Wrong column logical name",col3.getName(),panel.getColLogicalName().getText());
		assertEquals("Wrong Precision",col3.getPrecision(),((Integer) panel.getColPrec().getValue()).intValue());
		assertEquals("Wrong type",col3.getType(),((UserDefinedSQLType)panel.getColType().getLastSelectedPathComponent()).getType().intValue());
		assertEquals("Wrong Scale",col3.getScale(),((Integer) (panel.getColScale().getValue())).intValue());
		assertEquals(col3.isAutoIncrement(), ((YesNoEnum) panel.getColAutoInc().getSelectedItem()).getValue());
		assertEquals(col3.isPrimaryKey(), panel.getColInPK().getModel().isSelected());
		assertEquals(col3.getNullable() == DatabaseMetaData.columnNullable, ((YesNoEnum) panel.getColNullable().getSelectedItem()).getValue());
		assertEquals("None Specified",panel.getSourceLabel().getText());

		col2.getUserDefinedSQLType().setUpstreamType(session.getSQLTypes().get(1));
        panel = new ColumnEditPanel(col2, session);

        assertEquals("Wrong column logical name",col2.getName(),panel.getColLogicalName().getText());
        assertEquals("Wrong Precision",col2.getPrecision(),((Integer) panel.getColPrec().getValue()).intValue());
        assertEquals("Wrong type",col2.getType(),((UserDefinedSQLType)panel.getColType().getLastSelectedPathComponent()).getType().intValue());
        assertEquals("Wrong Scale",col2.getScale(),((Integer) (panel.getColScale().getValue())).intValue());
        assertEquals(col2.isAutoIncrement(), ((YesNoEnum) panel.getColAutoInc().getSelectedItem()).getValue());
        assertEquals(col2.isPrimaryKey(), panel.getColInPK().getModel().isSelected());
        assertEquals(col2.getNullable() == DatabaseMetaData.columnNullable, ((YesNoEnum) panel.getColNullable().getSelectedItem()).getValue());
        assertEquals("None Specified",panel.getSourceLabel().getText());
	}

	public void testApplyChanges() {

		panel.getColPhysicalName().setText("CHANGED");
		panel.getColLogicalName().setText("Easier Use Column Name");
		panel.getColType().setSelectionRow(1);
		panel.getColPrec().setValue(new Integer(1234));
		panel.getTypeOverrideMap().get(panel.getColPrec()).setSelected(true);
		panel.getColScale().setValue(new Integer(5432));
		panel.getTypeOverrideMap().get(panel.getColScale()).setSelected(true);
		
		
		panel.getColInPK().getModel().setSelected(true);
		panel.getColAutoInc().setSelectedItem(YesNoEnum.YES);
		panel.getTypeOverrideMap().get(panel.getColAutoInc()).setSelected(true);
		panel.getColNullable().setSelectedItem(YesNoEnum.YES);
		panel.getTypeOverrideMap().get(panel.getColNullable()).setSelected(true);
		
		panel.applyChanges();
		assertEquals("Panel check boxes borked",true, ((YesNoEnum) panel.getColAutoInc().getSelectedItem()).getValue());
		assertEquals("Wrong column physical name","CHANGED",col2.getPhysicalName());
		assertEquals("Wrong column logical name","Easier Use Column Name",col2.getName());
		assertEquals("Wrong Precision",1234,col2.getPrecision());
		assertEquals("Wrong type",1,col2.getType());
		assertEquals("Wrong Scale",5432,col2.getScale());
		assertTrue(col2.isAutoIncrement());
		assertTrue(col2.isPrimaryKey());
		assertTrue(col2.isDefinitelyNullable());
	}

	public void testDiscardChanges() throws Exception {
	    table.moveAfterPK(col2);
	    assertFalse(col2.isPrimaryKey());
	    
		panel.getColPhysicalName().setText("CHANGED");
		panel.getColLogicalName().setText("Easier Use Column Name");
		panel.getColType().setSelectionRow(1);
		panel.getColPrec().setValue(new Integer(1234));
		panel.getColScale().setValue(new Integer(5432));
		panel.getColAutoInc().setSelectedItem(YesNoEnum.YES);	
		panel.getColInPK().getModel().setSelected(true);
		panel.getColNullable().setSelectedItem(YesNoEnum.YES);
		panel.discardChanges();
		
		assertEquals("Wrong column physical name","Physical Name 2",col2.getPhysicalName());
		assertEquals("Wrong column logical name","Column 2",col2.getName());
		assertEquals("Wrong column data type", 2, col2.getType());
		assertEquals("Wrong Precision",3,col2.getPrecision());
		assertEquals("Wrong Scale",4,col2.getScale());
		assertFalse(col2.isAutoIncrement());
		assertFalse(col2.isPrimaryKey());
		assertFalse(col2.isDefinitelyNullable());
	}
	
	/** Tests for real problem (columns in pk were getting moved to bottom of PK after editing) */
	public void testPKColumnMoveRegression() throws Exception{		
		SQLColumn c1 = new SQLColumn(table,"PKColumn 1",1,2,3);
		SQLColumn c2 = new SQLColumn(table,"PKColumn 2",1,2,3);
		table.addColumn(c1);
		table.addColumn(c2);
		table.addToPK(c1);
		table.addToPK(c2);
		assertEquals(5, table.getColumns().size());
        assertTrue(c1.isPrimaryKey());
        assertTrue(c2.isPrimaryKey());
		
        int previousIdx = table.getColumnIndex(table.getColumnByName("PKColumn 1"));
        ColumnEditPanel editPanel = new ColumnEditPanel(c1, session);
		editPanel.applyChanges();
		assertEquals(previousIdx, table.getColumnIndex(table.getColumnByName("PKColumn 1")));		
	}
    
    /**
     * This test makes sure that if a column is put into pk via
     * the ColumnEditPanel that after the modification, it is still
     * being selected.
     * @throws IOException 
     */
    public void testColumnStaysSelectedWhenMovedToPK() throws SQLObjectException, IOException {
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
        PlayPen pp = new PlayPen(session);        
        TablePane tp = new TablePane(table, pp.getContentPane());
        tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
        tp.selectItem(table.getColumnIndex(col3));
        ColumnEditPanel ce = new ColumnEditPanel(col3, session);        
        ce.getColInPK().setSelected(true);
        ce.applyChanges();
        assertEquals(table.getColumnIndex(col3), tp.getSelectedItemIndex());
    }

}
