/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import ca.sqlpower.architect.swingui.CheckConstraintTable;
import ca.sqlpower.sqlobject.SQLCheckConstraint;

/**
 * This {@link Action} adds a new row to a {@link CheckConstraintTable} after
 * prompting the user to enter in a {@link SQLCheckConstraint} name and
 * constraint condition, and selects the row in the table. If either the name or
 * constraint condition exists, no rows are added and the row with the duplicate
 * entry is selected.
 */
public class AddCheckConstraintTableRowAction extends AbstractAction {

    /**
     * The {@link CheckConstraintTable} to add the row to.
     */
    private final CheckConstraintTable table;

    /**
     * Creates a new {@link AddCheckConstraintTableRowAction}.
     * 
     * @param table
     *            The {@link CheckConstraintTable} this action should perform
     *            on.
     */
    public AddCheckConstraintTableRowAction(CheckConstraintTable table) {
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        DefaultTableModel model = 
            (DefaultTableModel) table.getModel();
        
        String name = JOptionPane.showInputDialog("Define a new check constraint name.");
        int index;
        for (index = 0; index < model.getRowCount(); index++) {
            if (model.getValueAt(index, 0).equals(name)) {
                table.setRowSelectionInterval(index, index);
                break;
            }
        }
        
        if (index == model.getRowCount() && name != null && !name.equals("")) {
            String constraint = JOptionPane.showInputDialog("Define the check constraint condition.");
            for (index = 0; index < model.getRowCount(); index++) {
                if (model.getValueAt(index, 1).equals(constraint)) {
                    table.setRowSelectionInterval(index, index);
                    break;
                }
            }
            
            if (constraint != null && !constraint.equals("")) {
                if (index == model.getRowCount()) {
                    String[] row = {name, constraint};
                    model.addRow(row);
                }
                
                table.setRowSelectionInterval(index, index);
                
            }
        }
    }

}
