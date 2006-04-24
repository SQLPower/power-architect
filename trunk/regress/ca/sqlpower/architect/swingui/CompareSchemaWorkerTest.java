package regress.ca.sqlpower.architect.swingui;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericTypeDescriptor;
import ca.sqlpower.architect.swingui.CompareSchemaWorker;
import ca.sqlpower.architect.swingui.SQLObjectCompare;

public class CompareSchemaWorkerTest extends TestCase {

	
	CompareSchemaWorker csw;
	DefaultStyledDocument r;
	DefaultStyledDocument l;
			
	TreeSet <SQLTable>  rTableContainer;
	SQLTable r1;
	SQLTable r2;
	TreeSet <SQLTable> lTableContainer;
	SQLTable l1;
	SQLTable l2;
	
	private Map<Integer, GenericTypeDescriptor> typeMap;
	
	protected void setUp() throws Exception {
		super.setUp();		
		rTableContainer = new TreeSet<SQLTable>(new SQLObjectCompare());
		r1 = new SQLTable(null,"a","actually r1",SQLTable.class.toString(),true);
		r2 = new SQLTable(null,"b","actually r2",SQLTable.class.toString(),true);	
		rTableContainer.add(r1);
		rTableContainer.add(r2);
		
		lTableContainer = new TreeSet<SQLTable>(new SQLObjectCompare());
		l1 = new SQLTable(null,"c","actually l1",SQLTable.class.toString(),true);
		l2 = new SQLTable(null,"b","actually l2",SQLTable.class.toString(),true);
		lTableContainer.add(l1);
		lTableContainer.add(l2);
		typeMap = new HashMap<Integer, GenericTypeDescriptor> ();
		typeMap.put(new Integer(0),new GenericTypeDescriptor("MockInteger",0,1,"prefix","suffix",0,false,false));
		typeMap.put(new Integer(1),new GenericTypeDescriptor("Precision",1,1,"prefix","suffix",0,true,false));
		
		r = new DefaultStyledDocument();
		l = new DefaultStyledDocument();
		
		csw = new CompareSchemaWorker(rTableContainer,lTableContainer, r,l,typeMap,typeMap);
		csw.run();
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}



	/*
	 * Test method for 'ca.sqlpower.architect.swingui.CompareSchemaWorker.getJobSize()'
	 */
	public void testGetJobSize() {
		assertEquals (4,csw.getJobSize());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.CompareSchemaWorker.getProgress()'
	 */
	public void testGetProgress() {

	}
	
	public void testGenerateDiffWithNoColunmns() throws BadLocationException{
		DefaultStyledDocument rDoc = new DefaultStyledDocument();
		DefaultStyledDocument lDoc = new DefaultStyledDocument();
		
		TreeSet <SQLTable>ltree = new TreeSet(new SQLObjectCompare());
		TreeSet <SQLTable>rtree = new TreeSet(new SQLObjectCompare());
		//Both source and target no tables
		CompareSchemaWorker emptyWorker = new CompareSchemaWorker(ltree,rtree, lDoc,rDoc,
					typeMap,typeMap);		
		emptyWorker.run();				
		assertEquals (0, rDoc.getLength());
		assertEquals (0, lDoc.getLength());
		
		
		
		//Source has no tables, target does
		CompareSchemaWorker worker1 = new CompareSchemaWorker (ltree,rTableContainer, lDoc,rDoc, 
					typeMap,typeMap);
		worker1.run();				
		assertEquals ("Missing table: a\nMissing table: b\n",lDoc.getText(0, lDoc.getLength()));
		assertEquals ("Extra table: a\nExtra table: b\n",rDoc.getText(0, rDoc.getLength()));
				
		//Source has tables, target does not
		DefaultStyledDocument rDoc2 = new DefaultStyledDocument();
		DefaultStyledDocument lDoc2 = new DefaultStyledDocument();
		CompareSchemaWorker worker2 = new CompareSchemaWorker (lTableContainer,rtree, lDoc2,rDoc2,
					typeMap,typeMap);
		worker2.run();				
		assertEquals ("Extra table: b\nExtra table: c\n",lDoc2.getText(0, lDoc2.getLength()));
		assertEquals ("Missing table: b\nMissing table: c\n",rDoc2.getText(0, rDoc2.getLength()));
		
		//Both source and target have tables		
		assertEquals ("Missing table: a\nSame table: b\nExtra table: c\n",l.getText(0, l.getLength()) );
		assertEquals ("Extra table: a\nSame table: b\nMissing table: c\n",r.getText(0, r.getLength()) );
	}

	public void testGenerateDiffWithColumns() throws ArchitectException, BadLocationException{
		TreeSet<SQLTable> tree1 = new TreeSet<SQLTable>(new SQLObjectCompare());
		TreeSet<SQLTable> tree2 = new TreeSet<SQLTable>(new SQLObjectCompare());
		DefaultStyledDocument rDoc = new DefaultStyledDocument();
		DefaultStyledDocument lDoc = new DefaultStyledDocument();
		
		
		
		SQLTable table1 = new SQLTable(null,"a","actually r1",SQLTable.class.toString(),true);
		//The Column specs will need to be changed (scale, type, precision)		
		SQLColumn c1 = new SQLColumn(table1, "Column1", 0,2,3);
		SQLColumn c2 = new SQLColumn(table1, "Column2", 0,2,3);
		table1.addColumn(c1);
		table1.addColumn(c2);
		
		SQLTable table2 = new SQLTable(null, "b", "actually r2",SQLTable.class.toString(),true); 
		SQLColumn c3 = new SQLColumn(table2, "Column3", 0,2,3);
		SQLColumn c4 = new SQLColumn(table2, "Column3a", 0,2,3);
		table2.addColumn(c3);
		table2.addColumn(c4);
		
		SQLTable table3 = new SQLTable(null, "b", "actually r2",SQLTable.class.toString(),true);
		SQLColumn c5 = new SQLColumn (table3, "Column3",0,2,3);
		SQLColumn c6 = new SQLColumn (table3, "Column3b", 0,2,3);
		table3.addColumn(c5);
		table3.addColumn(c6);
		
		SQLTable table4 = new SQLTable(null, "c", "actually l3",SQLTable.class.toString(),true);
		SQLColumn c7 = new SQLColumn (table4, "Column4",0,2,3);
		table4.addColumn(c7);

		
		tree1.add(table1);		
		
		CompareSchemaWorker workerNoColAndCols = new CompareSchemaWorker(tree2, tree1, lDoc, rDoc, 
				typeMap,typeMap);		
		
		workerNoColAndCols.run();

		assertEquals ("Missing table: a\n\tMissing column: Column1: MockInteger\n\tMissing column: Column2: MockInteger\n", lDoc.getText(0,lDoc.getLength()));
		assertEquals ("Extra table: a\n\tExtra column: Column1: MockInteger\n\tExtra column: Column2: MockInteger\n", rDoc.getText(0,rDoc.getLength()));
		
		DefaultStyledDocument rDocTest2 = new DefaultStyledDocument();
		DefaultStyledDocument lDocTest2 = new DefaultStyledDocument();
		
		CompareSchemaWorker workerColAndNoCols = new CompareSchemaWorker (tree1, tree2, lDocTest2, rDocTest2, 
				typeMap,typeMap);
		workerColAndNoCols.run();
		assertEquals ("Missing table: a\n\tMissing column: Column1: MockInteger\n\tMissing column: Column2: MockInteger\n", rDocTest2.getText(0,rDocTest2.getLength()));
		assertEquals ("Extra table: a\n\tExtra column: Column1: MockInteger\n\tExtra column: Column2: MockInteger\n", lDocTest2.getText(0,lDocTest2.getLength()));
		
		
		tree1.add(table2);
		tree2.add(table3);
		tree2.add(table4);
		
		DefaultStyledDocument rDoc3 = new DefaultStyledDocument();
		DefaultStyledDocument lDoc3 = new DefaultStyledDocument();
		CompareSchemaWorker workerColAndCol = new CompareSchemaWorker (tree1, tree2, lDoc3, rDoc3, 
				typeMap,typeMap );
		workerColAndCol.run();
		assertEquals ("Extra table: a\n\tExtra column: Column1: MockInteger\n\tExtra column: Column2: MockInteger\nSame table: b\n\t" +
				"Same column: Column3: MockInteger\n\tExtra column: Column3a: MockInteger\n\tMissing column: Column3b: MockInteger\nMissing table: " + 
				"c\n\tMissing column: Column4: MockInteger\n", lDoc3.getText(0,lDoc3.getLength()));
		assertEquals ("Missing table: a\n\tMissing column: Column1: MockInteger\n\tMissing column: Column2: MockInteger\nSame table: b\n\t" +
				"Same column: Column3: MockInteger\n\tMissing column: Column3a: MockInteger\n\tExtra column: Column3b: MockInteger\nExtra table: " + 
				"c\n\tExtra column: Column4: MockInteger\n", rDoc3.getText(0,rDoc3.getLength()));
		
		TreeSet <SQLTable> tree3 = new TreeSet<SQLTable>(new SQLObjectCompare());
		TreeSet <SQLTable> tree4 = new TreeSet<SQLTable>(new SQLObjectCompare());
		
		DefaultStyledDocument rTest = new DefaultStyledDocument();
		DefaultStyledDocument lTest = new DefaultStyledDocument();
		
		SQLTable t1 = new SQLTable(null, "x", "test x",SQLTable.class.toString(),true);
		SQLColumn col = new SQLColumn (t1, "test",0,2,3);		
		t1.addColumn(col);		
		
		SQLTable t2 = new SQLTable(null, "x", "test x",SQLTable.class.toString(),true);
		SQLColumn col2 = new SQLColumn (t2, "test",1,3,3);		
		t2.addColumn(col2);
		
		SQLTable t3 = new SQLTable (null, "z", "test x", SQLTable.class.toString(), true);
		SQLColumn col3 = new SQLColumn (t3, "test 1", 0,2,3);
		t3.addColumn(col3);
		
		tree3.add(t1);
		tree4.add(t2);
		tree4.add(t3);
		
		CompareSchemaWorker testDiffColProp = new CompareSchemaWorker(tree3, tree4, lTest, rTest, typeMap, typeMap);
		testDiffColProp.run();
		assertEquals ("Same table: x\n\tModify column test from type: MockInteger to type: Precision(3)\n" +
				"Missing table: z\n\tMissing column: test 1: MockInteger\n", lTest.getText(0,lTest.getLength()));
		assertEquals ("Same table: x\n\tModify column test from type: Precision(3) to type: MockInteger\n" +
				"Extra table: z\n\tExtra column: test 1: MockInteger\n", rTest.getText(0,rTest.getLength()));
		
		
		
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
