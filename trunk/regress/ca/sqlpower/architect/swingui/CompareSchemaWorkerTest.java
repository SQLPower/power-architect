package regress.ca.sqlpower.architect.swingui;

import java.util.TreeSet;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
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
		
		r = new DefaultStyledDocument();
		l = new DefaultStyledDocument();
		
		csw = new CompareSchemaWorker(rTableContainer,lTableContainer, r,l,new GenericDDLGenerator(), new GenericDDLGenerator(), true);
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
					new GenericDDLGenerator(), new GenericDDLGenerator(), true);		
		emptyWorker.run();				
		assertEquals (0, rDoc.getLength());
		assertEquals (0, lDoc.getLength());
		
		
		
		//Source has no tables, target does
		CompareSchemaWorker worker1 = new CompareSchemaWorker (ltree,rTableContainer, lDoc,rDoc, 
					new GenericDDLGenerator(), new GenericDDLGenerator(), true);
		worker1.run();				
		assertEquals ("Missing table: a\nMissing table: b\n",lDoc.getText(0, lDoc.getLength()));
		assertEquals ("Extra table: a\nExtra table: b\n",rDoc.getText(0, rDoc.getLength()));
				
		//Source has tables, target does not
		DefaultStyledDocument rDoc2 = new DefaultStyledDocument();
		DefaultStyledDocument lDoc2 = new DefaultStyledDocument();
		CompareSchemaWorker worker2 = new CompareSchemaWorker (lTableContainer,rtree, lDoc2,rDoc2,
					new GenericDDLGenerator(), new GenericDDLGenerator(), true);
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
				new GenericDDLGenerator(), new GenericDDLGenerator(), true);		
		
		workerNoColAndCols.run();

		assertEquals ("Missing table: a\n\tMissing column: Column1\n\tMissing column: Column2\n", lDoc.getText(0,lDoc.getLength()));
		assertEquals ("Extra table: a\n\tExtra column: Column1\n\tExtra column: Column2\n", rDoc.getText(0,rDoc.getLength()));
		
		DefaultStyledDocument rDocTest2 = new DefaultStyledDocument();
		DefaultStyledDocument lDocTest2 = new DefaultStyledDocument();
		
		CompareSchemaWorker workerColAndNoCols = new CompareSchemaWorker (tree1, tree2, lDocTest2, rDocTest2, 
				new GenericDDLGenerator(), new GenericDDLGenerator(), true);
		workerColAndNoCols.run();
		assertEquals ("Missing table: a\n\tMissing column: Column1\n\tMissing column: Column2\n", rDocTest2.getText(0,rDocTest2.getLength()));
		assertEquals ("Extra table: a\n\tExtra column: Column1\n\tExtra column: Column2\n", lDocTest2.getText(0,lDocTest2.getLength()));
		
		
		tree1.add(table2);
		tree2.add(table3);
		tree2.add(table4);
		
		DefaultStyledDocument rDoc3 = new DefaultStyledDocument();
		DefaultStyledDocument lDoc3 = new DefaultStyledDocument();
		CompareSchemaWorker workerColAndCol = new CompareSchemaWorker (tree1, tree2, lDoc3, rDoc3, 
				new GenericDDLGenerator(), new GenericDDLGenerator(), true);
		workerColAndCol.run();
		assertEquals ("Extra table: a\n\tExtra column: Column1\n\tExtra column: Column2\nSame table: b\n\t" +
				"Same column: Column3\n\tExtra column: Column3a\n\tMissing column: Column3b\nMissing table: " + 
				"c\n\tMissing column: Column4\n", lDoc3.getText(0,lDoc3.getLength()));
		assertEquals ("Missing table: a\n\tMissing column: Column1\n\tMissing column: Column2\nSame table: b\n\t" +
				"Same column: Column3\n\tMissing column: Column3a\n\tExtra column: Column3b\nExtra table: " + 
				"c\n\tExtra column: Column4\n", rDoc3.getText(0,rDoc3.getLength()));
		
		
		
		
		
		
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
