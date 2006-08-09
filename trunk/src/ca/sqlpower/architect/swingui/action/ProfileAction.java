package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.lowagie.text.DocumentException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfilePDFFormat;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.ProfileResultFormatter;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.ProgressWatcher;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.event.TaskTerminationEvent;
import ca.sqlpower.architect.swingui.event.TaskTerminationListener;

public class ProfileAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ProfileAction.class);

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
            
            d = new JDialog(ArchitectFrame.getMainInstance(),"Table Profiles");
            final JPanel cp = new JPanel(new BorderLayout());
            final JProgressBar bar = new JProgressBar();
            bar.setPreferredSize(new Dimension(450,20));
            cp.add(bar, BorderLayout.CENTER);

            cp.add(buttonPanel, BorderLayout.SOUTH);
            ArchitectPanelBuilder.makeJDialogCancellable(
                    d, new CommonCloseAction(d));
            d.getRootPane().setDefaultButton(okButton);
            d.setContentPane(cp);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
            d.setVisible(true);
            
            new ProgressWatcher(bar,profileManager);
            
            new Thread( new Runnable() {
            
            	public void run() {
            	    try {
                        profileManager.createProfiles(tables);
                        cp.remove(bar);
                        bar.setVisible(false);
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
                        d.pack();
System.out.println("\n\nPDF output starts:");
                        try {
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            List tabList = new ArrayList(tables);
                            ProfilePDFFormat.createPdf(buffer,tabList,profileManager);
System.out.println("\n\nprofiled tables:"+ tabList);
                            FileOutputStream file = new FileOutputStream(new File("M:\\architect_profile.pdf"));
System.out.println("\n\nPDF:"+ buffer.toString());
                            buffer.writeTo(file);
                            file.close();
                        } catch (DocumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                        
                        
                    } catch (SQLException e) {
                        logger.error("Error in Profile Action ", e);
                        ASUtils.showExceptionDialogNoReport(dbTree, "Error during profile run", e);
                    } catch (ArchitectException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            	}
            
            }).start();

            
            
            
            
            
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
    
    private class DisplayProfileResult implements TaskTerminationListener {

        Set<SQLTable> tables;
        ProfileManager pm;
        JDialog d;

        public DisplayProfileResult(Set<SQLTable> tables,
                                    ProfileManager pm,
                                    JDialog d) {
            this.tables = tables;
            this.pm = pm;
            this.d = d;
        }
        
        public void taskFinished(TaskTerminationEvent e) {

            
            System.out.println("\n\n\n=========================================");
            try {
                for ( SQLTable t : tables ) {
                    ProfileResult pr = profileManager.getResult(t);
                    System.out.println(t.getName()+"  "+pr.toString());
                        for ( SQLColumn c : t.getColumns() ) {
                            pr = profileManager.getResult(c);
                            System.out.println(c.getName()+"["+c.getSourceDataTypeName()+"]   "+pr);
                        }
                }
            } catch (ArchitectException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            return;
           /* 
            d.removeAll();
            d.setVisible(false);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            Action okAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    d.setVisible(false);
                }
            };
            okAction.putValue(Action.NAME, "OK");
            JDefaultButton okButton = new JDefaultButton(okAction);
            buttonPanel.add(okButton);
            
            
            
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
            cp.add(buttonPanel, BorderLayout.SOUTH);
            ArchitectPanelBuilder.makeJDialogCancellable(
                    d, new CommonCloseAction(d));
            d.getRootPane().setDefaultButton(okButton);
            d.setContentPane(cp);
            
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
            d.setVisible(true);*/
        }
        
    }
}
