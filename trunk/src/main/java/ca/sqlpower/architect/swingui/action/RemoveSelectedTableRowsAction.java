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
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * This {@link Action} removes selected rows from a {@link JTable}. The
 * assumption made here is that the table uses a {@link DefaultTableModel}.
 */
public class RemoveSelectedTableRowsAction extends AbstractAction {

    /**
     * The {@link JTable} to remove the selected rows from.
     */
    private final JTable table;

    /**
     * Creates a new {@link RemoveSelectedTableRowsAction}.
     * 
     * @param table
     *            The {@link JTable} to remove the selected rows from.
     */
    public RemoveSelectedTableRowsAction(JTable table) {
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        if (table.getSelectedRowCount() > 0) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            
            for (int i = table.getSelectedRowCount()-1; i >= 0; i--) {
                int index = table.getSelectedRows()[i];
                model.removeRow(index);
            }
        }
    }

}
