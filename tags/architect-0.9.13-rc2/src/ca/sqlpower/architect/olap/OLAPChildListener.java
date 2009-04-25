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

/**
 * Interface for classes that want to know when the list of children of an
 * OLAPObject is modified.
 */
public interface OLAPChildListener {

    /**
     * Called just after a new child is added to an OLAPObject.
     * 
     * @param e
     *            The event describing the addition. The event's source will be
     *            the parent object which got the new child.
     */
    void olapChildAdded(OLAPChildEvent e);
    
    /**
     * Called just after a child is removed from an OLAPObject.
     * 
     * @param e
     *            The event describing the removal. The event's source will be
     *            the parent object which lost the child.
     */
    void olapChildRemoved(OLAPChildEvent e);
}
