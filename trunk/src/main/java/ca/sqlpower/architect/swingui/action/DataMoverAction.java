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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DataMoverPanel;

/**
 * A simple action that creates and displays a DataMoverPanel
 * in its own dialog.
 */
public class DataMoverAction extends AbstractAction {

    private final ArchitectFrame frame;
    
    public DataMoverAction(ArchitectFrame frame) {
        super(Messages.getString("DataMoverAction.name")); //$NON-NLS-1$
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            JDialog d = new JDialog(frame, Messages.getString("DataMoverAction.dialogTitle")); //$NON-NLS-1$
            DataMoverPanel dmp = new DataMoverPanel(frame.getCurrentSession());
            d.add(dmp.getPanel());
            d.pack();
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
        } catch (Exception ex) {
            ASUtils.showExceptionDialogNoReport(
                    frame, Messages.getString("DataMoverAction.couldNotStartDataMover"), ex); //$NON-NLS-1$
        }
    }
}
