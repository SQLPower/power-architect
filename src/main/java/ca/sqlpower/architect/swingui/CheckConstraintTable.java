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

package ca.sqlpower.architect.swingui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import ca.sqlpower.sqlobject.SQLCheckConstraint;
import ca.sqlpower.sqlobject.SQLCheckConstraintContainer;

/**
 * This {@link JTable} displays {@link SQLCheckConstraint}s that are contained
 * within a {@link SQLCheckConstraintContainer}.
 */
public class CheckConstraintTable extends JTable {
    
    public CheckConstraintTable() {
        super(new DefaultTableModel());
        setCellSelectionEnabled(true);
        getTableHeader().setReorderingAllowed(false);
        DefaultTableModel tableModel = (DefaultTableModel) getModel();
        tableModel.addColumn("Name");
        tableModel.addColumn("Check Constraint");
    }

    /**
     * Finds the first row of a {@link TableModel} that contains a specific
     * {@link SQLCheckConstraint} name.
     * 
     * @param model
     *            The {@link TableModel} to search through.
     * @param name
     *            The name of the {@link SQLCheckConstraint}.
     * @return The index of the matched row or -1 if not found.
     */
    public static int findFirstRow(TableModel model, String name) {
        String trimmedName = name.trim();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((String) model.getValueAt(i, 0)).trim().equals(trimmedName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the last row of a {@link TableModel} that contains a specific
     * {@link SQLCheckConstraint} name.
     * 
     * @param model
     *            The {@link TableModel} to search through.
     * @param name
     *            The name of the {@link SQLCheckConstraint}.
     * @return The index of the matched row or -1 if not found.
     */
    public static int findLastRow(TableModel model, String name) {
        String trimmedName = name.trim();
        for (int i = model.getRowCount()-1; i >= 0; i--) {
            if (((String) model.getValueAt(i, 0)).trim().equals(trimmedName)) {
                return i;
            }
        }
        return -1;
    }

}
