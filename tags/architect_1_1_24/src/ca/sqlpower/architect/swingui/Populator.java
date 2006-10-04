/**
 * 
 */
package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;

public abstract class Populator extends ArchitectSwingWorker implements
		Lister {

	protected SQLDatabase.PopulateProgressMonitor progressMonitor;

	public Integer getJobSize() throws ArchitectException {
		if (progressMonitor != null) {
			return progressMonitor.getJobSize();
		}
		return null;
	}

	public int getProgress() throws ArchitectException {
		if (progressMonitor != null) {
			return progressMonitor.getProgress();
		}
		return 0;
	}

	public boolean isFinished() throws ArchitectException {
		if (progressMonitor != null) {
			return progressMonitor.isFinished();
		}
		return true;
	}
}