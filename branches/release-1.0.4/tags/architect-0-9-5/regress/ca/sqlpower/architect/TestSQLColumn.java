package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.beanutils.BeanUtils;

public class TestSQLColumn extends SQLTestCase {

	/**
	 * Creates a wrapper around the normal test suite which runs the
	 * OneTimeSetup and OneTimeTearDown procedures.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(TestSQLColumn.class);
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
					" cow numeric(11) CONSTRAINT test1pk PRIMARY KEY,\n" +
					" moo varchar(10),\n" +
					" foo char(10))");
			
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
			mydb.disconnect();//  FIXME: this should be uncommented when bug 1005 is fixed
		}
	}

	/**
	 * One-time cleanup code.  The special {@link #suite()} method arranges for
	 * this method to be called one time before the individual tests are run.
	 */
	public static void oneTimeTearDown() {
		System.out.println("TestSQLColumn.oneTimeTearDown()");
        
	}

	
	// ============ Instance Variables =============

	/**
	 * A table with one primary key column.  Gets set up in setUp().
	 */
	private SQLTable table1pk;

	/**
	 * A table with no primary key.  Gets set up in setUp().
	 */
	private SQLTable table0pk;
	
	/**
	 * A table with three primary key columns.  Gets set up in setUp().
	 */
	private SQLTable table3pk;

	
	// ============= SetUp/TearDown and Utility Methods =============
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		table1pk = db.getTableByName("SQL_COLUMN_TEST_1PK");
		table0pk = db.getTableByName("SQL_COLUMN_TEST_0PK");
		table3pk = db.getTableByName("SQL_COLUMN_TEST_3PK");
	}
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
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
	 * Returns one of the columns of one of the tables that setUp made.  Right now, it's
	 * table1pk's first column.
	 * @throws ArchitectException 
	 */
	@Override
	protected SQLObject getSQLObjectUnderTest() throws ArchitectException {
		return table1pk.getColumn(0);
	}
	
	// ================= Constructor ====================
	
	public TestSQLColumn(String name) throws Exception {
		super(name);
	}

	
	// =============== The tests! ================
	
	public void testPopulateTable() throws ArchitectException {
		
		assertEquals("Table should have 4 folders as children",
				4, table1pk.getChildCount());
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
	
    public void testReferenceCountFiresEvents() {
        SQLColumn col = new SQLColumn();
        TestSQLObjectListener testListener = new TestSQLObjectListener();
        col.addSQLObjectListener(testListener);
        assertEquals("Strange the test listener has recieved events",testListener.getChangedCount(),0);
        col.addReference();
        assertEquals("Incorrect number of change events!",testListener.getChangedCount(),1);
        col.removeReference();
        assertEquals("Incorrect number of change events!",testListener.getChangedCount(),2);
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
		cowCol.setName(mooCol.getName());
		assertTrue(nameComp.compare(cowCol, mooCol) == 0);
	}
	
	public void testNoArgConstructor() throws Exception {
		SQLColumn col = new SQLColumn();
		assertEquals("Reference count init", 1, col.getReferenceCount());
		assertEquals(0, col.getChildCount());
	}
	public void testSmallConstructor() throws Exception {
		SQLColumn col = new SQLColumn(table0pk, "test_column", Types.INTEGER, 10, 30);
		assertEquals(table0pk, col.getParentTable());
		assertEquals("test_column", col.getName());
		assertEquals(Types.INTEGER, col.getType());
		assertEquals(10, col.getPrecision());
		assertEquals(30, col.getScale());
		assertEquals(1, col.getReferenceCount());
		assertEquals(0, col.getChildCount());
	}
	
	public void testMegaConstructor() throws Exception {
		SQLColumn col = new SQLColumn(table0pk,
				"test_column_2", Types.INTEGER, "my_test_integer",
				44, 33, DatabaseMetaData.columnNullable, "test remarks",
				"test default", null, true);
		assertEquals(table0pk, col.getParentTable());
		assertEquals("test_column_2", col.getName());
		assertEquals(Types.INTEGER, col.getType());
		assertEquals("my_test_integer", col.getSourceDataTypeName());
		assertEquals(44, col.getPrecision());
		assertEquals(33, col.getScale());
		assertEquals(DatabaseMetaData.columnNullable, col.getNullable());
		assertEquals("test remarks", col.getRemarks());
		assertEquals("test default", col.getDefaultValue());
		assertEquals(null, col.getPrimaryKeySeq());
		assertTrue(col.isAutoIncrement());
		assertEquals(1, col.getReferenceCount());
		assertEquals(0, col.getChildCount());
	}
	
	public void testGetDerivedInstance() throws Exception {
		SQLColumn origCol = table1pk.getColumn(0);
		SQLColumn derivCol = SQLColumn.getDerivedInstance(origCol, table3pk);
		
		// These should be the only two differences between origCol and derivCol
		assertEquals(table3pk, derivCol.getParentTable());
		assertEquals(origCol, derivCol.getSourceColumn());
		assertEquals("NUMBER", derivCol.getSourceDataTypeName());
		
		Map origProps = BeanUtils.describe(origCol);
		Map derivProps = BeanUtils.describe(derivCol);
		
		derivProps.remove("parentTable");
		derivProps.remove("parent");
		derivProps.remove("sourceColumn");
		derivProps.remove("sourceDataTypeName");
        derivProps.remove("SQLObjectListeners");
        origProps.remove("SQLObjectListeners");
		origProps.remove("parentTable");
		origProps.remove("parent");
		origProps.remove("sourceColumn");
		origProps.remove("sourceDataTypeName");
		assertEquals("Derived instance properties differ from original",
				origProps.toString(), derivProps.toString());
	}
	

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getName()'
	 */
	public void testGetName() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		tmpCol.setName("cow");
		assertEquals(tmpCol.getName(),"cow");
		
		SQLColumn cowCol = table0pk.getColumnByName("COW");
		assertEquals(cowCol.getName(),"COW");
		
		SQLColumn mooCol = table0pk.getColumn(1);
		assertEquals(mooCol.getName(), "MOO");
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getPhysicalName()'
	 */
	public void testGetPhysicalName() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		tmpCol.setPhysicalName("cow");
		assertEquals(tmpCol.getPhysicalName(),"cow");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getParent()'
	 */
	public void testGetParent() throws ArchitectException {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(tmpCol.getParent(),null);
		table0pk.addColumn(tmpCol);
		assertEquals(table0pk.getColumnsFolder(),tmpCol.getParent());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.populate()'
	 */
	public void testPopulate() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(true,tmpCol.isPopulated());
		tmpCol.populate();
		assertEquals(true,tmpCol.isPopulated());
		
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(true,cowCol.isPopulated());
		cowCol.populate();
		assertEquals(true,cowCol.isPopulated());
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getShortDisplayName()'
	 */
	public void testGetShortDisplayName() throws Exception {
		SQLColumn cowCol = table1pk.getColumn(0);
		int idx = cowCol.getShortDisplayName().indexOf("COW");
		assertFalse(idx<0);		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.allowsChildren()'
	 */
	public void testAllowsChildren() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(false,tmpCol.allowsChildren());
		
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(false,cowCol.allowsChildren());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.addColumnsToTable(SQLTable, String, String, String)'
	 */
	public void testAddColumnsToTable() throws Exception {
		SQLColumn mooCol = table3pk.getColumn(1);
		assertEquals(mooCol.getParentTable(),table3pk);
		assertEquals(mooCol.getChildCount(),0);
		assertEquals(mooCol.getName(),"MOO");
		assertEquals(mooCol.getScale(),0);
		assertEquals(mooCol.getPrecision(),10);
		assertEquals(mooCol.getType(),Types.VARCHAR);
		assertEquals(mooCol.getSourceDataTypeName(),"VARCHAR2");
		assertEquals(mooCol.getRemarks(),"");
		assertEquals(mooCol.getDefaultValue(),null);
		assertEquals(mooCol.isPrimaryKey(),true);
		assertEquals(mooCol.isDefinitelyNullable(),false);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.toString()'
	 */
	public void testToString() throws Exception {
		SQLColumn cowCol = table1pk.getColumn(0);
		int idx = cowCol.getShortDisplayName().indexOf("COW");
		assertFalse(idx<0);	
	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getSourceColumn()'
	 */
	public void testGetSourceColumn() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertNull(tmpCol.getSourceColumn());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertNull(cowCol.getSourceColumn());
		
		tmpCol = SQLColumn.getDerivedInstance(cowCol, table3pk);
		assertEquals(table3pk, tmpCol.getParentTable());
		assertEquals(cowCol, tmpCol.getSourceColumn());
		
		tmpCol = SQLColumn.getDerivedInstance(new SQLColumn(), table3pk);
		assertNotNull(tmpCol.getSourceColumn());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.setSourceColumn(SQLColumn)'
	 */
	public void testSetSourceColumn() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertNull(tmpCol.getSourceColumn());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertNull(cowCol.getSourceColumn());
		
		tmpCol.setSourceColumn(cowCol);
		assertEquals(cowCol, tmpCol.getSourceColumn());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getType()'
	 */
	public void testGetType() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(Types.INTEGER,tmpCol.getType());
		tmpCol.setType(Types.CHAR);
		assertEquals(Types.CHAR,tmpCol.getType());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(Types.DECIMAL,cowCol.getType());
		cowCol.setType(Types.CHAR);
		assertEquals(Types.CHAR,cowCol.getType());
	}

	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getSourceDataTypeName()'
	 */
	public void testGetSourceDataTypeName() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(null,tmpCol.getSourceDataTypeName());
		tmpCol.setSourceDataTypeName("xxx");
		assertEquals("xxx",tmpCol.getSourceDataTypeName());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals("NUMBER",cowCol.getSourceDataTypeName());
		cowCol.setSourceDataTypeName("yyy");
		assertEquals("yyy",cowCol.getSourceDataTypeName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getScale()'
	 */
	public void testGetScale() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(0,tmpCol.getScale());
		tmpCol.setScale(123);
		assertEquals(123,tmpCol.getScale());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(0,cowCol.getScale());
		cowCol.setScale(321);
		assertEquals(321,cowCol.getScale());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getPrecision()'
	 */
	public void testGetPrecision() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(10,tmpCol.getPrecision());
		tmpCol.setPrecision(123);
		assertEquals(123,tmpCol.getPrecision());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(11,cowCol.getPrecision());
		cowCol.setPrecision(321);
		assertEquals(321,cowCol.getPrecision());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.isDefinitelyNullable()'
	 */
	public void testIsDefinitelyNullable() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(false,tmpCol.isDefinitelyNullable());
		tmpCol.setNullable(DatabaseMetaData.columnNullable);
		assertEquals(true,tmpCol.isDefinitelyNullable());
		tmpCol.setNullable(DatabaseMetaData.columnNoNulls);
		assertEquals(false,tmpCol.isDefinitelyNullable());
		tmpCol.setNullable(DatabaseMetaData.columnNullableUnknown);
		assertEquals(false,tmpCol.isDefinitelyNullable());
		
		SQLColumn cowCol = table1pk.getColumn(0);

		assertEquals(false,cowCol.isDefinitelyNullable());
		cowCol.setNullable(DatabaseMetaData.columnNullable);
		assertEquals(true,cowCol.isDefinitelyNullable());
		cowCol.setNullable(DatabaseMetaData.columnNoNulls);
		assertEquals(false,cowCol.isDefinitelyNullable());
		cowCol.setNullable(DatabaseMetaData.columnNullableUnknown);
		assertEquals(false,cowCol.isDefinitelyNullable());
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.isPrimaryKey()'
	 */
	public void testIsPrimaryKey() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(false,tmpCol.isPrimaryKey());
		tmpCol.setPrimaryKeySeq(new Integer(1));
		assertEquals(true,tmpCol.isPrimaryKey());
		tmpCol.setPrimaryKeySeq(null);
		assertEquals(false,tmpCol.isPrimaryKey());
		
		SQLColumn cowCol = table3pk.getColumn(0);
		assertEquals(true,cowCol.isPrimaryKey());
		cowCol.setPrimaryKeySeq(new Integer(2));
		assertEquals(true,cowCol.isPrimaryKey());
		cowCol.setPrimaryKeySeq(null);
		assertEquals(false,cowCol.isPrimaryKey());
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getParentTable()'
	 */
	public void testGetParentTable() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(null,tmpCol.getParentTable());
		table0pk.addColumn(tmpCol);
		assertEquals(table0pk,tmpCol.getParentTable());
		table0pk.removeColumn(tmpCol);
		assertEquals(null,tmpCol.getParentTable());

		SQLColumn cowCol = table3pk.getColumn(0);
		assertEquals(table3pk,cowCol.getParentTable());
		table3pk.removeColumn(cowCol);
		assertEquals(null,cowCol.getParentTable());
		
		table0pk.addColumn(cowCol);
		assertEquals(table0pk,cowCol.getParentTable());
		table0pk.removeColumn(cowCol);
		assertEquals(null,cowCol.getParentTable());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getNullable()'
	 */
	public void testGetNullable() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(DatabaseMetaData.columnNoNulls,tmpCol.getNullable());
		tmpCol.setNullable(DatabaseMetaData.columnNullable);
		assertEquals(DatabaseMetaData.columnNullable,tmpCol.getNullable());
		tmpCol.setNullable(DatabaseMetaData.columnNullableUnknown);
		assertEquals(DatabaseMetaData.columnNullableUnknown,tmpCol.getNullable());
		tmpCol.setNullable(DatabaseMetaData.columnNoNulls);
		assertEquals(DatabaseMetaData.columnNoNulls,tmpCol.getNullable());
		
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(DatabaseMetaData.columnNoNulls,cowCol.getNullable());
		cowCol.setNullable(DatabaseMetaData.columnNullable);
		assertEquals(DatabaseMetaData.columnNullable,cowCol.getNullable());
		cowCol.setNullable(DatabaseMetaData.columnNullableUnknown);
		assertEquals(DatabaseMetaData.columnNullableUnknown,cowCol.getNullable());
		cowCol.setNullable(DatabaseMetaData.columnNoNulls);
		assertEquals(DatabaseMetaData.columnNoNulls,cowCol.getNullable());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getRemarks()'
	 */
	public void testGetRemarks() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals("",tmpCol.getRemarks());
		tmpCol.setRemarks("xxx");
		assertEquals("xxx",tmpCol.getRemarks());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals("",cowCol.getRemarks());
		cowCol.setRemarks("yyy");
		assertEquals("yyy",cowCol.getRemarks());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getDefaultValue()'
	 */
	public void testGetDefaultValue() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(null,tmpCol.getDefaultValue());
		tmpCol.setDefaultValue("xxx");
		assertEquals("xxx",tmpCol.getDefaultValue());
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(null,cowCol.getDefaultValue());
		cowCol.setDefaultValue("yyy");
		assertEquals("yyy",cowCol.getDefaultValue());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.getPrimaryKeySeq()'
	 */
	public void testGetPrimaryKeySeq() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(null,tmpCol.getPrimaryKeySeq());
		tmpCol.setPrimaryKeySeq(new Integer(2));
		assertEquals(new Integer(2),tmpCol.getPrimaryKeySeq());

        for (SQLColumn column : table1pk.getColumns()) {
            System.out.println("column: " + column.getName() + "  seq="+column.getPrimaryKeySeq());
        }
        
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(Integer.valueOf(0),cowCol.getPrimaryKeySeq());
		cowCol.setPrimaryKeySeq(new Integer(20));
		assertEquals(new Integer(0),cowCol.getPrimaryKeySeq());
		
		table1pk.addColumn(tmpCol);
		tmpCol.setPrimaryKeySeq(new Integer( cowCol.getPrimaryKeySeq().intValue()-1));
		assertEquals(new Integer(1),cowCol.getPrimaryKeySeq());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.isAutoIncrement()'
	 */
	public void testIsAutoIncrement() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(false,tmpCol.isAutoIncrement());
		tmpCol.setAutoIncrement(true);
		assertEquals(true,tmpCol.isAutoIncrement());
		tmpCol.setAutoIncrement(false);
		assertEquals(false,tmpCol.isAutoIncrement());
		
		SQLColumn cowCol = table1pk.getColumn(0);
		assertEquals(false,cowCol.isAutoIncrement());
		cowCol.setAutoIncrement(true);
		assertEquals(true,cowCol.isAutoIncrement());
		cowCol.setAutoIncrement(false);
		assertEquals(false,cowCol.isAutoIncrement());
	}

	/*
	 * Test method for SQLColumn(SQLColumn)
	 */
	public void testCopyConstructor() throws Exception {
		SQLColumn cowCol = table1pk.getColumn(0);
		SQLColumn tmpCol = new SQLColumn(cowCol);
		
		Set<String> propsToIgnore = new HashSet<String>();
		propsToIgnore.add("parentTable");
		propsToIgnore.add("parent");
        propsToIgnore.add("SQLObjectListeners");
		
		Map<String,Object> origProps = (Map<String,Object>) BeanUtils.describe(cowCol);
		Map<String,Object> derivProps = (Map<String,Object>) BeanUtils.describe(tmpCol);
		
		origProps.keySet().removeAll(propsToIgnore);
		derivProps.keySet().removeAll(propsToIgnore);

		assertEquals("clone column properties differ from original",
				origProps.toString(), derivProps.toString());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLColumn.addReference()'
	 */
	public void testAddReference() throws Exception {
		int count = 1;
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(count,tmpCol.getReferenceCount());
		tmpCol.addReference();
		count++;
		assertEquals(count,tmpCol.getReferenceCount());
		
		tmpCol.addReference();
		count++;
		assertEquals(count,tmpCol.getReferenceCount());
		
		tmpCol.removeReference();
		count--;
		assertEquals(count,tmpCol.getReferenceCount());
		
	
		SQLColumn cowCol = table1pk.getColumn(0);
		count = 1;
		assertEquals(count,cowCol.getReferenceCount());
		cowCol.addReference();
		count++;
		assertEquals(count,cowCol.getReferenceCount());
		
		cowCol.addReference();
		count++;
		assertEquals(count,cowCol.getReferenceCount());
		
		cowCol.removeReference();
		count--;
		assertEquals(count,cowCol.getReferenceCount());
		
	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setPopulated(boolean)'
	 */
	public void testSetPopulated() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		tmpCol.setPopulated(false);
		assertEquals(tmpCol.isPopulated(),true);
		tmpCol.setPopulated(true);
		assertEquals(tmpCol.isPopulated(),true);
		
		SQLColumn cowCol = table3pk.getColumn(0);
		cowCol.setPopulated(false);
		assertEquals(cowCol.isPopulated(),true);
		cowCol.setPopulated(true);
		assertEquals(cowCol.isPopulated(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildren()'
	 */
	public void testGetChildren() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		assertEquals(tmpCol.getChildren().isEmpty(),true);
		
		try {
			tmpCol.addChild(1,new SQLColumn());
			fail("SQLColumn should not have child");
		} catch (UnsupportedOperationException e) {
			/* it's normal */
		}
		assertEquals(tmpCol.getChildren().size(),0);
		
		SQLColumn cowCol = table3pk.getColumn(0);
		assertEquals(cowCol.getChildren().isEmpty(),true);
		
		try {
			cowCol.addChild(1,new SQLColumn());
			fail("SQLColumn should not have child");
		} catch (UnsupportedOperationException e) {
			/* it's normal */
		}
		assertEquals(cowCol.getChildren().size(),0);
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChild(int)'
	 */
	public void testGetChild() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		try {
			tmpCol.getChild(0);
			tmpCol.getChild(1);
		} catch (IndexOutOfBoundsException e1) {
			// it's normal
		}
				
		SQLColumn cowCol = table3pk.getColumn(0);
		try {
			cowCol.getChild(0);
			cowCol.getChild(1);
		} catch (IndexOutOfBoundsException e1) {
			// it's normal
		}
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildCount()'
	 */
	public void testGetChildCount() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		int count = tmpCol.getChildCount();
		assertEquals(count,0);

		SQLColumn cowCol = table3pk.getColumn(0);
		count = cowCol.getChildCount();
		assertEquals(count,0);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(int, SQLObject)'
	 */
	public void testAddChildIntSQLObject() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		try {
			tmpCol.addChild(0,table1pk);
			fail();
			tmpCol.addChild(2,table3pk);
			fail();
			
		} catch ( UnsupportedOperationException e ) {
		}


		SQLColumn cowCol = table3pk.getColumn(0);
		try {
			cowCol.addChild(0,table1pk);
			fail();
			cowCol.addChild(2,table3pk);
			fail();
		} catch ( UnsupportedOperationException e ) {
		}
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(SQLObject)'
	 */
	public void testAddChildSQLObject() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		try {
			tmpCol.addChild(table1pk);
			fail();
			tmpCol.addChild(table3pk);
			fail();
		} catch ( UnsupportedOperationException e ) {
		}


		SQLColumn cowCol = table3pk.getColumn(0);
		try {
			cowCol.addChild(table1pk);
			fail();
			cowCol.addChild(table3pk);
			fail();
		} catch ( UnsupportedOperationException e ) {
		}
	}

	public static class TestSQLObjectListener implements SQLObjectListener {

		private int insertedCount;
		private int removedCount;
		private int changedCount;
		private int structureChangedCount;
		private String lastEventName;

		public TestSQLObjectListener() {
			insertedCount = 0;
			removedCount = 0;
			changedCount = 0;
			structureChangedCount = 0;
			lastEventName = null;
		}
		public void dbChildrenInserted(SQLObjectEvent e) {
			insertedCount++;
		}

		public void dbChildrenRemoved(SQLObjectEvent e) {
			removedCount++;
		}

		public void dbObjectChanged(SQLObjectEvent e) {
			changedCount++;
			lastEventName = e.getPropertyName();
		}

		public void dbStructureChanged(SQLObjectEvent e) {
			structureChangedCount++;
			
		}

		public int getInsertedCount() {
			return insertedCount;
		}

		public int getRemovedCount() {
			return removedCount;
		}
		public int getChangedCount() {
			return changedCount;
		}
		public int getStructureChangedCount() {
			return structureChangedCount;
		}
		public String getLastEventName() {
			return lastEventName;
		}
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getSQLObjectListeners()'
	 */
	public void testGetSQLObjectListeners() throws Exception {
		SQLColumn tmpCol = new SQLColumn();
		SQLColumn cowCol = table3pk.getColumn(0);
		
        LinkedList ll = (LinkedList) tmpCol.getSQLObjectListeners();
		assertEquals(0,ll.size());
		TestSQLObjectListener test1 = new TestSQLObjectListener();
		tmpCol.addSQLObjectListener(test1);
		assertEquals(1,ll.size());
		TestSQLObjectListener test2 = new TestSQLObjectListener();
		tmpCol.addSQLObjectListener(test2);
		assertEquals(2,ll.size());
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		tmpCol.setName("xxx");
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),1);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),1);
		assertEquals(test2.getStructureChangedCount(),0);
		
		tmpCol.setName("xxx");
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),1);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),1);
		assertEquals(test2.getStructureChangedCount(),0);
		
		tmpCol.removeSQLObjectListener(test1);
		assertEquals(1,ll.size());
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),1);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),1);
		assertEquals(test2.getStructureChangedCount(),0);
		
		tmpCol.setName("xxxx");
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),1);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(0,test2.getInsertedCount());
		assertEquals(0,test2.getRemovedCount());
		assertEquals(2,test2.getChangedCount());
		assertEquals(0,test2.getStructureChangedCount());
		
		assertEquals("name",test2.getLastEventName());
		ll = (LinkedList) cowCol.getSQLObjectListeners();
        // We have one listener because the index is listening to this column
		assertEquals(ll.toString(),1,ll.size());
		test1 = new TestSQLObjectListener();
		cowCol.addSQLObjectListener(test1);
		assertEquals(2,ll.size());
		test2 = new TestSQLObjectListener();
		cowCol.addSQLObjectListener(test2);
		assertEquals(3,ll.size());
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),0);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),0);
		assertEquals(test2.getStructureChangedCount(),0);
		
		cowCol.setName("xxx");
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),1);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),1);
		assertEquals(test2.getStructureChangedCount(),0);
		
		cowCol.setName("xxxx");
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),2);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),2);
		assertEquals(test2.getStructureChangedCount(),0);
		
		cowCol.removeSQLObjectListener(test1);
		assertEquals(2,ll.size());
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),2);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),2);
		assertEquals(test2.getStructureChangedCount(),0);
		
		cowCol.setName("xxx");
		
		assertEquals(test1.getInsertedCount(),0);
		assertEquals(test1.getRemovedCount(),0);
		assertEquals(test1.getChangedCount(),2);
		assertEquals(test1.getStructureChangedCount(),0);
		
		assertEquals(test2.getInsertedCount(),0);
		assertEquals(test2.getRemovedCount(),0);
		assertEquals(test2.getChangedCount(),3);
		assertEquals(test2.getStructureChangedCount(),0);
		
		assertEquals("name",test2.getLastEventName());
	}


}
