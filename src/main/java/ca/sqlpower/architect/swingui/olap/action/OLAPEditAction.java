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
import javax.swing.JFrame;

import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.swingui.olap.SchemaEditPanel;
import ca.sqlpower.sqlobject.SQLObjectException;
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
    
    public OLAPEditAction(ArchitectFrame frame, OLAPSession olapSession) {
        super(frame, olapSession == null ? "New Schema..." : olapSession.getSchema().getName(), "Edit OLAP schema");
        this.olapSession = olapSession;
        newSchema = olapSession == null; 
    }

    public void actionPerformed(ActionEvent e) {                
        Schema schema;
        try {
            if (newSchema) {
                getSession().getWorkspace().begin("Opening OLAP schema");
                schema = new Schema();
                schema.setName("New OLAP Schema");
                getSession().getOLAPRootObject().addChild(olapSession = new OLAPSession(schema));
            } else {
                schema = olapSession.getSchema();
            }

            OLAPEditSession editSession = getSession().getOLAPEditSession(olapSession);

            final JFrame frame = editSession.getFrame();
            frame.setLocationRelativeTo(getSession().getArchitectFrame());
            frame.setVisible(true);     

            if (newSchema) {            
                final SchemaEditPanel schemaEditPanel = new SchemaEditPanel(getSession(), schema);

                Callable<Boolean> okCall = new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        try {
                            boolean ok = schemaEditPanel.applyChanges();                                
                            getSession().getWorkspace().commit();
                            return ok;
                        } catch (Throwable e) {
                            getSession().getWorkspace().rollback("Error applying changes: " + e.toString());
                            throw new RuntimeException(e);
                        }
                    }
                };

                Callable<Boolean> cancelCall = new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        frame.dispose();
                        getSession().getOLAPRootObject().removeOLAPSession(olapSession);
                        getSession().getWorkspace().rollback("New OLAP session cancelled");
                        return true;
                    }
                };

                JDialog schemaEditDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                        schemaEditPanel,
                        frame,
                        "New Schema Properties",
                        DataEntryPanelBuilder.OK_BUTTON_LABEL,
                        okCall,
                        cancelCall);
                schemaEditDialog.setLocationRelativeTo(frame);
                schemaEditDialog.setVisible(true);
            }
        } catch (SQLObjectException ex) {
            getSession().getWorkspace().rollback("Error opening OLAP schema: " + ex.toString());
            ASUtils.showExceptionDialogNoReport(
                    getSession().getArchitectFrame(),
                    "Failed to get list of databases.",
                    ex);
        } catch (Throwable ex) {
            getSession().getWorkspace().rollback("Error opening OLAP schema: " + ex.toString());
            throw new RuntimeException(ex);
        }
    }
}
