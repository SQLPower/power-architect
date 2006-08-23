package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DateRendererFactory extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String formattedValue = df.format(new Date((Long) value));
        return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }
}
