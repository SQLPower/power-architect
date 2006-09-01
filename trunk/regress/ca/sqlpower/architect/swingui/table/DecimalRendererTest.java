package ca.sqlpower.architect.swingui.table;

import java.math.BigDecimal;

import javax.swing.JLabel;

public class DecimalRendererTest extends BaseRendererTest {

    public void test1() {

        DecimalTableCellRenderer fmt = new DecimalTableCellRenderer();
        JLabel renderer = (JLabel) fmt.getTableCellRendererComponent(table, 1.2345, false, false, 0, 0);
        String renderedValue = renderer.getText();
        assertEquals("renderer formatted OK", "1.2", renderedValue);

        renderer = (JLabel) fmt.getTableCellRendererComponent(table, new BigDecimal(111111111.11), false, false, 0, 0);
        renderedValue = renderer.getText();
        // This test is not Locale-specific because the format is hard-coded in DecimalRenderer
        assertEquals("renderer formatted OK", "111,111,111.1", renderedValue);

        renderer = (JLabel) fmt.getTableCellRendererComponent(table, null, false, false, 0, 0);
        renderedValue = renderer.getText();
        assertEquals("renderer formatted OK", "null", renderedValue);
    }
}
