package regress.ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLData;
import java.util.ArrayList;
import java.util.Iterator;

import regress.ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class TestSQLCatalog extends SQLTestCase {

	public TestSQLCatalog(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getName()'
	 */
	public void testGetName() {
		SQLCatalog c = new SQLCatalog();
		assertNull(c.getName());
		c.setCatalogName("xxx");
		assertEquals(c.getName(),"xxx");
		
		c.setCatalogName("yyy");
		assertEquals(c.getName(),"yyy");
		assertEquals(c.getCatalogName(),"yyy");
		assertEquals(c.getShortDisplayName(),"yyy");
		assertEquals(c.toString(),"yyy");
		
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertEquals(c.getName(),"aaa");
		assertEquals(c.getCatalogName(),"aaa");
		assertEquals(c.getShortDisplayName(),"aaa");
		assertEquals(c.toString(),"aaa");
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getParent()'
	 */
	public void testGetParent() {
		SQLCatalog c = new SQLCatalog();
		assertEquals(c.getParent(),null);
		assertEquals(c.getParentDatabase(),null);
		
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertEquals(c.getParent(),mydb);
		assertEquals(c.getParentDatabase(),mydb);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.allowsChildren()'
	 */
	public void testAllowsChildren() {
		SQLCatalog c = new SQLCatalog();
		assertTrue(c.allowsChildren());
		
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertTrue(c.allowsChildren());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.isSchemaContainer()'
	 */
	public void testIsSchemaContainer() throws Exception {
		SQLCatalog c = new SQLCatalog();
		assertTrue(c.isSchemaContainer());
		c.addChild(new SQLTable());
		assertFalse(c.isSchemaContainer());
		c = new SQLCatalog();
		c.addChild(new SQLSchema(false));
		assertTrue(c.isSchemaContainer());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getNativeTerm()'
	 */
	public void testGetNativeTerm() {
		SQLCatalog c1 = new SQLCatalog();
		assertEquals(c1.getNativeTerm(),"catalog");
		c1.setNativeTerm(null);
		assertNull(c1.getNativeTerm());
		c1.setNativeTerm("AAA");
		assertEquals(c1.getNativeTerm(),"aaa");
		
		SQLCatalog c2 = new SQLCatalog(new SQLDatabase(db.getDataSource()),"x");
		assertEquals(c2.getNativeTerm(),"catalog");
		c2.setNativeTerm(null);
		assertNull(c2.getNativeTerm());
		c2.setNativeTerm("AAA");
		assertEquals(c2.getNativeTerm(),"aaa");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getPhysicalName()'
	 */
	public void testGetPhysicalName() {
		SQLCatalog c1 = new SQLCatalog();
		assertNull(c1.getPhysicalName());
		c1.setPhysicalName("aaa");
		assertEquals(c1.getPhysicalName(),"aaa");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.isPopulated()'
	 */
	public void testIsPopulated() throws Exception {
		SQLCatalog c1 = new SQLCatalog();
		assertFalse(c1.isPopulated());
		c1.setPopulated(true);
		assertTrue(c1.isPopulated());
		c1.setPopulated(false);
		assertFalse(c1.isPopulated());
		
		c1.setPopulated(true);
		c1.populate();
		c1.setPopulated(false);
		try {
			c1.populate();
		} catch ( NullPointerException e ) {
			// it's ok
		}
	}
	
	public void testIsPopulateWithCatalogs() throws Exception {		
		ArchitectDataSource dataSource = db.getDataSource();
		Connection conn = db.getConnection();
		DatabaseMetaData meta = conn.getMetaData();
		String ct = meta.getCatalogTerm();
		if (null == ct || ct.length() == 0) { // unless this platform has catalogs.
			return;
		}
		SQLCatalog c2 = new SQLCatalog(new SQLDatabase(dataSource),"x");		
		c2.setPopulated(false);			
		c2.populate();			
		assertTrue(c2.isPopulated());			
		c2.setPopulated(true);
		c2.populate();
		assertTrue(c2.isPopulated());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildren()'
	 */
	public void testGetChildren() throws Exception {
		SQLCatalog c1 = new SQLCatalog();
		SQLCatalog c2 = new SQLCatalog();
		c1.setPopulated(true);
		c2.setPopulated(true);
		assertNotNull(c1.getChildren());
		assertEquals(c1.getChildren().size(),0);
		assertEquals(c1.getChildCount(),0);
		
		
		String name[] = { "name1", "name2", "name3", "name4","name5", "name6" };
		ArrayList tableList = new ArrayList();
		ArrayList schemaList = new ArrayList();

		int i;
		for ( i=0; i<6; i++ ) {
			SQLTable t = new SQLTable(c1, name[i], "", "TABLE", true);
			SQLSchema s = new SQLSchema(c1, name[i], true);
			tableList.add(t);
			schemaList.add(i,s);
			
		}
		
		c1.setChildren(tableList);
		c2.setChildren(schemaList);
		assertNotNull(c1.getChildren());
		assertEquals(c1.getChildren().size(),6);
		assertEquals(c1.getChildCount(),6);
		
		assertNotNull(c2.getChildren());
		assertEquals(c2.getChildren().size(),6);
		assertEquals(c2.getChildCount(),6);
		
		Iterator it = c1.getChildren().iterator();
		Iterator it2 = c2.getChildren().iterator();
		
		i = 0;
		while ( it.hasNext() && it2.hasNext() ) {
			SQLObject o = (SQLObject) it.next();
			SQLObject o2 = (SQLObject) it2.next();
			assertEquals(o.getName(),name[i]);
			assertEquals(c1.getChild(i).getName(),name[i]);
			assertEquals(c2.getChild(i).getName(),name[i]);
			
			assertTrue(o instanceof SQLTable);
			assertTrue(o2 instanceof SQLSchema);
			i++;
		}
		
		c1.addChild(2,new SQLTable());
		assertEquals(c1.getChildren().size(),7);
		assertEquals(c1.getChildCount(),7);
		assertTrue(c1.getChild(2) instanceof SQLTable);
		
		c1.addChild(new SQLTable());
		assertEquals(c1.getChildren().size(),8);
		assertEquals(c1.getChildCount(),8);
		assertTrue(c1.getChild(c1.getChildren().size()-1) instanceof SQLTable);
		
		
		c2.addChild(2,new SQLSchema(true));
		assertEquals(c2.getChildren().size(),7);
		assertEquals(c2.getChildCount(),7);
		assertTrue(c2.getChild(2) instanceof SQLSchema);
		
		c2.addChild(new SQLSchema(true));
		assertEquals(c2.getChildren().size(),8);
		assertEquals(c2.getChildCount(),8);
		assertTrue(c2.getChild(c1.getChildren().size()-1) instanceof SQLSchema);
		
		try {
			c1.addChild(new SQLSchema(true));
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
		
		try {
			c2.addChild(new SQLTable());
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(int, SQLObject)'
	 */
	public void testAddChild() throws Exception {
		SQLCatalog c1 = new SQLCatalog();
		SQLCatalog c2 = new SQLCatalog();
		c1.setPopulated(true);
		c2.setPopulated(true);
		assertNotNull(c1.getChildren());
		assertEquals(c1.getChildren().size(),0);
		assertEquals(c1.getChildCount(),0);
		
		
		String name[] = { "name1", "name2", "name3", "name4","name5", "name6" };

		int i;
		for ( i=0; i<6; i++ ) {
			SQLTable t = new SQLTable(c1, name[i], "", "TABLE", true);
			SQLSchema s = new SQLSchema(c1, name[i], true);
			c1.addChild(i,t);
			c2.addChild(i,s);
			
		}
		assertNotNull(c1.getChildren());
		assertEquals(c1.getChildren().size(),6);
		assertEquals(c1.getChildCount(),6);
		
		assertNotNull(c2.getChildren());
		assertEquals(c2.getChildren().size(),6);
		assertEquals(c2.getChildCount(),6);
		
		Iterator it = c1.getChildren().iterator();
		Iterator it2 = c2.getChildren().iterator();
		
		i = 0;
		while ( it.hasNext() && it2.hasNext() ) {
			SQLObject o = (SQLObject) it.next();
			SQLObject o2 = (SQLObject) it2.next();
			assertEquals(o.getName(),name[i]);
			assertEquals(c1.getChild(i).getName(),name[i]);
			assertEquals(c2.getChild(i).getName(),name[i]);
			
			assertTrue(o instanceof SQLTable);
			assertTrue(o2 instanceof SQLSchema);
			i++;
		}
		
		c1.addChild(2,new SQLTable());
		assertEquals(c1.getChildren().size(),7);
		assertEquals(c1.getChildCount(),7);
		assertTrue(c1.getChild(2) instanceof SQLTable);
		
		c1.addChild(new SQLTable());
		assertEquals(c1.getChildren().size(),8);
		assertEquals(c1.getChildCount(),8);
		assertTrue(c1.getChild(c1.getChildren().size()-1) instanceof SQLTable);
		
		
		c2.addChild(2,new SQLSchema(true));
		assertEquals(c2.getChildren().size(),7);
		assertEquals(c2.getChildCount(),7);
		assertTrue(c2.getChild(2) instanceof SQLSchema);
		
		c2.addChild(new SQLSchema(true));
		assertEquals(c2.getChildren().size(),8);
		assertEquals(c2.getChildCount(),8);
		assertTrue(c2.getChild(c1.getChildren().size()-1) instanceof SQLSchema);
		
		try {
			c1.addChild(new SQLSchema(true));
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
		
		try {
			c2.addChild(new SQLTable());
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeChild(int)'
	 */
	public void testRemoveChild() throws Exception {
		SQLCatalog c1 = new SQLCatalog();
		
		c1.setPopulated(true);
		assertNotNull(c1.getChildren());
		assertEquals(c1.getChildren().size(),0);
		assertEquals(c1.getChildCount(),0);

		try {
			c1.removeChild(-1);
			c1.removeChild(0);
			c1.removeChild(1);
			fail();
		} catch ( IndexOutOfBoundsException e ) {
			// that's what we want
		}
		
		SQLTable t1 = new SQLTable(c1,"","","TABLE",true);
		SQLTable t2 = new SQLTable(c1,"","","TABLE",true);
		SQLTable t3 = new SQLTable(c1,"","","TABLE",true);
		SQLTable t4 = new SQLTable(c1,"","","TABLE",true);
		
		c1.addChild(t1);
		c1.addChild(t2);
		c1.addChild(t3);
		c1.addChild(t4);
		assertEquals(c1.getChildCount(),4);
		SQLTable tx = (SQLTable) c1.removeChild(1);
		assertEquals(tx,t2);
		assertEquals(c1.getChildCount(),3);
		
		assertTrue(c1.removeChild(t4));
		assertEquals(c1.getChildCount(),2);

		assertFalse(c1.removeChild(t4));
		assertEquals(c1.getChildCount(),2);
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getSQLObjectListeners()'
	 */
	public void testGetSQLObjectListeners() throws ArchitectException {
		TestSQLObjectListener test1 = new TestSQLObjectListener();
		TestSQLObjectListener test2 = new TestSQLObjectListener();
		SQLCatalog c1 = new SQLCatalog();
		
		c1.addSQLObjectListener(test1);
		c1.addSQLObjectListener(test2);
		
		assertEquals(c1.getSQLObjectListeners().get(0),test1);
		assertEquals(c1.getSQLObjectListeners().get(1),test2);
		
		for ( int i=0; i<5; i++ ) {
			c1.addChild(new SQLTable(c1,"","","TABLE",true));
		}
		
		assertEquals(test1.getInsertedCount(),5);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),5);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		c1.removeSQLObjectListener(test2);
		
		for ( int i=0; i<5; i++ ) {
			c1.removeChild(0);
		}
		
		assertEquals(test1.getInsertedCount(),5);
		assertEquals(test1.getRemovedCount(),5);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),5);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		assertEquals(c1.getSQLObjectListeners().size(),1);
	}



}
