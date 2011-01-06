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

package ca.sqlpower.architect.swingui.olap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentUI;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;

public class UsageComponentUI implements PlayPenComponentUI {
    
    private static final Logger logger = Logger.getLogger(UsageComponentUI.class);

    /**
     * The usage component this delegate belongs to.
     */
    private UsageComponent c;
    
    /**
     * The size of the blob to paint where this line intersects the edge of each pane.
     */
    private int blobRadius = 4;

    /**
     * The maximum distance from the visible line of this component that is
     * still considered "inside" it. The value of this variable influences the
     * {@link #contains(Point)} method.
     */
    private double selectionFuzziness = 3.1415926545;
    
    /**
     * Handles changes in the components that affect the appearance of this component.
     */
    private SPListener componentEventHandler = new AbstractSPListener() {

        public void propertyChanged(PropertyChangeEvent evt) {
            if (("topLeftCorner".equals(evt.getPropertyName()) || 
                    "lengths".equals(evt.getPropertyName())) && evt.getSource() != c) {
                revalidate();
                c.repaint();
            }
        }
    };

    public boolean intersects(Rectangle rubberBand) {
        Point p1c = centreOf(c.getPane1().getBounds());
        Point p2c = centreOf(c.getPane2().getBounds());
        Line2D line = new Line2D.Double(p1c.x, p1c.y, p2c.x, p2c.y);
        return line.intersects(
                rubberBand.x, rubberBand.y, rubberBand.width, rubberBand.height);
    }
    
    public boolean contains(Point p) {
        Point p1c = centreOf(c.getPane1().getBounds());
        Point p2c = centreOf(c.getPane2().getBounds());
        boolean containsPoint =
            Line2D.ptLineDist(p1c.x, p1c.y, p2c.x, p2c.y, p.x, p.y) <= selectionFuzziness;
        logger.debug("Contains point " + p + "? " + containsPoint);
        return containsPoint;
    }

    public Dimension getPreferredSize() {
        Point p1c = centreOf(c.getPane1().getBounds());
        Point p2c = centreOf(c.getPane2().getBounds());
        
        return new Dimension(
                Math.abs(p1c.x - p2c.x),
                Math.abs(p1c.y - p2c.y));
    }

    public void paint(Graphics2D g2) {
        OLAPPane<?, ?> p1 = c.getPane1();
        Rectangle p1b = p1.getBounds();
        Point p1c = centreOf(p1b);

        OLAPPane<?, ?> p2 = c.getPane2();
        Rectangle p2b = p2.getBounds();
        Point p2c = centreOf(p2b);
        
        Point p1edge = insersectionPoint(p1b, p1c, p2c);
        Point p2edge = insersectionPoint(p2b, p2c, p1c);
        
        if (c.isSelected()) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(c.getForegroundColor());
        }
        
        /// ----------------- above this line, everything is in playpen coordinates
        
        p1c.translate(-c.getX(), -c.getY());
        p2c.translate(-c.getX(), -c.getY());
        
        if (p1edge != null) {
            p1edge.translate(-c.getX(), -c.getY());
        }
        if (p2edge != null) {
            p2edge.translate(-c.getX(), -c.getY());
        }
        
        /// ----------------- below this line, everything is in component coordinates

        g2.drawLine(p1c.x, p1c.y, p2c.x, p2c.y);

        if (p1edge != null) {
            g2.fillOval(p1edge.x - blobRadius, p1edge.y - blobRadius, blobRadius * 2, blobRadius * 2);
        }
        if (p2edge != null) {
            g2.fillOval(p2edge.x - blobRadius, p2edge.y - blobRadius, blobRadius * 2, blobRadius * 2);
        }
    }

    /**
     * Finds the point of intersection between the line (p1,p2) and one of the
     * given rectangle's four edges. This will only work if p1 is inside r.
     * 
     * @param r
     *            The rectangle
     * @param p1
     *            A point inside the rectangle
     * @param p2
     *            A point anywhere (defines the other end of the line)
     * @return The point where the line (p1,p2) intersects one of the edges of
     *         r. Returns null if the line does not intersect r (this would be
     *         because p2 is also inside r).
     */
    private Point insersectionPoint(Rectangle r, Point p1, Point p2) {
        Point i;
        
        int outcode = r.outcode(p2.x, p2.y);
        if ( (outcode & (Rectangle.OUT_LEFT | Rectangle.OUT_RIGHT)) != 0) {
            int x;
            if ( (outcode & Rectangle.OUT_LEFT) != 0) {
                x = r.x;
            } else {
                x = r.x + r.width;
            }
            int y = y(p2, p1, x);
            if (y < r.y) {
                y = r.y;
                x = x(p1, p2, y);
            } else if (y > r.y + r.height) {
                y = r.y + r.height;
                x = x(p1, p2, y);
            }
            i = new Point(x, y);
        } else if ( (outcode & Rectangle.OUT_TOP) != 0) {
            int y = r.y;
            int x = x(p1, p2, y);
            i = new Point(x, y);
        } else if ( (outcode & Rectangle.OUT_BOTTOM) != 0) {
            int y = r.y + r.height;
            int x = x(p1, p2, y);
            i = new Point(x, y);
        } else {
            // the point is inside p1b
            i = null;
        }
        return i;
    }

    public void revalidate() {
        Point p1c = centreOf(c.getPane1().getBounds());
        Point p2c = centreOf(c.getPane2().getBounds());
        
        Rectangle r = new Rectangle(p1c.x, p1c.y, 0, 0);
        r.add(p2c);
        
        c.setBounds(r.x, r.y, r.width, r.height);
    }

    /**
     * Finds the Y coordinate where the line through points (p1,p2)
     * passes through X.
     * 
     * @param p1 One point on the line
     * @param p2 The other point on the line
     * @param x The x coordinate to find Y for
     * @return The y coordinate of the line at x
     */
    public static int y(Point2D p1, Point2D p2, double x) {
        double m1 = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
        double a = -m1 * p1.getX() + p1.getY();
        
        logger.debug("m1 = " + m1);
        double y = (m1 * x) + a;
        return (int) y;
    }

    /**
     * Finds the X coordinate where the line through points (p1,p2)
     * passes through Y.
     * 
     * @param p1 One point on the line
     * @param p2 The other point on the line
     * @param y The y coordinate to find X for
     * @return The x coordinate of the line at y
     */
    public static int x(Point2D p1, Point2D p2, double y) {
        if (p2.getX() == p1.getX()) {
            return (int) p2.getX();
        }
        
        double m1 = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
        double a = -m1 * p1.getX() + p1.getY();
        
        double x = (y - a) / m1;
        return (int) x;
    }

    protected Point centreOf(Rectangle r) {
        return new Point((int) r.getCenterX(), (int) r.getCenterY());
    }
    
    public void uninstallUI(PlayPenComponent c) {
        if (c != this.c) {
            throw new IllegalArgumentException(
                    "Attempted to uninstall this delegate from the wrong component");
        }
        UsageComponent uc = (UsageComponent) c;
        uc.removeSPListener(componentEventHandler);
        uc.getPane1().addSPListener(componentEventHandler);
        uc.getPane2().addSPListener(componentEventHandler);
        this.c = null;
    }

    public void installUI(PlayPenComponent c) {
        if (this.c != null) {
            throw new IllegalStateException("This delegate is already installed on a component");
        }
        UsageComponent uc = (UsageComponent) c;
        this.c = uc;
        uc.addSPListener(componentEventHandler);
        uc.getPane1().addSPListener(componentEventHandler);
        uc.getPane2().addSPListener(componentEventHandler);
    }

    public Point getPointForModelObject(Object modelObject) {
        return c.getLocation();
    }
}
