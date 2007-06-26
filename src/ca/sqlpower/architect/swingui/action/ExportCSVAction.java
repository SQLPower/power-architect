package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.etl.ExportCSV;
import ca.sqlpower.architect.swingui.PlayPen;

public class ExportCSVAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(ExportCSVAction.class);
    
    /**
     * The play pen that this action operates on.
     */
    private final PlayPen playpen;
    
    /**
     * The frame that will own the dialog(s) created by this action.
     * Neither argument is allowed to be null.
     */
    private final JFrame parentFrame;
    
    public ExportCSVAction(JFrame parentFrame, PlayPen playpen) {
        super("Export CSV");
        
        if (playpen == null) throw new NullPointerException("Null playpen");
        this.playpen = playpen;

        if (parentFrame == null) throw new NullPointerException("Null parentFrame");
        this.parentFrame = parentFrame;
    }
    
    public void actionPerformed(ActionEvent e) {
        FileWriter output = null;
        try {
            ExportCSV export = new ExportCSV(playpen.getDatabase().getTables());

            File file = null;

            JFileChooser fileDialog = new JFileChooser();
            fileDialog.setSelectedFile(new File("map.csv"));

            if (fileDialog.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION){
                file = fileDialog.getSelectedFile();
            } else {
                return;
            }

            output = new FileWriter(file);
            output.write(export.getCSVMapping());
            output.flush();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        } catch (ArchitectException e1) {
            throw new ArchitectRuntimeException(e1);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e1) {
                    logger.error("IO Error", e1);
                }
            }
        }
    }
}
