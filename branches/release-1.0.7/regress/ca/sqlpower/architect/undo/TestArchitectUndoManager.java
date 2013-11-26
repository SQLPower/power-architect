/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.BasicRelationshipUI;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.undo.PropertyChangeEdit;
import ca.sqlpower.object.undo.ArchitectPropertyChangeUndoableEditTest.TestSQLObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.TransactionEvent;

public class TestArchitectUndoManager extends TestCase {
    
    /**
     * An instance of this class will have basic property change functionalities
     * for testing.
     *
     */
    public static class TestPlayPenComp extends PlayPenComponent {
        
        protected TestPlayPenComp(PlayPenContentPane parent) {
            super("Test PlayPenComponent", parent);
            this.setBounds(0, 0, 100, 100);
        }

        private Point[] connectionPoints = {new Point(10,10), new Point(100,100)};

        @Override
        public Object getModel() {
            return null;
        }

        @Override
        public String getModelName() {
            // TODO Auto-generated method stub
            return null;
        }
        
        public boolean isSelected() {
            return false;
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

        public List<? extends SPObject> getDependencies() {
            // TODO Auto-generated method stub
            return null;
        }

        public void removeDependency(SPObject dependency) {
            // TODO Auto-generated method stub
            
        }
        
        /**
         * The firePropertyChange method needs to be public for the tests
         */
        public PropertyChangeEvent firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            return super.firePropertyChange(propertyName, oldValue, newValue);
        }
        
    }
    
    private final class StateChangeTestListner implements ChangeListener {
        public int stateChanges;

        public void stateChanged(ChangeEvent e) {
            stateChanges++;    
        }
    }
    
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
    
    ArchitectUndoManager undoManager;
    PlayPen pp;
    SQLTable fkTable;
    SQLTable pkTable;
    TablePane tp2;
    private ArchitectSwingSession session;
    
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("-----------------Start setup for "+getName()+"----------------");
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        session = context.createSession();
        pp = new PlayPen(session);
        undoManager = new ArchitectUndoManager(pp);
        SQLDatabase db = session.getTargetDatabase();
        fkTable = new SQLTable(db,true);
        fkTable.setName("child");
        TablePane tp = new TablePane(fkTable,pp.getContentPane());
        pp.addTablePane(tp,new Point(1,1));
        pkTable = new SQLTable(db,true);
        pkTable.setName("parent");
        tp2 = new TablePane(pkTable,pp.getContentPane());
        pp.addTablePane(tp2,new Point(1,1));
        pkTable.addColumn(new SQLColumn());
        pkTable.addColumn(new SQLColumn());
        pkTable.addToPK(pkTable.getColumn(0));
        pkTable.getColumn(0).setName("pk1");
        pkTable.getColumn(0).setType(Types.INTEGER);
        pkTable.addToPK(pkTable.getColumn(1));
        pkTable.getColumn(1).setName("pk2");
        pkTable.getColumn(1).setType(Types.INTEGER);
        db.addChild(pkTable);
        db.addChild(fkTable);
        System.out.println("-----------------End setup for "+getName()+"----------------");
        
    }
    
    /**
     * Tests PropertyChangeEdit functionalities
     */
    public void testPropertyChangeEdit() {
        TestPlayPenComp comp = new TestPlayPenComp(new PlayPenContentPane(new SQLDatabase()));
        PropertyChangeEvent e = new PropertyChangeEvent(comp, "bounds", null, null);
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
        Dimension size = comp.getSize();
        e = new PropertyChangeEvent(comp, "bounds", 
                new Rectangle(oldLocation, size), 
                new Rectangle(newLocation, size));
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
    public void testMovementPropertyChange() throws Exception {
        TestSQLObject testObject = new TestSQLObject();
        ArchitectUndoManager undoManager = new ArchitectUndoManager(testObject);
        final PropertyChangeListener l = undoManager.getEventAdapter();
        TestPlayPenComp comp = new TestPlayPenComp(new PlayPenContentPane(new SQLDatabase()));
        comp.addSPListener(new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("topLeftCorner")) {
                    l.propertyChange(evt);
                }
            }
        });
        Point oldLocation = comp.getLocation();
        Point newLocation = new Point(1, 2);
        Dimension size = comp.getSize();
        comp.setLocation(newLocation);
        comp.firePropertyChange("topLeftCorner", oldLocation, newLocation);
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
    public void testConnectionPointPropertyChange() throws Exception {
        TestSQLObject testObject = new TestSQLObject();
        ArchitectUndoManager undoManager = new ArchitectUndoManager(testObject);
        final PropertyChangeListener l = undoManager.getEventAdapter();
        TestPlayPenComp comp = new TestPlayPenComp(new PlayPenContentPane(new SQLDatabase()));        
        comp.addSPListener(new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("connectionPoints")) {
                    l.propertyChange(evt);
                }
            }
        });
        Point[] oldConnectionPoints = comp.getConnectionPoints();
        Point[] newConnectionPoints = {new Point(oldConnectionPoints[0].x + 10, oldConnectionPoints[0].y + 20), 
                new Point(oldConnectionPoints[1].x + 30, oldConnectionPoints[1].y + 40)};
        comp.setConnectionPoints(newConnectionPoints);
        comp.firePropertyChange("connectionPoints", oldConnectionPoints, newConnectionPoints);
        
        assertTrue(undoManager.canUndo());
        undoManager.undo();
        assertEquals(oldConnectionPoints, comp.getConnectionPoints());
        assertTrue(undoManager.canRedo());
        undoManager.redo();
        assertEquals(newConnectionPoints, comp.getConnectionPoints());
    }
    
    public void testMove() throws SQLObjectException, IOException {
        PlayPen pp = new PlayPen(session);
        SQLTable table = new SQLTable(session.getTargetDatabase(),true);
        TablePane tp = new TablePane(table,pp.getContentPane());
        pp.addTablePane(tp, new Point());
        ArchitectUndoManager undoManager = new ArchitectUndoManager(pp);
        final PropertyChangeListener l = undoManager.getEventAdapter();
        pp.getContentPane().addComponentPropertyListener("topLeftCorner", new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                l.propertyChange(evt);
            }
        });
        Point location;
        Point newLocation;
        location = tp.getLocation();
        assertEquals("Not moved to the right location", location, tp.getLocation());
        newLocation = location.getLocation();
        newLocation.x++;
        newLocation.y++;
        tp.setLocation(newLocation);
        assertEquals("Not moved to the right location", newLocation, tp.getLocation());
        undoManager.undo();
        assertEquals("Not moved to the right location", location, tp.getLocation());
        undoManager.redo();
        assertEquals("Not moved to the right location", newLocation, tp.getLocation());
        
        
    }
    
    public void testMultiMove() throws SQLObjectException 
    {
        PlayPen pp = new PlayPen(session);
        SQLDatabase db = session.getTargetDatabase();
        SQLTable table = new SQLTable(db,true);
        TablePane tp = new TablePane(table,pp.getContentPane());
        SQLTable table2 = new SQLTable(db,true);
        TablePane tp2 = new TablePane(table2,pp.getContentPane());
        Point location;
        pp.addTablePane(tp,new Point());
        Point newLocation;
        location = tp.getLocation();
        Point location2;
        pp.addTablePane(tp2,new Point());
        Point newLocation2;
        location2 = tp2.getLocation();
        ArchitectUndoManager undoManager = new ArchitectUndoManager(pp);
        final PropertyChangeListener l = undoManager.getEventAdapter();
        pp.getContentPane().addComponentPropertyListener(new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("topLeftCorner")) {
                    l.propertyChange(evt);
                }
            }
        });
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
    
    public void testCompoundEditEvent() throws SQLObjectException{
        PlayPen pp = new PlayPen(session);
        ArchitectUndoManager manager = new ArchitectUndoManager(pp);
        
        final SPListener undoListener = manager.getEventAdapter();
        
        pp.getContentPane().addComponentPropertyListener(new AbstractSPListener() {
            private List<String> filter = Arrays.asList(new String[] {
                    "bounds",
                    "connectionPoints",
                    "backgroundColor",
                    "foregroundColor",
                    "dashed",
                    "rounded"
            });
            public void propertyChanged(PropertyChangeEvent evt) {
                if (filter.contains(evt.getPropertyName())) {
                    undoListener.propertyChanged(evt);
                }
            }
        });
        
        StateChangeTestListner listener = new StateChangeTestListner();
        manager.addChangeListener(listener);
        ArchitectUndoManager.SPObjectUndoableEventAdapter adapter = manager.getEventAdapter();
        assertTrue(adapter.canUndoOrRedo());
        
        adapter.transactionStarted(TransactionEvent.createStartTransactionEvent(null, "Test"));
        assertEquals(" Improper number of state changes after first compound edit",1,listener.stateChanges);
        assertFalse(adapter.canUndoOrRedo());
        adapter.transactionEnded(TransactionEvent.createEndTransactionEvent(null));
        assertEquals(" Improper number of state changes after first compound edit",2,listener.stateChanges);
        assertTrue(adapter.canUndoOrRedo());
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
    
    public void testUndoMultipleTablePaneMovement() {
        TablePane tp0 = new TablePane(pkTable, pp.getContentPane());
        pp.addTablePane(tp0, new Point(0, 0));
        Point oldLoc0 = tp0.getLocation();
        TablePane tp1 = new TablePane(fkTable, pp.getContentPane());
        pp.addTablePane(tp1, new Point(999, 999));
        Point oldLoc1 = tp1.getLocation();
        pp.startCompoundEdit("start move");
        tp0.setLocation(0, 2);
        tp1.setLocation(400, 500);
        tp0.setLocation(0, 6);
        tp1.setLocation(456, 578);
        tp0.setLocation(23, 45);
        tp1.setLocation(423, 5);
        pp.endCompoundEdit("end move");
        assertEquals(new Point(23, 45), tp0.getLocation());
        assertEquals(new Point(423, 5), tp1.getLocation());
        undoManager.undo();
        assertEquals(oldLoc0, tp0.getLocation());
        assertEquals(oldLoc1, tp1.getLocation());
        undoManager.redo();
        assertEquals(new Point(23, 45), tp0.getLocation());
        assertEquals(new Point(423, 5), tp1.getLocation());
    }
    
    /**
     * Basically just a class to allow access to the ability to fire events, for testing.
     *
     */
    private class TestUndoRelationship extends Relationship {
        
        public TestUndoRelationship(SQLRelationship model, PlayPenContentPane parent) throws SQLObjectException {
            super(model, parent);
        }
        
        public PropertyChangeEvent firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            return super.firePropertyChange(propertyName, oldValue, newValue);
        }
        
    }
    
    /**
     * Tests undo/redo of the movement of 1 relationship's connection points.
     */
    public void testUndoRelationshipConnectionPointMovement() throws SQLObjectException{
        TablePane tp0 = new TablePane(pkTable, pp.getContentPane());
        TablePane tp1 = new TablePane(fkTable, pp.getContentPane());
        SQLRelationship model = new SQLRelationship();
        model.setName(pkTable.getName()+"_"+fkTable.getName()+"_fk");  //$NON-NLS-1$ //$NON-NLS-2$
        model.setIdentifying(true);
        model.attachRelationship(pkTable,fkTable,true);
        TestUndoRelationship rel = new TestUndoRelationship(model, pp.getContentPane());

        pp.addTablePane(tp0, new Point(0, 0));
        pp.addTablePane(tp1, new Point(0, 200));
        pp.addRelationship(rel);
        
        BasicRelationshipUI.ImmutablePoint oldPkCon = rel.createPkConnectionPoint();
        BasicRelationshipUI.ImmutablePoint oldFkCon = rel.createFkConnectionPoint();
        
        rel.setPkConnectionPoint(new Point(oldPkCon.getX() + 20, oldPkCon.getY()));
        rel.setFkConnectionPoint(new Point(oldFkCon.getX() - 20, oldFkCon.getY()));
        BasicRelationshipUI.ImmutablePoint newPkCon = rel.createPkConnectionPoint();
        BasicRelationshipUI.ImmutablePoint newFkCon = rel.createFkConnectionPoint();
        
        undoManager.undo();
        undoManager.undo();
        assertEquals(oldFkCon.getX(), rel.createFkConnectionPoint().getX());
        assertEquals(oldFkCon.getY(), rel.createFkConnectionPoint().getY());
        undoManager.undo();
        undoManager.undo();
        assertEquals(oldPkCon.getX(), rel.createPkConnectionPoint().getX());
        assertEquals(oldPkCon.getY(), rel.createPkConnectionPoint().getY());
        undoManager.redo();
        undoManager.redo();
        undoManager.redo();
        assertEquals(newPkCon.getX(), rel.createPkConnectionPoint().getX());
        assertEquals(newPkCon.getY(), rel.createPkConnectionPoint().getY());
        assertEquals(newFkCon.getX(), rel.createFkConnectionPoint().getX());
        assertEquals(newFkCon.getY(), rel.createFkConnectionPoint().getY());
    }
    
    public void testUndoManagerActionUpdates() throws SQLObjectException
    {
        // TODO: add a change listener to the undo manager and make sure it fires events when it changes
        
        undoManager = new ArchitectUndoManager(pp);
        final PropertyChangeListener l = undoManager.getEventAdapter();
        pp.getContentPane().addComponentPropertyListener(new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("topLeftCorner") || 
                        evt.getPropertyName().equals("connectionPoints")) {
                    l.propertyChange(evt);
                }
            }
        });
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
    
    public void testUndoCreateRelationship() throws SQLObjectException {
        assertEquals("Oops started out with relationships", 0, pkTable.getExportedKeys().size());
        assertEquals("Oops started out with relationships", 0, fkTable.getImportedKeys().size());
        CreateRelationshipAction.doCreateRelationship(pkTable, fkTable, pp, false);
        assertEquals("Wrong number of relationships created", 1, pp.getContentPane().getChildren(Relationship.class).size());
        assertEquals("Did the relationship create the columns in the fkTable", 2, fkTable.getColumns().size());
        assertFalse("First column should not be in PK", fkTable.getColumns().get(0).isPrimaryKey());
        assertFalse("Second column should not be in PK", fkTable.getColumns().get(1).isPrimaryKey());
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

    public void testRedoCreateRelationship() throws SQLObjectException {
        testUndoCreateRelationship();
        System.out.println(undoManager.toString());
        System.out.println("==REDOING==");
        undoManager.redo();
        
        assertEquals("Wrong number of relationships created", 1, pp.getContentPane().getChildren(Relationship.class).size());
        assertEquals("key didn't get re-added to pktable", 1, pkTable.getExportedKeys().size());
        assertEquals("key didn't get re-added to fktable", 1, fkTable.getImportedKeys().size());
        
        List<SQLColumn> columns = fkTable.getColumns();
        assertEquals("Wrong number of columns in the fkTable", 2, columns.size());
        
        assertEquals("Is the first column pk1?", "pk1", columns.get(0).getName());
        assertFalse("Is the first column a key column?", columns.get(0).isPrimaryKey());
        assertEquals("redo left incorrect reference count on pk1", 1, columns.get(0).getReferenceCount());
        
        assertEquals("Is the second column pk2?", "pk2", columns.get(1).getName());
        assertFalse("Is the second column a key column?", columns.get(1).isPrimaryKey());
        assertEquals("redo left incorrect reference count on pk2", 1, columns.get(1).getReferenceCount());
    }

    public void testUndoRedoCreateRelationship() throws SQLObjectException {
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
    
    /**
     * Regression test for bug 1618. Adding a data source should be able to be undone.
     */
    public void testUndoAffectsSourceDBs() throws Exception {
        SQLObjectRoot rootObject = pp.getSession().getRootObject();
        int undoCount = undoManager.getUndoableEditCount();
        assertEquals(1, rootObject.getChildCount());
        SQLDatabase targetDB = (SQLDatabase)rootObject.getChild(0);
        SQLDatabase db = new SQLDatabase();
        rootObject.addChild(db);
        assertEquals(2, rootObject.getChildCount());
        
        assertTrue(undoManager.canUndo());
        assertEquals(undoCount + 1, undoManager.getUndoableEditCount());
        int redoCount = undoManager.getRedoableEditCount();
        undoManager.undo();
        assertEquals(1, rootObject.getChildCount());
        assertEquals(targetDB, rootObject.getChild(0));
        
        assertTrue(undoManager.canRedo());
        assertEquals(redoCount + 1, undoManager.getRedoableEditCount());
        undoManager.redo();
        assertEquals(2, rootObject.getChildCount());
        assertEquals(targetDB, rootObject.getChild(0));
        assertEquals(db, rootObject.getChild(1));
        
    }

}
