/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;

public class TestFolder extends SQLTestCase {

	public TestFolder(String name) throws Exception {
		super(name);
	}
    
    /**
     * Creates a wrapper around the normal test suite which runs the
     * OneTimeSetup and OneTimeTearDown procedures.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestFolder.class);
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
		System.out.println("TestSQLColumn.oneTimeSetUp()");
		
		SQLDatabase mydb = new SQLDatabase(getDataSource());
		Connection con = null;
		Statement stmt = null;
		
		try {
			con = mydb.getConnection();
			stmt = con.createStatement();
			
			dropTableNoFail(con, "SQL_COLUMN_TEST_1PK");
			dropTableNoFail(con, "SQL_COLUMN_TEST_3PK");
			dropTableNoFail(con, "SQL_COLUMN_TEST_0PK");
	
			stmt.executeUpdate("CREATE TABLE SQL_COLUMN_TEST_1PK (\n" +
					" cow numeric(11),\n" +
					" moo varchar(10),\n" +
					" foo char(10),\n" +
                    " CONSTRAINT test1pk PRIMARY KEY (cow))");
            
			stmt.executeUpdate("CREATE TABLE SQL_COLUMN_TEST_3PK (\n" +
					" cow numeric(11) NOT NULL,\n" +
					" moo varchar(10) NOT NULL,\n" +
					" foo char(10) NOT NULL,\n" +
					" CONSTRAINT test3pk PRIMARY KEY (cow, moo, foo))");

            stmt.executeUpdate("CREATE TABLE SQL_COLUMN_TEST_0PK (\n" +
					" cow numeric(11),\n" +
					" moo varchar(10),\n" +
					" foo char(10))");

        } finally {
			try {
				if (stmt != null) stmt.close();
			} catch (SQLException ex) {
				System.out.println("Couldn't close statement");
			}
			try {
				if (con != null) con.close();
			} catch (SQLException ex) {
				System.out.println("Couldn't close connection");
			}
			//mydb.disconnect();  FIXME: this should be uncommented when bug 1005 is fixed
            System.out.println("finished TestSQLColumn.oneTimeSetUp()");
		}
	}
	
	/**
	 * Tries to drop the named table, but doesn't throw an exception if the
	 * DROP TABLE command fails.
	 * 
	 * @param con Connection to the database that has the offending table.
	 * @param tableName The table to nix.
	 * @throws SQLException if the created Statement object's close() method fails.
	 */
	private static void dropTableNoFail(Connection con, String tableName) throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("DROP TABLE "+tableName);
		} catch (SQLException e) {
			System.out.println("Ignoring SQLException.  Assume "+tableName+" didn't exist.");
			e.printStackTrace();
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	/**
	 * One-time cleanup code.  The special {@link #suite()} method arranges for
	 * this method to be called one time before the individual tests are run.
	 */
	public static void oneTimeTearDown() {
		System.out.println("TestSQLColumn.oneTimeTearDown()");
	}
	/**
	 * A table with one primary key column.  Gets set up in setUp().
	 */
	private SQLTable table1pk;

	protected void setUp() throws Exception {		
		super.setUp();
		table1pk = getDb().getTableByName("SQL_COLUMN_TEST_1PK");
		getDb().getTableByName("SQL_COLUMN_TEST_0PK");
		getDb().getTableByName("SQL_COLUMN_TEST_3PK");
	}

	/**
	 * Returns a random folder from a random test table.  Right now,
	 * this is table1pk.columns folder.  Whatever.
	 */
	@Override
	protected SQLObject getSQLObjectUnderTest() {
        return table1pk.getColumnsFolder();
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLTable.Folder.getName()'
	 */
	public void testGetName() {
		Folder f1;
		f1 = new Folder(Folder.COLUMNS, true);
		assertEquals (f1.getName(), "Columns");
		f1 = new Folder(Folder.IMPORTED_KEYS, true);
		assertEquals (f1.getName(), "Imported Keys");
		f1 = new Folder(Folder.EXPORTED_KEYS, true);
		assertEquals (f1.getName(), "Exported Keys");
		f1.setName("xyz");
		assertEquals (f1.getName(), "xyz");
		f1.setName(null);
		assertNull (f1.getName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLTable.Folder.getParent()'
	 */
	public void testGetParent() throws ArchitectException {
		Folder f1 = new Folder(Folder.COLUMNS, true);
		Folder f2 = new Folder(Folder.IMPORTED_KEYS, true);
		Folder f3 = new Folder(Folder.EXPORTED_KEYS, true);
		assertNull (f1.getParent());
		SQLTable t1 = new SQLTable();
		t1.addChild(f1);
		t1.addChild(f2);
		t1.addChild(f3);
		assertEquals (f1.getParent(), t1);		
		assertEquals (f2.getParent(), t1);	
		assertEquals (f3.getParent(), t1);
		t1.removeChild(1);
		assertEquals (t1.getChild(1), f3);
		t1.removeChild(f1);
		assertEquals (t1.getChild(0), f3);
		assertEquals (Folder.COLUMNS, f1.getType());
		assertEquals (Folder.IMPORTED_KEYS, f2.getType());
		assertEquals (Folder.EXPORTED_KEYS, f3.getType());
	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLTable.Folder.populate()'
	 */
	public void testPopulate() {

	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLTable.Folder.addChild(int, SQLObject)'
	 */
	public void testAddChild() throws ArchitectException {
		Folder main1 = new Folder(Folder.COLUMNS,true);
		SQLColumn c1 = new SQLColumn();
		SQLColumn c2 = new SQLColumn();
		SQLColumn c3 = new SQLColumn();
		
		main1.addChild(c1);
		assertEquals (main1.getChild(0), c1);
		main1.addChild(c2);
		assertEquals (main1.getChild(1), c2);
		main1.addChild(1,c3);
		
		List children = new ArrayList(main1.getChildren());
		
		assertTrue (children.contains(c1));
		assertTrue (children.contains(c2));
		assertTrue (children.contains(c3));
		assertEquals (children.size(), 3);
		assertEquals (main1.getChildCount(),3);
				
		assertEquals (main1.getChild(1), c3);	
		main1.removeChild(c3);
		assertEquals(main1.getChild(1), c2);
		main1.removeChild(0);
		assertEquals(main1.getChild(0), c2);
	}




	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildrenInserted(int[], List)'
	 */
	public void testFireDbChildrenInserted() throws Exception {

		Folder f1 = new Folder (Folder.COLUMNS, true);

		TestSQLObjectListener test1 = new TestSQLObjectListener();
		f1.addSQLObjectListener(test1);
		TestSQLObjectListener test2 = new TestSQLObjectListener();
		f1.addSQLObjectListener(test2);

		assertEquals(test1.getInsertedCount(), 0);
		assertEquals(test1.getRemovedCount(), 0);
		assertEquals(test1.getChangedCount(), 0);
		assertEquals(test1.getStructureChangedCount(), 0);

		assertEquals(test2.getInsertedCount(), 0);
		assertEquals(test2.getRemovedCount(), 0);
		assertEquals(test2.getChangedCount(), 0);
		assertEquals(test2.getStructureChangedCount(), 0);

		SQLColumn tmpCol = new SQLColumn();
		f1.addChild(tmpCol);

		assertEquals(test1.getInsertedCount(), 1);
		assertEquals(test1.getRemovedCount(), 0);
		assertEquals(test1.getChangedCount(), 0);
		assertEquals(test1.getStructureChangedCount(), 0);

		assertEquals(test2.getInsertedCount(), 1);
		assertEquals(test2.getRemovedCount(), 0);
		assertEquals(test2.getChangedCount(), 0);
		assertEquals(test2.getStructureChangedCount(), 0);

		f1.removeSQLObjectListener(test1);
		f1.removeChild(tmpCol);

		assertEquals(test1.getInsertedCount(), 1);
		assertEquals(test1.getRemovedCount(), 0);
		assertEquals(test1.getChangedCount(), 0);
		assertEquals(test1.getStructureChangedCount(), 0);

		assertEquals(test2.getInsertedCount(), 1);
		assertEquals(test2.getRemovedCount(), 1);
		assertEquals(test2.getChangedCount(), 0);
		assertEquals(test2.getStructureChangedCount(), 0);

		
	}

}