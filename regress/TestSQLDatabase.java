package regress;

import java.sql.*;
import ca.sqlpower.sql.*;
import java.util.*;

import ca.sqlpower.architect.*;

public class TestSQLDatabase extends SQLTestCase {

	public TestSQLDatabase(String name) {
		super(name);
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
		SQLTable battingTable, allStarTable, awardTable;
		assertNotNull(battingTable = db.getTableByName("batting"));
		assertNotNull(allStarTable = db.getTableByName("all_star"));
		assertNotNull(awardTable = db.getTableByName("award"));
		assertNull("should get null for nonexistant table", db.getTableByName("no_such_table"));
	}

	public void testReconnect() throws ArchitectException {
		SQLTable battingTable;
		// cause db to actually connect
		assertNotNull(battingTable = db.getTableByName("batting"));

		// cause disconnection
		db.setDataSource(db.getDataSource());
		assertFalse("db shouldn't be connected anymore", db.isConnected());
		assertFalse("db shouldn't be populated anymore", db.isPopulated());

		assertNotNull(battingTable = db.getTableByName("batting"));

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
}
