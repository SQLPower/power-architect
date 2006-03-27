package regress.ca.sqlpower.architect.undo;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.architect.undo.SQLObjectInsertChildren;
import ca.sqlpower.architect.undo.SQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.undo.UndoManager;

public class TestUndoManager extends TestCase {

	Action undo;
	Action redo;
	UndoManager undoManager;
	PlayPen pp;
	SQLTable fkTable;
	SQLTable pkTable;
	SQLObjectUndoableEventAdapter eAdapter;
	protected void setUp() throws Exception {
		super.setUp();
		undoManager = new UndoManager();
		eAdapter = new SQLObjectUndoableEventAdapter(undoManager);
		SQLDatabase db = new SQLDatabase();
		db.addUndoEventListener(eAdapter);
		pp = new PlayPen();
		fkTable = new SQLTable(db,true);
		TablePane tp = new TablePane(fkTable,pp);
		tp.addPlayPenComponentListener(eAdapter);
		pp.addTablePane(tp,new Point(1,1));
		 pkTable = new SQLTable(db,true);
		TablePane tp2 = new TablePane(pkTable,pp);
		tp2.addPlayPenComponentListener(eAdapter);
		pp.addTablePane(tp2,new Point(1,1));
		
		pkTable.addColumn(new SQLColumn());
		pkTable.addColumn(new SQLColumn());
		pkTable.getColumn(0).setPrimaryKeySeq(1);
		pkTable.getColumn(0).setName("pk1");
		pkTable.getColumn(1).setPrimaryKeySeq(1);
		pkTable.getColumn(1).setName("pk2");
		undo = new AbstractAction(){	
			public void actionPerformed(ActionEvent e){

			}
			
		};
		redo = new AbstractAction(){	
			public void actionPerformed(ActionEvent e){

			}
		};
		
		
		
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
	
	public void testUndoCreateRelationship() throws ArchitectException {
		assertEquals("Oops started out with relationships", 0, pkTable.getExportedKeys().size());
		assertEquals("Oops started out with relationships", 0, fkTable.getImportedKeys().size());
		CreateRelationshipAction.doCreateRelationship(pkTable, fkTable, pp, false);
		assertEquals("Wrong number of relationships created", 1, pp.getRelationships().size());
		assertEquals("Did the relationship create the columns in the fkTable", 2, fkTable.getColumns().size());
		List<SQLColumn> columns = fkTable.getColumns();
		assertNull("Is the first column a key column?", columns.get(0).getPrimaryKeySeq());
		assertNull("Is the second column a key column?", columns.get(1).getPrimaryKeySeq());
		assertEquals("Is the first column pk1?", "pk1", columns.get(0).getName());
		assertEquals("Is the second column pk2?", "pk2", columns.get(1).getName());
		assertTrue("Not registering create action with the undo manager", undoManager.canUndo());
		undoManager.undo();
		// If the Undo is working these columns should be gone
		columns = fkTable.getColumns();
		assertEquals("Relationship still attached to parent", 0, pkTable.getExportedKeys().size());
		assertEquals("Relationship still attached to child", 0, fkTable.getImportedKeys().size());
		assertNull("Orphaned imported key", fkTable.getColumnByName("pk1"));
		assertNull("Orphaned imported key", fkTable.getColumnByName("pk2"));
		assertNull("Missing exported key", pkTable.getColumnByName("pk1"));
		assertNull("Missing exported key", pkTable.getColumnByName("pk2"));
		
	}
}
