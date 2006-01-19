package regress.ca.sqlpower.architect;

import java.sql.*;
import java.util.*;

import ca.sqlpower.architect.*;

public class TestSQLColumn extends SQLTestCase {

	SQLTable table1pk;
	SQLTable table0pk;
	SQLTable table3pk;

	public TestSQLColumn(String name) throws Exception {
		super(name);
	}

	/**
	 * Tries to drop the named table, but doesn't throw an exception if the
	 * DROP TABLE command fails.
	 * 
	 * @param con Connection to the database that has the offending table.
	 * @param tableName The table to nix.
	 * @throws SQLException if the created Statement object's close() method fails.
	 */
	void dropTableNoFail(Connection con, String tableName) throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("DROP TABLE "+tableName);
		} catch (SQLException e) {
			System.out.println("Ignoring SQLException.  Assuming it means the table we tried to drop didn't exist.");
			e.printStackTrace();
		} finally {
			if (stmt != null) stmt.close();
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		Connection con = mydb.getConnection();
		Statement stmt = con.createStatement();
		
		dropTableNoFail(con, "SQL_COLUMN_TEST_1PK");
		dropTableNoFail(con, "SQL_COLUMN_TEST_3PK");
		dropTableNoFail(con, "SQL_COLUMN_TEST_0PK");

		stmt.executeUpdate("CREATE TABLE SQL_COLUMN_TEST_1PK (\n" +
				" cow numeric(10) CONSTRAINT test1pk PRIMARY KEY,\n" +
				" moo varchar(10),\n" +
		" foo char(10))");
		
		stmt.executeUpdate("CREATE TABLE SQL_COLUMN_TEST_3PK (\n" +
				" cow numeric(10) NOT NULL,\n" +
				" moo varchar(10) NOT NULL,\n" +
				" foo char(10) NOT NULL,\n" +
		" CONSTRAINT test3pk PRIMARY KEY (cow, moo, foo))");
		
		stmt.executeUpdate("CREATE TABLE SQL_COLUMN_TEST_0PK (\n" +
				" cow numeric(10),\n" +
				" moo varchar(10),\n" +
		" foo char(10))");
		table1pk = db.getTableByName("SQL_COLUMN_TEST_1PK");
		table0pk = db.getTableByName("SQL_COLUMN_TEST_3PK");
		table3pk = db.getTableByName("SQL_COLUMN_TEST_0PK");
		
		stmt.close();
		//mydb.disconnect();  FIXME: this should be uncommented when bug 1005 is fixed
		
	}

	public void testPopulateTable() throws ArchitectException {
		
		assertEquals("Table should have 3 folders as children",
				3, table1pk.getChildCount());
		assertFalse("Table columns should not have been populated already",
				table1pk.getColumnsFolder().isPopulated());
		table1pk.getColumnsFolder().populate();
		assertTrue("Table columns should be populated",
				table1pk.getColumnsFolder().isPopulated());

		// spot-check that expected columns exist
		assertNotNull("cow column not found", table1pk.getColumnByName("cow"));
		assertNotNull("moo column not found", table1pk.getColumnByName("moo"));
		assertNotNull("foo column not found", table1pk.getColumnByName("foo"));
	}
	
	public void testColumnOwnership() throws Exception {
		SQLColumn cowCol = table1pk.getColumnByName("cow");
		SQLColumn mooCol = table1pk.getColumnByName("moo");
		SQLColumn fooCol = table1pk.getColumnByName("foo");
		
		// check that all columns are owned by the correct table
		assertEquals("column doesn't belong to correct parent!", table1pk, cowCol.getParentTable());
		assertEquals("column doesn't belong to correct parent!", table1pk, mooCol.getParentTable());
		assertEquals("column doesn't belong to correct parent!", table1pk, fooCol.getParentTable());
	}
	
	public void testPKAttributes() throws Exception {
		SQLColumn cowCol = table1pk.getColumnByName("cow");
		SQLColumn fooCol = table1pk.getColumnByName("foo");

		// check for PK vs non PK attributes
		assertTrue("table1pk.cow should have been flagged as PK", cowCol.isPrimaryKey());
		assertEquals("table1pk.cow nullability incorrect", cowCol.getNullable(), DatabaseMetaData.columnNoNulls);
		assertFalse("table1pk.cow isDefinitelyNullable incorrect", cowCol.isDefinitelyNullable());

		assertFalse("table1pk.foo should NOT have been flagged as PK", fooCol.isPrimaryKey());
		assertEquals("table1pk.foo nullability incorrect", fooCol.getNullable(), DatabaseMetaData.columnNullable);
		assertTrue("table1pk.foo isDefinitelyNullable incorrect", fooCol.isDefinitelyNullable());
	}
	
	public void testCompareTo() throws Exception {
		SQLColumn cowCol = table1pk.getColumnByName("cow");
		SQLColumn mooCol = table1pk.getColumnByName("moo");
		SQLColumn fooCol = table1pk.getColumnByName("foo");

		// check column name comparator
		Comparator nameComp = new SQLColumn.ColumnNameComparator();
		assertTrue(nameComp.compare(cowCol, mooCol) < 0);
		assertTrue(nameComp.compare(mooCol, fooCol) > 0);
		assertTrue(nameComp.compare(fooCol, cowCol) > 0);
		assertTrue(nameComp.compare(cowCol, fooCol) < 0);
		assertTrue(nameComp.compare(cowCol, cowCol) == 0);
		cowCol.setColumnName(mooCol.getColumnName());
		assertTrue(nameComp.compare(cowCol, mooCol) == 0);
	}
}
