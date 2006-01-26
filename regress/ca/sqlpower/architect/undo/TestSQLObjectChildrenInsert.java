package regress.ca.sqlpower.architect.undo;

import javax.swing.undo.UndoableEditSupport;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.UndoManager;

public class TestSQLObjectChildrenInsert extends TestCase {
	
	UndoManager undoManager;
	
	SQLObjectUndoableEventAdapter undoEventListener;
	
	@Override
	protected void setUp() throws Exception {
		undoManager = new UndoManager();
		undoEventListener = new SQLObjectUndoableEventAdapter(undoManager);
		super.setUp();
	}
	
	
	public void testFolderInsert(){
		
	}
	
	public void testDatabaseInsert()throws ArchitectException {
		
		// setup a playpen like database
		SQLDatabase db = new SQLDatabase();
		db.setIgnoreReset(true);
		SQLTable table1 = new SQLTable(db,"table1","remark1","TABLE",true);
		SQLTable table2 = new SQLTable(db,"table2","remark2","TABLE",true);
		SQLTable table3 = new SQLTable(db,"table3","remark3","TABLE",true);
		SQLTable table4 = new SQLTable(db,"table4","remark4","TABLE",true);
		db.addChild(table1);
		db.addChild(table2);
		db.addChild(table3);
		db.addChild(table4);
		db.addSQLObjectListener(undoEventListener);
		db.removeChild(2);
		undoManager.undo();
		assertEquals("There should be 4 children",4,db.getChildCount());
		assertEquals("The first table is in the wrong position",table1,db.getChild(0));
		assertEquals("The Second table is in the wrong position",table2,db.getChild(1));
		assertEquals("The Third table is in the wrong position",table3,db.getChild(2));
		assertEquals("The Fourth table is in the wrong position",table4,db.getChild(3));
		

		undoManager.redo();
		assertEquals("There should be 3 children",3,db.getChildCount());
		assertEquals("The first table is in the wrong position",table1,db.getChild(0));
		assertEquals("The Second table is in the wrong position",table2,db.getChild(1));
		assertEquals("The Third table is in the wrong position",table4,db.getChild(2));
		
	}

}
