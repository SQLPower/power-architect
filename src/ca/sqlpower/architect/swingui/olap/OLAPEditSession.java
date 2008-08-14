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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.action.CreateCubeAction;
import ca.sqlpower.architect.swingui.olap.action.CreateDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.CreateMeasureAction;
import ca.sqlpower.architect.swingui.olap.action.CreateVirtualCubeAction;
import ca.sqlpower.architect.swingui.olap.action.EditCubeAction;
import ca.sqlpower.architect.swingui.olap.action.EditDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.EditVirtualCubeAction;
import ca.sqlpower.architect.swingui.olap.action.ExportSchemaAction;

public class OLAPEditSession {

    private final OLAPTree tree;
    
    /**
     * This is the playpen used within OLAP schema editor.
     */
    private final PlayPen pp;
    private final JPanel panel;
    
    private final CreateDimensionAction createDimensionAction;
    private final CreateCubeAction createCubeAction;
    private final CreateVirtualCubeAction createVirtualCubeAction;
    private final CreateMeasureAction createMeasureAction;
    private final ExportSchemaAction exportSchemaAction;
    
    private EditCubeAction editCubeAction;
    private EditVirtualCubeAction editVirtualCubeAction;
    private EditDimensionAction editDimensionAction;
    
    /**
     * Creates a new editor for the given OLAP schema. The schema's OLAPObjects should
     * all belong to the given session's dbtree and playpen.
     * 
     * @param session The session this editor and the given schema belong to
     * @param schema The schema to edit
     */
    public OLAPEditSession(ArchitectSwingSession session, Schema schema) {
        tree = new OLAPTree(session, this, schema);
        tree.setCellRenderer(new OLAPTreeCellRenderer());
        pp = new PlayPen(session); // TODO create OLAPPlayPenFactory class to set this up properly
        
        createDimensionAction = new CreateDimensionAction(session, schema, pp);
        createCubeAction = new CreateCubeAction(session, schema, pp);
        createVirtualCubeAction = new CreateVirtualCubeAction(session, schema, pp);
        createMeasureAction = new CreateMeasureAction(session, pp);
        exportSchemaAction = new ExportSchemaAction(session, schema);
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.add(createDimensionAction);
        toolbar.add(createCubeAction);
        toolbar.add(createVirtualCubeAction);
        toolbar.add(createMeasureAction);
        toolbar.add(exportSchemaAction);
        
        panel = new JPanel(new BorderLayout());
        panel.add(
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        new JScrollPane(tree),
                        new JScrollPane(pp)),
                BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.EAST);
    }
    
    public JComponent getPanel() {
        return panel;
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
}
