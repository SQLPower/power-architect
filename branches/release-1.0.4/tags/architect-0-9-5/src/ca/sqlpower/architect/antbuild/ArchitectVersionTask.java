package ca.sqlpower.architect.antbuild;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ca.sqlpower.architect.ArchitectVersion;

/**
 * Sets three properties in the ant project: app_ver_major, app_ver_minor, and
 * app_ver_tiny. All values are based on the constants in the ArchitectVersion
 * class.
 */
public class ArchitectVersionTask extends Task {
    
    public void execute() throws BuildException {
        getProject().setNewProperty("app_ver_major", ArchitectVersion.APP_VERSION_MAJOR );
        getProject().setNewProperty("app_ver_minor", ArchitectVersion.APP_VERSION_MINOR );
        getProject().setNewProperty("app_ver_tiny", ArchitectVersion.APP_VERSION_TINY );
    }

}
