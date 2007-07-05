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

import java.awt.Point;
import java.sql.Types;

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
		ppdb = pp.getDatabase();

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
}
