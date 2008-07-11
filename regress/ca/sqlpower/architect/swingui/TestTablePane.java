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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

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
		
		t.addColumn(0,pk1);
		t.addColumn(1,pk2);
		t.addColumn(2,pk3);
		t.addColumn(3,at1);
		t.addColumn(4,at2);
		t.addColumn(5,at3);
        
		pp = session.getPlayPen();
		tp = new TablePane(t, pp);
		
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
	
	public void testInsertColumnAtTop() throws ArchitectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
		List<SQLObject> newcolList = new ArrayList<SQLObject>();
		newcolList.add(newcol);
		tp.insertObjects(newcolList, 0);
		
		assertEquals(7, t.getColumns().size());
		assertEquals(4, t.getPkSize());
	}

	/** This tests for a regression we found in March 2006 (bug 1057) */
	public void testInsertColumnAtStartOfNonPK() throws ArchitectException {
		SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
		t.addColumn(0, newcol);
		
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, TablePane.COLUMN_INDEX_START_OF_NON_PK);
		
		assertEquals(3, t.getColumnIndex(newcol));
		assertNull("Column should have moved out of primary key", newcol.getPrimaryKeySeq());
	}

	/** This tests for a regression we found in March 2006 (bug 1057) */
	public void testInsertColumnAboveFirstNonPKColumn() throws ArchitectException {
		SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
		t.addColumn(0, newcol);
		
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, 4);
		
		assertEquals(3, t.getColumnIndex(newcol));
		assertNull("Column should have moved out of primary key", newcol.getPrimaryKeySeq());
	}

	public void testInsertNewColumnAboveFirstNonPKColumn() throws ArchitectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
		t2.addColumn(0, newcol);
		newcol.setPrimaryKeySeq(1);
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, 3);
		
		assertEquals(3, t.getColumnIndex(newcol));
		assertNull("Column should not be in primary key", newcol.getPrimaryKeySeq());
	}
	
	/** This tests for a real regression (the column was ending up at index 2 instead of 3) */
	public void testInsertNewColumnAtEndOfPK() throws ArchitectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		SQLColumn newcol = new SQLColumn(t2, "newcol", Types.INTEGER, 10, 0);
		t2.addColumn(0, newcol);
		newcol.setPrimaryKeySeq(1);
		assertNotNull("Column should start in primary key", newcol.getPrimaryKeySeq());
		
		List<SQLObject> movecolList = new ArrayList<SQLObject>();
		movecolList.add(newcol);
		tp.insertObjects(movecolList, TablePane.COLUMN_INDEX_END_OF_PK);
		
		assertEquals(3, t.getColumnIndex(newcol));
		assertNotNull("Column should be in primary key", newcol.getPrimaryKeySeq());
	}

	public void testDisallowImportTableFromPlaypen() throws ArchitectException {
		SQLTable t2 = new SQLTable(t.getParentDatabase(), true);
		t2.setName("Another Test Table");
		
		List<SQLObject> tableList = new ArrayList<SQLObject>();
		tableList.add(t2);
		
		assertFalse("Inserting a table from the playpen is not allowed", tp.insertObjects(tableList, 0));
	}
	
	public void testListenerDoesntCleanUpEarly() throws ArchitectException {
		class MySQLTable extends SQLTable {
			class MyFolder extends SQLTable.Folder<SQLColumn> {
				MyFolder() {
					super(COLUMNS, true);
				}
				
				public void removeLastChildNoEvent() {
					children.remove(children.size() - 1);
				}
			}
			public MySQLTable(String name) throws ArchitectException {
				super(session.getTargetDatabase(), true);
				setName(name);
				children.set(0, new MyFolder());
				columnsFolder = (Folder) children.get(0);
			}
			public void removeLastColumnNoEvent() {
				Folder<SQLColumn> columnsFolder2 = getColumnsFolder();
                ((MyFolder) columnsFolder2).removeLastChildNoEvent();
			}
		}
		
		MySQLTable t = new MySQLTable("table");
		SQLColumn c1 = new SQLColumn(t, "PK1", Types.BIT, 1, 0);
		t.addColumn(0, c1);
		
		TablePane tp = new TablePane(t, pp);
		
		assertEquals(1, t.getColumns().size());
		t.removeLastColumnNoEvent();
		assertEquals(0, t.getColumns().size());
		
		// now table has selection list size 1, and model's column list is size 0
		
		// this event came from somewhere else.  it shouldn't affect the success of the next event
		SQLColumn fakeSource = new SQLColumn();
		tp.columnListener.dbChildrenRemoved(new SQLObjectEvent(fakeSource, new int[] {6}, new SQLObject[] {fakeSource}));
		
		// this event notifies the table pane that we removed c1 earlier on.  It should not throw an exception
		tp.columnListener.dbChildrenRemoved(new SQLObjectEvent(t.getColumnsFolder(), new int[] {0}, new SQLObject[] {c1}));
	}
	
    public void testMultiHighlight() throws ArchitectException {
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
        TablePane tp2 = new TablePane(t2, pp);
        
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
        tp2.insertObjects(Collections.singletonList(parentPk1), t2.getColumns().size());
    }
    
    public void testUnlistenToRemovedColumns() throws Exception {
        SQLColumn c = t.getColumn(0);
        assertTrue(c.getSQLObjectListeners().contains(tp.columnListener));
        t.removeColumn(0);
        assertFalse(c.getSQLObjectListeners().contains(tp.columnListener));
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

    @Override
    protected TablePane getTargetCopy() {
        return new TablePane(tp, tp.getParent());
    }

    @Override
    protected TablePane getTarget() {
        return tp;
    }
}
