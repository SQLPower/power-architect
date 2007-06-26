package ca.sqlpower.architect.undo;

import java.awt.Point;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.StubSQLObject;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.architect.undo.UndoManager.SQLObjectUndoableEventAdapter;

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

    /**
     * Helps test undo manager by logging all calls to setFoo() in the
     * history list.
     */
    public class UndoTester extends StubSQLObject {
        public List<Integer> history = new ArrayList<Integer>();
        
        public void setFoo(Integer v) { history.add(v); }

    }

	UndoManager undoManager;
	PlayPen pp;
	SQLTable fkTable;
	SQLTable pkTable;
	TablePane tp2;
	
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("-----------------Start setup for "+getName()+"----------------");
		SQLDatabase db = new SQLDatabase();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		pp = new PlayPen(session, db);
		fkTable = new SQLTable(db,true);
		fkTable.setName("child");
		TablePane tp = new TablePane(fkTable,pp);
		pp.addTablePane(tp,new Point(1,1));
		pkTable = new SQLTable(db,true);
		pkTable.setName("parent");
		tp2 = new TablePane(pkTable,pp);
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
		db.addChild(pkTable);
		db.addChild(fkTable);
		System.out.println("-----------------End setup for "+getName()+"----------------");
		
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
	
	public void testAllowCompoundEdit()
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
	
	public void testNestedCompoundEdits() {
		pkTable.setName("old");
		fkTable.setName("old");
		pkTable.setRemarks("old");
		fkTable.setRemarks("old");
		
		undoManager.getEventAdapter().compoundEditStart(
				new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_START,"Starting compoundedit"));
		pkTable.setName("one");
		undoManager.getEventAdapter().compoundEditStart(
				new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_START,"Starting nested compoundedit"));
		fkTable.setName("two");
		undoManager.getEventAdapter().compoundEditEnd(
				new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_END,"Ending nested compoundedit"));
		pkTable.setRemarks("three");
		undoManager.getEventAdapter().compoundEditEnd(
				new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_END,"Ending compoundedit"));
		fkTable.setRemarks("four");
		
		assertEquals("one", pkTable.getName());
		assertEquals("two", fkTable.getName());
		assertEquals("three", pkTable.getRemarks());
		assertEquals("four", fkTable.getRemarks());
		
		undoManager.undo();
		
		assertEquals("one", pkTable.getName());
		assertEquals("two", fkTable.getName());
		assertEquals("three", pkTable.getRemarks());
		assertEquals("old", fkTable.getRemarks());
		
		undoManager.undo();

		assertEquals("old", pkTable.getName());
		assertEquals("old", fkTable.getName());
		assertEquals("old", pkTable.getRemarks());
		assertEquals("old", fkTable.getRemarks());

	}
    
    /**
     * Makes sure compound edits added through the sql object event adapter
     * are undone in order of most recent to least recent.
     */
    public void testCompoundEditsUndoInCorrectOrder() {
        UndoTester myTester = new UndoTester();
        SQLObjectUndoableEventAdapter adapter = undoManager.getEventAdapter();
        myTester.addUndoEventListener(adapter);
        myTester.startCompoundEdit("Test Compound undo");
        adapter.dbObjectChanged(
                new SQLObjectEvent(
                        myTester, "foo", 0, 1));
        adapter.dbObjectChanged(
                new SQLObjectEvent(
                        myTester, "foo", 1, 2));
        adapter.dbObjectChanged(
                new SQLObjectEvent(
                        myTester, "foo", 2, 3));
        myTester.endCompoundEdit("Test Compound undo");
        
        undoManager.undo();

        // Ensure the compound undo happened last..first
        assertEquals(Integer.valueOf(2), myTester.history.get(0));
        assertEquals(Integer.valueOf(1), myTester.history.get(1));
        assertEquals(Integer.valueOf(0), myTester.history.get(2));
    }
	
	public void testUndoCreateRelationship() throws ArchitectException {
		assertEquals("Oops started out with relationships", 0, pkTable.getExportedKeys().size());
		assertEquals("Oops started out with relationships", 0, fkTable.getImportedKeys().size());
		CreateRelationshipAction.doCreateRelationship(pkTable, fkTable, pp, false);
		assertEquals("Wrong number of relationships created", 1, pp.getRelationships().size());
		assertEquals("Did the relationship create the columns in the fkTable", 2, fkTable.getColumns().size());
		assertNull("First column should not be in PK", fkTable.getColumns().get(0).getPrimaryKeySeq());
		assertNull("Second column should not be in PK", fkTable.getColumns().get(1).getPrimaryKeySeq());
		assertEquals("first column should be called 'pk1'", "pk1", fkTable.getColumns().get(0).getName());
		assertEquals("second column should be called 'pk2'", "pk2", fkTable.getColumns().get(1).getName());
		
		assertTrue("Not registering create action with the undo manager", undoManager.canUndo());
		System.out.println(undoManager.toString());
		System.out.println("==UNDOING==");
		undoManager.undo();
		
		assertEquals("Relationship still attached to parent", 0, pkTable.getExportedKeys().size());

		// the following tests depend on FKColumnManager behaviour, not UndoManager
		assertEquals("Relationship still attached to child", 0, fkTable.getImportedKeys().size());
		assertNull("Orphaned imported key", fkTable.getColumnByName("pk1"));
		assertNull("Orphaned imported key", fkTable.getColumnByName("pk2"));
		assertNotNull("Missing exported key", pkTable.getColumnByName("pk1"));
		assertNotNull("Missing exported key", pkTable.getColumnByName("pk2"));
		
	}

	public void testRedoCreateRelationship() throws ArchitectException {
		testUndoCreateRelationship();
		System.out.println("==REDOING==");
		undoManager.redo();
		
		assertEquals("Wrong number of relationships created", 1, pp.getRelationships().size());
		assertEquals("key didn't get re-added to pktable", 1, pkTable.getExportedKeys().size());
		assertEquals("key didn't get re-added to fktable", 1, fkTable.getImportedKeys().size());
		
		List<SQLColumn> columns = fkTable.getColumns();
		assertEquals("Wrong number of columns in the fkTable", 2, columns.size());
		
		assertEquals("Is the first column pk1?", "pk1", columns.get(0).getName());
		assertNull("Is the first column a key column?", columns.get(0).getPrimaryKeySeq());
		assertEquals("redo left incorrect reference count on pk1", 1, columns.get(0).getReferenceCount());
		
		assertEquals("Is the second column pk2?", "pk2", columns.get(1).getName());
		assertNull("Is the second column a key column?", columns.get(1).getPrimaryKeySeq());
		assertEquals("redo left incorrect reference count on pk2", 1, columns.get(1).getReferenceCount());
	}

	public void testUndoRedoCreateRelationship() throws ArchitectException {
		testRedoCreateRelationship();
		System.out.println("==UNDOING the redo==");
		undoManager.undo();
		
		assertEquals("Relationship still attached to parent", 0, pkTable.getExportedKeys().size());

		// the following tests depend on FKColumnManager behaviour, not UndoManager
		assertEquals("Relationship still attached to child", 0, fkTable.getImportedKeys().size());
		assertNull("Orphaned imported key", fkTable.getColumnByName("pk1"));
		assertNull("Orphaned imported key", fkTable.getColumnByName("pk2"));
		assertNotNull("Missing exported key", pkTable.getColumnByName("pk1"));
		assertNotNull("Missing exported key", pkTable.getColumnByName("pk2"));
	}
	
	/** Makes sure that the side effects of changing a PK column's attributes are not a separate undo step */
	public void testUndoRelationshipPkAttributeChange() throws ArchitectException {
		CreateRelationshipAction.doCreateRelationship(pkTable, fkTable, pp, false);
		SQLColumn pk1 = pkTable.getColumnByName("pk1");
		assertEquals("pk1 was already the new type.. makes testing silly", pk1.getType(), Types.INTEGER);
		SQLColumn fk1 = fkTable.getColumnByName("pk1");
		assertNotNull("fk column not in fkTable", fk1);
        assertEquals("pk and fk must start out with same datatype", pk1.getType(), fk1.getType());
		pk1.setType(Types.BINARY);
		assertEquals("fkTable not updated when the pktable was updated",Types.BINARY,fk1.getType());
		undoManager.undo();
		assertEquals("fk1 didn't go back to old type", Types.INTEGER, fk1.getType());
		
		// this is the point of the test
		assertEquals("pk1 didn't go back to old type", Types.INTEGER, pk1.getType());
	}
	
	public void testUndoMovement() {
		Point oldLoc = tp2.getLocation();
		pp.startCompoundEdit("start move");
		tp2.setLocation(123, 456);
		tp2.setLocation(333, 444);
		tp2.setLocation(333, 344);
		pp.endCompoundEdit("end move");
		assertEquals(new Point(333,344), tp2.getLocation());
		undoManager.undo();
		assertEquals(oldLoc, tp2.getLocation());
	}

}
