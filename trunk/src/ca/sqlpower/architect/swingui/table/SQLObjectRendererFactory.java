package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectRendererFactory extends DefaultTableCellRenderer implements FormatFactory {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        String formattedValue = null;

        if (value == null) {
            formattedValue = "null";
        } else if (!(value instanceof SQLObject)) {
            throw new IllegalArgumentException("Value must be a SQLObject");
        } else {
            formattedValue = ((SQLObject)value).getName();
        }
        return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }

    public Format fakeFormatter = new Format() {

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            SQLObject o = (SQLObject)obj;
            return toAppendTo.append(o.getName());
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
