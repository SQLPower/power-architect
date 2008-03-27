/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.profile;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class TestProfileManager extends TestProfileBase {

    public void test1() throws Exception {
        SQLTable t = mydb.getTableByName("PROFILE_TEST1");
        Collection<TableProfileResult> tableResults = pm.getResults(t);
        
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
        Collection<TableProfileResult> tableResults = pm.getResults(t);
        
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
        Collection<TableProfileResult> tableResults = pm.getResults(t);
        
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
    
    /**
     * This test will add a database, profile tables, and then remove the database 
     * to see if the profiles dependent on the database are removed as well.
     * This test came from bug 1521.
     */
    public void testRemovingDBRemovesProfiles() throws Exception {
        List<TableProfileResult> t1Results = pm.getResults(t1);
        for (TableProfileResult tpr : t1Results) {
            assertEquals(mydb, tpr.getProfiledObject().getParentDatabase());
        }
        session.getRootObject().removeChild(mydb);
        t1Results = pm.getResults(t1);
        assertEquals(0, t1Results.size());
    }
}
