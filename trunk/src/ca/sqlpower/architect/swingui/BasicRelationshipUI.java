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
	 * This is the path that the relationship line follows.  Don't 
	 * use it for contains() and intersects() becuase
	 * it is closed by a diagonal line from start to finish.
	 * 
	 * @see containmentPath
	 */
	protected GeneralPath path;
	
	/**
	 * This is a closed path for use with contains() and intersects().
	 */
	protected GeneralPath containmentPath;

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
			Point pktloc = pkConnectionPoint;
			Point start = new Point(pktloc.x + r.getPkTable().getLocation().x,
									pktloc.y + r.getPkTable().getLocation().y);
			Point fktloc = fkConnectionPoint;
			Point end = new Point(fktloc.x + r.getFkTable().getLocation().x,
								  fktloc.y + r.getFkTable().getLocation().y);
			
			// XXX: could optimise by checking if PK or FK tables have moved
			containmentPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 10);

			if (relationship.getPkTable() == relationship.getFkTable()) {
				// special case hack for self-referencing table
				// assume orientation is PARENT_FACES_BOTTOM | CHILD_FACES_LEFT
				containmentPath.moveTo(start.x, start.y);
				containmentPath.lineTo(start.x, start.y + getTerminationLength() * 2);
				containmentPath.lineTo(end.x - getTerminationLength() * 2, start.y + getTerminationLength() * 2);
				containmentPath.lineTo(end.x - getTerminationLength() * 2, end.y);
				containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);
				
				containmentPath.lineTo(end.x - getTerminationLength() * 2, end.y);
				containmentPath.lineTo(end.x - getTerminationLength() * 2, start.y + getTerminationLength() * 2);
				containmentPath.lineTo(start.x, start.y + getTerminationLength() * 2);
			} else if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0
				 && (orientation & (CHILD_FACES_LEFT | CHILD_FACES_RIGHT)) != 0) {
				int midx = (Math.abs(end.x - start.x) / 2) + Math.min(start.x, end.x);
				containmentPath.moveTo(start.x, start.y);
				containmentPath.lineTo(midx, start.y);
				containmentPath.lineTo(midx, end.y);
				containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				containmentPath.lineTo(midx, end.y);
				containmentPath.lineTo(midx, start.y);
				containmentPath.moveTo(start.x, start.y);
			} else if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0
						&& (orientation & (CHILD_FACES_TOP | CHILD_FACES_BOTTOM)) != 0) {
				int midy = (Math.abs(end.y - start.y) / 2) + Math.min(start.y, end.y);
				containmentPath.moveTo(start.x, start.y);
				containmentPath.lineTo(start.x, midy);
				containmentPath.lineTo(end.x, midy);
				containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				// now retrace our steps so the shape doesn't autoclose with a straight line from finish to start
				containmentPath.lineTo(end.x, midy);
				containmentPath.lineTo(start.x, midy);
				containmentPath.moveTo(start.x, start.y);
			} else if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0) {
				containmentPath.moveTo(start.x, start.y);
				containmentPath.lineTo(end.x, start.y);
				containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				// now retrace our steps so the shape doesn't autoclose with a straight line from finish to start
				containmentPath.lineTo(end.x, start.y);
				containmentPath.moveTo(start.x, start.y);
			} else if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0) {
				containmentPath.moveTo(start.x, start.y);
				containmentPath.lineTo(start.x, end.y);
				containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				// now retrace our steps so the shape doesn't autoclose with a straight line from finish to start
				containmentPath.lineTo(start.x, end.y);
				containmentPath.moveTo(start.x, start.y);
			} else {
				// unknown case: draw straight line.
				containmentPath.moveTo(start.x, start.y);
				containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);
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
			if (logger.isDebugEnabled()) logger.debug("Drew path "+path);

			g2.setStroke(oldStroke);
			paintTerminations(g2, start, end, orientation);
		} finally {
			g2.translate(c.getX(), c.getY()); // playpen coordinate space
		}
	}

	public boolean contains(JComponent c, int x, int y) {
		if (containmentPath == null) {
			return false;
		} else {
			Point loc = relationship.getLocation();
			return containmentPath.intersects(x - radius + loc.x, y - radius + loc.y,
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

	/**
	 * Adjusts the UI's connection points to the default "best" position.
	 */
	public void bestConnectionPoints() {
		Rectangle pktBounds = relationship.getPkTable().getBounds();
		Rectangle fktBounds = relationship.getFkTable().getBounds();

		if (relationship.getPkTable() == relationship.getFkTable()) {
			// self-referencing table
			orientation = PARENT_FACES_BOTTOM | CHILD_FACES_LEFT;
			pkConnectionPoint.move(pktBounds.width/2, pktBounds.height);
			fkConnectionPoint.move(0, fktBounds.height/2);
			logger.debug("[[33mSelf-referencing table: set connection points pk="
						 +pkConnectionPoint+"; fk="+fkConnectionPoint+"[0m");
		} else {
			// distinct tables ("normal" case)
			orientation = getFacingEdges(relationship.getPkTable(), relationship.getFkTable());
			
			if ( (orientation & PARENT_FACES_TOP) != 0) {
				pkConnectionPoint.move(pktBounds.width/2, 0);
			} else if ( (orientation & PARENT_FACES_RIGHT) != 0) {
				pkConnectionPoint.move(pktBounds.width, pktBounds.height/2);
			} else if ( (orientation & PARENT_FACES_BOTTOM) != 0) {
				pkConnectionPoint.move(pktBounds.width/2, pktBounds.height);
			} else if ( (orientation & PARENT_FACES_LEFT) != 0) {
				pkConnectionPoint.move(0, pktBounds.height/2);
			} else {
				logger.error("Unrecognised parent orientation");
			}
			
			if ( (orientation & CHILD_FACES_TOP) != 0) {
				fkConnectionPoint.move(fktBounds.width/2, 0);
			} else if ( (orientation & CHILD_FACES_RIGHT) != 0) {
				fkConnectionPoint.move(fktBounds.width, fktBounds.height/2);
			} else if ( (orientation & CHILD_FACES_BOTTOM) != 0) {
				fkConnectionPoint.move(fktBounds.width/2, fktBounds.height);
			} else if ( (orientation & CHILD_FACES_LEFT) != 0) {
				fkConnectionPoint.move(0, fktBounds.height/2);
			} else {
				logger.error("Unrecognised child orientation");
			}
		}
	}

	/**
	 * Returns the closest point to <code>p</code> which is along an
	 * edge of either the PK table (onPkTable true) or the FK table
	 * (onPkTable false).
	 */
	public Point closestEdgePoint(boolean onPkTable, Point p) {
		TablePane tp = onPkTable ? relationship.getPkTable() : relationship.getFkTable();
		Dimension tpsize = tp.getSize();
		Point ep; // this is the return value (edge point), set in one of the cases below
		Point sp; // this is the stationary point at the non-moving end of the relationship

		if (onPkTable) {
			sp = new Point(relationship.getFkTable().getLocation());
			translatePoint(sp, fkConnectionPoint);
			sp.x -= relationship.getPkTable().getX();
			sp.y -= relationship.getPkTable().getY();

			if ((orientation & PARENT_FACES_LEFT) != 0) {
				ep = new Point(0, Math.max(0, Math.min(tpsize.height, p.y)));
				if (Math.abs(ep.y - sp.y) <= getSnapRadius()) ep.y = sp.y;
			} else if ((orientation & PARENT_FACES_RIGHT)  != 0) {
				ep = new Point(tpsize.width, Math.max(0, Math.min(tpsize.height, p.y)));
				if (Math.abs(ep.y - sp.y) <= getSnapRadius()) ep.y = sp.y;
			} else if ((orientation & PARENT_FACES_TOP)  != 0) {
				ep = new Point(Math.max(0, Math.min(tpsize.width, p.x)), 0);
				if (Math.abs(ep.x - sp.x) <= getSnapRadius()) ep.x = sp.x;
			} else if ((orientation & PARENT_FACES_BOTTOM)  != 0) {
				ep = new Point(Math.max(0, Math.min(tpsize.width, p.x)), tpsize.height);
				if (Math.abs(ep.x - sp.x) <= getSnapRadius()) ep.x = sp.x;
			} else {
				ep = new Point(p);
			}
		} else {
			sp = new Point(relationship.getPkTable().getLocation());
			translatePoint(sp, pkConnectionPoint);
			sp.x -= relationship.getFkTable().getX();
			sp.y -= relationship.getFkTable().getY();

			if ((orientation & CHILD_FACES_LEFT) != 0) {
				ep = new Point(0, Math.max(0, Math.min(tpsize.height, p.y)));
				if (Math.abs(ep.y - sp.y) <= getSnapRadius()) ep.y = sp.y;
			} else if ((orientation & CHILD_FACES_RIGHT)  != 0) {
				ep = new Point(tpsize.width, Math.max(0, Math.min(tpsize.height, p.y)));
				if (Math.abs(ep.y - sp.y) <= getSnapRadius()) ep.y = sp.y;
			} else if ((orientation & CHILD_FACES_TOP)  != 0) {
				ep = new Point(Math.max(0, Math.min(tpsize.width, p.x)), 0);
				if (Math.abs(ep.x - sp.x) <= getSnapRadius()) ep.x = sp.x;
			} else if ((orientation & CHILD_FACES_BOTTOM)  != 0) {
				ep = new Point(Math.max(0, Math.min(tpsize.width, p.x)), tpsize.height);
				if (Math.abs(ep.x - sp.x) <= getSnapRadius()) ep.x = sp.x;
			} else {
				ep = new Point(p);
			}
		}

		return ep;
	}

	/**
	 * Sums the X coordinates of the two arguments and saves the
	 * result in modify.x.  Does the same for the Y coordinates.
	 *
	 * <p>Note that this is similar to Point.translate(int,int) but it
	 * takes a second point as an argument rather than two integers.
	 */
	protected void translatePoint(Point modify, Point noModify) {
		modify.x += noModify.x;
		modify.y += noModify.y;
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
		boolean answer;
		if (relationship.getPkTable() == relationship.getFkTable()) {
			answer = (orientation == (PARENT_FACES_BOTTOM | CHILD_FACES_LEFT));
		} else {
			answer = (orientation == getFacingEdges(relationship.pkTable, relationship.fkTable));
		}
		if (answer == false) {
			logger.debug("[31misOrientationLegal() returning false[0m");
		}
		return answer;
	}

	protected void computeBounds() {
		// XXX: should check for valid cached bounds before recomputing!

		TablePane pkTable = relationship.pkTable;
		TablePane fkTable = relationship.fkTable;

		if (!isOrientationLegal()) {
			// bestConnectionPoints also updates orientation as a side effect
			bestConnectionPoints();
		}

		if (pkTable == fkTable) {
			// hack for supporting self-referencing table
			// assume orientation is PARENT_FACES_BOTTOM | CHILD_FACES_LEFT
			Point topLeft = new Point(fkConnectionPoint.x - getTerminationLength() * 2 - radius,
									  fkConnectionPoint.y - getTerminationWidth());
			Point bottomRight = new Point(pkConnectionPoint.x + getTerminationWidth(),
										  pkConnectionPoint.y + radius + getTerminationLength() * 2);
			computedBounds = new Rectangle(topLeft.x + pkTable.getX(), topLeft.y + pkTable.getY(),
										  bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
			return;
		}
		
		Point pkLimits = new Point(pkConnectionPoint);
		pkLimits.translate(pkTable.getX(), pkTable.getY());
		Point fkLimits = new Point(fkConnectionPoint);
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
			if (fkLimits.y <= pkConnectionPoint.y + pkTable.getY()) {
				fkLimits.y -= getTerminationWidth();
			} else {
				fkLimits.y += getTerminationWidth();
			}
		} else {
			if (fkLimits.x <= pkConnectionPoint.x + pkTable.getX()) {
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
		 (pkConnectionPoint.x + relationship.pkTable.getX() - relationship.getX(),
		  pkConnectionPoint.y + relationship.pkTable.getY() - relationship.getY());
		if (logger.isDebugEnabled()) logger.debug(
		        "p="+p+"; pkDec = "+pkDec+"; width="+relationship.getWidth()+
		        "; height="+relationship.getHeight()+"; orientation="+orientation);
		if ( (orientation & (PARENT_FACES_BOTTOM | PARENT_FACES_TOP)) != 0) {
		    if (p.x < pkDec.x + 5 && p.x > pkDec.x - 5) {
		        if ( (orientation & PARENT_FACES_BOTTOM) != 0) {
		            if ( (orientation & (CHILD_FACES_LEFT | CHILD_FACES_RIGHT)) != 0) {
		                return p.y >= pkDec.y && p.y < relationship.getHeight();
		            } else {
		                return p.y >= pkDec.y && p.y < pkDec.y + (relationship.getHeight() / 2);
		            }
		        } else {
		            if ( (orientation & (CHILD_FACES_LEFT | CHILD_FACES_RIGHT)) != 0) {
		                return p.y <= pkDec.y && p.y > pkDec.y - relationship.getHeight();
		            } else {
		                return p.y <= pkDec.y && p.y > pkDec.y - (relationship.getHeight() / 2);
		            }
		        }
		    } else {
		        return false;
		    }
		} else if ( (orientation & PARENT_FACES_LEFT | PARENT_FACES_RIGHT) != 0) {
		    if (p.y < pkDec.y + 5 && p.y > pkDec.y - 5) {
		        if ( (orientation & PARENT_FACES_LEFT) != 0) {
		            if ( (orientation & (CHILD_FACES_TOP | CHILD_FACES_BOTTOM)) != 0) {
		                return p.x <= pkDec.x && p.x >= pkDec.x - relationship.getWidth();
		            } else {
		                return p.x <= pkDec.x && p.x >= pkDec.x - (relationship.getWidth() / 2);
		            }
		        } else {
		            if ( (orientation & (CHILD_FACES_TOP | CHILD_FACES_BOTTOM)) != 0) {
		                return p.x >= pkDec.x && p.x <= pkDec.x + relationship.getWidth();
		            } else {
		                return p.x >= pkDec.x && p.x <= pkDec.x + (relationship.getWidth() / 2);
		            }
		        }
		    } else {
		        return false;
		    }
		} else {
		    // orientation unknown!
		    return ASUtils.distance(p, pkDec) < Math.max(getTerminationWidth(), getTerminationLength());
		}
	}

	/**
	 * Determines if the given point (in this Relationship's
	 * co-ordinates) is in the region defined as the primary key
	 * decoration.  This is useful for determining the behaviour of
	 * clicks and drags.
	 */
	public boolean isOverFkDecoration(Point p) {
		Point fkDec = new Point
		 (fkConnectionPoint.x + relationship.fkTable.getX() - relationship.getX(),
		  fkConnectionPoint.y + relationship.fkTable.getY() - relationship.getY());
		if ( (orientation & (CHILD_FACES_BOTTOM | CHILD_FACES_TOP)) != 0) {
		    if (p.x < fkDec.x + 5 && p.x > fkDec.x - 5) {
		        if ( (orientation & CHILD_FACES_BOTTOM) != 0) {
		            if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0) {
		                return p.y >= fkDec.y && p.y < fkDec.y + relationship.getHeight();
		            } else {
		                return p.y >= fkDec.y && p.y < fkDec.y + (relationship.getHeight() / 2);
		            }
		        } else {
		            if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0) {
		                return p.y <= fkDec.y && p.y > fkDec.y - relationship.getHeight();
		            } else {
		                return p.y <= fkDec.y && p.y > fkDec.y - (relationship.getHeight() / 2);
		            }
		        }
		    } else {
		        return false;
		    }
		} else if ( (orientation & CHILD_FACES_LEFT | CHILD_FACES_RIGHT) != 0) {
		    if (p.y < fkDec.y + 5 && p.y > fkDec.y - 5) {
		        if ( (orientation & CHILD_FACES_LEFT) != 0) {
		            if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0) {
		                return p.x <= fkDec.x && p.x >= fkDec.x - relationship.getWidth();
		            } else {
		                return p.x <= fkDec.x && p.x >= fkDec.x - (relationship.getWidth() / 2);
		            }
		        } else {
		            if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0) {
		                return p.x >= fkDec.x && p.x <= fkDec.x + relationship.getWidth();
		            } else {
		                return p.x >= fkDec.x && p.x <= fkDec.x + (relationship.getWidth() / 2);
		            }
		        } 
		    } else {
		        return false;
		    }
		} else {
		    // orientation unknown!
			return ASUtils.distance(p, fkDec) < Math.max(getTerminationWidth(), getTerminationLength());
		}
	}

	// --------------- PropertyChangeListener ----------------------
	public void propertyChange(PropertyChangeEvent e) {
		logger.debug("BasicRelationshipUI notices change of "+e.getPropertyName()
					 +" from "+e.getOldValue()+" to "+e.getNewValue()+" on "+e.getSource());
	}

    public boolean intersects(Rectangle region) {
        return containmentPath.intersects(region.x, region.y, region.width, region.height);
    }
}
