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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLIndex.IndexType;

public class TestSQLIndex extends SQLTestCase {

    private SQLIndex index;
    private SQLIndex index2;
    private SQLColumn col1;
    private SQLTable table;
    private SQLTable dbTable;
    
    
    /**
     * Creates a wrapper around the normal test suite which runs the
     * OneTimeSetup and OneTimeTearDown procedures.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestSQLIndex.class);
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
                    " foo char(10)," +
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
                ex.printStackTrace();
            }
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                System.out.println("Couldn't close connection");
                ex.printStackTrace();
            }
            //mydb.disconnect();  FIXME: this should be uncommented when bug 1005 is fixed
        }
    }

    /**
     * One-time cleanup code.  The special {@link #suite()} method arranges for
     * this method to be called one time before the individual tests are run.
     */
    public static void oneTimeTearDown() {
        System.out.println("TestSQLColumn.oneTimeTearDown()");
    }
    
    public TestSQLIndex(String name) throws Exception {
        super(name);
        propertiesToIgnoreForEventGeneration.add("parentTable");
        propertiesToIgnoreForUndo.add("parentTable");
    }

    protected void setUp() throws Exception {
        super.setUp();
        index = new SQLIndex("Test Index",true,"a",IndexType.HASHED,"b");
        table = new SQLTable(null,true);
        table.setName("Test Table");
        col1 = new SQLColumn();
        table.addColumn(col1);
        SQLColumn col2 = new SQLColumn();
        table.addColumn(col2);
        SQLColumn col3 = new SQLColumn();
        table.addColumn(col3);
        index.addIndexColumn(col1, true, true);
        index.addIndexColumn(col2, false, true);
        index.addIndexColumn(col3, true, false);
        table.addIndex(index);
        index2 = new SQLIndex("Test Index 2",true,"a",IndexType.HASHED,"b");
        index2.addIndexColumn(col1, true, true);
        index2.addIndexColumn(col3, false, true);
        table.addIndex(index2);
        dbTable = db.getTableByName("SQL_COLUMN_TEST_3PK");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected SQLObject getSQLObjectUnderTest() {
       
        return index;
    }

    /**
     * When you add an index column, it should attach a listener to its target column.
     */
    public void testReAddColumnAddsListener() throws Exception {
        System.out.println("Original listeners:       "+col1.getSQLObjectListeners());
        int origListeners = col1.getSQLObjectListeners().size();
        SQLIndex.Column removed = (Column) index.removeChild(0);
        index.addChild(removed);
        System.out.println("Post-remove-add listeners: "+col1.getSQLObjectListeners());
        assertEquals(origListeners, col1.getSQLObjectListeners().size());
    }
    
    /**
     * When you remove a column from an index, it has to unsubscribe its
     * listener from its target column.
     */
    public void testRemoveColumnNoListenerLeak() {
        System.out.println("Original listeners:    "+col1.getSQLObjectListeners());
        int origListeners = col1.getSQLObjectListeners().size();
        index.removeChild(0);
        System.out.println("Post-remove listeners: "+col1.getSQLObjectListeners());
        assertEquals(origListeners - 1, col1.getSQLObjectListeners().size());
    }
    
    public void testCopyConstructor() throws ArchitectException{
        SQLIndex copyIndex = new SQLIndex(index);
        
        assertEquals("Different Name",index.getName(),copyIndex.getName());
        assertEquals("Different uniqueness values", index.isUnique(),copyIndex.isUnique());
        assertEquals("Different index types", index.getType(),copyIndex.getType());
        assertEquals("Different qualifiers", index.getQualifier(),copyIndex.getQualifier());
        assertEquals("Different filters", index.getFilterCondition(),copyIndex.getFilterCondition());
        assertEquals("Different number of children", index.getChildCount(),copyIndex.getChildCount());
        
        for (int i=0; i< index.getChildCount();i++){
            assertEquals("Different columns for index column "+1, index.getChild(i).getColumn(),copyIndex.getChild(i).getColumn());
        }
    }
    
    public void testSetPrimaryKeyIndexTrueWithOnNonPkAndWithNoSetPK() throws ArchitectException {
        assertFalse("Test Index 1 already set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 already set as the pk", index2.isPrimaryKeyIndex());
        assertNull("Table contained a pk index",table.getPrimaryKeyIndex());
        index.setPrimaryKeyIndex(true);
        assertTrue("Test Index 1 not set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Index 1 not the table's primary key",index, table.getPrimaryKeyIndex());
    }
    
    public void testSetPrimaryKeyIndexTrueWithOnNonPkAndWithDifferentPKSet() throws ArchitectException {
        index2.setPrimaryKeyIndex(true);
        assertFalse("Test Index 1 already set as the pk", index.isPrimaryKeyIndex());
        assertTrue("Test Index 2 not set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Table did not contain index 2 as a pk index",index2,table.getPrimaryKeyIndex());
        index.setPrimaryKeyIndex(true);
        assertTrue("Test Index 1 not set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Index 1 not the table's primary key",index, table.getPrimaryKeyIndex());
    }
    
    public void testSetPrimaryKeyIndexTrueWithOnNonPkAndWithSameAsPK() throws ArchitectException {
        index.setPrimaryKeyIndex(true);
        assertTrue("Test Index 1 not set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Table did not contain index as a pk index",index,table.getPrimaryKeyIndex());
        index.setPrimaryKeyIndex(true);
        assertTrue("Test Index 1 not set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Index 1 not the table's primary key",index, table.getPrimaryKeyIndex());
    }
    
    public void testSetPrimaryKeyIndexFalseWithOnNonPkAndWithNoSetPK() throws ArchitectException {
        assertFalse("Test Index 1 already set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 already set as the pk", index2.isPrimaryKeyIndex());
        assertNull("Table contained a pk index",table.getPrimaryKeyIndex());
        index.setPrimaryKeyIndex(false);
        assertFalse("Test Index 1 already set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 already set as the pk", index2.isPrimaryKeyIndex());
        assertNull("Table contained a pk index",table.getPrimaryKeyIndex());
    }
    
    public void testSetPrimaryKeyIndexFalseWithOnNonPkAndWithDifferentPKSet() throws ArchitectException {
        index2.setPrimaryKeyIndex(true);
        assertFalse("Test Index 1 already set as the pk", index.isPrimaryKeyIndex());
        assertTrue("Test Index 2 not set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Table did not contain index 2 as a pk index",index2,table.getPrimaryKeyIndex());
        index.setPrimaryKeyIndex(false);
        assertFalse("Test Index 1 already set as the pk", index.isPrimaryKeyIndex());
        assertTrue("Test Index 2 not set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Table did not contain index 2 as a pk index",index2,table.getPrimaryKeyIndex());
    }
    
    public void testSetPrimaryKeyIndexFalseWithOnNonPkAndWithSameAsPK() throws ArchitectException {
        index.setPrimaryKeyIndex(true);
        assertTrue("Test Index 1 not set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 set as the pk", index2.isPrimaryKeyIndex());
        assertEquals("Table did not contain index as a pk index",index,table.getPrimaryKeyIndex());
        index.setPrimaryKeyIndex(false);
        assertFalse("Test Index 1 set as the pk", index.isPrimaryKeyIndex());
        assertFalse("Test Index 2 set as the pk", index2.isPrimaryKeyIndex());
        assertNull("The table's primary key is not null", table.getPrimaryKeyIndex());
    }
    
    public void testLoadFromDbGetsCorrectPK() throws ArchitectException{
        assertNotNull("No primary key loaded",dbTable.getPrimaryKeyIndex());
        assertEquals("Wrong number of indices",1,dbTable.getIndicesFolder().getChildCount());
        assertEquals("Wrong primary key","SYS_IDX_",dbTable.getPrimaryKeyName().substring(0, 8));
    }
    
    public void testAddStringColumnToPKThrowsException() throws ArchitectException{
        SQLIndex i = new SQLIndex("Index",true,"",IndexType.CLUSTERED,"");
        i.setPrimaryKeyIndex(true);
        try {
            i.addChild(i.new Column("index column",true,true));
            fail();
        } catch (ArchitectException e) {
            assertEquals("Cannot add a \"string\" column to a primary key index",e.getMessage());
            return;
        }
        fail();
    }
    
    public void testAddChangeIndexToPkWithStringColumn() throws ArchitectException{
        SQLIndex i = new SQLIndex("Index",true,"",IndexType.CLUSTERED,"");
        i.addChild(i.new Column("index column",true,true));
        try {
            i.setPrimaryKeyIndex(true);
            fail();
        } catch (ArchitectException e) {
            assertEquals("A PK must only refer to Index.Columns that contain SQLColumns",e.getMessage());
            return;
        }
    }
    
    public void testMakeColumnsLikeOtherIndexWhichHasNoColumns() throws ArchitectException {
        SQLIndex i = new SQLIndex("Index",true,"",IndexType.CLUSTERED,"");
        SQLColumn col = new SQLColumn();
        i.addChild(i.new Column("index column",true,true));
        i.addChild(i.new Column(col,true,true));
        
        SQLIndex i2 = new SQLIndex("Index2",false,"",IndexType.HASHED,"asdfa");
        i.makeColumnsLike(i2);
        assertEquals("Oh no some children are left!",0,i.getChildCount());
    }
    
    public void testMakeColumnsLikeOtherIndexWhichHasColumns() throws ArchitectException {
        SQLIndex i = new SQLIndex("Index",true,"",IndexType.CLUSTERED,"");
        SQLColumn col = new SQLColumn();
        
        SQLIndex i2 = new SQLIndex("Index2",false,"",IndexType.HASHED,"asdfa");
        i2.addChild(i2.new Column("index column",true,true));
        i2.addChild(i2.new Column(col,true,true));
        i.makeColumnsLike(i2);
        assertEquals("Wrong number of children!",2,i.getChildCount());
        assertEquals("Oh no wrong child!",i2.getChild(0),i.getChild(0));
        assertEquals("Oh no wrong child!",i2.getChild(1),i.getChild(1));
    }
    
    public void testMakeColumnsLikeOtherIndexReordersColumns() throws ArchitectException {
        SQLIndex i = new SQLIndex("Index",true,"",IndexType.CLUSTERED,"");
        SQLColumn col = new SQLColumn();
        i.addChild(i.new Column(col,true,true));
        i.addChild(i.new Column("index column",true,true));

        SQLIndex i2 = new SQLIndex("Index2",false,"",IndexType.HASHED,"asdfa");
        i2.addChild(i2.new Column("index column",true,true));
        i2.addChild(i2.new Column(col,true,true));
        i.makeColumnsLike(i2);
        assertEquals("Wrong number of children!",2,i.getChildCount());
        assertEquals("Oh no wrong child!",i2.getChild(0),i.getChild(0));
        assertEquals("Oh no wrong child!",i2.getChild(1),i.getChild(1));
    }
}
