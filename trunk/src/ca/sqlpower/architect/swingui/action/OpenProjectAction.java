package ca.sqlpower.architect.swingui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.RecentMenu;

public class OpenProjectAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(OpenProjectAction.class);
    
    RecentMenu recent;
    
    public OpenProjectAction(ArchitectSwingSession session) {
        super(session, "Open Project...", "Open", "folder");
        this.recent = session.getContext().getRecentMenu();
        putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_O,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(ASUtils.ARCHITECT_FILE_FILTER);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
//            LoadFileWorker worker;
            try {
                session.getContext().createSession(f, true);
//                worker = new LoadFileWorker(f,recent);
//                new Thread(worker).start();
//            } catch (FileNotFoundException e1) {
//                JOptionPane.showMessageDialog(
//                        frame,
//                        "File not found: "+f.getPath());
            } catch (Exception e1) {
                ASUtils.showExceptionDialog(session, "Error loading file", e1);
            }
        }
        logger.info("Opening a Project doesn't work yet ;)");
    }
}

