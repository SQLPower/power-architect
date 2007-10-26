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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class FreqValueTable extends JTable {


    public FreqValueTable(TableModel dm) {
        super(dm);
        
        TableColumnModel cm = getColumnModel();
        for (int col = 0; col < cm.getColumnCount(); col++) {
            TableColumn tc = cm.getColumn(col);
            switch(col) {
            case 0:
                tc.setCellRenderer(null);
                break;
            case 1:
                tc.setCellRenderer(new ValueTableCellRenderer());
                break;
            default:
                tc.setCellRenderer(null);
                break;
            }
        }
    }
    
    public void initColumnSizes() {
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer =
            getTableHeader().getDefaultRenderer();

        for (int i = 0; i < getColumnCount(); i++) {

            column = getColumnModel().getColumn(i);
            cellWidth = 0;
            comp = headerRenderer.getTableCellRendererComponent(
                                 this, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            for (int j = 0; j < getRowCount(); j++) {
                comp = getCellRenderer(j,i).getTableCellRendererComponent(this,
                        getModel().getValueAt(j,i),false,false,j,i);

                cellWidth = Math.max(cellWidth, comp.getPreferredSize().width);
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }


}
