package ca.sqlpower.architect.swingui;

import javax.swing.SwingUtilities;

public abstract class ArchitectSwingWorker implements Runnable {

	private Exception doStuffException;
	
	/**
	 * The message that will be displayed in a dialog box when
	 * cleanup() throws an exception.
	 */
	private String cleanupExceptionMessage = "A problem occurred.";
	
	public final void run() {
		try {
			doStuff();
		} catch (Exception e) {
			doStuffException = e;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					cleanup();
				} catch (Exception e) {
					ASUtils.showExceptionDialog(cleanupExceptionMessage, e);
				}
			}
		});
	}

	/**
	 * This gets invoked at some time after doStuff() returns.
	 */
	public abstract void cleanup() throws Exception;

	/**
	 * This runs on the thread you provide.  If it throws an exception, you can get it
	 * with getDoStuffException().
	 */
	public abstract void doStuff() throws Exception;
	
	public Exception getDoStuffException() {
		return doStuffException;
	}

	public String getCleanupExceptionMessage() {
		return cleanupExceptionMessage;
	}

	public void setCleanupExceptionMessage(String cleanupExceptionMessage) {
		this.cleanupExceptionMessage = cleanupExceptionMessage;
	}
	
	
}
