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
package ca.sqlpower.architect.swingui.action;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;


public class ProfilePanelAction extends ProgressAction {
    private static final Logger logger = Logger.getLogger(ProfilePanelAction.class);

    private final DBTree dbTree;
    private final ProfileManager profileManager;
    private JDialog dialog;

    public ProfilePanelAction(ArchitectSwingSession session, ProfileManager profileManager) {
        super(session, "Profile...", "Profile Tables", "Table_profiled");
        dbTree = frame.getDbTree();
        this.profileManager = profileManager; 
    }

    private void profileItemsFromDBTree(ActionMonitor monitor) {
        if (dbTree == null) {
            logger.debug("dbtree was null when actionPerformed called");
            return;
        }
        if (dbTree.getSelectionPaths() == null) {
            JOptionPane.showMessageDialog(dialog,
                    "Please select table(s) in the Database Tree to profile them",
                    "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TreePath targetDBPath = dbTree.getPathForRow(0);
        for (TreePath path: dbTree.getSelectionPaths()){
            // We wanted to cancel so exit
            if (monitor.isCancelled()) {
                logger.debug("Profile load canceled");
                return;
            }
            if (path.isDescendant(targetDBPath)) {

                int answer = JOptionPane.showConfirmDialog(dialog,
                        "Cannot perform profiling on the project database." +
                        "\nDo you want to continue profiling?",
                        "Continue Profiling",JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.CANCEL_OPTION){
                    return;
                } else {
                    break;
                }

            }
        }

        logger.debug("dbTree.getSelectionPaths() # = " + dbTree.getSelectionPaths().length);
        try {
            Set<SQLObject> sqlObject = new HashSet<SQLObject>();
            for ( TreePath tp : dbTree.getSelectionPaths() ) {
                if (monitor.isCancelled()) {
                    logger.debug("Profile load canceled");
                    return;
                }
                logger.debug("Top of first loop, treepath=" + tp);
                // skip the target db
                if (tp.isDescendant(targetDBPath)) continue;
                if ( tp.getLastPathComponent() instanceof SQLDatabase ) {
                    sqlObject.add((SQLDatabase)tp.getLastPathComponent());
                }
                else if ( tp.getLastPathComponent() instanceof SQLCatalog ) {
                    SQLCatalog cat = (SQLCatalog)tp.getLastPathComponent();
                    sqlObject.add(cat);
                    SQLDatabase db = ArchitectUtils.getAncestor(cat,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);
                } else if ( tp.getLastPathComponent() instanceof SQLSchema ) {
                    SQLSchema sch = (SQLSchema)tp.getLastPathComponent();
                    sqlObject.add(sch);

                    SQLCatalog cat = ArchitectUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = ArchitectUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);
                }  else if ( tp.getLastPathComponent() instanceof SQLTable ) {
                    SQLTable tab = (SQLTable)tp.getLastPathComponent();
                    sqlObject.add(tab);

                    SQLSchema sch = ArchitectUtils.getAncestor(tab,SQLSchema.class);
                    if ( sch != null && sqlObject.contains(sch))
                        sqlObject.remove(sch);
                    SQLCatalog cat = ArchitectUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = ArchitectUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);
                } else if ( tp.getLastPathComponent() instanceof SQLTable.Folder ) {
                    SQLTable tab = ArchitectUtils.getAncestor((Folder)tp.getLastPathComponent(),SQLTable.class);
                    sqlObject.add(tab);
                    SQLSchema sch = ArchitectUtils.getAncestor(tab,SQLSchema.class);
                    if ( sch != null && sqlObject.contains(sch))
                        sqlObject.remove(sch);
                    SQLCatalog cat = ArchitectUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = ArchitectUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);

                } else if ( tp.getLastPathComponent() instanceof SQLColumn ) {
                    SQLTable tab = ((SQLColumn)tp.getLastPathComponent()).getParentTable();
                    sqlObject.add((SQLColumn)tp.getLastPathComponent());
                    SQLSchema sch = ArchitectUtils.getAncestor(tab,SQLSchema.class);
                    if ( sch != null && sqlObject.contains(sch))
                        sqlObject.remove(sch);
                    SQLCatalog cat = ArchitectUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = ArchitectUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);

                }
            }
            monitor.setJobSize(sqlObject.size());
            final Set<SQLTable> tables = new HashSet<SQLTable>();
            for ( SQLObject o : sqlObject ) {
                if (monitor.isCancelled()) {
                    logger.debug("Profile load canceled");
                    return;
                }
                if ( o instanceof SQLColumn){
                    tables.add(((SQLColumn)o).getParentTable());
                } else {
                    tables.addAll(ArchitectUtils.tablesUnder(o));
                }
                monitor.setProgress(monitor.getProgress()+1);
            }

            logger.debug("Calling profileManager.asynchCreateProfiles(tables)");
            profileManager.asynchCreateProfiles(tables);
            JDialog profileDialog = session.getProfileDialog();
            profileDialog.pack();
            profileDialog.setVisible(true);

        } catch (Exception ex) {
            logger.error("Error in Profile Action ", ex);
            ASUtils.showExceptionDialog(session, "Error during profile run", ex);
        }
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public JDialog getDialog() {
        return dialog;
    }

    public void setDialog(JDialog dialog) {
        this.dialog = dialog;
    }

 
    @Override
    public String getDialogMessage() {
        return "Preparing Profiles:";
    }

    @Override
    public void cleanUp(ActionMonitor monitor) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void doStuff(ActionMonitor monitor, Map<String, Object> properties) {
        monitor.setStarted(true);
        profileItemsFromDBTree(monitor);
        monitor.setFinished(true);
    }

    @Override
    public boolean setup(ActionMonitor monitor, Map<String, Object> properties) {
        return true;
    }
    
    @Override
    public String getButtonText() {
        return "Cancel";
    }

}