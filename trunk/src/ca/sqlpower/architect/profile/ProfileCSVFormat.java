package ca.sqlpower.architect.profile;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class ProfileCSVFormat implements ProfileFormat {

    public void format(OutputStream nout, List<SQLTable> profile, ProfileManager pm) throws Exception {
        PrintWriter out = new PrintWriter(nout);
        for (SQLTable t : profile) {
            TableProfileResult tpr = (TableProfileResult) pm.getResult(t);
            // format results e.g., tpr.getRowCount());
            for (SQLColumn c : t.getColumns()) {
                ColumnProfileResult cpr = (ColumnProfileResult) pm.getResult(c);
                // format cpr...
            }
        }
    }

}
