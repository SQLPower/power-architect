/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
