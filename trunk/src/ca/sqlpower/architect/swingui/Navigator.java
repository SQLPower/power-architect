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

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;

/**
 * Navigator defines the behaviours of the overview navigation dialog. It
 * captures the whole Playpen and scales it down so that a user can easily
 * navigate to parts of the Playpen.
 * 
 * @author kaiyi
 * 
 */
public class Navigator extends JDialog implements PropertyChangeListener, SQLObjectListener {

    private static final int SCALED_IMAGE_WIDTH = 200;

    private static final int SCALED_IMAGE_HEIGHT = 125;

    private PlayPen pp;

    /**
     * The factor which the entire Playpen is scaled down to
     */
    private double scaleFactor;

    /**
     * Creates a Navigator dialog that displays a scaled down version of the playpen.
     * 
     * @param session Session of the architect frame creating this dialog.
     * @param location Top right corner where this dialog should be placed.
     */
    public Navigator(ArchitectSwingSession session, Point location) {
        super(session.getArchitectFrame(), Messages.getString("Navigator.name")); //$NON-NLS-1$
        this.pp = session.getPlayPen();
        if (pp != null) {
            try {
                ArchitectUtils.listenToHierarchy(this, pp.getSession().getTargetDatabase());
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
            pp.getPlayPenContentPane().addPropertyChangeListener("location", this);
            pp.getPlayPenContentPane().addPropertyChangeListener("connectionPoints", this);
            pp.getSession().getArchitectFrame().addPropertyChangeListener("viewPort", this);
            pp.getPlayPenContentPane().addPropertyChangeListener("backgroundColor", this);
            pp.getPlayPenContentPane().addPropertyChangeListener("foregroundColor", this);
            pp.getPlayPenContentPane().addPropertyChangeListener("dashed", this);
            pp.getPlayPenContentPane().addPropertyChangeListener("rounded", this);
        }
        
        final JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                double width = Math.max(pp.getUsedArea().getWidth(), pp.getViewportSize().getWidth());
                double height = Math.max(pp.getUsedArea().getHeight(), pp.getViewportSize().getHeight());

                scaleFactor = Math.min(SCALED_IMAGE_WIDTH / width, SCALED_IMAGE_HEIGHT / height);
                ((Graphics2D) g).scale(scaleFactor, scaleFactor);
                RepaintManager currentManager = RepaintManager.currentManager(this);
                try {
                    currentManager.setDoubleBufferingEnabled(false);
                    if (pp.isRenderingAntialiased() == true) {
                        try {
                            pp.setRenderingAntialiased(false);
                            pp.paintComponent(g);
                        } finally {
                            pp.setRenderingAntialiased(true);
                        }
                    } else {
                        pp.paintComponent(g);
                    }
                } finally {
                    currentManager.setDoubleBufferingEnabled(true);
                }

                Rectangle view = pp.getVisibleRect();
                g.setColor(Color.GREEN);
                ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g.drawRect(view.x, view.y, view.width, view.height-5);
            }
        };
        
        panel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                adjustViewPort(e.getPoint());
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                adjustViewPort(e.getPoint());
            }
        });
        
        panel.setPreferredSize(new Dimension(SCALED_IMAGE_WIDTH, SCALED_IMAGE_HEIGHT));
        setContentPane(panel);
        
        
        pack();
        location.translate(-getWidth(), 0);
        setLocation(location);
        setResizable(false);
        setVisible(true);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        cleanup();
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

        // makes the given point the center of the resulting viewport
        pointOnPlaypen.translate(-viewSize.width / 2, -viewSize.height / 2);
        
        int x = pointOnPlaypen.x;
        int y = pointOnPlaypen.y;
        
        int xBoundary = Math.max(0, usedArea.width - viewSize.width);
        int yBoundary = Math.max(0, usedArea.height - viewSize.height);
        
        if (x < 0) {
            x = 0;
        } else if (x > xBoundary) {
            x = xBoundary;
        }
        
        if (y < 0) {
            y = 0;
        } else if (y > yBoundary) {
            y = yBoundary;
        }
        
        pp.setViewPosition(new Point(x, y));
        
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
        
        SQLObject[] children = e.getChildren();
        for (SQLObject child : children) {
            try {
                ArchitectUtils.listenToHierarchy(this, child);
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
        }
    }

    /**
     * Refreshes the navigator upon the removal of a PlaypenComponent
     */
    public void dbChildrenRemoved(SQLObjectEvent e) {
        repaint();
        
        SQLObject[] children = e.getChildren();
        for (SQLObject child : children) {
            try {
                ArchitectUtils.unlistenToHierarchy(this, child);
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
        }
    }

    public void dbObjectChanged(SQLObjectEvent e) {
        repaint();
    }

    public void dbStructureChanged(SQLObjectEvent e) {
    }
    
    /**
     * Removes this listener from connected objects.
     */
    public void cleanup() {
        try {
            ArchitectUtils.unlistenToHierarchy(this, pp.getSession().getTargetDatabase());
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
        pp.getPlayPenContentPane().removePropertyChangeListener(this);
        pp.getPlayPenContentPane().removePropertyChangeListener(this);
        pp.getSession().getArchitectFrame().removePropertyChangeListener(this);
    }
}
