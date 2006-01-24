package regress.ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;

import regress.ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class TestSQLSchema extends SQLTestCase {

	public TestSQLSchema(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		

	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getName()'
	 */
	public void testGetName() {
		SQLSchema s1 = new SQLSchema(true);
		assertNull(s1.getName());
		
		SQLSchema s2 = new SQLSchema(s1,"xxx",true);
		assertEquals("xxx",s2.getName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getParent()'
	 */
	public void testGetParent() {
		SQLSchema s1 = new SQLSchema(true);
		assertNull(s1.getParent());
		
		SQLSchema s2 = new SQLSchema(s1,"xxx",true);
		assertEquals(s1,s2.getParent());
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.populate()'
	 */
	public void testPopulate() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.isPopulated(),true);
		s1.populate();
		assertEquals(s1.isPopulated(),true);
		s1.setPopulated(false);
		assertEquals(s1.isPopulated(),false);
		s1.setPopulated(true);
		assertEquals(s1.isPopulated(),true);
		
		
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
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.allowsChildren(),true);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									"xx",false);
		assertEquals(s2.allowsChildren(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getTableByName(String)'
	 */
	public void testGetTableByName() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		SQLTable t1 = s1.getTableByName("REGRESSION_TEST1");
		assertNull(t1);
		
		t1 = new SQLTable();
		t1.setName("xx1");
		s1.addChild(t1);
		
		t1 = new SQLTable();
		t1.setName("xx2");
		s1.addChild(t1);
		
		t1 = new SQLTable();
		t1.setName("xx3");
		s1.addChild(t1);
		
		t1 = new SQLTable();
		t1.setName("xx2");
		s1.addChild(t1);
		
		t1 = s1.getTableByName("xx1");
		assertNotNull(t1);
		assertEquals(t1.getName(),"xx1");
		
		t1 = s1.getTableByName("xx2");
		assertNotNull(t1);
		assertEquals(t1.getName(),"xx2");
		
		t1 = s1.getTableByName("xx3");
		assertNotNull(t1);
		assertEquals(t1.getName(),"xx3");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.isParentTypeDatabase()'
	 */
	public void testIsParentTypeDatabase() {
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.isParentTypeDatabase(),false);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									"xx",false);
		assertEquals(s2.isParentTypeDatabase(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.setSchemaName(String)'
	 */
	public void testSetSchemaName() {
		SQLSchema s1 = new SQLSchema(true);
		assertNull(s1.getName());
		s1.setSchemaName("xx23");
		assertEquals(s1.getSchemaName(),"xx23");
		
		SQLSchema s2 = new SQLSchema(s1,"xxx",true);
		s2.setSchemaName("xx23");
		assertEquals(s2.getSchemaName(),"xx23");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getNativeTerm()'
	 */
	public void testGetNativeTerm() {
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.getNativeTerm(),"schema");
		s1.setNativeTerm(null);
		assertNull(s1.getNativeTerm());
		s1.setNativeTerm("AAA");
		assertEquals(s1.getNativeTerm(),"aaa");
		
		SQLSchema s2 = new SQLSchema(s1,"xxx",true);
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
		SQLSchema s1 = new SQLSchema(true);
		int cnt = 0;
		assertEquals(0,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		
		s1.addChild(new SQLTable(s1,"","","TABLE"));
		assertEquals(++cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		
		s1.addChild(new SQLTable(s1,"","","TABLE"));
		assertEquals(++cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		
		s1.addChild(new SQLTable(s1,"","","TABLE"));
		assertEquals(++cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		
		s1.addChild(new SQLTable(s1,"","","TABLE"));
		assertEquals(++cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		
		s1.addChild(new SQLTable(s1,"","","TABLE"));
		assertEquals(++cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		
		s1.removeChild(0);
		assertEquals(--cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		s1.removeChild(0);
		assertEquals(--cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		s1.removeChild(0);
		assertEquals(--cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
		s1.removeChild(0);
		assertEquals(--cnt,s1.getChildren().size());
		assertEquals(cnt,s1.getChildCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setChildren(List)'
	 */
	public void testSetChildren() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		ArrayList tableList = new ArrayList();
		for ( int i=0; i<5; i++ ) {
			tableList.add(new SQLTable(s1,"","","TABLE"));
		}
		s1.setChildren(tableList);
		assertEquals(5,s1.getChildren().size());
		assertEquals(5,s1.getChildCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChild(int)'
	 */
	public void testGetChild() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		ArrayList tableList = new ArrayList();
		for ( int i=0; i<5; i++ ) {
			tableList.add(new SQLTable(s1,"","","TABLE"));
		}
		s1.setChildren(tableList);
		assertEquals(5,s1.getChildren().size());
		assertEquals(5,s1.getChildCount());
		
		SQLTable t = (SQLTable) s1.getChild(1);
		assertNotNull(t);
		assertTrue(t instanceof SQLTable);
		assertEquals(t.getName(),"" );
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(int, SQLObject)'
	 */
	public void testAddChildIntSQLObject() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		for ( int i=0; i<5; i++ ) {
			s1.addChild(i,new SQLTable(s1,"","","TABLE"));
			assertEquals(i+1,s1.getChildren().size());
			assertEquals(i+1,s1.getChildCount());
		}
		SQLTable t = new SQLTable(s1,"xxx","","TABLE");
		s1.addChild(0,t);
		assertEquals(6,s1.getChildren().size());
		assertEquals(6,s1.getChildCount());
		
		s1.removeChild(t);
		assertEquals(5,s1.getChildren().size());
		assertEquals(5,s1.getChildCount());
		
		for ( int i=4; i>=0; i-- ) {
			s1.removeChild(0);
			assertEquals(i,s1.getChildren().size());
			assertEquals(i,s1.getChildCount());
		}
		
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(SQLObject)'
	 */
	public void testAddChildSQLObject() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		for ( int i=0; i<5; i++ ) {
			s1.addChild(new SQLTable(s1,"","","TABLE"));
			assertEquals(i+1,s1.getChildren().size());
			assertEquals(i+1,s1.getChildCount());
		}
		
		SQLTable t = new SQLTable(s1,"xxx","","TABLE");
		s1.addChild(t);
		
		for ( int i=5; i>0; i-- ) {
			s1.removeChild(0);
			assertEquals(i,s1.getChildren().size());
			assertEquals(i,s1.getChildCount());
		}
		
		s1.removeChild(t);
		assertEquals(0,s1.getChildren().size());
		assertEquals(0,s1.getChildCount());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getSQLObjectListeners()'
	 */
	public void testGetSQLObjectListeners() throws Exception {
		TestSQLObjectListener test1 = new TestSQLObjectListener();
		TestSQLObjectListener test2 = new TestSQLObjectListener();
		SQLSchema s1 = new SQLSchema(true);
		
		s1.addSQLObjectListener(test1);
		s1.addSQLObjectListener(test2);
		
		assertEquals(s1.getSQLObjectListeners().get(0),test1);
		assertEquals(s1.getSQLObjectListeners().get(1),test2);
		
		for ( int i=0; i<5; i++ ) {
			s1.addChild(new SQLTable(s1,"","","TABLE"));
		}
		
		assertEquals(test1.getInsertedCount(),5);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),5);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		s1.removeSQLObjectListener(test2);
		
		for ( int i=0; i<5; i++ ) {
			s1.removeChild(0);
		}
		
		assertEquals(test1.getInsertedCount(),5);
		assertEquals(test1.getRemovedCount(),5);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),5);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		assertEquals(s1.getSQLObjectListeners().size(),1);
		

	}




}
