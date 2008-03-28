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
package ca.sqlpower.architect.profile.output;

import java.io.OutputStream;
import java.util.List;

import ca.sqlpower.architect.profile.ProfileResult;

/**
 * The general contract of a class that can format a profile results.
 * Implementations may format the profile results in HTML, XML, PDF, CSV, etc.
 * 
 * @author ian
 */
public interface ProfileFormat {

    /**
     * Formats a set of profile results to an open OutputStream.
     * The "out" parameter is an OutputStream not a Writer since some
     * of the formats (e.g., PDF) are binary formats.
     * An outline of one possible algorithm is:
     * <pre>
     * // Generate headers ...
     * for (ProfileResult res : profile) {
     *      for (ProfileColumn pc : ProfileColumn.values()) {
     *          switch (pc) {
     *              // format each column here
     *          }
     *      }
     * }
     * </pre>
     * 
     * @param out   The file to write to.
     * @param profile The list of SQL Tables
     * @param pm    The ProfileManager which generated this Profile
     */
    public void format(OutputStream out, List<ProfileResult> profile) throws Exception;
}
