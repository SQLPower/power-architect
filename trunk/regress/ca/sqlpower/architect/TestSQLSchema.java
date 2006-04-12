package regress.ca.sqlpower.architect;

import regress.ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class TestSQLSchema extends SQLTestCase {

	private SQLSchema s;
	
	public TestSQLSchema(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		s = new SQLSchema(true);
	}
	
	@Override
	protected SQLObject getSQLObjectUnderTest() {
		return s;
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getName()'
	 */
	public void testGetName() {
		assertNull(s.getName());
		
		SQLSchema s2 = new SQLSchema(s,"xxx",true);
		assertEquals("xxx",s2.getName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getParent()'
	 */
	public void testGetParent() {
		assertNull(s.getParent());
		
		SQLSchema s2 = new SQLSchema(s,"xxx",true);
		assertEquals(s,s2.getParent());
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.populate()'
	 */
	public void testPopulate() throws Exception {
		assertEquals(s.isPopulated(),true);
		s.populate();
		assertEquals(s.isPopulated(),true);
		s.setPopulated(false);
		assertEquals(s.isPopulated(),false);
		s.setPopulated(true);
		assertEquals(s.isPopulated(),true);
		
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									"xx",false);
		assertEquals(s2.isPopulated(),false);
		s2.populate();
		assertEquals(s2.isPopulated(),true);
		s2.setPopulated(false);
		assertEquals(s2.isPopulated(),false);
		s2.setPopulated(true);
		assertEquals(s2.isPopulated(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.allowsChildren()'
	 */
	public void testAllowsChildren() throws Exception {
		assertEquals(s.allowsChildren(),true);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									"xx",false);
		assertEquals(s2.allowsChildren(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getTableByName(String)'
	 */
	public void testGetTableByName() throws Exception {
		SQLTable t1 = s.getTableByName("REGRESSION_TEST1");
		assertNull(t1);
		
		t1 = new SQLTable();
		t1.setName("xx1");
		s.addChild(t1);
		
		t1 = new SQLTable();
		t1.setName("xx2");
		s.addChild(t1);
		
		t1 = new SQLTable();
		t1.setName("xx3");
		s.addChild(t1);
		
		t1 = new SQLTable();
		t1.setName("xx2");
		s.addChild(t1);
		
		t1 = s.getTableByName("xx1");
		assertNotNull(t1);
		assertEquals(t1.getName(),"xx1");
		
		t1 = s.getTableByName("xx2");
		assertNotNull(t1);
		assertEquals(t1.getName(),"xx2");
		
		t1 = s.getTableByName("xx3");
		assertNotNull(t1);
		assertEquals(t1.getName(),"xx3");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.isParentTypeDatabase()'
	 */
	public void testIsParentTypeDatabase() {
		assertEquals(s.isParentTypeDatabase(),false);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									"xx",false);
		assertEquals(s2.isParentTypeDatabase(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.setSchemaName(String)'
	 */
	public void testSetSchemaName() {
		assertNull(s.getName());
		s.setName("xx23");
		assertEquals(s.getName(),"xx23");
		
		SQLSchema s2 = new SQLSchema(s,"xxx",true);
		s2.setName("xx23");
		assertEquals(s2.getName(),"xx23");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getNativeTerm()'
	 */
	public void testGetNativeTerm() {
		assertEquals(s.getNativeTerm(),"schema");
		s.setNativeTerm(null);
		assertNull(s.getNativeTerm());
		s.setNativeTerm("AAA");
		assertEquals(s.getNativeTerm(),"aaa");
		
		SQLSchema s2 = new SQLSchema(s,"xxx",true);
		assertEquals(s2.getNativeTerm(),"schema");
		s2.setNativeTerm(null);
		assertNull(s2.getNativeTerm());
		s2.setNativeTerm("AAA");
		assertEquals(s2.getNativeTerm(),"aaa");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildren()'
	 */
	public void testGetChildren() throws Exception {
		int cnt = 0;
		assertEquals(0,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		
		s.addChild(new SQLTable(s,"","","TABLE", true));
		assertEquals(++cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		
		s.addChild(new SQLTable(s,"","","TABLE", true));
		assertEquals(++cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		
		s.addChild(new SQLTable(s,"","","TABLE", true));
		assertEquals(++cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		
		s.addChild(new SQLTable(s,"","","TABLE", true));
		assertEquals(++cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		
		s.addChild(new SQLTable(s,"","","TABLE", true));
		assertEquals(++cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		
		s.removeChild(0);
		assertEquals(--cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		s.removeChild(0);
		assertEquals(--cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		s.removeChild(0);
		assertEquals(--cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
		s.removeChild(0);
		assertEquals(--cnt,s.getChildren().size());
		assertEquals(cnt,s.getChildCount());
	}

	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChild(int)'
	 */
	public void testGetChild() throws Exception {
		for ( int i=0; i<5; i++ ) {
			s.addChild(new SQLTable(s,"","","TABLE", true));
		}
		
		assertEquals(5,s.getChildren().size());
		assertEquals(5,s.getChildCount());
		
		SQLTable t = (SQLTable) s.getChild(1);
		assertNotNull(t);
		assertTrue(t instanceof SQLTable);
		assertEquals(t.getName(),"" );
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(int, SQLObject)'
	 */
	public void testAddChildIntSQLObject() throws Exception {
		for ( int i=0; i<5; i++ ) {
			s.addChild(i,new SQLTable(s,"","","TABLE", true));
			assertEquals(i+1,s.getChildren().size());
			assertEquals(i+1,s.getChildCount());
		}
		SQLTable t = new SQLTable(s,"xxx","","TABLE", true);
		s.addChild(0,t);
		assertEquals(6,s.getChildren().size());
		assertEquals(6,s.getChildCount());
		
		s.removeChild(t);
		assertEquals(5,s.getChildren().size());
		assertEquals(5,s.getChildCount());
		
		for ( int i=4; i>=0; i-- ) {
			s.removeChild(0);
			assertEquals(i,s.getChildren().size());
			assertEquals(i,s.getChildCount());
		}
		
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(SQLObject)'
	 */
	public void testAddChildSQLObject() throws Exception {
		for ( int i=0; i<5; i++ ) {
			s.addChild(new SQLTable(s,"","","TABLE", true));
			assertEquals(i+1,s.getChildren().size());
			assertEquals(i+1,s.getChildCount());
		}
		
		SQLTable t = new SQLTable(s,"xxx","","TABLE", true);
		s.addChild(t);
		
		for ( int i=5; i>0; i-- ) {
			s.removeChild(0);
			assertEquals(i,s.getChildren().size());
			assertEquals(i,s.getChildCount());
		}
		
		s.removeChild(t);
		assertEquals(0,s.getChildren().size());
		assertEquals(0,s.getChildCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getSQLObjectListeners()'
	 */
	public void testGetSQLObjectListeners() throws Exception {
		TestSQLObjectListener test1 = new TestSQLObjectListener();
		TestSQLObjectListener test2 = new TestSQLObjectListener();
		
		s.addSQLObjectListener(test1);
		s.addSQLObjectListener(test2);
		
		assertEquals(s.getSQLObjectListeners().get(0),test1);
		assertEquals(s.getSQLObjectListeners().get(1),test2);
		
		for ( int i=0; i<5; i++ ) {
			s.addChild(new SQLTable(s,"","","TABLE", true));
		}
		
		assertEquals(test1.getInsertedCount(),5);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),5);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		s.removeSQLObjectListener(test2);
		
		for ( int i=0; i<5; i++ ) {
			s.removeChild(0);
		}
		
		assertEquals(test1.getInsertedCount(),5);
		assertEquals(test1.getRemovedCount(),5);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),5);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		assertEquals(s.getSQLObjectListeners().size(),1);
	}
}
