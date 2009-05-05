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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.event.TaskTerminationEvent;
import ca.sqlpower.swingui.event.TaskTerminationListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RefreshAction extends AbstractArchitectAction {
    
    /**
     * This will refresh all of the databases on a different thread and
     * allows the refresh to be monitorable.
     */
    private class RefreshMonitorableWorker extends SPSwingWorker {
        
        private final Set<SQLDatabase> databasesToRefresh = new HashSet<SQLDatabase>();
        private final Component parent;
        private SQLDatabase dbBeingRefreshed;
        
        public RefreshMonitorableWorker(SwingWorkerRegistry registry, Component parent, Set<SQLDatabase> dbs) {
            super(registry);
            this.parent = parent;
            this.databasesToRefresh.addAll(dbs);
            setJobSize(null);
        }
        
        @Override
        protected String getMessageImpl() {
            if (dbBeingRefreshed == null) {
                return "Refreshing selected databases.";
            }
            return "Refreshing database " + dbBeingRefreshed.getName();
        }
    
        @Override
        public void doStuff() throws Exception {
            setProgress(0);
            try {
                for (SQLDatabase db : databasesToRefresh) {
                    dbBeingRefreshed = db;
                    db.refresh();
                    increaseProgress();
                }
            } catch (SQLObjectException ex) {
                setDoStuffException(ex);
            } finally {
                dbBeingRefreshed = null;
            }
        }
    
        @Override
        public void cleanup() throws Exception {
            if (getDoStuffException() != null) {
                ASUtils.showExceptionDialogNoReport(parent, "Refresh failed", getDoStuffException());
            }
        }
    }

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

        final SPSwingWorker worker = new RefreshMonitorableWorker(session, session.getArchitectFrame(), databasesToRefresh);
        final Thread thread = new Thread(worker, "Refresh database worker");
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel messageLabel = new JLabel("Refreshing selected databases.");
        ProgressWatcher.watchProgress(progressBar, worker, messageLabel);
        
        final JDialog dialog = new JDialog(session.getArchitectFrame(), "Refresh");
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref"));
        builder.setDefaultDialogBorder();
        builder.append(messageLabel, 3);
        builder.nextLine();
        builder.append(progressBar, 3);
        builder.nextLine();
        builder.append(new JLabel(""), new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                worker.kill();
            }
        }));
        dialog.add(builder.getPanel());
        
        worker.addTaskTerminationListener(new TaskTerminationListener() {
            public void taskFinished(TaskTerminationEvent e) {
                dialog.dispose();
            }
        });
        
        dialog.pack();
        dialog.setModal(true);
        dialog.setLocationRelativeTo(session.getArchitectFrame());
        thread.start();
        dialog.setVisible(true);
        
    }

}
