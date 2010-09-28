/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
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
