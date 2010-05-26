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
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.enterprise.NetworkConflictResolver;
import ca.sqlpower.architect.enterprise.NetworkConflictResolver.UpdateListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;

/**
 * PlayPenComponent is the base class for a component that can live in the playpen's
 * content pane.
 */
public abstract class PlayPenComponent extends AbstractSPObject
implements Selectable {
    
    private static final Logger logger = Logger.getLogger(PlayPenComponent.class);    

    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections.emptyList();
    
    private Rectangle bounds = new Rectangle();
    private Dimension minimumSize = new Dimension();
    protected Color backgroundColor;
    protected Color foregroundColor;
    private Insets insets = new Insets(0,0,0,0);
    private String toolTipText;
    private boolean opaque;
    
    private PlayPenComponentUI ui;

    /**
     * A selected component is one that the user has clicked on. It will appear
     * more prominently than non-selected ContainerPane, and its status as
     * selected makes it the target of actions that are invoked on the playpen.
     */
    protected boolean selected;

    protected boolean componentPreviouslySelected;
    
    /**
     * Keeps track of whether this component is being dragged or not.
     */
    private boolean isBeingDragged = false;
    
    /**
     * This listens for updates in case the user is dragging a table     
     * the same time an update comes in.
     */
    private final UpdateListener updateWhileMovingListener = new UpdateListener() {       
        public boolean updatePerformed(NetworkConflictResolver resolver) {
            if (isBeingDragged) {
                doneDragging(false);
            }
            return true;
        }
        
        public boolean updateException(NetworkConflictResolver resolver) {return false;}

        public void preUpdatePerformed(NetworkConflictResolver resolver) {
            //do nothing
        }
        
        public void workspaceDeleted() {
            // do nothing
        }
    };
    
    protected PlayPenComponent(String name) {
        setName(name);
    }
    
    protected PlayPenComponent(String name, PlayPenContentPane parent) {
        this(name);
        setParent(parent);
    }

    /**
     * Copy constructor. Makes deep copies of all PlayPenComponent state.
     * Subclasses that implement a copy constructor should chain to this
     * constructor in their own copy constructors.
     * <p>
     * The parent reference of the new copy will be null. Copy constructors in
     * subclasses should also leave the parent pointer null--it is up to the
     * code initiating the copy to add the newly-copied component to some parent
     * (if you want it to belong to some parent).
     * 
     * @param copyMe the playpen component this new component should be a copy of
     * @param parent the parent content pane of this new copy
     */
    protected PlayPenComponent(PlayPenComponent copyMe, PlayPenContentPane parent) {
        this(copyMe.getName(), parent);
        backgroundColor = copyMe.backgroundColor;
        if (copyMe.bounds != null) {
            bounds = new Rectangle(copyMe.bounds);
        }
        componentPreviouslySelected = copyMe.componentPreviouslySelected;
        foregroundColor = copyMe.foregroundColor;
        if (copyMe.insets != null) {
            insets = new Insets(
                    copyMe.insets.top, copyMe.insets.left,
                    copyMe.insets.bottom, copyMe.insets.right);
        }
        if (copyMe.minimumSize != null) {
            minimumSize = new Dimension(copyMe.minimumSize);
        }
        opaque = copyMe.opaque;
        setParent(parent);
        // pcs should not be copied
        selected = copyMe.selected;
        // selectionListeners should not be copied
        toolTipText = copyMe.toolTipText;
        // ui should not be copied, but subclass should call updateUI()
    }

    @Transient @Accessor
    public PlayPen getPlayPen() {
        if (getParent() == null) return null;
        return getParent().getPlayPen();
    }
    
    @Transient @Accessor
    public PlayPenComponentUI getUI() {
        return ui;
    }

    @Transient @Mutator
    public void setUI(PlayPenComponentUI ui) {
        PlayPenComponentUI oldValue = this.ui;
        this.ui = ui;
        firePropertyChange("UI", oldValue, ui);
    }

    /**
     * Shows the component's popup menu on the PlayPen that owns this component
     * because it doesn't work to show it on this component, which is not really
     * part of the swing hierarchy. Only executed if the component has a popup
     * menu, see {@link #getPopup()}.
     * 
     * @param p
     *            the point (relative to this component's top-left corner) to
     *            show it at.
     */
    public void showPopup(Point p) {
    	JPopupMenu menu = getPopup(new Point(p));
        if (menu != null) {
            final int xAdjust = 5;  // ensure menu doesn't appear directly under pointer
            p.translate(getX(), getY());
            getPlayPen().zoomPoint(p);
            menu.show(getPlayPen(), p.x + xAdjust, p.y);
        }
    }
    
    /**
     * Returns a component specific popup menu. Defaulted here to null
     * so components that have popup menus must override this class. 
     */
    @NonBound
    public JPopupMenu getPopup(Point p) {
        return null;
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
            logger.debug("getPlayPen() returned null.  Not generating repaint request."); //$NON-NLS-1$
            return;
        } else {
            Rectangle r = new Rectangle(bounds);
            if (isMagicEnabled()) { 
                // This temporary disabling of magic is under the
                // assumption that this method properly revalidates
                // the component in one pass, and does not rely 
                // on recursive calls due to magical side effects
                setMagicEnabled(false);
                PlayPenComponentUI ui = getUI();
                if (ui != null) {
                    ui.revalidate();
                    Dimension ps = ui.getPreferredSize();
                    if (ps != null) setSize(ps);
                }            
                if (logger.isDebugEnabled()) logger.debug("Scheduling repaint at "+r); //$NON-NLS-1$
                setMagicEnabled(true);
            }
            pp.zoomRect(r);
            pp.repaint(r);
        }
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
    @Transient @Mutator
    public void setBounds(int x, int y, int width, int height) { 
        setBounds(new Rectangle(x, y, width, height));
    }
    
    @Mutator
    public void setBounds(Rectangle r) {
        Rectangle oldBounds = getBounds();   
        repaint();

        if (logger.isDebugEnabled()) {
            logger.debug("Updating bounds on "+getName() //$NON-NLS-1$
                    +" to ["+r.x+","+r.y+","+r.width+","+r.height+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }

        bounds.setBounds(r.x,r.y,r.width,r.height);            
        firePropertyChange("bounds", oldBounds, new Rectangle(bounds));

        repaint();
    }

    /**
     * Returns a copy of this component's bounding rectangle.
     */
    @Accessor
    public Rectangle getBounds() {
        return getBounds(null);
    }

    /**
     * Returns a copy of this component's minimum size. Modifications to the
     * returned object have no effect on this component.
     */
    @Accessor
    public Dimension getMinimumSize() {
        return new Dimension(minimumSize);
    }

    /**
     * Sets this component's minimum size. Minimum size may not take effect
     * until the next time this component is validated (which also often happens
     * to play pen components when they are being painted).
     * 
     * @param minimumSize
     *            The new minimum size this component should take on. Only a
     *            copy of the given Dimension object is stored.
     */
    @Mutator
    public void setMinimumSize(Dimension minimumSize) {
        Dimension oldMinimumSize = this.minimumSize;
        this.minimumSize = new Dimension(minimumSize);
        firePropertyChange("minimumSize", oldMinimumSize, new Dimension(minimumSize));
    }
    
    /**
     * Sets the given rectangle to be identical to this component's bounding box.
     * 
     * @param r An existing rectangle.  If null, this method creates a new rectangle for you.
     * @return r if r was not null; a new rectangle otherwise.
     */
    @NonBound
    public Rectangle getBounds(Rectangle r) {
        if (r == null) r = new Rectangle();
        r.setBounds(bounds);
        return r;
    }

    @Transient @Accessor(isInteresting=true)
    public Dimension getSize() {
        return new Dimension(bounds.width, bounds.height);
    }
    
    /**
     * The revalidate() call uses this to determine the component's
     * correct location.  This implementation just returns the current
     * location.  Override it if you need to be moved during validation.
     */
    @Transient @Accessor
    public Point getPreferredLocation() {
        return getLocation();
    }

    @Transient @Accessor(isInteresting=true)
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
    @Transient @Accessor
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
    @Transient @Mutator
    public void setLocation(Point point) {
        setBounds(point.x,point.y, getWidth(), getHeight());
    }

    /**
     * Updates the on-screen location of this component.  If you try to move this
     * component to a negative co-ordinate, it will automatically be normalized (along
     * with everything else in the playpen) to non-negative coordinates.
     */
    @Transient @Mutator
    public void setLocation(int x, int y) {
		setBounds(x, y, getWidth(), getHeight());
    }
    
    @NonBound
    public static boolean isLocationChange(PropertyChangeEvent evt) {
        Rectangle oldVal = (Rectangle) evt.getOldValue();
        Rectangle newVal = (Rectangle) evt.getNewValue();
        return (oldVal.x != newVal.x || oldVal.y != newVal.y);
    }
    
    @Transient @Mutator
    public void setSize(Dimension size) {
        setBounds(getX(), getY(), size.width, size.height);
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

    /**
     * Returns the user-visible name for this component--often the same as
     * getModel().getName(), but this depends entirely on the subclass's idea
     * of what in the model constitutes its name.
     */
    @Transient @Accessor
    public abstract String getModelName();
    
    @Transient @Accessor
    public int getX() {
        return bounds.x;
    }
    
    @Transient @Accessor
    public int getY() {
        return bounds.y;
    }
    
    @Transient @Accessor
    public int getWidth() {
        return bounds.width;
    }
    
    @Transient @Accessor
    public int getHeight() {
        return bounds.height;
    }
    
    @Transient @Accessor
    public Insets getInsets() {
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Transient @Mutator
    public void setInsets(Insets insets) {
        Insets oldValue = this.insets;
        this.insets = new Insets(insets.top, insets.left, insets.bottom, insets.right);
        firePropertyChange("insets", oldValue, insets);
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
        
        int x1 = (int) Math.floor(x * zoom);
        int y1 = (int) Math.floor(y * zoom);
        int x2 = (int) Math.ceil((x + width) * zoom);
        int y2 = (int) Math.ceil((y + height) * zoom);
        
        if (owner.isRenderingAntialiased()) {
            x1--;
            y1--;
            x2++;
            y2++;
        }
        owner.repaint(x1, y1, (x2 - x1), (y2 - y1));
    }
    
    @Accessor(isInteresting=true)
    public boolean isOpaque() {
        return opaque;
    }

    @Mutator
    public void setOpaque(boolean opaque) {
        if (this.opaque != opaque) {
            this.opaque = opaque;
            firePropertyChange("opaque", !opaque, opaque);
        }
    }

    @Accessor(isInteresting=true)
    public Color getBackgroundColor() {
        if (backgroundColor == null) {
            return getPlayPen().getBackground();
        }
        return backgroundColor;
    }
    
    @Mutator
    public void setBackgroundColor(Color c) {
        Color oldColor = backgroundColor;
        backgroundColor = c;
        firePropertyChange("backgroundColor", oldColor, backgroundColor);
    }

    @Accessor(isInteresting=true)
    public Color getForegroundColor() {
        if (foregroundColor == null && getPlayPen() != null) {
            return getPlayPen().getForeground();
        }
        return foregroundColor;
    }
    
    @Mutator
    public void setForegroundColor(Color c) {
        Color oldColor = getForegroundColor();
        foregroundColor = c;
        firePropertyChange("foregroundColor", oldColor, foregroundColor);
    }
    
    @Transient @Accessor
    public String getToolTipText() {
        return toolTipText;
    }

    @Transient @Mutator
    public void setToolTipText(String toolTipText) {
        String oldValue = this.toolTipText;
        if (!ArchitectUtils.areEqual(toolTipText, this.toolTipText)) {
            this.toolTipText = toolTipText;
            firePropertyChange("tooTipText", oldValue, toolTipText);
            logger.debug("ToolTipText changed to "+toolTipText); //$NON-NLS-1$
        }
    }

    @Transient @Accessor
    public Font getFont() {
        return getPlayPen().getFont();
    }

    @Transient @Accessor
    public FontMetrics getFontMetrics(Font f) {
        return getPlayPen().getFontMetrics(f);
    }
    
    @Transient @Accessor
    public FontRenderContext getFontRenderContext() {
        return getPlayPen().getFontRenderContext();
    }
    
    public boolean contains(Point p) {
        boolean containsPoint = getUI().contains(p);
        logger.debug("" + this + " contains " + p + "? " + containsPoint);
        return containsPoint;
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

    @Transient @Accessor
    public Dimension getPreferredSize() {
        return getUI().getPreferredSize();
    }

    @Accessor(isInteresting=true)
    public abstract Object getModel();
    
    /**
     * Performs the component specific actions for the given MouseEvent. 
     */
    public abstract void handleMouseEvent(MouseEvent evt);

    // --------------------- SELECTABLE SUPPORT ---------------------

    private final List<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();

    public final void addSelectionListener(SelectionListener l) {
        logger.info("" + this + " is adding " + l);
        selectionListeners.add(l);
    }

    public final void removeSelectionListener(SelectionListener l) {
        selectionListeners.remove(l);
    }

    protected final void fireSelectionEvent(SelectionEvent e) {
        if (logger.isDebugEnabled()) {
            logger.debug("Notifying "+selectionListeners.size() //$NON-NLS-1$
                         +" listeners of selection change"); //$NON-NLS-1$
        }
        Iterator<SelectionListener> it = selectionListeners.iterator();
        if (e.getType() == SelectionEvent.SELECTION_EVENT) {
            while (it.hasNext()) {
                it.next().itemSelected(e);
            }
        } else if (e.getType() == SelectionEvent.DESELECTION_EVENT) {
            while (it.hasNext()) {
                it.next().itemDeselected(e);
            }
        } else {
            throw new IllegalStateException("Unknown selection event type "+e.getType()); //$NON-NLS-1$
        }
    }

    /**
     * See {@link #selected}.
     */
    @Transient @Accessor
    public boolean isSelected() {
        return selected;
    }

    /**
     * Tells this component it is selected or deselected. If isSelected is different
     * from the current selection state for this component, a SelectionEvent will be
     * fired to all selection listeners.
     * <p>
     * See {@link #selected}.
     * 
     * @param isSelected The new selection state for this component
     * @param multiSelectType One of the type codes from {@link SelectionEvent}.
     */
    @Transient @Mutator
    public void setSelected(boolean isSelected, int multiSelectType) {
        if (selected != isSelected) {
            selected = isSelected;
            fireSelectionEvent(new SelectionEvent(this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT, multiSelectType));
            repaint();
        }
    }
    
    @NonBound
    public boolean isBeingDragged() {
        return isBeingDragged;
    }
    
    /**
     * Initiate a drag, which begins a transaction for this object
     * and sets up an update listener to listen for conflicts while dragging
     */
    public void startedDragging() {
        if (!isBeingDragged) {
            isBeingDragged = true;
            if (getPlayPen().getSession().isEnterpriseSession()) {
                getPlayPen().getSession().getEnterpriseSession().getUpdater().addListener(updateWhileMovingListener);
            }
            begin("Dragging " + this);
        } else {
            throw new IllegalStateException("Component is already in the middle of a drag");
        }
    }
    
    public void doneDragging() {
        doneDragging(true);
    }
    
    /**
     * Completes the drag on this component by ending the transaction.
     * @param ok If false, this component will rollback instead of commit.
     * Used by the update conflict listener to rollback the drag.
     */
    public void doneDragging(boolean ok) {
        if (isBeingDragged) {
            isBeingDragged = false;
            if (ok) {
                commit();
            } else {
                rollback("Update received while dragging");
                JOptionPane.showMessageDialog(getPlayPen(), "There was an update while you were dragging");
            }
        } else {
            throw new IllegalStateException("Component is not in the middle of a drag");
        }
    }
    
    public boolean allowsChildren() {
        return (allowedChildTypes.size() > 0);
    }    
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }
    
    public int childPositionOffset(Class<? extends SPObject> childType) {
        throw new IllegalArgumentException("This class does not allow children");
    }
    
    @Override
    @Accessor
    public PlayPenContentPane getParent() {
        return (PlayPenContentPane) super.getParent();
    }
    
    @Mutator 
    public void setParent(SPObject parent) {
        if (parent instanceof PlayPenContentPane || parent == null) {
            super.setParent(parent);
        } else {
            throw new IllegalArgumentException("Parent of PlayPenComponent must be " +
            		"PlayPenContentPane, not " + parent.getClass().getSimpleName());
        }
    }
    
    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean removeChildImpl(SPObject child) {
        return false;
    }
    
    public List<? extends SPObject> getDependencies() {
        return Collections.singletonList((SPObject) getModel());
    }

    public void removeDependency(SPObject dependency) {
        if (dependency == getModel()) {
            try {       
                getParent().removeChild(this);
            } catch (ObjectDependentException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * You must call this method when you are adding a TablePane component after
     * the parent is defined. It will register the necessary listeners to all
     * necessary parties.
     */
    public void connect() {
        //by default do nothing.
    }

}
