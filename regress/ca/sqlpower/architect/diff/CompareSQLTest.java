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

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
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

	
	CompareSchemaWorker csw;
	DefaultStyledDocument r;
	DefaultStyledDocument l;
			
	TreeSet <SQLTable>  rTableContainer;
	SQLTable table1;
	SQLTable table1LookAlike;
	SQLTable table2;
	SQLTable table3;
	SQLTable tableNoColumn1;
	SQLTable tableNoColumn2;
	SQLTable tableNoColumn3;
	SQLTable table1NoColumn;
	SQLColumn c1; 
	SQLColumn c1Dupl;
	SQLColumn c2;
	SQLColumn c2LookAlike;
	SQLColumn c3; 
	SQLColumn c4;
	SQLColumn c5;
	SQLColumn c6;
	List <SQLTable> listWithATable;
	List <SQLTable> temp2;

	
	private Map<Integer, GenericTypeDescriptor> typeMap;
	
	protected void setUp() throws Exception {
		//Just tables with Columns
		tableNoColumn1 = new SQLTable(null,"table1" , "...",SQLTable.class.toString(), true);		
		tableNoColumn2 = new SQLTable(null,"table2" , "...",SQLTable.class.toString(), true);
		tableNoColumn3 = new SQLTable(null,"table3" , "...",SQLTable.class.toString(), true);
		table1NoColumn = new SQLTable(null,"tableWithColumn1","it's lying!", 
				SQLTable.class.toString(), true);
		
		//Table with two columns
		table1 = new SQLTable(null,"tableWithColumn1","actually r1",
							SQLTable.class.toString(),true);
		//The Column specs will need to be changed (scale, type, precision)		
		c1 = new SQLColumn(table1, "Column1", 0,2,3);
	    c2 = new SQLColumn(table1, "Column2", Types.INTEGER,5,0);
		table1.addColumn(c1);
		table1.addColumn(c2);
		
		//Similar to table 1 with minor changes
		table1LookAlike = new SQLTable(null,"tableWithColumn1","actually r1lookAlike",
									SQLTable.class.toString(),true);
		c1Dupl = new SQLColumn(table1LookAlike, "Column1", 0,2,3);
		c2LookAlike = new SQLColumn (table1LookAlike, "Column2", 1, 3,5);
		table1LookAlike.addColumn(c1Dupl);
		table1LookAlike.addColumn(c2LookAlike);
		
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
		CompareSQL worker1 = new CompareSQL((Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList2);
		List<DiffChunk<SQLObject>> tableWithColumnAndNothing = worker1.generateTableDiffs();
		assertEquals (3,tableWithColumnAndNothing.size());
		
		assertEquals (DiffType.LEFTONLY, tableWithColumnAndNothing.get(0).getType());
		assertEquals (SQLTable.class, tableWithColumnAndNothing.get(0).getData().getClass());
		assertEquals ("tableWithColumn1", tableWithColumnAndNothing.get(0).getData().getName());
		
		assertEquals (DiffType.LEFTONLY, tableWithColumnAndNothing.get(1).getType());
		assertEquals (SQLColumn.class, tableWithColumnAndNothing.get(1).getData().getClass());
		assertEquals ("Column1", tableWithColumnAndNothing.get(1).getData().getName());
		
		assertEquals (DiffType.LEFTONLY, tableWithColumnAndNothing.get(2).getType());
		assertEquals (SQLColumn.class, tableWithColumnAndNothing.get(2).getData().getClass());
		assertEquals ("Column2", tableWithColumnAndNothing.get(2).getData().getName());
		
		
		
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
		tableList1.add(table2);
		tableList2.add(table1LookAlike);
		tableList2.add(table3);
		
		CompareSQL worker4 = new CompareSQL((Collection<SQLTable>)tableList1,
				(Collection<SQLTable>)tableList2);
		List<DiffChunk<SQLObject>> manyProperties = worker4.generateTableDiffs();
		assertEquals (9, manyProperties.size());
		
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
		
		assertEquals (DiffType.LEFTONLY, manyProperties.get(4).getType());
		assertEquals (SQLColumn.class, manyProperties.get(4).getData().getClass());
		assertEquals ("Column3", manyProperties.get(4).getData().getName());
		
		assertEquals (DiffType.LEFTONLY, manyProperties.get(5).getType());
		assertEquals (SQLColumn.class, manyProperties.get(5).getData().getClass());
		assertEquals ("Column3a", manyProperties.get(5).getData().getName());
		
		assertEquals (DiffType.RIGHTONLY, manyProperties.get(6).getType());
		assertEquals (SQLTable.class, manyProperties.get(6).getData().getClass());
		assertEquals ("tableWithColumn3", manyProperties.get(6).getData().getName());
		
		assertEquals (DiffType.RIGHTONLY, manyProperties.get(7).getType());
		assertEquals (SQLColumn.class, manyProperties.get(7).getData().getClass());
		assertEquals ("Column3a", manyProperties.get(7).getData().getName());
		
		assertEquals (DiffType.RIGHTONLY, manyProperties.get(8).getType());
		assertEquals (SQLColumn.class, manyProperties.get(8).getData().getClass());
		assertEquals ("Column4", manyProperties.get(8).getData().getName());

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
