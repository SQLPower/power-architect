package ca.sqlpower.architect.swingui;

import javax.swing.plaf.ComponentUI;
import ca.sqlpower.architect.ArchitectException;
import java.awt.Point;

public abstract class RelationshipUI extends ComponentUI implements java.io.Serializable {
	public static final String UI_CLASS_ID = "RelationshipUI";

	public abstract void bestConnectionPoints(TablePane tp1, TablePane tp2,
											  Point tp1point, Point tp2point);

	public abstract Point closestEdgePoint(TablePane tp, Point p);

	public abstract Point getPreferredLocation();

	public abstract boolean isOverPkDecoration(Point p);
	public abstract boolean isOverFkDecoration(Point p);
}
