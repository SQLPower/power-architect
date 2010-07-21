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

import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.DimensionEditPanel;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditDimensionAction extends AbstractArchitectAction {

    /**
     * The dimension this action edits.
     */
    private final Dimension dimension;
    
    /**
     * The frame or dialog that will own the popup window.
     */
    private final Window dialogOwner;

    public EditDimensionAction(ArchitectSwingSession session, Dimension dimension, PlayPen pp) {
        super(
                session,
                pp,
                "Dimension Properties...",
                "Edit the properties of "+dimension.getName()+" in a dialog",
                (String) null);
        this.dialogOwner = SwingUtilities.getWindowAncestor(pp);
        this.dimension = dimension;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            DataEntryPanel panel = new DimensionEditPanel(dimension);
            JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                    panel, dialogOwner, "Dimension Properties", DataEntryPanelBuilder.OK_BUTTON_LABEL);
            dialog.setLocationRelativeTo(getSession().getArchitectFrame());
            dialog.setVisible(true);
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }

}
