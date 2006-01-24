package regress.ca.sqlpower.architect;

import java.sql.SQLData;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class TestSQLCatalog extends SQLTestCase {

	public TestSQLCatalog(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getName()'
	 */
	public void testGetName() {
		SQLCatalog c = new SQLCatalog();
		assertNull(c.getName());
		c.setCatalogName("xxx");
		assertEquals(c.getName(),"xxx");
		
		c.setCatalogName("yyy");
		assertEquals(c.getName(),"yyy");
		assertEquals(c.getCatalogName(),"yyy");
		assertEquals(c.getShortDisplayName(),"yyy");
		assertEquals(c.toString(),"yyy");
		
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertEquals(c.getName(),"aaa");
		assertEquals(c.getCatalogName(),"aaa");
		assertEquals(c.getShortDisplayName(),"aaa");
		assertEquals(c.toString(),"aaa");
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getParent()'
	 */
	public void testGetParent() {
		SQLCatalog c = new SQLCatalog();
		assertEquals(c.getParent(),null);
		assertEquals(c.getParentDatabase(),null);
		
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertEquals(c.getParent(),mydb);
		assertEquals(c.getParentDatabase(),mydb);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.allowsChildren()'
	 */
	public void testAllowsChildren() {
		SQLCatalog c = new SQLCatalog();
		assertTrue(c.allowsChildren());
		
		SQLDatabase mydb = new SQLDatabase(db.getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertTrue(c.allowsChildren());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.isSchemaContainer()'
	 */
	public void testIsSchemaContainer() throws Exception {
		SQLCatalog c = new SQLCatalog();
		assertTrue(c.isSchemaContainer());
		c.addChild(new SQLTable());
		assertFalse(c.isSchemaContainer());
		c = new SQLCatalog();
		c.addChild(new SQLSchema(false));
		assertTrue(c.isSchemaContainer());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getNativeTerm()'
	 */
	public void testGetNativeTerm() {
		SQLCatalog c1 = new SQLCatalog();
		assertEquals(c1.getNativeTerm(),"catalog");
		c1.setNativeTerm(null);
		assertNull(c1.getNativeTerm());
		c1.setNativeTerm("AAA");
		assertEquals(c1.getNativeTerm(),"aaa");
		
		SQLCatalog c2 = new SQLCatalog(new SQLDatabase(db.getDataSource()),"x");
		assertEquals(c2.getNativeTerm(),"catalog");
		c2.setNativeTerm(null);
		assertNull(c2.getNativeTerm());
		c2.setNativeTerm("AAA");
		assertEquals(c2.getNativeTerm(),"aaa");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getPhysicalName()'
	 */
	public void testGetPhysicalName() {
		SQLCatalog c1 = new SQLCatalog();
		assertNull(c1.getPhysicalName());
		c1.setPhysicalName("aaa");
		assertNull(c1.getPhysicalName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.isPopulated()'
	 */
	public void testIsPopulated() throws Exception {
		SQLCatalog c1 = new SQLCatalog();
		assertFalse(c1.isPopulated());
		c1.setPopulated(true);
		assertTrue(c1.isPopulated());
		c1.setPopulated(false);
		assertFalse(c1.isPopulated());
		
		SQLCatalog c2 = new SQLCatalog(new SQLDatabase(db.getDataSource()),"x");
		c2.setPopulated(false);
		c2.populate();
		assertTrue(c2.isPopulated());
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

}
