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

import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.SchemaEditPanel;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditSchemaAction extends AbstractArchitectAction{

    /**
     * The schema this action edits.
     */
    private final Schema schema;
    
    /**
     * Contains the databases that the schema can belong to. 
     */
    private final ArchitectSwingSession session;
    
    /**
     * The frame or dialog that will own the popup window.
     */
    private final Window dialogOwner;

    public EditSchemaAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, pp, "Schema Properties...", "Edit the properties of "+schema.getName()+" in a dialog", (String) null);
        this.dialogOwner = SwingUtilities.getWindowAncestor(pp);
        this.schema = schema;
        this.session = session;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            DataEntryPanel panel = new SchemaEditPanel(session, schema);
            JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(panel, dialogOwner, "Schema Properties", DataEntryPanelBuilder.OK_BUTTON_LABEL);
            dialog.setLocationRelativeTo(session.getArchitectFrame());
            dialog.setVisible(true);
        } catch (SQLObjectException ex) {
            ASUtils.showExceptionDialogNoReport(
                    dialogOwner,
                    "Failed to get list of databases.",
                    ex);
        }
    }
}
