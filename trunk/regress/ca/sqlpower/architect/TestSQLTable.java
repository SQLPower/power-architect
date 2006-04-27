package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.beanutils.BeanUtils;

import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.TestSQLColumn.TestSQLObjectListener;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ColumnEditPanel;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

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
        SQLTable t = new SQLTable(ArchitectFrame.getMainInstance().getProject().getTargetDatabase(), true);
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
        ArchitectFrame af = ArchitectFrame.getMainInstance();
        SQLTable parentTable = new SQLTable(af.getProject().getPlayPen().getDatabase(), "parent", null, "TABLE", true);
        parentTable.addColumn(new SQLColumn(parentTable, "pkcol_1", Types.INTEGER, 10, 0));
        parentTable.addColumn(new SQLColumn(parentTable, "pkcol_2", Types.INTEGER, 10, 0));
        parentTable.addColumn(new SQLColumn(parentTable, "attribute_1", Types.INTEGER, 10, 0));
        
        SQLTable childTable1 = new SQLTable(af.getProject().getPlayPen().getDatabase(), "child_1", null, "TABLE", true);
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
    
    public void testChangeSFifthColumnIdxToSecond() throws ArchitectException{
        table.changeColumnIndex(4, 1, true);        
        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));       
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));
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
    
    public void testChangeFifthColumnKey() throws ArchitectException{
        SQLColumn col5 = table.getColumnByName("five");
        assertNotNull(col5);
        col5.setPrimaryKeySeq(0);       
        assertEquals(4, table.getPkSize());
        
        assertEquals(0, table.getColumnIndex(table.getColumnByName("one")));
        assertEquals(1, table.getColumnIndex(table.getColumnByName("five")));
        assertEquals(2, table.getColumnIndex(table.getColumnByName("two")));
        assertEquals(3, table.getColumnIndex(table.getColumnByName("three")));
        assertEquals(4, table.getColumnIndex(table.getColumnByName("four")));
        assertEquals(5, table.getColumnIndex(table.getColumnByName("six")));  
    }
    
    
    
    //------------Testing Undo/Redo for SQLTable Changes----------//
    public static class EventLogger implements SQLObjectListener, UndoCompoundEventListener {
        private List currentCompoundLog;
        private List log = new ArrayList();
        private int compoundDepth = 0;
        
        private void addEvent(SQLObjectEvent e) {
            if (currentCompoundLog != null) {
                currentCompoundLog.add(e);
            } else {
                log.add(e);
            }
        }
        public void dbChildrenInserted(SQLObjectEvent e) {
            log.add(e);
        }

        public void dbChildrenRemoved(SQLObjectEvent e) {
            log.add(e);
        }

        public void dbObjectChanged(SQLObjectEvent e) {
            log.add(e);
        }

        public void dbStructureChanged(SQLObjectEvent e) {
            log.add(e);
        }

        public void compoundEditStart(UndoCompoundEvent e) {
            compoundDepth++;
            if (compoundDepth == 1) {
                currentCompoundLog = new ArrayList();
            }
        }

        public void compoundEditEnd(UndoCompoundEvent e) {
            if (compoundDepth == 0) throw new IllegalStateException("Not in a compound edit");
            compoundDepth--;
            if (compoundDepth == 0) {
                log.add(currentCompoundLog);
                currentCompoundLog = null;
            }
        }
    }
    
    public void testUndoColumnIndexChange() throws ArchitectException{                   
        
    }
    
    public void testUndoColumnKeyChange(){
        
    }
}

