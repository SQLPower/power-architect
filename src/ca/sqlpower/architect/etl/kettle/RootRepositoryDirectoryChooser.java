package ca.sqlpower.architect.etl.kettle;

import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;

/**
 * This is a basic repository directory chooser that always selects the root directory. 
 */
public class RootRepositoryDirectoryChooser implements KettleRepositoryDirectoryChooser {

    public RepositoryDirectory selectDirectory(Repository repo) {
        return repo.getDirectoryTree();
    }

}
