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

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;

public class FocusToChildOrParentTableAction extends AbstractArchitectAction{
    
    private boolean isToFocusParentTable;

    public FocusToChildOrParentTableAction(ArchitectFrame frame, String actionName, String actionDescription, boolean isToFocusParentTable) {
        super(frame, actionName, actionDescription);
        this.isToFocusParentTable = isToFocusParentTable;
    }

    public void actionPerformed(ActionEvent e) {
        List<Relationship> selection = getPlaypen().getSelectedRelationShips();
        if (selection.size() == 1) {
            TablePane focusingTable;
            if(isToFocusParentTable) {
                focusingTable = selection.get(0).getPkTable();
            } else {
                focusingTable = selection.get(0).getFkTable();
            }
            getPlaypen().selectNone();
            focusingTable.setSelected(true, SelectionEvent.SINGLE_SELECT);
            getPlaypen().showSelected();
        } else {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("FocusToChildOrParentTableAction.selectExactlyOneRelationship")); //$NON-NLS-1$
        }
    }
}
