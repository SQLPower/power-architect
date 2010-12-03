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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.sqlpower.util.Version;

/**
 * The ArchitectVersion class exists as a means of finding out which
 * version of the Architect application you are dealing with.  It is
 * also used during the build process in order to determine which version
 * number to put in the archive file names.
 * <p>
 * XXX It is extremely important that this class has no dependencies aside
 * from the standard Java libraries.
 */
public class ArchitectVersion extends Version {
    
    /**
     * The major version number. Currently we're working toward the 1.0 release.
     * This number will become 1 when we are satisfied that the API is feature
     * complete and can remain stable for a period of time. Until then, we will
     * leave the major version at 0 and all bets are off.
     */
    public static final String APP_VERSION_MAJOR  = "1";
    
    /**
     * Minor version number. This changes when new features appear that might
     * break forward compatibility.
     */
    public static final String APP_VERSION_MINOR  = "0";
    
    /**
     * Tiny version number.  This number changes with each release, but resets
     * back to 0 when the minor version changes.  All versions under the same
     * minor version number are fully compatible with each other.
     */
    public static final String APP_VERSION_TINY   = "3";
    
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
     * The normal readable version number, formatted as Major.Minor.Tiny. 
     */
    public static final ArchitectVersion APP_VERSION = new ArchitectVersion(APP_VERSION_MAJOR + "." +
            APP_VERSION_MINOR + "." + APP_VERSION_TINY);
    /**
     * The full version number, formatted as Major.Minor.Tiny[-Suffix].  Note the square
     * brackets are not part of the version string; they indicate that the hyphen and
     * suffix are omitted when there is no suffix.
     */
    public static final ArchitectVersion APP_FULL_VERSION = new ArchitectVersion(APP_VERSION_MAJOR + "." +
                                            APP_VERSION_MINOR + "." + APP_VERSION_TINY +
                                            (APP_VERSION_SUFFIX.length() > 0
                                              ? "-" + APP_VERSION_SUFFIX
                                              : ""));
    
    /**
     * Creates a new ArchitectVersion object from the given string. The format is
     * <tt>a1.a2.(...).aN[suffix]</tt>.  Examples: <tt>1.2.3-alpha</tt>
     * or <tt>1.3</tt> or <tt>2</tt>.  The version number must have at
     * least one numeric component, so <tt>1suffix</tt> is legal but
     * <tt>suffix</tt> on its own is not. A suffix may contain any sequence of
     * characters, but every <tt>.</tt> in the suffix must be followed with a number.
     * 
     * @param version The version string
     */
    public ArchitectVersion(String version) {
        super(version);
    }
    
    /**
     * ArchitectVersion numbers are mutually comparable even if they have different
     * numbers of getParts(), and in that case, version <tt>2.0</tt> is older
     * than <tt>2.0.0</tt> or <tt>2.0.1</tt> but still newer than
     * <tt>1.0.0</tt>.
     * <p>
     * If two versions differ only as far as one having a suffix and the other
     * not having a suffix, the one without the suffix is considered newer. This
     * allows the natural idea that the following are in chronological order:
     * <ul>
     *  <li>1.0-alpha
     *  <li>1.0-beta
     *  <li>1.0-rc1
     *  <li>1.0-rc2
     *  <li>1.0
     *  <li>1.1-alpha
     *  <li>1.1
     * </ul>
     * Suffixes are broken down into numeric and non-numeric sections, and evaluated
     * as such.
     */
    public int compareTo(ArchitectVersion o) {
        int i;
        for (i = 0; i < getParts().length && i < o.getParts().length; i++) {
            if (getParts()[i] instanceof Integer && o.getParts()[i] instanceof Integer) {
                int v = (Integer) getParts()[i];
                int ov = (Integer) o.getParts()[i];
                if (v > ov) return 1;
                if (v < ov) return -1;
            } else if (getParts()[i] instanceof String && o.getParts()[i] instanceof String) {
                String v = (String) getParts()[i];
                String ov = (String) o.getParts()[i];
                // Splits into name and final number
                Pattern p = Pattern.compile("([^0-9]*)([0-9]*)");
                Matcher vm = p.matcher(v);
                Matcher ovm = p.matcher(ov);
                
                while (vm.find() && ovm.find()) {
                    if (!vm.group(1).equals(ovm.group(1))) {
                        return vm.group(1).compareTo(ovm.group(1));
                    } else {
                        Integer vi = Integer.parseInt("0"+vm.group(2));
                        Integer ovi = Integer.parseInt("0"+ovm.group(2));
                        if (vi.compareTo(ovi) != 0) {
                            return vi.compareTo(ovi);
                        }
                    }
                }
            } else if (getParts()[i] instanceof Integer && o.getParts()[i] instanceof String) {
                return 1;
            } else if (getParts()[i] instanceof String && o.getParts()[i] instanceof Integer) {
                return -1;
            } else {
                throw new IllegalStateException("Found a version part that's not a String or Integer");
            }
        }
        
        // check for special case where comparing 1.0a to 1.0 (1.0 should be newer)
        if (getParts().length == o.getParts().length + 1 && getParts()[getParts().length-1] instanceof String) return -1;
        if (o.getParts().length == getParts().length + 1 && o.getParts()[o.getParts().length-1] instanceof String) return 1;
        
        // otherwise if one version has more integer getParts(), it's newer.
        if (getParts().length > o.getParts().length) return 1;
        if (getParts().length < o.getParts().length) return -1;
        
        // they're actually the same
        return 0;
    }
    
}
