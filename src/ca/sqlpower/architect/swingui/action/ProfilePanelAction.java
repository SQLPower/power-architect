package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
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
import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.SwingUserSettings;


public class ProfilePanelAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ProfilePanelAction.class);

    protected DBTree dbTree;
    protected TableProfileManager profileManager;
    private JDialog dialog;

    public ProfilePanelAction() {
        super("Profile...", ASUtils.createJLFIcon( "general/Information",
                "Information",
                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));

        putValue(SHORT_DESCRIPTION, "Profile Tables");
    }

    /**
     * Called to profile the item selected in the tree 
     * and pop up the ProfilePanel.
     */
    public void actionPerformed(ActionEvent e) {
        if (dbTree == null) {
            logger.debug("dbtree was null when actionPerformed called");
            return;
        }
        if ( dbTree.getSelectionPaths() == null ) {
            logger.debug("dbtree path selection was null when actionPerformed called");
            return;
        }

        TreePath targetDBPath = dbTree.getPathForRow(0);
        for (TreePath path: dbTree.getSelectionPaths()){
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
            JDialog profileDialog = ArchitectFrame.getMainInstance().getProject().getProfileDialog();
            profileDialog.pack();
            profileDialog.setVisible(true);

        } catch (Exception ex) {
            logger.error("Error in Profile Action ", ex);
            ASUtils.showExceptionDialog(dbTree, "Error during profile run", ex, new ArchitectExceptionReportFactory());
        }
    }

    public void setDBTree(DBTree dbTree) {
        this.dbTree = dbTree;
    }

    public TableProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(TableProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public JDialog getDialog() {
        return dialog;
    }

    public void setDialog(JDialog dialog) {
        this.dialog = dialog;
    }
}