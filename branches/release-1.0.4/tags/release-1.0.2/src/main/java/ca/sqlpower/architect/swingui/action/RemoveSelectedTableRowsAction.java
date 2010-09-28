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
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

/**
 * This {@link Action} removes selected rows from a {@link JTable}. This class
 * is abstract because different {@link TableModel}s may have different
 * implementations of removing rows.
 */
public abstract class RemoveSelectedTableRowsAction extends AbstractAction {

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
            RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
            int[] selectedRows = table.getSelectedRows();
            
            if (rowSorter == null) {
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    removeRow(selectedRows[i]);
                }
            } else {
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    removeRow(rowSorter.convertRowIndexToModel(selectedRows[i]));
                }
            }
        }
    }

    /**
     * Removes a row in a {@link TableModel}.
     * 
     * @param row
     *            The index of the row to remove.
     */
    public abstract void removeRow(int row);

}
