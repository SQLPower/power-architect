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

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;

/**
 * This action deletes OLAPObjects that are selected on the OLAPTree using removeChild methods. 
 */
public class OLAPDeleteSelectedAction extends AbstractArchitectAction {

    private final OLAPEditSession editSession;
    
    public OLAPDeleteSelectedAction(ArchitectSwingSession session, OLAPEditSession editSession) {
        super(session, editSession.getOlapPlayPen(), "Delete Selected", "Delete selected item.", null);
        this.editSession = editSession;
    }
    
    public void actionPerformed(ActionEvent arg0) {
        TreePath[] tps = editSession.getOlapTree().getSelectionPaths();
        if (tps.length > 1 ) {
            int decision = JOptionPane.showConfirmDialog(editSession.getPanel(), "Are you sure you want to delete the following " + tps.length + " items?",
                            "Multiple Delete", JOptionPane.YES_NO_OPTION);
            if (decision != JOptionPane.YES_OPTION ) {
                return;
            }
        } else if (tps.length < 1) {
            JOptionPane.showMessageDialog(playpen, "No items to delete!");
            return;
        }   
        try {
            playpen.startCompoundEdit("OLAP Delete");
            for (TreePath tp : tps) {
                OLAPObject obj = (OLAPObject) tp.getLastPathComponent();
                obj.getParent().removeChild(obj);
            }
        } finally {
            playpen.endCompoundEdit("OLAP Delete End");
        }
        
    }
}
