package ca.sqlpower.architect.profile;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;

//import com.darwinsys.csv.CSVExport;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.table.DateRendererFactory;
import ca.sqlpower.architect.swingui.table.DecimalRendererFactory;

public class ProfileCSVFormat implements ProfileFormat {

    /** The desired CSV column list is published in the ProfileColumn enum.
     * @see ca.sqlpower.architect.profile.ProfileFormat#format(java.io.OutputStream, java.util.List, ca.sqlpower.architect.profile.ProfileManager)
     */
    public void format(OutputStream nout, List<SQLTable> profile, ProfileManager pm) throws Exception {
        PrintWriter out = new PrintWriter(nout);

        // Print a header
//        out.println(CSVExport.toString(Arrays.asList(ProfileColumn.values())));
        DateFormat dateFormat = (DateFormat) new DateRendererFactory().getFormat();
        DecimalFormat decFormat = (DecimalFormat) new DecimalRendererFactory().getFormat();

        // Now print column profile
        for (SQLTable t : profile) {
            TableProfileResult tpr = (TableProfileResult) pm.getResult(t);
            List<Object> commonData = new ArrayList<Object>();
            commonData.add(t.getParent().getName());
            commonData.add(t.getName());
            commonData.add(t.getCatalog() != null ? t.getCatalog().getName() : "");
            commonData.add(t.getSchema() != null ? t.getSchema().getName() : "");
            for (SQLColumn c : t.getColumns()) {
                List<Object> rowData = new ArrayList<Object>();
                rowData.addAll(commonData);
                rowData.add(c.getName());
                Date date = new Date(tpr.getCreateStartTime());
                rowData.add(dateFormat.format(date));
                rowData.add(tpr.getRowCount());
                rowData.add(c.getType());
                ColumnProfileResult cpr = (ColumnProfileResult) pm.getResult(c);
                rowData.add(cpr.getNullCount());
                rowData.add(cpr.getValueCount());
                rowData.add(cpr.getMinLength());
                rowData.add(cpr.getMaxLength());
                rowData.add(decFormat.format(cpr.getAvgLength()));
                rowData.add(cpr.getMinValue());
                rowData.add(cpr.getMaxValue());
                rowData.add(cpr.getAvgValue());
//                out.println(CSVExport.toString(rowData));
            }
        }
        out.close();
    }

}
