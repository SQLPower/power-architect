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

import javax.swing.JDialog;

import ca.sqlpower.architect.olap.MondrianDef.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.OLAPSchemaEditorPanel;

/**
 * Action that pops up a dialog with an OLAP cube editor in it.
 * Soon, this action will support either editing an existing
 * schema or creating a new one.
 */
public class OLAPEditAction extends AbstractArchitectAction {

    public OLAPEditAction(ArchitectSwingSession session) {
        super(session, "New OLAP Schema...", "Creates a new OLAP schema");
    }

    public void actionPerformed(ActionEvent e) {
        OLAPSchemaEditorPanel panel = new OLAPSchemaEditorPanel(session, new Schema());
        
        // TODO register listener on schema object and make dialog title track schema name
        JDialog d = new JDialog(session.getArchitectFrame(), "OLAP Schema Editor");
        d.setContentPane(panel.getPanel());
        d.pack();
        d.setLocationRelativeTo(session.getArchitectFrame());
        d.setVisible(true);
    }

    
}
