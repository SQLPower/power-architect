/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect;

import junit.framework.TestCase;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.util.SQLPowerUtils;

public class ArchitectUtilsTest extends TestCase {

	SQLObject sqlo;
    
	public void setUp()	{
		sqlo = new StubSQLObject();
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

	    //listener that doesn't do anything
		UndoCompoundEventListener listener = new UndoCompoundEventListener(){
			public void compoundEditStart(UndoCompoundEvent e) {
			}
			public void compoundEditEnd(UndoCompoundEvent e) {
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
			}
			public void compoundEditEnd(UndoCompoundEvent e) {
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
			assertEquals(td[1], SQLPowerUtils.escapeXML(td[0]));
		}
		
	}
    
    public void testGetAncestor() throws ArchitectException {
        SQLDatabase parentdb = new SQLDatabase();
        SQLSchema sch = new SQLSchema(true);
        SQLTable t = new SQLTable(sch, "cows", "remarkable cows", "TABLE", true);
        
        parentdb.addChild(sch);
        sch.addChild(t);
        
        assertEquals(parentdb, ArchitectUtils.getAncestor(t.getColumnsFolder(), SQLDatabase.class));
        assertEquals(sch, ArchitectUtils.getAncestor(t.getColumnsFolder(), SQLSchema.class));
        assertEquals(t, ArchitectUtils.getAncestor(t.getColumnsFolder(), SQLTable.class));
        assertEquals(t.getColumnsFolder(), ArchitectUtils.getAncestor(t.getColumnsFolder(), SQLTable.Folder.class));
        assertNull(ArchitectUtils.getAncestor(t.getColumnsFolder(), SQLCatalog.class));
        
        parentdb.removeChild(sch);
        assertNull(ArchitectUtils.getAncestor(t.getColumnsFolder(), SQLDatabase.class));
    }
    
    public void testCreateTableWhenExisting() throws Exception {
        SQLDatabase db = new SQLDatabase();
        SQLCatalog cat = new SQLCatalog(db, "cat",true);
        db.addChild(cat);
        SQLSchema schem = new SQLSchema(cat, "schem", true);
        cat.addChild(schem);
        SQLTable tab = new SQLTable(schem, "tab", null, "TABLE", true);
        schem.addChild(tab);
        
        try {
            ArchitectUtils.addSimulatedTable(db, "cat", "schem", "tab");
            fail("Should not have been allowed because table exists");
        } catch (ArchitectException ex) {
            // expected
        }
    }
}
