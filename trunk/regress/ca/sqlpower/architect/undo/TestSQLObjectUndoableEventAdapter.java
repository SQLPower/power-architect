package regress.ca.sqlpower.architect.undo;

import java.awt.Point;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.undo.UndoManager;

public class TestSQLObjectUndoableEventAdapter extends TestCase {
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	/*
	private void testStateSwap() {
		int index[] = {0};
		SQLObject children[] = {new SQLTable()};
		assertEquals("We started in the wrong state",UndoState.REGULAR ,eAdapter.getState());
		eAdapter.dbChildrenInserted(new SQLObjectEvent(new SQLTable(),index,children));			
	}*/
	
	public void testMove() throws ArchitectException
	{
		PlayPen pp = new PlayPen(new SQLDatabase());
		SQLTable table = new SQLTable(pp.getDatabase(),true);
		TablePane tp = new TablePane(table,pp);
		pp.addTablePane(tp, new Point());
		UndoManager undoManager = new UndoManager(pp);
		Point location;
		Point newLocation;
		location = tp.getLocation();
		assertTrue("Moved to the right location", location.equals(tp.getLocation() ));
		newLocation = location.getLocation();
		newLocation.x++;
		newLocation.y++;
		tp.setLocation(newLocation);
		assertTrue("Moved to the right location", newLocation.equals(tp.getLocation() ));
		undoManager.undo();
		assertTrue("Moved to the right location", location.equals(tp.getLocation() ));
		undoManager.redo();
		assertTrue("Moved to the right location", newLocation.equals(tp.getLocation() ));
		
		
	}
	
	public void testMultiMove() throws ArchitectException 
	{
		SQLDatabase db = new SQLDatabase();
		PlayPen pp = new PlayPen(db);
		SQLTable table = new SQLTable(db,true);
		TablePane tp = new TablePane(table,pp);
		SQLTable table2 = new SQLTable(db,true);
		TablePane tp2 = new TablePane(table2,pp);
		Point location;
		pp.addTablePane(tp,new Point());
		Point newLocation;
		location = tp.getLocation();
		Point location2;
		pp.addTablePane(tp2,new Point());
		Point newLocation2;
		location2 = tp2.getLocation();
		UndoManager undoManager = new UndoManager(pp);
		assertTrue("Moved to the right location", location2.equals(tp2.getLocation() ));
		assertTrue("Moved to the right location", location.equals(tp.getLocation() ));
		newLocation = location.getLocation();
		newLocation.x++;
		newLocation.y++;
		newLocation2 = location2.getLocation();
		newLocation2.x+=2;
		newLocation2.y+=2;
		pp.startCompoundEdit("Starting move");
		tp.setLocation(newLocation);
		tp2.setLocation(newLocation2);
		pp.endCompoundEdit("Ending move");
		assertTrue("Moved 1 to the right location", newLocation.equals(tp.getLocation() ));
		assertTrue("Moved 2 to the right location", newLocation2.equals(tp2.getLocation() ));
		undoManager.undo();
		assertTrue("Moved 1 to the right location", location.equals(tp.getLocation() ));
		assertTrue("Moved 2 to the right location", location2.equals(tp2.getLocation() ));
		undoManager.redo();
		assertTrue("Moved 1 to the right location", newLocation.equals(tp.getLocation() ));
		assertTrue("Moved 2 to the right location", newLocation2.equals(tp2.getLocation() ));
		
	}
	
}
