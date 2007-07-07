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

/**
 * The TableUtils class contains simple static utility methods that are
 * useful when working with JTable.
 */
public class TableUtils {

    /**
     * Sets the given column of the given table to be exactly as
     * wide as it needs to be to fit its current contents.  The contents
     * of each body cell in this column, as well as the header component
     * is taken into account.
     * 
     * @param table The table whose column to resize
     * @param colIndex The index of the column to resize 
     */
    public static void fitColumnWidth(JTable table, int colIndex, int padding) {
        fitColumnWidth(table, colIndex, -1, padding);
    }
    
    /**
     * Sets the given column of the given table to be exactly as
     * wide as it needs to be to fit its current contents, but not exceeding
     * a specified maximum.  The contents
     * of each body cell in this column, as well as the header component
     * is taken into account.
     * 
     * @param table The table whose column to resize
     * @param colIndex The index of the column to resize 
     * @param maxWidth The maximum width, in pixels, that the column is allowed
     * to have.  Nonpositive values mean "no maximum."
     */
    public static void fitColumnWidth(JTable table, int colIndex, int maxWidth, int padding) {
        TableColumn column = null;
        Component comp = null;
        int cellWidth = 0;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();
        column = table.getColumnModel().getColumn(colIndex);

        comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(),
                false, false, 0, 0);

        // Headers need additional padding for some reason!
        cellWidth = comp.getPreferredSize().width + 2;

        for (int j = 0; j < table.getRowCount(); j++) {                
            comp = table.getCellRenderer(j,colIndex).getTableCellRendererComponent(table,
                    table.getValueAt(j, colIndex),false,false,j, colIndex);  

            // we add a one-pixel fudge factor here because the result is often too short by a pixel
            cellWidth = Math.max(cellWidth, comp.getPreferredSize().width + 1);

            if (maxWidth > 0 && cellWidth >= maxWidth) {
                cellWidth = maxWidth;
                break;
            }
        }
        column.setPreferredWidth(cellWidth + padding);
    }

    /**
     * Makes each column of the given table exactly the right size it needs but
     * not greater than the maxColumnWidth provided.  To have no restraints on the 
     * maximum column size, pass in a negative number for maxColumnWidth.  Note that
     * this method turns the table auto resize off.
     * 
     * @param table the table that will have its columns adjust to appropiate size
     * @param maxColumnWidth specifies the maximum width of the column, if no
     * maximum size are needed to be specified, pass in a negative number
     * @param padding the number of pixels of extra space to leave (the actual column
     * width will be the maximum width of any value in the column, plus this padding amount)
     */
    public static void fitColumnWidths(JTable table, int maxColumnWidth, int padding) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int colIndex = 0; colIndex < table.getColumnCount(); colIndex++) {
            fitColumnWidth(table, colIndex, maxColumnWidth, padding);
        }
    }
    
    /**
     * Makes each column of the given table exactly the right size with no 
     * constraints on how big the column width would go
     * 
     * @param table the table that will have its columns adjust to appropiate size
     * @param padding the number of pixels of extra space to leave (the actual column
     * width will be the maximum width of any value in the column, plus this padding amount)
     */
    public static void fitColumnWidths(JTable table, int padding) {
        fitColumnWidths(table, -1, padding);
    }

}
