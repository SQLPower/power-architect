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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.RepaintManager;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;

/**
 * Navigator defines the behaviours of the overview navigation panel. It
 * captures the whole Playpen and scales it down so that a user can easily
 * navigate to parts of the Playpen.
 * 
 * @author kaiyi
 * 
 */
public class Navigator extends JPanel implements PropertyChangeListener, SQLObjectListener {

    private static final int SCALED_IMAGE_WIDTH = 200;

    private static final int SCALED_IMAGE_HEIGHT = 125;

    private PlayPen pp;

    /**
     * The factor which the entire Playpen is scaled down to
     */
    private double scaleFactor;

    public Navigator(PlayPen pp) {
        super();
        this.pp = pp;
        if (pp != null) {
            try {
                ArchitectUtils.listenToHierarchy(this, pp.getSession().getTargetDatabase());
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
            pp.getPlayPenContentPane().addPropertyChangeListener("location", this);
            pp.getPlayPenContentPane().addPropertyChangeListener("connectionPoints", this);
            pp.getSession().getArchitectFrame().addPropertyChangeListener("viewPort", this);
        }
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                adjustViewPort(e.getPoint());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                adjustViewPort(e.getPoint());
            }
        });
        setPreferredSize(new Dimension(SCALED_IMAGE_WIDTH, SCALED_IMAGE_HEIGHT));
    }

    /**
     * Paints the scaled Playpen contents and an unfilled green rectangle
     * indicating the current view portion on the Playpen.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension playpenArea = pp.getUsedArea();

        scaleFactor = Math.min(SCALED_IMAGE_WIDTH / playpenArea.getWidth(), SCALED_IMAGE_HEIGHT /
                playpenArea.getHeight());
        ((Graphics2D) g).scale(scaleFactor, scaleFactor);
        RepaintManager currentManager = RepaintManager.currentManager(this);
        try {
            currentManager.setDoubleBufferingEnabled(false);
            if (pp.isRenderingAntialiased() == true) {
                try {
                    pp.setRenderingAntialiased(false);
                    pp.paint(g);
                } finally {
                    pp.setRenderingAntialiased(true);
                }
            } else {
                pp.paint(g);
            }
        } finally {
            currentManager.setDoubleBufferingEnabled(true);
        }
        if (playpenArea.width < SCALED_IMAGE_WIDTH || playpenArea.height < SCALED_IMAGE_HEIGHT) {
            return;
        }
        Rectangle view = pp.getVisibleRect();
        g.setColor(Color.GREEN);
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g.drawRect(view.x, view.y, view.width, view.height);
    }

    /**
     * Returns the corresponding point on the Playpen given a point on the
     * Navigator
     */
    private Point getPointOnPlaypen(Point pointOnNavigator) {
        return new Point((int) (pointOnNavigator.x / scaleFactor), (int) (pointOnNavigator.y / scaleFactor));
    }

    /**
     * Adjusts and moves the view portion of the playpen given a destination
     * point on the Navigator
     */
    private void adjustViewPort(Point pointOnNavigator) {
        Point pointOnPlaypen = getPointOnPlaypen(pointOnNavigator);
        Dimension viewSize = pp.getViewportSize();
        Dimension usedArea = pp.getUsedArea();
        pointOnPlaypen.translate(-(int) (viewSize.width / 2), -(int) (viewSize.height / 2));

        if (pointOnPlaypen.x < 0) {
            if (pointOnPlaypen.y + viewSize.height <= usedArea.height) {
                pp.setViewPosition(new Point(0, pointOnPlaypen.y < 0 ? 0 : pointOnPlaypen.y));
            } else if (pointOnPlaypen.y + viewSize.height > usedArea.height && pointOnPlaypen.y - viewSize.height >= 0) {
                pp.setViewPosition(new Point(0, usedArea.height - viewSize.height < 0 ? 0 : usedArea.height -
                        viewSize.height));
            }
        } else if (pointOnPlaypen.y < 0) {
            if (pointOnPlaypen.x + viewSize.width <= usedArea.width) {
                pp.setViewPosition(new Point(pointOnPlaypen.x < 0 ? 0 : pointOnPlaypen.x, 0));
            } else if (pointOnPlaypen.x + viewSize.width > usedArea.width && pointOnPlaypen.x - viewSize.width >= 0) {
                pp.setViewPosition(new Point(usedArea.width - viewSize.width < 0 ? 0 : usedArea.width - viewSize.width,
                        0));
            }
        } else if (pointOnPlaypen.x + viewSize.width > usedArea.width) {
            if (pointOnPlaypen.y + viewSize.height <= usedArea.height) {
                pp.setViewPosition(new Point(usedArea.width - viewSize.width < 0 ? 0 : usedArea.width - viewSize.width,
                        pointOnPlaypen.y));
            } else if (pointOnPlaypen.y + viewSize.height > usedArea.height && pointOnPlaypen.y - viewSize.height >= 0) {
                pp.setViewPosition(new Point(usedArea.width - viewSize.width < 0 ? 0 : usedArea.width - viewSize.width,
                        usedArea.height - viewSize.height < 0 ? 0 : usedArea.height - viewSize.height));
            }
        } else if (pointOnPlaypen.y + viewSize.height > usedArea.height) {
            if (pointOnPlaypen.x + viewSize.width <= usedArea.width) {
                pp.setViewPosition(new Point(pointOnPlaypen.x, usedArea.height - viewSize.height < 0 ? 0
                        : usedArea.height - viewSize.height));
            } else if (pointOnPlaypen.x + viewSize.width > usedArea.width && pointOnPlaypen.x - viewSize.width >= 0) {
                pp.setViewPosition(new Point(usedArea.width - viewSize.width < 0 ? 0 : usedArea.width - viewSize.width,
                        usedArea.height - viewSize.height < 0 ? 0 : usedArea.height - viewSize.height));
            }
        } else {
            pp.setViewPosition(pointOnPlaypen);
        }
        repaint();
    }

    /**
     * Refreshes the navigator upon a visible property change
     */
    public void propertyChange(PropertyChangeEvent evt) {
        repaint();

    }

    /**
     * Refreshes the navigator upon the addition of a new PlaypenComponent
     */
    public void dbChildrenInserted(SQLObjectEvent e) {
        repaint();

    }

    /**
     * Refreshes the navigator upon the removal of a PlaypenComponent
     */
    public void dbChildrenRemoved(SQLObjectEvent e) {
        repaint();

    }

    public void dbObjectChanged(SQLObjectEvent e) {
        repaint();
    }

    public void dbStructureChanged(SQLObjectEvent e) {

    }
}
