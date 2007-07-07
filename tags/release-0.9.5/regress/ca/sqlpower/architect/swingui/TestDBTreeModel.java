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

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class TestDBTreeModel extends TestCase {

    private static class LoggingSwingTreeModelListener implements TreeModelListener {

        private int changeCount;
        private int insertCount;
        private int removeCount;
        private int structureChangeCount;
        private List<TreeModelEvent> eventLog = new ArrayList<TreeModelEvent>();
        
        public void treeNodesChanged(TreeModelEvent e) {
            changeCount++;
            eventLog.add(e);
        }

        public void treeNodesInserted(TreeModelEvent e) {
            insertCount++;
            eventLog.add(e);
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            removeCount++;
            eventLog.add(e);
        }

        public void treeStructureChanged(TreeModelEvent e) {
            structureChangeCount++;
            eventLog.add(e);
        }
        
        public int getChangeCount() {
            return changeCount;
        }
        
        public int getInsertCount() {
            return insertCount;
        }
        
        public int getRemoveCount() {
            return removeCount;
        }
        
        public int getStructureChangeCount() {
            return structureChangeCount;
        }
        
        public List<TreeModelEvent> getEventLog() {
            return eventLog;
        }
        
        @Override
        public String toString() {
            return String.format("[insert: %d, remove: %d, change: %d, structure: %d, eventLog: %s]",
                    insertCount, removeCount, changeCount, structureChangeCount, eventLog); 
        }
    }


    private DBTreeModel tm;

    protected void setUp() throws Exception {
        tm = new DBTreeModel(new ArchitectSessionImpl());
        tm.setTestMode(true);
	}
	
    public void testRefireRelationshipMappingEvents() throws ArchitectException {
        SQLObject treeRoot = (SQLObject) tm.getRoot();
        SQLDatabase db = new SQLDatabase();
        db.setName("test database");

        SQLTable t = new SQLTable(db, true);
        t.setName("Table");
        
        SQLColumn c = new SQLColumn(null, "column", Types.ARRAY, 1, 1);

        treeRoot.addChild(db);
        db.addChild(t);
        t.addColumn(c);
        c.setPrimaryKeySeq(0);
        
        SQLRelationship r = new SQLRelationship();
        r.setName("my relationship is cooler than your silly columnmnm");
        r.attachRelationship(t, t, true);
        
        LoggingSwingTreeModelListener l = new LoggingSwingTreeModelListener();
        tm.addTreeModelListener(l);

        r.removeChild(0);
        
        System.out.println(l);
        
        assertEquals(2, l.getRemoveCount());
        
        TreePath expectPkPath = new TreePath(new Object[] { treeRoot, db, t, t.getExportedKeysFolder(), r });
        TreePath expectFkPath = new TreePath(new Object[] { treeRoot, db, t, t.getImportedKeysFolder(), r });
        
        Set<TreePath> actualPaths = new HashSet<TreePath>();
        for (TreeModelEvent tme : l.getEventLog()) {
            actualPaths.add(tme.getTreePath());
        }

        assertTrue(actualPaths.contains(expectFkPath));
        assertTrue(actualPaths.contains(expectPkPath));
    }
}
