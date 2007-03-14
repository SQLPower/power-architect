package ca.sqlpower.architect.profile;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.table.DateTableCellRenderer;
import ca.sqlpower.architect.swingui.table.DecimalTableCellRenderer;
import ca.sqlpower.architect.swingui.table.PercentTableCellRenderer;

import com.darwinsys.csv.CSVExport;

public class ProfileCSVFormat implements ProfileFormat {

    /** The desired CSV column list is published in the ProfileColumn enum.
     * @see ca.sqlpower.architect.profile.ProfileFormat#format(java.io.OutputStream, java.util.List, ca.sqlpower.architect.profile.TableProfileManager)
     */
    public void format(OutputStream nout, List<ProfileResult> profileResult) 
                                                                throws Exception {
        PrintWriter out = new PrintWriter(nout);

        // Print a header
        ProfileColumn[] columns = ProfileColumn.values();
        out.println(CSVExport.toString(Arrays.asList(columns)));

        Format dateFormat = new DateTableCellRenderer().getFormat();
        Format decFormat =  new DecimalTableCellRenderer().getFormat();
        Format pctFormat =  new PercentTableCellRenderer().getFormat();
        // Now print column profile
        for ( ProfileResult result : profileResult ) {

            if ( !(result instanceof ColumnProfileResult) )
                continue;

            SQLColumn c = (SQLColumn) result.getProfiledObject();
            SQLTable t = c.getParentTable();
            TableProfileResult tpr = ((ColumnProfileResult)result).getParentResult();
            List<Object> commonData = new ArrayList<Object>();

            for ( ProfileColumn pc : columns ) {
                switch(pc) {
                case DATABASE:
                    commonData.add(t.getParentDatabase().getName());
                    break;
                case CATALOG:
                    commonData.add(t.getCatalog() != null ? t.getCatalog().getName() : "");
                    break;
                case SCHEMA:
                    commonData.add(t.getSchema() != null ? t.getSchema().getName() : "");
                    break;
                case TABLE:
                    commonData.add(t.getName());
                    break;
                case COLUMN:
                    commonData.add(c.getName());
                    break;
                case RUNDATE:
                    Date date = new Date(tpr.getCreateStartTime());
                    commonData.add(dateFormat.format(date));
                    break;
                case RECORD_COUNT:
                    commonData.add(tpr.getRowCount());
                    break;
                case DATA_TYPE:
                    commonData.add(c.getType());
                    break;
                case NULL_COUNT:
                    commonData.add(((ColumnProfileResult) result).getNullCount());
                    break;
                case PERCENT_NULL:
                    if ( tpr.getRowCount() == 0 )
                        commonData.add("n/a");
                    else
                        commonData.add( pctFormat.format(
                            ((ColumnProfileResult) result).getNullCount() / (double)tpr.getRowCount()));
                    break;
                case UNIQUE_COUNT:
                    commonData.add(((ColumnProfileResult) result).getDistinctValueCount());
                    break;
                case PERCENT_UNIQUE:
                    if ( tpr.getRowCount() == 0 )
                        commonData.add("n/a");
                    else
                        commonData.add( pctFormat.format(
                            ((ColumnProfileResult) result).getDistinctValueCount() / (double)tpr.getRowCount()));
                    break;
                case MIN_LENGTH:
                    commonData.add(((ColumnProfileResult) result).getMinLength());
                    break;
                case MAX_LENGTH:
                    commonData.add(((ColumnProfileResult) result).getMaxLength());
                    break;
                case AVERAGE_LENGTH:
                    commonData.add(decFormat.format(((ColumnProfileResult) result).getAvgLength()));
                    break;
                case MIN_VALUE:
                    commonData.add(((ColumnProfileResult) result).getMinValue());
                    break;
                case MAX_VALUE:
                    commonData.add(((ColumnProfileResult) result).getMaxValue());
                    break;
                case AVERAGE_VALUE:

                    String formattedValue;
                    Object value = ((ColumnProfileResult) result).getAvgValue();
                    if (value == null) {
                        formattedValue = "";
                    } else if (value instanceof Number) {
                        formattedValue = decFormat.format(value);
                    } else {
                        formattedValue = value.toString();
                    }
                    commonData.add(formattedValue);
                    break;
                case TOP_VALUE:
                    commonData.add("");
                    break;
                default:
                    throw new IllegalStateException("Need code to handle this column!");

                }
            }
            out.println(CSVExport.toString(commonData));
        }
        out.close();
    }

}
