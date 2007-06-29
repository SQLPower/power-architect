package ca.sqlpower.architect.etl.kettle;

import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;

/**
 * This interface defines how a repository directory should be selected.
 */
public interface KettleRepositoryDirectoryChooser {

    public RepositoryDirectory selectDirectory(Repository repo);     
}
