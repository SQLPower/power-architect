package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DateRendererFactory extends DefaultTableCellRenderer implements FormatFactory {

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        String formattedValue = df.format(new Date((Long) value));
        return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.table.FormatFactory#getFormat()
     */
    public Format getFormat() {
        return df;
    }
}
