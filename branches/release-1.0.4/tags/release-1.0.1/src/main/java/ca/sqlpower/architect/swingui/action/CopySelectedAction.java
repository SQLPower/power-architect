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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBTree;

public class CopySelectedAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(CopySelectedAction.class);
    
    public CopySelectedAction(ArchitectFrame frame) {
        super(frame, Messages.getString("CopySelectedAction.name"), Messages.getString("CopySelectedAction.description"));
        putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        final DBTree dbTree = getSession().getDBTree();
        final Component focusOwner = getSession().getArchitectFrame().getFocusOwner();
        logger.debug("Copy action invoked. Focus owner is " + focusOwner);
        if (getSession().getArchitectFrame().isAncestorOf(focusOwner)) {
            dbTree.copySelection();
        }
    }

}
