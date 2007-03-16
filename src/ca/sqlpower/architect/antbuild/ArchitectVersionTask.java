package ca.sqlpower.architect.antbuild;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ca.sqlpower.architect.ArchitectVersion;

public class ArchitectVersionTask extends Task {
    
    
    
    // The method executing the task
    public void execute() throws BuildException {
        getProject().setNewProperty("app_ver_major", ArchitectVersion.APP_VERSION_MAJOR );
        getProject().setNewProperty("app_ver_minor", ArchitectVersion.APP_VERSION_MINOR );
        getProject().setNewProperty("app_ver_tiny", ArchitectVersion.APP_VERSION_TINY );
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
    }
}
