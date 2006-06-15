package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.etl.PLUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.EngineExecPanel;
import ca.sqlpower.architect.swingui.JDefaultButton;

public class RunPLAction  extends AbstractAction{

    private final static Logger logger = Logger.getLogger(RunPLAction.class);
    
    
    public void actionPerformed(ActionEvent e) {
        File plEngine = new File(ArchitectFrame.getMainInstance().getUserSettings().getETLUserSettings().getString(ETLUserSettings.PROP_PL_ENGINE_PATH,""));                  
        File plDir = plEngine.getParentFile();
        File engineExe = new File(plDir, "PowerLoader_odbc.exe");
        
        final StringBuffer commandLine = getPLCommandLine();
        System.out.println("Running pl");
        if ( engineExe.exists()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        final Process proc = Runtime.getRuntime().exec(commandLine.toString());
                        // Could in theory make this use ArchitectPanelBuilder, by creating
                        // a JPanel subclass, but may not be worthwhile as it has both an
                        // Abort and a Close button...
                        final JDialog pld = new JDialog(ArchitectFrame.getMainInstance(), "Power*Loader Engine");
                        
                        EngineExecPanel eep = new EngineExecPanel(commandLine.toString(), proc);
                        pld.setContentPane(eep);
                        
                        Action closeAction = new CommonCloseAction(pld);
                        JButton abortButton = new JButton(eep.getAbortAction());
                        JDefaultButton closeButton = new JDefaultButton(closeAction);
                        
                        
                        JCheckBox scrollLockCheckBox = new JCheckBox(eep.getScrollBarLockAction());
                        
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        buttonPanel.add(abortButton);
                        buttonPanel.add(closeButton);
                        buttonPanel.add(scrollLockCheckBox);
                        eep.add(buttonPanel, BorderLayout.SOUTH);
                        
                        pld.getRootPane().setDefaultButton(closeButton);
                        pld.pack();
                        pld.setLocationRelativeTo(ArchitectFrame.getMainInstance());
                        pld.setVisible(true);
                    } catch (IOException ie){
                        JOptionPane.showMessageDialog(ArchitectFrame.getMainInstance(), "Unexpected Exception running Engine:\n"+ie);
                        logger.error("IOException while trying to run engine.",ie);
                    }
                }
            });
        }
    }


    private StringBuffer getPLCommandLine() {
        
        JTextArea args = new JTextArea();
        
        return new StringBuffer(args.getText());
    }

}
