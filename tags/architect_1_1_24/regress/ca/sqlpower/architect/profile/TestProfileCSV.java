package ca.sqlpower.architect.profile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class TestProfileCSV extends TestProfileBase {

    public void test1() throws Exception {
        ProfileFormat fmt = new ProfileCSVFormat();
        ByteArrayOutputStream  out = new ByteArrayOutputStream();
        List<ProfileResult> profileResults = new ArrayList<ProfileResult>();
        SQLTable t = mydb.getTableByName("PROFILE_TEST1");

        // Add results for table
        ProfileResult pr = pm.getResult(t);
        profileResults.add(pr);

        // ... and for all its columns
        for ( SQLColumn c : t.getColumns() ) {
            profileResults.add(pm.getResult(c));
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
