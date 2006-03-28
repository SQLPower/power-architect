package ca.sqlpower.architect.swingui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.undo.UndoManager;

public class UndoAction extends AbstractAction {
	
	private class ManagerListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			updateSettingsFromManager();
		}
	}

	private UndoManager manager;
	private ChangeListener managerListener = new ManagerListener();
	
	public UndoAction() {
		putValue(Action.SMALL_ICON, ASUtils.createJLFIcon("general/Undo",
				"Undo",
				ArchitectFrame.getMainInstance().getSwingUserSettings().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(Action.NAME,"Undo");
		putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		updateSettingsFromManager();
	}
	
	public void actionPerformed(ActionEvent evt ) {
		manager.undo();
	}

	/**
	 * Attaches this action to the given undo manager.
	 * 
	 * @param manager The manager to attach to, or <code>null</code> for no manager.
	 */
	public void setManager(UndoManager manager) {
		if (this.manager != null) {
			this.manager.removeChangeListener(managerListener);
		}
		
		this.manager = manager;
		
		if (this.manager != null) {
			this.manager.addChangeListener(managerListener);
		}
	}

	private void updateSettingsFromManager() {
		if (manager == null) {
			putValue(SHORT_DESCRIPTION, "Can't Undo");
			setEnabled(false);
		} else {
			putValue(SHORT_DESCRIPTION, manager.getUndoPresentationName());
			setEnabled(manager.canUndo());
		}
	}
}
