package ca.sqlpower.architect.swingui;

import javax.swing.SwingUtilities;

public abstract class ArchitectSwingWorker implements Runnable {

	private Exception doStuffException;

	public final void run() {
		try {
			doStuff();
		} catch (Exception e) {
			doStuffException = e;
		}
		SwingUtilities.invokeLater(new Runnable() { public void run() { cleanup(); } });
	}

	/**
	 * This gets invoked at some time after doStuff() returns.
	 */
	public abstract void cleanup();

	/**
	 * This runs on the thread you provide.  If it throws an exception, you can get it
	 * with getDoStuffException().
	 */
	public abstract void doStuff() throws Exception;
	
	public Exception getDoStuffException() {
		return doStuffException;
	}
}
