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
 * An event object that describes adding or removing a single child of the
 * given type to/from a parent.
 */
public class OLAPChildEvent {

    /**
     * The parent that gained or lost a child.
     */
    private final OLAPObject source;
    
    /**
     * The child type for which the parent gained or lost a child (OLAP Objects support
     * multiple child types).
     */
    private final Class<? extends OLAPObject> childType;
    
    /**
     * The child that was added or removed.
     */
    private final OLAPObject child;

    /**
     * The index of the child that was added or removed. This index is the
     * overall position in the list returned by
     * <code>source.getChildren()</code>, not just the position within the
     * separate list of just these children). For example, if the source is a
     * Schema, and the added child is a Cube called newCube, this is the same as
     * <code>schema.getChildren().indexOf(newCube)</code>, not
     * <code>schema.getCubes().indexOf(newCube)</code>.
     */
    private final int index;

    /**
     * Creates a new event object that describes adding or removing a single
     * child of the given type to/from a parent.
     * 
     * @param source
     *            The parent that gained or lost a child.
     * @param childType
     *            The child type for which the parent gained or lost a child
     *            (OLAP Objects support multiple child types).
     * @param child
     *            The child that was added or removed.
     * @param index
     *            The index of the child that was added or removed (indices are
     *            counted separately within each child type).
     */
    public OLAPChildEvent(OLAPObject source, Class<? extends OLAPObject> childType, OLAPObject child, int index) {
        this.source = source;
        this.childType = childType;
        this.child = child;
        this.index = index;
    }

    public OLAPObject getSource() {
        return source;
    }

    public Class<? extends OLAPObject> getChildType() {
        return childType;
    }

    public OLAPObject getChild() {
        return child;
    }

    public int getIndex() {
        return index;
    }
    
}
