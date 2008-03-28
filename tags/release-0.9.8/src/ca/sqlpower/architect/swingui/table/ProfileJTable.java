/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui.table;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileColumn;
import ca.sqlpower.swingui.table.DateTableCellRenderer;
import ca.sqlpower.swingui.table.DecimalTableCellRenderer;
import ca.sqlpower.swingui.table.PercentTableCellRenderer;
import ca.sqlpower.swingui.table.TableModelSearchDecorator;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.swingui.table.TableTextConverter;

/**
 * Override JTable methods that control cell formatting, because we want
 * to always use particular format subclasses (from our swingui.table package)
 * to format particular columns.
 */
public class ProfileJTable extends JTable implements TableTextConverter {

    public ProfileJTable(TableModel model) {
        super(model);
        
        TableColumnModel cm = getColumnModel();
        for (int col = 0; col < cm.getColumnCount(); col++) {
            TableColumn tc = cm.getColumn(col);
            switch(ProfileColumn.values()[col]) {
            case DATABASE: case SCHEMA: case CATALOG: case TABLE: case COLUMN:
                tc.setCellRenderer(new SQLObjectTableCellRenderer());
                break;
            case RUNDATE:
                tc.setCellRenderer(new DateTableCellRenderer());
                break;
            case PERCENT_UNIQUE:
            case PERCENT_NULL:
                tc.setCellRenderer(new PercentTableCellRenderer());
                break;
            case AVERAGE_LENGTH:
                tc.setCellRenderer(new DecimalTableCellRenderer());
                break;
            case MIN_VALUE:
            case MAX_VALUE:
            case AVERAGE_VALUE:
            case TOP_VALUE:
                tc.setCellRenderer(new ValueTableCellRenderer());
                break;
            default:
                tc.setCellRenderer(null);
                break;
            }
        }
    }


    /**
     * Returns the ColumnProfileResult object that is associated with the
     * given row number.
     * 
     * @param row The visible row number as displayed in the table
     * @return The ColumnProfileResult object for that row
     */
    public ColumnProfileResult getColumnProfileResultForRow(int row) {
        return (ColumnProfileResult) 
                (getModel().getValueAt(
                        row, ProfileTableModel.CPR_PSEUDO_COLUMN_INDEX));
    }
    
    public TableModel getDataTableModel() {
        TableModelSortDecorator m1 = (TableModelSortDecorator) getModel();
        TableModelSearchDecorator m2 = (TableModelSearchDecorator) m1.getTableModel();
        ProfileTableModel m3 = (ProfileTableModel) m2.getTableModel();
        return m3;
    }
    
    public int modelIndex(int viewIndex) {
        TableModelSortDecorator m1 = (TableModelSortDecorator) getModel();        
        return m1.modelIndex(viewIndex);
    }
      
    public String getTextForCell(int row, int col) {
        // note: this will only work because we know all the renderers are jlabels
        JLabel renderer = (JLabel) getCellRenderer(row, col).getTableCellRendererComponent(this, getModel().getValueAt(row, getColumnModel().getColumn(col).getModelIndex()), false, false, row, col);
        return renderer.getText();
    }


}
