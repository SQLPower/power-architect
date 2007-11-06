/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        StateChangeTestListner listner = new StateChangeTestListner();
        manager.addChangeListener(listner);
        UndoManager.SQLObjectUndoableEventAdapter adapter = manager.getEventAdapter();
        assertTrue(adapter.canUndoOrRedo());
        
        adapter.compoundEditStart(new UndoCompoundEvent(new SQLTable(),EventTypes.COMPOUND_EDIT_START, "Test"));
        assertEquals(" Improper number of state changes after first compound edit",1,listner.stateChanges);
        assertFalse(adapter.canUndoOrRedo());
        adapter.compoundEditEnd(new UndoCompoundEvent(new SQLTable(),EventTypes.COMPOUND_EDIT_END, "Test"));
        assertEquals(" Improper number of state changes after first compound edit",2,listner.stateChanges);
        assertTrue(adapter.canUndoOrRedo());
    }
}
