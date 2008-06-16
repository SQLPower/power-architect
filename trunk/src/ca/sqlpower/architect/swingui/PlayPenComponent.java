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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentListener;
import ca.sqlpower.architect.swingui.event.RelationshipConnectionPointEvent;

/**
 * PlayPenComponent is the base class for a component that can live in the playpen's
 * content pane.
 */
public abstract class PlayPenComponent implements Selectable {

	private static final Logger logger = Logger.getLogger(PlayPenComponent.class);

	private PlayPenContentPane parent;
	private Rectangle bounds = new Rectangle();
	protected Color backgroundColor;
	private Insets insets = new Insets(0,0,0,0);
	private String name;
	protected Color foregroundColor;
	private String toolTipText;
	private boolean opaque;
	
	private PlayPenComponentUI ui;

	private List propertyChangeListeners = new ArrayList();

	private List playPenComponentListeners = new ArrayList();
	
	protected PlayPenComponent(PlayPenContentPane parent) {
		this.parent = parent;
	}

	public PlayPen getPlayPen() {
		if (parent == null) return null;
		return parent.getOwner();
	}

	
	public PlayPenComponentUI getUI() {
		return ui;
	}

	public void setUI(PlayPenComponentUI ui) {
		this.ui = ui;
		revalidate();
	}

	/**
	 * Shows the given popup menu on the PlayPen that owns this table
	 * pane because it doesn't work to show it on this component,
	 * which is not really part of the swing hierarchy.
	 *
	 * @param menu the menu to show
	 * @param p the point (relative to this component's top-left
	 * corner) to show it at.
	 */
	public void showPopup(JPopupMenu menu, Point p) {
        final int xAdjust = 5;  // ensure menu doesn't appear directly under pointer
		p.translate(getX(), getY());
		getPlayPen().zoomPoint(p);
		menu.show(getPlayPen(), p.x + xAdjust, p.y);
	}
	
	/**
	 * Translates this request into a call to
	 * PlayPen.repaint(Rectangle).  That will eventually cause a call
	 * to PlayPen.paint(). Painting a TablePane causes it to re-evaluate its
	 * preferred size, which is what validation is really all about.
	 */
	public void revalidate() {
		PlayPen pp = getPlayPen();
		if (pp == null) {
			logger.debug("getPlayPen() returned null.  Not generating repaint request.");
			return;
		}
		Rectangle r = new Rectangle(bounds);
		PlayPenComponentUI ui = getUI();
		if (ui != null) {
			ui.revalidate();
			Dimension ps = ui.getPreferredSize();
			if (ps != null) setSize(ps);
		}
		pp.zoomRect(r);
		if (logger.isDebugEnabled()) logger.debug("Scheduling repaint at "+r);
		pp.repaint(r);
	}

	/**
	 * Updates the bounds of this component, then issues a repaint to the
	 * PlayPen which covers the old bounds of this component. This will allow
	 * newly-exposed sections of the PlayPen to draw themselves in case this
	 * setBounds call is shrinking this component.  Also ensures the new bounds
	 * do not remain left of or above the (0,0) point by normalizing the play pen.
	 * 
	 * <p>All methods that affect the bounds rectangle should do so by calling this
	 * method.
	 */
	protected void setBoundsImpl(int x, int y, int width, int height) { 
		Rectangle oldBounds = getBounds(); 
		PlayPen owner = getPlayPen();
		if (owner != null) {
			Rectangle r = getBounds();
			double zoom = owner.getZoom();
			owner.repaint((int) Math.floor((double) r.x * zoom),
						  (int) Math.floor((double) r.y * zoom),
						  (int) Math.ceil((double) r.width * zoom),
						  (int) Math.ceil((double) r.height * zoom));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("[36mUpdating bounds on "+getName()
						 +" to ["+x+","+y+","+width+","+height+"][0m");
		}
		Point oldPoint = new Point(bounds.x,bounds.y);
		bounds.setBounds(x,y,width,height);
		
		
		
		if (oldBounds.x != x || oldBounds.y != y) {
			firePlayPenComponentMoved(oldPoint, new Point(x,y));
		}
		
		if (oldBounds.width != width || oldBounds.height != height) {
			firePlayPenComponentResized();
		}

		repaint();
	}

	/**
	 * See setBoundsImpl.
	 */
	public void setBounds(int x, int y, int width, int height) {
		setBoundsImpl(x, y, width, height);
	}

	/**
	 * Returns a copy of this component's bounding rectangle.
	 */
	public Rectangle getBounds() {
		return getBounds(null);
	}

	/**
	 * Sets the given rectangle to be identical to this component's bounding box.
	 * 
	 * @param r An existing rectangle.  If null, this method creates a new rectangle for you.
	 * @return r if r was not null; a new rectangle otherwise.
	 */
	public Rectangle getBounds(Rectangle r) {
		if (r == null) r = new Rectangle();
		r.setBounds(bounds);
		return r;
	}

	public Dimension getSize() {
		return new Dimension(bounds.width, bounds.height);
	}
	
	/**
	 * The revalidate() call uses this to determine the component's
	 * correct location.  This implementation just returns the current
	 * location.  Override it if you need to be moved during validation.
	 */
	public Point getPreferredLocation() {
		return getLocation();
	}

	public Point getLocation() {
		return getLocation(null);
	}
	
	/**
	 * Copies this component's location into the given point object.
	 * 
	 * @param p A point that this method will modify.  If you pass in null, this method will
	 * create a new point for you.
	 * @return p if p was not null; a new point otherwise.
	 */
	public Point getLocation(Point p) {
		if (p == null) p = new Point();
		p.x = bounds.x;
		p.y = bounds.y;
		return p;
	}
	
	/**
	 * Updates the on-screen location of this component.  If you try to move this
	 * component to a negative co-ordinate, it will automatically be normalized (along
	 * with everything else in the playpen) to non-negative coordinates.
	 */
	public void setLocation(Point point) {
		setBoundsImpl(point.x,point.y, getWidth(), getHeight());
	}

	/**
	 * Updates the on-screen location of this component.  If you try to move this
	 * component to a negative co-ordinate, it will automatically be normalized (along
	 * with everything else in the playpen) to non-negative coordinates.
	 */
	public void setLocation(int x, int y) {
		setBoundsImpl(x, y, getWidth(), getHeight());
	}
	
	public void setSize(Dimension size) {
		setBoundsImpl(getX(), getY(), size.width, size.height);
	}

	/**
	 * Forwards to {@link #repaint(Rectangle)}.
	 */
	public void repaint() {
		repaint(getBounds());
	}

	/**
	 * Forwards to {@link #repaint(long,int,int,int,int)}.
	 */
	public void repaint(Rectangle r) {
		repaint(0, r.x, r.y, r.width, r.height);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.add(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.remove(l);
	}
	
	protected void firePropertyChange(String propName, Object oldValue, Object newValue) {
		PropertyChangeEvent e = new PropertyChangeEvent(this, propName, oldValue, newValue);
		Iterator it = propertyChangeListeners.iterator();
		while (it.hasNext()) {
			((PropertyChangeListener) it.next()).propertyChange(e);
		}
	}
	
	protected void firePropertyChange(String propName, int oldValue, int newValue) {
		firePropertyChange(propName, new Integer(oldValue), new Integer(newValue));
	}
	
	public void addPlayPenComponentListener(PlayPenComponentListener l) {
		playPenComponentListeners.add(l);
	}
	
	public void removePlayPenComponentListener(PlayPenComponentListener l) {
		playPenComponentListeners.remove(l);
	}
	
	protected void firePlayPenComponentMoved(Point oldPoint,Point newPoint) {
		PlayPenComponentEvent e = new PlayPenComponentEvent(this,oldPoint,newPoint);
		Iterator it = playPenComponentListeners.iterator();
		while (it.hasNext()) {
			((PlayPenComponentListener) it.next()).componentMoved(e);
		}
	}
	
	protected void firePlayPenComponentResized() {
		PlayPenComponentEvent e = new PlayPenComponentEvent(this);
		Iterator it = playPenComponentListeners.iterator();
		while (it.hasNext()) {
			((PlayPenComponentListener) it.next()).componentResized(e);
		}
	}
	
	public void fireRelationshipConnectionPointsMovedByUser(Point pkPoint, Point fkPoint, boolean isStraighteningLine) {
	    RelationshipConnectionPointEvent e;
	    e = new RelationshipConnectionPointEvent(this, pkPoint, fkPoint, isStraighteningLine);
	    Iterator it = playPenComponentListeners.iterator();
	    while (it.hasNext()) {
	        ((PlayPenComponentListener) it.next()).relationshipConnectionPointsMoved(e);
	    }
	}

	
	public int getX() {
		return bounds.x;
	}
	
	public int getY() {
		return bounds.y;
	}
	
	public int getWidth() {
		return bounds.width;
	}
	
	public int getHeight() {
		return bounds.height;
	}
	
	public Insets getInsets() {
		return new Insets(insets.top, insets.left, insets.bottom, insets.right);
	}

	public void setInsets(Insets insets) {
		this.insets = new Insets(insets.top, insets.left, insets.bottom, insets.right);
	}

	/**
	 * Tells the owning PlayPen to repaint the given region.  The
	 * rectangle is manipulated (zoomed) into screen coordinates.
	 */
	public void repaint(long tm,
						int x,
						int y,
						int width,
						int height) {
		PlayPen owner = getPlayPen();
		if (owner == null) return;
 		double zoom = owner.getZoom();
 		owner.repaint((int) Math.floor((double) x * zoom),
 					  (int) Math.floor((double) y * zoom),
 					  (int) Math.ceil((double) width * zoom),
 					  (int) Math.ceil((double) height * zoom));
	}
	
	public boolean isOpaque() {
		return opaque;
	}

	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
		revalidate();
	}

	public Color getBackground() {
		if (backgroundColor == null) {
			return getPlayPen().getBackground();
		}
		return backgroundColor;
	}
	
	public void setBackground(Color c) {
		backgroundColor = c;
		revalidate();
	}

	public Color getForeground() {
		if (foregroundColor == null) {
			return getPlayPen().getForeground();
		}
		return foregroundColor;
	}
	
	public void setForeground(Color c) {
		foregroundColor = c;
		revalidate();
	}
	
	public String getToolTipText() {
		return toolTipText;
	}

	public void setToolTipText(String toolTipText) {
		if (toolTipText == null && this.toolTipText == null) return;
		if (toolTipText != null && toolTipText.equals(this.toolTipText)) return;
		this.toolTipText = toolTipText;
		logger.debug("ToolTipText changed to "+toolTipText);
	}

	public Font getFont() {
		return getPlayPen().getFont();
	}

	public FontMetrics getFontMetrics(Font f) {
		return getPlayPen().getFontMetrics(f);
	}
	
	public FontRenderContext getFontRenderContext() {
		return getPlayPen().getFontRenderContext();
	}
	
	public boolean contains(Point p) {
		return getUI().contains(p);
	}

	public void paint(Graphics2D g2) {
		getUI().paint(g2);
		if (logger.isDebugEnabled()) {
            Color oldColor = g2.getColor();
		    g2.setColor(Color.ORANGE);
            g2.drawRect(0, 0, getWidth(), getHeight());
            g2.setColor(oldColor);
        }
	}

	public Dimension getPreferredSize() {
		return getUI().getPreferredSize();
	}

	public abstract Object getModel();

    public PlayPenContentPane getParent() {
        return parent;
    }
	
}
