package ca.sqlpower.architect.diff;

import java.sql.Types;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class SQLRelationshipComparatorTest extends TestCase {
	
	SQLRelationshipComparator colComparator = new SQLRelationshipComparator(); 
	Comparator<SQLObject> comparator = new SQLObjectComparator();
	
	SQLTable table1L;
	SQLTable table2L;
	SQLRelationship left;
	
	protected void setUp() throws Exception {
		table1L = makeTable(1);
		table2L = makeTable(3);
		left= new SQLRelationship();
		table1L.getColumn(0).setPrimaryKeySeq(1);
		left.addMapping(table1L.getColumn(0),table2L.getColumn(1));
	}

	public void testCompareRelationShipWithOneNull() throws ArchitectException{		
		assertEquals ("The source is null, should be -1", -1, colComparator.compare(null, left));
		assertEquals ("The source is null, should be 1", 1, colComparator.compare(left,null));
	}
		
	public void testCompareSameRelationShip() throws ArchitectException{		
		SQLTable table1R = makeTable(1);
		SQLTable table2R = makeTable(3);
		SQLRelationship right = new SQLRelationship();
		table1R.getColumn(0).setPrimaryKeySeq(1);
		right.addMapping(table1R.getColumn(0),table2R.getColumn(1));
		assertEquals ("Should be same relationship", 0,colComparator.compare(left,right));
	}
	

	public void testCompareWithDifferentMappings() throws ArchitectException{		
		SQLTable table1R = makeTable(1);
		SQLTable table2R = makeTable(3);
		SQLRelationship right = new SQLRelationship();
		table1R.getColumn(0).setPrimaryKeySeq(1);
		right.addMapping(table1R.getColumn(0),table2R.getColumn(0));//Different mapping here
		assertNotSame ("Shouldn't be same relationship", 0,colComparator.compare(left,right));
	}
	
	public void testCompareWithExtraMapping() throws ArchitectException{
		SQLTable table1R = makeTable(1);
		SQLTable table2R = makeTable(3);
		SQLRelationship right = new SQLRelationship();
		table1R.getColumn(0).setPrimaryKeySeq(1);
		right.addMapping(table1R.getColumn(0),table2R.getColumn(1));
		right.addMapping(table1R.getColumn(0),table2R.getColumn(2));
		assertNotSame ("Shouldn't be same relationship", 0,colComparator.compare(left,right));
	}
	
	public void testCompareColumn () throws ArchitectException{
		Set<SQLColumn>list1 = generateColumnList(3);
		Set<SQLColumn>list2 = generateColumnList(3);
		assertEquals (0, colComparator.compareColumns(list1, list2));
		
		list1.add(new SQLColumn());
		assertEquals (1, colComparator.compareColumns(list1, list2));		
		assertEquals (-1, colComparator.compareColumns(list2, list1));
	}
	
	public Set<SQLColumn> generateColumnList(int num) throws ArchitectException{
		Set<SQLColumn> colList = new TreeSet<SQLColumn>(comparator);
		for (int ii=1; ii <= num; ii++){
			colList.add(new SQLColumn(new SQLTable(),"col"+ii,Types.INTEGER,3, 0)); 
		}		
		return colList;
	}
	
	private SQLTable makeTable(int i) throws ArchitectException {
		SQLTable t = new SQLTable(null, "table_"+i, "remark on this", "TABLE", true);
		for (int j = 0; j < i; j++) {
			t.addColumn(new SQLColumn(t, "column_"+j, Types.INTEGER, 3, 0));
		}
		return t;
	}
}
