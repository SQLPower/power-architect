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

package ca.sqlpower.architect.olap;

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Schema;

/**
 * A collection of static utility methods for working with the OLAP classes.
 */
public class OLAPUtil {

    private OLAPUtil() {
        throw new AssertionError("Don't instantiate this class");
    }
    
    /**
     * Returns the first cube that has the given name, or null if no such
     * cube has that name.
     * 
     * @param parent The schema to search.
     * @param name The cube name to search for. Must not be null.
     * @return The first cube having the given name; null if no cube has that name.
     */
    public static Cube findCube(Schema parent, String name) {
        for (Cube c : parent.getCubes()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }
}
