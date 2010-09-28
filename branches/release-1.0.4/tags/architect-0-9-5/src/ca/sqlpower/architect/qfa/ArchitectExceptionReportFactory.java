package ca.sqlpower.architect.qfa;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ArchitectVersion;
/**
 * Creates Exception reports that are setup for the architect application 
 */
public class ArchitectExceptionReportFactory implements QFAFactory {
    private static final Logger logger = Logger.getLogger(ArchitectExceptionReportFactory.class);
    
    /**
     * The system property that controls which URL error reports go to.
     * The default (used when this property is not defined) is
     * the value of <tt>DEFAULT_REPORT_URL</tt>.
     */
    private static final String REPORT_URL_SYSTEM_PROP = "ca.sqlpower.architect.qfa.REPORT_URL";

    /**
     * The URL to post the error report to if the system property
     * that overrides it isn't defined.
     */
    private static final String DEFAULT_REPORT_URL = "http://bugs.sqlpower.ca/architect/postReport";
    
    public ExceptionReport createExceptionReport(Throwable exception) {
        ExceptionReport er = new ExceptionReport(
                exception, REPORT_URL_SYSTEM_PROP,
                DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION, 
                ArchitectUtils.getAppUptime());
        return er;
    }

}
