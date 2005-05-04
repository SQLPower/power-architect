package ca.sqlpower.architect.swingui;
import ca.sqlpower.architect.ArchitectException;

/**
 *  Monitorable interface -- for JProgress Meters
 */
public interface Monitorable {

	public void prepareToStart(); // need to run this _before_ starting an associated Timer thread

	public int getProgress() throws ArchitectException;

	public int getJobSize() throws ArchitectException;

	public boolean isFinished() throws ArchitectException;

}