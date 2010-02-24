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

import java.sql.Types;
import junit.framework.TestCase;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObjectUtils;
import ca.sqlpower.sqlobject.CountingSQLObjectListener;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.StubSQLObject;
import ca.sqlpower.util.SQLPowerUtils;

public class ArchitectUtilsTest extends TestCase {

	SQLObject sqlo;
    
	public void setUp()	{
		sqlo = new StubSQLObject();
	}
	
	public void testListenToHierarchySQLObjectListenerSQLObject() throws SQLObjectException {

		SPListener listener = new CountingSQLObjectListener();
		SQLPowerUtils.listenToHierarchy(sqlo, listener);
		assertEquals("There are the wrong number of listeners",1,sqlo.getSPListeners().size());
		assertTrue("The wrong listener is listening",sqlo.getSPListeners().contains(listener));
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectUtils.listenToHierarchy(SQLObjectListener, SQLObject[])'
	 */
	public void testListenToHierarchySQLObjectListenerSQLObjectArray() {

	
	}

	public void testUnlistenToHierarchySQLObjectListenerSQLObject() throws SQLObjectException {

		SPListener listener = new CountingSQLObjectListener();
		SQLPowerUtils.listenToHierarchy(sqlo, listener);
		assertEquals("There are the wrong number of listeners",1,sqlo.getSPListeners().size());
		assertTrue("The wrong listener is listening",sqlo.getSPListeners().contains(listener));
		SQLPowerUtils.unlistenToHierarchy(sqlo, listener);
		assertEquals("There are the wrong number of listeners",0,sqlo.getSPListeners().size());
	}

	public void testColumnsDiffer() {
		SQLColumn col1 = new SQLColumn(null, "column_1", Types.VARCHAR, "VARCHAR", 25, 0, 0, "remarks", null, false);
		SQLColumn col2 = new SQLColumn(null, "column_1", Types.VARCHAR, "VARCHAR", 25, -1, 0, "remarks", null, false);
		assertFalse(ArchitectUtils.columnsDiffer(col1, col2));

		col1 = new SQLColumn(null, "column_1", Types.CHAR, "CHAR", 25, 0, 0, "remarks", null, false);
		col2 = new SQLColumn(null, "column_1", Types.CHAR, "CHAR", 25, -1, 0, "remarks", null, false);
		assertFalse(ArchitectUtils.columnsDiffer(col1, col2));

		col1 = new SQLColumn(null, "column_1", Types.INTEGER, "INTEGER", 33, 0, 0, "remarks", null, false);
		col2 = new SQLColumn(null, "column_1", Types.INTEGER, "LONGINT", 42, -1, 0, "remarks", null, false);
		assertFalse(ArchitectUtils.columnsDiffer(col1, col2));

		col1 = new SQLColumn(null, "column_1", Types.DECIMAL, "NUMBER", 1, 0, 0, "remarks", null, false);
		col2 = new SQLColumn(null, "column_1", Types.DECIMAL, "NUMBER", 2, 0, 0, "remarks", null, false);
		assertTrue(ArchitectUtils.columnsDiffer(col1, col2));

		col1 = new SQLColumn(null, "column_1", Types.NUMERIC, "NUMBER", 1, 0, 0, "remarks", null, false);
		col2 = new SQLColumn(null, "column_1", Types.DECIMAL, "NUMBER", 2, 0, 0, "remarks", null, false);
		assertTrue(ArchitectUtils.columnsDiffer(col1, col2));

		col1 = new SQLColumn(null, "column_1", Types.NUMERIC, "NUMBER", 10, 5, 0, "remarks", null, false);
		col2 = new SQLColumn(null, "column_1", Types.DECIMAL, "NUMBER", 10, 5, 0, "remarks", null, false);
		assertFalse(ArchitectUtils.columnsDiffer(col1, col2));
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
    
    public void testGetAncestor() throws SQLObjectException, IllegalArgumentException, ObjectDependentException {
        SQLDatabase parentdb = new SQLDatabase();
        SQLSchema sch = new SQLSchema(true);
        SQLTable t = new SQLTable(sch, "cows", "remarkable cows", "TABLE", true);
        
        parentdb.addChild(sch);
        sch.addChild(t);
        
        assertEquals(parentdb, SPObjectUtils.getAncestor(t, SQLDatabase.class));
        assertEquals(sch, SPObjectUtils.getAncestor(t, SQLSchema.class));
        assertEquals(t, SPObjectUtils.getAncestor(t, SQLTable.class));
        assertNull(SPObjectUtils.getAncestor(t, SQLCatalog.class));
        
        parentdb.removeChild(sch);
        assertNull(SPObjectUtils.getAncestor(t, SQLDatabase.class));
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
            SQLObjectUtils.addSimulatedTable(db, "cat", "schem", "tab");
            fail("Should not have been allowed because table exists");
        } catch (SQLObjectException ex) {
            // expected
        }
    }
}
