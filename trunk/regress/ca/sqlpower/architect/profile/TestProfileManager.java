package ca.sqlpower.architect.profile;

import java.math.BigDecimal;
import java.util.Collection;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class TestProfileManager extends TestProfileBase {

    public void test1() throws Exception {
        SQLTable t = mydb.getTableByName("PROFILE_TEST1");
        Collection<TableProfileResult> tableResults = pm.getTableResult(t);
        
        for (TableProfileResult tpr : tableResults) {
            System.out.println(t.getName() + "  " + tpr.toString());      
            SQLColumn c = t.getColumnByName("t1_c1");
            Collection<ColumnProfileResult> columnResults = tpr.getColumnProfileResult(c);
            for (ColumnProfileResult cr : columnResults) {
                // Test min value: trim because it's stored in char(100).
                assertEquals("get min value", "abc12345678901234567890a", cr.getMinValue().toString().trim());
            }
                
            c = t.getColumnByName("t1_c6");
            columnResults = tpr.getColumnProfileResult(c);
            for (ColumnProfileResult cr : columnResults) {
                assertNotNull("get column result", cr);
                assertEquals(5, cr.getDistinctValueCount());
                assertEquals(3, ((BigDecimal) cr.getAvgValue()).intValue());
            }
        }
    }

    public void test2() throws Exception {
        SQLTable t = mydb.getTableByName("PROFILE_TEST2");
        Collection<TableProfileResult> tableResults = pm.getTableResult(t);
        
        for (TableProfileResult tpr : tableResults) {
            System.out.println(t.getName() + "  " + tpr.toString());
            SQLColumn c = t.getColumnByName("t2_c6");
            Collection<ColumnProfileResult> columnResults = tpr.getColumnProfileResult(c);
            for (ColumnProfileResult cr : columnResults) {
                assertNotNull("get column result", cr);
                assertEquals(4, cr.getDistinctValueCount()); // one is null
                assertEquals(987654322, ((BigDecimal) cr.getAvgValue()).intValue());
            }
        }
    }

    public void test3() throws Exception {
        SQLTable t = mydb.getTableByName("PROFILE_TEST3");
        Collection<TableProfileResult> tableResults = pm.getTableResult(t);
        
        for (TableProfileResult tpr : tableResults) {
            System.out.println(t.getName() + "  " + tpr.toString());
            SQLColumn c = t.getColumnByName("t3_c6");
            Collection<ColumnProfileResult> columnResults = tpr.getColumnProfileResult(c);
            for (ColumnProfileResult cr : columnResults) {
                assertNotNull("get column result", cr);
                assertEquals(0, cr.getDistinctValueCount());
                assertNull("get avg of no values", (BigDecimal) cr.getAvgValue());
                assertNull("get min of no values", (BigDecimal) cr.getMinValue());
                assertNull("get max of no values", (BigDecimal) cr.getMaxValue());                
            }
        }

    }
}
