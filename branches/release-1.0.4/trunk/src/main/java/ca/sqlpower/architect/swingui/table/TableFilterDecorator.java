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

package ca.sqlpower.architect.swingui.table;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.swingui.table.TableModelWrapper;

public class TableFilterDecorator extends AbstractTableModel implements TableModelWrapper {

    private TableModel wrappedModel;
    private TableProfileResult filter;
    
    private TableModelListener handler = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            fireTableChanged(e);
        }
    };
    
    public TableFilterDecorator(TableModel wrappedModel) {
        this.wrappedModel = wrappedModel;
        wrappedModel.addTableModelListener(handler);
    }
    
    @Override
    public TableModel getWrappedModel() {
        return wrappedModel;
    }

    @Override
    public void setWrappedModel(TableModel model) {
        this.wrappedModel.removeTableModelListener(handler);
        this.wrappedModel = model;
        model.addTableModelListener(handler);
    }
    
    public void setFilter(TableProfileResult filter) {
        this.filter = filter;
        fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public int getColumnCount() {
        return wrappedModel.getColumnCount();
    }

    @Override
    public int getRowCount() {
        if (filter == null) {
            return wrappedModel.getRowCount();
        } else {
            int count = 0;
            for (int i = 0; i < wrappedModel.getRowCount(); i++) {
                ColumnProfileResult cpr = (ColumnProfileResult) wrappedModel.getValueAt(i, ProfileTableModel.CPR_PSEUDO_COLUMN_INDEX);
                if (filter.equals(cpr.getParent())) {
                    count++;
                }
            }
            return count;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (filter == null) {
            return wrappedModel.getValueAt(rowIndex, columnIndex);
        } else {
            int count = 0;
            for (int i = -1; i < rowIndex; count++) {
                ColumnProfileResult cpr = (ColumnProfileResult) wrappedModel.getValueAt(count, ProfileTableModel.CPR_PSEUDO_COLUMN_INDEX);
                if (filter.equals(cpr.getParent())) {
                    i++;
                }
            }
            return wrappedModel.getValueAt(count-1, columnIndex);
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return wrappedModel.isCellEditable(rowIndex, columnIndex);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        wrappedModel.setValueAt(aValue, rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return wrappedModel.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return wrappedModel.getColumnClass(columnIndex);
    }

}
