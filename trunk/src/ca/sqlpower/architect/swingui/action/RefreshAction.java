/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;

public class RefreshAction extends AbstractArchitectAction {

    public RefreshAction(ArchitectSwingSession session) {
        super(session,
              "Refresh",
              "Refreshes the tree to match current structure in the selected database",
              new ImageIcon(RefreshAction.class.getResource("/icons/database_refresh.png")));
    }
    
    public void actionPerformed(ActionEvent e) {
        DBTree dbTree = session.getSourceDatabases();
        
        Set<SQLDatabase> databasesToRefresh = new HashSet<SQLDatabase>();
        for (TreePath tp : dbTree.getSelectionPaths()) {
            SQLObject so = (SQLObject) tp.getLastPathComponent();
            SQLDatabase db = SQLObjectUtils.getAncestor(so, SQLDatabase.class);
            if (db != null && !db.isPlayPenDatabase()) {
                databasesToRefresh.add(db);
            }
        }
        
        if (databasesToRefresh.isEmpty()) {
            JOptionPane.showMessageDialog(dbTree, "Please select a source database to refresh");
            return;
        }
        
        try {
            for (SQLDatabase db : databasesToRefresh) {
                db.refresh();
            }
        } catch (SQLObjectException ex) {
            ASUtils.showExceptionDialogNoReport(dbTree, "Refresh failed", ex);
        }
    }

}
