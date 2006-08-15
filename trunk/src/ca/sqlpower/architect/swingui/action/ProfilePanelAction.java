package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResultFormatter;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.ProfilePanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.ProfilePanel.ChartTypes;

public class ProfilePanelAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ProfileAction.class);

    protected DBTree dbTree;
    protected ProfileManager profileManager;
    protected JDialog d;
            
    
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
        try {
            if ( dbTree.getSelectionPaths() == null )
                return;
            
            final Set <SQLTable> tables = new HashSet();
            for ( TreePath p : dbTree.getSelectionPaths() ) {
                SQLObject so = (SQLObject) p.getLastPathComponent();
                Collection<SQLTable> tablesUnder = tablesUnder(so);
                System.out.println("Tables under "+so+" are: "+tablesUnder);
                tables.addAll(tablesUnder);
            }
            
            final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            Action okAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    d.setVisible(false);
                }
            };
            okAction.putValue(Action.NAME, "OK");
            final JDefaultButton okButton = new JDefaultButton(okAction);
            buttonPanel.add(okButton);
            d = new JDialog(ArchitectFrame.getMainInstance(), "Table Profiles");
            profileManager.createProfiles(tables);
            JTabbedPane tabPane = new JTabbedPane();
            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.setContentType("text/html");
            ProfileResultFormatter prf = new ProfileResultFormatter();
            editorPane.setText(prf.format(tables,profileManager) );

            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(800, 600));
            editorScrollPane.setMinimumSize(new Dimension(10, 10));
            
            JPanel htmlPane = new JPanel(new BorderLayout());
            htmlPane.add(editorScrollPane,BorderLayout.CENTER);
            ButtonBarBuilder buttonBuilder = new ButtonBarBuilder();
            JButton save = new JButton("Save");
            JButton print = new JButton("Print");
            JButton close = new JButton("close");
            JButton[] buttonArray = {save,print,close};
            buttonBuilder.addGriddedButtons(buttonArray);
            htmlPane.add(buttonBuilder.getPanel(),BorderLayout.SOUTH);
            tabPane.addTab("Table View", htmlPane );
            ProfilePanel p = new ProfilePanel(profileManager);
            tabPane.addTab("Graph View",p);
            
            JPanel empty = new JPanel();
            tabPane.addTab("Profile Explorer",empty);
            ProfileAction profileAction = new ProfileAction();
            empty.add(new JButton(profileAction));
            profileAction.setDBTree(dbTree);
            profileAction.setProfileManager(profileManager);
            
            List<SQLTable> list = new ArrayList(tables);
            p.setTables(list);
            p.setChartType(ChartTypes.PIE);

            ArchitectPanelBuilder.makeJDialogCancellable(
                    d, new CommonCloseAction(d));
            d.setContentPane(tabPane);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
            d.setVisible(true);
    
        } catch (Exception ex) {
            logger.error("Error in Profile Action ", ex);
            ASUtils.showExceptionDialog(dbTree, "Error during profile run", ex);
        }
    }


    private Collection<SQLTable> tablesUnder(SQLObject so) throws ArchitectException {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        if (so instanceof SQLTable) {
            tables.add((SQLTable) so);
        } else if (so.allowsChildren()) {
            for (SQLObject child : (List<SQLObject>) so.getChildren()) {
                tables.addAll(tablesUnder(child));
            }
        }
        return tables;
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
    

}
