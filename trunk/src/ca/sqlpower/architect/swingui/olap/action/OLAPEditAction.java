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

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;

import javax.swing.JDialog;

import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.swingui.olap.SchemaEditPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * Action that pops up a dialog with an OLAP cube editor in it.
 * Soon, this action will support either editing an existing
 * schema or creating a new one.
 */
public class OLAPEditAction extends AbstractArchitectAction {

    private OLAPSession olapSession;
    private final boolean newSchema;
    
    public OLAPEditAction(ArchitectSwingSession session, OLAPSession olapSession) {
        super(session, olapSession == null ? "New Schema..." : olapSession.getSchema().getName(), "Edit OLAP schema");
        this.olapSession = olapSession;
        newSchema = olapSession == null; 
    }

    public void actionPerformed(ActionEvent e) {
        Schema schema;
        if (newSchema) {
            schema = new Schema();
            schema.setName("New OLAP Schema");
            session.getOLAPRootObject().addChild(olapSession = new OLAPSession(schema));
        } else {
            schema = olapSession.getSchema();
        }
        
        OLAPEditSession editSession = session.getOLAPEditSession(olapSession);
        
        final JDialog d = editSession.getDialog();
        d.setLocationRelativeTo(session.getArchitectFrame());
        d.setVisible(true);
        
        if (newSchema) {
            final SchemaEditPanel schemaEditPanel = new SchemaEditPanel(schema, session);

            Callable<Boolean> okCall = new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return schemaEditPanel.applyChanges();
                }
            };

            Callable<Boolean> cancelCall = new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    d.dispose();
                    session.getOLAPRootObject().removeOLAPSession(olapSession);
                    return true;
                }
            };

            JDialog schemaEditDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                    schemaEditPanel,
                    d,
                    "New Schema Properties",
                    "OK",
                    okCall,
                    cancelCall);
            schemaEditDialog.setLocationRelativeTo(d);
            schemaEditDialog.setVisible(true);
        }
    }
}
