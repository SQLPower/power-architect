package ca.sqlpower.architect.swingui;
import ca.sqlpower.architect.ArchitectException;

/**
 *  Monitorable interface -- for JProgress Meters
 */
public interface Monitorable extends Runnable {

	public int getProgress() throws ArchitectException;

	public int getJobSize() throws ArchitectException;

	public boolean isFinished() throws ArchitectException;

	// also inherits run();

}