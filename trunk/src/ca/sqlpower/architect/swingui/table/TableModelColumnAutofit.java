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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
/*
 * This class is used to manipulate the sizing on columns within profiling. 
 * By ctrl + selecting the header, the column will automatically resize to fit
 * all the elements under it.  However at the moment, it will not expand fully
 * to its desired size due to the fact that the maximum amount of space a column
 * has is bounded by the other columns as well.  This problem could be fixed
 * if profile table has a horizontal scroll pane as well.
 */
public class TableModelColumnAutofit extends AbstractTableModel{

    private TableModel tableModel;
    private MouseListener mouseListener;
    private JTable table;
    private JTableHeader tableHeader;

    public TableModelColumnAutofit(TableModel tableModel, JTable table){
        this.tableModel = tableModel;
        this.table = table;
        tableHeader = table.getTableHeader();
        mouseListener = new MouseListener();        
    }
    
    public int getRowCount() {
        if (tableModel == null) return 0;
        else return tableModel.getRowCount();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (tableModel == null) return null;
        return tableModel.getColumnClass(columnIndex);
    }
    
    public int getColumnCount() {
        if (tableModel == null) return 0;
        else return tableModel.getColumnCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (tableModel == null) return null;
        else return tableModel.getValueAt(rowIndex, columnIndex);
    }
    @Override
    public String getColumnName(int column) {
        if (tableModel == null) return null;
        return tableModel.getColumnName(column);
    }

    public JTableHeader getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader) {
        this.tableHeader.removeMouseListener(mouseListener);
        this.tableHeader = tableHeader;
        this.tableHeader.addMouseListener(mouseListener);
    }
  
    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    public void initColumnSizes() {
        for (int i = 0; i < getColumnCount(); i++) {
            initSingleColumnSize(i);
        }
    }

    public void initSingleColumnSize(int colIndex) {
        TableUtils.fitColumnWidth(table, colIndex, 0);
    }
    
    private class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            //XXX: Should change to a better condition for size editting
            //     for now, it's just ctrl click on the header
            if (e.isControlDown()){                                                    
                initSingleColumnSize(viewColumn);
            }            
        }
    }
    
}
