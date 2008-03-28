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
package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

public class TestDBTree extends TestCase {

	DBTree dbTree;
	SPDataSource ds;
	SPDataSource db2ds;
	protected void setUp() throws Exception {
		ds = new SPDataSource(new PlDotIni());
		db2ds = new SPDataSource(new PlDotIni());
		SQLDatabase ppdb = new SQLDatabase(ds);
		
		List dbList = new ArrayList();
		dbList.add(0,ppdb);
		dbList.add(1,new SQLDatabase(db2ds));
        
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		dbTree = new DBTree(session, dbList);
	}
	
	public void testdbcsAlreadyExists() throws ArchitectException {
		SPDataSource ds2 = new SPDataSource(new PlDotIni());
		assertTrue("ds2 must .equals ds for this test to work", ds.equals(ds2));
		assertFalse("dbcsAlreadyExists Should not find ds2", dbTree.dbcsAlreadyExists(ds2));
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
}
