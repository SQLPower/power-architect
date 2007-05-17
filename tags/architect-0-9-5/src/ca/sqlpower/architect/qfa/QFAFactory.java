package ca.sqlpower.architect.qfa;

public interface QFAFactory {
    
    /** 
     * Create a new exception report that is setup for this application
     * @param exception the exception that is getting reported
     * @return the exception report.
     */
    public ExceptionReport createExceptionReport(Throwable exception);
}
