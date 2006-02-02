package regress.ca.sqlpower.architect;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectUtils;

public class ArchitectUtilsTest extends TestCase {

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.listenToHierarchy(SQLObjectListener, SQLObject)'
	 */
	public void testListenToHierarchySQLObjectListenerSQLObject() {
		System.out.println(
			"ArchitectUtilsTest.testListenToHierarchySQLObjectListenerSQLObject() Important test still to be written!!");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.listenToHierarchy(SQLObjectListener, SQLObject[])'
	 */
	public void testListenToHierarchySQLObjectListenerSQLObjectArray() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.unlistenToHierarchy(SQLObjectListener, SQLObject)'
	 */
	public void testUnlistenToHierarchySQLObjectListenerSQLObject() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.unlistenToHierarchy(SQLObjectListener, SQLObject[])'
	 */
	public void testUnlistenToHierarchySQLObjectListenerSQLObjectArray() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.areEqual(Object, Object)'
	 */
	public void testAreEqual() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.findColumnsSourcedFromDatabase(SQLDatabase, SQLDatabase)'
	 */
	public void testFindColumnsSourcedFromDatabase() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.pokeDatabase(SQLObject)'
	 */
	public void testPokeDatabase() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.countTablesSnapshot(SQLObject)'
	 */
	public void testCountTablesSnapshot() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.findDescendentsByClass(SQLObject, Class, List)'
	 */
	public void testFindDescendentsByClass() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.truncateString(String)'
	 */
	public void testTruncateString() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.countTables(SQLObject)'
	 */
	public void testCountTables() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.getDriverTemplateMap()'
	 */
	public void testGetDriverTemplateMap() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.getDriverTyprMap()'
	 */
	public void testGetDriverTyprMap() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.getDriverDDLGeneratorMap()'
	 */
	public void testGetDriverDDLGeneratorMap() {

	}

	// Used by testEscapeXML
	private static String[][] sanitizeData = {
		// Test each one singly
		{ "a<b", "a&lt;b" },
		{ "a>b", "a&gt;b" },
		{ "a\"b", "a&quot;b" },
		{ "a\'b", "a&apos;b" },
		{ "a&b", "a&amp;b" },
		// Multiples
		{ "a<>b", "a&lt;&gt;b" },
		{ "a&&b", "a&amp;&amp;b" },
		{ "abc", "abc" },
		{ "<source-databases/>", "&lt;source-databases/&gt;" },
		{ "", "" },
	};
	
	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.escapeXML(String)'
	 */
	public void testEscapeXML() throws Exception {
		for (String[] td : sanitizeData) {
			assertEquals(td[1], ArchitectUtils.escapeXML(td[0]));
		}
		
	}
}
