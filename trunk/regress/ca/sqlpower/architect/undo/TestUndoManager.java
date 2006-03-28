package regress.ca.sqlpower.architect.undo;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.sql.Types;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.architect.undo.UndoManager;

public class TestUndoManager extends TestCase {

	/**
	 * Toy change listener that just counts how many changes it's seen.
	 */
	private static class CL implements ChangeListener {
		int changeCount;
		public void stateChanged(ChangeEvent e) {
			changeCount++;
		}
		public int getChangeCount() {
			return changeCount;
		}
	}

	UndoManager undoManager;
	PlayPen pp;
	SQLTable fkTable;
	SQLTable pkTable;
	protected void setUp() throws Exception {
		super.setUp();
		
		SQLDatabase db = new SQLDatabase();
		pp = new PlayPen(db);
		fkTable = new SQLTable(db,true);
		TablePane tp = new TablePane(fkTable,pp);
		pp.addTablePane(tp,new Point(1,1));
		 pkTable = new SQLTable(db,true);
		TablePane tp2 = new TablePane(pkTable,pp);
		pp.addTablePane(tp2,new Point(1,1));
		undoManager = new UndoManager(pp);
		pkTable.addColumn(new SQLColumn());
		pkTable.addColumn(new SQLColumn());
		pkTable.getColumn(0).setPrimaryKeySeq(1);
		pkTable.getColumn(0).setName("pk1");
		pkTable.getColumn(0).setType(Types.INTEGER);
		pkTable.getColumn(1).setPrimaryKeySeq(1);
		pkTable.getColumn(1).setName("pk2");
		pkTable.getColumn(1).setType(Types.INTEGER);
		
	}
		
	public void testUndoManagerActionUpdates() throws ArchitectException
	{
		// TODO: add a change listener to the undo manager and make sure it fires events when it changes
		
		undoManager = new UndoManager(pp);
				UndoableEdit stubEdit = new AbstractUndoableEdit() {
			public String getPresentationName() { return "cows"; }
		};
	
		CL cl = new CL();
		undoManager.addChangeListener(cl);
		
		undoManager.addEdit(stubEdit);
		assertEquals("cows", undoManager.getPresentationName());

		assertEquals("Change listener wasn't notified", 1, cl.getChangeCount());
		assertTrue(undoManager.canUndo());
		undoManager.undo();
		assertEquals("Change listener wasn't notified", 2, cl.getChangeCount());
		assertTrue(undoManager.canRedo());
		assertFalse(undoManager.canUndo());
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
		assertTrue(undoManager.canUndo());
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

	/** Makes sure that the side effects of changing a PK column's attributes are not a separate undo step */
	public void testUndoRelationshipPkAttributeChange() throws ArchitectException {
		CreateRelationshipAction.doCreateRelationship(pkTable, fkTable, pp, false);
		SQLColumn pk1 = pkTable.getColumnByName("pk1");
		assertEquals("pk1 was already the new type.. makes testing silly", pk1.getType(), Types.INTEGER);
		pk1.setType(Types.BINARY);
		SQLColumn fk1 = fkTable.getColumnByName("pk1");
		assertNotNull("Pk1 was not propegated to fkTable",fk1);
		assertEquals("fkTable not updated when the pktable was updated",Types.BINARY,fk1.getType());
		undoManager.undo();
		assertEquals("fk1 didn't go back to old type", Types.INTEGER, fk1.getType());
		
		// this is the point of the test
		assertEquals("pk1 didn't go back to old type", Types.INTEGER, pk1.getType());
	}

}
