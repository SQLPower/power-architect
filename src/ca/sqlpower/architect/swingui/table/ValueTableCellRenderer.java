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
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.architect.profile.ColumnValueCount;

public class ValueTableCellRenderer extends DefaultTableCellRenderer {

    DecimalFormat aldf;

    public ValueTableCellRenderer() {
        aldf = new DecimalFormat("#,##0.0");
        aldf.setMaximumFractionDigits(1);
        aldf.setMinimumFractionDigits(0);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        String formattedValue;

        if (value == null) {
            formattedValue = "";
        } else if (value instanceof Number) {
            formattedValue = aldf.format(value);
        } else if ( value instanceof List ) {
            if ( ((List) value).size() > 0 )
                formattedValue = String.valueOf(
                        ((ColumnValueCount)((List) value).get(0)).getValue());
            else
                formattedValue = "";


            StringBuffer toolTip = new StringBuffer();
            toolTip.append("<html><table>");
            for ( ColumnValueCount v : (List<ColumnValueCount>)value ) {
                toolTip.append("<tr>");
                toolTip.append("<td align=\"left\">");
                if ( v.getValue() == null ) {
                    toolTip.append("null");
                } else {
                    toolTip.append(v.getValue().toString());
                }
                toolTip.append("</td>");
                toolTip.append("<td>&nbsp;&nbsp;&nbsp;</td>");
                toolTip.append("<td align=\"right\"><b>[");
                toolTip.append(v.getCount());
                toolTip.append("]</td>");
                toolTip.append("</tr>");
            }
            toolTip.append("</table></html>");
            setToolTipText(toolTip.toString());
        } else {
            formattedValue = value.toString();
        }
         return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }

    public Format fakeFormatter = new Format() {

        @Override
        public StringBuffer format(Object value, StringBuffer toAppendTo, FieldPosition pos) {
            if (value instanceof Number) {
                return toAppendTo.append(aldf.format(value));
            } else {
                return toAppendTo.append(value.toString());
            }
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            throw new UnsupportedOperationException("This formatter cannot parse");
        }

    };

    public Format getFormat() {
        return fakeFormatter;
    }
}
