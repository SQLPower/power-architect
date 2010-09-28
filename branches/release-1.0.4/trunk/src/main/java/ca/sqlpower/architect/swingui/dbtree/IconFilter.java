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

package ca.sqlpower.architect.swingui.dbtree;

import javax.swing.Icon;

import ca.sqlpower.object.SPObject;

/**
 * This interface is a hook into the DBTreeCellRenderer that allows clients
 * to modify or substitute the icon displayed for every node in the DB Tree.
 * <p>
 * Implementing classes are given the icon that the renderer would have used
 * in the absence of filters, and the SQLObject that is represented by the
 * icon. The filter may then either return the original icon as-is, return a
 * different icon, or return the original icon wrapped in some way (for instance,
 * composing the original icon with a decoration that indicates some difference
 * in state such as an error or warning condition, or a greyed-out version of
 * the icon to indicate the object is disabled or unavailable).
 */
public interface IconFilter {

    /**
     * Returns an icon to use in place of the given icon, or returns the given
     * icon if no substitution is necessary.
     * 
     * @param original
     *            The icon that would have been used if this filter did not
     *            exist. If there was not going to be an icon on the given node,
     *            this parameter will be null.
     * @param node
     *            The tree node the icon applies to.
     * @return The icon that should be used instead of original, or
     *         <code>null</code> if the tree node should have no icon at all.
     */
    Icon filterIcon(Icon original, SPObject node);
    
}
