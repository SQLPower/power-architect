package ca.sqlpower.architect.swingui.table;

import javax.swing.JLabel;

public class PercentRendererTest extends BaseRendererTest {

    public void test1() {

        PercentTableCellRenderer fmt = new PercentTableCellRenderer();
        JLabel renderer = (JLabel) fmt.getTableCellRendererComponent(table, 1.2345, false, false, 0, 0);
        String renderedValue = renderer.getText();
        assertEquals("renderer formatted OK", "123%", renderedValue);

        renderer = (JLabel) fmt.getTableCellRendererComponent(table, 0.8777, false, false, 0, 0);
        renderedValue = renderer.getText();
        assertEquals("renderer formatted OK", "88%", renderedValue);

        renderer = (JLabel) fmt.getTableCellRendererComponent(table, null, false, false, 0, 0);
        renderedValue = renderer.getText();
        assertEquals("renderer formatted OK", "N/A", renderedValue);
    }
}
