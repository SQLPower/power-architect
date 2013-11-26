/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.object.undo.NotifyingUndoManager;

public class UndoAction extends AbstractArchitectAction {
	
    private static final Logger logger = Logger.getLogger(UndoAction.class);
    
	private class ManagerListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			updateSettingsFromManager();
		}
	}

	private NotifyingUndoManager manager;
	private ChangeListener managerListener = new ManagerListener();
	
	public UndoAction(ArchitectSwingSession session, NotifyingUndoManager manager) {
        super(session, Messages.getString("UndoAction.name"), Messages.getString("UndoAction.description"), "undo_arrow"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.manager = manager;
        this.manager.addChangeListener(managerListener);
		updateSettingsFromManager();
	}
	
	public UndoAction(ArchitectSwingSession session, ArchitectFrame frame, NotifyingUndoManager manager) {
	    super(session, frame, Messages.getString("UndoAction.name"), Messages.getString("UndoAction.description"), "undo_arrow"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                    "Undo manager state dumped to logger." + //$NON-NLS-1$
                    "\n\n" + //$NON-NLS-1$
                    "Proceed with undo?"); //$NON-NLS-1$
            if (choice == JOptionPane.YES_OPTION) {
                manager.undo();
            }
        } else {
            manager.undo();
        }
	}

	public void updateSettingsFromManager() {
		putValue(SHORT_DESCRIPTION, manager.getUndoPresentationName());
		setEnabled(manager.canUndo());
	}
	
	public void setUndoManager(NotifyingUndoManager manager) {
	    this.manager.removeChangeListener(managerListener);
	    this.manager = manager;
	    manager.addChangeListener(managerListener);
	    updateSettingsFromManager();
	}
}
