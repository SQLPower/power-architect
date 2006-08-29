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
    public void format(OutputStream nout, List<ProfileResult> profileResult,
                                        ProfileManager pm) throws Exception {
        PrintWriter out = new PrintWriter(nout);

        // Print a header
//        out.println(CSVExport.toString(Arrays.asList(ProfileColumn.values())));
        DateFormat dateFormat = (DateFormat) new DateRendererFactory().getFormat();
        DecimalFormat decFormat = (DecimalFormat) new DecimalRendererFactory().getFormat();

        // Now print column profile
        for ( ProfileResult result : profileResult ) {

            if ( !(result instanceof ColumnProfileResult) )
                continue;

            SQLColumn c = (SQLColumn) result.getProfiledObject();
            SQLTable t = c.getParentTable();
            TableProfileResult tpr = (TableProfileResult) pm.getResult(t);
            List<Object> commonData = new ArrayList<Object>();
            commonData.add(t.getParentDatabase().getName());
            commonData.add(t.getName());
            commonData.add(t.getCatalog() != null ? t.getCatalog().getName() : "");
            commonData.add(t.getSchema() != null ? t.getSchema().getName() : "");
            commonData.add(c.getName());
            Date date = new Date(tpr.getCreateStartTime());
            commonData.add(dateFormat.format(date));
            commonData.add(tpr.getRowCount());
            commonData.add(c.getType());
            ColumnProfileResult cpr = (ColumnProfileResult) pm.getResult(c);
            commonData.add(cpr.getNullCount());
            commonData.add(cpr.getValueCount());
            commonData.add(cpr.getMinLength());
            commonData.add(cpr.getMaxLength());
            commonData.add(decFormat.format(cpr.getAvgLength()));
            commonData.add(cpr.getMinValue());
            commonData.add(cpr.getMaxValue());
            commonData.add(cpr.getAvgValue());
//          out.println(CSVExport.toString(commonData));
        }
        out.close();
    }

}
