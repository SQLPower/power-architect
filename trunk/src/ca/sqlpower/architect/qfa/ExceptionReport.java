/*
 * Created on Jun 13, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.qfa;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;

/**
 * Implements a "call home, we're broken" functionality - does not report
 * anything terribly secret, but does dump out the list of what's in the
 * playpen; the data are posted to a URL on our web site to keep track
 * of errors that people see when running the product.
 */
public class ExceptionReport {

    private static final int MAX_REPORT_TRIES = 10;

    private static final Logger logger = Logger.getLogger(ExceptionReport.class);

    private Throwable exception;
    private String reportUrlSysProp;
    private String reportUrl;
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

    private ExceptionReport(){
        totalMem = Runtime.getRuntime().totalMemory();
        freeMem = Runtime.getRuntime().freeMemory();
        maxMem = Runtime.getRuntime().maxMemory();
        jvmVendor = System.getProperty("java.vendor");
        jvmVersion = System.getProperty("java.version");
        osArch = System.getProperty("os.arch");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
    }
    
    public ExceptionReport(Throwable exception, String reportUrlSysProp, String reportUrl, String architectVersion, long applicationUptime) {
        this();
        this.exception = exception;
        this.reportUrlSysProp = reportUrlSysProp;
        this.reportUrl = reportUrl;
        this.architectVersion = architectVersion;
        this.applicationUptime = applicationUptime;
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
        xml.append("\n<architect-exception-report version=\"1.0\">");
        appendNestedExceptions(xml,exception);
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

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Exception Report: ");
        sb.append(exception);
        sb.append(" ");
        sb.append(remarks);
        return sb.toString();
    }
    private void appendNestedExceptions(StringBuffer xml, Throwable exception) {
        if (exception == null) return;
        xml.append("\n <exception class=\"").append(ArchitectUtils.escapeXML(exception.getClass().getName())).append("\" message=\"")
                        .append(ArchitectUtils.escapeXML(exception.getMessage())).append("\">");
        for (StackTraceElement ste : exception.getStackTrace()) {
            xml.append("\n  <trace-element class=\"").append(ArchitectUtils.escapeXML(ste.getClassName()))
                    .append("\" method=\"").append(ArchitectUtils.escapeXML(ste.getMethodName()))
                    .append("\" file=\"").append(ArchitectUtils.escapeXML(ste.getFileName()))
                    .append("\" line=\"").append(ste.getLineNumber())
                    .append("\" />");
        }
        appendNestedExceptions(xml,exception.getCause());
        xml.append("\n </exception>");
    }

    static int numReportsThisRun = 0;

    /**
     * Attempt to send this exception report, in XML form, to the official SQL
     * Power error reporting URL for the Architect.
     */
    public void postReport(ArchitectSwingSessionContext context) {
        logger.debug("posting report: "+toString());
        if (numReportsThisRun++ > MAX_REPORT_TRIES) {
            logger.info(
                String.format(
                    "Not logging this error, threshold of %d exceeded", MAX_REPORT_TRIES));
            return;
        }
        exception.printStackTrace();
        String url = System.getProperty(reportUrlSysProp);
        if (url == null) {
            url = reportUrl;
        }
        // TODO decouple this from the main frame
        UserSettings settings = context.getUserSettings().getQfaUserSettings();
        if(!settings.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true)) return;
        logger.info("Posting error report to SQL Power at URL <"+url+">");
        try {
            HttpURLConnection dest = (HttpURLConnection) new URL(url).openConnection();
            dest.setConnectTimeout(3000);
            dest.setReadTimeout(3000);
            dest.setDoOutput(true);
            dest.setDoInput(true);
            dest.setUseCaches(false);
            dest.setRequestMethod("POST");
            dest.setRequestProperty("Content-Type", "text/xml");
            dest.connect();
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(dest.getOutputStream());
                out.write(toXML().getBytes("ISO-8859-1"));
                out.flush();
            } finally {
                if (out != null) out.close();
            }


            // Note: the error report will only get sent if we attempt to read from the URL Connection (!??!?)
            InputStreamReader inputStreamReader = new InputStreamReader(dest.getInputStream());
            BufferedReader in = new BufferedReader(inputStreamReader);
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            logger.info("Error report servlet response: "+response);
        } catch (Exception e) {
            // Just catch-and-squash everything because we're already in up to our necks at this point.
            logger.error("Couldn't send exception report to <\""+url+"\">", e);
        }
        logger.debug("Finished posting report");
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
