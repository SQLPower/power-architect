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

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class FocusToChildOrParentTableAction extends AbstractArchitectAction implements SelectionListener{
    
    private boolean isToFocusParentTable;

    public FocusToChildOrParentTableAction(ArchitectSwingSession session, String actionName, String actionDescription, boolean isToFocusParentTable) {
        super(session, actionName, actionDescription);
        this.isToFocusParentTable = isToFocusParentTable;
    }

    public void actionPerformed(ActionEvent e) {
        List<Relationship> selection = playpen.getSelectedRelationShips();
        if (selection.size() < 1) {
            JOptionPane.showMessageDialog(playpen, "Select exactly 1 table to get to focus on its parent or child table.");
        } else if (selection.size() == 1) {
            if(isToFocusParentTable) {
                TablePane parentTable = selection.get(0).getPkTable();
                playpen.selectNone();
                parentTable.setSelected(true, SelectionEvent.SINGLE_SELECT);
                playpen.showSelected();
            }
            else {
                TablePane childTable = selection.get(0).getFkTable();
                playpen.selectNone();
                childTable.setSelected(true, SelectionEvent.SINGLE_SELECT);
                playpen.showSelected();
            }
        } else {
            JOptionPane.showMessageDialog(playpen, "Select exactly 1 table to get to focus on its parent or child table.");
        }
    }
    
    public void itemDeselected(SelectionEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void itemSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
        
    }
}
