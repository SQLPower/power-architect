package ca.sqlpower.architect.swingui;

import java.sql.DatabaseMetaData;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;

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
		panel = new ColumnEditPanel(table,table.getColumnIndex(col2));
		
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

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.ColumnEditPanel.setModel(SQLTable)'
	 */
	public void testSetModel() {
		
		assertEquals("Wrong model was added by the constructor",panel.getModel(),table);
		panel.setModel(table2);
		assertEquals("Error changing models",panel.getModel(),table2);
		
	}

	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.ColumnEditPanel.editColumn(int)'
	 */
	public void testEditColumn() throws ArchitectException {
        assertEquals(null, col1.getPrimaryKeySeq());
        //assertEquals(0, table.getPkSize());
        assertEquals(1, table.getColumnIndex(col2));
	    assertEquals("The column we're editing is not in PK", null, col2.getPrimaryKeySeq());
        
		assertEquals("Wrong column name",col2.getName(),panel.getColName().getText());
		assertEquals("Wrong Precision",col2.getPrecision(),((Integer) (panel.getColPrec().getValue())).intValue());
		assertEquals("Wrong type",2,((SQLType)(panel.getColType().getSelectedItem())).getType());
		assertEquals("Wrong Scale",col2.getScale(),((Integer) (panel.getColScale().getValue())).intValue());
		assertFalse(panel.getColAutoInc().getModel().isSelected());
		assertFalse(panel.getColAutoInc().getModel().isEnabled());
		assertFalse(panel.getColInPK().getModel().isSelected());
		assertTrue(panel.getColInPK().getModel().isEnabled());
		assertFalse(panel.getColNullable().getModel().isSelected());
		assertTrue(panel.getColNullable().getModel().isEnabled());
		assertEquals("None Specified",panel.getSourceDB().getText());
		assertEquals("None Specified",panel.getSourceTableCol().getText());
	}


	/*
	 * Test method for 'ca.sqlpower.architect.swingui.ColumnEditPanel.cleanup()'
	 */
	public void testCleanup() {
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.ColumnEditPanel.applyChanges()'
	 */
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

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.ColumnEditPanel.discardChanges()'
	 */
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
		assertEquals (5, table.getColumns().size());
        assertTrue (c1.isPrimaryKey());
        assertTrue (c1.isPrimaryKey());
		
        int previousIdx = table.getColumnIndex(table.getColumnByName("PKColumn 1"));
		ColumnEditPanel editPanel = new ColumnEditPanel(table, previousIdx);
		editPanel.applyChanges();
		assertEquals (previousIdx, table.getColumnIndex(table.getColumnByName("PKColumn 1")));		
	}
    
    /*
     * This test makes sure that if a column is put into pk via
     * the ColumnEditPanel that after the modification, it is still
     * being selected.
     */
    
    public void testSelectColumnTestAfterKeyChange() throws ArchitectException{        
        PlayPen pp = new PlayPen(db);        
        TablePane tp = new TablePane(table, pp);
        tp.setSelected(true);
        tp.selectColumn(table.getColumnIndex(col3));        
        ColumnEditPanel ce = new ColumnEditPanel(table, table.getColumnIndex(col3));        
        ce.getColInPK().setSelected(true);
        ce.applyChanges();
        assertEquals(table.getColumnIndex(col3), tp.getSelectedColumnIndex());
    }
    
	
	/*
	 * This test case is making sure that we let the user
	 * know that they cannot give a column an empty name.
	 * If a column has an empty name, this can cause errors 
	 * in functions like Forward Engineering or CompareDM.
	 */	

	public void testDenyEmptyColumnName(){
		boolean gotException = false;
		panel.getColName().setText("");
		try{
			panel.applyChanges();			
		} catch (Exception e){
			//Proper behaviour is to get here
			gotException = true;			
			System.out.println("Caught expected error");
		}
		assertTrue ("Having an empty column name should throw exception!",
				gotException);
	}
		

}
