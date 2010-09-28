package ca.sqlpower.architect.swingui.table;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JLabel;

public class DateRendererTest extends BaseRendererTest {

    /* Test Column 5, create date */
    public void test5() {
        Calendar c = new GregorianCalendar(1999, 12-1, 31);
        Date d = c.getTime();
        long time = d.getTime();

        DateTableCellRenderer dateRenderer = new DateTableCellRenderer();
        JLabel renderer = (JLabel) dateRenderer.getTableCellRendererComponent(table, time, false, false, 0, 0);
        String renderedValue = renderer.getText();
        assertEquals("renderer formatted OK", "1999-12-31 12:00:00", renderedValue);

        // XXX we don't check for null here because that "can't happen"
        // checkForNull(column);
    }
}
