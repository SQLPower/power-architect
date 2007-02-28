package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.profile.ProfileColumn;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.Monitorable;
import ca.sqlpower.architect.swingui.ProfilePanel;
import ca.sqlpower.architect.swingui.ProgressWatcher;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.ProfilePanel.ChartTypes;
import ca.sqlpower.architect.swingui.table.ProfileTable;
import ca.sqlpower.architect.swingui.table.ProfileTableModel;
import ca.sqlpower.architect.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.architect.swingui.table.TableModelSearchDecorator;
import ca.sqlpower.architect.swingui.table.TableModelSortDecorator;

import com.jgoodies.forms.builder.ButtonBarBuilder;


public class ProfilePanelAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ProfilePanelAction.class);

    protected DBTree dbTree;
    protected ProfileManager profileManager;
    private JDialog dialog;

    public ProfilePanelAction() {
        super("Profile...", ASUtils.createJLFIcon( "general/Information",
                "Information",
                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));

        putValue(SHORT_DESCRIPTION, "Profile Tables");
    }

    /**
     * Called to pop up the ProfilePanel
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

        try {
            Set<SQLObject> sqlObject = new HashSet<SQLObject>();
            for ( TreePath tp : dbTree.getSelectionPaths() ) {
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

            final ArrayList<SQLObject> filter = new ArrayList<SQLObject>();
            final Set<SQLTable> tables = new HashSet<SQLTable>();
            for ( SQLObject o : sqlObject ) {
                if ( o instanceof SQLColumn){
                    tables.add(((SQLColumn)o).getParentTable());
                } else {
                    tables.addAll(ArchitectUtils.tablesUnder(o));
                }
                if (! (o instanceof Folder)){
                    filter.add(o);
                }
            }

            // TODO use this dialog to display progress bar
            final JDialog d = new JDialog(ArchitectFrame.getMainInstance(), "Table Profiles");

            if ( !dialog.isVisible()) {
                ArchitectFrame.getMainInstance().getProject().getFilter().clear();
            }
            ArchitectFrame.getMainInstance().getProject().getFilter().addAll(filter);

            final CommonCloseAction commonCloseAction = new CommonCloseAction(dialog);
            Action closeAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    profileManager.setCancelled(true);
                    commonCloseAction.actionPerformed(evt);
                }
            };
            closeAction.putValue(Action.NAME, "Close");
            final JDefaultButton closeButton = new JDefaultButton(closeAction);

            final JPanel progressViewPanel = new JPanel(new BorderLayout());
            final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeButton);
            progressViewPanel.add(buttonPanel, BorderLayout.SOUTH);
            final JProgressBar progressBar = new JProgressBar();
            progressBar.setPreferredSize(new Dimension(450,20));
            progressViewPanel.add(progressBar, BorderLayout.CENTER);
            final JLabel workingOn = new JLabel("Profiling:");
            progressViewPanel.add(workingOn, BorderLayout.NORTH);

            ASUtils.makeJDialogCancellable(
                    dialog, commonCloseAction);
            dialog.getRootPane().setDefaultButton(closeButton);
            dialog.setContentPane(progressViewPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(ArchitectFrame.getMainInstance());
            dialog.setVisible(true);

            // XXX This should be its own Action class?
            new Thread( new Runnable() {

                public void run() {
                    try {
                        List<SQLTable> toBeProfiled = new ArrayList<SQLTable>();
                        for (SQLTable t: tables) {
                            if (profileManager.getResult(t)== null) {
                                toBeProfiled.add(t);
                                workingOn.setText("Adding "+t.getName()+
                                        "  ("+toBeProfiled.size()+")");
                            }
                        }

                        Monitorable m = profileManager.asynchCreateProfiles(toBeProfiled);
                        new ProgressWatcher(progressBar, m, workingOn);
                        progressBar.setVisible(false);

                        JLabel status = new JLabel("Generating reports, Please wait......");
                        progressViewPanel.add(status, BorderLayout.NORTH);
                        status.setVisible(true);

                        JTabbedPane tabPane = new JTabbedPane();

                        ProfileTableModel tm = new ProfileTableModel();

                        for (SQLObject sqo: ArchitectFrame.getMainInstance().getProject().getFilter()){
                            tm.addFilter(sqo);
                        }

                        tm.setProfileManager(profileManager);

                        TableModelSearchDecorator searchDecorator =
                            new TableModelSearchDecorator(tm);
                        TableModelSortDecorator tableModelSortDecorator =
                            new TableModelSortDecorator(searchDecorator);
                        final ProfileTable viewTable =
                            new ProfileTable(tableModelSortDecorator);
                        searchDecorator.setTableTextConverter(viewTable);
                        TableModelColumnAutofit columnAutoFit =
                            new TableModelColumnAutofit(tableModelSortDecorator, viewTable);

                        JTableHeader tableHeader = viewTable.getTableHeader();
                        tableModelSortDecorator.setTableHeader(tableHeader);
                        columnAutoFit.setTableHeader(tableHeader);

                        viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                        ProfilePanelMouseListener profilePanelMouseListener =
                            new ProfilePanelMouseListener();
                        profilePanelMouseListener.setTabPane(tabPane);
                        viewTable.addMouseListener( profilePanelMouseListener);
                        JScrollPane editorScrollPane = new JScrollPane(viewTable);
                        editorScrollPane.setVerticalScrollBarPolicy(
                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                        editorScrollPane.setPreferredSize(new Dimension(800, 600));
                        editorScrollPane.setMinimumSize(new Dimension(10, 10));

                        // reset column widths
                        ((ProfileTable) viewTable).initColumnSizes();

                        JPanel tableViewPane = new JPanel(new BorderLayout());

                        tableViewPane.add(editorScrollPane,BorderLayout.CENTER);

                        JLabel searchLabel = new JLabel("Search:");
                        JTextField searchField = new JTextField(searchDecorator.getDoc(),"",25);
                        searchField.setEditable(true);
                        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                        searchPanel.add(searchLabel);
                        searchPanel.add(searchField);
                        tableViewPane.add(searchPanel,BorderLayout.NORTH);

                        ButtonBarBuilder buttonBuilder = new ButtonBarBuilder();
                        JButton save = new JButton(new SaveProfileAction(dialog,viewTable,profileManager));

                        JButton refresh = new JButton(new AbstractAction("Refresh"){

                            public void actionPerformed(ActionEvent e) {
                                Set<SQLTable> uniqueTables = new HashSet();

                                //If no column in the viewTable is selected, refresh
                                //all the columns that are visible.
                                if (viewTable.getSelectedRowCount() == 0){
                                    for (int i = 0; i < viewTable.getRowCount(); i++){
                                        refreshProfileTable(viewTable, uniqueTables, i);
                                    }
                                } else{
                                    for (int i: viewTable.getSelectedRows()) {
                                        refreshProfileTable(viewTable, uniqueTables, i);
                                    }
                                }

                                try {
                                    profileManager.setCancelled(false);
                                    profileManager.createProfiles(uniqueTables);
                                } catch (SQLException e1) {
                                    throw new RuntimeException(e1);
                                } catch (ArchitectException e1) {
                                    throw new ArchitectRuntimeException(e1);
                                }
                            }

                            private void refreshProfileTable(final ProfileTable viewTable, Set<SQLTable> uniqueTables, int i) {
                                Object o = viewTable.getValueAt(i,
                                        viewTable.convertColumnIndexToView(
                                                ProfileColumn.valueOf("TABLE").ordinal()));
                                SQLTable table = (SQLTable) o ;
                                uniqueTables.add(table);
                            }

                        });

                        JButton delete  = new JButton(new AbstractAction("Delete"){

                            public void actionPerformed(ActionEvent e) {
                                int[] killMe = viewTable.getSelectedRows();
                                Arrays.sort(killMe);

                                // iterate backwards so the rows don't shift away on us!
                                for (int i = killMe.length-1; i >= 0; i--) {
                                    logger.debug("Deleting row "+
                                            killMe[i]+
                                            ": "+
                                            viewTable.getValueAt(killMe[i],
                                                    viewTable.convertColumnIndexToView(
                                                            ProfileColumn.valueOf("COLUMN").ordinal())));
                                    SQLColumn col = (SQLColumn) viewTable.getValueAt(killMe[i],
                                            viewTable.convertColumnIndexToView(
                                                    ProfileColumn.valueOf("COLUMN").ordinal()));
                                    try {
                                        profileManager.remove(col);
                                    } catch (ArchitectException e1) {
                                        ASUtils.showExceptionDialog(dialog,
                                           "Could delete row of:" + col.getName(), e1, new ArchitectExceptionReportFactory());
                                    }
                                }
                            }

                        });
                        JButton deleteAll = new JButton(new AbstractAction("Delete All"){

                            public void actionPerformed(ActionEvent e) {
                                while ( viewTable.getRowCount() > 0 ) {
                                    SQLColumn col = (SQLColumn) viewTable.getValueAt(0,
                                            viewTable.convertColumnIndexToView(
                                                    ProfileColumn.valueOf("COLUMN").ordinal()));
                                    try {
                                        profileManager.remove(col);
                                    } catch (ArchitectException e1) {
                                        ASUtils.showExceptionDialog(dialog,
                                            "Could delete row of:" + col.getName(), e1, new ArchitectExceptionReportFactory());
                                    }
                                }
                            }

                        });

                        JButton[] buttonArray = {refresh,delete,deleteAll,save,closeButton};
                        buttonBuilder.addGriddedButtons(buttonArray);
                        tableViewPane.add(buttonBuilder.getPanel(),BorderLayout.SOUTH);
                        tabPane.addTab("Table View", tableViewPane );
                        ProfilePanel p = new ProfilePanel(profileManager);
                        p.setViewTable(viewTable);
                        p.setTabPane(tabPane);
                        p.setTableModel(tm);
                        tabPane.addTab("Graph View",p);

                        profilePanelMouseListener.setProfilePanel(p);
                        p.setChartType(ChartTypes.PIE);


                        if ( viewTable.getRowCount() > 0 ) {
                            SQLColumn col = (SQLColumn)viewTable.getValueAt(0,
                                    viewTable.convertColumnIndexToView(
                                            ProfileColumn.valueOf("COLUMN").ordinal()));
                            p.getTableSelector().setSelectedItem(col.getParentTable());
                            p.getColumnSelector().setSelectedValue(col,true);
                        }


                        dialog.setVisible(false);
                        dialog.remove(progressViewPanel);
                        dialog.setContentPane(tabPane);
                        dialog.pack();
                        dialog.setLocationRelativeTo(ArchitectFrame.getMainInstance());
                        dialog.setVisible(true);


                    } catch (SQLException e) {
                        logger.error("Error in Profile Action ", e);
                        ASUtils.showExceptionDialogNoReport(dbTree,
                            "Error during profile run", e);
                    } catch (ArchitectException e) {
                        logger.error("Error in Profile Action", e);
                        ASUtils.showExceptionDialog(dbTree,
                            "Error during profile run", e, new ArchitectExceptionReportFactory());
                    }
                }

            }, "ProfilePanelAction Profile Runner").start();


        } catch (Exception ex) {
            logger.error("Error in Profile Action ", ex);
            ASUtils.showExceptionDialog(dbTree, "Error during profile run", ex, new ArchitectExceptionReportFactory());
        }
    }

    public void setDBTree(DBTree dbTree) {
        this.dbTree = dbTree;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }


    /**
     * The PPMouseListener class receives all mouse and mouse motion
     * events in the PlayPen.  It tries to dispatch them to the
     * ppcomponents, and also handles playpen-specific behaviour like
     * rubber band selection and popup menu triggering.
     */
    protected class ProfilePanelMouseListener implements MouseListener  {
        private ProfilePanel profilePanel;
        private JTabbedPane tabPane;

        public ProfilePanel getProfilePanel() {
            return profilePanel;
        }

        public void setProfilePanel(ProfilePanel profilePanel) {
            this.profilePanel = profilePanel;
        }

        public void mouseClicked(MouseEvent evt) {
            Object obj = evt.getSource();
            if (evt.getClickCount() == 2) {
                if ( obj instanceof JTable ) {
                    JTable t = (JTable)obj;
                    SQLColumn col = (SQLColumn)t.getValueAt(t.getSelectedRow(),
                            t.convertColumnIndexToView(
                                    ProfileColumn.valueOf("COLUMN").ordinal()));
                    profilePanel.getTableSelector().setSelectedItem(col.getParentTable());
                    profilePanel.getColumnSelector().setSelectedValue(col,true);
                    tabPane.setSelectedIndex(1);
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            // don't care
        }

        public void mouseReleased(MouseEvent e) {
            // don't care
        }

        public void mouseEntered(MouseEvent e) {
            // don't care
        }

        public void mouseExited(MouseEvent e) {
            // don't care
        }

        public JTabbedPane getTabPane() {
            return tabPane;
        }

        public void setTabPane(JTabbedPane tabPane) {
            this.tabPane = tabPane;
        }



    }


    public JDialog getDialog() {
        return dialog;
    }

    public void setDialog(JDialog dialog) {
        this.dialog = dialog;
    }


}