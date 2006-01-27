package regress.ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.UndoManager;
import ca.sqlpower.architect.undo.SQLObjectInsertChildren;

public class TestUndoManager extends TestCase {

	Action undo;
	Action redo;
	UndoManager undoManager;
	
	@Override
	protected void setUp() throws Exception {
		undo = new AbstractAction(){	
			public void actionPerformed(ActionEvent e){

			}
			
		};
		redo = new AbstractAction(){	
			public void actionPerformed(ActionEvent e){

			}
		};
		undoManager = new UndoManager();
	}
	
	public void testUndoManagerActionUpdates()
	{
		undoManager = new UndoManager();
				UndoableEdit stubEdit = new AbstractUndoableEdit() {
			public String getPresentationName() { return "cows"; }
		};
		
		undoManager.addEdit(stubEdit);
		assertEquals("cows", undoManager.getPresentationName());

		undo = undoManager.getUndo();
		assertNotNull(undo);
		assertEquals("Undo cows",undo.getValue(Action.SHORT_DESCRIPTION));
		assertTrue(undo.isEnabled());
		redo = undoManager.getRedo();
		assertNotNull(redo);
		assertFalse(redo.isEnabled());
		undoManager.undo();
		assertEquals("Redo cows",redo.getValue(Action.SHORT_DESCRIPTION));
		assertTrue(redo.isEnabled());
		assertFalse(undo.isEnabled());

	}
	
	public void testCompoundEdits()
	{
		UndoableEdit stubEdit1 = new AbstractUndoableEdit(); 
		UndoableEdit stubEdit2 = new AbstractUndoableEdit(); 
		UndoableEdit stubEdit3 = new AbstractUndoableEdit(); 
		CompoundEdit ce = new CompoundEdit();
		ce.addEdit(stubEdit1);
		ce.addEdit(stubEdit2);
		ce.addEdit(stubEdit3);
		ce.end();
		undoManager.addEdit(ce);
		undo = undoManager.getUndo();
		assertTrue(undoManager.canUndo());
		assertNotNull(undo);
		assertTrue(undo.isEnabled());
		
	}
}
