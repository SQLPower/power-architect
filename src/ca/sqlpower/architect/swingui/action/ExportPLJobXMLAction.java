package ca.sqlpower.architect.swingui.action;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.etl.PLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingWorker;
import ca.sqlpower.architect.swingui.PLExportXMLPanel;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.ProgressWatcher;
import ca.sqlpower.architect.swingui.QuickStartWizard;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.WizardDialog;

public class ExportPLJobXMLAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ExportPLJobXMLAction.class);

    protected ArchitectFrame architectFrame;
    protected PlayPen pp;

    /** The PLExport object that this action uses to create PL transactions. */
    protected PLExport plexp;

    /** The dialog box that this action uses to configure plexp. */
    protected JDialog d;

    /** Progress Bar to tell the user PL Export is still running */
    protected JProgressBar plCreateTxProgressBar;
    protected JLabel plCreateTxLabel;

    public ExportPLJobXMLAction() {
        super("PL XML Script Export...",
                // FIXME: This is not a 16 by 16 icon. We need to get a new one, or remove this icon.
              ASUtils.createIcon("PLTransExport",
                                 "PL XML Script Export",
                                 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        architectFrame = ArchitectFrame.getMainInstance();
        putValue(SHORT_DESCRIPTION, "PL XML Script Export");
    }

    /**
     * Sets up the dialog the first time it is called.  After that,
     * just returns without doing anything.
     *
     * <p>Note: the <code>plexp</code> variable must be initialized before calling this method!
     *
     * @throws NullPointerException if <code>plexp</code> is null.
     */
    public synchronized void setupDialog() {

        logger.debug("running setupDialog()");
        if (plexp == null) {
            throw new NullPointerException("setupDialog: plexp was null");
        }

        d = new JDialog(ArchitectFrame.getMainInstance(),
                        "Export ETL Transactions to XML script");

        // set export defaults if necessary
        if (plexp.getFolderName() == null || plexp.getFolderName().trim().length() == 0) {
            plexp.setFolderName(PLUtils.toPLIdentifier(architectFrame.getProject().getName()+"_FOLDER"));
        }

        if (plexp.getJobId() == null || plexp.getJobId().trim().length() == 0) {
            plexp.setJobId(PLUtils.toPLIdentifier(architectFrame.getProject().getName()+"_JOB"));
        }
        
        JPanel plp = new JPanel(new BorderLayout(12,12));
        plp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); 
        
        final PLExportXMLPanel plPanel = new PLExportXMLPanel();
        plPanel.setPLExport(plexp);
        plp.add(plPanel, BorderLayout.CENTER);
        
        // make an intermediate JPanel
        JPanel bottomPanel = new JPanel(new GridLayout(1,2,25,0)); // 25 pixel hgap     
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton(ArchitectPanelBuilder.OK_BUTTON_LABEL);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (!plPanel.applyChanges()) {
                    return;
                }
                
                try {
                    List targetTables;
                    targetTables = pp.getTables();
                   
                    // got this far, so it's ok to run the PL Export thread
                    ExportTxProcess etp = new ExportTxProcess(plexp,targetTables,d,
                            plCreateTxProgressBar,
                            plCreateTxLabel);
                    new Thread(etp).start();       
                } catch (ArchitectException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        });
        buttonPanel.add(okButton);

        Action cancelAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    plPanel.discardChanges();
                    d.setVisible(false);
                }
        };
        cancelAction.putValue(Action.NAME, ArchitectPanelBuilder.CANCEL_BUTTON_LABEL);
        ASUtils.makeJDialogCancellable(d, cancelAction);
        d.getRootPane().setDefaultButton(okButton);
        JButton cancelButton = new JButton(cancelAction);
        buttonPanel.add(cancelButton);

        // stick in the progress bar here...
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));     
        plCreateTxProgressBar = new JProgressBar();
        plCreateTxProgressBar.setStringPainted(true); 
        progressPanel.add(plCreateTxProgressBar);       
        plCreateTxLabel = new JLabel ("Exporting PL Transactions...");
        progressPanel.add(plCreateTxLabel);
        
        // figure out how much space this needs before setting 
        // child components to be invisible
        progressPanel.setPreferredSize(progressPanel.getPreferredSize());  
        plCreateTxProgressBar.setVisible(false);        
        plCreateTxLabel.setVisible(false);      

        bottomPanel.add(progressPanel); // left side, left justified
        bottomPanel.add(buttonPanel); // right side, right justified

        plp.add(bottomPanel, BorderLayout.SOUTH);
        
        d.setContentPane(plp);
        
        // experiment with preferred size crap:
        d.pack();
        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
    }


    public void actionPerformed(ActionEvent e) {
        plexp = architectFrame.getProject().getPLExport();
        setupDialog();
        d.setVisible(true); 
    }

    
    public class ExportTxProcess extends ArchitectSwingWorker {     
        
        PLExport plExport;
        final JDialog d;
        private Runnable nextProcess;
        List targetTables;

        public ExportTxProcess (PLExport plExport,
                                List targetTables, JDialog parentDialog,
                                JProgressBar progressBar,
                                JLabel label) {
            this.plExport = plExport;
            this.targetTables = targetTables;
            d = parentDialog;
            label.setText("Exporting Meta Data...");            
            new ProgressWatcher(progressBar, plExport, label);          
        }       

        public void doStuff() {
            if (isCanceled())
                return;
            // now implements Monitorable, so we can ask it how it's doing
            try {
                plExport.exportXML(targetTables);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ArchitectException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cleanup() throws Exception {
            if (!(d instanceof WizardDialog))
                d.setVisible(false);
            else {
                if (nextProcess != null)
                    new Thread(nextProcess).start();
                WizardDialog wd = ((WizardDialog)d);
                ((QuickStartWizard)wd.getWizard()).UpdateTextArea();
                
            }
        }

    }


    public void setPlayPen(PlayPen pp) {
        this.pp = pp;
    }   
}
