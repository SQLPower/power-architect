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
package ca.sqlpower.architect.swingui;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import junit.framework.TestCase;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;

public class TestDBTree extends TestCase {

	DBTree dbTree;
	JDBCDataSource ds;
	JDBCDataSource db2ds;
	protected void setUp() throws Exception {
		ds = new JDBCDataSource(new PlDotIni());
		db2ds = new JDBCDataSource(new PlDotIni());

        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
        session.getTargetDatabase().setDataSource(ds);
        session.getRootObject().addChild(new SQLDatabase(db2ds), 1);
		dbTree = new DBTree(session);
	}
	
	public void testdbcsAlreadyExists() throws SQLObjectException {
	    JDBCDataSource ds2 = new JDBCDataSource(new PlDotIni());
		assertTrue("ds2ds must .equals ds for this test to work", db2ds.equals(ds2));
		assertTrue("dbcsAlreadyExists Should not find ds2", dbTree.dbcsAlreadyExists(ds2));
		assertTrue("db2ds should be in the list",dbTree.dbcsAlreadyExists(db2ds));
	}
	
	//The next set of test check that the Compare To Current menu item only appears when
	//it is needed
	
	public void testCompareDMOnlyTables() throws Exception {
        //this only looks for menu options
        SQLDatabase db = new SQLDatabase();
        db.addChild(new SQLTable());
        TreePath p = new TreePath(db);
        JPopupMenu pop = dbTree.refreshMenu(p);
        
        for (int x = 0; x< pop.getComponentCount(); x++) {
            if (pop.getComponent(x) instanceof JMenuItem) {
                JMenuItem m = (JMenuItem)pop.getComponent(x);
                if (m.getText().equals("Compare to Current")) {
                    return;
                }   
            }
        }   
        fail("The compare to current menu was not included");
	}
	
	public void testCompareDMSchema() throws Exception {
        //this only looks for menu options    
        
        SQLSchema schema = new SQLSchema(false);
        schema.addChild(new SQLTable());
        TreePath p = new TreePath(schema);
        JPopupMenu pop = dbTree.refreshMenu(p);
        
        for (int x = 0; x< pop.getComponentCount(); x++) {
            if (pop.getComponent(x) instanceof JMenuItem) {
                JMenuItem m = (JMenuItem)pop.getComponent(x);
                if (m.getText().equals("Compare to Current")) {
                    return;
                }   
            }
        }   
        fail("The compare to current menu was not included");
    }
	
	   public void testCompareDMCatalog() throws Exception {
	        //this only looks for menu options
       
	        
	        SQLCatalog catalog = new SQLCatalog();
	        catalog.addChild(new SQLTable());
	        TreePath p = new TreePath(catalog);
	        JPopupMenu pop = dbTree.refreshMenu(p);
	        
	        for (int x = 0; x< pop.getComponentCount(); x++) {
	            if (pop.getComponent(x) instanceof JMenuItem) {
	                JMenuItem m = (JMenuItem)pop.getComponent(x);
	                if (m.getText().equals("Compare to Current")) {
	                    return;
	                }   
	            }
	        }   
	        fail("The compare to current menu was not included");
	    }
	   
	   public void testCompareDMScheamCatalog() throws Exception {
           //this only looks for menu options           
           
           SQLCatalog catalog = new SQLCatalog();
           SQLSchema schema = new SQLSchema(catalog,"s1",false);
           catalog.addChild(schema);
           schema.addChild(new SQLTable());
           
           TreePath p = new TreePath(catalog);
           JPopupMenu pop = dbTree.refreshMenu(p);
           
           for (int x = 0; x< pop.getComponentCount(); x++) {
               if (pop.getComponent(x) instanceof JMenuItem) {
                   JMenuItem m = (JMenuItem)pop.getComponent(x);
                   if (m.getText().equals("Compare to Current")) {
                       fail("The compare to current menu found in the menu");
                   }   
               }
           }   
       }
	   
	    public void testCompareDMOnlyDatabaseSchemas() throws Exception {
	        //this only looks for menu options
	        SQLDatabase db = new SQLDatabase();
	        SQLSchema s = new SQLSchema(false);
	        db.addChild(s);
	        s.addChild(new SQLTable());
	        
	        TreePath p = new TreePath(db);
	        JPopupMenu pop = dbTree.refreshMenu(p);
	        
	        for (int x = 0; x< pop.getComponentCount(); x++) {
	            if (pop.getComponent(x) instanceof JMenuItem) {
	                JMenuItem m = (JMenuItem)pop.getComponent(x);
	                if (m.getText().equals("Compare to Current")) {
	                    fail("The compare to current menu found in the menu");
	                }   
	            }
	        }
	    }
	    
	       public void testCompareDMOnlyDatabaseCatalogs() throws Exception {
	            //this only looks for menu options
	            SQLDatabase db = new SQLDatabase();
	            SQLCatalog cat = new SQLCatalog();
	            db.addChild(cat);
	            cat.addChild(new SQLTable());
	            
	            TreePath p = new TreePath(db);
	            JPopupMenu pop = dbTree.refreshMenu(p);
	            
	            for (int x = 0; x< pop.getComponentCount(); x++) {
	                if (pop.getComponent(x) instanceof JMenuItem) {
	                    JMenuItem m = (JMenuItem)pop.getComponent(x);
	                    if (m.getText().equals("Compare to Current")) {
	                        fail("The compare to current menu found in the menu");
	                    }   
	                }
	            }
	        }
	       
	/**
	 * Regression test to confirm a selected table will be added to
	 * the copy selection list.
	 */
    public void testTableAddedToCopy() throws Exception {
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File("pl.regression.ini"));
        JDBCDataSource dbcs = plIni.getDataSource("regression_test", JDBCDataSource.class);
        Connection con = dbcs.createConnection();
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE test1 (col1 varchar(50), col2 varchar(50))");
        dbTree.addSourceConnection(dbcs);

        //The SQLDatabase representing the SPDataSource
        SQLDatabase db = (SQLDatabase) dbTree.getModel().getChild(dbTree.getModel().getRoot(), 2);
        db.populate();
        SQLObject schema = db.getChildByName("PUBLIC", SQLSchema.class);
        SQLObject table = schema.getChildByName("TEST1", SQLTable.class);
        assertNotNull(table);
        TreePath path = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table});
        dbTree.setSelectionPath(path);
        
        Set<SQLObject> objectsToCopy = dbTree.findSQLObjectsToCopy();
        assertEquals(1, objectsToCopy.size());
        assertTrue(objectsToCopy.contains(table));
        
    }
    
    /**
     * Regression test to confirm 2 selected columns will be added to
     * the copy selection list even if the table is selected.
     */
    public void testColumnsAddedToCopy() throws Exception {
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File("pl.regression.ini"));
        JDBCDataSource dbcs = plIni.getDataSource("regression_test", JDBCDataSource.class);
        Connection con = dbcs.createConnection();
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE test2 (col1 varchar(50), col2 varchar(50))");
        dbTree.addSourceConnection(dbcs);

        //The SQLDatabase representing the SPDataSource
        SQLDatabase db = (SQLDatabase) dbTree.getModel().getChild(dbTree.getModel().getRoot(), 2);
        db.populate();
        SQLObject schema = db.getChildByNameIgnoreCase("PUBLIC", SQLSchema.class);
        SQLTable table = schema.getChildByNameIgnoreCase("TEST2", SQLTable.class);
        SQLObject col1 = table.getColumnByName("COL1");
        SQLObject col2 = table.getColumnByName("COL2");
        assertNotNull(col1);
        assertNotNull(col2);
        TreePath tablePath = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table});
        TreePath path = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table, col1});
        TreePath path2 = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table, col2});
        dbTree.setSelectionPaths(new TreePath[]{path, path2, tablePath});
        
        Set<SQLObject> objectsToCopy = dbTree.findSQLObjectsToCopy();
        assertEquals(2, objectsToCopy.size());
        assertTrue(objectsToCopy.contains(col1));
        assertTrue(objectsToCopy.contains(col2));
        
    }
    
    /**
     * This test selects two columns out of one table and another 
     * table that is not the column's parent table. The end result
     * expected is to have both tables but not the columns to be
     * added to the copy list.
     */
    public void testCopyTablesWithColumnsSelected() throws Exception {
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File("pl.regression.ini"));
        JDBCDataSource dbcs = plIni.getDataSource("regression_test", JDBCDataSource.class);
        Connection con = dbcs.createConnection();
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE test3 (col1 varchar(50), col2 varchar(50))");
        stmt.execute("CREATE TABLE test4 (col1 varchar(50), col2 varchar(50))");
        dbTree.addSourceConnection(dbcs);

        //The SQLDatabase representing the SPDataSource
        SQLDatabase db = (SQLDatabase) dbTree.getModel().getChild(dbTree.getModel().getRoot(), 2);
        db.populate();
        SQLSchema schema = db.getChildByNameIgnoreCase("PUBLIC", SQLSchema.class);
        SQLTable table = schema.getChildByNameIgnoreCase("TEST3", SQLTable.class);
        SQLObject col1 = table.getColumnByName("COL1");
        SQLObject col2 = table.getColumnByName("COL2");
        SQLTable table2 = schema.getChildByNameIgnoreCase("test4", SQLTable.class);
        assertNotNull(col1);
        assertNotNull(col2);
        TreePath tablePath = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table2});
        TreePath path = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table, col1});
        TreePath path2 = new TreePath(new Object[]{dbTree.getModel().getRoot(), db, schema, table, col2});
        dbTree.setSelectionPaths(new TreePath[]{path, path2, tablePath});
        
        Set<SQLObject> objectsToCopy = dbTree.findSQLObjectsToCopy();
        assertEquals(2, objectsToCopy.size());
        assertTrue(objectsToCopy.contains(table));
        assertTrue(objectsToCopy.contains(table2));
        
    }
    
    /**
     * This test selects two tables in different databases and confirms that the tables
     * are the objects being transfered, not the databases themselves.
     */
    public void testFindSQLObjectsToCopySelectionAcrossDBs() throws Exception {
        SQLDatabase db1 = new SQLDatabase();
        SQLDatabase db2 = new SQLDatabase();
        ((SQLObjectRoot) dbTree.getModel().getRoot()).addChild(db1);
        ((SQLObjectRoot) dbTree.getModel().getRoot()).addChild(db2);
        
        SQLTable table1 = new SQLTable(db1, true);
        SQLTable table2 = new SQLTable(db2, true);
        
        TreePath path1 = new TreePath(new Object[]{dbTree.getModel().getRoot(), db1, table1});
        TreePath path2 = new TreePath(new Object[]{dbTree.getModel().getRoot(), db2, table2});
        
        dbTree.setSelectionPaths(new TreePath[]{path1, path2});
        
        Set<SQLObject> objectsToCopy = dbTree.findSQLObjectsToCopy();
        assertEquals(2, objectsToCopy.size());
        assertTrue(objectsToCopy.contains(table1));
        assertTrue(objectsToCopy.contains(table2));
        
    }
}
