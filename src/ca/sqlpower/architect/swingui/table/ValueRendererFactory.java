package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ValueRendererFactory extends DefaultTableCellRenderer {

    static DecimalFormat aldf;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        if (aldf == null) {
            aldf = new DecimalFormat("#,##0.0");
            aldf.setMaximumFractionDigits(1);
            aldf.setMinimumFractionDigits(0);
        }

        String formattedValue;

        if (value == null) {
            formattedValue = "";
        } else if (value instanceof Number) {
            formattedValue = aldf.format(value);
        } else {
            formattedValue = value.toString();
        }
         return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }
}
