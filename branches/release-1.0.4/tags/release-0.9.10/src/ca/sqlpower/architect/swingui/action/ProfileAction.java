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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
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


public class ProfileAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(ProfileAction.class);

    private final DBTree dbTree;
    private final ProfileManager profileManager;

    public ProfileAction(ArchitectSwingSession session, ProfileManager profileManager) {
        super(session, "Profile...", "Profile Tables", "Table_profiled");
        dbTree = frame.getDbTree();
        if (dbTree == null) {
            throw new NullPointerException("The database tree is null");
        }
        this.profileManager = profileManager; 
    }

    private void profileItemsFromDBTree() {
        TreePath targetDBPath = dbTree.getPathForRow(0);
        
        if (dbTree.getSelectionPaths() != null) {
            for (TreePath path: dbTree.getSelectionPaths()){
                if (path.isDescendant(targetDBPath)) {
    
                    int answer = JOptionPane.showConfirmDialog(session.getArchitectFrame(),
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
        } else {
            //this is a very rare case where you load a project then immediately push 
            //profile, it should not tell you to select a table. All other cases will
            //have something selected
            dbTree.setSelectionPath(targetDBPath);
        }

        //logger.debug("dbTree.getSelectionPaths() # = " + dbTree.getSelectionPaths().length);
        try {
            Set<SQLObject> sqlObject = new HashSet<SQLObject>();
            for ( TreePath tp : dbTree.getSelectionPaths() ) {
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
            final Set<SQLTable> tables = new HashSet<SQLTable>();
            for ( SQLObject o : sqlObject ) {
                if ( o instanceof SQLColumn){
                    tables.add(((SQLColumn)o).getParentTable());
                } else {
                    tables.addAll(ArchitectUtils.tablesUnder(o));
                }
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

    public void actionPerformed(ActionEvent e) {
        profileItemsFromDBTree();
    }

}