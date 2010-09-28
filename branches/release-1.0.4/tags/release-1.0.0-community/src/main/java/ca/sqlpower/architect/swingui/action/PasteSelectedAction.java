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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;

public class PasteSelectedAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(PasteSelectedAction.class);
    
    public PasteSelectedAction(final ArchitectFrame frame) {
        super(frame, Messages.getString("PasteSelectedAction.name"), Messages.getString("PasteSelectedAction.description"));
        putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        PlayPen playPen = getSession().getPlayPen();
        final Component focusOwner = getSession().getArchitectFrame().getFocusOwner();
        if (playPen.isAncestorOf(focusOwner) || playPen == focusOwner) {
            Transferable clipboardContents = getSession().getContext().getClipboardContents();
            logger.debug("Pasting " + clipboardContents + " into the playpen.");
            if (clipboardContents != null) {
                playPen.pasteData(clipboardContents);
            } else {
                JOptionPane.showMessageDialog(getSession().getArchitectFrame(), "There is no contents in the clipboard to paste.", "Clipboard empty", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

}
