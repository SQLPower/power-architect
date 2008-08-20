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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentUI;

public class UsageComponentUI implements PlayPenComponentUI {
    
    private static final Logger logger = Logger.getLogger(UsageComponent.class);

    /**
     * The usage component this delegate belongs to.
     */
    private UsageComponent c;
    
    /**
     * The maximum distance from the visible line of this component that is
     * still considered "inside" it. The value of this variable influences the
     * {@link #contains(Point)} method.
     */
    private double selectionFuzziness = 3.1415926545;
    
    /**
     * Handles changes in the components that affect the appearance of this component.
     */
    private PropertyChangeListener componentEventHandler = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            if ("location".equals(evt.getPropertyName()) && evt.getSource() != c) {
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
        OLAPPane<?, ?> p2 = c.getPane2();
        
        Point p1c = centreOf(p1.getBounds());
        Point p2c = centreOf(p2.getBounds());
        
        p1c.translate(-c.getX(), -c.getY());
        p2c.translate(-c.getX(), -c.getY());

        if (c.isSelected()) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(c.getForegroundColor());
        }
        g2.drawLine(p1c.x, p1c.y, p2c.x, p2c.y);
    }

    public void revalidate() {
        Point p1c = centreOf(c.getPane1().getBounds());
        Point p2c = centreOf(c.getPane2().getBounds());
        
        Rectangle r = new Rectangle(p1c.x, p1c.y, 0, 0);
        r.add(p2c);
        
        c.setBounds(r.x, r.y, r.width, r.height);
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
        uc.removePropertyChangeListener(componentEventHandler);
        uc.getPane1().addPropertyChangeListener(componentEventHandler);
        uc.getPane2().addPropertyChangeListener(componentEventHandler);
        this.c = null;
    }

    public void installUI(PlayPenComponent c) {
        if (this.c != null) {
            throw new IllegalStateException("This delegate is already installed on a component");
        }
        UsageComponent uc = (UsageComponent) c;
        this.c = uc;
        uc.addPropertyChangeListener(componentEventHandler);
        uc.getPane1().addPropertyChangeListener(componentEventHandler);
        uc.getPane2().addPropertyChangeListener(componentEventHandler);
    }

}
