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
import java.awt.Window;
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
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.event.TaskTerminationEvent;
import ca.sqlpower.swingui.event.TaskTerminationListener;
import ca.sqlpower.util.SQLPowerUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RefreshAction extends AbstractArchitectAction {
    
    private DBTree dbTree;
    
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
            // XXX this is not ideal because it collapses all tree nodes after the refresh is done.
            // However, it is necessary to notify the DBTree (JTree) of the changes, because all the
            // events that happened during the refresh were not on the Event Dispatch Thread,
            // and DBTreeModel correctly filtered them out. At this point, it's too late to feed the
            // piecewise updates to the JTree because they describe interim state that has already
            // come and gone.
            //
            // I believe the best fix for this is to factor out the refresh logic into a separate
            // class (SQLDatabaseRefresher?) which can be instantiated for any database. It would
            // walk the tree and do the refresh work, but know enough to perform the actual tree
            // manipulations on the EDT.
            //
            // Another possibility to look into would be to improve the SQLObject event mechanism
            // with enough locking that it would be possible to use SwingUtilities.invokeAndWait()
            // to refire the events in the DBTreeModel. The behaviour would be that other threads
            // trying to mutate anything in the tree which is currently busy firing an event would
            // block until the event has been delivered to all its listeners. This could probably
            // deadlock too easily though, since the EDT might already have something in the queue
            // that would block until the invokeAndWait item has run.
            ((DBTreeModel) getSession().getDBTree().getModel()).refreshTreeStructure();
            
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
    
    /** 
     * Refreshes based on selections in a specific tree.
     */
    public RefreshAction(ArchitectSwingSession session, DBTree tree) {
        super(session,
              "Refresh",
              "Refreshes the tree to match current structure in the selected database",
              new ImageIcon(RefreshAction.class.getResource("/icons/database_refresh.png")));
        dbTree = tree;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (dbTree == null) {
            dbTree = getSession().getDBTree();
        }
        
        Set<SQLDatabase> databasesToRefresh = new HashSet<SQLDatabase>();
        for (TreePath tp : dbTree.getSelectionPaths()) {
            SQLObject so = (SQLObject) tp.getLastPathComponent();
            SQLDatabase db = SQLPowerUtils.getAncestor(so, SQLDatabase.class);
            if (db != null && !db.isPlayPenDatabase()) {
                databasesToRefresh.add(db);
            }
        }
        
        if (databasesToRefresh.isEmpty()) {
            JOptionPane.showMessageDialog(dbTree, "Please select a source database to refresh");
            return;
        }

        final SPSwingWorker worker = new RefreshMonitorableWorker(getSession(), (Window)SwingUtilities.getRoot(dbTree), databasesToRefresh);
        final Thread thread = new Thread(worker, "Refresh database worker");
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel messageLabel = new JLabel("Refreshing selected databases.");
        ProgressWatcher.watchProgress(progressBar, worker, messageLabel);
        
        final JDialog dialog = new JDialog((Window)SwingUtilities.getRoot(dbTree), "Refresh");
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
        dialog.setLocationRelativeTo(getSession().getArchitectFrame());
        thread.start();
        dialog.setVisible(true);
        
    }

}
