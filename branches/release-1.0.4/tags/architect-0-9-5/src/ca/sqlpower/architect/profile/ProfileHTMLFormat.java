package ca.sqlpower.architect.profile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;

public class ProfileHTMLFormat implements ProfileFormat {

    /**
     * The character encoding that will appear in the HTML declaration.
     */
    private String encoding;

    public ProfileHTMLFormat(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Generates formatted HTML of the profile information
     */
    public void format(OutputStream out, List<ProfileResult> profileResults) 
                                            throws IOException, SQLException {

        // Create header first, obtaining column count, so we can use it in a colspan later.
        StringBuffer s = new StringBuffer();
        int cellCount = 0;

        s.append("\n  <tr>");

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

        String header = s.toString();

        // Do the rest in normal I/O mode...
        PrintWriter outw = new PrintWriter(new OutputStreamWriter(out, encoding));

        outw.printf("<? xml version=\"1.0\" encoding=\"%s\" ?>", encoding);

        outw.print("\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        outw.print("\n<html><body>");

        NumberFormat mf = NumberFormat.getInstance();
        mf.setMaximumFractionDigits(1);
        mf.setGroupingUsed(false);

        GenericDDLGenerator gddl = new GenericDDLGenerator();

        boolean firstTable = true;

        for ( ProfileResult result : profileResults ) {
            if ( result instanceof TableProfileResult ) {

                if ( firstTable ) {
                    firstTable = false;
                } else {
                    outw.print("\n </table>");
                }

                SQLTable t = ((TableProfileResult)result).getProfiledObject();

                outw.print("\n<br/><br/>");
                outw.print("\n<table border=\"0\" width=\"100%\">");

                outw.print("\n  <tr><td colspan=\"" +cellCount+ "\">" );
                outw.print("<h3>");
                outw.print(t.getName());

                if (result == null || result.getException() != null) {
                    outw.print("&nbsp;&nbsp;&nbsp;Profiling Error:");
                    outw.print("</h3>");
                    outw.print("</td></tr>");
                    outw.print("\n  <tr><td colspan=\"" +cellCount+ "\">" );
                    if ( result != null && result.getException() != null ) {
                        outw.print(result.getException());
                        outw.print("</td></tr>");
                    }
                } else {
                    double rowCount = (double) (((TableProfileResult)result).getRowCount());
                    outw.print("&nbsp;&nbsp;&nbsp;Row&nbsp;Count:&nbsp;");
                    outw.print(rowCount);
                    outw.print("&nbsp;&nbsp;&nbsp;Run&nbsp;Date:");
                    outw.print(new Date(result.getCreateStartTime()));

                    outw.print("&nbsp;&nbsp;&nbsp;Time&nbsp;To&nbsp;Create:");
                    outw.print(result.getTimeToCreate());
                    outw.print(" ms");
                    outw.print("</h3>");
                    outw.print("</td></tr>");

                    outw.print(header);
                }
            } else if ( result instanceof ColumnProfileResult ) {
                SQLColumn c = (SQLColumn) result.getProfiledObject();
                TableProfileResult tResult = ((ColumnProfileResult)result).getParentResult();
                double rowCount = (double) tResult.getRowCount();

                outw.print("\n  <tr>");
                outw.print("<td bgcolor=\"#e0e0e0\">");
                if ( c.isPrimaryKey() )
                    outw.print("<b>");
                outw.print(c.getName());
                if ( c.isPrimaryKey() )
                    outw.print("</b>");
                outw.print("</td>");

                outw.print("<td bgcolor=\"#e0e0e0\">");
                if ( gddl != null )
                    outw.print(gddl.columnType(c));
                else
                    outw.print("-----");
                outw.print("</td>");

                if ( result == null || result.getException() != null ) {
                    outw.print("<td bgcolor=\"#f0f0f0\" colspan=\""+(cellCount-2)+"\">");
                    outw.print("Column Profile Error:");
                    if ( result != null ) {
                        outw.print(result.getException());
                    }
                    outw.print("</td>");
                } else {

                    // distinct count
                    outw.print("<td bgcolor=\"#f0f0f0\">");
                    outw.print( ((ColumnProfileResult) result).getDistinctValueCount());
                    if ( rowCount > 0 ) {
                        outw.print("(");
                        outw.print(mf.format( ((ColumnProfileResult) result).getDistinctValueCount()*100.0/rowCount) );
                        outw.print("%)");
                    } else {
                        outw.print("(-)");
                    }
                    outw.print("</td>");

                    // null count
                    outw.print("<td bgcolor=\"#f0f0f0\">");
                    outw.print( ((ColumnProfileResult) result).getNullCount());
                    if ( rowCount > 0 ) {
                        outw.print("(" );
                        outw.print( mf.format( ((ColumnProfileResult) result).getNullCount()*100.0/rowCount) );
                        outw.print("%)");
                    } else {
                        outw.print("(-)");
                    }
                    outw.print("</td>");

                    // min value
                    outw.print("<td bgcolor=\"#e0e0e0\">");
                    String minVal = null;
                    Object minValObj = ((ColumnProfileResult) result).getMinValue();
                    if ( minValObj != null ) {
                        minVal = minValObj.toString();
                        if ( minVal != null && minVal.length() > 30 ) {
                            String minVal2 = minVal.substring(0,30);
                            minVal = minVal2 + "...";
                        }
                    }
                    outw.print( minVal );
                    outw.print("</td>");

                    // max value
                    outw.print("<td bgcolor=\"#e0e0e0\">");
                    String maxVal = null;
                    Object maxValObj = ((ColumnProfileResult) result).getMaxValue();
                    if ( maxValObj != null ) {
                        maxVal = maxValObj.toString();
                        if ( maxVal != null && maxVal.length() > 30 ) {
                            String maxVal2 = maxVal.substring(0,30);
                            maxVal = maxVal2 + "...";
                        }
                    }
                    outw.print( maxVal);
                    outw.print("</td>");

                    // avg value
                    outw.print("<td bgcolor=\"#e0e0e0\">");
                    if (((ColumnProfileResult) result).getAvgValue() instanceof Number) {
                        outw.print(mf.format(((ColumnProfileResult) result).getAvgValue()));
                    } else {
                        outw.print(((ColumnProfileResult) result).getAvgValue());
                    }
                    outw.print("</td>");

                    // min length
                    outw.print("<td bgcolor=\"#f0f0f0\">");
                    outw.print( ((ColumnProfileResult) result).getMinLength());
                    outw.print("</td>");

                    // max length
                    outw.print("<td bgcolor=\"#f0f0f0\">");
                    outw.print( ((ColumnProfileResult) result).getMaxLength());
                    outw.print("</td>");

                    // avg length
                    outw.print("<td bgcolor=\"#f0f0f0\">");
                    outw.print(mf.format(((ColumnProfileResult) result).getAvgLength()));
                    outw.print("</td>");

                }

                outw.print("</tr>");
            }
        }

        outw.print("\n </table>");
        outw.print("\n</body></html>");
        outw.close();
    }

}
