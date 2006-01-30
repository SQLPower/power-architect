package regress.ca.sqlpower.architect;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.undo.SQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.undo.UndoManager;
import ca.sqlpower.architect.undo.SQLObjectUndoableEventAdapter.UndoState;

public class TestSQLObjectUndoableEventAdapter extends TestCase {
	SQLObjectUndoableEventAdapter eAdapter;
	UndoManager undoManager;
	
	@Override
	protected void setUp() throws Exception {
		undoManager = new UndoManager();
		eAdapter = new SQLObjectUndoableEventAdapter(undoManager);
		super.setUp();
	}
	
	private void testStateSwap() {
		int index[] = {0};
		SQLObject children[] = {new SQLTable()};
		assertEquals("We started in the wrong state",UndoState.REGULAR ,eAdapter.getState());
		eAdapter.dbChildrenInserted(new SQLObjectEvent(new SQLTable(),index,children));
		
		
	}
	
}
