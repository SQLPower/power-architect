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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfilePDFFormat;
import ca.sqlpower.architect.profile.ProfileResultFormatter;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.ProfilePanel;
import ca.sqlpower.architect.swingui.ProgressWatcher;
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
            
            
            d = new JDialog(ArchitectFrame.getMainInstance(), "Table Profiles");
            
            
            Action closeAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    d.setVisible(false);
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

            ArchitectPanelBuilder.makeJDialogCancellable(
                    d, new CommonCloseAction(d));
            d.getRootPane().setDefaultButton(closeButton);
            d.setContentPane(progressViewPanel);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
            d.setVisible(true);

            new ProgressWatcher(progressBar,profileManager,workingOn);

            new Thread( new Runnable() {

                public void run() {
                    try {
                        profileManager.createProfiles(tables, workingOn);




                        progressBar.setVisible(false);
                        
                        JLabel status = new JLabel("Generating reports, Please wait......");
                        progressViewPanel.add(status, BorderLayout.NORTH);
                        status.setVisible(true);

                        JTabbedPane tabPane = new JTabbedPane();
                        JEditorPane editorPane = new JEditorPane();
                        editorPane.setEditable(false);
                        editorPane.setContentType("text/html");
                        ProfileResultFormatter prf = new ProfileResultFormatter();

                        JScrollPane editorScrollPane = new JScrollPane(editorPane);
                        editorScrollPane.setVerticalScrollBarPolicy(
                                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                        editorScrollPane.setPreferredSize(new Dimension(800, 600));
                        editorScrollPane.setMinimumSize(new Dimension(10, 10));
                        
                        JPanel htmlPane = new JPanel(new BorderLayout());
                        htmlPane.add(editorScrollPane,BorderLayout.CENTER);
                        ButtonBarBuilder buttonBuilder = new ButtonBarBuilder();
                        JButton save = new JButton(new AbstractAction("Save") {
                        
                            public void actionPerformed(ActionEvent e) {

                                JFileChooser chooser = new JFileChooser();
                                chooser.addChoosableFileFilter(ASUtils.PDF_FILE_FILTER);
                                int response = chooser.showSaveDialog(d);
                                if (response != JFileChooser.APPROVE_OPTION) {
                                    return;
                                } else {
                                    File file = chooser.getSelectedFile();
                                    if (!file.getPath().endsWith(".pdf")) {
                                        file = new File(file.getPath()+".pdf");
                                    }
                                    if (file.exists()) {
                                        response = JOptionPane.showConfirmDialog(
                                                d,
                                                "The file\n\n"+file.getPath()+"\n\nalready exists. Do you want to overwrite it?",
                                                "File Exists", JOptionPane.YES_NO_OPTION);
                                        if (response == JOptionPane.NO_OPTION) {
                                            actionPerformed(e);
                                            return;
                                        }
                                    }
                                    
                                    final File file2 = new File(file.getPath());
                                    Runnable saveTask = new Runnable() {
                                        public void run() {
                                            List tabList = new ArrayList(tables);
                                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                            FileOutputStream PDFFile = null;
                                            try {
                                                PDFFile = new FileOutputStream(file2);
                                                new ProfilePDFFormat().createPdf(buffer,tabList,profileManager);
                                                buffer.writeTo(PDFFile);
                                            } catch (Exception ex) {
                                                ASUtils.showExceptionDialog(d,"Could not save PDF File", ex);
                                            } finally {
                                                if ( PDFFile != null ) {
                                                    try {
                                                        PDFFile.close();
                                                    } catch (IOException ex) {
                                                        ASUtils.showExceptionDialog(d,"Could not close PDF File", ex);
                                                    }
                                                }
                                            }
                                        }
                                    };
                                    new Thread(saveTask).start();
                                }
                            }
                        
                        });
                        
                        JButton print = new JButton("Print");
                        
                        JButton[] buttonArray = {save,print,closeButton};
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

                        d.remove(progressViewPanel);
                        d.setContentPane(tabPane);
                        d.pack();
                        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
                        editorPane.setText(prf.format(tables,profileManager) );

                    } catch (SQLException e) {
                        logger.error("Error in Profile Action ", e);
                        ASUtils.showExceptionDialogNoReport(dbTree, "Error during profile run", e);
                    } catch (ArchitectException e) {
                        logger.error("Error in Profile Action", e);
                        ASUtils.showExceptionDialog(dbTree, "Error during profile run", e);
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

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }
    

}
