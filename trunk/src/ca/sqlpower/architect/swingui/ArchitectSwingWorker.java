package ca.sqlpower.architect.swingui;

import javax.swing.SwingUtilities;

public abstract class ArchitectSwingWorker implements Runnable {

	private Exception doStuffException;
	
	private ArchitectSwingWorker nextProcess;
	private boolean cancelled; 
	
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
					
					if (nextProcess != null) {
						nextProcess.setCancelled(cancelled);
						new Thread(nextProcess).start();
					}
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

	public synchronized boolean isCanceled() {
		return cancelled;
	}

	/**
	 * Cancel this and all following tasks
	 * @param cancelled
	 */
	public synchronized void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public ArchitectSwingWorker getNextProcess() {
		return nextProcess;
	}

	public void setNextProcess(ArchitectSwingWorker nextProcess) {
		this.nextProcess = nextProcess;
	}
	
	
}
