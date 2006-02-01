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

/**
 * PlayPenComponent is the base class for a component that can live in the playpen's
 * content pane.
 */
public abstract class PlayPenComponent implements Selectable {

	private static final Logger logger = Logger.getLogger(PlayPenComponent.class);

	private PlayPenContentPane parent;
	private Rectangle bounds = new Rectangle();
	private Color backgroundColor;
	private Insets insets = new Insets(0,0,0,0);
	private String name;
	private Color foregroundColor;
	private String toolTipText;
	private boolean moving=false;
	
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
		p.translate(getX(), getY());
		getPlayPen().zoomPoint(p);
		menu.show(getPlayPen(), p.x, p.y);
	}
	
	/**
	 * Translates this request into a call to
	 * PlayPen.repaint(Rectangle).  That will eventually cause a call
	 * to PlayPen.paint(). This override is required because the
	 * PlayPen's contents are not part of the Swing containment
	 * hierarchy, and the standard RepaintManager discards
	 * revalidate() calls for invisible/unreachable components.
	 *
	 * <p>FIXME: should we check if this is the event dispatch thread?
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
			Dimension ps = ui.getPreferredSize();
			if (ps != null) setSize(ps);
		}
		pp.zoomRect(r);
		if (logger.isDebugEnabled()) logger.debug("Scheduling repaint at "+r);
		pp.repaint(r);
	}

	/**
	 * Issues a repaint to the PlayPen which covers the old bounds of
	 * this component.  This will allow newly-exposed sections of the
	 * PlayPen to draw themselves in case this setBounds call is
	 * shrinking this component. Normally in Swing, this is done
	 * automatically to the parent component but because of the
	 * zooming implementation, the PlayPen is not our parent.
	 */
	public void setBounds(int x, int y, int width, int height) {
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
	 * Set a new point along the movement path
	 *
	 */
	public void setMovePathPoint(Point point)
	{
		setMovePathPoint(point.x,point.y);
	}
	/**
	 * Set a new point along the movement path
	 *
	 */
	public void setMovePathPoint(int x,int y)
	{
	
		Point oldPoint = new Point(bounds.x,bounds.y);
		PlayPen owner = getPlayPen();
		if (owner != null) {
			Rectangle r = getBounds();
			double zoom = owner.getZoom();
			owner.repaint((int) Math.floor((double) r.x * zoom),
						  (int) Math.floor((double) r.y * zoom),
						  (int) Math.ceil((double) r.width * zoom),
						  (int) Math.ceil((double) r.height * zoom));
		}
		if (bounds.x != x || bounds.y != y) {
			bounds.x = x;
			bounds.y = y;
			firePlayPenComponentMoved(oldPoint,new Point(x,y));
		}
	}
	
	/**
	 * Perform a single move operation on one component.
	 * If you need to do multiple moves or move multiple items at once
	 * use @link{setMovePathPoint(Point)}
	 * 
	 * @param point
	 */
	
	public void setLocation(Point point) {
		setLocation(point.x,point.y);
	}
	/**
	 * Perform a single move operation on one component.
	 * If you need to do multiple moves or move multiple items at once
	 * use @link{setMovePathPoint(int,int)}
	 *
	 */
	public void setLocation(int x, int y) {
	
		setMoving(true);
		setMovePathPoint(x,y);
		setMoving(false);
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
	

	

	protected void firePlayPenComponentMoveStart(Point oldPoint) {
		PlayPenComponentEvent e = new PlayPenComponentEvent(this,oldPoint,null);
		Iterator it = playPenComponentListeners.iterator();
		while (it.hasNext()) {
			((PlayPenComponentListener) it.next()).componentMoveStart(e);
		}
	}

	protected void firePlayPenComponentMoveEnd(Point newPoint) {
		PlayPenComponentEvent e = new PlayPenComponentEvent(this,null,newPoint);
		Iterator it = playPenComponentListeners.iterator();
		while (it.hasNext()) {
			((PlayPenComponentListener) it.next()).componentMoveEnd(e);
		}
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
		
	}

	public Dimension getPreferredSize() {
		return getUI().getPreferredSize();
	}

	public void setSize(Dimension size) {
		bounds.height = size.height;
		bounds.width = size.width;
		firePlayPenComponentResized();
		
	}


	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		if(moving && !this.moving){
			firePlayPenComponentMoveStart(new Point(bounds.x,bounds.y));
		}
		else if (!moving && this.moving)
		{
			firePlayPenComponentMoveEnd(new Point(bounds.x,bounds.y));
		}
		else
		{
			logger.debug("Trying to change the moving state to the current state of moving="+moving);
		}
		this.moving = moving;
	}

	
}
