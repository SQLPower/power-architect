/*
 * Created on Jun 13, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.qfa;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;

public class ExceptionReport {

    private static final Logger logger = Logger.getLogger(ExceptionReport.class);
    
    private Throwable exception;
    private String architectVersion;
    private long applicationUptime;
    private long totalMem;
    private long freeMem;
    private long maxMem;
    private String jvmVendor;
    private String jvmVersion;
    private String osArch;
    private String osName;
    private String osVersion;
    private int numSourceConnections;
    private int numObjectsInPlayPen;
    // security manager info
    // permission to get project file
    // undo history (need permission)
    // JDBC drivers and other crap on classpath (need permission)
    private String userActivityDescription;

    private String remarks;
    
    
    public ExceptionReport(Throwable exception) {
        this.exception = exception;
        architectVersion = ArchitectUtils.APP_VERSION;
        applicationUptime = ArchitectUtils.getAppUptime();
        totalMem = Runtime.getRuntime().totalMemory();
        freeMem = Runtime.getRuntime().freeMemory();
        maxMem = Runtime.getRuntime().maxMemory();
        jvmVendor = System.getProperty("java.vendor");
        jvmVersion = System.getProperty("java.version");
        osArch = System.getProperty("os.arch");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
    }
    
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
        xml.append("\n<architect-exception-report version=\"1.0\">");
        xml.append("\n <exception class=\"").append(ArchitectUtils.escapeXML(exception.getClass().getName())).append("\">");
        for (StackTraceElement ste : exception.getStackTrace()) {
            xml.append("\n  <trace-element class=\"").append(ArchitectUtils.escapeXML(ste.getClassName()))
                    .append("\" method=\"").append(ArchitectUtils.escapeXML(ste.getMethodName()))
                    .append("\" file=\"").append(ArchitectUtils.escapeXML(ste.getFileName()))
                    .append("\" line=\"").append(ste.getLineNumber())
                    .append("\" />");
        }
        xml.append("\n </exception>");
        xml.append("\n <architect-version>").append(ArchitectUtils.escapeXML(architectVersion)).append("</architect-version>");
        xml.append("\n <architect-uptime>").append(applicationUptime).append("</architect-uptime>");
        xml.append("\n <total-mem>").append(totalMem).append("</total-mem>");
        xml.append("\n <free-mem>").append(freeMem).append("</free-mem>");
        xml.append("\n <max-mem>").append(maxMem).append("</max-mem>");
        xml.append("\n <jvm vendor=\"").append(ArchitectUtils.escapeXML(jvmVendor)).append("\" version=\"").append(ArchitectUtils.escapeXML(jvmVersion)).append("\" />");
        xml.append("\n <os arch=\"").append(ArchitectUtils.escapeXML(osArch)).append("\" name=\"").append(ArchitectUtils.escapeXML(osName)).append("\" version=\"").append(ArchitectUtils.escapeXML(osVersion)).append("\" />");
        xml.append("\n <num-source-connections>").append(numSourceConnections).append("</num-source-connections>");
        xml.append("\n <num-objects-in-playpen>").append(numObjectsInPlayPen).append("</num-objects-in-playpen>");
        xml.append("\n <user-activity-description>").append(ArchitectUtils.escapeXML(userActivityDescription)).append("</user-activity-description>");
        xml.append("\n <remarks>").append(ArchitectUtils.escapeXML(remarks)).append("</remarks>");
        xml.append("\n</architect-exception-report>");
        xml.append("\n");
        return xml.toString();
    }

    /**
     * Attempts to send this exception report, in XML form, to the official SQL
     * Power error reporting URL for the architect.
     */
    public void postReportToSQLPower() {
        final String url = "http://bugs.sqlpower.ca/architect/postReport";
        try {
            HttpURLConnection dest = (HttpURLConnection) new URL(url).openConnection();
            dest.setDoOutput(true);
            dest.setRequestMethod("POST");
            dest.connect();
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(dest.getOutputStream());
                out.write(toXML().getBytes("ISO-8859-1"));
                out.flush();
            } finally {
                if (out != null) out.close();
            }
        } catch (Exception e) {
            // Just catch-and-squash everything because we're already in up to our necks at this point.
            logger.error("Couldn't send exception report to <\""+url+"\">", e);
        }
    }
    
    public long getApplicationUptime() {
        return applicationUptime;
    }


    public void setApplicationUptime(long applicationUptime) {
        this.applicationUptime = applicationUptime;
    }


    public Throwable getException() {
        return exception;
    }


    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }


    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public int getNumObjectsInPlayPen() {
        return numObjectsInPlayPen;
    }


    public void setNumObjectsInPlayPen(int numObjectsInPlayPen) {
        this.numObjectsInPlayPen = numObjectsInPlayPen;
    }


    public int getNumSourceConnections() {
        return numSourceConnections;
    }


    public void setNumSourceConnections(int numSourceConnections) {
        this.numSourceConnections = numSourceConnections;
    }


    public String getOsVersion() {
        return osVersion;
    }


    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }


    public String getUserActivityDescription() {
        return userActivityDescription;
    }


    public void setUserActivityDescription(String userActivityDescription) {
        this.userActivityDescription = userActivityDescription;
    }

    public String getArchitectVersion() {
        return architectVersion;
    }

    public void setArchitectVersion(String architectVersion) {
        this.architectVersion = architectVersion;
    }

    public void setRemarks(String v) {
        this.remarks = v;
    }
    
    public String getRemarks() {
        return remarks;
    }

}
