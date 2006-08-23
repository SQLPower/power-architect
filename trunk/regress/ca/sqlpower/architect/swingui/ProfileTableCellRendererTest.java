package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JLabel;
import javax.swing.JTable;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

import junit.framework.TestCase;

public class ProfileTableCellRendererTest extends TestCase {

    JTable table = new JTable() {
        @Override
        public int convertColumnIndexToModel(int viewColumnIndex) {
            return viewColumnIndex;
        }
    };

    ProfileTableCellRenderer target;

    @Override
    protected void setUp() throws Exception {
        target = new ProfileTableCellRenderer();
    }

    /* Test Column 0, SQLDatabase */
    public void test0() {
        SQLDatabase db = new SQLDatabase();
        db.setDataSource(new ArchitectDataSource());
        db.setName("MyName");
        int column = 0;

        String renderedValue = renderValue(db, column);
        assertEquals("renderer formatted OK", "MyName", renderedValue);

        checkForNull(column);
    }

    /* Test Column 1, SQLCatalog */
    public void test1() {
        int column = 1;
        SQLCatalog cat = new SQLCatalog();
        cat.setName("MyName");

        String renderedValue = renderValue(cat, column);
        assertEquals("renderer formatted OK", "MyName", renderedValue);

        checkForNull(column);
    }

    /* Test Column 2, SQLSchema */
    public void test2() {
        int column = 2;
        SQLSchema scm = new SQLSchema(false);
        scm.setName("MyName");

        String renderedValue = renderValue(scm, column);
        assertEquals("renderer formatted OK", "MyName", renderedValue);

        checkForNull(column);
    }

    /* Test Column 3, SQLTable */
    public void test3() {
        int column = 3;
        SQLTable tab = new SQLTable();
        tab.setName("MyName");

        String renderedValue = renderValue(tab, column);
        assertEquals("renderer formatted OK", "MyName", renderedValue);

        checkForNull(column);
    }

    /* Test Column 4, SQLColumn */
    public void test4() {
        int column = 4;
        SQLColumn col = new SQLColumn();
        col.setName("MyName");

        String renderedValue = renderValue(col, column);
        assertEquals("renderer formatted OK", "MyName", renderedValue);

        checkForNull(column);
    }

    /* Test Column 5, create date */
    public void test5() {
        int column = 5;
        Calendar c = new GregorianCalendar(1999, 12-1, 31);
        Date d = c.getTime();

        String renderedValue = renderValue(d.getTime(), column);
        assertEquals("renderer formatted OK", "1999-12-31 12:00:00", renderedValue);

        // XXX we don't check for null here because that "can't happen"
        // checkForNull(column);
    }

    /* Test Generic Column (6, others not listed here), format value unchanged */
    public void test6() {
        int column = 6;

        String string = "Hello World";
        String renderedValue = renderValue(string, column);
        assertEquals("renderer formatted OK", string, renderedValue);

        checkForNull(column);
    }

    /* Test Column 9, percentage */
    public void test9() {
        int column = 9;

        BigDecimal val = new BigDecimal(1.2345);
        String renderedValue = renderValue(val, column);
        assertEquals("renderer formatted OK", "123%", renderedValue);

        // can't use checkForNull(column); since the Profiler returns N/A for %ages
        renderedValue = renderValue(null, column);
        assertEquals("renderer formatted OK", "N/A", renderedValue);
    }


    private void checkForNull(int column) {
        String renderedValue;
        renderedValue = renderValue(null, column);
        assertEquals("renderer formatted OK", "null", renderedValue);
    }

    /*
     * Test method for column 14, average length
     */
    public void test14() {
        int column = 14;
        Object value = new BigDecimal(123.45);
        String renderedValue = renderValue(value, column);
        assertEquals("renderer formatted OK", "123.5", renderedValue);

        checkForNull(column);
    }

    /*
     * Test method for column 17, average value
     */
    public void test17() {
        int column = 17;
        Object value = new BigDecimal(123.45);
        String renderedValue = renderValue(value, column);
        assertEquals("renderer formatted OK", "123.5", renderedValue);

        checkForNull(column);

        value = "Hello";
        renderedValue = renderValue(value, column);
        assertEquals("renderer formatted OK", value, renderedValue);
    }

    private String renderValue(Object value, int column) {
        Component retValue =
            target.getTableCellRendererComponent(table, value, false, false, 0, column);
        assertTrue("profile renderer returns JLabel", retValue instanceof JLabel);
        JLabel renderer = (JLabel) retValue;
        return renderer.getText();
    }

}
