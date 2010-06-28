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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.swingui.table.DateTableCellRenderer;
import ca.sqlpower.swingui.table.PercentTableCellRenderer;

/**
 * This class is similar to the {@link FreqValueCountTableModel} except it
 * displays the information for multiple columns at the same time.
 */
public class MultiFreqValueCountTableModel extends AbstractTableModel {
    
    /**
     * In the future we may want this to be a different type of listener when the 
     * {@link ProfileTableModel} is not the container for the existing set of
     * profiles being viewed.
     */
    private final TableModelListener profileListListener = new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
            refresh();
        }
    };
    
    /**
     * Apparently the ProfileTableModel is the object that tracks what profiles
     * are currently being viewed by the profile viewer. This seems like an odd
     * place to store this information and the ProfileResultsViewer that
     * actually gets this information first may be more logical. More
     * investigation into the current state of how the profile UI is needed to
     * fully understand and update this set of classes however.
     */
    private final ProfileTableModel tm;

    /**
     * This is the list of currently selected table profile results to display
     * in this table model.
     */
    private final List<ColumnValueCount> cvcList = new ArrayList<ColumnValueCount>();

    /**
     * @param tm
     *            The table model that is used as a collection of all of the
     *            profiles currently being used.
     */
    public MultiFreqValueCountTableModel(ProfileTableModel tm) {
        this.tm = tm;
        tm.addTableModelListener(profileListListener);
        refresh();
    }
    
    public void refresh() {
        cvcList.clear();
        for (TableProfileResult tpr : tm.getTableResultsToScan()) {
            for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                cvcList.addAll(cpr.getValueCount());
            }
        }
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return 6;
    }

    public int getRowCount() {
        return cvcList.size();
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Table";
            case 1:
                return "Column";
            case 2:
                return "Value";
            case 3:
                return "Count";
            case 4:
                return "%";
            case 5:
                return "Creation Time";
            default:
                throw new IllegalArgumentException("Column " + column + " does not exist.");
        }
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        ColumnValueCount cvc = cvcList.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return cvc.getParent().getParent().getProfiledObject();
        case 1:
            return cvc.getParent().getProfiledObject();
        case 2:
            return cvc.getValue();
        case 3:
            return cvc.getCount();
        case 4:
            return cvc.getPercent();
        case 5:
            return cvc.getParent().getParent().getCreateStartTime();
        default:
            throw new IllegalArgumentException("Column at index " + columnIndex + " does not exist.");
        }
    }

    /**
     * Returns a {@link TableCellRenderer} that defines how cells of a column
     * should be rendered. The renderer returned may not be the same renderer
     * each time it is requested.
     * 
     * @param colIndex
     *            The index of the column whose cell renderer will be returned.
     *            Must be less than the column count and non-negative.
     */
    public TableCellRenderer getCellRenderer(int colIndex) {
        switch (colIndex) {
            case 0:
            case 1:
                return new SQLObjectTableCellRenderer();
            case 2:
            case 3:
                return new ValueTableCellRenderer();
            case 4:
                return new PercentTableCellRenderer();
            case 5:
                return new DateTableCellRenderer();
            default:
                throw new IllegalArgumentException("No cell renderer for column " + colIndex);
        }
    }
}
