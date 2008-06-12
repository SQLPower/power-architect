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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ArchitectVersion class exists as a means of finding out which
 * version of the Architect application you are dealing with.  It is
 * also used during the build process in order to determine which version
 * number to put in the archive file names.
 * <p>
 * It is extremely important that this class has no dependancies aside
 * from the standard Java libraries.
 */
public class ArchitectVersion implements Comparable<ArchitectVersion> {
    
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
    
    /**
     * The components of this version, from most major to least major. Parts
     * will either be Integer values or String values. If there is a String
     * part, it will be the last part, and is referred to as the "Suffix."
     */
    private Object[] parts;

    /**
     * Creates a new Version object from the given string. The format is
     * <tt>a1.a2.a3-[suffix]</tt>.  Examples: <tt>1.2.3-alpha</tt>
     * or <tt>1.3.0</tt> or <tt>2.0.0</tt>.  The version number must have exactly
     * 3 numeric components.
     * 
     * @param version The version string
     */
    public ArchitectVersion(String version) throws Exception{
        String[] rawParts = version.split("\\.");
        List<Object> parsedParts = new ArrayList<Object>();
        Pattern p = Pattern.compile("[0-9]+");
        for (int i = 0; i < rawParts.length; i++) {
            Matcher m = p.matcher(rawParts[i]);
            if (m.matches()) {
                parsedParts.add(Integer.parseInt(rawParts[i]));
            } else if (i == rawParts.length - 1) {
                Pattern suffixPattern = Pattern.compile("([0-9]+)(-)(.+)");
                Matcher suffixMatcher = suffixPattern.matcher((String) rawParts[i]);
                if (suffixMatcher.matches()) {
                    parsedParts.add(Integer.parseInt(suffixMatcher.group(1)));
                    parsedParts.add(suffixMatcher.group(3));
                } else {
                    throw new Exception("Bad version format \""+version+"\"");
                }
            } else {
                throw new Exception("Bad version format \""+version+"\"");
            }
        }
        parts = parsedParts.toArray();
        if (parts.length != 3 && parts.length != 4) throw new Exception("Bad version format \""+version+"\"");
    }
    
    /**
     * Returns the String representation of this version number in the same format
     * accepted by the {@link #ArchitectVersion(String)} constructor.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(parts[0]);
        sb.append("." + parts[1]);
        sb.append("." + parts[2]);
        
        if (sb.length() == 3) return sb.toString();
        
        sb.append("-" + parts[3]);
        
        return sb.toString();
    }

    /**
     * If two versions differ only as far as one having a suffix and the other
     * not having a suffix, the one without the suffix is considered newer. This
     * allows the natural idea that the following are in chronological order:
     * <ul>
     *  <li>1.0.0-alpha
     *  <li>1.0.0-beta
     *  <li>1.0.0-rc1
     *  <li>1.0.0-rc2
     *  <li>1.0.0
     *  <li>1.1.0-alpha
     *  <li>1.1.0
     * </ul>
     */
    public int compareTo(ArchitectVersion version) {
        int i;
        for (i = 0; i < parts.length && i < version.parts.length; i++) {
            if (parts[i] instanceof Integer && version.parts[i] instanceof Integer) {
                int v = (Integer) parts[i];
                int ov = (Integer) version.parts[i];
                if (v > ov) return 1;
                if (v < ov) return -1;
            } else if (parts[i] instanceof String && version.parts[i] instanceof String) {
                String v = (String) parts[i];
                String ov = (String) version.parts[i];
                int diff = v.compareTo(ov);
                if (diff != 0) return diff;
            } else if (parts[i] instanceof Integer && version.parts[i] instanceof String) {
                return 1;
            } else if (parts[i] instanceof String && version.parts[i] instanceof Integer) {
                return -1;
            } else {
                throw new IllegalStateException("Found a version part that's not a String or Integer");
            }
        }
        
        // check for special case where comparing 1.0a to 1.0 (1.0 should be newer)
        if (parts.length == version.parts.length + 1 && parts[parts.length-1] instanceof String) return -1;
        if (version.parts.length == parts.length + 1 && version.parts[version.parts.length-1] instanceof String) return 1;
        
        // otherwise if one version has more integer parts, it's newer.
        if (parts.length > version.parts.length) return 1;
        if (parts.length < version.parts.length) return -1;
        
        // they're actually the same
        return 0;
    }
    
    /**
     * Initializes and returns an ArchitectVersion object based on its own version number.
     * It uses the constant APP_VERSION declared above
     */
    public static ArchitectVersion getAppVersionObject() throws Exception{
        return new ArchitectVersion(APP_VERSION);
    }

}
