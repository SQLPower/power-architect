package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PercentRendererFactory extends DefaultTableCellRenderer {

    static DecimalFormat pctFormat = new DecimalFormat("0%");

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
}
