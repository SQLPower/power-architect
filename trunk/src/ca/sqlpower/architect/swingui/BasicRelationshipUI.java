package ca.sqlpower.architect.swingui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.geom.*;
import org.apache.log4j.Logger;

/**
 * The BasicRelationshipUI is responsible for drawing the lines
 * between tables.  Subclasses decorate the ends of the lines.
 */
public class BasicRelationshipUI extends RelationshipUI
	implements PropertyChangeListener, java.io.Serializable {

	private static Logger logger = Logger.getLogger(BasicRelationshipUI.class);

	protected Relationship relationship;

	protected int orientation;

	protected Rectangle computedBounds;

	/**
	 * This is the path that the relationship line follows.
	 */
	protected GeneralPath path;

	protected Color selectedColor = new Color(204, 204, 255);
	protected Color unselectedColor = Color.black;
	protected BasicStroke nonIdStroke = new BasicStroke(1.0f);
	protected BasicStroke idStroke = new BasicStroke(1.0f);

	/**
	 * Points within radius pixels of this relationship's visible path
	 * are considered to be contained within this component.
	 *
	 * @see #contains
	 */
	protected int radius = 4;

	public static final int NO_FACING_EDGES = 0;
	public static final int PARENT_FACES_RIGHT = 1;
	public static final int PARENT_FACES_LEFT = 2;
	public static final int PARENT_FACES_BOTTOM = 4;
	public static final int PARENT_FACES_TOP = 8;
	public static final int CHILD_FACES_RIGHT = 16;
	public static final int CHILD_FACES_LEFT = 32;
	public static final int CHILD_FACES_BOTTOM = 64;
	public static final int CHILD_FACES_TOP = 128;

	// ------------------------ ComponentUI methods ------------------------

	public static ComponentUI createUI(JComponent c) {
		logger.debug("Creating new BasicRelationshipUI for "+c);
        return new BasicRelationshipUI();
    }

    public void installUI(JComponent c) {
		logger.debug("Installing BasicRelationshipUI on "+c);
		relationship = (Relationship) c;
		relationship.addPropertyChangeListener(this);
    }

    public void uninstallUI(JComponent c) {
		relationship = (Relationship) c;
		relationship.removePropertyChangeListener(this);
    }
	
	/**
	 * @param g The graphics to paint on.  It should be in the
	 * coordinate space of the containing playpen.
	 */
    public void paint(Graphics g, JComponent c) {
		logger.debug("BasicRelationshipUI is painting");
		Relationship r = (Relationship) c;
		Graphics2D g2 = (Graphics2D) g;

		g2.translate(c.getX() * -1, c.getY() * -1); // playpen coordinate space

		if (logger.isDebugEnabled()) {
			g2.setColor(c.getBackground());
			Rectangle bounds = c.getBounds();
			g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g2.setColor(c.getForeground());
		}

		try {
			Point pktloc = r.getPkConnectionPoint();
			Point start = new Point(pktloc.x + r.getPkTable().getLocation().x,
									pktloc.y + r.getPkTable().getLocation().y);
			Point fktloc = r.getFkConnectionPoint();
			Point end = new Point(fktloc.x + r.getFkTable().getLocation().x,
								  fktloc.y + r.getFkTable().getLocation().y);
			
			// XXX: could optimise by checking if PK or FK tables have moved
			if (path == null) {
				path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
			} else {
				path.reset();
			}

			if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0
				 && (orientation & (CHILD_FACES_LEFT | CHILD_FACES_RIGHT)) != 0) {
				int midx = (Math.abs(end.x - start.x) / 2) + Math.min(start.x, end.x);
				path.moveTo(start.x, start.y);
				path.lineTo(midx, start.y);
				path.lineTo(midx, end.y);
				path.lineTo(end.x, end.y);
			} else if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0
						&& (orientation & (CHILD_FACES_TOP | CHILD_FACES_BOTTOM)) != 0) {
				int midy = (Math.abs(end.y - start.y) / 2) + Math.min(start.y, end.y);
				path.moveTo(start.x, start.y);
				path.lineTo(start.x, midy);
				path.lineTo(end.x, midy);
				path.lineTo(end.x, end.y);
			} else if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0) {
				path.moveTo(start.x, start.y);
				path.lineTo(end.x, start.y);
				path.lineTo(end.x, end.y);
			} else if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0) {
				path.moveTo(start.x, start.y);
				path.lineTo(start.x, end.y);
				path.lineTo(end.x, end.y);
			} else {
				// unknown case: draw straight line.
				path.moveTo(start.x, start.y);
				path.lineTo(end.x, end.y);
			}
			
			if (r.isSelected()) {
				g2.setColor(selectedColor);
			} else {
				g2.setColor(unselectedColor);
			}

			Stroke oldStroke = g2.getStroke();
			
			if (relationship.getModel().isIdentifying()) {
				g2.setStroke(getIdentifyingStroke());
			} else {
				g2.setStroke(getNonIdentifyingStroke());
			}

			g2.draw(path);
			logger.debug("Drew path "+path);

			g2.setStroke(oldStroke);
			paintTerminations(g2, start, end, orientation);
		} finally {
			g2.translate(c.getX(), c.getY()); // playpen coordinate space
		}
	}

	public boolean contains(JComponent c, int x, int y) {
		if (path == null) {
			return false;
		} else {
			Point loc = relationship.getLocation();
			return path.intersects(x - radius + loc.x, y - radius + loc.y,
								   radius*2,           radius*2);
		}
	}

	// ------------------ Custom methods ---------------------
	
	/**
	 * Paints red dots.  Subclasses will implement real notation.
	 */
	protected void paintTerminations(Graphics2D g2, Point start, Point end, int orientation) {
		Color oldColor = g2.getColor();
		g2.setColor(Color.red);
		g2.fillOval(start.x - 2, start.y - 2, 4, 4);
		g2.fillOval(end.x - 2, end.y - 2, 4, 4);
		g2.setColor(oldColor);
	}

	public void bestConnectionPoints(TablePane tp1, TablePane tp2,
									 Point tp1point, Point tp2point) {
		Rectangle tp1b = tp1.getBounds();
		Rectangle tp2b = tp2.getBounds();
		orientation = getFacingEdges(tp1, tp2);
		if ( (orientation & PARENT_FACES_TOP) != 0) {
			tp1point.move(tp1b.width/2, 0);
		} else if ( (orientation & PARENT_FACES_RIGHT) != 0) {
			tp1point.move(tp1b.width, tp1b.height/2);
		} else if ( (orientation & PARENT_FACES_BOTTOM) != 0) {
			tp1point.move(tp1b.width/2, tp1b.height);
		} else if ( (orientation & PARENT_FACES_LEFT) != 0) {
			tp1point.move(0, tp1b.height/2);
		} else {
			logger.error("Unrecognised parent orientation");
		}

		if ( (orientation & CHILD_FACES_TOP) != 0) {
			tp2point.move(tp2b.width/2, 0);
		} else if ( (orientation & CHILD_FACES_RIGHT) != 0) {
			tp2point.move(tp2b.width, tp2b.height/2);
		} else if ( (orientation & CHILD_FACES_BOTTOM) != 0) {
			tp2point.move(tp2b.width/2, tp2b.height);
		} else if ( (orientation & CHILD_FACES_LEFT) != 0) {
			tp2point.move(0, tp2b.height/2);
		} else {
			logger.error("Unrecognised child orientation");
		}
	}

	public Point closestEdgePoint(TablePane tp, Point p) {
		Dimension tpsize = tp.getSize();

		// clip point p to inside of tp
		Point bcp = new Point(Math.max(0, Math.min(tpsize.width, p.x)),
							  Math.max(0, Math.min(tpsize.height, p.y)));
		
		boolean adjustX = bcp.y != 0 && bcp.y != tpsize.height;
		boolean adjustY = bcp.x != 0 && bcp.x != tpsize.width;

		// push x-coordinate to left or right edge of tp, if y-coord is inside tp
		if (adjustX) {
			if (bcp.x < (tpsize.width/2)) {
				bcp.x = 0;
			} else {
				bcp.x = tpsize.width;
			}
		}
		
		// push y-coordinate to top or bottom edge of tp, if x-coord is inside tp
		if (adjustY) {
			if (bcp.y < (tpsize.height/2)) {
				bcp.y = 0;
			} else {
				bcp.y = tpsize.height;
			}
		}

		return bcp;
	}

	protected int getFacingEdges(TablePane parent, TablePane child) {
		Rectangle parentb = parent.getBounds();
		Rectangle childb = child.getBounds();
		int tl = getTerminationLength();

		if (parentb.x-tl >= childb.x+childb.width+tl) {
			return PARENT_FACES_LEFT | CHILD_FACES_RIGHT;
		} else if (parentb.x+parentb.width+tl <= childb.x-tl) {
			return PARENT_FACES_RIGHT | CHILD_FACES_LEFT;
		} else if (parentb.y-tl >= childb.y+childb.height+tl) {
			return PARENT_FACES_TOP | CHILD_FACES_BOTTOM;
		} else if (parentb.y+parentb.height+tl <= childb.y-tl) {
			return PARENT_FACES_BOTTOM | CHILD_FACES_TOP;
		} else if (parentb.y >= childb.y+(childb.height/2)) {
			if (parentb.x+(parentb.width/2) < childb.x) {
				return PARENT_FACES_TOP | CHILD_FACES_LEFT;
			} else {
				return PARENT_FACES_TOP | CHILD_FACES_RIGHT;
			}
		} else if (parentb.y+parentb.height <= childb.y+(childb.height/2)) {
			if (parentb.x+(parentb.width/2) < childb.x) {
				return PARENT_FACES_BOTTOM | CHILD_FACES_LEFT;
			} else {
				return PARENT_FACES_BOTTOM | CHILD_FACES_RIGHT;
			}
			// xxx: two more conditions!
		} else {
			return NO_FACING_EDGES;
		}
	}

	/**
	 * The distance that the termination extends perpendicularly away
	 * from the table's edge.  If the parent and child terminations
	 * are different sizes, this returns the maximum of the two.
	 */
	public int getTerminationLength() {
		return 5;
	}

	/**
	 * The distance that the termination extends parallel to the
	 * table's edge.  If the parent and child terminations are
	 * different sizes, this returns the maximum of the two.
	 */
	public int getTerminationWidth() {
		return 5;
	}

	public Stroke getIdentifyingStroke() {
		return idStroke;
	}

	public Stroke getNonIdentifyingStroke() {
		return nonIdStroke;
	}

	/**
	 * Figures out if the current orientation is legal, given the
	 * current pkTable and fkTable positions.
	 *
	 * <p>XXX: answers false sometimes when true would be more
	 * correct.  A more sophisticated implementation is warranted when
	 * time allows.
	 */
	public boolean isOrientationLegal() {
		return orientation == getFacingEdges(relationship.pkTable, relationship.fkTable);
	}

	protected void computeBounds() {
		// XXX: should check for valid cached bounds before recomputing!

		TablePane pkTable = relationship.pkTable;
		TablePane fkTable = relationship.fkTable;

		if (!isOrientationLegal()) {
			// bestConnectionPoints also updates orientation as a side effect
			bestConnectionPoints(pkTable, fkTable,
								 relationship.pkConnectionPoint,  // in pktable-space
								 relationship.fkConnectionPoint); // in fktable-space
		}

		Point pkLimits = new Point(relationship.pkConnectionPoint);
		pkLimits.translate(pkTable.getX(), pkTable.getY());
		Point fkLimits = new Point(relationship.fkConnectionPoint);
		fkLimits.translate(fkTable.getX(), fkTable.getY());

		if (logger.isDebugEnabled()) {
			logger.debug("Absolute connection points: pk="+pkLimits+"; fk="+fkLimits);
		}

		// make room for parent decorations
		if ( (orientation & (PARENT_FACES_RIGHT | PARENT_FACES_LEFT)) != 0) {
			if (pkLimits.y >= fkLimits.y) {
				pkLimits.y += getTerminationWidth();
			} else {
				pkLimits.y -= getTerminationWidth();
			}
		} else {
			if (pkLimits.x >= fkLimits.x) {
				pkLimits.x += getTerminationWidth();
			} else {
				pkLimits.x -= getTerminationWidth();
			}
		}

		// make room for child decorations
		if ( (orientation & (CHILD_FACES_RIGHT | CHILD_FACES_LEFT)) != 0) {
			if (fkLimits.y <= relationship.pkConnectionPoint.y + pkTable.getY()) {
				fkLimits.y -= getTerminationWidth();
			} else {
				fkLimits.y += getTerminationWidth();
			}
		} else {
			if (fkLimits.x <= relationship.pkConnectionPoint.x + pkTable.getX()) {
				fkLimits.x -= getTerminationWidth();
			} else {
				fkLimits.x += getTerminationWidth();
			}
		}

		if (logger.isDebugEnabled()) logger.debug("Limits: pk="+pkLimits+"; fk="+fkLimits);

		Point topLeft = new Point(Math.min(pkLimits.x,
										   fkLimits.x),
								  Math.min(pkLimits.y,
										   fkLimits.y));
		Point bottomRight = new Point(Math.max(pkLimits.x,
											   fkLimits.x),
									  Math.max(pkLimits.y,
											   fkLimits.y));
		computedBounds = new Rectangle(topLeft.x, topLeft.y,
									   bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		if (logger.isDebugEnabled()) {
			logger.debug("Updating bounds to "+computedBounds
						 +" (topleft="+topLeft+"; bottomRight="+bottomRight+")");
		}
	}

	public Dimension getPreferredSize(JComponent c) {
		computeBounds();
		if (logger.isDebugEnabled()) {
			logger.debug("[31mComputed size is ["+computedBounds.width+","+computedBounds.height+"][0m");
		}
		return new Dimension(computedBounds.width, computedBounds.height);
	}

	public Point getPreferredLocation() {
		computeBounds();
		if (logger.isDebugEnabled()) {
			logger.debug("[31mComputed locn is ["+computedBounds.x+","+computedBounds.y+"][0m");
		}
		return new Point(computedBounds.x, computedBounds.y);
	}

	/**
	 * Determines if the given point (in this Relationship's
	 * co-ordinates) is in the region defined as the primary key
	 * decoration.  This is useful for determining the behaviour of
	 * clicks and drags.
	 */
	public boolean isOverPkDecoration(Point p) {
		Point pkDec = new Point
		 (relationship.pkConnectionPoint.x + relationship.pkTable.getX() - relationship.getX(),
		  relationship.pkConnectionPoint.y + relationship.pkTable.getY() - relationship.getY());
		if (logger.isDebugEnabled()) logger.debug("p="+p+"; pkDec = "+pkDec);
		return ASUtils.distance(p, pkDec) < Math.max(getTerminationWidth(), getTerminationLength());
	}

	/**
	 * Determines if the given point (in this Relationship's
	 * co-ordinates) is in the region defined as the primary key
	 * decoration.  This is useful for determining the behaviour of
	 * clicks and drags.
	 */
	public boolean isOverFkDecoration(Point p) {
		Point fkDec = new Point
		 (relationship.fkConnectionPoint.x + relationship.fkTable.getX() - relationship.getX(),
		  relationship.fkConnectionPoint.y + relationship.fkTable.getY() - relationship.getY());
		return ASUtils.distance(p, fkDec) < Math.max(getTerminationWidth(), getTerminationLength());	}

	// --------------- PropertyChangeListener ----------------------
	public void propertyChange(PropertyChangeEvent e) {
		logger.debug("BasicRelationshipUI notices change of "+e.getPropertyName()
					 +" from "+e.getOldValue()+" to "+e.getNewValue()+" on "+e.getSource());
	}
}
