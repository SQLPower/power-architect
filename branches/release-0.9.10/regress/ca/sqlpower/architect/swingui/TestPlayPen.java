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

import java.awt.Point;
import java.sql.Types;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class TestPlayPen extends TestCase {
	ArchitectFrame af;
	private PlayPen pp;
	private SQLDatabase ppdb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession(false);
		af = session.getArchitectFrame();
		pp = session.getPlayPen();
		ppdb = session.getTargetDatabase();

	}

	public void testUndoAddTable() throws ArchitectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);

		TablePane tp = new TablePane(t, pp);
		ppdb.addChild(t);

		pp.addTablePane(tp, new Point(99,98));



		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(pp.findTablePane(t));

		//Undo the add child and the move table pane
		af.getUndoManager().undo();

		assertNull(pp.findTablePane(t));
	}

	public void testRedoAddTable() throws ArchitectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);

		TablePane tp = new TablePane(t, pp);

		ppdb.addChild(t);
		pp.addTablePane(tp, new Point(99,98));

		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(ppdb.getTableByName("test_me"));
		assertNotNull(pp.findTablePane(t));
		//undo the add child and the move table pane
		System.out.println("Undo action is "+af.getUndoManager().getUndoPresentationName());
		af.getUndoManager().undo();
		System.out.println("After undo, undo action is "+af.getUndoManager().getUndoPresentationName());
		assertNull(ppdb.getTableByName("test_me"));
		assertNull(pp.findTablePane(t));
		// redo the add table and the move
		af.getUndoManager().redo();
		tp = pp.findTablePane(t);
		assertNotNull("Table pane didn't come back!", tp);
		assertEquals("Table came back, but in wrong location",
				new Point(99,98), tp.getLocation());
	}

	public void testImportTableCopyHijacksProperly() throws ArchitectException {

		SQLDatabase sourceDB = new SQLDatabase();

		SQLTable sourceParentTable = new SQLTable(sourceDB, true);
		sourceParentTable.setName("parent");
		sourceParentTable.addColumn(new SQLColumn(sourceParentTable, "key", Types.BOOLEAN, 1, 0));
		sourceParentTable.getColumn(0).setPrimaryKeySeq(0);
		sourceDB.addChild(sourceParentTable);

		SQLTable sourceChildTable = new SQLTable(sourceDB, true);
		sourceChildTable.setName("child");
		sourceChildTable.addColumn(new SQLColumn(sourceChildTable, "key", Types.BOOLEAN, 1, 0));
		sourceDB.addChild(sourceChildTable);

		SQLRelationship sourceRel = new SQLRelationship();
		sourceRel.attachRelationship(sourceParentTable, sourceChildTable, true);

		pp.importTableCopy(sourceChildTable, new Point(10, 10));
		pp.importTableCopy(sourceParentTable, new Point(10, 10));
		pp.importTableCopy(sourceParentTable, new Point(10, 10));

		int relCount = 0;
		int tabCount = 0;
		int otherCount = 0;
		for (int i = 0; i < pp.getPlayPenContentPane().getComponentCount(); i++) {
			PlayPenComponent ppc = pp.getPlayPenContentPane().getComponent(i);
			if (ppc instanceof Relationship) {
				relCount++;
			} else if (ppc instanceof TablePane) {
				tabCount++;
			} else {
				otherCount++;
			}
		}
		assertEquals("Expected three tables in pp", 3, tabCount);
		assertEquals("Expected two relationships in pp", 2, relCount);
		assertEquals("Found junk in playpen", 0, otherCount);

		TablePane importedChild = pp.findTablePaneByName("child");
		assertEquals("Incorrect reference count on imported child col",
				3, importedChild.getModel().getColumn(0).getReferenceCount());
	}
	
	/**
	 * Test to ensure that the self-referencing table gets imported properly into the PlayPen.
	 * @throws Exception
	 */
	public void testImportTableCopyOnSelfReferencingTable() throws Exception {
	    SQLDatabase sourceDB = new SQLDatabase();

        SQLTable table = new SQLTable(sourceDB, true);
        table.setName("self_ref");
        SQLColumn pkCol = new SQLColumn(table, "key", Types.INTEGER, 10, 0);
        table.addColumn(pkCol);
        table.getColumn(0).setPrimaryKeySeq(0);
        SQLColumn fkCol = new SQLColumn(table, "self_ref_column", Types.INTEGER, 10, 0);
        table.addColumn(fkCol);
        
        SQLRelationship rel = new SQLRelationship();
        rel.attachRelationship(table, table, false);
        rel.addMapping(pkCol, fkCol);
        sourceDB.addChild(table);
        
        pp.importTableCopy(table, new Point(10, 10));
        
        int relCount = 0;
        int tabCount = 0;
        int otherCount = 0;
        for (int i = 0; i < pp.getPlayPenContentPane().getComponentCount(); i++) {
            PlayPenComponent ppc = pp.getPlayPenContentPane().getComponent(i);
            if (ppc instanceof Relationship) {
                relCount++;
            } else if (ppc instanceof TablePane) {
                tabCount++;
            } else {
                otherCount++;
            }
        }
        assertEquals("Expected one table in pp", 1, tabCount);
        assertEquals("Expected one relationship in pp", 1, relCount);
        assertEquals("Found junk in playpen", 0, otherCount);
	}
	
	/**
	 * Test to ensure that when importing two copies of a self-referencing table, the
	 * correct number of relationships get added, and furthermore, the relationships
	 * all point to the correct table.
	 * @throws Exception
	 */
	public void testImportTableCopyOnTwoCopiesOfSelfReferencingTable() throws Exception {
        SQLDatabase sourceDB = new SQLDatabase();

        SQLTable table = new SQLTable(sourceDB, true);
        table.setName("self_ref");
        SQLColumn pkCol = new SQLColumn(table, "key", Types.INTEGER, 10, 0);
        table.addColumn(pkCol);
        table.getColumn(0).setPrimaryKeySeq(0);
        SQLColumn fkCol = new SQLColumn(table, "self_ref_column", Types.INTEGER, 10, 0);
        table.addColumn(fkCol);
        
        SQLRelationship rel = new SQLRelationship();
        rel.attachRelationship(table, table, false);
        rel.addMapping(pkCol, fkCol);
        sourceDB.addChild(table);
        
        pp.importTableCopy(table, new Point(10, 10));
        pp.importTableCopy(table, new Point(30, 30));
        
        int relCount = 0;
        int tabCount = 0;
        int otherCount = 0;
        for (int i = 0; i < pp.getPlayPenContentPane().getComponentCount(); i++) {
            PlayPenComponent ppc = pp.getPlayPenContentPane().getComponent(i);
            if (ppc instanceof Relationship) {
                relCount++;
            } else if (ppc instanceof TablePane) {
                tabCount++;
            } else {
                otherCount++;
            }
        }
        assertEquals("Expected two tables in pp", 2, tabCount);
        assertEquals("Expected two relationships in pp", 2, relCount);
        assertEquals("Found junk in playpen", 0, otherCount);
        
        for (SQLTable t: pp.getTables()) {
            List<SQLRelationship> exportedKeys = t.getExportedKeys();
            List<SQLRelationship> importedKeys = t.getImportedKeys();
            
            assertEquals("Expected only one exported key in table", 1, exportedKeys.size());
            assertEquals("Expected only one imported key in table", 1, importedKeys.size());
            
            SQLRelationship exportedKey = exportedKeys.get(0);
            SQLRelationship importedKey = importedKeys.get(0);
            
            assertEquals("Expected exported key PK and FK tables to be the same", exportedKey.getFkTable(), 
                    exportedKey.getPkTable());
            assertEquals("Expected imported key PK and FK tables to be the same", importedKey.getFkTable(), 
                    importedKey.getPkTable());
            assertEquals("Expected exported key and imported key tables to be the same", exportedKey.getPkTable(),
                    importedKey.getPkTable());
        }
    }
}
