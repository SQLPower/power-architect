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
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import junit.framework.TestCase;
import ca.sqlpower.architect.StubSQLObject;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.event.SelectionListener;


public class ArchitectPropertyChangeUndoableEditTest extends TestCase {
    
	private UndoManager undoManager;
	
	private TestSQLObject testObject;
	
	protected void setUp() throws Exception {
		super.setUp();
		testObject = new TestSQLObject();
		undoManager = new UndoManager(testObject);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
	}

	public void testUndoAdded()
	{
		assertFalse(undoManager.canUndo());
		testObject.setFoo(1);
		testObject.setBar("New string");
		assertEquals("Wrong number of undoable edits added to undo manager",2,undoManager.getUndoableEditCount());
		assertEquals("Unexpected redoable",0,undoManager.getRedoableEditCount());
	}
	
	
	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testUndoIntProperty() {
		testObject.setFoo(1);
		assertEquals("foo setter didn't work!", 1, testObject.getFoo());
		undoManager.undo();
		assertEquals("undo didn't work!", 0, testObject.getFoo());
		assertEquals("wrong redo size",1,undoManager.getRedoableEditCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testUndoStringPropertyToNull() {
		final String newBarValue = "cows often say moo";
		testObject.setBar(newBarValue);
		assertEquals("bar setter didn't work!", newBarValue, testObject.getBar());
		undoManager.undo();
		assertEquals("undo didn't work!", null, testObject.getBar());
		assertEquals("wrong redo size",1,undoManager.getRedoableEditCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testUndoStringPropertyToNonNull() {
		final String newBarValue1 = "cows often say moo";
		final String newBarValue2 = "chickens don't often say moo";
		testObject.setBar(newBarValue1);
		testObject.setBar(newBarValue2);
		assertEquals("bar setter didn't work!", newBarValue2, testObject.getBar());
		undoManager.undo();
		assertEquals("undo didn't work!", newBarValue1, testObject.getBar());
		assertEquals("wrong redo size",1,undoManager.getRedoableEditCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testRedoIntProperty() {
		testObject.setFoo(1);
		assertEquals("foo setter didn't work!", 1, testObject.getFoo());
		undoManager.undo();
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("redo didn't work!", 1, testObject.getFoo());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testRedoStringPropertyToNull() {
		final String newBarValue = "cows often say moo";
		testObject.setBar(newBarValue);
		assertEquals("bar setter didn't work!", newBarValue, testObject.getBar());
		undoManager.undo();
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("redo bar didn't work!", newBarValue, testObject.getBar());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.undo()'
	 */
	public void testRedoStringPropertyToNonNull() {
		final String newBarValue1 = "cows often say moo";
		final String newBarValue2 = "chickens don't often say moo";
		testObject.setBar(newBarValue1);
		testObject.setBar(newBarValue2);
		assertEquals("bar setter didn't work!", newBarValue2, testObject.getBar());
		undoManager.undo();
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("redo bar change didn't work!", newBarValue2, testObject.getBar());
	}

	public void testMultipleUndoRedo()
	{
		testObject.setFoo(1);
		testObject.setFoo(2);
		testObject.setFoo(3);
		testObject.setFoo(4);
		testObject.setFoo(5);
		assertEquals("wrong number of edits",5,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",0,undoManager.getRedoableEditCount());
		undoManager.undo();
		undoManager.undo();
		assertEquals("wrong number of edits",3,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",2,undoManager.getRedoableEditCount());
		assertEquals("Foo has wrong value",3,testObject.getFoo());
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("wrong number of edits",4,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",1,undoManager.getRedoableEditCount());
		assertEquals("Foo has wrong value",4,testObject.getFoo());
		assertTrue(undoManager.canRedo());
		undoManager.redo();
		assertEquals("wrong number of edits",5,undoManager.getUndoableEditCount());
		assertEquals("wrong number of edits",0,undoManager.getRedoableEditCount());
		assertEquals("Foo has wrong value",5,testObject.getFoo());
		
		
	}
	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.ArchitectPropertyChangeUndoableEdit(SQLObjectEvent)'
	 */
	public void testArchitectPropertyChangeUndoableEdit() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.undo.ArchitectPropertyChangeUndoableEdit.getPresentationName()'
	 */
	public void testGetPresentationName() {

	}
	
	/**
	 * Tests PropertyChangeEdit functionalities
	 */
	public void testPropertyChangeEdit() {
	    TestPlayPenComp comp = new TestPlayPenComp(null);
	    PropertyChangeEvent e = new PropertyChangeEvent(comp, "location", null, null);
	    PropertyChangeEdit edit = new PropertyChangeEdit(e);
	    assertEquals("property change edit", edit.getPresentationName());
	    Point oldLocation = new Point(1, 2) {
	        @Override
	        public String toString() {
	            return "(" + this.x + ", " + this.y + ")"; 
	        }
	    };
	    Point newLocation = new Point(3, 4) {
	        @Override
            public String toString() {
	            return "(" + this.x + ", " + this.y + ")";
            }
        };
        comp.setLocation(oldLocation);
        comp.setLocation(newLocation);
        e = new PropertyChangeEvent(comp, "location", oldLocation, newLocation);
        edit = new PropertyChangeEdit(e);
        assertTrue(edit.canUndo());
        edit.undo();
        assertEquals(oldLocation, comp.getLocation());
        assertFalse(edit.canUndo());
        assertTrue(edit.canRedo());
        edit.redo();
        assertEquals(newLocation, comp.getLocation());
        assertFalse(edit.canRedo());
	}
	
	/**
	 * Tests undo/redo of playpen component movements
	 */
	public void testMovementPropertyChange() {
	    TestPlayPenComp comp = new TestPlayPenComp(null);
	    comp.addPropertyChangeListener("location", undoManager.getEventAdapter());
	    Point oldLocation = comp.getLocation();
	    Point newLocation = new Point(1, 2);
	    comp.setLocation(newLocation);
	    comp.firePropertyChange(new PropertyChangeEvent(comp, "location", oldLocation, newLocation));
	    
	    assertTrue(undoManager.canUndo());
	    undoManager.undo();
	    assertEquals(oldLocation, comp.getLocation());
	    assertTrue(undoManager.canRedo());
	    undoManager.redo();
	    assertEquals(newLocation, comp.getLocation());
	}
	
	/**
	 * Tests undo/redo of connection points changes
	 */
	public void testConnectionPointPropertyChange() {
        TestPlayPenComp comp = new TestPlayPenComp(null);
        comp.addPropertyChangeListener("connectionPoints", undoManager.getEventAdapter());
        Point[] oldConnectionPoints = comp.getConnectionPoints();
        Point[] newConnectionPoints = {new Point(oldConnectionPoints[0].x + 10, oldConnectionPoints[0].y + 20), 
                new Point(oldConnectionPoints[1].x + 30, oldConnectionPoints[1].y + 40)};
        comp.setConnectionPoints(newConnectionPoints);
        comp.firePropertyChange(new PropertyChangeEvent(comp, "connectionPoints", oldConnectionPoints, newConnectionPoints));
        
        assertTrue(undoManager.canUndo());
        undoManager.undo();
        assertEquals(oldConnectionPoints, comp.getConnectionPoints());
        assertTrue(undoManager.canRedo());
        undoManager.redo();
        assertEquals(newConnectionPoints, comp.getConnectionPoints());
    }
	

	public static class TestSQLObject extends StubSQLObject {
		
		private int foo;
		private String bar;
		
		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			String oldBar = this.bar;
			this.bar = bar;
			fireDbObjectChanged("bar",oldBar,bar);
		}

		public int getFoo() {
			return foo;
		}

		public void setFoo(int foo) {
			int oldFoo = this.foo;
			this.foo = foo;
			fireDbObjectChanged("foo",oldFoo,foo);
		}
	}
	
	/**
	 * An instance of this class will have basic property change functionalities
	 * for testing.
	 *
	 */
	public static class TestPlayPenComp extends PlayPenComponent {
	    
	    protected TestPlayPenComp(PlayPenContentPane parent) {
            super(parent);
            this.setBounds(0, 0, 100, 100);
        }

	    private Point[] connectionPoints = {new Point(10,10), new Point(100,100)};

        @Override
        public Object getModel() {
            return null;
        }

        public void addSelectionListener(SelectionListener l) {
            
        }

        public boolean isSelected() {
            return false;
        }

        public void removeSelectionListener(SelectionListener l) {
            
        }

        public void setSelected(boolean v, int multiSelectionType) {
            
        }
        
        public void setConnectionPoints(Point[] connectionPoints) {
            this.connectionPoints = connectionPoints;
        }
        
        public Point[] getConnectionPoints() {
            return connectionPoints;
        }
        
        @Override
        public String toString() {
            return "testComp";
        }

        @Override
        public void handleMouseEvent(MouseEvent evt) {
        }
	    
	}
}
