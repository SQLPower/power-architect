package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.DataMoverPanel;

/**
 * A simple action that creates and displays a DataMoverPanel
 * in its own dialog.
 */
public class DataMoverAction extends AbstractAction {

    private final JFrame owner;
    private final ArchitectSession architectSession;
    
    public DataMoverAction(JFrame owner, ArchitectSession architectSession) {
        super("Copy Table Data...");
        this.owner = owner;
        this.architectSession = architectSession;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            JDialog d = new JDialog(owner, "Copy table data");
            DataMoverPanel dmp = new DataMoverPanel(architectSession);
            d.add(dmp.getPanel());
            d.pack();
            d.setLocationRelativeTo(owner);
            d.setVisible(true);
        } catch (Exception ex) {
            ASUtils.showExceptionDialogNoReport(
                    owner, "Couldn't start Data Mover", ex);
        }
    }
}
