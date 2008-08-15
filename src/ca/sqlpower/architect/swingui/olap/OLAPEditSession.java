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

package ca.sqlpower.architect.swingui.olap;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.action.CreateCubeAction;
import ca.sqlpower.architect.swingui.olap.action.CreateDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.CreateMeasureAction;
import ca.sqlpower.architect.swingui.olap.action.CreateVirtualCubeAction;
import ca.sqlpower.architect.swingui.olap.action.ExportSchemaAction;
import ca.sqlpower.architect.swingui.olap.action.OLAPDeleteSelectedAction;

public class OLAPEditSession implements OLAPChildListener {

    private final OLAPTree tree;
    
    /**
     * This is the playpen used within OLAP schema editor.
     */
    private final PlayPen pp;
    private final JDialog d;
    
    /**
     * OLAPSession associated with the schema that this edit session edits.
     */
    private final OLAPSession olapSession;
    
    private final CreateDimensionAction createDimensionAction;
    private final CreateCubeAction createCubeAction;
    private final CreateVirtualCubeAction createVirtualCubeAction;
    private final CreateMeasureAction createMeasureAction;
    private final ExportSchemaAction exportSchemaAction;
    private final OLAPDeleteSelectedAction olapDeleteSelectedAction;
     
    private final ArchitectSwingSession swingSession;
    
    /**
     * Creates a new editor for the given OLAP schema. The schema's OLAPObjects should
     * all belong to the given session's dbtree and playpen. This should only be called by the
     * ArchitectSwingSession, see {@link ArchitectSwingSession#getOLAPEditSession(OLAPSession)}.
     * 
     * @param session The architect swing session this editor and the given schema belong to.
     * @param olapSession The OLAPSession of the schema to edit
     */
    public OLAPEditSession(ArchitectSwingSession swingSession, OLAPSession olapSession) {
        this.olapSession = olapSession;
        this.swingSession = swingSession;

        // listen for when to remove this from the architect session.
        swingSession.getOLAPRootObject().addChildListener(this);
        
        // add to the architect session's list of edit sessions.
        swingSession.getOLAPEditSessions().add(this);

        Schema schema = olapSession.getSchema();
        tree = new OLAPTree(swingSession, this, schema);
        tree.setCellRenderer(new OLAPTreeCellRenderer());
        pp = OLAPPlayPenFactory.createPlayPen(swingSession, this);
        
        createDimensionAction = new CreateDimensionAction(swingSession, schema, pp);
        createCubeAction = new CreateCubeAction(swingSession, schema, pp);
        createVirtualCubeAction = new CreateVirtualCubeAction(swingSession, schema, pp);
        createMeasureAction = new CreateMeasureAction(swingSession, pp);
        exportSchemaAction = new ExportSchemaAction(swingSession, schema);
        olapDeleteSelectedAction = new OLAPDeleteSelectedAction(swingSession, this);
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.add(createDimensionAction);
        toolbar.add(createCubeAction);
        toolbar.add(createVirtualCubeAction);
        toolbar.add(createMeasureAction);
        toolbar.add(exportSchemaAction);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        new JScrollPane(tree),
                        new JScrollPane(pp)),
                BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.EAST);
        
        d = new JDialog(swingSession.getArchitectFrame(), generateDialogTitle());
        schema.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("name")) {
                    d.setTitle(generateDialogTitle());
                }
            }
        });
        d.setContentPane(panel);
        d.pack();
        
        OLAPPlayPenFactory.setupOLAPKeyboardActions(pp, this);
    }
    
    /**
     * Returns the OLAP Schema Editor dialog. It does not come with location and
     * visibility set.
     */
    public JDialog getDialog() {
        return d;
    }
    
    /**
     * Returns the playpen used within OLAP Schema Editor
     */
    public PlayPen getOlapPlayPen() {
        return pp;
    }
    
    public OLAPTree getOlapTree() {
        return tree;
    }
    
    /**
     * Returns the schema edit dialog's title that includes the schema's name.
     */
    private String generateDialogTitle() {
        return olapSession.getSchema().getName() + " - OLAP Schema Editor";
    }

    /**
     * Returns the OLAPSession this this edit session edits.
     */
    public OLAPSession getOlapSession() {
        return olapSession;
    }

    // ------ Action getter methods ------ //
    
    public CreateDimensionAction getCreateDimensionAction() {
        return createDimensionAction;
    }

    public CreateCubeAction getCreateCubeAction() {
        return createCubeAction;
    }

    public CreateVirtualCubeAction getCreateVirtualCubeAction() {
        return createVirtualCubeAction;
    }

    public CreateMeasureAction getCreateMeasureAction() {
        return createMeasureAction;
    }

    public ExportSchemaAction getExportSchemaAction() {
        return exportSchemaAction;
    }
    
    public OLAPDeleteSelectedAction getOLAPDeleteSelectedAction() {
        return olapDeleteSelectedAction;
    }
    
    // ------ OLAPChildListener methods ------ //

    public void olapChildAdded(OLAPChildEvent e) {
        // do nothing
    }

    public void olapChildRemoved(OLAPChildEvent e) {
        if (e.getChild() == olapSession) {
            // remove from architect's list of edit sessions and stop listening
            swingSession.getOLAPEditSessions().remove(this);
            swingSession.getOLAPRootObject().removeChildListener(this);
            d.dispose();
        }
    }
}
