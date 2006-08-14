package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;

public class ProfileResultFormatter {

    /**
     * This class generates formatted HTML of the profile information
     *
     * TODO Make an interface and this class a implementation of the interface
     */
    public String format(Collection <SQLTable> tables, ProfileManager pm) throws SQLException {
        StringBuffer s = new StringBuffer();
        int cellCount = 0;

        s.append("<table border=\"0\" width=\"100%\">");
        s.append("<tr>");

        s.append("<th>");
        s.append("Column Name");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("Data Type");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("Distinct Value");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("Null Value");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("MinValue");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("MaxValue");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("AvgValue");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("MinLength");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("MaxLength");
        cellCount++;
        s.append("</th>");

        s.append("<th>");
        s.append("AvgLength");
        cellCount++;
        s.append("</th>");

        s.append("</tr>");

        NumberFormat mf = NumberFormat.getInstance();
        mf.setMaximumFractionDigits(1);
        mf.setGroupingUsed(false);

        GenericDDLGenerator gddl = new GenericDDLGenerator();

        for ( SQLTable t : tables ) {

            s.append("<tr><tr>");

            s.append("<tr><td colspan=\"" +cellCount+ "\">" );
            s.append("<h3>");
            s.append(t.getName());

            TableProfileResult tpr = (TableProfileResult) pm.getResult(t);
            if ( tpr == null || tpr.isError() ) {
                s.append("&nbsp;&nbsp;&nbsp;Profiling Error:");
                s.append("</h3>");
                s.append("</td></tr>");
                s.append("<tr><td colspan=\"" +cellCount+ "\">" );
                if ( tpr != null && tpr.getException() != null ) {
                    s.append(tpr.getException());
                    s.append("</td></tr>");
                }
            }
            else {
                s.append("&nbsp;&nbsp;&nbsp;Row&nbsp;Count:&nbsp;");
                s.append(tpr.getRowCount());
                s.append("&nbsp;&nbsp;&nbsp;Run&nbsp;Date:");
                s.append(new Date(tpr.getCreateStartTime()));

                s.append("&nbsp;&nbsp;&nbsp;Time&nbsp;To&nbsp;Create:");
                s.append(tpr.getTimeToCreate());
                s.append(" ms");
                s.append("</h3>");
                s.append("</td></tr>");

            }


            double rowCount = (double)tpr.getRowCount();

            try {
                for ( SQLColumn c : t.getColumns() ) {

                    ColumnProfileResult cpr = (ColumnProfileResult) pm.getResult(c);

                    s.append("<tr>");
                    s.append("<td bgcolor=\"#e0e0e0\">");
                    if ( c.isPrimaryKey() )
                        s.append("<b>");
                    s.append(c.getName());
                    if ( c.isPrimaryKey() )
                        s.append("</b>");
                    s.append("</td>");

                    s.append("<td bgcolor=\"#e0e0e0\">");
                    if ( gddl != null )
                        s.append(gddl.columnType(c));
                    else
                        s.append("-----");
                    s.append("</td>");

                    if ( cpr == null || cpr.isError() ) {
                        s.append("<td bgcolor=\"#f0f0f0\" colspan=\""+(cellCount-2)+"\">");
                        s.append("Column Profile Error:");
                        if ( cpr != null ) {
                            s.append(cpr.getException());
                        }
                        s.append("</td>");
                    }
                    else {

                        // distinct count
                        s.append("<td bgcolor=\"#f0f0f0\">");
                        s.append( cpr.getDistinctValueCount());
                        if ( rowCount > 0 ) {
                            s.append("(");
                            s.append(mf.format( cpr.getDistinctValueCount()*100.0/rowCount) );
                            s.append("%)");
                        } else {
                            s.append("(-)");
                        }
                        s.append("</td>");

                        // null count
                        s.append("<td bgcolor=\"#f0f0f0\">");
                        s.append( cpr.getNullCount());
                        if ( rowCount > 0 ) {
                            s.append("(" );
                            s.append( mf.format( cpr.getNullCount()*100.0/rowCount) );
                            s.append("%)");
                        }
                        else
                            s.append("(-)");
                        s.append("</td>");

                        // min value
                        s.append("<td bgcolor=\"#e0e0e0\">");
                        String minVal = null;
                        Object minValObj = cpr.getMinValue();
                        if ( minValObj != null ) {
                            minVal = minValObj.toString();
                            if ( minVal != null && minVal.length() > 30 ) {
                                String minVal2 = minVal.substring(0,30);
                                minVal = minVal2 + "...";
                            }
                        }
                        s.append( minVal );
                        s.append("</td>");

                        // max value
                        s.append("<td bgcolor=\"#e0e0e0\">");
                        String maxVal = null;
                        Object maxValObj = cpr.getMaxValue();
                        if ( maxValObj != null ) {
                            maxVal = maxValObj.toString();
                            if ( maxVal != null && maxVal.length() > 30 ) {
                                String maxVal2 = maxVal.substring(0,30);
                                maxVal = maxVal2 + "...";
                            }
                        }
                        s.append( maxVal);
                        s.append("</td>");

                        // avg value
                        s.append("<td bgcolor=\"#e0e0e0\">");
                        if (cpr.getAvgValue() instanceof Number) {
                            s.append(mf.format(cpr.getAvgValue()));
                        } else {
                            s.append(cpr.getAvgValue());
                        }
                        s.append("</td>");

                        // min length
                        s.append("<td bgcolor=\"#f0f0f0\">");
                        s.append( cpr.getMinLength());
                        s.append("</td>");

                        // max length
                        s.append("<td bgcolor=\"#f0f0f0\">");
                        s.append( cpr.getMaxLength());
                        s.append("</td>");

                        // avg length
                        s.append("<td bgcolor=\"#f0f0f0\">");
                        s.append(mf.format(cpr.getAvgLength()));
                        s.append("</td>");

                    }


                    s.append("</tr>");
                }
            } catch (ArchitectException e) {
                e.printStackTrace();
            }
        }
        s.append("</table>");
        return s.toString();

    }

}
