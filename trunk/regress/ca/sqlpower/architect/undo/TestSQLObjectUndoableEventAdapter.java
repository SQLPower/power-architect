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
package ca.sqlpower.architect.undo;

import java.awt.Point;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class TestSQLObjectUndoableEventAdapter extends TestCase {
	
	
	private final class StateChangeTestListner implements ChangeListener {
        public int stateChanges;

        public void stateChanged(ChangeEvent e) {
            stateChanges++;    
        }
    }

    ArchitectSwingSession session;
    
    @Override
	protected void setUp() throws Exception {
		super.setUp();
		TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
		session = context.createSession();
	}
	/*
	private void testStateSwap() {
		int index[] = {0};
		SQLObject children[] = {new SQLTable()};
		assertEquals("We started in the wrong state",UndoState.REGULAR ,eAdapter.getState());
		eAdapter.dbChildrenInserted(new SQLObjectEvent(new SQLTable(),index,children));			
	}*/
	
	public void testMove() throws ArchitectException, IOException {
		PlayPen pp = new PlayPen(session);
		SQLTable table = new SQLTable(session.getTargetDatabase(),true);
		TablePane tp = new TablePane(table,pp);
		pp.addTablePane(tp, new Point());
		UndoManager undoManager = new UndoManager(pp);
		if (pp != null) {
		    pp.getPlayPenContentPane().addPropertyChangeListener("location", undoManager.getEventAdapter());
        }
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
		PlayPen pp = new PlayPen(session);
		SQLDatabase db = session.getTargetDatabase();
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
		if (pp != null) {
            pp.getPlayPenContentPane().addPropertyChangeListener("location", undoManager.getEventAdapter());
        }
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
    
    public void testCompoundEditEvent() throws ArchitectException{
        PlayPen pp = new PlayPen(session);
        UndoManager manager = new UndoManager(pp);
        if (pp != null) {
            pp.getPlayPenContentPane().addPropertyChangeListener("location", manager.getEventAdapter());
            pp.getPlayPenContentPane().addPropertyChangeListener("connectionPoints", manager.getEventAdapter());
            pp.getPlayPenContentPane().addPropertyChangeListener("backgroundColor", manager.getEventAdapter());
            pp.getPlayPenContentPane().addPropertyChangeListener("foregroundColor", manager.getEventAdapter());
            pp.getPlayPenContentPane().addPropertyChangeListener("dashed", manager.getEventAdapter());
            pp.getPlayPenContentPane().addPropertyChangeListener("rounded", manager.getEventAdapter());
        }
        StateChangeTestListner listner = new StateChangeTestListner();
        manager.addChangeListener(listner);
        UndoManager.SQLObjectUndoableEventAdapter adapter = manager.getEventAdapter();
        assertTrue(adapter.canUndoOrRedo());
        
        adapter.compoundEditStart(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START, "Test"));
        assertEquals(" Improper number of state changes after first compound edit",1,listner.stateChanges);
        assertFalse(adapter.canUndoOrRedo());
        adapter.compoundEditEnd(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END, "Test"));
        assertEquals(" Improper number of state changes after first compound edit",2,listner.stateChanges);
        assertTrue(adapter.canUndoOrRedo());
    }
}
