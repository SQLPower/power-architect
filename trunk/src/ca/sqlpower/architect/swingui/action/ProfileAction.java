package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.ProfileResultFormatter;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class ProfileAction extends AbstractAction {

    
    protected DBTree dbTree;
    protected ProfileManager profileManager;
    protected JDialog d;
            
    
    public ProfileAction(ProfileManager profileManager) {
        super("Profile...", ASUtils.createJLFIcon( "general/Information",
                        "Information", 
                        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        
        putValue(SHORT_DESCRIPTION, "Profile Tables");
        this.profileManager = profileManager;
        
    }
    
        
    public void actionPerformed(ActionEvent e) {
        try {
            Set <SQLTable> tables = new HashSet();
            for ( TreePath p : dbTree.getSelectionPaths() ) {
                SQLObject so = (SQLObject) p.getLastPathComponent();
                Collection<SQLTable> tablesUnder = tablesUnder(so);
                System.out.println("Tables under "+so+" are: "+tablesUnder);
                tables.addAll(tablesUnder);
            }
            
            profileManager.createProfiles(tables);
            
            for ( SQLTable t : tables ) {
                ProfileResult pr = profileManager.getResult(t);
                System.out.println(t.getName()+"  "+pr.toString());
                for ( SQLColumn c : t.getColumns() ) {
                    pr = profileManager.getResult(c);
                    System.out.println(c.getName()+"["+c.getSourceDataTypeName()+"]   "+pr);
                }
            }
            
            d = new JDialog(ArchitectFrame.getMainInstance(),"Table Profiles");
            
            JPanel cp = new JPanel(new BorderLayout());
            
            
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
            
            
            cp.add(editorScrollPane, BorderLayout.CENTER);
            
            
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                
            Action okAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    d.setVisible(false);
                }
            };
            okAction.putValue(Action.NAME, "OK");
            JDefaultButton okButton = new JDefaultButton(okAction);
            buttonPanel.add(okButton);
                
            cp.add(buttonPanel, BorderLayout.SOUTH);
            ArchitectPanelBuilder.makeJDialogCancellable(
                    d, new CommonCloseAction(d));
            d.getRootPane().setDefaultButton(okButton);
            d.setContentPane(cp);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
            d.setVisible(true);
            
            
            
        } catch (SQLException ex) {
            ASUtils.showExceptionDialogNoReport(dbTree, "Error during profile run", ex);
        } catch (Exception ex) {
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
}
