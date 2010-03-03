/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

public class PasteSelectedAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(PasteSelectedAction.class);
    
    public PasteSelectedAction(final ArchitectSwingSession session) {
        super(session, Messages.getString("PasteSelectedAction.name"), Messages.getString("PasteSelectedAction.description"));
        putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final FocusListener focusListener = new FocusListener() {
        
            public void focusLost(FocusEvent e) {
                PasteSelectedAction.this.setEnabled(false);
            }
        
            public void focusGained(FocusEvent e) {
                PasteSelectedAction.this.setEnabled(true);
            }
        };
        session.getPlayPen().getPanel().addFocusListener(focusListener);
        
        final SessionLifecycleListener<ArchitectSession> lifecycleListener = new SessionLifecycleListener<ArchitectSession>() {
        
            public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
                session.getPlayPen().getPanel().removeFocusListener(focusListener);
            }
        };
        session.addSessionLifecycleListener(lifecycleListener);
    }

    public void actionPerformed(ActionEvent e) {
        PlayPen playPen = session.getPlayPen();
        final Component focusOwner = session.getArchitectFrame().getFocusOwner();
        if (playPen.getPanel().isAncestorOf(focusOwner) || playPen.getPanel() == focusOwner) {
            Transferable clipboardContents = session.getContext().getClipboardContents();
            logger.debug("Pasting " + clipboardContents + " into the playpen.");
            if (clipboardContents != null) {
                playPen.pasteData(clipboardContents);
            } else {
                JOptionPane.showMessageDialog(session.getArchitectFrame(), "There is no contents in the clipboard to paste.", "Clipboard empty", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

}
