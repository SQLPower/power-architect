package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PercentTableCellRenderer extends DefaultTableCellRenderer implements FormatFactory {

    DecimalFormat pctFormat = new DecimalFormat("0%");

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        String formattedValue;
        if (value == null) {
            formattedValue = "N/A";
        } else if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Value must be a Number object");
        } else {
            formattedValue = pctFormat.format(value);
        }
        return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }
    
    public Format fakeFormatter = new Format() {

        @Override
        public StringBuffer format(Object value, StringBuffer toAppendTo, FieldPosition pos) {
            if (value instanceof Number) {
                return toAppendTo.append(pctFormat.format(value));
            } else {
                throw new IllegalArgumentException("Value must be a Number object");
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
