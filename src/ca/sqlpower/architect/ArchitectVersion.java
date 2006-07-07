package ca.sqlpower.architect;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ArchitectVersion extends Task {
    
    // The method executing the task
    public void execute() throws BuildException {
        getProject().setNewProperty("app_ver_major", ArchitectUtils.APP_VERSION_MAJOR );
        getProject().setNewProperty("app_ver_minor", ArchitectUtils.APP_VERSION_MINOR );
        getProject().setNewProperty("app_ver_tiny", ArchitectUtils.APP_VERSION_TINY );
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
    }
}
