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
package ca.sqlpower.architect.diff;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffType;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

public class CompareSQLTest extends TestCase {



	SQLTable table1;
	SQLTable table2;
	SQLTable table3;
	SQLTable tableNoColumn1;


	SQLColumn c1;
	SQLColumn c1Dupl;
	SQLColumn c2;
	SQLColumn c2LookAlike;
	SQLColumn c3;
	SQLColumn c4;
	SQLColumn c5;
	SQLColumn c6;
	List <SQLTable> listWithATable;

	protected void setUp() throws Exception {
		//Just tables with Columns
		tableNoColumn1 = new SQLTable(null,"table1" , "...",SQLTable.class.toString(), true);



		//Table with two columns
		table1 = new SQLTable(null,"tableWithColumn1","actually r1",
							SQLTable.class.toString(),true);
		//The Column specs will need to be changed (scale, type, precision)
		c1 = new SQLColumn(table1, "Column1", 0,2,3);
	    c2 = new SQLColumn(table1, "Column2", Types.INTEGER,5,0);
		table1.addColumn(c1);
		table1.addColumn(c2);

		//Table with two columns
		table2 = new SQLTable(null, "tableWithColumn2", "actually r2",
									SQLTable.class.toString(),true);
		c3 = new SQLColumn(table2, "Column3", 0,2,3);
		c4 = new SQLColumn(table2, "Column3a", 0,2,3);
		table2.addColumn(c3);
		table2.addColumn(c4);

		table3 = new SQLTable(null, "tableWithColumn3", "actually r3",SQLTable.class.toString(),true);
		c5 = new SQLColumn(table3, "Column3a", 0,2,3);
		c6= new SQLColumn(table3, "Column4", 0,2,3);
		table3.addColumn(c5);
		table3.addColumn(c6);



		listWithATable = new ArrayList();
		listWithATable.add(tableNoColumn1);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	/*
	 * Test method for 'ca.sqlpower.architect.swingui.CompareSchemaWorker.getProgress()'
	 */
	public void testGetProgress() {

	}

	public void testEmptyPlayPenCompareSQL(){

	}

	public void testDiffListWithTablesOnly() throws SQLObjectException{
		//Testing diffchunk with nothing and nothing
		List <SQLTable> list1 = new ArrayList();

		CompareSQL compare1 = new CompareSQL ((Collection<SQLTable>)list1,
												(Collection<SQLTable>)list1, false);
		List<DiffChunk<SQLObject>> nullChecker = compare1.generateTableDiffs();
		assertEquals (0, nullChecker.size());


		//Testing diff chunk with one table and nothing;
		CompareSQL compareWorker = new CompareSQL((Collection<SQLTable>)listWithATable,
													(Collection<SQLTable>)list1, false);
		List<DiffChunk<SQLObject>> tableAndNull = compareWorker.generateTableDiffs();
		assertEquals (1,tableAndNull.size());
		assertEquals (DiffType.LEFTONLY, tableAndNull.get(0).getType());
		assertEquals ("table1", tableAndNull.get(0).getData().getName());


		//Testing diff chunk with two list that has the same properties
		CompareSQL compareWorker1 = new CompareSQL((Collection<SQLTable>)listWithATable,
													(Collection<SQLTable>)listWithATable, false);
		List<DiffChunk<SQLObject>> exactlySameTable = compareWorker1.generateTableDiffs();
		assertEquals (1,exactlySameTable.size());
		assertEquals (DiffType.SAME, exactlySameTable.get(0).getType());
		assertEquals ("table1", tableAndNull.get(0).getData().getName());



		//Testing diff chunk with lists that have same and different tables

		SQLTable tableNoColumn2 = new SQLTable(null,"table2" , "...",SQLTable.class.toString(), true);
		SQLTable tableNoColumn3 = new SQLTable(null,"table3" , "...",SQLTable.class.toString(), true);
		listWithATable.add(tableNoColumn3);
		list1.add(tableNoColumn1);
		list1.add(tableNoColumn2);

		CompareSQL compareWorker2 = new CompareSQL((Collection<SQLTable>)listWithATable,
				(Collection<SQLTable>)list1, false);
		List<DiffChunk<SQLObject>> differentProp = compareWorker2.generateTableDiffs();
		assertEquals (3,differentProp.size());

		assertEquals (DiffType.SAME, differentProp.get(0).getType());
		assertEquals ("table1", differentProp.get(0).getData().getName());

		assertEquals (DiffType.RIGHTONLY, differentProp.get(1).getType());
		assertEquals ("table2", differentProp.get(1).getData().getName());

		assertEquals (DiffType.LEFTONLY, differentProp.get(2).getType());
		assertEquals ("table3", differentProp.get(2).getData().getName());
	}


	public void testDiffListWithTableHavingColumn() throws SQLObjectException{
		List <SQLTable >tableList1 = new ArrayList();
		List <SQLTable >tableList2 = new ArrayList();

		//Testing table with column and nothing
		tableList1.add(table1);
		CompareSQL worker1 = new CompareSQL(
				(Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList2, false);
		List<DiffChunk<SQLObject>> tableWithColumnAndNothing = worker1.generateTableDiffs();
		assertEquals (1,tableWithColumnAndNothing.size());

		assertEquals (DiffType.LEFTONLY, tableWithColumnAndNothing.get(0).getType());
		assertEquals (SQLTable.class, tableWithColumnAndNothing.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", tableWithColumnAndNothing.get(0).getData().getName());


		//Testing tables with the same column
		CompareSQL worker2 = new CompareSQL((Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList1, false);
		List<DiffChunk<SQLObject>> sameTablesWithColumns = worker2.generateTableDiffs();
		assertEquals (3,sameTablesWithColumns.size());

		assertEquals (DiffType.SAME, sameTablesWithColumns.get(0).getType());
		assertEquals (SQLTable.class, sameTablesWithColumns.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", sameTablesWithColumns.get(0).getData().getName());

		assertEquals (DiffType.SAME, sameTablesWithColumns.get(1).getType());
		assertEquals (SQLColumn.class, sameTablesWithColumns.get(1).getData().getClass());
		assertEquals ("Column1", sameTablesWithColumns.get(1).getData().getName());

		assertEquals (DiffType.SAME, sameTablesWithColumns.get(2).getType());
		assertEquals (SQLColumn.class, sameTablesWithColumns.get(2).getData().getClass());
		assertEquals ("Column2", sameTablesWithColumns.get(2).getData().getName());


		//Testing table with column against table with no column
		SQLTable table1NoColumn = new SQLTable(null,"tableWithColumn1","it's lying!",
				SQLTable.class.toString(), true);
		List<SQLTable> tempList = new ArrayList();
		tempList.add(table1NoColumn);
		CompareSQL worker3 = new CompareSQL((Collection<SQLTable>)tempList,
				(Collection<SQLTable>)tableList1, false);
		List<DiffChunk<SQLObject>> diffTest = worker3.generateTableDiffs();

		assertEquals (3, diffTest.size());		
		assertEquals (DiffType.SQL_MODIFIED, diffTest.get(0).getType());
		assertEquals (1, diffTest.get(0).getPropertyChanges().size());
		assertEquals ("remarks", diffTest.get(0).getPropertyChanges().get(0).getPropertyName());
		assertEquals (SQLTable.class, diffTest.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", diffTest.get(0).getData().getName());

		assertEquals (DiffType.RIGHTONLY, diffTest.get(1).getType());
		assertEquals (SQLColumn.class, diffTest.get(1).getData().getClass());
		assertEquals ("Column1", diffTest.get(1).getData().getName());

		assertEquals (DiffType.RIGHTONLY, diffTest.get(2).getType());
		assertEquals (SQLColumn.class, diffTest.get(2).getData().getClass());
		assertEquals ("Column2", diffTest.get(2).getData().getName());

		//Testing tables with all DiffTypes: SAME, LEFTONLY, RIGHTONLY, MODIFIED

		//Similar to table 1 with minor changes
		SQLTable table1LookAlike = new SQLTable(null,"tableWithColumn1","actually r1lookAlike",
									SQLTable.class.toString(),true);
		c1Dupl = new SQLColumn(table1LookAlike, "Column1", 0,2,3);
		c2LookAlike = new SQLColumn (table1LookAlike, "Column2", 1, 3,5);
		table1LookAlike.addColumn(c1Dupl);
		table1LookAlike.addColumn(c2LookAlike);

		tableList1.add(table2);
		tableList2.add(table1LookAlike);
		tableList2.add(table3);

		CompareSQL worker4 = new CompareSQL((Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList2, false);
		List<DiffChunk<SQLObject>> manyProperties = worker4.generateTableDiffs();
//		System.out.println("-/|\\-"+manyProperties+"-/|\\-");
		assertEquals (5, manyProperties.size());

		assertEquals (DiffType.SQL_MODIFIED, manyProperties.get(0).getType());
		assertEquals (1, manyProperties.get(0).getPropertyChanges().size());
		assertEquals ("remarks", manyProperties.get(0).getPropertyChanges().get(0).getPropertyName());
		assertEquals (SQLTable.class, manyProperties.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", manyProperties.get(0).getData().getName());

		assertEquals (DiffType.SAME, manyProperties.get(1).getType());
		assertEquals (SQLColumn.class, manyProperties.get(1).getData().getClass());
		assertEquals ("Column1", manyProperties.get(1).getData().getName());

		assertEquals (DiffType.SQL_MODIFIED, manyProperties.get(2).getType());
		assertEquals (SQLColumn.class, manyProperties.get(2).getData().getClass());
		assertEquals ("Column2", manyProperties.get(2).getData().getName());

		assertEquals (DiffType.LEFTONLY, manyProperties.get(3).getType());
		assertEquals (SQLTable.class, manyProperties.get(3).getData().getClass());
		assertEquals ("tableWithColumn2", manyProperties.get(3).getData().getName());

		assertEquals (DiffType.RIGHTONLY, manyProperties.get(4).getType());
		assertEquals (SQLTable.class, manyProperties.get(4).getData().getClass());
		assertEquals ("tableWithColumn3", manyProperties.get(4).getData().getName());

	}

	public void testTableWithRelationShip() throws SQLObjectException{
		SQLTable newTable1 = table1;
		SQLTable newTable2 = table2;
		SQLRelationship relation1 = new SQLRelationship();
		relation1.attachRelationship(table1,table2,false);
		relation1.addMapping(newTable1.getColumn(0), newTable2.getColumn(1));
		relation1.setName("relation1");


		List<SQLTable> newList1 = new ArrayList<SQLTable>();
		List<SQLTable> newList2 = new ArrayList<SQLTable>();

		newList1.add(newTable1);
		newList1.add(newTable2);

		CompareSQL worker1 = new CompareSQL((Collection<SQLTable>)newList1,
				(Collection<SQLTable>)newList2, false);
		List<DiffChunk<SQLObject>> diffList = worker1.generateTableDiffs();
		assertEquals (3, diffList.size());
		assertEquals (DiffType.LEFTONLY, diffList.get(0).getType());
		assertEquals (SQLTable.class, diffList.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", diffList.get(0).getData().getName());

		assertEquals (DiffType.LEFTONLY, diffList.get(1).getType());
		assertEquals (SQLTable.class, diffList.get(1).getData().getClass());
		assertEquals ("tableWithColumn2", diffList.get(1).getData().getName());

		assertEquals (DiffType.LEFTONLY, diffList.get(2).getType());
		assertEquals (SQLRelationship.class, diffList.get(2).getData().getClass());
		assertEquals ("relation1", diffList.get(2).getData().getName());

	}

	public void testRelationshipsWithSameMappings() throws SQLObjectException {

		// Set up source (left hand side) envorinment
		SQLTable newTable1L = makeTable(4);
		SQLTable newTable2L = makeTable(6);
		SQLRelationship relationL = new SQLRelationship();
		relationL.addMapping(newTable1L.getColumn(0), newTable2L.getColumn(1));
		relationL.setName("relation1");

		relationL.attachRelationship(newTable1L,newTable2L,false);

		List<SQLTable> tableListL = new ArrayList<SQLTable>();
		tableListL.add(newTable1L);
		tableListL.add(newTable2L);

		// Set up source (left hand side) envorinment
		SQLTable newTable1R = makeTable(4);
		SQLTable newTable2R = makeTable(6);
		SQLRelationship relationR = new SQLRelationship();
		relationR.addMapping(newTable1R.getColumn(0), newTable2R.getColumn(1));
		relationR.setName("relation1");
		relationR.attachRelationship(newTable1R,newTable2R,false);

		List<SQLTable> tableListR = new ArrayList<SQLTable>();
		tableListR.add(newTable1R);
		tableListR.add(newTable2R);

		CompareSQL cs = new CompareSQL(tableListL, tableListR, false);
		List<DiffChunk<SQLObject>> diffs = cs.generateTableDiffs();

		for (DiffChunk<SQLObject> chunk : diffs) {
			assertEquals(
					"Left side == Right side. Diff list should be all same",
					DiffType.SAME, chunk.getType());
		}
	}

	public void testRelationshipsWithDifferentNames() throws SQLObjectException {

		// Set up source (left hand side) envorinment
		SQLTable newTable1L = makeTable(4);
		SQLTable newTable2L = makeTable(6);
		SQLRelationship relationL = new SQLRelationship();
		relationL.addMapping(newTable1L.getColumn(0), newTable2L.getColumn(1));
		relationL.setName("relation1");

		relationL.attachRelationship(newTable1L,newTable2L,false);

		List<SQLTable> tableListL = new ArrayList<SQLTable>();
		tableListL.add(newTable1L);
		tableListL.add(newTable2L);

		// Set up source (left hand side) envorinment
		SQLTable newTable1R = makeTable(4);
		SQLTable newTable2R = makeTable(6);
		SQLRelationship relationR = new SQLRelationship();
		relationR.addMapping(newTable1R.getColumn(0), newTable2R.getColumn(1));
		relationR.setName("not_relation1");

		relationR.attachRelationship(newTable1R,newTable2R,false);

		List<SQLTable> tableListR = new ArrayList<SQLTable>();
		tableListR.add(newTable1R);
		tableListR.add(newTable2R);

		CompareSQL cs = new CompareSQL(tableListL, tableListR, false, false);
		List<DiffChunk<SQLObject>> diffs = cs.generateTableDiffs();

		for (DiffChunk<SQLObject> chunk : diffs) {
			if (chunk.getData().getClass().equals(SQLRelationship.class)) {
				assertEquals("The relationships have different names",
						DiffType.NAME_CHANGED, chunk.getType());
				assertEquals(2, chunk.getPropertyChanges().size());
			} else {
				assertEquals(
						"Diff list should be all same for non-relationship SQLObjects",
						DiffType.SAME, chunk.getType());
			}
		}
	}

	public void testRelationshipsWithDifferentMappings() throws SQLObjectException {

		// Set up source (left hand side) envorinment
		SQLTable newTable1L = makeTable(4);
		SQLTable newTable2L = makeTable(6);
		SQLRelationship relationL = new SQLRelationship();
		//This is done because the architect requires imported key to be in the primary key
		newTable1L.addToPK(newTable1L.getColumn(0));
		relationL.addMapping(newTable1L.getColumn(0), newTable2L.getColumn(2));  // this is the difference
		relationL.setName("relation1");
		relationL.attachRelationship(newTable1L,newTable2L,false);

		List<SQLTable> tableListL = new ArrayList<SQLTable>();
		tableListL.add(newTable1L);
		tableListL.add(newTable2L);

		// Set up source (left hand side) envorinment
		SQLTable newTable1R = makeTable(4);
		SQLTable newTable2R = makeTable(6);
		SQLRelationship relationR = new SQLRelationship();
		newTable1R.addToPK(newTable1R.getColumn(0));
		relationR.addMapping(newTable1R.getColumn(0), newTable2R.getColumn(1));
		relationR.setName("relation1");

		relationR.attachRelationship(newTable1R,newTable2R,false);

		List<SQLTable> tableListR = new ArrayList<SQLTable>();
		tableListR.add(newTable1R);
		tableListR.add(newTable2R);

		CompareSQL cs = new CompareSQL(tableListL, tableListR, false);
		List<DiffChunk<SQLObject>> diffs = cs.generateTableDiffs();

		boolean foundColMapDiff = false;

		for (DiffChunk<SQLObject> chunk : diffs) {
			if (chunk.getData().getClass().equals(SQLRelationship.class)) {
				foundColMapDiff = true;
				assertNotSame("The mappings have different columns",
						DiffType.SAME, chunk.getType());
			} else {
				assertEquals(
						"Diff list should be all same for non-mapping SQLObjects",
						DiffType.SAME, chunk.getType());
			}
		}

		assertTrue("No column mapping diffs found!", foundColMapDiff);
	}

	public void testDropTableWithUUID() throws SQLObjectException{
		List<SQLTable> oldModel = new ArrayList<SQLTable>();
		List<SQLTable> newModel = new ArrayList<SQLTable>();

		SQLTable o1 = makeTable(1, 5);
		o1.addToPK(o1.getColumn(0));

		SQLTable o2 = makeTable(2, 6);
		o2.addToPK(o2.getColumn(0));

		SQLRelationship fk = new SQLRelationship();
		fk.addMapping(o1.getColumn(0), o2.getColumn(1));
		fk.setName("relation1");
		fk.attachRelationship(o1, o2, false);
		
		SQLTable o3 = makeTable(3, 3);
		o3.addToPK(o3.getColumn(0));

		SQLTable o4 = makeTable(4, 3);
		
		oldModel.add(o1);
		oldModel.add(o2);
		oldModel.add(o3);
		oldModel.add(o4);

		SQLTable n1 = makeTable(1,5);
		n1.addToPK(n1.getColumn(0));
		
		copyUUIDs(o1, n1);
		SQLTable n3 = makeTable(3,3);
		n3.addToPK(n3.getColumn(0));
		copyUUIDs(o3, n3);

		SQLTable n4 = makeTable(4,3);
		copyUUIDs(o4, n4);

		newModel.add(n1);
		newModel.add(n3);
		newModel.add(n4);

		CompareSQL sqlComparator = new CompareSQL(oldModel, newModel, true, true);
		List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();
		for (DiffChunk<SQLObject> chunk : diffs) {
			DiffType type = chunk.getType();
			if (type != DiffType.SAME) {
				if (chunk.getData().getName().equals("table_2")) {
					assertEquals(DiffType.LEFTONLY, type);
				}
				if (chunk.getData().getName().equals("relation1")) {
					assertEquals(DiffType.LEFTONLY, type);
				}
			}
		}
	}
	
	/**
	 * This test checks if a table rename is detected properly when comparing two PA models
	 * (renames cannot be detected when a "real" database is involved in the diff)
	 *
	 * @throws SQLObjectException
	 */
	public void testRenameTableAndColumnWithUUID() throws SQLObjectException{
		List<SQLTable> list1 = new ArrayList<SQLTable>();
		SQLTable t1 = makeTable(1,2);
		list1.add(t1);

		List<SQLTable> list2 = new ArrayList<SQLTable>();
		SQLTable t2 = makeTable(1,2);

		t2.setName("new_table_name");
		t2.setPhysicalName("new_table_name");
		list2.add(t2);

		// Copying the UUID simulates loading a different version of the same model file
		// as the UUIDs are stored (and thus preserved) for objects in the architect XML format
		copyUUIDs(t1, t2);
		
		t2.getColumn(1).setName("column_name_changed");
		t2.getColumn(1).setPhysicalName("column_name_changed");

		CompareSQL sqlComparator = new CompareSQL(list1, list2, false, true);
		sqlComparator.setCompareIndices(false);
		List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();
		for (DiffChunk<SQLObject> chunk : diffs) {
			System.out.println(chunk.toString());
		}
		assertEquals(3, diffs.size());
		assertEquals(DiffType.NAME_CHANGED, diffs.get(0).getType());

		// The remaining chunk list is order by UUID of the columns.
		// As the UUID will change with every test run the type of the chunk
		// needs to be asserted depending on the column's name, not the index
		// in the list
		DiffChunk<SQLObject> chunk = diffs.get(0);
		if (chunk.getData().getName().equalsIgnoreCase("column_name_changed")) {
			assertEquals(DiffType.NAME_CHANGED, chunk.getType());
		}

		chunk = diffs.get(1);
		if (chunk.getData().getName().equalsIgnoreCase("column_0")) {
			assertEquals(DiffType.SAME, chunk.getType());
		}
	}

	public void testRenameIndex() throws SQLObjectException{

		List<SQLTable> sourceList = new ArrayList<SQLTable>();
		SQLTable t1 = makeTable(2);
		sourceList.add(t1);

		SQLIndex index1 = new SQLIndex("IDX_TEST", false, null, null, null);
		index1.addIndexColumn(t1.getColumn(1));
		t1.addIndex(index1);

		List<SQLTable> targetList = new ArrayList<SQLTable>();
		SQLTable t2 = makeTable(2);
		targetList.add(t2);

		SQLIndex index2 = new SQLIndex("IDX_TEST_NEW", false, null, null, null);
		index2.addIndexColumn(t2.getColumn(1));
		t2.addIndex(index2);

		// Make sure all identical objects have the same UUID
		index2.setUUID(index1.getUUID());
		t2.getPrimaryKeyIndex().setUUID(t1.getPrimaryKeyIndex().getUUID());
		copyUUIDs(t1, t2);

		CompareSQL sqlComparator = new CompareSQL(sourceList, targetList, false, true);
		sqlComparator.setCompareIndices(false);
		List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();

//		for (DiffChunk<SQLObject> chunk : diffs) {
//			System.out.println(chunk.toString());
//		}
		// No table or column may have changed
		for (DiffChunk<SQLObject> chunk : diffs) {
			if (chunk.getData() instanceof SQLIndex) {
				SQLIndex index = (SQLIndex) chunk.getData();
				if (index.isPrimaryKeyIndex()) {
					assertEquals(DiffType.SAME, chunk.getType());
				} else {
					assertEquals(DiffType.NAME_CHANGED, chunk.getType());
				}
			} else {
				assertEquals(DiffType.SAME, chunk.getType());
			}
		}
	}
	
	public void testRenameRelationshipWithUUID() throws SQLObjectException{
		List<SQLTable> list1 = new ArrayList<SQLTable>();
		SQLTable source1 = makeTable(1,2);
		list1.add(source1);
		source1.addToPK(source1.getColumn(0));

		SQLTable source2 = makeTable(2,2);

		SQLRelationship fk = new SQLRelationship();
		fk.addMapping(source1.getColumn(0), source2.getColumn(1));
		fk.setName("relation1");
		fk.attachRelationship(source1, source2, false);
		list1.add(source2);

		List<SQLTable> list2 = new ArrayList<SQLTable>();

		SQLTable target1 = makeTable(1,2);
		target1.addToPK(target1.getColumn(0));
		SQLTable target2 = makeTable(2,2);

		copyUUIDs(source1, target1);
		copyUUIDs(source2, target2);
		
		list2.add(target1);

		SQLRelationship fk2 = new SQLRelationship();
		fk2.addMapping(target1.getColumn(0), target2.getColumn(1));
		fk2.setName("relation1_changed");
		fk2.attachRelationship(target1, target2, false);
		fk2.setUUID(fk.getUUID());
		list2.add(target2);

		CompareSQL sqlComparator = new CompareSQL(list1, list2, true, true);
		sqlComparator.setCompareIndices(false);
		List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();

		// No table or column may have changed
		for (DiffChunk<SQLObject> chunk : diffs) {
//			System.out.println(chunk.toString());
			if (chunk.getData() instanceof SQLRelationship) {
				assertEquals(DiffType.NAME_CHANGED, chunk.getType());
			} else {
				assertEquals(DiffType.SAME, chunk.getType());
			}
		}
	}

	private void copyUUIDs(SQLTable source, SQLTable target)
		throws SQLObjectException {
		target.setUUID(source.getUUID());
		for (SQLColumn col : source.getColumns()) {
			SQLColumn tcol = target.getColumnByName(col.getName(), false, false);
			if (tcol != null) {
				tcol.setUUID(col.getUUID());
			}
		}
	}
	/**
	 * This test adds a primary key to the source table which is not added
	 * to the primary key target table. This will simulate a user removing a key from a table.
	 */
	public void testKeyRemovedFromPrimaryKey() throws SQLObjectException{
		List<SQLTable> list1 = new ArrayList<SQLTable>();
		SQLTable t1 = makeTable(4);
		list1.add(t1);
		SQLColumn c = t1.getColumn(2);
		t1.addToPK(c);

		List<SQLTable> list2 = new ArrayList<SQLTable>();
		SQLTable t2 = makeTable(4);
		list2.add(t2);

		CompareSQL sqlComparator = new CompareSQL(list1, list2, false);
		List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();


		boolean first_table = true;

		for (DiffChunk dc : diffs){
			if (dc.getData().getClass().equals(SQLTable.class)){
				if(first_table){
					assertEquals (DiffType.SAME ,dc.getType());
				} else if (dc.getData().equals(t1)) {
				    assertEquals(DiffType.DROP_KEY, dc.getType());
				} else {
					assertEquals (DiffType.KEY_CHANGED, dc.getType());
				}
				first_table = false;
			}
			else if (dc.getData().getClass().equals(SQLColumn.class)){
				if (((SQLObject) dc.getData()).getName().equals(c.getName())){
					assertEquals(DiffType.SQL_MODIFIED, dc.getType());
				} else {
					assertEquals (DiffType.SAME, dc.getType());
				}
			}
		}
	}

	/**
     * This test adds a primary key to the source table which is not added
     * to the target table. This will simulate a user deleting a column that
     * was a primary key.
     */
    public void testKeyRemoved() throws SQLObjectException{
        List<SQLTable> list1 = new ArrayList<SQLTable>();
        SQLTable t1 = makeTable(4);
        list1.add(t1);
        SQLColumn c = t1.getColumn(3);
        t1.addToPK(c);

        List<SQLTable> list2 = new ArrayList<SQLTable>();
        SQLTable t2 = makeTable(4);
        t2.removeColumn(3);
        list2.add(t2);

        CompareSQL sqlComparator = new CompareSQL(list1, list2, false);
        List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();


        boolean first_table = true;

        for (DiffChunk dc : diffs){
            if (dc.getData().getClass().equals(SQLTable.class)){
                if(first_table){
                    assertEquals (DiffType.SAME ,dc.getType());
                } else if (dc.getData().equals(t1)) {
                    assertEquals(DiffType.DROP_KEY, dc.getType());
                } else {
                    assertEquals (DiffType.KEY_CHANGED, dc.getType());
                }
                first_table = false;
            }
            else if (dc.getData().getClass().equals(SQLColumn.class)){
                if (((SQLObject) dc.getData()).getName().equals(c.getName())){
                    assertEquals(DiffType.LEFTONLY, dc.getType());
                } else {
                    assertEquals (DiffType.SAME, dc.getType());
                }
            }
        }
    }

    /**
     * This test adds a primary key to the target table which is not added
     * to the source table. This will simulate a user adding a new column
     * to be the primary key of a table.
     */
    public void testKeyAddedNew() throws SQLObjectException{
        List<SQLTable> list1 = new ArrayList<SQLTable>();
        SQLTable t1 = makeTable(4);
        list1.add(t1);
        t1.removeColumn(3);

        List<SQLTable> list2 = new ArrayList<SQLTable>();
        SQLTable t2 = makeTable(4);
        list2.add(t2);
        SQLColumn c = t2.getColumn(3);
        t2.addToPK(c);

        CompareSQL sqlComparator = new CompareSQL(list1, list2, false);
        List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();


        boolean first_table = true;

        for (DiffChunk dc : diffs){
            if (dc.getData().getClass().equals(SQLTable.class)){
                if(first_table){
                    assertEquals (DiffType.SAME ,dc.getType());
                } else {
                    assertEquals (DiffType.KEY_CHANGED, dc.getType());
                }
                first_table = false;
            }
            else if (dc.getData().getClass().equals(SQLColumn.class)){
                if (((SQLObject) dc.getData()).getName().equals(c.getName())){
                    assertEquals(DiffType.RIGHTONLY, dc.getType());
                } else {
                    assertEquals (DiffType.SAME, dc.getType());
                }
            }
        }
    }

    /**
     * This test adds a primary key to the target table which is in
     * the source table. This will simulate a user setting an existing
     * column to be the primary key of a table.
     */
    public void testKeyAddedFromExisting() throws SQLObjectException{
        List<SQLTable> list1 = new ArrayList<SQLTable>();
        SQLTable t1 = makeTable(4);
        list1.add(t1);

        List<SQLTable> list2 = new ArrayList<SQLTable>();
        SQLTable t2 = makeTable(4);
        list2.add(t2);
        SQLColumn c = t2.getColumn(3);
        t2.addToPK(c);

        CompareSQL sqlComparator = new CompareSQL(list1, list2, false);
        List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();


        boolean first_table = true;

        for (DiffChunk dc : diffs){
            if (dc.getData().getClass().equals(SQLTable.class)){
                if(first_table){
                    assertEquals (DiffType.SAME ,dc.getType());
                } else {
                    assertEquals (DiffType.KEY_CHANGED, dc.getType());
                }
                first_table = false;
            }
            else if (dc.getData().getClass().equals(SQLColumn.class)){
                if (((SQLObject) dc.getData()).getName().equals(c.getName())){
                    assertEquals(DiffType.SQL_MODIFIED, dc.getType());
                } else {
                    assertEquals (DiffType.SAME, dc.getType());
                }
            }
        }
    }

    /**
     * This test changes the primary key of a table to be a different
     * column in the same table. This will simulate a user changing
     * the primary key of a table but not adding or removing any columns.
     */
    public void testKeyModified() throws SQLObjectException{
        List<SQLTable> list1 = new ArrayList<SQLTable>();
        SQLTable t1 = makeTable(4);
        list1.add(t1);
        SQLColumn c1 = t1.getColumn(3);
        t1.addToPK(c1);

        List<SQLTable> list2 = new ArrayList<SQLTable>();
        SQLTable t2 = makeTable(4);
        list2.add(t2);
        SQLColumn c2 = t2.getColumn(2);
        t2.addToPK(c2);

        CompareSQL sqlComparator = new CompareSQL(list1, list2, false);
        List<DiffChunk<SQLObject>> diffs = sqlComparator.generateTableDiffs();


        boolean first_table = true;

        for (DiffChunk dc : diffs){
            if (dc.getData().getClass().equals(SQLTable.class)){
                if(first_table){
                    assertEquals (DiffType.SAME ,dc.getType());
                } else if (dc.getData().equals(t1)) {
                    assertEquals(DiffType.DROP_KEY, dc.getType());
                } else {
                    assertEquals (DiffType.KEY_CHANGED, dc.getType());
                }
                first_table = false;
            }
            else if (dc.getData().getClass().equals(SQLColumn.class)){
                if (((SQLObject) dc.getData()).getName().equals(c1.getName()) || ((SQLObject) dc.getData()).getName().equals(c2.getName())){
                    assertEquals(DiffType.SQL_MODIFIED, dc.getType());
                } else {
                    assertEquals (DiffType.SAME, dc.getType());
                }
            }
        }
    }

	/**
	 * Creates a table with the name <tt>table_<i>i</i></tt> (where <i>i</i> is the
	 * argument given to this function.  The new table will have i columns called
	 * <tt>column_0</tt> .. <tt>column_<i>i-1</i></tt>.
	 *
	 * @param i The number of columns to give the new table (and also the suffix on its name)
	 * @return A new SQLTable instance.
	 * @throws SQLObjectException
	 */
	private SQLTable makeTable(int i) throws SQLObjectException {
		return makeTable(i,i);
	}
	private SQLTable makeTable(int tableNumber, int columnCount) throws SQLObjectException {
		SQLTable t = new SQLTable(null, "table_"+tableNumber, "remark on this", "TABLE", true);
		for (int j = 0; j < columnCount; j++) {
			t.addColumn(new SQLColumn(t, "column_"+j, Types.INTEGER, 3, 0));
		}
		return t;
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.CompareSchemaWorker.isFinished()'
	 */
	public void testIsFinished() {

	}


	/*
	 * Test method for 'ca.sqlpower.architect.swingui.CompareSchemaWorker.setLeftDiff(AbstractDocument)'
	 */
	public void testSetLeftDiff() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.CompareSchemaWorker.setRightDiff(AbstractDocument)'
	 */
	public void testSetRightDiff() {

	}

}
