package regress.ca.sqlpower.architect;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

public class ArchitectUtilsTest extends TestCase {

	SQLObject sqlo;
	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.listenToHierarchy(SQLObjectListener, SQLObject)'
	 */
	public void setUp()
	{
		sqlo = new SQLObject(){

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public SQLObject getParent() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected void setParent(SQLObject parent) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected void populate() throws ArchitectException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public String getShortDisplayName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean allowsChildren() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Class<? extends SQLObject> getChildType() {
				return null;
			}
			
		};
	}
	
	public void testListenToHierarchySQLObjectListenerSQLObject() throws ArchitectException {

		SQLObjectListener listener = new CountingSQLObjectListener();
		ArchitectUtils.listenToHierarchy(listener,sqlo);
		ArchitectUtils.listenToHierarchy(listener,sqlo);
		assertEquals("There are the wrong number of listeners",1,sqlo.getSQLObjectListeners().size());
		assertTrue("The wrong listener is listening",sqlo.getSQLObjectListeners().contains(listener));
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.listenToHierarchy(SQLObjectListener, SQLObject[])'
	 */
	public void testListenToHierarchySQLObjectListenerSQLObjectArray() {

	
	}

	public void testAddUndoListenerToHierarchy() throws ArchitectException{

		UndoCompoundEventListener listener = new UndoCompoundEventListener(){

			public void compoundEditStart(UndoCompoundEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void compoundEditEnd(UndoCompoundEvent e) {
				// TODO Auto-generated method stub
				
			}

		};
		ArchitectUtils.addUndoListenerToHierarchy(listener,sqlo);
		ArchitectUtils.addUndoListenerToHierarchy(listener,sqlo);
		assertEquals("There are the wrong number of listeners",1,sqlo.getUndoEventListeners().size());
		assertTrue("The wrong listener is listening",sqlo.getUndoEventListeners().contains(listener));
	}
	

	public void testUnlistenToHierarchySQLObjectListenerSQLObject() throws ArchitectException {

		SQLObjectListener listener = new CountingSQLObjectListener();
		ArchitectUtils.listenToHierarchy(listener,sqlo);
		ArchitectUtils.listenToHierarchy(listener,sqlo);
		assertEquals("There are the wrong number of listeners",1,sqlo.getSQLObjectListeners().size());
		assertTrue("The wrong listener is listening",sqlo.getSQLObjectListeners().contains(listener));
		ArchitectUtils.unlistenToHierarchy(listener,sqlo);
		assertEquals("There are the wrong number of listeners",0,sqlo.getSQLObjectListeners().size());
	}

	public void testUndoUnlistenToHierarchy() throws ArchitectException
	{

		UndoCompoundEventListener listener = new UndoCompoundEventListener(){

			public void compoundEditStart(UndoCompoundEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void compoundEditEnd(UndoCompoundEvent e) {
				// TODO Auto-generated method stub
				
			}

		};
		ArchitectUtils.addUndoListenerToHierarchy(listener,sqlo);
		ArchitectUtils.addUndoListenerToHierarchy(listener,sqlo);
		assertEquals("There are the wrong number of listeners",1,sqlo.getUndoEventListeners().size());
		assertTrue("The wrong listener is listening",sqlo.getUndoEventListeners().contains(listener));

		ArchitectUtils.undoUnlistenToHierarchy(listener,sqlo);
		assertEquals("There are the wrong number of listeners",0,sqlo.getUndoEventListeners().size());
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
