package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;

public interface Lister {

	public Integer getJobSize() throws ArchitectException;

	public int getProgress() throws ArchitectException;

	public boolean isFinished() throws ArchitectException;

}