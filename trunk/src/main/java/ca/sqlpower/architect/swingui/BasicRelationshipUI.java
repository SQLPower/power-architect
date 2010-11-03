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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
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
import java.util.List;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.ColourScheme;

/**
 * The BasicRelationshipUI is responsible for drawing the lines
 * between tables.  Subclasses decorate the ends of the lines.
 */
public class BasicRelationshipUI extends RelationshipUI implements java.io.Serializable {
	
	/**
	 * A wrapper for a point so that when we get the points on where to draw the relationship, we don't
	 * change them.
	 */
	public static class ImmutablePoint {
		Point p;
		public ImmutablePoint(Point p) {
			this.p = p;
		}
		public int getX() {
			return (int)p.getX();
		}
		public int getY() {
			return (int)p.getY();
		}
		public boolean equals(ImmutablePoint p) {
			return (getX() == p.getX() && getY() == p.getY());
		}
        public boolean equals(Point p) {
            return equals(new ImmutablePoint(p));
        }
	}

	private static Logger logger = Logger.getLogger(BasicRelationshipUI.class);
	
	private static final double NINETY_DEGREES = Math.toRadians(90.0);

	protected Rectangle computedBounds;

	/**
	 * This is the path that the relationship line follows.  Don't 
	 * use it for contains() and intersects() becuase
	 * it is closed by a diagonal line from start to finish.
	 * 
	 * @see containmentPath
	 */
	protected transient GeneralPath path;
	
	/**
	 * This is a closed path for use with contains() and intersects().
     * <p>
     * This path is recalculated every time paint() is called, and
     * it's cached here for the benefit of {@link #contains(Point)}
     * and {@link #intersectsShape(Shape)}.
	 */
	protected transient GeneralPath containmentPath;

	/**
	 * The stroke width to use when the relationship is selected.
	 */
	protected float nonSelectedStrokeWidth = 1f;
	
	/**
	 * The stroke width to use when the relationship is not selected.
	 */
	protected float selectedStrokeWidth = 2f;
    
	/**
	 * Points within radius pixels of this relationship's visible path
	 * are considered to be contained within this component.
	 *
	 * @see #contains
	 */
	protected int radius = 4;
	
	private FontMetrics fm;
	
	private JLabel parentToChild = new JLabel();
	private JLabel childToParent = new JLabel();
	// ------------------------ ComponentUI methods ------------------------

	public static PlayPenComponentUI createUI(PlayPenComponent c) {
		logger.debug("Creating new BasicRelationshipUI for "+c);
        return new BasicRelationshipUI();
    }

    public void installUI(PlayPenComponent c) {
		logger.debug("Installing BasicRelationshipUI on "+c);
		relationship = (Relationship) c;
		if (relationship.getPlayPen() != null) {
		    fm = relationship.getFontMetrics(relationship.getFont());
		} else {
		    fm = null;
		}
		computedBounds = new Rectangle(relationship.getBounds());
		relationship.addSPListener(this);
    }

    public void uninstallUI(PlayPenComponent c) {
		relationship = (Relationship) c;
		relationship.removeSPListener(this);
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
		final int orientation = r.getOrientation();
		childToParent.setFont(g2.getFont());
		parentToChild.setFont(g2.getFont());
		logger.debug("orientation is: " + orientation);
		if (c == null)
		{
			throw new NullPointerException("Relationship c is null");
		}
			
		g2.translate(c.getX() * -1, c.getY() * -1); // playpen coordinate space

		if (logger.isDebugEnabled()) {
			g2.setColor(c.getBackgroundColor());
			Rectangle bounds = c.getBounds();
			g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			logger.debug("Relationship bounds " + bounds);
			g2.setColor(c.getForegroundColor());
		}

		try {
		    ImmutablePoint pktloc = relationship.createPkConnectionPoint();
            Point start = new Point(pktloc.getX() + r.getPkTable().getLocation().x,
            		                pktloc.getY() + r.getPkTable().getLocation().y);
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
            
            ImmutablePoint fktloc = relationship.createFkConnectionPoint();
            Point end = new Point(fktloc.getX() + r.getFkTable().getLocation().x,
            		              fktloc.getY() + r.getFkTable().getLocation().y);
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
			
			/**
			 * The proper x-coordinate point for <code>parentToChild</code>.
			 */
			float parentToChildLabelStartX = 0;
			
			/**
			 * The proper x-coordinate point for <code>childToParent</code>.
			 */
			float childToParentLabelStartX = 0;
			
			/**
			 * The proper x-coordinate point for <code>parentToChild</code>.
			 */
			float parentToChildLabelStartY = 0;
			
			/**
			 * The proper x-coordinate point for <code>childToParent</code>.
			 */
			float childToParentLabelStartY = 0;
			g2.setColor(r.getForegroundColor());
			
			float leftmost = 0;
            float rightmost = 0;
            /**
             * The highest point is actually smaller than the lowest point
             */
            float highest = 0;
            /**
             * The lowest point is actually bigger than the highest point
             */
            float lowest = 0;
            if (r.displayRelationshipLabel()) {
                if (lineStart.x > lineEnd.x) {
                    logger.debug("Parent is at the right side of child");
                    leftmost = lineEnd.x;
                    rightmost = lineStart.x;
                } else {
                    logger.debug("Parent is at the left side of child");
                    leftmost = lineStart.x;
                    rightmost = lineEnd.x;
                }
                if (lineStart.y > lineEnd.y) {
                    logger.debug("Parent is below the child");
                    highest = lineEnd.y;
                    lowest = lineStart.y;
                } else {
                    logger.debug("Parent is above the child");
                    highest = lineStart.y;
                    lowest = lineEnd.y;
                }
                parentToChildLabelStartX = calculateRelationshipLabelStart(leftmost, rightmost,
                                                fm.stringWidth(r.getTextForParentLabel()));
                childToParentLabelStartX = calculateRelationshipLabelStart(leftmost, rightmost,
                                                fm.stringWidth(r.getTextForChildLabel()));
                parentToChildLabelStartY = calculateRelationshipLabelStart(highest, lowest,
                                                fm.stringWidth(r.getTextForParentLabel()));
                childToParentLabelStartY = calculateRelationshipLabelStart(highest, lowest,
                                                fm.stringWidth(r.getTextForChildLabel()));
            }
             
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
                // draw relationship labels
                if (r.displayRelationshipLabel()) {
                    leftmost = lineEnd.x - getTerminationLength();
                    rightmost = lineStart.x;
                    parentToChildLabelStartX = calculateRelationshipLabelStart(leftmost, rightmost, 
                                                                        fm.stringWidth(r.getTextForParentLabel()));
                    childToParentLabelStartX = calculateRelationshipLabelStart(leftmost, rightmost,
                                                                        fm.stringWidth(r.getTextForChildLabel()));
                    logger.debug("relationship label starts at: " + parentToChildLabelStartX);
                    g2.translate(parentToChildLabelStartX,lineStart.y + getTerminationLength() - fm.getHeight());
                    parentToChild.setIcon(null);
                    parentToChild.setText(r.getTextForParentLabel());
                    parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                    parentToChild.paint(g2);
                    g2.translate(-parentToChildLabelStartX, -(lineStart.y + getTerminationLength()) + fm.getHeight());
                    
                    g2.translate(childToParentLabelStartX,lineStart.y + getTerminationLength());
                    childToParent.setIcon(null);
                    childToParent.setText(r.getTextForChildLabel());
                    childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                    childToParent.paint(g2);
                    g2.translate(-childToParentLabelStartX, -(lineStart.y + getTerminationLength()));
                }
				
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
				// draw relationship labels
				if (r.displayRelationshipLabel()) {
    				if ((orientation & PARENT_FACES_LEFT) != 0) {
    				    if (lineStart.y < lineEnd.y) {
    				        logger.debug("pk table is at right and higher");
    				        
    				        if ((lineStart.x - lineEnd.x) > (lineEnd.y - lineStart.y)) {
        				        g2.translate(parentToChildLabelStartX, lineEnd.y);
        				        parentToChild.setIcon(null);
                                parentToChild.setText(r.getTextForParentLabel());
        	                    parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
        	                    parentToChild.paint(g2);
        	                    g2.translate(-parentToChildLabelStartX, -lineEnd.y);
        	                    
        	                    g2.translate(childToParentLabelStartX, lineStart.y - fm.getHeight());
        	                    childToParent.setIcon(null);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                childToParent.paint(g2);
                                g2.translate(-childToParentLabelStartX, -(lineStart.y - fm.getHeight()));
    				        } else {
    				            g2.translate(midx + fm.getHeight() + fm.getDescent(), parentToChildLabelStartY);
    				            g2.rotate(NINETY_DEGREES);
    				            parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx - fm.getHeight() - fm.getDescent(), -parentToChildLabelStartY);
                                
                                g2.translate(midx, childToParentLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                childToParent.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx, -childToParentLabelStartY);
    				        }
    				    } else {
    				        logger.debug("pk table is at right and lower");
    				        
    				        if ((lineStart.x - lineEnd.x) > (lineStart.y - lineEnd.y)) {
        				        g2.translate(parentToChildLabelStartX,lineStart.y);
        				        parentToChild.setIcon(null);
                                parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.translate(-parentToChildLabelStartX, -lineStart.y);
                                
                                g2.translate(childToParentLabelStartX,lineEnd.y - fm.getHeight());
                                childToParent.setIcon(null);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                childToParent.paint(g2);
                                g2.translate(-childToParentLabelStartX, -(lineEnd.y - fm.getHeight()));
    				        } else {
    				            g2.translate(midx, parentToChildLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx, -parentToChildLabelStartY);
                                
                                g2.translate(midx + fm.getHeight() + fm.getDescent(), childToParentLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                childToParent.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx - fm.getHeight() - fm.getDescent(), -childToParentLabelStartY);
    				        }
    				    }
    				} else {
    				    if (lineStart.y < lineEnd.y) {
    				        logger.debug("pk table is at left and higher");
    				        
    				        if ((lineEnd.x - lineStart.x) > (lineEnd.y - lineStart.y)) {
        				        g2.translate(parentToChildLabelStartX,lineStart.y - fm.getHeight());
        				        parentToChild.setIcon(null);
        				        parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.translate(-parentToChildLabelStartX, -(lineStart.y - fm.getHeight()));
                                
                                g2.translate(childToParentLabelStartX,lineEnd.y);
                                childToParent.setIcon(null);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                childToParent.paint(g2);
                                g2.translate(-childToParentLabelStartX, -lineEnd.y);
    				        } else {
    				            g2.translate(midx + fm.getHeight() + fm.getDescent(), parentToChildLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx - fm.getHeight() - fm.getDescent(), -parentToChildLabelStartY);
                                
                                g2.translate(midx, childToParentLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                childToParent.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx, -childToParentLabelStartY);
    				        }
                        } else {
                            logger.debug("pk table is at left and lower");
                            
                            if ((lineEnd.x - lineStart.x) > (lineStart.y - lineEnd.y)) {
                                g2.translate(parentToChildLabelStartX,lineEnd.y - fm.getHeight());
                                parentToChild.setIcon(null);
                                parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.translate(-parentToChildLabelStartX, -(lineEnd.y - fm.getHeight()));
                                
                                g2.translate(childToParentLabelStartX,lineStart.y);
                                childToParent.setIcon(null);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                                childToParent.paint(g2);
                                g2.translate(-childToParentLabelStartX, -lineStart.y);
                            } else {
                                g2.translate(midx, parentToChildLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                parentToChild.setText(r.getTextForParentLabel());
                                parentToChild.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                parentToChild.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx, -parentToChildLabelStartY);
                                
                                g2.translate(midx + fm.getHeight() + fm.getDescent(), childToParentLabelStartY);
                                g2.rotate(NINETY_DEGREES);
                                childToParent.setText(r.getTextForChildLabel());
                                childToParent.setBounds(0, 0, (int)(lowest - highest), fm.getHeight());
                                childToParent.paint(g2);
                                g2.rotate(-NINETY_DEGREES);
                                g2.translate(-midx - fm.getHeight() - fm.getDescent(), -childToParentLabelStartY);
                            }
                        }
    				}
				}
				
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
				// draw relationship labels
				if (r.displayRelationshipLabel()) {
				    parentToChild.setIcon(null);
				    childToParent.setIcon(null);
                    if (lineStart.x > lineEnd.x) {
                        logger.debug("TOP-BOTTOM : pk table is at right");
                        g2.translate(parentToChildLabelStartX,midy);
                        parentToChild.setText(r.getTextForParentLabel());
                        parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        parentToChild.paint(g2);
                        g2.translate(-parentToChildLabelStartX, -midy);
                        
                        g2.translate(childToParentLabelStartX, midy - fm.getHeight());
                        childToParent.setText(r.getTextForChildLabel());
                        childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        childToParent.paint(g2);
                        g2.translate(-childToParentLabelStartX, -(midy - fm.getHeight()));
                    } else {
                        logger.debug("TOP-BOTTOM : pk table is at left");
                        g2.translate(parentToChildLabelStartX,midy - fm.getHeight());
                        parentToChild.setText(r.getTextForParentLabel());
                        parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        parentToChild.paint(g2);
                        g2.translate(-parentToChildLabelStartX, -(midy - fm.getHeight()));
                        
                        g2.translate(childToParentLabelStartX, midy);
                        childToParent.setText(r.getTextForChildLabel());
                        childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        childToParent.paint(g2);
                        g2.translate(-childToParentLabelStartX, -midy);
                    }
				}
				
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
				// draw relationship labels
				if (r.displayRelationshipLabel()) {
				    parentToChild.setIcon(null);
                    childToParent.setIcon(null);
    				if ((orientation & PARENT_FACES_LEFT) != 0) {
                        logger.debug("pk table is at right");
                        g2.translate(parentToChildLabelStartX,lineStart.y);
                        parentToChild.setText(r.getTextForParentLabel());
                        parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        parentToChild.paint(g2);
                        g2.translate(-parentToChildLabelStartX, -lineStart.y);
                        
                        g2.translate(childToParentLabelStartX,lineStart.y - fm.getHeight());
                        childToParent.setText(r.getTextForChildLabel());
                        childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        childToParent.paint(g2);
                        g2.translate(-childToParentLabelStartX, -(lineStart.y - fm.getHeight()));
                    } else {
                        logger.debug("pk table is at left");
                        g2.translate(parentToChildLabelStartX,lineStart.y - fm.getHeight());
                        parentToChild.setText(r.getTextForParentLabel());
                        parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        parentToChild.paint(g2);
                        g2.translate(-parentToChildLabelStartX, -(lineStart.y - fm.getHeight()));
                        
                        g2.translate(childToParentLabelStartX,lineStart.y);
                        childToParent.setText(r.getTextForChildLabel());
                        childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        childToParent.paint(g2);
                        g2.translate(-childToParentLabelStartX, -lineStart.y);
                    }
				}
				
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
				// draw relationship labels
				if (r.displayRelationshipLabel()) {
				    parentToChild.setIcon(null);
                    childToParent.setIcon(null);
                    if (lineStart.x > lineEnd.x) {
                        logger.debug("TOP_BOTTOM--LEFT_RIGHT pk table is at right");
                        g2.translate(parentToChildLabelStartX,lineEnd.y);
                        parentToChild.setText(r.getTextForParentLabel());
                        parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        parentToChild.paint(g2);
                        g2.translate(-parentToChildLabelStartX, -lineEnd.y);
                        
                        g2.translate(childToParentLabelStartX,lineEnd.y - fm.getHeight());
                        childToParent.setText(r.getTextForChildLabel());
                        childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        childToParent.paint(g2);
                        g2.translate(-childToParentLabelStartX, -(lineEnd.y - fm.getHeight()));
                    } else {
                        logger.debug("TOP_BOTTOM--LEFT_RIGHT pk table is at left");
                        g2.translate(parentToChildLabelStartX,lineEnd.y - fm.getHeight());
                        parentToChild.setText(r.getTextForParentLabel());
                        parentToChild.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        parentToChild.paint(g2);
                        g2.translate(-parentToChildLabelStartX, -(lineEnd.y - fm.getHeight()));
                        
                        g2.translate(childToParentLabelStartX,lineEnd.y);
                        childToParent.setText(r.getTextForChildLabel());
                        childToParent.setBounds(0, 0, (int) (rightmost - leftmost), fm.getHeight());
                        childToParent.paint(g2);
                        g2.translate(-childToParentLabelStartX, -lineEnd.y);
                    }
				}
				
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
                
                parentToChild.setIcon(null);
                childToParent.setIcon(null);
			}
			if (!r.isSelected()) {
				g2.setColor(r.getForegroundColor());
			} else {
			    if(r.getForegroundColor().darker().equals(r.getForegroundColor())) {
			        g2.setColor(ColourScheme.SQLPOWER_ORANGE);
			    } else {
			        g2.setColor(r.getForegroundColor().darker());
			    }
			}

			Stroke oldStroke = g2.getStroke();
			
			if (relationship.getModel().isIdentifying()) {
				g2.setStroke(getIdentifyingStroke());
			} else {
				g2.setStroke(getNonIdentifyingStroke());
			}

			g2.draw(path);
			if (logger.isDebugEnabled()) logger.debug("Drew path "+path);

			g2.setStroke(new BasicStroke(getStrokeWidth()));
			paintTerminations(g2, start, end, orientation);
			g2.setStroke(oldStroke);
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
	
	public BasicRelationshipUI () {
	    
	}
	
	/**
	 * This copy constructor does not copy all values at current, 
	 * such as computed bounds or the path.
	 */
	BasicRelationshipUI (BasicRelationshipUI copy) {
	    this.relationship = copy.getRelationship();
	}
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
        logger.debug("bestConnectionPoints()");
		Rectangle pktBounds = relationship.getPkTable().getBounds();
		Rectangle fktBounds = relationship.getFkTable().getBounds();
		
		ImmutablePoint fkConnectionPoint = relationship.createFkConnectionPoint();
		ImmutablePoint pkConnectionPoint = relationship.createPkConnectionPoint();
		
		int orientation = relationship.getOrientation();

		if (relationship.getPkTable() == relationship.getFkTable()) {
			// self-referencing table
		    orientation = PARENT_FACES_BOTTOM | CHILD_FACES_LEFT;
			relationship.setOrientation(orientation);
			relationship.setPkConnection(0.5);
			relationship.setFkConnection(0.5);
			logger.debug("Self-referencing table: set connection points pk="
						 +pkConnectionPoint+"; fk="+fkConnectionPoint);
		} else {
			// distinct tables ("normal" case)
			Line2D.Double pkToFkBorderLine = calcConnectionPoints(pktBounds, fktBounds);

			orientation = getFacingEdges(pktBounds, fktBounds);
			relationship.setOrientation(orientation);

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

			relationship.setPkConnectionPoint(new Point(
			        (int) (pkToFkBorderLine.x1 - pktBounds.x),
			        (int) (pkToFkBorderLine.y1 - pktBounds.y)));
			
			relationship.setFkConnectionPoint(new Point(
					(int) (pkToFkBorderLine.x2 - fktBounds.x),
					(int) (pkToFkBorderLine.y2 - fktBounds.y)));
		}
		// deals with overlapping connections
        fixConnectionPoints();
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
	 * NOTE: The orientation of the relationship will now be changed by 
	 * this method.
	 */
	public Point closestEdgePoint(boolean onPkTable, Point p) {
		TablePane tp = onPkTable ? relationship.getPkTable() : relationship.getFkTable();
		Dimension tpsize = tp.getSize();
		Point ep; // this is the return value (edge point), set in one of the cases below
		Point sp; // this is the stationary point at the non-moving end of the relationship
		if (onPkTable) {
			sp = new Point(relationship.createFkConnectionPoint().getX(), relationship.createFkConnectionPoint().getY());
			ep = checkClosestPointOnTable(tpsize, sp, p, PARENT_FACES_LEFT, PARENT_FACES_RIGHT, PARENT_FACES_TOP, PARENT_FACES_BOTTOM, CHILD_MASK, true);
			
		} else {
			sp = new Point(relationship.createPkConnectionPoint().getX(), relationship.createPkConnectionPoint().getY());
            ep = checkClosestPointOnTable(tpsize, sp, p, CHILD_FACES_LEFT, CHILD_FACES_RIGHT, CHILD_FACES_TOP, CHILD_FACES_BOTTOM, PARENT_MASK, false);
		}
		return ep;
	}
	/**
	 * This method is a helper method for the ClosestEdgePoint method that will check 
	 * if the orientation of an adjacent edge and will create a new point 
	 * whenever the newly calculated orientation is valid. 
	 * NOTE: This method will also change the current relationships orientation as a result.
	 * The left, right, top and bottom variables passed to this method are the custom 
	 * values of the orientation found in the RelationshipUI class.
	 * @param sp Stationary point of the opposite table.
	 * @param p Mouse Point in reference to the table
	 * @param oppositeMask Mask of the opposite table.
	 * @param pkTable This boolean value indicates which table has to be checked.
	 * @return
	 */
	private Point checkClosestPointOnTable (Dimension tpsize, Point sp, Point p, int left, int right, int top, int bottom, int oppositeMask ,boolean pkTable)
	{
	    Point ep = new Point();
	    int orientation = relationship.getOrientation();
	    if ((orientation & left) != 0) {
            ep = new Point(0, Math.max(0, Math.min(tpsize.height, p.y)));
            logger.debug("ep is : " + ep + " sp is " + sp + " p is " + p);
            if (Math.abs(ep.y - sp.y) <= getSnapRadius()) {
                ep.y = sp.y;
            }
            if (tpsize.height < p.y) {
                orientation = orientation & oppositeMask | bottom;
            } else if (p.y <= 0) {
                orientation = orientation & oppositeMask | top;
            }
            relationship.setOrientation(orientation);
            BasicRelationshipUI newRelation = new BasicRelationshipUI(this);
            if(pkTable){
                newRelation.getRelationship().setPkConnectionPoint(ep);
            } else {
                newRelation.getRelationship().setFkConnectionPoint(ep);
            }
            if (!newRelation.isOrientationLegal()) {
                orientation = orientation & oppositeMask | left;
                relationship.setOrientation(orientation);
            }
        } else if ((orientation & right)  != 0) {
            ep = new Point(tpsize.width, Math.max(0, Math.min(tpsize.height, p.y)));
            if (Math.abs(ep.y - sp.y) <= getSnapRadius()) ep.y = sp.y;
            if (tpsize.height < p.y) {
                orientation = orientation & oppositeMask | bottom;
            } else if (p.y <= 0) {
                orientation = orientation & oppositeMask | top;
            }
            relationship.setOrientation(orientation);
            BasicRelationshipUI newRelation = new BasicRelationshipUI(this);
            if(pkTable){
                newRelation.getRelationship().setPkConnectionPoint(ep);
            }else{
                newRelation.getRelationship().setFkConnectionPoint(ep);
            }
            if (!newRelation.isOrientationLegal()) {
                orientation = orientation & oppositeMask | right;
            }
            relationship.setOrientation(orientation);
        } else if ((orientation & top)  != 0) {
            ep = new Point(Math.max(0, Math.min(tpsize.width, p.x)), 0);
            if (Math.abs(ep.x - sp.x) <= getSnapRadius()) ep.x = sp.x;
            if (tpsize.width < p.x) {
                orientation = orientation & oppositeMask | right;
            } else if (p.x <= 0) {
                orientation = orientation & oppositeMask | left;
            }
            relationship.setOrientation(orientation);
            BasicRelationshipUI newRelation = new BasicRelationshipUI(this);
            if(pkTable){
                newRelation.getRelationship().setPkConnectionPoint(ep);
            }else{
                newRelation.getRelationship().setFkConnectionPoint(ep);
            }
            if (!newRelation.isOrientationLegal()) {
                orientation = orientation & oppositeMask | top;
            }
            relationship.setOrientation(orientation);
        } else if ((orientation & bottom)  != 0) {
            ep = new Point(Math.max(0, Math.min(tpsize.width, p.x)), tpsize.height);
            if (Math.abs(ep.x - sp.x) <= getSnapRadius()) ep.x = sp.x;
            if (tpsize.width < p.x) {
                orientation = orientation & oppositeMask | right;
            } else if (p.x <= 0) {
                orientation = orientation & oppositeMask | left;
            }
            relationship.setOrientation(orientation);
            BasicRelationshipUI newRelation = new BasicRelationshipUI(this);
            if(pkTable){
                newRelation.getRelationship().setPkConnectionPoint(ep);
            }else{
                newRelation.getRelationship().setFkConnectionPoint(ep);
            }
            if (!newRelation.isOrientationLegal()) {
                orientation = orientation & oppositeMask | bottom;
            }
            relationship.setOrientation(orientation);
        } else {
            ep = new Point(p);
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
	
	public Relationship getRelationship() {
	    return relationship;
	}

    /**
     * Returns the stroke thickness that should be used, based on the
     * relationship component's current state (for example, whether or not it's
     * selected).
     * <p>
     * If you just want to know what stroke to use, don't call this method; use
     * {@link #getIdentifyingStroke()} or {@link #getNonIdentifyingStroke()}.
     * 
     * @return The correct stroke thickness to use.
     */
	protected float getStrokeWidth() {
	    if (relationship.isSelected()) {
            return selectedStrokeWidth;
        } else {
            return nonSelectedStrokeWidth;
        }
	}
	
	public Stroke getIdentifyingStroke() {
	    return new BasicStroke(getStrokeWidth());
	}

	public Stroke getNonIdentifyingStroke() {
        return new BasicStroke(getStrokeWidth());
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
	    ImmutablePoint fkConnectionPoint = relationship.createFkConnectionPoint();
	    ImmutablePoint pkConnectionPoint = relationship.createPkConnectionPoint();
	    
	    final int orientation = relationship.getOrientation();
	    
		if (relationship.getPkTable() == relationship.getFkTable()) {
			return (orientation == (PARENT_FACES_BOTTOM | CHILD_FACES_LEFT));
		} else {
		    if (orientation == 0) return false;
		    Rectangle pkBounds = relationship.getPkTable().getBounds();
		    Rectangle fkBounds = relationship.getFkTable().getBounds();
		    Line2D.Double relationshipLine =
		            new Line2D.Double(fkBounds.getX() + fkConnectionPoint.getX(),fkBounds.getY() + fkConnectionPoint.getY(),
		                            pkBounds.getX() + pkConnectionPoint.getX(), pkBounds.getY() + pkConnectionPoint.getY());
		        
		    List<Point2D.Double> pkTableIntersectPoints = ASUtils.getIntersectPoints(pkBounds,relationshipLine);
		    List<Point2D.Double> fkTableIntersectPoints = ASUtils.getIntersectPoints(fkBounds,relationshipLine);
		    if ((pkTableIntersectPoints.size() <= 1 && fkTableIntersectPoints.size() <= 1)) {
		        logger.debug("intersect points to pkTable " + pkTableIntersectPoints.size() + " intersect points to fkTable " + fkTableIntersectPoints.size());
		        return true;
		    }
		    
		    Point newParent = new Point();
            Point newChild = new Point();
            
		    if (pkTableIntersectPoints.size() == 2 && pkTableIntersectPoints.get(0).equals(pkTableIntersectPoints.get(1))) {
		        newParent = checkOrientationForCorner(pkConnectionPoint, orientation, PARENT_FACES_LEFT, PARENT_FACES_RIGHT, PARENT_FACES_TOP, PARENT_FACES_BOTTOM, PARENT_MASK);
		    } 
		    else {
		        newParent.setLocation(pkConnectionPoint.getX(), pkConnectionPoint.getY());
		    }
		        
		    if (fkTableIntersectPoints.size() == 2 && fkTableIntersectPoints.get(0).equals(fkTableIntersectPoints.get(1))) {
		        newChild = checkOrientationForCorner(fkConnectionPoint, orientation, CHILD_FACES_LEFT, CHILD_FACES_RIGHT, CHILD_FACES_TOP, CHILD_FACES_BOTTOM, CHILD_MASK);
            } else {
                newChild.setLocation(fkConnectionPoint.getX(), fkConnectionPoint.getY());
		    }

		    relationshipLine =
		        new Line2D.Double(fkBounds.getX() + newChild.getX(),fkBounds.getY() + newChild.getY(),
		                pkBounds.getX() + newParent.getX(), pkBounds.getY() + newParent.getY());

		    pkTableIntersectPoints = ASUtils.getIntersectPoints(pkBounds,relationshipLine);
		    fkTableIntersectPoints = ASUtils.getIntersectPoints(fkBounds,relationshipLine);
		    if ((pkTableIntersectPoints.size() <= 1 && fkTableIntersectPoints.size() <= 1)) {
		        logger.debug("intersect points to pkTable " + pkTableIntersectPoints.size() + " intersect points to fkTable " + fkTableIntersectPoints.size());
		        return true;
		    }

		}

		logger.debug("OrientationLegal() returning false");
		return false;
	}

	/**
	 * This is a helper method for isOrientationLegal. This method will
	 * take in an end point to a relation as well as the current orientation
	 * and move the point off of a corner. The returned point is the new location
	 * of the end point off the corner. The left, right, top, bottom and mask integers
	 * are for the parent or child constant values since they are dependent on if they
	 * are for the parent or child.
	 */
    private Point checkOrientationForCorner(ImmutablePoint relationEndpoint, int orientation, int left, int right, int top, int bottom, int mask) 
    {
       Point newEndpoint = new Point();
       int parentOrientation = orientation & mask;
       if (relationEndpoint.getX() == 0 && relationEndpoint.getY() == 0) {
           if (parentOrientation == left) {
               newEndpoint.setLocation(relationEndpoint.getX(), relationEndpoint.getY() + 1);
           } else if (parentOrientation == top) {
               newEndpoint.setLocation(relationEndpoint.getX() + 1, relationEndpoint.getY());
           }
       } else if (relationEndpoint.getX() == 0) {
           if (parentOrientation == left) {
               newEndpoint.setLocation(relationEndpoint.getX(), relationEndpoint.getY() - 1);
           } else if (parentOrientation == bottom) {
               newEndpoint.setLocation(relationEndpoint.getX() + 1, relationEndpoint.getY());
           }
       } else if (relationEndpoint.getY() == 0) {
           if (parentOrientation == top) {
               newEndpoint.setLocation(relationEndpoint.getX() - 1, relationEndpoint.getY());
           } else if (parentOrientation == right) {
               newEndpoint.setLocation(relationEndpoint.getX(), relationEndpoint.getY() + 1);
           }
       } else {
           if (parentOrientation == right) {
               newEndpoint.setLocation(relationEndpoint.getX(), relationEndpoint.getY() - 1);
           } else if (parentOrientation == bottom) {
               newEndpoint.setLocation(relationEndpoint.getX() - 1, relationEndpoint.getY());
           }
       }
       return newEndpoint;
    }
	
	/**
	 * Attempts to move the connection points if they collided with another 
	 * relationship's connection points. It depends on the points having
	 * a previous location point already, preferably the best connection point.
	 * <p>
	 * Guesses for new connection points in the following order:
	 *             7  5  3  1  origin  2  4  6  8
	 */
	public void fixConnectionPoints() {
	    logger.debug("fixConnectionPoints()");
	    PlayPen playPen = relationship.getPlayPen();
	    Rectangle pktBounds = relationship.getPkTable().getBounds();
        Rectangle fktBounds = relationship.getFkTable().getBounds();      
        
        int orientation = getFacingEdges(pktBounds, fktBounds);
        // hack for the orientation of self referencing relationships
        if (relationship.getPkTable() == relationship.getFkTable()) {
            orientation = PARENT_FACES_BOTTOM | CHILD_FACES_LEFT;
        }
	    
        // sets up to fix the pkConnectionPoint
        boolean isPkConnectionPoint = true;
        Point connectionPoint;
        TablePane table = relationship.getPkTable();
        
        // the last adjusted point that was not out of bounds
        ImmutablePoint lastValidPoint = relationship.createPkConnectionPoint();

        // two passes: first, the setup (directly above) causes the code in the loop
        // to update the PK connection point.  At the bottom of the loop, the setup
        // changes to fix the FK connection point.
        for (int i = 0; i < 2; i++) {
    	    int count = 1;
    	    
            boolean collided;
            
            // indicate if an adjusted point has been out of bounds,
            // two values for the two possible sides from the origin
            boolean[] outOfBounds = {false, false};

            do {
                collided = false;

                // skip this offset because this side has already went out of bounds
                if (outOfBounds[count%2]) {
                    collided = true;
                } else {
                    for (Relationship r : playPen.getContentPane().getChildren(Relationship.class)) {
                        // skips this relationship and any that is not connected
                        // to the current table pane
                        if (r == relationship || (r.getPkTable() != table && r.getFkTable() != table)) continue;
                        
                        // checks for collision, r.contains() was not used because
                        // it did not pick up collisions when the table was dragged
                        // past the illegal orientation points.
                        if (r.createPkConnectionPoint().equals(lastValidPoint) ||
                                r.createFkConnectionPoint().equals(lastValidPoint)) {
                            collided = true;
                            
                            // determines offset according to count in this order:
                            // 1, -1, 2, -2, 3, -3, ...
                            int offset = count/2;
                            if (count%2 != 0) {
                                offset = (offset + 1)*-1;
                            } 
                            
                            connectionPoint = adjustConnectionPoint(orientation, isPkConnectionPoint, offset);
                            
                            // sets the indicators if the point was out of bounds
                            if (connectionPoint == null) {
                                outOfBounds[count%2] = true;
                            // saves the new adjusted point
                            } else {
                                lastValidPoint = new ImmutablePoint(connectionPoint);
                            }
                            break;
                        }
                    }
                }
                count++;
            } while (collided && !(outOfBounds[0] && outOfBounds[1]));
            
            // actual work to adjust the connection point
            if (isPkConnectionPoint) {
                relationship.setPkConnection(((double)lastValidPoint.getX() / relationship.getPkTable().getWidth()));
            } else {
                relationship.setFkConnection(((double)lastValidPoint.getX() / relationship.getFkTable().getWidth()));
            }
            
            // sets up for the next connection point, assumes
            // that there are only two: pkConnectionPoint, fkConnectionPoint
            isPkConnectionPoint = false;
            table = relationship.getFkTable();
            lastValidPoint = relationship.createFkConnectionPoint();
        }
	}
	
	/**
	 * Adjusts and returns a new connection point that is an offset of either
	 * the pkConnectionPoint or the fkConnection point. Returns null if the
	 * new connection point was out of bounds.
	 * 
	 * @param orientation The orientation of the associated table
	 * @param isPkConnectionPoint Whether the point of origin is the pkConnectionPoint, assumes
	 *                            that there are only two: pkConnectionPoint, fkConnectionPoint
	 * @param offset The offset to adjust the point by: multiplied by the snap radius * 2.
	 * @return The new connection point, null if out of bounds.
	 */
	private Point adjustConnectionPoint(int orientation, boolean isPkConnectionPoint, int offset) {
	    logger.debug("adjustConnectionPoint()");
	    Rectangle tBounds;
	    ImmutablePoint connectionPoint;

	    // sets up the corresponding orientations, bounds, 
	    // and point of origin to check for according to
	    // isPkConnectionPoint
	    int[] orientations;
	    if (isPkConnectionPoint) {
	        tBounds = relationship.getPkTable().getBounds();
	        orientations = new int[]{PARENT_FACES_TOP, PARENT_FACES_RIGHT,
	                PARENT_FACES_BOTTOM, PARENT_FACES_LEFT};
	        connectionPoint = relationship.createPkConnectionPoint();
	    } else {
	        tBounds = relationship.getFkTable().getBounds();
	        orientations = new int[]{CHILD_FACES_TOP, CHILD_FACES_RIGHT,
	                CHILD_FACES_BOTTOM, CHILD_FACES_LEFT};
	        connectionPoint = relationship.createFkConnectionPoint();
	    }

	    int x = connectionPoint.getX();
	    int y = connectionPoint.getY();

	    // adjusts the x coordinate if the table faced top or bottom
	    if ( ((orientation & orientations[2]) != 0) || ((orientation & orientations[0]) != 0) ) {
	        x += offset * getTerminationWidth() * 2;
	        if ((x < getTerminationWidth()) || 
	                (x > tBounds.width - getTerminationWidth())) {
	            return null;
	        }
	    // adjusts the y coordinate if the table faced left or right
	    } else if ( ((orientation & orientations[3]) != 0) || ((orientation & orientations[1]) != 0) ) {
	        y += offset * getTerminationWidth() * 2;
	        if ((y < getTerminationWidth()) ||
	                (y > tBounds.height - getTerminationWidth())) {
	            return null;
	        }
	    }

	    return new Point(x, y);
	}

	/**
	 * Compute bounds should only be called by objects in this package or from regress.
	 */
	protected Rectangle computeBounds() {
        logger.debug("computeBounds()");
		// XXX: should check for valid cached bounds before recomputing!

		TablePane pkTable = relationship.getPkTable();
		TablePane fkTable = relationship.getFkTable();
		
		ImmutablePoint fkConnectionPoint = relationship.createFkConnectionPoint();
		ImmutablePoint pkConnectionPoint = relationship.createPkConnectionPoint();
		
		if (!isOrientationLegal() && relationship.isMagicEnabled()) {
			// bestConnectionPoints also updates orientation as a side effect
			bestConnectionPoints();
		}

		if (pkTable == fkTable) {
			// hack for supporting self-referencing table
			// assume orientation is PARENT_FACES_BOTTOM | CHILD_FACES_LEFT
			Point topLeft = new Point(fkConnectionPoint.getX() - getTerminationLength() * 2 - radius,
									  fkConnectionPoint.getY() - getTerminationWidth());
			Point bottomRight = new Point(pkConnectionPoint.getX() + getTerminationWidth(),
										  pkConnectionPoint.getY() + radius + getTerminationLength() * 2);
			computedBounds = new Rectangle(topLeft.x + pkTable.getX(), topLeft.y + pkTable.getY(),
										  bottomRight.x - topLeft.x, bottomRight.y - topLeft.y + fm.getHeight());
		} else {
			
			Point pkLimits = new Point(pkConnectionPoint.getX(),pkConnectionPoint.getY());
			pkLimits.translate(pkTable.getX(), pkTable.getY());
			Point fkLimits = new Point(fkConnectionPoint.getX(),fkConnectionPoint.getY());
			fkLimits.translate(fkTable.getX(), fkTable.getY());
			
			final int orientation = relationship.getOrientation();
			
			if (logger.isDebugEnabled()) {
				logger.debug("Absolute connection points: pk="+pkLimits+"; fk="+fkLimits);
			}
			
			Point topLeft = new Point();
			Point bottomRight = new Point();
			
			topLeft.x = (pkTable.getX() > fkTable.getX()) ? fkTable.getX() : pkTable.getX();
            topLeft.y = (pkTable.getY() > fkTable.getY()) ? fkTable.getY() : pkTable.getY();
            
            if ((orientation & PARENT_FACES_LEFT) != 0) {
                bottomRight.x = pkLimits.x + getTerminationLength();
            } else if ((orientation & CHILD_FACES_LEFT) != 0) {
                bottomRight.x = fkLimits.x + getTerminationLength();
            } else {
                if(pkLimits.x < fkLimits.x) {
                    bottomRight.x = fkLimits.x + getTerminationWidth();
                } else {
                    bottomRight.x = pkLimits.x + getTerminationWidth();
                }
            }
            
            if ((orientation & PARENT_FACES_TOP) != 0) {
                bottomRight.y = pkLimits.y + getTerminationLength();
            } else if ((orientation & CHILD_FACES_TOP) != 0) {
                bottomRight.y = fkLimits.y + getTerminationLength();
            } else {
                if(pkLimits.y < fkLimits.y) {
                    bottomRight.y = fkLimits.y + getTerminationWidth();
                } else {
                    bottomRight.y = pkLimits.y + getTerminationWidth();
                }
            }
            
            int largestTermination = Math.max(getTerminationLength(), getTerminationWidth());
            topLeft.x -= largestTermination;
            topLeft.y -= largestTermination;
            
            computedBounds = new Rectangle(topLeft.x,
                    topLeft.y,
                    bottomRight.x - topLeft.x,
                    bottomRight.y - topLeft.y);
			
			
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
			logger.debug("Computed size is ["+computedBounds.width+","+computedBounds.height+"]");
		}
		return new Dimension(computedBounds.width, computedBounds.height);
	}

	public Point getPreferredLocation() {
		//computeBounds();
		if (logger.isDebugEnabled()) {
			logger.debug("Computed locn is ["+computedBounds.x+","+computedBounds.y+"]");
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
        ImmutablePoint pkPoint = relationship.createPkConnectionPoint();
        Point pkDec = new Point(pkPoint.getX() + relationship.getPkTable().getX() - relationship.getX(),
        		pkPoint.getY() + relationship.getPkTable().getY() - relationship.getY());
		final int orientation = relationship.getOrientation();
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
		ImmutablePoint fkPoint = relationship.createFkConnectionPoint();
	    Point fkDec = new Point((int)fkPoint.getX() + relationship.getFkTable().getX() - relationship.getX(),
	    		(int)fkPoint.getY() + relationship.getFkTable().getY() - relationship.getY());
	    final int orientation = relationship.getOrientation();
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

	@Override
	public boolean intersectsShape(Shape s) {
	    if (path == null) {
	        return false;
	    }
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
		return (list.size() > 0);
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
     * Returns the actual path that this relationship ui draws. It will get
     * reset from time to time as this relationship (or its connected tables)
     * gets moved by the user, and it will not be initialized until
     * {@link #paint(Graphics2D)} has been called.
     */
	@Override
	public Shape getShape() {
		return path;
	}

    /**
     * Returns the length of this relationship's path.
     * 
     * @throws NullPointerException
     *             if paint() has not yet been called on this relationship UI.
     */
	@Override
	public int getShapeLength() {
		Rectangle b = path.getBounds();
		return b.width + b.height;
	}

	/**
	 * Calculate and return the appropriate x-position for relationship label.
	 * @param smaller The leftmost x coordinate or highest y coordinate of the relationship line 
	 * @param bigger The rightmost x coordinate or lowest y coordinate of the relationship line
	 * @param StringWidth The width of the display string under the specified fontMetrics 
	 */
	private float calculateRelationshipLabelStart(float smaller, float bigger, float StringWidth) {
	    float x;
	    if (bigger - smaller - StringWidth >= 0) {
	        x = smaller + (bigger - smaller - StringWidth)/2;
	    } else {
	        x = smaller;
	    }
	    return x;
	}
	
	public Point getPointForModelObject(Object modelObject) {
	    // Combine the two halves of the bitmask.
	    final int sidesUsed = relationship.getOrientation() / (PARENT_MASK + 1) +
	        relationship.getOrientation() % (PARENT_MASK + 1);
	    Dimension preferredSize = getPreferredSize();
	    if (sidesUsed == PARENT_FACES_TOP + PARENT_FACES_BOTTOM ||
	            sidesUsed == PARENT_FACES_LEFT + PARENT_FACES_RIGHT) {
	        // Zero or two bends: center on the relationship line.
	        return new Point(relationship.getLocation().x + (preferredSize.width /2) + 8, 
                relationship.getLocation().y + (preferredSize.height /2) - 4);
	    } else {
	        // One bend: choose corner positions based on the bitmask.
	        return new Point(relationship.getLocation().x + (sidesUsed%2 == 1 ? preferredSize.width-25 : 44),
	                relationship.getLocation().y + (sidesUsed >= PARENT_FACES_TOP ? 24 : preferredSize.height-32));
	    }
	}
}