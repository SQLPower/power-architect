package regress.ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class TestSQLSchema extends SQLTestCase {

	public TestSQLSchema(String name) throws Exception {
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
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getName()'
	 */
	public void testGetName() {
		SQLSchema s1 = new SQLSchema(true);
		assertNull(s1.getName());
		
		SQLSchema s2 = new SQLSchema(s1,"xxx",true);
		assertEquals("xxx",s2.getName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getParent()'
	 */
	public void testGetParent() {
		SQLSchema s1 = new SQLSchema(true);
		assertNull(s1.getParent());
		
		SQLSchema s2 = new SQLSchema(s1,"xxx",true);
		assertEquals(s1,s2.getParent());
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.populate()'
	 */
	public void testPopulate() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.isPopulated(),true);
		s1.populate();
		assertEquals(s1.isPopulated(),true);
		
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									db.getDataSource().getPlSchema(),
									false);
		assertEquals(s2.isPopulated(),false);
		s2.populate();
		assertEquals(s2.isPopulated(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.allowsChildren()'
	 */
	public void testAllowsChildren() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.allowsChildren(),true);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									db.getDataSource().getPlSchema(),
									false);
		assertEquals(s2.allowsChildren(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getTableByName(String)'
	 */
	public void testGetTableByName() throws Exception {
		SQLSchema s1 = new SQLSchema(true);
		SQLTable t1 = s1.getTableByName("REGRESSION_TEST1");
		assertNull(t1);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									db.getDataSource().getUser(),
									false);
		
		System.out.println(db.getChildren());
		System.out.println("schema=["+db.getDataSource().getUser()+"]");
		System.out.println("children count="+s2.getChildCount());
		System.out.println(s2.getChildren());
		
		t1 = s2.getTableByName("REGRESSION_TEST1");
		assertNotNull(t1);
		assertEquals(t1.getName(),"REGRESSION_TEST1");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.isParentTypeDatabase()'
	 */
	public void testIsParentTypeDatabase() {
		SQLSchema s1 = new SQLSchema(true);
		assertEquals(s1.isParentTypeDatabase(),false);
		
		SQLSchema s2 = new SQLSchema(new SQLDatabase(db.getDataSource()),
									db.getDataSource().getPlSchema(),
									false);
		assertEquals(s2.isParentTypeDatabase(),true);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getSchemaName()'
	 */
	public void testGetSchemaName() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.setSchemaName(String)'
	 */
	public void testSetSchemaName() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.getNativeTerm()'
	 */
	public void testGetNativeTerm() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLSchema.setNativeTerm(String)'
	 */
	public void testSetNativeTerm() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getPhysicalName()'
	 */
	public void testGetPhysicalName() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setPhysicalName(String)'
	 */
	public void testSetPhysicalName() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.isPopulated()'
	 */
	public void testIsPopulated() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setPopulated(boolean)'
	 */
	public void testSetPopulated() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildren()'
	 */
	public void testGetChildren() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setChildren(List)'
	 */
	public void testSetChildren() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChild(int)'
	 */
	public void testGetChild() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildCount()'
	 */
	public void testGetChildCount() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(int, SQLObject)'
	 */
	public void testAddChildIntSQLObject() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(SQLObject)'
	 */
	public void testAddChildSQLObject() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeChild(int)'
	 */
	public void testRemoveChildInt() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeChild(SQLObject)'
	 */
	public void testRemoveChildSQLObject() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeDependencies()'
	 */
	public void testRemoveDependencies() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getSQLObjectListeners()'
	 */
	public void testGetSQLObjectListeners() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addSQLObjectListener(SQLObjectListener)'
	 */
	public void testAddSQLObjectListener() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeSQLObjectListener(SQLObjectListener)'
	 */
	public void testRemoveSQLObjectListener() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildrenInserted(int[], List)'
	 */
	public void testFireDbChildrenInserted() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildInserted(int, SQLObject)'
	 */
	public void testFireDbChildInserted() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildrenRemoved(int[], List)'
	 */
	public void testFireDbChildrenRemoved() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbChildRemoved(int, SQLObject)'
	 */
	public void testFireDbChildRemoved() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbObjectChanged(String)'
	 */
	public void testFireDbObjectChanged() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.fireDbStructureChanged(String)'
	 */
	public void testFireDbStructureChanged() {

	}

	/*
	 * Test method for 'java.lang.Object.Object()'
	 */
	public void testObject() {

	}

	/*
	 * Test method for 'java.lang.Object.getClass()'
	 */
	public void testGetClass() {

	}

	/*
	 * Test method for 'java.lang.Object.hashCode()'
	 */
	public void testHashCode() {

	}

	/*
	 * Test method for 'java.lang.Object.equals(Object)'
	 */
	public void testEquals() {

	}

	/*
	 * Test method for 'java.lang.Object.clone()'
	 */
	public void testClone() {

	}

	/*
	 * Test method for 'java.lang.Object.toString()'
	 */
	public void testToString1() {

	}

	/*
	 * Test method for 'java.lang.Object.notify()'
	 */
	public void testNotify() {

	}

	/*
	 * Test method for 'java.lang.Object.notifyAll()'
	 */
	public void testNotifyAll() {

	}

	/*
	 * Test method for 'java.lang.Object.wait(long)'
	 */
	public void testWaitLong() {

	}

	/*
	 * Test method for 'java.lang.Object.wait(long, int)'
	 */
	public void testWaitLongInt() {

	}

	/*
	 * Test method for 'java.lang.Object.wait()'
	 */
	public void testWait() {

	}

	/*
	 * Test method for 'java.lang.Object.finalize()'
	 */
	public void testFinalize() {

	}

}
