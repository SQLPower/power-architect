package ca.sqlpower.architect;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 * Provide a simple logging facility for long running, complicated
 * processes on the architect.  Very similar to what you
 * have when running engines from the VB app (some sort of written
 * record the user can look at in case things go horribly wrong);
 */
public class LogWriter {

	private static final Logger logger = Logger.getLogger(LogWriter.class);
	private boolean ready = false;
	private int lineCount = 0;
	
	// the log file we are printing to
	PrintWriter pw;

	/**
	 * Open the log file for writing.  It always tries to append.
	 */
	public LogWriter (String path) throws ArchitectException {		
		try {
			pw = new PrintWriter(new FileWriter(path,true)); // true == append to the file
			ready = true;
		} catch (IOException ex) {
			throw new ArchitectException("could not open ETL log file for writing: " + path, ex);
		}		
	}

	/*
     * just a passthrough to pw.println (make it look like a call to logger.info
     * so that we can change over to log4j easily).
     */
	public void info (String strLine) {
		if (ready) {
			lineCount++;
			pw.println("(" + lineCount + ") " + strLine);
		} else {
			logger.error("tried to write to closed ETL log file.");
		}
	}

	public void flush () {
		if (ready) {
			pw.flush();
		} else {
			logger.error("tried to flush closed ETL log file.");
		}		
	}


	/*
     * Close the file handle if it's still open...
     */
	public void close() {
		ready = false;
		if (pw != null) {
			try {
				pw.close();
			} catch (Exception e) {
				logger.error("error closing ETL log file handle",e);
			}	
		}
	}	
}