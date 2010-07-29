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
 * This {@link Action} adds a new row to a {@link JTable}'s {@link TableModel}
 * and selects the newly added row. This class is abstract because different
 * {@link TableModel}s may have different implementations of adding rows.
 */
public abstract class AddAndSelectTableRowAction extends AbstractAction {

    /**
     * The {@link JTable} to add and select the row on when this {@link Action}
     * is performed.
     */
    private final JTable table;

    /**
     * Creates a new {@link AddAndSelectTableRowAction}.
     * 
     * @param table
     *            The JTable to add and select the row when this {@link Action}
     *            is performed.
     */
    public AddAndSelectTableRowAction(JTable table) {
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        table.getSelectionModel().clearSelection();
        addRow();
        
        RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
        int index = table.getRowCount() - 1;
        if (rowSorter != null) {
            index = rowSorter.convertRowIndexToView(index);
        }
        
        table.getSelectionModel().setSelectionInterval(index, index);
    }

    /**
     * Adds a row in the {@link TableModel}. This method must add the row at the
     * end of the {@link TableModel}.
     */
    public abstract void addRow();

}
