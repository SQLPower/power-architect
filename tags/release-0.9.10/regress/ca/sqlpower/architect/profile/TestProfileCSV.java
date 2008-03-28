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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.output.ProfileCSVFormat;
import ca.sqlpower.architect.profile.output.ProfileFormat;

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
