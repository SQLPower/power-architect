/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
        getProject().setNewProperty("app_ver_suffix", ArchitectVersion.APP_VERSION_SUFFIX);
    }

}
