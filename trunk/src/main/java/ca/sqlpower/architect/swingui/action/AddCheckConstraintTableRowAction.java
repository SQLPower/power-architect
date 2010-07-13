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

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.CheckConstraintTable;
import ca.sqlpower.sqlobject.SQLCheckConstraint;

/**
 * This {@link Action} adds a new row to a {@link CheckConstraintTable} after
 * prompting the user to enter in a {@link SQLCheckConstraint} name and
 * constraint condition, and selects the row in the table. If name already
 * exists, no rows are added and the duplicate row is selected.
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
        if (name == null || name.trim().equals("")) {
            return;
        }
        
        int index = ASUtils.findFirstRow(model, 0, name);
        if (index != -1) {
            table.setRowSelectionInterval(index, index);
            return;
        }
        
        String constraint = JOptionPane.showInputDialog("Define the check constraint condition.");
        if (constraint == null || constraint.trim().equals("")) {
            return;
        }
        
        String[] row = {name, constraint};
        model.addRow(row);
        table.setRowSelectionInterval(model.getRowCount() - 1, model.getRowCount() - 1);

    }

}
