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
import javax.swing.JTree;

import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.action.CreateDimensionAction;

public class OLAPSchemaEditorPanel {

    private final ArchitectSwingSession session;
    private final JTree tree;
    private final PlayPen pp;
    private final JPanel panel;
    
    /**
     * Creates a new editor for the given OLAP schema. The schema's SQLObjects should
     * all belong to the given session's dbtree and playpen.
     * 
     * @param session The session this editor and the given schema belong to
     * @param schema The schema to edit
     */
    public OLAPSchemaEditorPanel(ArchitectSwingSession session, Schema schema) {
        this.session = session;
        tree = new JTree(new OLAPTreeModel(schema));
        tree.setCellRenderer(new OLAPTreeCellRenderer());
        pp = new PlayPen(session);
        
        JToolBar toolbar = new JToolBar();
        toolbar.add(new CreateDimensionAction(session, schema));
        
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
}
