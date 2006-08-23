package ca.sqlpower.architect.swingui.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectRendererFactory extends DefaultTableCellRenderer {

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
}
