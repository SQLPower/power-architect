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

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

/**
 * A collection of static utility methods for working with the OLAP classes.
 */
public class OLAPUtil {

    private OLAPUtil() {
        throw new AssertionError("Don't instantiate this class");
    }

    /**
     * Finds the OLAPSession that owns the given OLAPObject, by following
     * parent pointers successively until an ancestor of type OLAPSession
     * is encountered. If there is no OLAPSession ancestor, returns null.
     * <p>
     * Note that when we say <i>ancestor</i> in this doc comment, we include
     * the given <code>oo</code> object as an "ancestor" of itself.  In other
     * words, if you pass in an OLAPSession, you will get that same object back.
     * 
     * @param oo The object to start searching at
     * @return The nearest ancestor of type OLAPObject, or null if there is
     * no such ancestor.
     */
    public static OLAPSession getSession(OLAPObject oo) {
        while (oo != null && !(oo instanceof OLAPSession)) {
            oo = oo.getParent();
        }
        return (OLAPSession) oo;
    }

    /**
     * Mondrian uses a single string as a table name qualifier, so this method
     * helps by finding the parent schema and/or catalog and produces the correct
     * Mondrian qualifier for the given table.
     */
    public static String getQualifier(SQLTable t) {
        SQLSchema schema = t.getSchema();
        SQLCatalog catalog = t.getCatalog();
        if (catalog == null && schema == null) {
            return null;
        } else if (schema == null) {
            return catalog.getName();
        } else if (catalog == null) {
            return schema.getName();
        } else {
            return catalog.getName() + "." + schema.getName();
        }
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
