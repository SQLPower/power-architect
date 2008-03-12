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

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.beanutils.BeanUtils;

import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;
import ca.sqlpower.architect.TestSQLTable.EventLogger.SQLObjectSnapshot;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.MockJDBCDriver;

public class TestSQLTable extends SQLTestCase {
    
    /**
     * Creates a wrapper around the normal test suite which runs the
     * OneTimeSetup and OneTimeTearDown procedures.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestSQLTable.class);
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
        System.out.println("TestSQLTable.oneTimeSetUp()");
        SQLDatabase mydb = new SQLDatabase(getDataSource());
        Connection con = null;
        Statement stmt = null;
        
        /*
         * Setting up a clean db for each of the tests
         */
        try {
            con = mydb.getConnection();
            stmt = con.createStatement();
            try {
                stmt.executeUpdate("DROP TABLE REGRESSION_TEST1");
                stmt.executeUpdate("DROP TABLE REGRESSION_TEST2");
            } catch (SQLException sqle) {
                System.out.println("+++ TestSQLDatabase exception should be for dropping a non-existant table");
                sqle.printStackTrace();
            }
            
            stmt.executeUpdate("CREATE TABLE REGRESSION_TEST1 (t1_c1 numeric(10), t1_c2 numeric(5))");
            stmt.executeUpdate("CREATE TABLE REGRESSION_TEST2 (t2_c1 char(10))");
            stmt.executeUpdate("CREATE VIEW REGRESSION_TEST1_VIEW AS SELECT * FROM REGRESSION_TEST1");
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close statement");
            }
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close connection");
            }
            mydb.disconnect();
        }
    }
    
    /**
     * One-time cleanup code.  The special {@link #suite()} method arranges for
     * this method to be called one time before the individual tests are run.
     */
    public static void oneTimeTearDown() {
        System.out.println("TestSQLTable.oneTimeTearDown()");
    }
    
    private SQLTable table;    
    
    public TestSQLTable(String name) throws Exception {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        table = new SQLTable(null, true);
        table.addColumn(new SQLColumn(table, "one", Types.INTEGER, 10, 0));
        table.addColumn(new SQLColumn(table, "two", Types.INTEGER, 10, 0));
        table.addColumn(new SQLColumn(table, "three", Types.INTEGER, 10, 0));
        table.addColumn(new SQLColumn(table, "four", Types.INTEGER, 10, 0));
        table.addColumn(new SQLColumn(table, "five", Types.INTEGER, 10, 0));
        table.addColumn(new SQLColumn(table, "six", Types.INTEGER, 10, 0));
        table.getColumn(0).setPrimaryKeySeq(0);        
        table.getColumn(1).setPrimaryKeySeq(1);
        table.getColumn(2).setPrimaryKeySeq(2);
        table.getColumn(0).setNullable(DatabaseMetaData.columnNullable);
        table.getColumn(0).setAutoIncrement(true); 
    }
    
    @Override
    protected SQLObject getSQLObjectUnderTest() {
        return table;
    }
    
    public void testConstructor() {
        // FIXME need to test both constructors!
    }
    
    public void testGetDerivedInstance() throws Exception {
        SQLTable derivedTable;
        SQLTable table1;
        // Check to make sure it can be added to a playpen like database
        SQLDatabase pp = new SQLDatabase();
        pp.setPlayPenDatabase(true);
        assertNotNull(table1 = db.getTableByName("REGRESSION_TEST1"));
        derivedTable = SQLTable.getDerivedInstance(table1, pp);
        
        TreeMap derivedPropertyMap = new TreeMap(BeanUtils.describe(derivedTable));
        TreeMap table1PropertyMap = new TreeMap(BeanUtils.describe(table1));
        
        derivedPropertyMap.remove("parent");
        derivedPropertyMap.remove("parentDatabase");
        derivedPropertyMap.remove("schemaName");
        derivedPropertyMap.remove("schema");
        derivedPropertyMap.remove("shortDisplayName");
        table1PropertyMap.remove("parent");
        table1PropertyMap.remove("schemaName");
        table1PropertyMap.remove("schema");
        table1PropertyMap.remove("parentDatabase");
        table1PropertyMap.remove("shortDisplayName");
        assertEquals("Derived table not properly copied",
                derivedPropertyMap.toString(),
                table1PropertyMap.toString());
        
    }
    
    public void testRenameTableRenamesPK() throws ArchitectException{
        assertNotNull("Table has null name",table.getName());
        String newName = "newTableName";
        table.setName(newName);
        assertEquals(newName+"_pk",table.getPrimaryKeyName());
    }
    
    public void testRenameTableDoesntRenamePKIfPKRenamed() throws ArchitectException {
        assertNotNull("Table has null name",table.getName());
        String newTableName = "newTableName";
        String newPKName = "NewPKName";
        table.getPrimaryKeyIndex().setName(newPKName);
        table.setName(newTableName);
        assertEquals(newPKName, table.getPrimaryKeyName());
    }
    
    public void testRenameTableRenamesSequences() throws Exception {
        table.setName("old name");
        table.getColumn(0).setAutoIncrementSequenceName("moo_" + table.getName() + "_cow");
        table.setName("new name");
        assertTrue(table.getColumn(0).isAutoIncrementSequenceNameSet());
        assertEquals("moo_" + table.getName() + "_cow", table.getColumn(0).getAutoIncrementSequenceName());
    }

    public void testRenameTableDoesNotRenameUnnamedSequences() throws Exception {
        table.setName("old name");
        table.getColumn(0).setAutoIncrementSequenceName("moo_" + table.getName() + "_cow");
        table.setName("new name");
        assertTrue(table.getColumn(0).isAutoIncrementSequenceNameSet());
        for (int i = 1; i < table.getColumnsFolder().getChildCount(); i++) {
            assertFalse(table.getColumn(i).isAutoIncrementSequenceNameSet());
        }
    }
    
    public void testInherit() throws ArchitectException {
        SQLTable table1;
        SQLTable table2;
        table1 = db.getTableByName("REGRESSION_TEST1");
        table2 = db.getTableByName("REGRESSION_TEST2");
        
        // the tables need to load properly
        assertEquals(2, table1.getColumns().size());
        assertEquals(1, table2.getColumns().size());
        
        table2.inherit(table1);
        assertEquals("The wrong 1st column was inherited",
                table1.getColumn(0).toString(), table2.getColumn(1).toString());
        assertEquals("The wrong 2nd column was inherited",
                table1.getColumn(1).toString(), table2.getColumn(2).toString());
        assertEquals("The wrong number of columns were inherited",
                table2.getColumns().size(), 3);
        try {
            table2.inherit(table2);
        } catch (ArchitectException ae) {
            if ("Cannot inherit from self".equals(ae.getMessage())) {
                System.out.println("Expected Behaviour is to not be able to inherit from self");
            } else {
                throw ae;
            }
        }
    }
    
    public void testGetColumnByName() throws ArchitectException {
        SQLTable table1;
        SQLColumn col1;
        SQLColumn col2;
        table1 = db.getTableByName("REGRESSION_TEST1");
        col2 = table1.getColumnByName("t1_c2");
        assertNotNull(col2);
        assertEquals("The wrong colomn us returned", col2, table1.getColumn(1));
        
        col1 = table1.getColumnByName("t1_c1");
        assertNotNull(col1);
        assertEquals("The wrong colomn us returned", col1, table1.getColumn(0));
        
        assertNull(col1 = table1.getColumnByName("This_is_a_non_existant_column"));
        assertNull("Invalid column name", col1 = table1.getColumnByName("$#  #$%#%"));
    }
    
    public void testAddColumn() throws ArchitectException {
        SQLTable table1 = db.getTableByName("REGRESSION_TEST1");
        SQLColumn newColumn = new SQLColumn(table1, "my new column", Types.INTEGER, 10, 0);
        table1.addColumn(2, newColumn);
        SQLColumn addedCol = table1.getColumn(2);
        assertSame("Column at index 2 isn't same object as we added", newColumn, addedCol);
    }
    
    public void testAddColumnReference() throws ArchitectException {
        SQLTable table = db.getTableByName("REGRESSION_TEST1");
        SQLColumn col = table.getColumn(0);
        assertEquals("Existing column had refcount != 1", 1, col.getReferenceCount());
        table.addColumn(col);
        assertEquals("refcount didn't increase", 2, col.getReferenceCount());
    }
    
    public void tesRemoveColumnByZeroRefs() throws ArchitectException {
        SQLTable table = db.getTableByName("REGRESSION_TEST1");
        SQLColumn col = table.getColumn(0);
        table.addColumn(col);
        table.addColumn(col);
        col.removeReference();
        assertTrue(table.getColumns().contains(col));
        col.removeReference();
        assertTrue(table.getColumns().contains(col));
        col.removeReference();
        assertFalse(table.getColumns().contains(col));
    }
    
    /** this tests for a real bug.. the column was showing up one index above the end of the pk  */
    public void testAddColumnAtEndOfPK() throws ArchitectException {
        SQLTable t = new SQLTable(null, true);
        t.setName("Test Table");
        SQLColumn pk1 = new SQLColumn(t, "PKColumn1", Types.INTEGER, 10, 0);
        SQLColumn pk2 = new SQLColumn(t, "PKColumn2", Types.INTEGER, 10, 0);
        SQLColumn pk3 = new SQLColumn(t, "PKColumn3", Types.INTEGER, 10, 0);
        SQLColumn at1 = new SQLColumn(t, "AT1", Types.INTEGER, 10, 0);
        SQLColumn at2 = new SQLColumn(t, "AT2", Types.INTEGER, 10, 0);
        SQLColumn at3 = new SQLColumn(t, "AT3", Types.INTEGER, 10, 0);
        
        t.addColumn(0,pk1);
        t.addColumn(1,pk2);
        t.addColumn(2,pk3);
        t.addColumn(3,at1);
        t.addColumn(4,at2);
        t.addColumn(5,at3);
        
        pk1.setPrimaryKeySeq(1);
        pk2.setPrimaryKeySeq(2);
        pk3.setPrimaryKeySeq(3);
        
        assertEquals(3, t.getPkSize());
        
        SQLColumn newcol = new SQLColumn(t, "newcol", Types.INTEGER, 10, 0);
        t.addColumn(3, newcol);
        assertEquals("New column should be at requested position", 3, t.getColumnIndex(newcol));
        newcol.setPrimaryKeySeq(3);
        assertEquals("New column should still be at requested position", 3, t.getColumnIndex(newcol));
    }
    
    public void testMoveToPKClearsNullability() throws ArchitectException{             
        SQLTable t = db.getTableByName("REGRESSION_TEST1");
        SQLColumn c = t.getColumnByName("t1_c1");
        assertNull("Column shouldn't be in PK to begin", c.getPrimaryKeySeq());
        c.setNullable(DatabaseMetaData.columnNullable);

        // Now c is not in the PK and is nullable.  Let's add it to PK
        t.changeColumnIndex(0,0,true);      
        
        assertTrue(c.isPrimaryKey());
        assertEquals(DatabaseMetaData.columnNoNulls, c.getNullable());
    }
    
    public void testRemoveColumnOutBounds() throws ArchitectException {
        SQLTable table1;
        
        table1 = db.getTableByName("REGRESSION_TEST1");
        Exception exc = null;
        try {
            table1.removeColumn(16);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Method throws proper error");
            exc = e;
        }
        
        assertNotNull("Should have thrown an exception", exc);
    }
    
    public void testRemoveColumn() throws ArchitectException {
        SQLTable table1;
        SQLColumn col1;
        SQLColumn col2;
        
        table1 = db.getTableByName("REGRESSION_TEST1");
        col1 = table1.getColumn(0);
        col2 = table1.getColumn(1);
        
        assertEquals("We removed a column when we shouldn't have",
                table1.getColumns().size(), 2);
        table1.removeColumn(col1);
        assertEquals("Either 0 or 2+ columns were removed",
                table1.getColumns().size(), 1);
        assertEquals("The wrong column was removed", col2, table1.getColumn(0));
        table1.removeColumn(0);
        assertEquals("Last Column failed to be removed",
                table1.getColumns().size(), 0);
        Exception exc = null;
        try {
            table1.removeColumn(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Method throws proper error");
            exc = e;
        }
        assertNotNull("Should have thrown an exception", exc);
    }
    
    public void testNormalizePrimaryKey() throws ArchitectException {
        SQLTable table1;
        SQLColumn col2;
        SQLColumn col1 = db.getTableByName("REGRESSION_TEST2").getColumn(0);
        col1.setPrimaryKeySeq(new Integer(5));
        table1 = db.getTableByName("REGRESSION_TEST1");
        col2 = new SQLColumn(col1);
        col2.setPrimaryKeySeq(new Integer(16));
        table1.addColumn(2, col1);
        table1.addColumn(3, col2);
        table1.normalizePrimaryKey();
        assertEquals("Wrong number of primary keys", table1.getPkSize(), 0);
        
        col1.setPrimaryKeySeq(new Integer(5));
        col2.setPrimaryKeySeq(new Integer(16));
        
        assertEquals("Invalid key order", table1.getColumn(0), col1);
        assertEquals("2nd key out of order", table1.getColumn(1), col2);
        assertEquals("Too many or too few primary keys", table1.getPkSize(), 2);
        
    }
    
    public void testGetPrimaryKey() throws ArchitectException {
        SQLTable t1 = new SQLTable(null,true);
        SQLColumn c1 = new SQLColumn(t1,"col1",1,0,0);
        SQLIndex i1 = new SQLIndex("name",true,null,SQLIndex.CLUSTERED,null);
        i1.addIndexColumn(c1, AscendDescend.UNSPECIFIED);
        t1.getIndicesFolder().addChild(i1);
        SQLIndex i2 = new SQLIndex("name 2",true,null,SQLIndex.CLUSTERED,null);
        i2.addChild(i2.new Column("Index column string",AscendDescend.UNSPECIFIED));
        t1.getIndicesFolder().addChild(i2);
        
        assertNull(t1.getPrimaryKeyIndex());
        
        i1.setPrimaryKeyIndex(true);
        assertEquals(i1,t1.getPrimaryKeyIndex());
        i1.setPrimaryKeyIndex(false);
        assertNull(t1.getPrimaryKeyIndex());
        
    }
    
    public void testGetPrimaryKeyWhenPrimarykeyNotFirstIndex() throws ArchitectException {
        SQLTable t1 = new SQLTable(null,true);
        SQLColumn c1 = new SQLColumn(t1,"col1",1,0,0);
        SQLIndex i1 = new SQLIndex("name",true,null,SQLIndex.CLUSTERED,null);
        i1.addIndexColumn(c1, AscendDescend.UNSPECIFIED);
        SQLIndex i2 = new SQLIndex("name 2",true,null,SQLIndex.CLUSTERED,null);
        i2.addChild(i2.new Column("Index column string",AscendDescend.UNSPECIFIED));
        t1.getIndicesFolder().addChild(i2);
        t1.getIndicesFolder().addChild(i1);
        i1.setPrimaryKeyIndex(true);
        assertEquals(i1,t1.getPrimaryKeyIndex());
        i1.setPrimaryKeyIndex(false);
        assertNull(t1.getPrimaryKeyIndex());
    }
    
    /*
     * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildrenInserted(int[], List)'
     */
    public void testFireDbChildrenInserted() throws Exception {
        
        SQLTable table1 = db.getTableByName("REGRESSION_TEST1");
        SQLColumn c1 = table1.getColumn(0);
        Folder folder = table1.getColumnsFolder();
        
        TestSQLObjectListener test1 = new TestSQLObjectListener();
        folder.addSQLObjectListener(test1);
        TestSQLObjectListener test2 = new TestSQLObjectListener();
        folder.addSQLObjectListener(test2);
        
        assertEquals(test1.getInsertedCount(), 0);
        assertEquals(test1.getRemovedCount(), 0);
        assertEquals(test1.getChangedCount(), 0);
        assertEquals(test1.getStructureChangedCount(), 0);
        
        assertEquals(test2.getInsertedCount(), 0);
        assertEquals(test2.getRemovedCount(), 0);
        assertEquals(test2.getChangedCount(), 0);
        assertEquals(test2.getStructureChangedCount(), 0);
        
        SQLColumn tmpCol = new SQLColumn();
        table1.addColumn(tmpCol);
        table1.changeColumnIndex(table1.getColumnIndex(c1), 2, false);
        
        assertEquals(test1.getInsertedCount(), 2);
        assertEquals(test1.getRemovedCount(), 1);
        assertEquals(test1.getChangedCount(), 0);
        assertEquals(test1.getStructureChangedCount(), 0);
        
        assertEquals(test2.getInsertedCount(), 2);
        assertEquals(test2.getRemovedCount(), 1);
        assertEquals(test2.getChangedCount(), 0);
        assertEquals(test2.getStructureChangedCount(), 0);
        
        folder.removeSQLObjectListener(test1);
        table1.changeColumnIndex(table1.getColumnIndex(c1), 1, false);
        
        assertEquals(test1.getInsertedCount(), 2);
        assertEquals(test1.getRemovedCount(), 1);
        assertEquals(test1.getChangedCount(), 0);
        assertEquals(test1.getStructureChangedCount(), 0);
        
        assertEquals(test2.getInsertedCount(), 3);
        assertEquals(test2.getRemovedCount(), 2);
        assertEquals(test2.getChangedCount(), 0);
        assertEquals(test2.getStructureChangedCount(), 0);
        
        table1.removeColumn(tmpCol);
        assertEquals(test2.getInsertedCount(), 3);
        assertEquals(test2.getRemovedCount(), 3);
        assertEquals(test2.getChangedCount(), 0);
        assertEquals(test2.getStructureChangedCount(), 0);
    }
    
    public void testDeleteLockedColumn() throws ArchitectException {
        SQLTable parentTable = new SQLTable(null, "parent", null, "TABLE", true);
        parentTable.addColumn(new SQLColumn(parentTable, "pkcol_1", Types.INTEGER, 10, 0));
        parentTable.addColumn(new SQLColumn(parentTable, "pkcol_2", Types.INTEGER, 10, 0));
        parentTable.addColumn(new SQLColumn(parentTable, "attribute_1", Types.INTEGER, 10, 0));
        
        SQLTable childTable1 = new SQLTable(null, "child_1", null, "TABLE", true);
        childTable1.addColumn(new SQLColumn(childTable1, "child_pkcol_1", Types.INTEGER, 10, 0));
        childTable1.addColumn(new SQLColumn(childTable1, "child_pkcol_2", Types.INTEGER, 10, 0));
        childTable1.addColumn(new SQLColumn(childTable1, "child_attribute", Types.INTEGER, 10, 0));
        
        SQLRelationship rel1 = new SQLRelationship();
        rel1.attachRelationship(parentTable,childTable1,false);
        rel1.addMapping(parentTable.getColumn(0), childTable1.getColumn(0));
        rel1.addMapping(parentTable.getColumn(1), childTable1.getColumn(1));
        
        
        try {
            SQLColumn inheritedCol = childTable1.getColumnByName("child_pkcol_1");
            childTable1.removeColumn(inheritedCol);
            fail("Remove should have thrown LockedColumnException");
        } catch (LockedColumnException ex) {
            // good
        } catch (Exception ex) {
            fail("Didn't get the exception we were expecting: " + ex);
        }
    }
    
    public void testRemovePKColumn() throws ArchitectException{
        assertEquals("There should be 6 columns to start",6, 
                table.getColumns().size());
        table.removeColumn((table.getColumnIndex(table.getColumnByName("two"))));
        
        assertEquals("A column should have been removed", 
                5, table.getColumns().size());
        
        assertEquals(2, table.getPkSize());        
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("six")));        
        
    }
    
    public void testRemoveFKColumn() throws ArchitectException{
        assertEquals("There should be 6 columns to start",6, 
                table.getColumns().size());
        table.removeColumn(table.getColumnIndex(table.getColumnByName("five")));
        assertEquals("A column should have been removed", 
                5, table.getColumns().size());
        
        assertEquals(3, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testAddColAtFirstIdx() throws ArchitectException{
        table.addColumn(0, new SQLColumn(table, "zero", Types.INTEGER, 10, 0));        
        assertEquals(7, table.getColumns().size());
        assertEquals(4, table.getPkSize());
        
        assertTrue(table.getColumnByName("zero").isPrimaryKey());
        assertEquals(0, table.getColumnIndex(table.getColumnByName("zero")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(6, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testAddColAbovePK() throws ArchitectException{
        table.addColumn(2, new SQLColumn(table, "indextwo", Types.INTEGER, 10, 0));        
        assertEquals(7, table.getColumns().size());
        assertEquals(4, table.getPkSize());
        
        assertTrue(table.getColumnByName("indextwo").isPrimaryKey());
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("indextwo")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(6, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testAddColBelowPK() throws ArchitectException{
        table.addColumn(4, new SQLColumn(table, "indexfour", Types.INTEGER, 10, 0));        
        assertEquals(7, table.getColumns().size());
        assertEquals(3, table.getPkSize());
        
        assertFalse(table.getColumnByName("indexfour").isPrimaryKey());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));        
        assertEquals(2, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("indexfour")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(6, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeFirstColumnIdx() throws ArchitectException{
        table.changeColumnIndex(0, 1, true);
        assertEquals(3, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("four")));        
        assertEquals(4, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeSecondColumnIdx() throws ArchitectException{
        table.changeColumnIndex(1, 0, true);
        assertEquals(3, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("four")));        
        assertEquals(4, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeSecondColumnIdxToFifth() throws ArchitectException{
        table.changeColumnIndex(1, 4, true);
        assertEquals(2, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeSFifthColumnIdxToSecond() throws Exception {
        EventLogger l = new EventLogger();
        SQLObjectSnapshot original = l.makeSQLObjectSnapshot(table);
        
        ArchitectUtils.listenToHierarchy(l, table);
        table.changeColumnIndex(4, 1, true);        
        ArchitectUtils.unlistenToHierarchy(l, table);

        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));       
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));

        System.out.println("Event log:\n"+l);

        SQLObjectSnapshot afterChange = l.makeSQLObjectSnapshot(table);

        System.out.println("Original: "+original);
        System.out.println("After: "+afterChange);
        
        l.rollBack(afterChange);
        
        assertEquals(original.toString(), afterChange.toString());
    }
    
    public void testChangeSFifthColumnIdxToTop() throws Exception {
        EventLogger l = new EventLogger();
        SQLObjectSnapshot original = l.makeSQLObjectSnapshot(table);
        
        ArchitectUtils.listenToHierarchy(l, table);
        table.changeColumnIndex(4, 0, true);        
        ArchitectUtils.unlistenToHierarchy(l, table);

        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));       
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));

        System.out.println("Event log:\n"+l);

        SQLObjectSnapshot afterChange = l.makeSQLObjectSnapshot(table);

        System.out.println("Original: "+original);
        System.out.println("After: "+afterChange);
        
        l.rollBack(afterChange);
        
        assertEquals(original.toString(), afterChange.toString());
    }
    
    public void testChangeSFifthColumnIdxToThird() throws ArchitectException{
        table.changeColumnIndex(4, 2, true);        
        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeSForthColumnIdxToThird() throws ArchitectException{
        table.changeColumnIndex(3, 2, true);        
        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeSThirdColumnIdxToForth() throws ArchitectException{
        table.changeColumnIndex(2, 3, true);        
        assertEquals(2, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeFirstColumnKey() throws ArchitectException{
        SQLColumn col1 = table.getColumnByName("one");
        assertNotNull(col1);
        col1.setPrimaryKeySeq(null);
        assertEquals(2, table.getPkSize());
        //We just want to make sure it's no longer the first or second 
        //column where the PK column lies
        assertTrue(table.getColumnIndex(col1) > 1);        
    }
    
    public void testChangeThirdColumnKey() throws ArchitectException{
        SQLColumn col3 = table.getColumnByName("three");
        assertNotNull(col3);
        col3.setPrimaryKeySeq(null);
        assertEquals(2, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeForthColumnKey() throws ArchitectException{
        SQLColumn col4 = table.getColumnByName("four");
        assertNotNull(col4);
        col4.setPrimaryKeySeq(0);
        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
    }
    
    public void testChangeFifthColumnKey() throws Exception {
        EventLogger l = new EventLogger();
        SQLColumn col5 = table.getColumnByName("five");
        assertNotNull(col5);

        SQLObjectSnapshot original = l.makeSQLObjectSnapshot(table);
        
        ArchitectUtils.listenToHierarchy(l, table);
        col5.setPrimaryKeySeq(0);
        ArchitectUtils.unlistenToHierarchy(l, table);

        System.out.println("Event log:\n"+l);

        SQLObjectSnapshot afterChange = l.makeSQLObjectSnapshot(table);

        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
        
        System.out.println("Original: "+original);
        System.out.println("After: "+afterChange);
        
        l.rollBack(afterChange);
        
        assertEquals(original.toString(), afterChange.toString());
        
        // also roll forward original and compare to afterChange
    }
    
    public void testPopulateColumnsCaseSensitive() throws Exception {
        SPDataSource ds = new SPDataSource(getPLIni());
        ds.setDisplayName("tableWithMixedColumnCase");
        ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
        ds.setUser("fake");
        ds.setPass("fake");
        ds.setUrl("jdbc:mock:" +
                "tables=tab1" +
                "&columns.tab1=this_is_my_column,THIS_IS_MY_COLUMN");
        SQLDatabase db = new SQLDatabase(ds);
        SQLTable t = db.getTableByName("tab1");
        
        // this shouldn't throw a DuplicateColumnException
        assertEquals(2, t.getColumns().size());
    }
    
    //TODO: Convert this test to work on a local Hypersonic DB
    public void testPopulateViewIndices() throws Exception {
        SPDataSource ds = getDataSource();
        
        SQLDatabase db = new SQLDatabase(ds);
          
        SQLTable t = db.getTableByName("REGRESSION_TEST1_VIEW");
        
        SQLObject o = t.getIndicesFolder();
        
        // Should not throw an ArchitectException
        o.populate();
    }
    
    /**
     * Utility class that can log events for a tree of SQLObjects, and make snapshots of the
     * state of that tree on demand.  The snapshots can be rolled back using the event log
     * (this is similar functionality to the undo manager, but does not depend on the undo manager
     * in any way), and compared to older snapshots.  Old snapshots can also be rolled forward
     * (like an undo manager redo operation) to test that a stream of events is fully redoable.
     */
    public static class EventLogger implements SQLObjectListener, ca.sqlpower.architect.undo.UndoCompoundEventListener {

        /**
         * The list of events captured from the SQLObject tree.  Events are stored in the order they
         * are recieved, oldest first.
         */
        private List<LogItem> log = new ArrayList<LogItem>();

        /**
         * Enumerates the event types for items that are stored in the log.
         */
        private enum LogItemType { INSERT, REMOVE, CHANGE, STRUCTURE_CHANGE }

        /**
         * An item in the log.  It has a type (based on which listener method was invoked) and the
         * event that the listener recieved.
         */
        private static class LogItem {
            private LogItemType type;
            private SQLObjectEvent event;
            public LogItem(LogItemType type, SQLObjectEvent event) {
                this.type = type;
                this.event = event;
            }
            
            public LogItemType getType() {
                return type;
            }
            
            public SQLObjectEvent getEvent() {
                return event;
            }
            
            @Override
            public String toString() {
                return type+": "+event;
            }
        }
        
        /**
         * Adds an item to the end of the event log.
         *
         * @param type The event type (based on which listener method was called)
         * @param e The event.  Must not be null.
         */
        private void addToLog(LogItemType type, SQLObjectEvent e) {
            if (e == null) throw new NullPointerException("Can't add null events, dude");
            log.add(new LogItem(type, e));
        }
        
        /**
         * Listener method.  Adds the received event to the log.
         */
        public void dbChildrenInserted(SQLObjectEvent e) {
            addToLog(LogItemType.INSERT, e);
        }

        /**
         * Listener method.  Adds the received event to the log.
         */
        public void dbChildrenRemoved(SQLObjectEvent e) {
            addToLog(LogItemType.REMOVE, e);
        }

        /**
         * Listener method.  Adds the received event to the log.
         */
        public void dbObjectChanged(SQLObjectEvent e) {
            addToLog(LogItemType.CHANGE, e);
            // FIXME have to unlisten to old objects and listen to new ones
        }

        /**
         * Listener method.  Throws UnsupportedOperationException when called, because we are trying
         * to wean the object model from StructureChangeEvents, because they are not undoable.
         * Failing early will help to weed these events out of the object model.
         */
        public void dbStructureChanged(SQLObjectEvent e) {
            throw new UnsupportedOperationException("Structure changes are not undoable");
        }

        public void compoundEditStart(UndoCompoundEvent e) {
            // whatever
        }

        public void compoundEditEnd(UndoCompoundEvent e) {
            // whatever
        }
        
        /**
         * Holds a snapshot of all property values in a SQLObject instance at one particular point
         * in time. Also allows rolling property change-type events forward and backward.  This is
         * different from the UndoManager in that it does not operate on actual SQLObjects, just
         * snapshots of their properties.
         */
        public static class SQLObjectSnapshot {
            
            /**
             * A collection of properties to ignore when creating a snaphsot of
             * a SQLObject. (These ignored properties interfere with the comparison
             * process which checks if the roll forward/roll back operation reproduced
             * the identical object state).
             */
            final static Map<Class, Set<String>> ignoreProperties;
            
            static {
                ignoreProperties = new HashMap<Class, Set<String>>();
                
                Set<String> set = new HashSet<String>();
                set.add("columns");  // tracked by the snapshot's "children" list
                set.add("pkSize");   // depends on number of columns with non-null PKSeq
                set.add("SQLObjectListeners"); // interferes with EventLogger, which listens to all objects
                ignoreProperties.put(SQLTable.class, set);

                set = new HashSet<String>();
                set.add("children");  // tracked by the snapshot's "children" list
                set.add("childCount"); // tracked by the snapshot's "children" list
                set.add("SQLObjectListeners"); // interferes with EventLogger, which listens to all objects
                ignoreProperties.put(SQLTable.Folder.class, set);

                set = new HashSet<String>();
                set.add("definitelyNullable");  // secondary property depends on nullable
                set.add("primaryKey");          // secondary property depends on position in parent is isInPk
                set.add("SQLObjectListeners"); // interferes with EventLogger, which listens to all objects
                ignoreProperties.put(SQLColumn.class, set);
                
                set = new HashSet<String>();
                set.add("children");  // tracked by the snapshot's "children" list
                set.add("childCount"); // tracked by the snapshot's "children" list
                set.add("SQLObjectListeners"); // interferes with EventLogger, which listens to all objects
                ignoreProperties.put(SQLIndex.class, set);
                
                set = new HashSet<String>();
                set.add("SQLObjectListeners"); // interferes with EventLogger, which listens to all objects
                ignoreProperties.put(SQLIndex.Column.class, set);

            }
            
            /**
             * The snapshot (BeanUtils.describe format) of the snapshotted object's properties.
             * we use the sortedMap there because we need the key sorted, so the toString() of 
             * the properties will come out in a consistant order.
             */
            private SortedMap<String,Object> properties;
            
            /**
             * Snapshots of the snapshotted object's children at the time of the snapshot.
             */
            private List<SQLObjectSnapshot> children;
            
            /**
             * The snapshotted object's identity hash code (from System.identityHashCode()).
             */
            private int snapshottedObjectIdentity;
            
            /**
             * The class of the snapshotted object.
             */
            private Class snapshottedObjectClass;
         
            public SQLObjectSnapshot(SQLObject object) 
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArchitectException {
                snapshottedObjectClass = object.getClass();
                snapshottedObjectIdentity = System.identityHashCode(object);
                children = new ArrayList<SQLObjectSnapshot>();
                
                properties = new TreeMap<String, Object>(BeanUtils.describe(object));
                if (ignoreProperties.containsKey(object.getClass())) {
                    for (String propName : ignoreProperties.get(object.getClass())) {
                        properties.remove(propName);
                    }
                }
                
                for (SQLObject c : (List<SQLObject>) object.getChildren()) {
                    children.add(new SQLObjectSnapshot(c));
                }
            }
            
            @Override
            public String toString() {
                StringBuffer sb = new StringBuffer();
                getPropertiesToString(sb,0);
                return sb.toString();
            }
            
            private void getPropertiesToString(StringBuffer buffer, int indent) {
                buffer.append(snapshottedObjectClass.getName());
                buffer.append("@").append(snapshottedObjectIdentity);
                buffer.append(" \"").append(properties.get("name")).append("\" ");
                
                // buffer.append(properties.toString());
                for (String key : properties.keySet()) {
                    buffer.append(key).append("=");
                    if (snapshottedObjectClass.getName().equals("ca.sqlpower.architect.SQLColumn") &&
                            key.equals("SQLObjectListeners")) {
                        buffer.append("xxx");
                    } else {
                        buffer.append(properties.get(key));
                    }
                    buffer.append(" ");
                }
                
                if (children.size() > 0) {
                    buffer.append("\n");
                    appendSpaces(buffer, indent);
                    buffer.append(children.size());
                    buffer.append(" children:");
                    for (SQLObjectSnapshot c : children) {
                        buffer.append("\n");
                        appendSpaces(buffer, indent + 1);
                        c.getPropertiesToString(buffer, indent + 1);
                    }
                }
            }
            
            /**
             * Appends the given number of spaces to the end of the given string buffer.
             */
            private void appendSpaces(StringBuffer sb, int spaces) {
                for (int i = 0; i < spaces; i++) {
                    sb.append(" ");
                }
            }
            

            public void insertChild(int i, SQLObject object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArchitectException {
                SQLObjectSnapshot snapshot = new SQLObjectSnapshot(object);
                children.add(i,snapshot);
            }

            public void removeChild(int i, SQLObject object) {
                SQLObjectSnapshot removed = children.remove(i);
                checkSnapshottedObject(object, removed, "removing child "+i);
            }

            private void checkSnapshottedObject(SQLObject object, SQLObjectSnapshot snapshot, String message) {
                StringBuffer wrongThing = new StringBuffer();
                if (!snapshot.getSnapshottedClass().equals(object.getClass())) {
                    wrongThing.append(" class");
                }
                
                // Skip the identity check for SQLIndex.Column because normalizePrimaryKey
                // recreates the objects every time it is called (which changes their identities)
                if (!snapshot.getSnapshottedClass().equals(SQLIndex.Column.class)) {
                    if (snapshot.getSnapshottedObjectIdentity() != System.identityHashCode(object)) {
                        wrongThing.append(" identity");
                    }
                }
                
                if (wrongThing.length() > 0) {
                    throw new IllegalStateException(
                            "Snapshot "+wrongThing+" mismatch. Expected: " +
                            snapshot.getSnapshottedClass().getName() + "@" + snapshot.getSnapshottedObjectIdentity() +
                            "; actual: " + object.getClass().getName() + "@" + System.identityHashCode(object) +
                            " while " + message);
                }
            }

            private int getSnapshottedObjectIdentity() {
                return snapshottedObjectIdentity;
            }

            private Class getSnapshottedClass() {
                return snapshottedObjectClass;
            }

            public void applyChange(SQLObjectEvent e) {
                if (!properties.containsKey(e.getPropertyName())) {
                    throw new IllegalStateException("the snapshotted object does not contain this property: " +
                            e.getPropertyName());
                }
                checkSnapshottedObject(e.getSQLSource(), this, "applying a property modification");
                properties.put(e.getPropertyName(),e.getNewValue());                
            }

            public void revertChange(SQLObjectEvent e) {
                if (!properties.containsKey(e.getPropertyName())) {
                    throw new IllegalStateException("this snapshotted object does not contain property: " +
                            e.getPropertyName());
                }
                checkSnapshottedObject(e.getSQLSource(), this, "reversing a property midification");
                properties.put(e.getPropertyName(), e.getOldValue());
            }

            /**
             * Applies the given SQLObjectEvent to this snapshot, or the appropriate
             * descendant snapshot object.  If the appropriate snapshot is not a descendant
             * of this snapshot, no changes will be applied, and the return value of this
             * method will be false.  Otherwise, the change will be applied and this method
             * will return true.
             * 
             * @param type The event type
             * @param e The event itself
             * @param rollForward Controls whether this event is applied as a "roll-forward" event
             * (like a redo operation would do), or a roll-back (like an undo).
             * @return True if the snapshot tree rooted at this snapshot contains a snapshot
             * of the SQLObject e.getSource(); false if it does not.
             */
            public boolean applyEvent(LogItemType type, SQLObjectEvent e, boolean rollForward) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArchitectException {
                if (System.identityHashCode(e.getSource()) != getSnapshottedObjectIdentity()) {
                    for (SQLObjectSnapshot snap : children) {
                        if (snap.applyEvent(type, e, rollForward)) return true;
                    }
                    return false;
                } else {
                    if (type == LogItemType.STRUCTURE_CHANGE) {
                        throw new UnsupportedOperationException("Structure changes are not repeatable");
                    }
                    if (rollForward) {
                        System.out.println("Rolling forward a "+type+": "+e);
                        if (type == LogItemType.INSERT) {
                            for (int i = 0; i < e.getChangedIndices().length; i++) {
                                insertChild(e.getChangedIndices()[i], (SQLColumn) e.getChildren()[i]);
                            }
                        } else if (type == LogItemType.REMOVE) {
                            for (int i = 0; i < e.getChangedIndices().length; i++) {
                                removeChild(e.getChangedIndices()[i], e.getChildren()[i]);
                            }
                        } else if (type == LogItemType.CHANGE) {
                            applyChange(e);
                        } else {
                            throw new UnsupportedOperationException("Unknown log item type "+type);
                        }
                    } else {
                        System.out.println("Rolling back a "+type+": "+e);
                        if (type == LogItemType.INSERT) {
                            for (int i = 0; i < e.getChangedIndices().length; i++) {
                                removeChild(e.getChangedIndices()[i], e.getChildren()[i]);
                            }
                        } else if (type == LogItemType.REMOVE) {
                            for (int i = 0; i < e.getChangedIndices().length; i++) {
                                insertChild(e.getChangedIndices()[i], e.getChildren()[i]);
                            }
                        } else if (type == LogItemType.CHANGE) {
                            revertChange(e);
                        } else {
                            throw new UnsupportedOperationException("Unknown log item type "+type);
                        }
                    }
                    return true;
                }
            }
        }
        
        public SQLObjectSnapshot makeSQLObjectSnapshot(SQLTable t) throws Exception {
            return new SQLObjectSnapshot(t);
        }
        
        /**
         * Applies all the changes in this log to the given snapshot.
         * @param snapshot
         */
        public void rollForward(SQLObjectSnapshot snapshot) throws Exception {
            for (LogItem li : log) {
                LogItemType type = li.getType();
                SQLObjectEvent e = li.getEvent();
                snapshot.applyEvent(type, e, true);
            }
        }
        
        /**
         * Reverts all the changes in this log on the given snapshot, in reverse order.
         * @param snapshot
         */
        public void rollBack(SQLObjectSnapshot snapshot) throws Exception {
            List<LogItem> revlog = new ArrayList(log);
            Collections.reverse(revlog);
            for (LogItem li : revlog) {
                LogItemType type = li.getType();
                SQLObjectEvent e = li.getEvent();
                snapshot.applyEvent(type, e, false);
            }
        }
        
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (LogItem li : log) {
                sb.append(li.getType()).append(": ").append(li.getEvent()).append("\n");
            }
            return sb.toString();
        }
    }
    
}