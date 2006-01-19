package regress;

import java.sql.*;
import ca.sqlpower.sql.*;
import java.util.*;

import ca.sqlpower.architect.*;

public class TestSQLDatabase extends SQLTestCase {

	public TestSQLDatabase(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		Connection con = mydb.getConnection();
		
		
		/*
		 * Setting up a clean db for each of the tests
		 */
		Statement stmt = con.createStatement();
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
		stmt.close();
		mydb.disconnect();
		
	}
	
	protected void tearDown() throws Exception {
		
		super.tearDown();
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

	public void testGetTableByName() throws ArchitectException {
		SQLTable table1, table2;
		assertNotNull(table1 = db.getTableByName("REGRESSION_TEST1"));
		assertNotNull(table2 = db.getTableByName("REGRESSION_TEST2"));
		assertNull("should get null for nonexistant table", db.getTableByName("no_such_table"));
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
//		 XXX: this test should be re-enabled when the product has I18N implemented.
		//assertEquals("error message should have been dbconnect.connectionFailed", "dbconnect.connectionFailed", exc.getMessage());
		assertNull("connection should be null", con);
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
