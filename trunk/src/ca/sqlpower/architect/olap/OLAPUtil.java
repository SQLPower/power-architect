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

import java.beans.PropertyChangeListener;

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
     * Returns the first cube that has the given name, or null if no such cube
     * has that name.
     * 
     * @param parent
     *            The schema to search.
     * @param name
     *            The cube name to search for. Must not be null.
     * @return The first cube having the given name; null if no cube has that
     *         name.
     */
    public static Cube findCube(Schema parent, String name) {
        for (Cube c : parent.getCubes()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Adds the given OLAPChildListener and optional PropertyChangeListener to
     * the given root object and all of its descendants.
     * 
     * @param ocl
     *            The OLAPChildListener to add to the subtree of OLAPObjects
     *            rooted at root.
     * @param pcl
     *            The PropertyChangeListener to add to the subtree of
     *            OLAPObjects rooted at root. If you don't want to know about
     *            property changes to the nodes themselves, you can leave this
     *            parameter as null.
     */
    public static void listenToHierarchy(OLAPObject root, OLAPChildListener ocl, PropertyChangeListener pcl) {
        root.addChildListener(ocl);
        if (pcl != null) {
            root.addPropertyChangeListener(pcl);
        }
        for (OLAPObject child : root.getChildren()) {
            listenToHierarchy(child, ocl, pcl);
        }
    }

    /**
     * Removes the given OLAPChildListener and optional PropertyChangeListener
     * from the given root object and all of its descendants.
     * 
     * @param ocl
     *            The OLAPChildListener to remove from the subtree of
     *            OLAPObjects rooted at root. It is not an error if the listener
     *            is not registered with any or all of the objects in the
     *            subtree, so it's safe to call this with the root of the tree
     *            if you want.
     * @param pcl
     *            The PropertyChangeListener to add to the subtree of
     *            OLAPObjects rooted at root. It is not an error if the listener
     *            is not registered with any or all of the objects in the
     *            subtree, so it's safe to call this with the root of the tree
     *            if you want. If you weren't listening for property change events,
     *            you can leave this parameter as null. Note that this parameter
     *            is pronounced "pockle," not "pickle."
     */
    public static void unlistenToHierarchy(OLAPObject root, OLAPChildListener ocl, PropertyChangeListener pcl) {
        root.removeChildListener(ocl);
        if (pcl != null) {
            root.removePropertyChangeListener(pcl);
        }
        for (OLAPObject child : root.getChildren()) {
            unlistenToHierarchy(child, ocl, pcl);
        }
    }
}
