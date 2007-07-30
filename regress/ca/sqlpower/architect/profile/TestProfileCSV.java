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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.sqlpower.architect.SQLTable;

public class TestProfileCSV extends TestProfileBase {

    public void test1() throws Exception {
        ProfileFormat fmt = new ProfileCSVFormat();
        ByteArrayOutputStream  out = new ByteArrayOutputStream();
        SQLTable t = mydb.getTableByName("PROFILE_TEST1");

        Collection<TableProfileResult> tableResults = pm.getResults(t);
        
        List<ProfileResult> profileResults = new ArrayList<ProfileResult>();

        // Add results for table
        for (TableProfileResult tpr : tableResults) {
            profileResults.add(tpr);

            // ... and for all its columns
            for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                profileResults.add(cpr);
            }
        }

        fmt.format(out, profileResults);
        String x = out.toString();
        System.out.println("RET=" + x);

        BufferedReader rdr = new BufferedReader(new StringReader(x));
        String line;
        assertNotNull(line = rdr.readLine());   // Header line
        assertTrue(line.startsWith("DATABASE,CATALOG,SCHEMA"));
        assertNotNull(line = rdr.readLine());   // first results line
        assertTrue("Incorrect line: " + line, line.endsWith("12345678901234567890a"));
    }

}
