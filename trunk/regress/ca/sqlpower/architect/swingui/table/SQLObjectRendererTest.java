package ca.sqlpower.architect.swingui.table;

import javax.swing.JLabel;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;

public class SQLObjectRendererTest extends BaseRendererTest {

    /* Test rendering of SQLObjects  */
    public void test5() {
        SQLDatabase db = new SQLDatabase();
        db.setDataSource(new ArchitectDataSource());
        db.setName("MyName");

        // Test with SQLDatabase
        SQLObjectRendererFactory sqlRenderer = new SQLObjectRendererFactory();
        JLabel renderer = (JLabel) sqlRenderer.getTableCellRendererComponent(table, db, false, false, 0, 0);
        assertEquals("renderer formatted OK", "MyName", renderer.getText());

        // Test with SQLColumn
        SQLCatalog cat = new SQLCatalog();
        cat.setName("MyName2");
        renderer = (JLabel) sqlRenderer.getTableCellRendererComponent(table, cat, false, false, 0, 0);
        assertEquals("renderer formatted OK", "MyName2", renderer.getText());

        // Test for null
        renderer = (JLabel) sqlRenderer.getTableCellRendererComponent(table, null, false, false, 0, 0);
        assertEquals("renderer formatted OK", "null", renderer.getText());

    }
}
