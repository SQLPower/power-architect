package regress.ca.sqlpower.architect.swingui;

import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.TableEditPanel;

public class TestTableEditPane extends TestCase {

	private SQLTable t;	
	private TableEditPanel tep;
	
	protected void setUp() throws Exception {
		super.setUp();
		t = new SQLTable(ArchitectFrame.getMainInstance().getProject().getTargetDatabase(), true);
		t.setName("Test Table");
		SQLColumn pk1 = new SQLColumn(t, "PKColumn1", Types.INTEGER, 10,0);
		
		t.addColumn(0,pk1);						
		tep = new TableEditPanel(t);
		
		pk1.setPrimaryKeySeq(1);		
	}
	
	public void testChangeName(){			
		tep.setNameText("New Name");
		tep.applyChanges();
		assertEquals ("New Name", t.getName());		
	}
	
	
	/*
	 * This test case is making sure that we let the user
	 * know that they cannot give a table an empty name.
	 * If a table has an empty name, this can cause errors 
	 * in functions like Forward Engineering or CompareDM.
	 */	
	public void testDeniesEmptyName(){			
		boolean gotException = false;
		tep.setNameText("");
		try{
			//This should throw exception so we could warn
			//users empty names are not allowed
			tep.applyChanges();			
		} catch (Exception e){
			//should reach here!
			System.out.println ("Caught expected error");
			gotException = true;
		}
		assertTrue("Having a empty table name should throw exception!",
				gotException);		
	}
	
}
