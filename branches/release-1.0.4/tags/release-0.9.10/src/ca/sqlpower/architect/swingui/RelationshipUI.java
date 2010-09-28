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
package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

public abstract class RelationshipUI implements PlayPenComponentUI, java.io.Serializable {
	public static final String UI_CLASS_ID = "RelationshipUI";

    /**
     * A bitmask of the constants (PARENT|CHILD)_FACES_(LEFT|RIGHT|TOP|BOTTOM).
     */
    protected int orientation;

    public static final int NO_FACING_EDGES = 0;
    public static final int PARENT_FACES_RIGHT = 1;
    public static final int PARENT_FACES_LEFT = 2;
    public static final int PARENT_FACES_BOTTOM = 4;
    public static final int PARENT_FACES_TOP = 8;
    public static final int CHILD_FACES_RIGHT = 16;
    public static final int CHILD_FACES_LEFT = 32;
    public static final int CHILD_FACES_BOTTOM = 64;
    public static final int CHILD_FACES_TOP = 128;
    public static final int CHILD_MASK = 240;
    public static final int PARENT_MASK = 15;


	public RelationshipUI() {
		pkConnectionPoint = new Point();
		fkConnectionPoint = new Point();
	}

	/**
	 * Adjusts the UI's connection points to the default "best" position.
	 */
	public abstract void bestConnectionPoints();

	public abstract Point closestEdgePoint(boolean onPkTable, Point p);

	public abstract Point getPreferredLocation();

	public abstract boolean isOverPkDecoration(Point p);
	public abstract boolean isOverFkDecoration(Point p);

	/**
	 * The minimum number of pixels for a "kink" in the relationship
	 * line.  If the kink would be smaller than snapRadius, it snaps
	 * to a straight line.
	 */
	protected int snapRadius = 4;
	
	public int getSnapRadius() {
		return snapRadius;
	}

	public void setSnapRadius(int v) {
		snapRadius = v;
	}

	/**
	 * This is the point where this relationship meets its PK table.
	 * The point is in the table's coordinate space.
	 */
	protected Point pkConnectionPoint;

	public void setPkConnectionPoint(Point p) {
		pkConnectionPoint = p;
	}

	public Point getPkConnectionPoint() {
		return pkConnectionPoint;
	}
	
	public void setOrientation(int o) {
	    this.orientation = o;
	}

	/**
	 * This is the point where this relationship meets its FK table.
	 * The point is in the table's coordinate space.
	 */
	protected Point fkConnectionPoint;

	public void setFkConnectionPoint(Point p) {
		fkConnectionPoint = p;
	}

	public Point getFkConnectionPoint() {
		return fkConnectionPoint;
	}

    /**
     * Determines if the given rectangle is visibly touching this component.
     * 
     * @param region The region to test.
     * @return Whether or not this Relationship visibly intersects the given region
     */
	public abstract boolean intersects(Rectangle region);
	
	/**
	 * Determines if the given point touches the visible part of this relationship.
	 */
	public abstract boolean contains(Point p);
	
	/**
	 * Returns true iff this relationship's visible line intersects any part
	 * of the given shape.
	 * 
	 * @param s The shape to test for intersection with.
	 * @return Whether or not this relationship intersects <tt>s</tt>.
	 */
	public abstract boolean intersectsShape(Shape s);

	/**
	 * Returns the visible shape of this relationship's line.
	 */
	public abstract Shape getShape();
	
	public abstract int getShapeLength();
    
    /**
     * Returns the current orientation of this relationship; that is, which
     * sides of its PK table and its FK table it is attached to. The return
     * value is a bitmask of the constants
     * (PARENT|CHILD)_FACES_(LEFT|RIGHT|TOP|BOTTOM).
     */
    public int getOrientation() {
        return orientation;
    }
}
