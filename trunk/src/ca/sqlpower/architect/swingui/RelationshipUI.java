package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

public abstract class RelationshipUI implements PlayPenComponentUI, java.io.Serializable {
	public static final String UI_CLASS_ID = "RelationshipUI";

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
}
