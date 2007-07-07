/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
