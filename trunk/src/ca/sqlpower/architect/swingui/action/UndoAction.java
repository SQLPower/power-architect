package ca.sqlpower.architect.swingui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.undo.UndoManager;

public class UndoAction extends AbstractArchitectAction {
	
    private static final Logger logger = Logger.getLogger(UndoAction.class);
    
	private class ManagerListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			updateSettingsFromManager();
		}
	}

	private final UndoManager manager;
	private ChangeListener managerListener = new ManagerListener();
	
	public UndoAction(ArchitectSwingSession session, UndoManager manager) {
        super(session, "Undo", "Undo", "undo_arrow");
		putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.manager = manager;
        this.manager.addChangeListener(managerListener);
		updateSettingsFromManager();
	}
	
	public void actionPerformed(ActionEvent evt ) {
        if (logger.isDebugEnabled()) {
            logger.debug(manager);
            int choice = JOptionPane.showConfirmDialog(null,
                    "Undo manager state dumped to logger." +
                    "\n\n" +
                    "Proceed with undo?");
            if (choice == JOptionPane.YES_OPTION) {
                manager.undo();
            }
        } else {
            manager.undo();
        }
	}

	private void updateSettingsFromManager() {
		putValue(SHORT_DESCRIPTION, manager.getUndoPresentationName());
		setEnabled(manager.canUndo());
	}
}
