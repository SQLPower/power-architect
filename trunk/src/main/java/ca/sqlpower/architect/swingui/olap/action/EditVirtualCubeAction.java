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

package ca.sqlpower.architect.swingui.olap.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.VirtualCubeEditPanel;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditVirtualCubeAction extends AbstractArchitectAction{
    
    /**
     * The virtual cube this action edits.
     */
    private final VirtualCube vCube;
    
    /**
     * The frame or dialog that will own the popup window.
     */
    private final Window dialogOwner;

    public EditVirtualCubeAction(ArchitectSwingSession session, VirtualCube vCube, PlayPen pp) {
        super(
                session,
                pp,
                "Virtual Cube Properties...",
                "Edit the properties of "+vCube.getName()+" in a dialog",
                (String) null);
        this.dialogOwner = SwingUtilities.getWindowAncestor(pp);
        this.vCube = vCube;
    }

    public void actionPerformed(ActionEvent e) {
        DataEntryPanel panel = new VirtualCubeEditPanel(vCube);
        JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(panel, dialogOwner, "Virtual Cube Properties", DataEntryPanelBuilder.OK_BUTTON_LABEL);
        dialog.setLocationRelativeTo(getSession().getArchitectFrame());
        dialog.setVisible(true);
    }
}
