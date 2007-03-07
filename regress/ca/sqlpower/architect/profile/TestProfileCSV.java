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
        List<ProfileResult> profileResults = new ArrayList<ProfileResult>();
        SQLTable t = mydb.getTableByName("PROFILE_TEST1");

        Collection<TableProfileResult> tableResults = pm.getTableResult(t);
        
        // Add results for table
        for (TableProfileResult tpr : tableResults) {
            profileResults.add(tpr);

            // ... and for all its columns
            for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                profileResults.add(cpr);
            }
        }

        fmt.format(out, profileResults, pm);
        String x = out.toString();
        System.out.println("RET=" + x);

        BufferedReader rdr = new BufferedReader(new StringReader(x));
        String line;
        assertNotNull(line = rdr.readLine());   // Header line
        assertTrue(line.startsWith("DATABASE,CATALOG,SCHEMA"));
        assertNotNull(line = rdr.readLine());   // first results line
        assertTrue(line.endsWith("12345678901234567890a"));
    }

}
