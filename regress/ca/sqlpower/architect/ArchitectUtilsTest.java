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
