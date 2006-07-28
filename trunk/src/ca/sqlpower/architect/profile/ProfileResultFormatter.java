package ca.sqlpower.architect.profile;

import java.util.Set;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class ProfileResultFormatter {

    
    public String format(Set <SQLTable> tables, ProfileManager pm) {
        StringBuffer s = new StringBuffer();
        int cellCount = 0;
        
        s.append("<table border=\"1\" width=\"100%\">");
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
        for ( SQLTable t : tables ) {
            
            s.append("<tr><tr>");

            s.append("<tr><td colspan=\"" +cellCount+ "\">" );
            s.append("<h4>");
            s.append(t.getName());
            s.append("&nbsp;&nbsp;&nbsp;Row&nbsp;Count:");
            
            TableProfileResult tpr = (TableProfileResult) pm.getResult(t);
            s.append(tpr.getRowCount());
            
            s.append("&nbsp;&nbsp;&nbsp;Run&nbsp;Date:");
            s.append(tpr.getCreateDate());
            
            s.append("&nbsp;&nbsp;&nbsp;Time&nbsp;To&nbsp;Create:");
            s.append(tpr.getTimeToCreate());
            s.append(" ms");
            s.append("</h4>");
            s.append("</td></tr>");
            
            try {
                for ( SQLColumn c : t.getColumns() ) {
                    ColumnProfileResult cpr = (ColumnProfileResult) pm.getResult(c);
                    s.append("<tr>");
                    
                    s.append("<td>");
                    s.append(c.getName());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append(c.getSourceDataTypeName()+"("+c.getPrecision()+")");
                    s.append("</td>");

                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getDistinctValueCount());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getMinLength());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getMaxLength());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getAvgLength());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getMinValue());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getMaxLength());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getAvgValue());
                    s.append("</td>");
                    
                    s.append("<td>");
                    s.append( ((ColumnProfileResult)cpr).getNullCount());
                    s.append("</td>");
                    
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
