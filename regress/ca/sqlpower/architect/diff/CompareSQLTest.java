package regress.ca.sqlpower.architect.diff;

import java.lang.reflect.Array;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import junit.extensions.xml.elements.AssertNotEqualsTagHandler;
import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericTypeDescriptor;
import ca.sqlpower.architect.diff.CompareSQL;
import ca.sqlpower.architect.diff.DiffChunk;
import ca.sqlpower.architect.diff.DiffType;
import ca.sqlpower.architect.diff.SQLObjectComparator;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.CompareSchemaWorker;
import ca.sqlpower.architect.swingui.PlayPen;

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

	
	private Map<Integer, GenericTypeDescriptor> typeMap;
	
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
	
	public void testDiffListWithTablesOnly() throws ArchitectException{
		//Testing diffchunk with nothing and nothing
		List <SQLTable> list1 = new ArrayList();
		
		CompareSQL compare1 = new CompareSQL ((Collection<SQLTable>)list1,
												(Collection<SQLTable>)list1);
		List<DiffChunk<SQLObject>> nullChecker = compare1.generateTableDiffs();
		assertEquals (0, nullChecker.size());
		
		
		//Testing diff chunk with one table and nothing;
		CompareSQL compareWorker = new CompareSQL((Collection<SQLTable>)listWithATable,
													(Collection<SQLTable>)list1);
		List<DiffChunk<SQLObject>> tableAndNull = compareWorker.generateTableDiffs();
		assertEquals (1,tableAndNull.size());
		assertEquals (DiffType.LEFTONLY, tableAndNull.get(0).getType());
		assertEquals ("table1", tableAndNull.get(0).getData().getName());
	
		
		//Testing diff chunk with two list that has the same properties
		CompareSQL compareWorker1 = new CompareSQL((Collection<SQLTable>)listWithATable,
													(Collection<SQLTable>)listWithATable);
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
				(Collection<SQLTable>)list1);
		List<DiffChunk<SQLObject>> differentProp = compareWorker2.generateTableDiffs();
		assertEquals (3,differentProp.size());
		
		assertEquals (DiffType.SAME, differentProp.get(0).getType());
		assertEquals ("table1", differentProp.get(0).getData().getName());
		
		assertEquals (DiffType.RIGHTONLY, differentProp.get(1).getType());
		assertEquals ("table2", differentProp.get(1).getData().getName());
		
		assertEquals (DiffType.LEFTONLY, differentProp.get(2).getType());			
		assertEquals ("table3", differentProp.get(2).getData().getName());		
	}
	
	
	public void testDiffListWithTableHavingColumn() throws ArchitectException{
		List <SQLTable >tableList1 = new ArrayList();
		List <SQLTable >tableList2 = new ArrayList();
		
		//Testing table with column and nothing
		tableList1.add(table1);
		CompareSQL worker1 = new CompareSQL(
				(Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList2);
		List<DiffChunk<SQLObject>> tableWithColumnAndNothing = worker1.generateTableDiffs();
		assertEquals (1,tableWithColumnAndNothing.size());
		
		assertEquals (DiffType.LEFTONLY, tableWithColumnAndNothing.get(0).getType());
		assertEquals (SQLTable.class, tableWithColumnAndNothing.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", tableWithColumnAndNothing.get(0).getData().getName());
		
		
		//Testing tables with the same column 
		CompareSQL worker2 = new CompareSQL((Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList1);
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
		List<SQLTable>tempList = new ArrayList();
		tempList.add(table1NoColumn);
		CompareSQL worker3 = new CompareSQL((Collection<SQLTable>)tempList,
				(Collection<SQLTable>)tableList1);
		List<DiffChunk<SQLObject>> diffTest = worker3.generateTableDiffs();
		
		assertEquals (3, diffTest.size());
		assertEquals (DiffType.SAME, diffTest.get(0).getType());
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
				(Collection<SQLTable>)tableList2);
		List<DiffChunk<SQLObject>> manyProperties = worker4.generateTableDiffs();
		System.out.println("-/|\\-"+manyProperties+"-/|\\-");
		assertEquals (5, manyProperties.size());
		
		assertEquals (DiffType.SAME, manyProperties.get(0).getType());
		assertEquals (SQLTable.class, manyProperties.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", manyProperties.get(0).getData().getName());
		
		assertEquals (DiffType.SAME, manyProperties.get(1).getType());
		assertEquals (SQLColumn.class, manyProperties.get(1).getData().getClass());
		assertEquals ("Column1", manyProperties.get(1).getData().getName());
		
		assertEquals (DiffType.MODIFIED, manyProperties.get(2).getType());
		assertEquals (SQLColumn.class, manyProperties.get(2).getData().getClass());
		assertEquals ("Column2", manyProperties.get(2).getData().getName());
		
		assertEquals (DiffType.LEFTONLY, manyProperties.get(3).getType());
		assertEquals (SQLTable.class, manyProperties.get(3).getData().getClass());
		assertEquals ("tableWithColumn2", manyProperties.get(3).getData().getName());
		
		assertEquals (DiffType.RIGHTONLY, manyProperties.get(4).getType());
		assertEquals (SQLTable.class, manyProperties.get(4).getData().getClass());
		assertEquals ("tableWithColumn3", manyProperties.get(4).getData().getName());

	}
	
	public void testTableWithRelationShip() throws ArchitectException{
		SQLTable newTable1 = table1;
		SQLTable newTable2 = table2;
		SQLRelationship relation1 = new SQLRelationship();
		relation1.addMapping(newTable1.getColumn(0), newTable2.getColumn(1));
		relation1.setName("relation1");
		
		newTable1.addExportedKey(relation1);
		newTable2.addImportedKey(relation1);

		List<SQLTable> newList1 = new ArrayList<SQLTable>();
		List<SQLTable> newList2 = new ArrayList<SQLTable>();
		
		newList1.add(newTable1);
		newList1.add(newTable2);
		
		CompareSQL worker1 = new CompareSQL((Collection<SQLTable>)newList1,
				(Collection<SQLTable>)newList2);
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
	
	public void testRelationshipsWithSameMappings() throws ArchitectException {

		// Set up source (left hand side) envorinment
		SQLTable newTable1L = makeTable(4);
		SQLTable newTable2L = makeTable(6);
		SQLRelationship relationL = new SQLRelationship();
		relationL.addMapping(newTable1L.getColumn(0), newTable2L.getColumn(1));
		relationL.setName("relation1");
		
		
		newTable1L.addExportedKey(relationL);
		newTable2L.addImportedKey(relationL);
		
		List<SQLTable> tableListL = new ArrayList<SQLTable>();
		tableListL.add(newTable1L);
		tableListL.add(newTable2L);
		
		// Set up source (left hand side) envorinment
		SQLTable newTable1R = makeTable(4);
		SQLTable newTable2R = makeTable(6);
		SQLRelationship relationR = new SQLRelationship();
		relationR.addMapping(newTable1R.getColumn(0), newTable2R.getColumn(1));
		relationR.setName("relation1");
		
		newTable1R.addExportedKey(relationR);
		newTable2R.addImportedKey(relationR);
		
		List<SQLTable> tableListR = new ArrayList<SQLTable>();
		tableListR.add(newTable1R);
		tableListR.add(newTable2R);
		
		CompareSQL cs = new CompareSQL(tableListL, tableListR);
		List<DiffChunk<SQLObject>> diffs = cs.generateTableDiffs();
		
		for (DiffChunk<SQLObject> chunk : diffs) {
			assertEquals(
					"Left side == Right side. Diff list should be all same",
					DiffType.SAME, chunk.getType());
		}
	}
	
	public void testRelationshipsWithDifferentNames() throws ArchitectException {

		// Set up source (left hand side) envorinment
		SQLTable newTable1L = makeTable(4);
		SQLTable newTable2L = makeTable(6);
		SQLRelationship relationL = new SQLRelationship();
		relationL.addMapping(newTable1L.getColumn(0), newTable2L.getColumn(1));
		relationL.setName("relation1");
		
		newTable1L.addExportedKey(relationL);
		newTable2L.addImportedKey(relationL);
		
		List<SQLTable> tableListL = new ArrayList<SQLTable>();
		tableListL.add(newTable1L);
		tableListL.add(newTable2L);
		
		// Set up source (left hand side) envorinment
		SQLTable newTable1R = makeTable(4);
		SQLTable newTable2R = makeTable(6);
		SQLRelationship relationR = new SQLRelationship();
		relationR.addMapping(newTable1R.getColumn(0), newTable2R.getColumn(1));
		relationR.setName("not_relation1");
		
		newTable1R.addExportedKey(relationR);
		newTable2R.addImportedKey(relationR);
		
		List<SQLTable> tableListR = new ArrayList<SQLTable>();
		tableListR.add(newTable1R);
		tableListR.add(newTable2R);
		
		CompareSQL cs = new CompareSQL(tableListL, tableListR);
		List<DiffChunk<SQLObject>> diffs = cs.generateTableDiffs();
		
		for (DiffChunk<SQLObject> chunk : diffs) {
			if (chunk.getData().getClass().equals(SQLRelationship.class)) {
				//We now do not mind if the relationships have the same name
				//We just consider the mappings
				assertEquals("The relationships have different names",
						DiffType.SAME, chunk.getType());
			} else {
				assertEquals(
						"Diff list should be all same for non-relationship SQLObjects",
						DiffType.SAME, chunk.getType());
			}
		}
	}

	public void testRelationshipsWithDifferentMappings() throws ArchitectException {

		// Set up source (left hand side) envorinment
		SQLTable newTable1L = makeTable(4);
		SQLTable newTable2L = makeTable(6);
		SQLRelationship relationL = new SQLRelationship();		
		//This is done because the architect requires imported key to be in the primary key
		newTable1L.getColumn(0).setPrimaryKeySeq(1); 
		relationL.addMapping(newTable1L.getColumn(0), newTable2L.getColumn(2));  // this is the difference
		relationL.setName("relation1");

		
		newTable1L.addExportedKey(relationL);
		newTable2L.addImportedKey(relationL);
		
		List<SQLTable> tableListL = new ArrayList<SQLTable>();
		tableListL.add(newTable1L);
		tableListL.add(newTable2L);
		
		// Set up source (left hand side) envorinment
		SQLTable newTable1R = makeTable(4);
		SQLTable newTable2R = makeTable(6);		
		SQLRelationship relationR = new SQLRelationship();
		newTable1R.getColumn(0).setPrimaryKeySeq(1);
		relationR.addMapping(newTable1R.getColumn(0), newTable2R.getColumn(1));
		relationR.setName("relation1");
		
		newTable1R.addExportedKey(relationR);
		newTable2R.addImportedKey(relationR);
		
		List<SQLTable> tableListR = new ArrayList<SQLTable>();
		tableListR.add(newTable1R);
		tableListR.add(newTable2R);
		
		CompareSQL cs = new CompareSQL(tableListL, tableListR);
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
	
	public void testKeyChanged() throws ArchitectException{			
		List<SQLTable> list1 = new ArrayList<SQLTable>();
		SQLTable t1 = makeTable(4);
		list1.add(t1);
		SQLColumn c = t1.getColumn(2); 
		c.setPrimaryKeySeq(new Integer(0));

		List<SQLTable> list2 = new ArrayList<SQLTable>();
		SQLTable t2 = makeTable(4);
		list2.add(t2);
				
		CompareSQL sqlComparator = new CompareSQL(list1, list2);
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
					assertEquals(DiffType.MODIFIED, dc.getType());
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
	 * @throws ArchitectException 
	 */
	private SQLTable makeTable(int i) throws ArchitectException {
		SQLTable t = new SQLTable(null, "table_"+i, "remark on this", "TABLE", true);
		for (int j = 0; j < i; j++) {
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
