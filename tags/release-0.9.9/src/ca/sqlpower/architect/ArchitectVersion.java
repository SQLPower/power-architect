/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
    public static final String APP_VERSION_TINY   = "9";
    
    /**
     * Suffixes indicate pre-release builds.  They normally progress from "alpha"
     * to "beta" to "rcN" (release candidate N).  This progression is common
     * for a lot of software projects, and has the advantage of sorting alphabetically
     * into the correct order from oldest to newest.
     * <p>
     * Full releases do not have a suffix.  In that case, the suffix is the empty
     * string (not null).
     */
    public static final String APP_VERSION_SUFFIX = "";
    
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
