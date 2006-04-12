package regress.ca.sqlpower.architect.diff;

import java.util.Comparator;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.diff.SQLObjectComparator;

public class SQLComparatorTest extends TestCase {
	Comparator<SQLObject> comparator = new SQLObjectComparator();

	SQLObject o1 = new SQLObject() {

		@Override
		public SQLObject getParent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void setParent(SQLObject parent) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void populate() throws ArchitectException {
			// TODO Auto-generated method stub

		}

		@Override
		public String getShortDisplayName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean allowsChildren() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Class<? extends SQLObject> getChildType() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	public void testForNull() {
		assertEquals(0, comparator.compare(null, null));
		assertEquals(0, comparator.compare(o1, o1));
	}

	public void testForConsistentWithEquals() {
		// assertEquals(comparator.compare((Object)e1, (Object)e2)==0),
		// e1.equals((Object)e2)
	}
	
	public void testForObjectCompareToNull(){
		SQLTable t = new SQLTable();
		t.setName("Testing");
		assertEquals (1, comparator.compare(t, null));
		assertEquals (-1, comparator.compare(null, t));		
	}
	
	public void testForObjectCompareToObject(){
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		t1.setName("cow");
		t2.setName("pigs");
		assertTrue( comparator.compare(t1,t2) < 0);
	
		
	}
	
	public void testWithNullName() {
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		assertEquals(0, comparator.compare(t1,t2));		
	}
	
	public void testWithSameName(){
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		t1.setName("cow");
		t2.setName("cow");
		assertEquals( 0, comparator.compare(t1,t2));
	}
}
