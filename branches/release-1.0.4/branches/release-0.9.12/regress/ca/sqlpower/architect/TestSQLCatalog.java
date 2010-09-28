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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Iterator;

import ca.sqlpower.sql.SPDataSource;

public class TestSQLCatalog extends SQLTestCase {

	private SQLCatalog c;
	public TestSQLCatalog(String name) throws Exception {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		c = new SQLCatalog();
        c.setPopulated(true);
        c.setParent(new SQLDatabase());
	}

	@Override
	protected SQLObject getSQLObjectUnderTest() {
		return c;
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getName()'
	 */
	public void testGetName() {
		
		assertNull(c.getName());
		c.setName("xxx");
		assertEquals(c.getName(),"xxx");
		
		c.setName("yyy");
		assertEquals(c.getName(),"yyy");
		assertEquals(c.getName(),"yyy");
		assertEquals(c.getShortDisplayName(),"yyy");
		assertEquals(c.toString(),"yyy");
		
		SQLDatabase mydb = new SQLDatabase(getDb().getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertEquals(c.getName(),"aaa");
		assertEquals(c.getName(),"aaa");
		assertEquals(c.getShortDisplayName(),"aaa");
		assertEquals(c.toString(),"aaa");
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.getParent()'
	 */
	public void testGetParent() {
		SQLDatabase mydb = new SQLDatabase(getDb().getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertEquals(c.getParent(),mydb);
		assertEquals(c.getParentDatabase(),mydb);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.allowsChildren()'
	 */
	public void testAllowsChildren() {

		assertTrue(c.allowsChildren());
		
		SQLDatabase mydb = new SQLDatabase(getDb().getDataSource());
		c = new SQLCatalog(mydb,"aaa");
		assertTrue(c.allowsChildren());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLCatalog.isSchemaContainer()'
	 */
	public void testIsSchemaContainer() throws Exception {

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
		assertEquals(c.getNativeTerm(),"catalog");
		c.setNativeTerm(null);
		assertNull(c.getNativeTerm());
		c.setNativeTerm("AAA");
		assertEquals(c.getNativeTerm(),"aaa");
		
		SQLCatalog c2 = new SQLCatalog(new SQLDatabase(getDb().getDataSource()),"x");
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
	
		assertNull(c.getPhysicalName());
		c.setPhysicalName("aaa");
		assertEquals(c.getPhysicalName(),"aaa");
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.isPopulated()'
	 */
	public void testIsPopulated() throws Exception {
		c.setPopulated(true);
		assertTrue(c.isPopulated());
		c.setPopulated(false);
		assertFalse(c.isPopulated());
		
		c.setPopulated(true);
		c.populate();
		c.setPopulated(false);
		try {
			c.populate();
		} catch ( NullPointerException e ) {
			// it's ok
		}
	}
	
	public void testIsPopulateWithCatalogs() throws Exception {		
		SPDataSource dataSource = getDb().getDataSource();
		Connection conn = getDb().getConnection();
		DatabaseMetaData meta = conn.getMetaData();
		conn.close();
		conn = null;
		String ct = meta.getCatalogTerm();
		if (null == ct || ct.length() == 0) { // unless this platform has catalogs.
			return;
		}
		SQLCatalog c2 = new SQLCatalog(new SQLDatabase(dataSource),"x");		
		c2.setPopulated(false);			
		c2.populate();			
		assertTrue(c2.isPopulated());			
		c2.setPopulated(true);
		c2.populate();
		assertTrue(c2.isPopulated());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.getChildren()'
	 */
	public void testGetChildren() throws Exception {
		SQLCatalog c2 = new SQLCatalog();
		c.setPopulated(true);
		c2.setPopulated(true);
		assertNotNull(c.getChildren());
		assertEquals(c.getChildren().size(),0);
		assertEquals(c.getChildCount(),0);
		
		
		String name[] = { "name1", "name2", "name3", "name4","name5", "name6" };

		int i;
		for ( i=0; i<6; i++ ) {
			SQLTable t = new SQLTable(c, name[i], "", "TABLE", true);
			SQLSchema s = new SQLSchema(c, name[i], true);
			c.addChild(t);
			c2.addChild(i,s);
		}
		
		assertNotNull(c.getChildren());
		assertEquals(c.getChildren().size(),6);
		assertEquals(c.getChildCount(),6);
		
		assertNotNull(c2.getChildren());
		assertEquals(c2.getChildren().size(),6);
		assertEquals(c2.getChildCount(),6);
		
		Iterator it = c.getChildren().iterator();
		Iterator it2 = c2.getChildren().iterator();
		
		i = 0;
		while ( it.hasNext() && it2.hasNext() ) {
			SQLObject o = (SQLObject) it.next();
			SQLObject o2 = (SQLObject) it2.next();
			assertEquals(o.getName(),name[i]);
			assertEquals(c.getChild(i).getName(),name[i]);
			assertEquals(c2.getChild(i).getName(),name[i]);
			
			assertTrue(o instanceof SQLTable);
			assertTrue(o2 instanceof SQLSchema);
			i++;
		}
		
		c.addChild(2,new SQLTable());
		assertEquals(c.getChildren().size(),7);
		assertEquals(c.getChildCount(),7);
		assertTrue(c.getChild(2) instanceof SQLTable);
		
		c.addChild(new SQLTable());
		assertEquals(c.getChildren().size(),8);
		assertEquals(c.getChildCount(),8);
		assertTrue(c.getChild(c.getChildren().size()-1) instanceof SQLTable);
		
		
		c2.addChild(2,new SQLSchema(true));
		assertEquals(c2.getChildren().size(),7);
		assertEquals(c2.getChildCount(),7);
		assertTrue(c2.getChild(2) instanceof SQLSchema);
		
		c2.addChild(new SQLSchema(true));
		assertEquals(c2.getChildren().size(),8);
		assertEquals(c2.getChildCount(),8);
		assertTrue(c2.getChild(c.getChildren().size()-1) instanceof SQLSchema);
		
		try {
			c.addChild(new SQLSchema(true));
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
		
		try {
			c2.addChild(new SQLTable());
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addChild(int, SQLObject)'
	 */
	public void testAddChild() throws Exception {
	
		SQLCatalog c2 = new SQLCatalog();
		c.setPopulated(true);
		c2.setPopulated(true);
		assertNotNull(c.getChildren());
		assertEquals(c.getChildren().size(),0);
		assertEquals(c.getChildCount(),0);
		
		
		String name[] = { "name1", "name2", "name3", "name4","name5", "name6" };

		int i;
		for ( i=0; i<6; i++ ) {
			SQLTable t = new SQLTable(c, name[i], "", "TABLE", true);
			SQLSchema s = new SQLSchema(c, name[i], true);
			c.addChild(i,t);
			c2.addChild(i,s);
			
		}
		assertNotNull(c.getChildren());
		assertEquals(c.getChildren().size(),6);
		assertEquals(c.getChildCount(),6);
		
		assertNotNull(c2.getChildren());
		assertEquals(c2.getChildren().size(),6);
		assertEquals(c2.getChildCount(),6);
		
		Iterator it = c.getChildren().iterator();
		Iterator it2 = c2.getChildren().iterator();
		
		i = 0;
		while ( it.hasNext() && it2.hasNext() ) {
			SQLObject o = (SQLObject) it.next();
			SQLObject o2 = (SQLObject) it2.next();
			assertEquals(o.getName(),name[i]);
			assertEquals(c.getChild(i).getName(),name[i]);
			assertEquals(c2.getChild(i).getName(),name[i]);
			
			assertTrue(o instanceof SQLTable);
			assertTrue(o2 instanceof SQLSchema);
			i++;
		}
		
		c.addChild(2,new SQLTable());
		assertEquals(c.getChildren().size(),7);
		assertEquals(c.getChildCount(),7);
		assertTrue(c.getChild(2) instanceof SQLTable);
		
		c.addChild(new SQLTable());
		assertEquals(c.getChildren().size(),8);
		assertEquals(c.getChildCount(),8);
		assertTrue(c.getChild(c.getChildren().size()-1) instanceof SQLTable);
		
		
		c2.addChild(2,new SQLSchema(true));
		assertEquals(c2.getChildren().size(),7);
		assertEquals(c2.getChildCount(),7);
		assertTrue(c2.getChild(2) instanceof SQLSchema);
		
		c2.addChild(new SQLSchema(true));
		assertEquals(c2.getChildren().size(),8);
		assertEquals(c2.getChildCount(),8);
		assertTrue(c2.getChild(c.getChildren().size()-1) instanceof SQLSchema);
		
		try {
			c.addChild(new SQLSchema(true));
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
		
		try {
			c2.addChild(new SQLTable());
		} catch ( ArchitectException e) {
			System.out.println("Caught expected exception.");
		}
	}


	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeChild(int)'
	 */
	public void testRemoveChild() throws Exception {
		
		c.setPopulated(true);
		assertNotNull(c.getChildren());
		assertEquals(c.getChildren().size(),0);
		assertEquals(c.getChildCount(),0);

		try {
			c.removeChild(-1);
			c.removeChild(0);
			c.removeChild(1);
			fail();
		} catch ( IndexOutOfBoundsException e ) {
			// that's what we want
		}
		
		SQLTable t1 = new SQLTable(c,"","","TABLE",true);
		SQLTable t2 = new SQLTable(c,"","","TABLE",true);
		SQLTable t3 = new SQLTable(c,"","","TABLE",true);
		SQLTable t4 = new SQLTable(c,"","","TABLE",true);
		
		c.addChild(t1);
		c.addChild(t2);
		c.addChild(t3);
		c.addChild(t4);
		assertEquals(c.getChildCount(),4);
		SQLTable tx = (SQLTable) c.removeChild(1);
		assertEquals(tx,t2);
		assertEquals(c.getChildCount(),3);
		
		assertTrue(c.removeChild(t4));
		assertEquals(c.getChildCount(),2);

		assertFalse(c.removeChild(t4));
		assertEquals(c.getChildCount(),2);
		
	}

	public void testFireDbChildrenInserted() throws ArchitectException {
		TestingSQLObjectListener test1 = new TestingSQLObjectListener();
		c.addSQLObjectListener(test1);
		c.addChild(new SQLTable(c,"","","TABLE",true));
		assertEquals("Children inserted not fired!", 1, test1.getInsertedCount());
	}
	
	public void testFireDbChildrenRemoved() throws ArchitectException {
	    SQLTable tempTable = new SQLTable(c,"","","TABLE",true);
	    c.addChild(tempTable);

	    TestingSQLObjectListener test1 = new TestingSQLObjectListener();
        c.addSQLObjectListener(test1);
        
        c.removeChild(tempTable);
        assertEquals("Children removed not fired!", 1, test1.getRemovedCount());
    }
}
