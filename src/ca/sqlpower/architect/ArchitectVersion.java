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
package ca.sqlpower.architect;

/**
 * The ArchitectVersion class exists as a means of finding out which
 * version of the Architect application you are dealing with.  It is
 * also used during the build process in order to determine which version
 * number to put in the archive file names.
 * <p>
 * It is extremely important that this class has no dependancies aside
 * from the standard Java libraries.
 */
public class ArchitectVersion {
    
    /**
     * The major version number. Currently we're working toward the 1.0 release.
     * This number will become 1 when we are satisfied that the API is feature
     * complete and can remain stable for a period of time. Until then, we will
     * leave the major version at 0 and all bets are off.
     */
    public static final String APP_VERSION_MAJOR  = "0";
    
    /**
     * Minor version number. This changes when new features appear that might
     * break forward compatibility.
     */
    public static final String APP_VERSION_MINOR  = "9";
    
    /**
     * Tiny version number.  This number changes with each release, but resets
     * back to 0 when the minor version changes.  All versions under the same
     * minor version number are fully compatible with each other.
     */
    public static final String APP_VERSION_TINY   = "11";
    
    /**
     * Suffixes indicate pre-release builds.  They normally progress from "alpha"
     * to "beta" to "rcN" (release candidate N).  This progression is common
     * for a lot of software projects, and has the advantage of sorting alphabetically
     * into the correct order from oldest to newest.
     * <p>
     * Full releases do not have a suffix.  In that case, the suffix is the empty
     * string (not null).
     */
    public static final String APP_VERSION_SUFFIX = "alpha";
    
    /**
     * The full version number, formatted as Major.Minor.Tiny[-Suffix].  Note the square
     * brackets are not part of the version string; they indicate that the hyphen and
     * suffix are omitted when there is no suffix.
     */
    public static final String APP_VERSION = APP_VERSION_MAJOR + "." +
                                            APP_VERSION_MINOR + "." +
                                            APP_VERSION_TINY +
                                            (APP_VERSION_SUFFIX.length() > 0
                                              ? "-" + APP_VERSION_SUFFIX
                                              : "");
}
