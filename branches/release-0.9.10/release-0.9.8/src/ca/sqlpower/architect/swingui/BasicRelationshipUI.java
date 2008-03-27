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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The BasicRelationshipUI is responsible for drawing the lines
 * between tables.  Subclasses decorate the ends of the lines.
 */
public class BasicRelationshipUI extends RelationshipUI
	implements PropertyChangeListener, java.io.Serializable {

	private static Logger logger = Logger.getLogger(BasicRelationshipUI.class);

	protected Relationship relationship;

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

	// ------------------------ ComponentUI methods ------------------------

	public static PlayPenComponentUI createUI(PlayPenComponent c) {
		logger.debug("Creating new BasicRelationshipUI for "+c);
        return new BasicRelationshipUI();
    }

    public void installUI(PlayPenComponent c) {
		logger.debug("Installing BasicRelationshipUI on "+c);
		relationship = (Relationship) c;
		relationship.addPropertyChangeListener(this);
    }

    public void uninstallUI(PlayPenComponent c) {
		relationship = (Relationship) c;
		relationship.removePropertyChangeListener(this);
    }
	public void revalidate() {
		Rectangle OldBounds = computedBounds;
		Rectangle bounds = computeBounds();
		if (!computedBounds.equals(OldBounds)) {
			relationship.setBounds(bounds.x,bounds.y,bounds.width,bounds.height);
		}
	}
    public void paint(Graphics2D g2) {
		paint(g2,relationship);
	}
	/**
	 * @param g The graphics to paint on.  It should be in the
	 * coordinate space of the containing playpen.
	 */
    public void paint(Graphics g, PlayPenComponent c) {
		logger.debug("BasicRelationshipUI is painting");
		Relationship r = (Relationship) c;
		Graphics2D g2 = (Graphics2D) g;

		if (g2 == null)
		{
			throw new NullPointerException("Graphics g2 is null");
		}
		if (c == null)
		{
			throw new NullPointerException("Relationship c is null");
		}
			
		g2.translate(c.getX() * -1, c.getY() * -1); // playpen coordinate space

		if (logger.isDebugEnabled()) {
			g2.setColor(c.getBackground());
			Rectangle bounds = c.getBounds();
			g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			logger.debug("Relationship bounds " + bounds);
			g2.setColor(c.getForeground());
		}

		try {
			Point pktloc = pkConnectionPoint;
			Point start = new Point(pktloc.x + r.getPkTable().getLocation().x,
			                        pktloc.y + r.getPkTable().getLocation().y);
            Point lineStart = new Point(start);
            if ((orientation & PARENT_FACES_LEFT) != 0) {
                lineStart.x -= getTerminationLength();
            } else if ((orientation & PARENT_FACES_RIGHT) != 0) {
                lineStart.x += getTerminationLength();
            } else if ((orientation & PARENT_FACES_TOP) != 0) {
                lineStart.y -= getTerminationLength();
            } else if ((orientation & PARENT_FACES_BOTTOM) != 0) {
                lineStart.y += getTerminationLength();
            }
            
			Point fktloc = fkConnectionPoint;
			Point end = new Point(fktloc.x + r.getFkTable().getLocation().x,
			                      fktloc.y + r.getFkTable().getLocation().y);
            Point lineEnd = new Point(end);
            if ((orientation & CHILD_FACES_LEFT) != 0) {
                lineEnd.x -= getTerminationLength();
            } else if ((orientation & CHILD_FACES_RIGHT) != 0) {
                lineEnd.x += getTerminationLength();
            } else if ((orientation & CHILD_FACES_TOP) != 0) {
                lineEnd.y -= getTerminationLength();
            } else if ((orientation & CHILD_FACES_BOTTOM) != 0) {
                lineEnd.y += getTerminationLength();
            }

			// XXX: could optimise by checking if PK or FK tables have moved
			containmentPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 10);

            if (relationship.getPkTable() == relationship.getFkTable()) {
				// special case hack for self-referencing table
				// assume orientation is PARENT_FACES_BOTTOM | CHILD_FACES_LEFT
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
				containmentPath.lineTo(lineStart.x, lineStart.y + getTerminationLength());
				containmentPath.lineTo(lineEnd.x - getTerminationLength(), lineStart.y + getTerminationLength());
				containmentPath.lineTo(lineEnd.x - getTerminationLength(), lineEnd.y);
                containmentPath.lineTo(lineEnd.x, lineEnd.y);
                containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);
				
				containmentPath.lineTo(lineEnd.x - getTerminationLength(), lineEnd.y);
				containmentPath.lineTo(lineEnd.x - getTerminationLength(), lineStart.y + getTerminationLength());
				containmentPath.lineTo(lineStart.x, lineStart.y + getTerminationLength());

            } else if (r.isStraightLine()) {
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
                containmentPath.lineTo(lineEnd.x, lineEnd.y);
                containmentPath.lineTo(end.x, end.y);
                path = new GeneralPath(containmentPath);
			
            } else if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0
				 && (orientation & (CHILD_FACES_LEFT | CHILD_FACES_RIGHT)) != 0) {
				int midx = (Math.abs(lineEnd.x - lineStart.x) / 2) + Math.min(lineStart.x, lineEnd.x);
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
				containmentPath.lineTo(midx, lineStart.y);
				containmentPath.lineTo(midx, lineEnd.y);
				containmentPath.lineTo(lineEnd.x, lineEnd.y);
                containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				containmentPath.lineTo(midx, lineEnd.y);
				containmentPath.lineTo(midx, lineStart.y);
				containmentPath.moveTo(lineStart.x, lineStart.y);
			} else if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0
						&& (orientation & (CHILD_FACES_TOP | CHILD_FACES_BOTTOM)) != 0) {
				int midy = (Math.abs(lineEnd.y - lineStart.y) / 2) + Math.min(lineStart.y, lineEnd.y);
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
				containmentPath.lineTo(lineStart.x, midy);
				containmentPath.lineTo(lineEnd.x, midy);
				containmentPath.lineTo(lineEnd.x, lineEnd.y);
                containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				// now retrace our steps so the shape doesn't autoclose with a straight line from finish to start
				containmentPath.lineTo(lineEnd.x, midy);
				containmentPath.lineTo(lineStart.x, midy);
				containmentPath.moveTo(lineStart.x, lineStart.y);
			} else if ( (orientation & (PARENT_FACES_LEFT | PARENT_FACES_RIGHT)) != 0) {
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
				containmentPath.lineTo(lineEnd.x, lineStart.y);
				containmentPath.lineTo(lineEnd.x, lineEnd.y);
                containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				// now retrace our steps so the shape doesn't autoclose with a straight line from finish to start
				containmentPath.lineTo(lineEnd.x, lineStart.y);
				containmentPath.moveTo(lineStart.x, lineStart.y);
			} else if ( (orientation & (PARENT_FACES_TOP | PARENT_FACES_BOTTOM)) != 0) {
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
				containmentPath.lineTo(lineStart.x, lineEnd.y);
				containmentPath.lineTo(lineEnd.x, lineEnd.y);
                containmentPath.lineTo(end.x, end.y);
				path = new GeneralPath(containmentPath);

				// now retrace our steps so the shape doesn't autoclose with a straight line from finish to start
				containmentPath.lineTo(lineStart.x, lineEnd.y);
				containmentPath.moveTo(lineStart.x, lineStart.y);
			} else {
				// unknown case: draw straight line.
                containmentPath.moveTo(start.x, start.y);
                containmentPath.lineTo(lineStart.x, lineStart.y);
                containmentPath.lineTo(lineEnd.x, lineEnd.y);
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

    public boolean contains(Point p) {
    		return contains(relationship, p.x, p.y);
    }
    
	public boolean contains(PlayPenComponent c, int x, int y) {
		logger.debug("Contains, Looking for " + x + "," + y + " My bounds are: " + relationship.getBounds());
		if (containmentPath == null) {
			return false;
		} else {
			return containmentPath.intersects(x - radius, y - radius, radius*2, radius*2);
		}
	}
	
	public boolean intersects(Rectangle region) {
		if (containmentPath == null) return false;
		else return containmentPath.intersects(region.x, region.y, region.width, region.height);
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
			logger.debug("Self-referencing table: set connection points pk="
						 +pkConnectionPoint+"; fk="+fkConnectionPoint);
		} else {
			// distinct tables ("normal" case)
			Line2D.Double pkToFkBorderLine = calcConnectionPoints(pktBounds, fktBounds);

			orientation = getFacingEdges(pktBounds, fktBounds);

			// make sure the connection points aren't too close to corners
			if ( ((orientation & PARENT_FACES_BOTTOM) != 0) || ((orientation & PARENT_FACES_TOP) != 0) ) {
				pkToFkBorderLine.x1 = Math.max(pktBounds.x + getTerminationWidth(), pkToFkBorderLine.x1);
				pkToFkBorderLine.x1 = Math.min(pktBounds.x + pktBounds.width - getTerminationWidth(), pkToFkBorderLine.x1);
			} else if ( ((orientation & PARENT_FACES_LEFT) != 0) || ((orientation & PARENT_FACES_RIGHT) != 0) ) {
				pkToFkBorderLine.y1 = Math.max(pktBounds.y + getTerminationWidth(), pkToFkBorderLine.y1);
				pkToFkBorderLine.y1 = Math.min(pktBounds.y + pktBounds.height - getTerminationWidth(), pkToFkBorderLine.y1);
			}

			if ( ((orientation & CHILD_FACES_BOTTOM) != 0) || ((orientation & CHILD_FACES_TOP) != 0) ) {
				pkToFkBorderLine.x2 = Math.max(fktBounds.x + getTerminationWidth(), pkToFkBorderLine.x2);
				pkToFkBorderLine.x2 = Math.min(fktBounds.x + fktBounds.width - getTerminationWidth(), pkToFkBorderLine.x2);
			} else if ( ((orientation & CHILD_FACES_LEFT) != 0) || ((orientation & CHILD_FACES_RIGHT) != 0) ) {
				pkToFkBorderLine.y2 = Math.max(fktBounds.y + getTerminationWidth(), pkToFkBorderLine.y2);
				pkToFkBorderLine.y2 = Math.min(fktBounds.y + fktBounds.height - getTerminationWidth(), pkToFkBorderLine.y2);
			}

			pkConnectionPoint.move(
					(int) (pkToFkBorderLine.x1 - pktBounds.x),
					(int) (pkToFkBorderLine.y1 - pktBounds.y));
			
			fkConnectionPoint.move(
					(int) (pkToFkBorderLine.x2 - fktBounds.x),
					(int) (pkToFkBorderLine.y2 - fktBounds.y));
		}
	}

	private Line2D.Double calcConnectionPoints(Rectangle pktBounds, Rectangle fktBounds) {
		Line2D.Double centreToCentreLine =
			new Line2D.Double(pktBounds.getCenterX(),pktBounds.getCenterY(),
							fktBounds.getCenterX(),fktBounds.getCenterY());
		Line2D.Double retval = new Line2D.Double();
		
		List<Point2D.Double> pkTableIntersectPoints = ASUtils.getIntersectPoints(pktBounds,centreToCentreLine);
		List<Point2D.Double> fkTableIntersectPoints = ASUtils.getIntersectPoints(fktBounds,centreToCentreLine);
		
		if (pkTableIntersectPoints.size() == 0) {
			logger.debug("Could not calculate intersection of pk tablepane bound and center line between pk/fk tablepanes, returning the top left corner");
			retval.x1 = pktBounds.x;
			retval.y1 = pktBounds.y;
		} else {
			retval.x1 = pkTableIntersectPoints.get(0).x;
			retval.y1 = pkTableIntersectPoints.get(0).y;
		}

		if (fkTableIntersectPoints.size() == 0) {
			logger.debug("Could not calculate intersection of fk tablepane bound and center line between pk/fk tablepanes, returning the top left corner");
			retval.x2 = fktBounds.x;
			retval.y2 = fktBounds.y;
		} else {
			retval.x2 = fkTableIntersectPoints.get(0).x;
			retval.y2 = fkTableIntersectPoints.get(0).y;
		}
		
		return retval;
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

	protected int getFacingEdges(Rectangle pktBounds, Rectangle fktBounds) {
		Line2D.Double intersectionLine = calcConnectionPoints(pktBounds,fktBounds);
		Point2D.Double pkIntersectPt = new Point2D.Double(intersectionLine.x1,intersectionLine.y1);
		Point2D.Double fkIntersectPt = new Point2D.Double(intersectionLine.x2,intersectionLine.y2);
		
		return getFacingEdges(pktBounds,fktBounds, pkIntersectPt, fkIntersectPt);
	}

	protected int getFacingEdges(Rectangle pktBounds, Rectangle fktBounds,
					Point2D.Double pkIntersectPt, Point2D.Double fkIntersectPt) {

		int retval = 0;
		if (pkIntersectPt.x == pktBounds.x) retval |= PARENT_FACES_LEFT;
		else if (pkIntersectPt.y == pktBounds.y) retval |= PARENT_FACES_TOP;
		else if (pkIntersectPt.x == pktBounds.x + pktBounds.width) retval |= PARENT_FACES_RIGHT;
		else if (pkIntersectPt.y == pktBounds.y + pktBounds.height) retval |= PARENT_FACES_BOTTOM;
		else logger.error(String.format(
				"Unrecognised pktable orientation. pt=(%f,%f); bounds=(%d,%d %dx%d)",
				pkIntersectPt.x, pkIntersectPt.y,
				pktBounds.x, pktBounds.y, pktBounds.width, pktBounds.height));

		if (fkIntersectPt.x == fktBounds.x) retval |= CHILD_FACES_LEFT;
		else if (fkIntersectPt.y == fktBounds.y) retval |= CHILD_FACES_TOP;
		else if (fkIntersectPt.x == fktBounds.x + fktBounds.width) retval |= CHILD_FACES_RIGHT;
		else if (fkIntersectPt.y == fktBounds.y + fktBounds.height) retval |= CHILD_FACES_BOTTOM;
		else logger.error(String.format(
				"Unrecognised fktable orientation. pt=(%f,%f); bounds=(%d,%d %dx%d)",
				fkIntersectPt.x, fkIntersectPt.y,
				fktBounds.x, fktBounds.y, fktBounds.width, fktBounds.height));

		return retval;
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
			answer = (orientation == getFacingEdges(relationship.getPkTable().getBounds(), relationship.getFkTable().getBounds()));
		}
		if (answer == false) {
			logger.debug("[31misOrientationLegal() returning false[0m");
		}
		return answer;
	}

	/**
	 * Compute bounds should only be called by objects in this package or from regress.
	 *
	 */
	protected Rectangle computeBounds() {
		// XXX: should check for valid cached bounds before recomputing!

		TablePane pkTable = relationship.getPkTable();
		TablePane fkTable = relationship.getFkTable();

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
		} else {
			
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
		//relationship.setBounds(computedBounds.x, computedBounds.y, computedBounds.width, computedBounds.height);
		return computedBounds;
	}
	
	public Dimension getPreferredSize() {
		return getPreferredSize(relationship);
	}

	public Dimension getPreferredSize(PlayPenComponent c) {
		//computeBounds();
		if (logger.isDebugEnabled()) {
			logger.debug("[31mComputed size is ["+computedBounds.width+","+computedBounds.height+"][0m");
		}
		return new Dimension(computedBounds.width, computedBounds.height);
	}

	public Point getPreferredLocation() {
		//computeBounds();
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
		 (pkConnectionPoint.x + relationship.getPkTable().getX() - relationship.getX(),
		  pkConnectionPoint.y + relationship.getPkTable().getY() - relationship.getY());
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
		 (fkConnectionPoint.x + relationship.getFkTable().getX() - relationship.getX(),
		  fkConnectionPoint.y + relationship.getFkTable().getY() - relationship.getY());
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

	@Override
	public boolean intersectsShape(Shape s) {
		Rectangle myBounds = path.getBounds();
		Rectangle otherBounds = s.getBounds();
		
		// adjust bounds so that they have at least 1px width and height
		if (myBounds.width == 0) myBounds.width = 1;
		if (myBounds.height == 0) myBounds.height = 1;
		if (otherBounds.width == 0) otherBounds.width = 1;
		if (otherBounds.height == 0) otherBounds.height = 1;
		
		if (logger.isDebugEnabled()) {
			logger.debug("intersectsShape: my rectangle = "+myBounds);
			logger.debug("              other rectangle = "+otherBounds);
			logger.debug("intersectsShape: rectangles overlap? "+myBounds.intersects(otherBounds));
		}
		
		// premature optimization: if my path's box does not intersect s, the lines can't cross
		if (!myBounds.intersects(otherBounds)) return false;
		
		if (logger.isDebugEnabled()) {
			logger.debug("   myPI = "+pathIteratorToString(path.getPathIterator(null)));
			logger.debug("otherPI = "+pathIteratorToString(s.getPathIterator(null)));
		}
		
		List <Point2D.Double> list = getIntersectPoints(s);
		if ( list.size() > 0 )
			return true;
		return false;
	}

	private List<Point2D.Double> getIntersectPoints(Shape s) {
		return ASUtils.getIntersectPoints(this.getShape(),s);
	}
	
	private String pathIteratorToString(PathIterator pathIterator) {
		StringBuffer sb = new StringBuffer();
		float[] coords = new float[6];
		while (!pathIterator.isDone()) {
			int type = pathIterator.currentSegment(coords);
			sb.append("Type: "+type+"; coords: ");
			for (int i = 0; i < coords.length; i++) {
				sb.append(coords[i]).append(',');
			}
			sb.append('\n');
			pathIterator.next();
		}
		return sb.toString();
	}

	

	/**
	 * Returns the actual path that this relationship ui draws.  It will get reset from time
	 * to time as this relationship (or its connected tables) gets moved by the user.
	 */
	@Override
	public Shape getShape() {
		return path;
	}

	@Override
	public int getShapeLength() {
		Rectangle b=  path.getBounds();
		return b.width+b.height;
	}
}
