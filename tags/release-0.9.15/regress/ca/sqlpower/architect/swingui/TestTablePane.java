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
package ca.sqlpower.architect.swingui;


import java.awt.Color;
import java.awt.Point;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPChildEvent.EventType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

public class TestTablePane extends TestPlayPenComponent<TablePane> {

	private SQLTable t;
	private TablePane tp;
	
	protected void setUp() throws Exception {
		super.setUp();
		t = new SQLTable(session.getTargetDatabase(), true);
		t.setName("Test Table");
		SQLColumn pk1 = new SQLColumn(t, "PKColumn1", Types.INTEGER, 10,0);
		SQLColumn pk2 = new SQLColumn(t, "PKColumn2", Types.INTEGER, 10,0);
		SQLColumn pk3 = new SQLColumn(t, "PKColumn3", Types.INTEGER, 10,0);
		SQLColumn at1 = new SQLColumn(t, "AT1", Types.INTEGER, 10,0);
		SQLColumn at2 = new SQLColumn(t, "AT2", Types.INTEGER, 10,0);
		SQLColumn at3 = new SQLColumn(t, "AT3", Types.INTEGER, 10,0);
		
		t.addColumn(pk1,0);
		t.addColumn(pk2,1);
		t.addColumn(pk3,2);
		t.addColumn(at1,3);
		t.addColumn(at2,4);
		t.addColumn(at3,5);
        
		pp = session.getPlayPen();
		tp = new TablePane(t, pp.getContentPane());
		
		pk1.setPrimaryKeySeq(1);
		pk2.setPrimaryKeySeq(2);
		pk3.setPrimaryKeySeq(3);
		
		assertEquals(3, t.getPkSize());
		
		
		copyIgnoreProperties.add("height");
        copyIgnoreProperties.add("dropTargetListener");

        // selected columns are not to be copied
        copyIgnoreProperties.add("selectedColumnIndex");
        
        // layout node methods that are determined by the model
        copyIgnoreProperties.add("inboundEdges");
        copyIgnoreProperties.add("outboundEdges");
        
        // same as name
        copyIgnoreProperties.add("nodeName");

        // used specifically in mapping report to show full name (e.g. db.schema.cat.table)
        copyIgnoreProperties.add("fullyQualifiedNameInHeader");
	}
	
	public void testInsertColumnAtTop() throws SQLObjectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
		List<SQLObject> newcolList = new ArrayList<SQLObject>();
		newcolList.add(newcol);
		tp.insertObjects(newcolList, 0, false);
		
		assertEquals(7, t.getColumns().size());
		assertEquals(4, t.getPkSize());
	}

	/** This tests for a regression we found in March 2006 (bug 1057) */
	public void testInsertColumnAtStartOfNonPK() throws Exception {
	    SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
	    t.addColumn(newcol, 0);

	    assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());

	    List<SQLObject> movecolList = new ArrayList<SQLObject>();
	    movecolList.add(newcol);
	    tp.insertObjects(movecolList, TablePane.COLUMN_INDEX_START_OF_NON_PK, true);

	    assertEquals(3, t.getColumnIndex(newcol));
	    assertNull("Column should have moved out of primary key", newcol.getPrimaryKeySeq());
	}

	/** This tests for a regression we found in March 2006 (bug 1057) */
	public void testInsertColumnAboveFirstNonPKColumn() throws Exception {
	    SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
	    t.addColumn(newcol, 0);

	    assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());

	    List<SQLObject> movecolList = new ArrayList<SQLObject>();
	    movecolList.add(newcol);
	    tp.insertObjects(movecolList, 4, true);

	    assertEquals(3, t.getColumnIndex(newcol));
	    assertNull("Column should have moved out of primary key", newcol.getPrimaryKeySeq());
	}

	public void testInsertNewColumnAboveFirstNonPKColumn() throws Exception {
	    SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
	    t2.setName("Another Test Table");
	    SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
	    t2.addColumn(newcol, 0);
	    newcol.setPrimaryKeySeq(1);
	    assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());

	    List<SQLObject> movecolList = new ArrayList<SQLObject>();
	    movecolList.add(newcol);
	    tp.insertObjects(movecolList, 3, true);

	    assertEquals(3, t.getColumnIndex(newcol));
	    assertNull("Column should not be in primary key", newcol.getPrimaryKeySeq());
	}

	/** This tests for a real regression (the column was ending up at index 2 instead of 3) */
	public void testInsertNewColumnAtEndOfPK() throws Exception {
	    SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
	    t2.setName("Another Test Table");
	    SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
	    t2.addColumn(newcol, 0);
	    newcol.setPrimaryKeySeq(1);
	    assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());

	    List<SQLObject> movecolList = new ArrayList<SQLObject>();
	    movecolList.add(newcol);
	    tp.insertObjects(movecolList, TablePane.COLUMN_INDEX_END_OF_PK, true);

	    assertEquals(3, t.getColumnIndex(newcol));
	    assertNotNull("Column should be in primary key", newcol.getPrimaryKeySeq());
	}

	/** This tests for a regression we found in March 2006 (bug 1057) */
	public void testInsertColumnAtStartOfNonPKByCopy() throws SQLObjectException {
		SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
		t.addColumn(newcol, 0);
		
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLColumn> oldColumns = new ArrayList<SQLColumn>(t.getColumns());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, TablePane.COLUMN_INDEX_START_OF_NON_PK, false);
		
		List<SQLColumn> newColumns = new ArrayList<SQLColumn>(t.getColumns());
		newColumns.removeAll(oldColumns);
		
		assertEquals(1, newColumns.size());
		final SQLColumn copyCol = newColumns.get(0);
        assertEquals(4, t.getColumnIndex(copyCol));
		assertNull("Column should have moved out of primary key", copyCol.getPrimaryKeySeq());
		
		//assert column copied still exists
		assertEquals(0, t.getColumnIndex(newcol));
		assertNotNull("Column copied should stay in primary key", newcol.getPrimaryKeySeq());
	}

	/** This tests for a regression we found in March 2006 (bug 1057) */
	public void testInsertColumnAboveFirstNonPKColumnByCopy() throws SQLObjectException {
		SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
		t.addColumn(newcol, 0);
		
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLColumn> oldColumns = new ArrayList<SQLColumn>(t.getColumns());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, 4, false);
		
		List<SQLColumn> newColumns = new ArrayList<SQLColumn>(t.getColumns());
		newColumns.removeAll(oldColumns);

		assertEquals(1, newColumns.size());
		SQLColumn copyColumn = newColumns.get(0);
		assertEquals(4, t.getColumnIndex(copyColumn));
		assertNull("Column copied should be out of primary key", copyColumn.getPrimaryKeySeq());
		
		//column copied should not be changed
		assertEquals(0, t.getColumnIndex(newcol));
		assertNotNull("Column should stay in primary key", newcol.getPrimaryKeySeq());
	}

	public void testInsertNewColumnAboveFirstNonPKColumnByCopy() throws SQLObjectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
		t2.addColumn(newcol, 0);
		newcol.setPrimaryKeySeq(1);
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLColumn> oldColumns = new ArrayList<SQLColumn>(t.getColumns());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, 3, false);
		
		List<SQLColumn> newColumns = new ArrayList<SQLColumn>(t.getColumns());
		newColumns.removeAll(oldColumns);
		
		assertEquals(1, newColumns.size());
		SQLColumn copyCol = newColumns.get(0);
		assertEquals(3, t.getColumnIndex(copyCol));
		assertNull("Copy column should not bein in the primary key", copyCol.getPrimaryKeySeq());
		
		assertEquals(0, t2.getColumnIndex(newcol));
		assertNotNull("Column should still be in primary key", newcol.getPrimaryKeySeq());
	}
	
	/** This tests for a real regression (the column was ending up at index 2 instead of 3) */
	public void testInsertNewColumnAtEndOfPKByCopy() throws SQLObjectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
		t2.addColumn(newcol, 0);
		newcol.setPrimaryKeySeq(1);
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLColumn> oldColumns = new ArrayList<SQLColumn>(t.getColumns());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, TablePane.COLUMN_INDEX_END_OF_PK, false);
		
		List<SQLColumn> newColumns = new ArrayList<SQLColumn>(t.getColumns());
		newColumns.removeAll(oldColumns);
		
		assertEquals(1, newColumns.size());
		SQLColumn copyCol = newColumns.get(0);
		
		assertEquals(3, t.getColumnIndex(copyCol));
		assertNotNull("Copy column should be in primary key", copyCol.getPrimaryKeySeq());
		
		//Assert column copied is unmodified.
		assertEquals(0, t2.getColumnIndex(newcol));
		assertNotNull("Column should still be in primary key", newcol.getPrimaryKeySeq());
	}

	public void testImportTableFromPlaypenByCopy() throws SQLObjectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newCol = new SQLColumn(t2, "another column", Types.VARCHAR, 10, 0);
		t2.addColumn(newCol);
		
		List<SQLObject> tableList = new ArrayList<SQLObject>();
		tableList.add(t2);
		
		List<SQLColumn> oldColumns = new ArrayList<SQLColumn>(t.getColumns());
		
		tp.insertObjects(tableList, 0, false);
		
		List<SQLColumn> newColumns = new ArrayList<SQLColumn>(t.getColumns());
		newColumns.removeAll(oldColumns);
		
		assertEquals(1, t2.getColumns().size());
		assertEquals(1, newColumns.size());
		assertEquals(newCol.getName(), newColumns.get(0).getName());
	}
	
	/**
	 * Regression test: The table pane used to respond to all events as if they came
	 * from the table it listened on; in fact, the events can come from the table or
	 * any of its folders and those events have to be handled in the right way.
	 */
	public void testListenerDoesntCleanUpEarly() throws SQLObjectException {
	    SQLTable t = new SQLTable(session.getTargetDatabase(), true);
		SQLColumn c1 = new SQLColumn(t, "PK1", Types.BIT, 1, 0);
		t.addColumn(c1, 0);
		
		TablePane tp = new TablePane(t, pp.getContentPane());
		
		assertEquals(1, t.getColumns().size());
		t.removeColumn(t.getChildrenWithoutPopulating(SQLColumn.class).size() - 1);
		assertEquals(0, t.getColumns().size());
		
		// now table has selection list size 1, and model's column list is size 0
		
		// this event came from somewhere else.  it shouldn't affect the success of the next event
		SQLColumn fakeSource = new SQLColumn();
		tp.columnListener.childRemoved(new SPChildEvent(fakeSource, fakeSource.getClass(), fakeSource, 6, EventType.REMOVED));
		
		// this event notifies the table pane that we removed c1 earlier on.  It should not throw an exception
		tp.columnListener.childRemoved(new SPChildEvent(t, c1.getClass(), c1, 0, EventType.REMOVED));
	}
	
    public void testMultiHighlight() throws SQLObjectException {
        SQLColumn col = tp.getModel().getColumn(0);
        tp.addColumnHighlight(col, Color.RED);
        tp.addColumnHighlight(col, Color.GREEN);
        assertEquals(new Color(128, 128, 0), tp.getColumnHighlight(col));
        tp.removeColumnHighlight(col, Color.RED);
        assertEquals(Color.GREEN, tp.getColumnHighlight(col));
        tp.removeColumnHighlight(col, Color.GREEN);
        assertEquals(tp.getForegroundColor(), tp.getColumnHighlight(col));
    }
    
    /**
     * Regression test for bug 1542.
     */
    public void testDragDropParentPKToChildTable() throws Exception {
        SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
        TablePane tp2 = new TablePane(t2, pp.getContentPane());
        
        SQLRelationship r = new SQLRelationship();
        r.attachRelationship(t, t2, true);
        
        SQLColumn parentPk1 = t.getColumn(0);
        assertTrue(parentPk1.isPrimaryKey());
        
        // Before fixing bug 1542, the following operation failed with
        // java.lang.IndexOutOfBoundsException: Index: 3, Size: 2
        // Because before inserting parentPk1 into t2, it was removed from t1
        // That removal from t1 causes the side effect of removing the imported
        // copy of parentPk1 in t2. Hence, t2 shrinks by one column, and the
        // specified insertion index is out of bounds.
        tp2.insertObjects(Collections.singletonList(parentPk1), t2.getColumns().size(), false);
    }
    
    public void testUnlistenToRemovedColumns() throws Exception {
        SQLColumn c = t.getColumn(0);
        assertTrue(c.getSPListeners().contains(tp.columnListener));
        t.removeColumn(0);
        assertFalse(c.getSPListeners().contains(tp.columnListener));
    }
    
    public void testSetLocationFiresEvents() {
        PlayPenComponentEventCounter eventCounter = new PlayPenComponentEventCounter();
        tp.addPropertyChangeListener("location", eventCounter);
        assertEquals("" +
                "We started out with the wrong number of events", 0,eventCounter.getEvents() );
        pp.startCompoundEdit("Starting move");
        tp.setLocation(1,1);
        tp.setLocation(2,2);
        pp.endCompoundEdit("Ending move");
        assertEquals("Compound edit did not fire a move event for each setLocation",2,eventCounter.getMoved());
        
        tp.setLocation(3,3);
        assertEquals("Single edit did not fire move event!",3,eventCounter.getMoved());
    }

    /**
     * When you delete a column (say, the 4th one) from a table, the new 4th
     * column should be selected. This helps when deleting several columns.
     */
    public void testSelectionAfterColumnDeleted() throws Exception {
        tp.selectItem(2);
        assertTrue(tp.isItemSelected(2));
        t.removeChild(t.getChild(2));
        assertTrue(tp.isItemSelected(2));
    }

    /**
     * Test for actual regression where you get an ArrayIndexOutOfBoundsException
     * when trying to move the only column into the PK.
     */
    public void testMoveOnlyColumn() throws Exception {
        while (t.getColumns().size() > 1) {
            t.removeColumn(0);
        }
        assertEquals(1, t.getColumns().size());
        
        tp.selectItem(0);
        
        // this was throwing an exception
        t.getColumn(0).setPrimaryKeySeq(0);
        
    }
    /**
     * When you delete a column (say, the 4th one) from a table, the new 4th
     * column should be selected. This helps when deleting several columns.
     */
    public void testSelectionAfterColumnDeletedWithSynchronizer() throws Exception {
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        TestingArchitectSwingSession session = (TestingArchitectSwingSession) context.createSession();
        PlayPen pp = RelationalPlayPenFactory.createPlayPen(session, session.getSourceDatabases());
        
        TablePane tp = new TablePane(t, pp.getContentPane());
        pp.addTablePane(tp, new Point(2,2));
        tp.selectItem(2);
        assertEquals(Collections.singletonList(t.getColumn(2)), tp.getSelectedItems());
        t.removeChild(t.getChild(2));
        assertEquals(Collections.singletonList(t.getColumn(2)), tp.getSelectedItems());
    }
    
    /**
     * This is regression testing for bug 1628. Previously dragging a column from one table to
     * another would not fire a change event on the columns folder the child column was added to.
     * This broke the relationships as they update based on inserts and removes on the column
     * folder. 
     */
    public void testDropColumnBetweenTablesFiresFolderEvent() throws Exception {
        
        SQLRelationship rel = new SQLRelationship();
        rel.setIdentifying(true);
        SQLTable fkTable = new SQLTable(tp.getModel().getParentDatabase(), true);
        rel.attachRelationship(t, fkTable, true);

        SQLColumn col = new SQLColumn();
        col.setName("Test Col");
        fkTable.addColumn(col);
        
        assertEquals(3, t.getPkSize());
        assertEquals(3, fkTable.getPkSize());
        
        List<SQLColumn> oldColumns = new ArrayList<SQLColumn>(t.getColumns());
        
        tp.insertObjects(Collections.singletonList(col), TablePane.COLUMN_INDEX_END_OF_PK, false);
        
        List<SQLColumn> newColumns = new ArrayList<SQLColumn>(t.getColumns());
        newColumns.removeAll(oldColumns);
        
        assertEquals(1, newColumns.size());
        SQLColumn copyCol = newColumns.get(0);
        assertTrue(copyCol.isPrimaryKey());
        
        assertEquals(fkTable, col.getParent());
        assertTrue(col.isPrimaryKey());
        assertEquals(4, fkTable.getColumns().size());
        assertEquals(4, rel.getChildren().size());
        assertNotNull(rel.getMappingByPkCol(copyCol));
        assertNotNull(rel.getMappingByFkCol(col));
    }

    @Override
    protected TablePane getTargetCopy() {
        return new TablePane(tp, tp.getParent());
    }

    @Override
    protected TablePane getTarget() {
        return tp;
    }
}
