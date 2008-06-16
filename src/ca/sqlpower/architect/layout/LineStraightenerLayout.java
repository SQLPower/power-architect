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

package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.BasicRelationshipUI;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.RelationshipUI;
import ca.sqlpower.architect.swingui.TablePane;

/**
 * A layout implementation that leaves all tables in their original
 * positions, but manipulates the relationship connection points to
 * make them straight lines (horizontal only or vertical only) if
 * at all possible.
 * 
 * @author fuerth
 */
public class LineStraightenerLayout extends AbstractLayout {

    private static Logger logger = Logger.getLogger(LineStraightenerLayout.class);
    
    private Collection<? extends LayoutEdge> edges;
    private boolean hasRun;
    
    /**
     * Sets up this layout.  Until the LayoutNode and LayoutEdge interfaces start
     * supporting specific connection points, this class assumes all the nodes are
     * TablePane instances, and all the edges are Relationships.
     */
    @Override
    public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle rect) {
        super.setup(nodes, edges, rect);
        this.edges = edges;
        hasRun = false;
    }
    
    /**
     * Does nothing, since this layout happens in a single step.
     */
    public void done() {
       // nothing to do 
    }

    public boolean isDone() {
        return hasRun;
    }

    public void nextFrame() {
        for (LayoutEdge e : edges) {
            if (e instanceof Relationship) {
                Relationship r = (Relationship) e;
                r.fireRelationshipConnectionPointsMovedByUser(r.getPkConnectionPoint(), r.getFkConnectionPoint(), true);
                attemptToStraighten(r);
            }
        }
        hasRun = true;
    }

    private void attemptToStraighten(Relationship r) {
        TablePane tp1 = r.getPkTable();
        TablePane tp2 = r.getFkTable();
        int orientation = ((RelationshipUI) r.getUI()).getOrientation();

        if (logger.isDebugEnabled()) {
            logger.debug("PK Table is at " + tp1.getBounds());
            logger.debug("FK Table is at " + tp2.getBounds());
        }
        
        if (((orientation & (RelationshipUI.CHILD_FACES_LEFT | RelationshipUI.CHILD_FACES_RIGHT)) != 0) &&
            ((orientation & (RelationshipUI.PARENT_FACES_LEFT | RelationshipUI.PARENT_FACES_RIGHT)) != 0)) {
            Interval tp1Vert = new Interval(tp1.getY(), tp1.getHeight());
            Interval tp2Vert = new Interval(tp2.getY(), tp2.getHeight());
            Interval vertOverlap = tp1Vert.findOverlap(tp2Vert);
            if (vertOverlap != null) {
                int y = vertOverlap.getMidpoint();
                logger.debug("Found vertical overlap at y = " + y);
                r.setPkConnectionPoint(new Point(r.getPkConnectionPoint().x, y - tp1.getY()));
                r.setFkConnectionPoint(new Point(r.getFkConnectionPoint().x, y - tp2.getY()));
                ((BasicRelationshipUI)(r.getUI())).fixConnectionPoints();
                
            }
        } else if (((orientation & (RelationshipUI.CHILD_FACES_TOP | RelationshipUI.CHILD_FACES_BOTTOM)) != 0) &&
                   ((orientation & (RelationshipUI.PARENT_FACES_TOP | RelationshipUI.PARENT_FACES_BOTTOM)) != 0)) {
            Interval tp1Horiz = new Interval(tp1.getX(), tp1.getWidth());
            Interval tp2Horiz = new Interval(tp2.getX(), tp2.getWidth());
            Interval horizOverlap = tp1Horiz.findOverlap(tp2Horiz);
            if (horizOverlap != null) {
                int x = horizOverlap.getMidpoint();
                logger.debug("Found horizontal overlap at x = " + x);
                r.setPkConnectionPoint(new Point(x - tp1.getX(), r.getPkConnectionPoint().y));
                r.setFkConnectionPoint(new Point(x - tp2.getX(), r.getFkConnectionPoint().y));
                ((BasicRelationshipUI)(r.getUI())).fixConnectionPoints();
            }
        } else {
            logger.debug("No facing sides found. Skipping this relationship.");
        }
    }

    /**
     * The Interval class represents line segments along the integer number line.
     * A line segment has a starting location and an ending location.  This class
     * ensures that start &lt;= end.
     */
    private static class Interval {
        final int x1;
        final int x2;
        
        Interval(int start, int length) {
            if (length >= 0) {
                x1 = start;
                x2 = start + length;
            } else {
                x1 = start + length;
                x2 = start;
            }
        }
        
        public Interval findOverlap(Interval other) {
            
            // a1, a2 are one interval (start, end); b1, b2 are the other interval (start, end)
            int a1, a2, b1, b2;
            
            // set up so a1 <= b1, which will cut the number of cases in half
            if (other.x1 > x1) {
                a1 = x1;
                a2 = x2;
                b1 = other.x1;
                b2 = other.x2;
            } else {
                a1 = other.x1;
                a2 = other.x2;
                b1 = x1;
                b2 = x2;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Looking for overlap in intervals ("+a1+","+a2+") and ("+b1+","+b2+")");
            }
            if (b1 <= a2) {
                Interval overlap = new Interval(b1, Math.min(a2, b2) - b1);
                logger.debug("Found overlap: " + overlap);
                return overlap;
            } else {
                logger.debug("No overlap found.");
                return null;
            }
        }
        
        /**
         * Returns the approximate midpoint between this interval's beginning
         * and end points.
         */
        public int getMidpoint() {
            return (x1 + x2) / 2;
        }
        
        @Override
        public String toString() {
            return "("+x1+","+x2+")";
        }
        
    }

}
