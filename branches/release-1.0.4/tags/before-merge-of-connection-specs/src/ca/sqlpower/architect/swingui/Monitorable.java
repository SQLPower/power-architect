package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;

/**
 * The Monitorable interface is a generic way for objects which perform certain
 * tasks to make their progress monitorable.  This information can be interpreted
 * by a ProgressWatcher which will in turn drive a progress bar in the GUI. 
 */
public interface Monitorable {

	/**
	 * Tells how much work has been done.
	 * 
	 * @return The amount of work that has been done so far (between 0 and the job size).
	 * @throws ArchitectException
	 */
	public int getProgress() throws ArchitectException;

	/**
	 * Tells the size of the job being preformed.  If the size is not yet known (because
	 * work needs to be done to calculate the job size), returns null.
	 * 
	 * @return An Integer saying how much work must be done; null if this amount is not
	 * yet known.
	 * @throws ArchitectException
	 */
	public Integer getJobSize() throws ArchitectException;

	/**
	 * Tells interested parties that the task being performed by this object is finished.
	 * Normally, getJobSize() and getProgress will return equal integers at this point,
	 * but this is not required.  For example, when the user cancels the operation, it will
	 * be finished even though we have not progressed to the end of the job.
	 * 
	 * @return True if and only if the process is finished.
	 * @throws ArchitectException
	 */
	public boolean isFinished() throws ArchitectException;

}