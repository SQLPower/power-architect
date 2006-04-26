package ca.sqlpower.architect.swingui;


import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLTable;

public class TestTablePane extends TestCase {

	private SQLTable t;
	private TablePane tp;
	private PlayPen pp;
	
	protected void setUp() throws Exception {
		super.setUp();
		t = new SQLTable(ArchitectFrame.getMainInstance().getProject().getTargetDatabase(), true);
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
		
		pp = ArchitectFrame.getMainInstance().getProject().getPlayPen();
		tp = new TablePane(t, pp);
		
		pk1.setPrimaryKeySeq(1);
		pk2.setPrimaryKeySeq(2);
		pk3.setPrimaryKeySeq(3);
		
		assertEquals(3, t.getPkSize());
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
			class MyFolder extends SQLTable.Folder {
				MyFolder() {
					super(COLUMNS, true);
				}
				
				public void removeLastChildNoEvent() {
					children.remove(children.size() - 1);
				}
			}
			public MySQLTable(String name) throws ArchitectException {
				super(ArchitectFrame.getMainInstance().getProject().getTargetDatabase(), true);
				setName(name);
				children.set(0, new MyFolder());
				columnsFolder = (Folder) children.get(0);
			}
			public void removeLastColumnNoEvent() {
				((MyFolder) getColumnsFolder()).removeLastChildNoEvent();
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
		tp.dbChildrenRemoved(new SQLObjectEvent(fakeSource, new int[] {6}, new SQLObject[] {fakeSource}));
		
		// this event notifies the table pane that we removed c1 earlier on.  It should not throw an exception
		tp.dbChildrenRemoved(new SQLObjectEvent(t.getColumnsFolder(), new int[] {0}, new SQLObject[] {c1}));
	}
	
}
