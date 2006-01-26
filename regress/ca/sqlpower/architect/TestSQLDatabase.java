package regress.ca.sqlpower.architect;

import java.beans.PropertyChangeEvent;
import java.sql.Connection;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import regress.ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class TestSQLDatabase extends SQLTestCase {
	
	/**
	 * Creates a wrapper around the normal test suite which runs the
	 * OneTimeSetup and OneTimeTearDown procedures.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(TestSQLDatabase.class);
		TestSetup wrapper = new TestSetup(suite) {
			protected void setUp() throws Exception {
				oneTimeSetUp();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return wrapper;
	}

	/**
	 * One-time initialization code.  The special {@link #suite()} method arranges for
	 * this method to be called one time before the individual tests are run.
	 * @throws Exception 
	 */
	public static void oneTimeSetUp() throws Exception {
		System.out.println("TestSQLDatabase.oneTimeSetUp()");
		SQLDatabase mydb = new SQLDatabase(getDataSource());
		Connection con = mydb.getConnection();
		Statement stmt = null;

		try {
			stmt = con.createStatement();
			
			/*
			 * Setting up a clean db for each of the tests
			 */
			try {
				stmt.executeUpdate("DROP TABLE REGRESSION_TEST1");
				stmt.executeUpdate("DROP TABLE REGRESSION_TEST2");
			}
			catch (SQLException sqle ){
				System.out.println("+++ TestSQLDatabase exception should be for dropping a non-existant table");
				sqle.printStackTrace();
			}
			
			stmt.executeUpdate("CREATE TABLE REGRESSION_TEST1 (t1_c1 numeric(10))");
			stmt.executeUpdate("CREATE TABLE REGRESSION_TEST2 (t2_c1 char(10))");
			
		} finally {
			if (stmt != null) stmt.close();
			mydb.disconnect();
		}
	}

	/**
	 * One-time cleanup code.  The special {@link #suite()} method arranges for
	 * this method to be called one time before the individual tests are run.
	 */
	public static void oneTimeTearDown() {
		System.out.println("TestSQLDatabase.oneTimeTearDown()");
	}

	public TestSQLDatabase(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getName()'
	 */
	public void testGetName() {
		SQLDatabase db1 = new SQLDatabase();
		assertEquals("PlayPen Database", db1.getName());
		assertEquals("PlayPen Database", db1.getShortDisplayName());
		assertEquals(db1.getName(), db1.getPhysicalName());
		
		assertEquals(db.getName(), db.getDataSource().getDisplayName());
		assertEquals(db.getDataSource().getDisplayName(), db.getShortDisplayName());
		assertEquals(db.getName(), db.getPhysicalName());
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getParent()'
	 */
	public void testGetParent() {
		SQLDatabase db1 = new SQLDatabase();
		assertNull(db1.getParent());
		assertNull(db.getParent());
	}
	
	public void testGoodConnect() throws ArchitectException {
		assertFalse("db shouldn't have been connected yet", db.isConnected());
		Connection con = db.getConnection();
		assertNotNull("db gave back a null connection", con);
		assertTrue("db should have said it is connected", db.isConnected());
	}

	public void testPopulate() throws ArchitectException {
		db.getConnection(); // causes db to actually connect
		assertFalse("even though connected, should not be populated yet", db.isPopulated());
		db.populate();
		assertTrue("should be populated now", db.isPopulated());

		db.populate(); // it must be allowed to call populate multiple times
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.allowsChildren()'
	 */
	public void testAllowsChildren() {
		SQLDatabase db1 = new SQLDatabase();
		assertTrue(db1.allowsChildren());
		assertTrue(db.allowsChildren());
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getTableByName(String)'
	 */
	public void testGetTableByName() throws ArchitectException {
		SQLTable table1, table2;
		assertNotNull(table1 = db.getTableByName("REGRESSION_TEST1"));
		assertNotNull(table2 = db.getTableByName("REGRESSION_TEST2"));
		assertNull("should get null for nonexistant table", db.getTableByName("no_such_table"));
		
		SQLTable table3 = new SQLTable(db, "xyz", "", "TABLE",true);
		SQLCatalog cat1 = null;
		SQLSchema sch1 = null;
		if (db.isCatalogContainer()){
			cat1 = new SQLCatalog(db,"cat1");
			cat1.addChild(table3);
			db.addChild (cat1);
		}		
		else if (db.isSchemaContainer()){
			sch1 = new SQLSchema(db,"sch1",true);
			sch1.addChild(table3);
			db.addChild(sch1);
		}			
		else{
			db.addChild(table3);
		}
			
		table1 =db.getTableByName (table3.getName());
		assertEquals(table1, table3);
		table1 = null;
		if (cat1 != null){
			table1 = db.getTableByName(cat1.getName(), null, table3.getName());			
		}		
		else if (sch1 != null){
			table1 = db.getTableByName(null, sch1.getName(),table3.getName());
		}		
		else{
			table1 = db.getTableByName(null, null,table3.getName());			
		}
		assertNotNull(table1);		
		assertEquals (table1,table3);		
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getTables()'
	 */
	public void testGetTables() throws ArchitectException {
		SQLTable table1, table2;
		assertNotNull(table1 = db.getTableByName("REGRESSION_TEST1"));
		assertNotNull(table2 = db.getTableByName("REGRESSION_TEST2"));
		assertNull("should get null for nonexistant table", db.getTableByName("no_such_table"));
		
		SQLTable table3 = new SQLTable(db, "xyz", "", "TABLE",true);
		SQLCatalog cat1 = null;
		SQLSchema sch1 = null;
		if (db.isCatalogContainer()){
			cat1 = new SQLCatalog(db,"cat1");
			cat1.addChild(table3);
			db.addChild (cat1);
		}		
		else if (db.isSchemaContainer()){
			sch1 = new SQLSchema(db,"sch1",true);
			sch1.addChild(table3);
			db.addChild(sch1);
		}			
		else{
			db.addChild(table3);
		}

		List getTablesTest = db.getTables();
		assertTrue(getTablesTest.contains(table1));
		assertTrue(getTablesTest.contains(table2));
		assertTrue(getTablesTest.contains(table3));					
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getDataSource()'
	 */
	public void testGetDataSource() {
		SQLDatabase db1 = new SQLDatabase();
		assertNull (db1.getDataSource());
		ArchitectDataSource data = db.getDataSource();
		db1.setDataSource(data);
		assertEquals (db.getDataSource(), db1.getDataSource());
		db1 = new SQLDatabase(data);
		assertEquals (db.getDataSource(), db1.getDataSource());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getSchemaByName(String)'
	 */
	public void testGetSchemaByName() throws Exception {						
		db.populate();
		if (db.isSchemaContainer()){			
			SQLSchema s = new SQLSchema(db,"a schema name should not exist in database",true);
			db.addChild(s);
			db.addChild (new SQLSchema(true));
			db.addChild (new SQLSchema (false));
			assertEquals(db.getSchemaByName("a schema name should not exist in database"),s);
			assertNull(db.getSchemaByName("a schema name should not exist in database xx2"));			
		}
		
		
		SQLDatabase db1 = new SQLDatabase();
		if (db1.isSchemaContainer()){
			SQLSchema s = new SQLSchema(db1,"a schema name should not exist in database",true);
			db1.addChild(s);
			db1.addChild (new SQLSchema(true));
			db1.addChild (new SQLSchema (false));
			assertEquals(db1.getSchemaByName("a schema name should not exist in database"),s);
			assertNull(db1.getSchemaByName("a schema name should not exist in database xx2"));	
		}		
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getCatalogByName(String)'
	 */
	public void testGetCatalogByName() throws Exception {
		SQLCatalog cat = new SQLCatalog(db,"a catalog name should not exist in database");
		
		try {
			db.populate();
			db.addChild(cat);
			assertEquals(db.getCatalogByName("a catalog name should not exist in database"),cat);
			assertNull(db.getCatalogByName("a catalog name should not exist in database xx2"));
		} catch ( ArchitectException e ) {
			if ( db.isCatalogContainer() ) {
				throw e;
			}					
		}
						
		SQLDatabase db1 = new SQLDatabase();
		if ( db1.isCatalogContainer() ) {
			db1.addChild(cat);
			assertEquals(db1.getCatalogByName("a catalog name should not exist in database"),cat);
			assertNull(db1.getCatalogByName("a catalog name should not exist in database xx2"));
		}				
	}

		
	public void testIgnoreReset() throws ArchitectException
	{		
		// Cause db to connect
		db.getChild(0);
		
		db.setIgnoreReset(true);
		assertTrue(db.getIgnoreReset());
		
		db.setDataSource(db.getDataSource());
		assertTrue(db.isPopulated());
		
		db.setIgnoreReset(false);
		assertFalse(db.getIgnoreReset());
		db.setDataSource(db.getDataSource());
		assertFalse(db.isPopulated());			
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.reset()'
	 */
	public void testReset() throws ArchitectException {
		SQLDatabase db1 = new SQLDatabase();
		
		db1.setDataSource(db.getDataSource());
		db1.setDataSource(db.getDataSource());
		
		assertFalse(db1.isPopulated());
		assertFalse (db1.isConnected());		
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setChildren(List)'
	 */
	public void testSetChildren() throws ArchitectException {
		SQLDatabase db1 = new SQLDatabase();
		db1.setChildren(db.getChildren());
		SQLObject o1 = (SQLObject) db.getChildren().get(0);
		SQLObject o2 = (SQLObject) db.getChildren().get(1);
		assertTrue (db1.getChildren().contains(o1));
		assertTrue (db1.getChildren().contains(o2));
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChild(int)'
	 */
	public void testGetChild() throws ArchitectException {
		SQLDatabase db1 = new SQLDatabase();
		SQLTable t1 = new SQLTable(db1,"t1","","TABLE",true);
		SQLTable t2 = new SQLTable(db1,"t2","","TABLE",true);
		SQLTable t3 = new SQLTable(db1,"t3","","TABLE",true);
		db1.addChild(t1);
		db1.addChild(1,t2);
		assertEquals (db1.getChild(0), t1);
		assertEquals (db1.getChild(1), t2);
		db1.addChild(0,t3);
		assertEquals (db1.getChild(1), t1);
		assertEquals (db1.getChild(0), t3);
		db1.removeChild (1);
		assertEquals (db1.getChild(1), t2);
		db1.removeChild(t3);
		assertEquals (db1.getChild(0), t2);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getSQLObjectListeners()'
	 */
	public void testGetSQLObjectListeners() throws ArchitectException {
		TestSQLObjectListener test1 = new TestSQLObjectListener();
		TestSQLObjectListener test2 = new TestSQLObjectListener();
		SQLDatabase s1 = new SQLDatabase();
		
		s1.addSQLObjectListener(test1);
		s1.addSQLObjectListener(test2);
		
		assertEquals(s1.getSQLObjectListeners().get(0),test1);
		assertEquals(s1.getSQLObjectListeners().get(1),test2);
		
		for ( int i=0; i<5; i++ ) {
			s1.addChild(new SQLTable(s1,"","","TABLE", true));
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

	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.getProgressMonitor()'
	 */
	public void testGetProgressMonitor() throws ArchitectException {
		SQLDatabase db1 = new SQLDatabase();
		assertNotNull (db1.getProgressMonitor());
		assertNotNull (db.getProgressMonitor());
	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildrenInserted(int[], List)'
	 */
	public void testFireDbChildrenInserted() throws ArchitectException {
		
		SQLDatabase db1 = new SQLDatabase();
		TestSQLObjectListener test1 = new TestSQLObjectListener();		
				
		db1.addSQLObjectListener(test1);		
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
				
		db1.setDataSource(db.getDataSource());
		db1.setPopulated(false);
		db1.populate();
		
		assertEquals(test1.getInsertedCount(),1);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),1);
		assertEquals(test1.getStructureChangedCount(),0);
		
		db1.setDataSource(db1.getDataSource());
		
		assertEquals(test1.getInsertedCount(),1);
		assertEquals(test1.getRemovedCount(),1);
		assertEquals(test1.getChangedCount(),2);
		assertEquals(test1.getStructureChangedCount(),0);		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLDatabase.disconnect()'
	 */
	public void testDisconnect() throws ArchitectException {
		assertNotNull (db.getChild(0));
		assertTrue (db.isConnected());
		db.disconnect();
		assertFalse (db.isConnected());				
	}
	
	public void testReconnect() throws ArchitectException {
		
		// cause db to actually connect
		assertNotNull(db.getChild(0));

		// cause disconnection
		db.setDataSource(db.getDataSource());
		assertFalse("db shouldn't be connected anymore", db.isConnected());
		assertFalse("db shouldn't be populated anymore", db.isPopulated());

		assertNotNull(db.getChild(1));

		assertTrue("db should be repopulated", db.isPopulated());
		assertTrue("db should be reconnected", db.isConnected());
		assertNotNull("db should be reconnected", db.getConnection());
	}

	public void testMissingDriverConnect() {
		ArchitectDataSource ds = db.getDataSource();
		ds.setDriverClass("ca.sqlpower.xxx.does.not.exist");
		
		SQLDatabase mydb = new SQLDatabase(ds);
		Connection con = null;
		ArchitectException exc = null;
		try {
			assertFalse("db shouldn't have been connected yet", db.isConnected());
			con = mydb.getConnection();
		} catch (ArchitectException e) {
			exc = e;
		}
		assertNotNull("should have got an ArchitectException", exc);
		// XXX: this test should be re-enabled when the product has I18N implemented.
		//assertEquals("error message should have been dbconnect.noDriver", "dbconnect.noDriver", exc.getMessage());
		assertNull("connection should be null", con);
	}

	public void testBadURLConnect() {
		ArchitectDataSource ds = db.getDataSource();
		ds.setUrl("jdbc:bad:moo");
		
		SQLDatabase mydb = new SQLDatabase(ds);
		Connection con = null;
		ArchitectException exc = null;
		try {
			assertFalse("db shouldn't have been connected yet", db.isConnected());
			con = mydb.getConnection();
		} catch (ArchitectException e) {
			exc = e;
		}
		assertNotNull("should have got an ArchitectException", exc);
//		XXX: this test should be re-enabled when the product has I18N implemented.
		//assertEquals("error message should have been dbconnect.connectionFailed", "dbconnect.connectionFailed", exc.getMessage());
		assertNull("connection should be null", con);
	}

	public void testBadPasswordConnect() {
		ArchitectDataSource ds = db.getDataSource();
		ds.setPass("foofoofoofoofooSDFGHJK");  // XXX: if this is the password, we lose.
		
		SQLDatabase mydb = new SQLDatabase(ds);
		Connection con = null;
		ArchitectException exc = null;
		try {
			assertFalse("db shouldn't have been connected yet", db.isConnected());
			con = mydb.getConnection();
		} catch (ArchitectException e) {
			exc = e;
		}
		assertNotNull("should have got an ArchitectException", exc);
		// XXX: this test should be re-enabled when the product has I18N implemented.
		// assertEquals("error message should have been dbconnect.connectionFailed", "dbconnect.connectionFailed", exc.getMessage());
		assertNull("connection should be null", con);
	}
	
	public void testPropertyChange() throws Exception
	{
		try {
			PropertyChangeEvent e = new PropertyChangeEvent(null, null,"1", "2");		
			fail("Property change event didn't reject null source;" + e);
		} catch (IllegalArgumentException ile) {
			System.out.println("Caught expected exception.");
		}
		PropertyChangeEvent e = new PropertyChangeEvent(this, null,"1", "2");		
		db.propertyChange(e);
	}
	
	
	public void testUnpopulatedDB(){
		assertFalse(db.isPopulated());
	}

	public void testAutoPopulate() throws Exception {
		assertFalse(db.isPopulated());		
		SQLObject child = db.getChild(0);
		assertTrue(db.isPopulated());
		assertFalse(child.isPopulated());
	}
	
	
	
}
