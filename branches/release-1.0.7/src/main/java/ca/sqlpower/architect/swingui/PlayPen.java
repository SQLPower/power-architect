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
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.olap.MondrianModel;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsages;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.action.CancelAction;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleEvent;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.DimensionPane.HierarchySection;
import ca.sqlpower.architect.swingui.olap.OLAPPane;
import ca.sqlpower.architect.swingui.olap.OLAPTree;
import ca.sqlpower.architect.swingui.olap.PaneSection;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.undo.CompoundEventListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTable.TransferStyles;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider;
import ca.sqlpower.swingui.CursorManager;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.util.TransactionEvent.TransactionState;

import com.google.common.collect.ArrayListMultimap;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


/**
 * The PlayPen is the main GUI component of the SQL*Power Architect.
 */
@SuppressWarnings(
        justification = "PlayPen is not meant to be serializable",
        value = {"SE_BAD_FIELD"})
public class PlayPen extends JPanel
	implements SPListener, SelectionListener, Scrollable {

    public interface CancelableListener {

		public void cancel();

	}
 // actionCommand identifier for actions shared by Playpen
    public static final String ACTION_COMMAND_SRC_PLAYPEN = "PlayPen";
    
	private static Logger logger = Logger.getLogger(PlayPen.class);

	public enum MouseModeType {IDLE,
						CREATING_TABLE,
						CREATING_RELATIONSHIP,
						SELECT_TABLE,
						SELECT_RELATIONSHIP,
						SELECT_ITEM,
						SELECT_SECTION,
						MULTI_SELECT,
						RUBBERBAND_MOVE}
	private MouseModeType mouseMode = MouseModeType.IDLE;

	/**
	 * The cursor manager for this play pen.
	 */
	private final CursorManager cursorManager;

	protected void addImpl(Component c, Object constraints, int index) {
	    throw new UnsupportedOperationException("You can't add swing component for argument"); //$NON-NLS-1$
	}

	// --- Scrollable Methods --- //
	public Dimension getPreferredScrollableViewportSize() {
	    // return getPreferredSize();
	    return new Dimension(800,600);
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	    if (orientation == SwingConstants.HORIZONTAL) {
	        return visibleRect.width;
	    } else { // SwingConstants.VERTICAL
	        return visibleRect.height;
	    }
	}

	public boolean getScrollableTracksViewportHeight() {
	    return false;
	}

	public boolean getScrollableTracksViewportWidth() {
	    return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	    if (orientation == SwingConstants.HORIZONTAL) {
	        return visibleRect.width/5;
	    } else { // SwingConstants.VERTICAL
	        return visibleRect.height/5;
	    }
	}

	// -------------------------- JComponent overrides ---------------------------

	/**
	 * Calculates the smallest rectangle that will completely
	 * enclose the visible components.
	 *
	 * This is then compared to the viewport size, one dimension
	 * at a time.  To ensure the whole playpen is "live", always
	 * choose the larger number in each Dimension.
	 *
	 * There is also a lower bound on how small the playpen can get.  The
	 * layout manager returns a preferred size of (100,100) when asked.
	 */
	public Dimension getPreferredSize() {

	    Dimension usedSpace = getUsedArea();
	    Dimension vpSize = getViewportSize();
	    Dimension ppSize = null;

	    // viewport seems to never come back as null, but protect anyways...
	    if (vpSize != null) {
	        ppSize = new Dimension(Math.max(usedSpace.width, vpSize.width),
	                Math.max(usedSpace.height, vpSize.height));
	    }

	    if (logger.isDebugEnabled()) {
	        logger.debug("minsize is: " + this.getMinimumSize()); //$NON-NLS-1$
	        logger.debug("unzoomed userDim is: " + unzoomPoint(new Point(usedSpace.width,usedSpace.height))); //$NON-NLS-1$
	        logger.debug("zoom="+zoom+",usedSpace size is " + usedSpace); //$NON-NLS-1$ //$NON-NLS-2$
	    }

	    if (ppSize != null) {
	        logger.debug("preferred size is ppSize (viewport size was null): " + ppSize); //$NON-NLS-1$
	        return ppSize;
	    } else {
	        logger.debug("preferred size is usedSpace: " + usedSpace); //$NON-NLS-1$
	        return usedSpace;
	    }
	}

	public Dimension getUsedArea() {
	    Rectangle cbounds = null;
	    int minx = 0, miny = 0, maxx = 0, maxy = 0;
	    for (PlayPenComponent c : contentPane.getChildren()) {
	        cbounds = c.getBounds(cbounds);
	        minx = Math.min(cbounds.x, minx);
	        miny = Math.min(cbounds.y, miny);
	        maxx = Math.max(cbounds.x + cbounds.width , maxx);
	        maxy = Math.max(cbounds.y + cbounds.height, maxy);
	    }

	    return new Dimension((int) ((double) Math.max(maxx - minx, this.getMinimumSize().width) * zoom),
	            (int) ((double) Math.max(maxy - miny, this.getMinimumSize().height) * zoom));
	}

	// get the size of the viewport that we are sitting in (return null if there isn't one);
	public Dimension getViewportSize() {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        logger.debug("viewport size is: " + jvp.getSize()); //$NON-NLS-1$
	        return jvp.getSize();
	    } else {
	        logger.debug("viewport size is NULL"); //$NON-NLS-1$
	        return null;
	    }
	}

	// set the size of the viewport that we are sitting in (return null if there isn't one);
	public void setViewportSize(int width, int height) {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        logger.debug("viewport size set to: " + width + "," + height); //$NON-NLS-1$ //$NON-NLS-2$
	        jvp.setSize(width,height);
	    }
	}

	/**
	 * If some playPen components get dragged into a negative range all tables are then shifted
	 * so that the lowest x and y values are 0.  The tables will retain their relative location.
	 *
	 * If this function is moved into a layout manager it causes problems with undo because we do
	 * no know when this gets called.
	 */
	protected void normalize() {
	    if (normalizing) return;
	    normalizing=true;
	    int minX = 0;
	    int minY = 0;

	    for (PlayPenComponent ppc : contentPane.getChildren()) {
	        minX = Math.min(minX, ppc.getX());
	        minY = Math.min(minY, ppc.getY());
	    }       

	    //Readjusts the playPen's components, since minX and min <= 0,
	    //the adjustments of subtracting minX and/or minY makes sense.
	    if ( minX < 0 || minY < 0 ) {           
	        for (PlayPenComponent ppc : contentPane.getChildren()) {
	            ppc.setLocation(ppc.getX()-minX, ppc.getY()-minY);
	        }

	        // This function may have expanded the playpen's minimum
	        // and preferred sizes, so the original repaint region could be
	        // too small!
	        this.repaint();
	    }
	    normalizing = false;
	}

	//   get the position of the viewport that we are sitting in
	public Point getViewPosition() {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        Point viewPosition = jvp.getViewPosition();
	        logger.debug("view position is: " + viewPosition); //$NON-NLS-1$
	        return viewPosition;
	    } else {
	        return viewportPosition;
	    }
	}

	// set the position of the viewport that we are sitting in
	public void setViewPosition(Point p) {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        logger.debug("view position set to: " + p); //$NON-NLS-1$
	        if (p != null) {
	            jvp.setViewPosition(p);
	        }

	    }
	    viewportPosition = p;
	}

	public void setInitialViewPosition() {
	    setViewPosition(viewportPosition);
	}

	/** See {@link #paintingEnabled}. */
	public void setPaintingEnabled(boolean paintingEnabled) {
	    PlayPen.this.paintingEnabled = paintingEnabled;
	}

	/** See {@link #paintingEnabled}. */
	public boolean isPaintingEnabled() {
	    return paintingEnabled;
	}

	public void paintComponent(Graphics g) {
	    if (!paintingEnabled) return;

	    logger.debug("start of paintComponent, width=" + this.getWidth() +
	            ",height=" + this.getHeight()); //$NON-NLS-1$ //$NON-NLS-2$
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setColor(this.getBackground());
	    g2.fillRect(0, 0, this.getWidth(), this.getHeight());
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasSetting);

	    if (isDebugEnabled()) {
	        Rectangle clip = g2.getClipBounds();
	        if (clip != null) {
	            g2.setColor(Color.green);
	            clip.width--;
	            clip.height--;
	            g2.draw(clip);
	            g2.setColor(this.getBackground());
	            logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
	        } else {
	            logger.debug("Null clipping region"); //$NON-NLS-1$
	        }
	    }

	    Rectangle bounds = new Rectangle();
	    AffineTransform backup = g2.getTransform();
	    g2.scale(zoom, zoom);
	    AffineTransform zoomedOrigin = g2.getTransform();

	    List<PlayPenComponent> relationshipsLast = new ArrayList<PlayPenComponent>();
	    List<Relationship> relations = contentPane.getChildren(Relationship.class);
	    List<UsageComponent> usages = contentPane.getChildren(UsageComponent.class);
	    relationshipsLast.addAll(contentPane.getAllChildren());
	    relationshipsLast.removeAll(relations);
	    relationshipsLast.addAll(relations);
	    relationshipsLast.removeAll(usages);	  
	    relationshipsLast.addAll(usages);
	    
	    // counting down so visual z-order matches click detection z-order
	    for (int i = relationshipsLast.size() - 1; i >= 0; i--) {
	        PlayPenComponent c = relationshipsLast.get(i);
	        c.getBounds(bounds);
	        //expanding width and height by 1 as lines have 0 width or height when vertical/horizontal
	        if ( g2.hitClip(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1)) {
	            if (logger.isDebugEnabled()) logger.debug("Painting visible component "+c); //$NON-NLS-1$
	            g2.translate(c.getLocation().x, c.getLocation().y);
	            Font g2Font = g2.getFont();
	            c.paint(g2);
	            g2.setFont(g2Font);
	            g2.setTransform(zoomedOrigin);
	        } else {
	            if (logger.isDebugEnabled()) logger.debug("paint: SKIPPING "+c); //$NON-NLS-1$
	            logger.debug(" skipped bounds are: x=" + bounds.x + " y=" + bounds.y + " width=" + bounds.width + " height=" + bounds.height);
	            logger.debug(" clipping rectangle: x=" + g2.getClipBounds().x + " y=" + g2.getClipBounds().y + " width=" + g2.getClipBounds().width + " height=" + g2.getClipBounds().height);
	        }
	    }

	    if (rubberBand != null && !rubberBand.isEmpty()) {
	        if (logger.isDebugEnabled()) logger.debug("painting rubber band "+rubberBand); //$NON-NLS-1$
	        g2.setColor(rubberBandColor);
	        Composite backupComp = g2.getComposite();
	        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
	        g2.fillRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
	        g2.setComposite(backupComp);
	        g2.drawRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
	    }

	    g2.setTransform(backup);

	    logger.debug("end of paintComponent, width=" + this.getWidth() +
	            ",height=" + this.getHeight()); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Delegates to the content pane.
	 *
	 * <p>Important Note: If you want tooltips to be active on this PlayPen instance,
	 * you have to call <tt>ToolTipManager.sharedInstance().registerComponent(pp)</tt> on this
	 * instance (where <tt>pp</tt> is whatever your reference to this playpen is called).
	 */
	public String getToolTipText(MouseEvent e) {
	    Point zp = unzoomPoint(e.getPoint());
	    MouseEvent zoomedEvent =
	        new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
	                zp.x, zp.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
	    return contentPane.getToolTipText(zoomedEvent);
	}       
    
	/**
	 * The factory responsible for setting up popup menu contents for this playpen.
	 */
	private PopupMenuFactory popupFactory;

	/**
	 * Maps table names (Strings) to Integers.  Useful for making up
	 * new table names if two tables of the same name are added todrag
	 * this playpen.
	 */
	protected Set<String> tableNames;

	/**
	 * This object receives all mouse and mouse motion events in the
	 * PlayPen.  It tries to dispatch them to the ppcomponents, and
	 * also handles playpen-specific behaviour like rubber band
	 * selection and popup menu triggering.
	 */
	protected PPMouseListener ppMouseListener;

	/**
	 * The RubberBand allows the user to select multiple ppcomponents
	 * by click-and-drag across a region.
	 */
	protected Rectangle rubberBand;

	/**
	 * This is the colour that the rubber band will be painted with.
	 */
	protected Color rubberBandColor = Color.black;

	/**
	 * The visual magnification factor for this playpen.
	 */
	protected double zoom;

	/**
	 * Contains the child components of this playpen.
	 */
	protected PlayPenContentPane contentPane;

	/**
	 * This action brings the selected TablePane or Relationship to
	 * the front/top of the component stack.
	 */
	protected Action bringToFrontAction;

	/**
	 * This action sends the selected TablePane or Relationship to
	 * the back/bottom of the component stack.
	 */
	protected Action sendToBackAction;
	
	/**
	 * The zoom in action used by the mouse listener.
	 */
	protected Action zoomInAction;
	
	/**
     * The zoom out action used by the mouse listener.
     */
	protected Action zoomOutAction;
	
	/**
     * The component that is used my the mouse listener to be scrolled.
     * Will always be a JScrollPane, but since the ArchitectFrame returns
     * it as a Component this field is also a Component.
     */
	protected Component ppScrollPane;
	
	/**
	 * This dialog box is for editting the PlayPen's DB Connection spec.
	 */
	protected JDialog dbcsDialog;

	/**
     * used by mouseReleased to figure out if a DND operation just took place in the
     * playpen, so it can make a good choice about leaving a group of things selected
     * or deselecting everything except the TablePane that was clicked on.
     */
	protected boolean draggingContainerPanes = false;

	private boolean selectionInProgress = false;
	
	/**
	 * A RenderingHints value of VALUE_ANTIALIAS_ON, VALUE_ANTIALIAS_OFF, or VALUE_ANTIALIAS_DEFAULT.
	 */
    private Object antialiasSetting = RenderingHints.VALUE_ANTIALIAS_DEFAULT;

	/**
	 * A graveyard for components that used to be associated with model
	 * components that are no longer in the model.  If the model components
	 * come back from the dead (thanks the the UndoManager), then the
	 * corresponding PlayPenComonent can be revived from this map. The play pen
	 * components are mapped to UUIDs of the objects as some SQLObject classes
	 * implement a different version of equals.
	 *
	 * Allows the garbage collecter to clean up any components not in the undo manager
	 *
	 */
    private Map<String,PlayPenComponent> removedComponents = new WeakHashMap<String, PlayPenComponent>();

    /**
     * Tells whether or not this component will paint its contents.  This was
     * originally added to test the speed of the SpringLayout when it doesn't
     * have to repaint everything for every frame.  It might be useful for
     * other stuff later on too.
     */
    private boolean paintingEnabled = true;

	private boolean normalizing;

    /**
     * The session that contains this playpen
     */
	final ArchitectSwingSession session;
	
	/**
	 * The initial position of the viewport.
	 */
	private Point viewportPosition;
	
	/**
	 * The font render context for cases where the play pen
	 * has no graphics object to get the font render context
	 * but we know it from another panel.
	 */
	private FontRenderContext fontRenderContext;
	
	/**
	 * Flag to prevent recursive selections for selectObjects()
	 */
	private boolean ignoreTreeSelection = false;
	
	public PlayPen(ArchitectSwingSession session) {
	    this(session, session.getTargetDatabase());
	}
	
	public PlayPen(ArchitectSwingSession session, SPObject modelContainer) {
	    this(session, new PlayPenContentPane(modelContainer));
	}
	
	/**
     * Creates a play pen with reasonable defaults.  If you are creating
     * this PlayPen for temporary use (as opposed to creating a session's
     * main PlayPen), don't forget to call {@link #destroy()} when you are
     * done with it.
     * 
     * @param session
     *            The session this play pen belongs to. Null is not allowed.
     * @param modelContainer This is the top-level object of 
     * the model that this PlayPen will represent (ie: SQLDatabase, OLAPSession)     
     */
	public PlayPen(ArchitectSwingSession session, PlayPenContentPane ppcp) {
        if (session == null) throw new NullPointerException("A null session is not allowed here."); //$NON-NLS-1$
		this.session = session;		
		setDatabase(session.getTargetDatabase());
		if (session.isEnterpriseSession()) {
		    zoom = session.getEnterpriseSession().getPrefDouble("zoom", 1.0);
		} else {
	        Preferences p = Preferences.userNodeForPackage(ArchitectSessionImpl.class);
	        Preferences prefs = p.node(session.getWorkspace().getName());
            zoom = prefs.getDouble("zoom", 1.0);
		}
        viewportPosition = new Point(0, 0);
		this.setBackground(java.awt.Color.white);
		contentPane = ppcp;
		contentPane.setPlayPen(this);
		this.setName("Play Pen"); //$NON-NLS-1$
		this.setMinimumSize(new Dimension(1,1));
		if (!GraphicsEnvironment.isHeadless()) {
		    //XXX See http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=3036
		    new DropTarget(this, new PlayPenDropListener());
		    new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new TablePaneDragGestureListener());
		    logger.debug("DragGestureRecognizer motion threshold: " + 
		            this.getToolkit().getDesktopProperty("DnD.gestureMotionThreshold")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		bringToFrontAction = new BringToFrontAction(this);
		sendToBackAction = new SendToBackAction(this);
		ppMouseListener = new PPMouseListener();
		this.addMouseListener(ppMouseListener);
		this.addMouseMotionListener(ppMouseListener);
		
		cursorManager = new CursorManager(this);
		fontRenderContext = null;
	}

	/**
	 * Creates a new PlayPen with similar contents to the given PlayPen.  The new copy will have fresh
	 * copies of all the contained PlayPenComponents, but will share the same model as the original
	 * play pen.  This was originally intended for use by the print preview panel, but it may end
	 * up useful for other things too.
     * <p>
     * Remember to call {@link #destroy()} when you are done with this playpen!
	 *
     * @param session The session that this new copy should live in.  If you specify a session other
     * than the session that the given playpen lives in, it should still produce a usable copy, however
     * be aware that the underlying SQLObjects will be shared between the two sessions.
	 * @param pp The playpen to duplicate.
	 */
	public PlayPen(ArchitectSwingSession session, PlayPen pp) {
		this(session);
		logger.debug("Copying PlayPen@" + System.identityHashCode(pp) + " into " + System.identityHashCode(this));
		this.antialiasSetting = pp.antialiasSetting;
				
		this.setFont(pp.getFont());
		this.setForeground(pp.getForeground());
		this.setBackground(pp.getBackground());
		
		// XXX this should be done by making PlayPenComponent cloneable.
		// it's silly that playpen has to know about every subclass of ppc
		logger.debug("Copying " + pp.getContentPane().getChildren().size() + " components...");
		for (int i = 0; i < pp.getContentPane().getChildren().size(); i++) {
		    PlayPenComponent ppc = pp.getContentPane().getChildren().get(i);
		    PlayPenContentPane contentPane = (PlayPenContentPane) this.contentPane;
		    if (ppc instanceof TablePane) {
		        TablePane tp = (TablePane) ppc;
		        addImpl(new TablePane(tp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof Relationship) {
		        Relationship rel = (Relationship) ppc;
		        addImpl(new Relationship(rel, contentPane), ppc.getPreferredLocation());			    
		    } else if (ppc instanceof CubePane) {
		        CubePane cp = (CubePane) ppc;
		        addImpl(new CubePane(cp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof DimensionPane) {
		        DimensionPane dp = (DimensionPane) ppc;
		        addImpl(new DimensionPane(dp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof VirtualCubePane) {
		        VirtualCubePane vcp = (VirtualCubePane) ppc;
		        addImpl(new VirtualCubePane(vcp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof UsageComponent) {
		        UsageComponent uc = (UsageComponent) ppc;
		        contentPane.addChild(new UsageComponent(uc, contentPane), i);
		    } else {
		        throw new UnsupportedOperationException(
		                "I don't know how to copy PlayPenComponent type " + ppc.getClass().getName());
		    }
		}		
		this.setSize(this.getPreferredSize());
	}
    
    /**
     * Adds the given component to this PlayPen's content pane.  Does
     * NOT add it to the Swing containment hierarchy. The playpen is a
     * leaf in the hierarchy as far as swing is concerned.
     *
     * @param c The component to add.  The PlayPen only accepts
     * Relationship and ContainerPane components.
     * @param constraints The Point at which to add the component
     * @param index ignored for now, but would normally specify the
     * index of insertion for c in the child list.
     */
    protected void addImpl(PlayPenComponent c, Object constraints) {        
        if (c instanceof Relationship || c instanceof UsageComponent || c instanceof PlayPenLabel) {
            contentPane.addChild(c, contentPane.getFirstDependentComponentIndex());
        } else if (c instanceof ContainerPane<?, ?>) {
            if (constraints instanceof Point) {
                c.setLocation((Point) constraints);
                contentPane.addChild(c, 0);
            } else {
                throw new IllegalArgumentException("Constraints must be a Point"); //$NON-NLS-1$
            }
            
            if (c instanceof TablePane) {
                // Makes drag and dropped tables show the proper columns
                ((TablePane) c).updateHiddenColumns();
                ((TablePane) c).updateNameDisplay();
            }
        } else {
            throw new IllegalArgumentException("PlayPen can't contain components of type " //$NON-NLS-1$
                                               +c.getClass().getName());
        }
        Dimension size = c.getPreferredSize();
        c.setSize(size);
        logger.debug("Set size to "+size); //$NON-NLS-1$
        logger.debug("Final state looks like "+c); //$NON-NLS-1$
    }

    /**
     * Disconnects this play pen from everything it's listening to.
     * It is important to do this whenever you make a temporary PlayPen
     * instance for some specific purpose (for example, print preview
     * and the column mapping editor panel create temporary play pens).
     * The primary play pen of the session itself doesn't really need
     * to be destroyed, because all of the listener interconnections are
     * contained within the session, and the whole tangled mess can just
     * go away together.
     * <p>
     * As the method name implies, once you have called this method,
     * this PlayPen instance will not function properly, so you should
     * stop using it.
     */
    public void destroy() {
        logger.debug("Destroying playpen " + System.identityHashCode(this));
        // FIXME the content pane must be notified of this destruction, either explicitly or via a lifecycle event
        firePlayPenLifecycleEvent();
        removeHierarchyListeners(session.getTargetDatabase());
    }

    /**
     * Returns a new list of all tables in this play pen. The list returned will
     * be your own private (shallow) copy, so you are free to modify it.
     */
    public List<SQLTable> getTables() throws SQLObjectException {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        SQLObjectUtils.findDescendentsByClass(session.getTargetDatabase(), SQLTable.class, tables);
        return tables;

    }

	private final void setDatabase(SQLDatabase newdb) {
		if (newdb == null) throw new NullPointerException("db must be non-null"); //$NON-NLS-1$
		
		// Note, this also happens in CoreProject, but that's only helpful when loading a project file
		// And you get fireworks if you call setDataSource() on a non-playpen connection
		newdb.setPlayPenDatabase(true);

		JDBCDataSource dbcs = new JDBCDataSource(session.getDataSources());
        newdb.setDataSource(dbcs);

        SQLPowerUtils.listenToHierarchy(newdb, this);
		tableNames = new HashSet<String>();
	}

    public void setDatabaseConnection(JDBCDataSource dbcs){
        JDBCDataSource tSpec = session.getTargetDatabase().getDataSource();
        tSpec.setDisplayName(dbcs.getDisplayName());
        tSpec.getParentType().setJdbcDriver(dbcs.getDriverClass());
        tSpec.setUrl(dbcs.getUrl());
        tSpec.setUser(dbcs.getUser());
        tSpec.setPass(dbcs.getPass());
        tSpec.setPlSchema(dbcs.getPlSchema());
        tSpec.setPlDbType(dbcs.getPlDbType());
        tSpec.setOdbcDsn(dbcs.getOdbcDsn());
    }

    /**
     * Sets up the generic keyboard actions for this playpen. This should only
     * be called once, which is normally done at the time the playpen is
     * created. If no keyboard actions (zoom, delete selected, cursor up/down
     * for item selection) are desired, just don't call this when creating your
     * playpen.
     */
    public void setupKeyboardActions() {
        InputMap inputMap = this.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL"); //$NON-NLS-1$
        this.getActionMap().put("CANCEL", new CancelAction(this)); //$NON-NLS-1$

        final Object KEY_SELECT_UPWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_UPWARD"; //$NON-NLS-1$
        final Object KEY_SELECT_DOWNWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_DOWNWARD"; //$NON-NLS-1$

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KEY_SELECT_UPWARD);
        this.getActionMap().put(KEY_SELECT_UPWARD, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List<PlayPenComponent> items = getSelectedItems();
                if (items.size() == 1) {
                    PlayPenComponent item = items.get(0);
                    if (item instanceof TablePane) {
                        TablePane tp = (TablePane) item;
                        int oldIndex = tp.getSelectedItemIndex();
                        
                        try {
                            if (oldIndex < 0) {
                                oldIndex = tp.getModel().getColumns().size();
                            }
                            int newIndex = oldIndex;
                            while (newIndex - 1 >= 0) {
                                newIndex--;
                                if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                    break;
                                }
                            }
                            if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                tp.selectNone();
                                tp.selectItem(newIndex);
                            }
                        } catch (SQLObjectException ex) {
                            throw new SQLObjectRuntimeException(ex);
                        }
                    }
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KEY_SELECT_DOWNWARD);
        this.getActionMap().put(KEY_SELECT_DOWNWARD, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List<PlayPenComponent> items = getSelectedItems();
                if (items.size() == 1) {
                    PlayPenComponent item = items.get(0);
                    if (item instanceof TablePane) {
                        TablePane tp = (TablePane) item;
                        int oldIndex = tp.getSelectedItemIndex();
                        
                        // If the selected "column" is one of the special values
                        // (title, none, before pk, etc) then pressing down arrow
                        // should select the first column
                        if (oldIndex < 0) {
                            oldIndex = -1;
                        }
                        
                        try {
                            if (oldIndex < tp.getModel().getColumns().size() - 1) {
                                try {
                                    int newIndex = oldIndex;
                                    while (newIndex + 1 < tp.getModel().getColumns().size()) {
                                        newIndex++;
                                        if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                            break;
                                        }
                                    }
                                    if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                        tp.selectNone();
                                        tp.selectItem(newIndex);
                                    }
                                } catch (SQLObjectException ex) {
                                    throw new SQLObjectRuntimeException(ex);
                                }
                            }
                        } catch (SQLObjectException e1) {
                            logger.error("Could not get columns of "+ tp.getName(), e1); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        
        this.addKeyListener(new KeyListener() {

            private void changeCursor(KeyEvent e) {
                if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
                    cursorManager.dragAllModeStarted();
                } else {
                    cursorManager.dragAllModeFinished();
                }
            }

            public void keyPressed(KeyEvent e) { changeCursor(e); }
            public void keyReleased(KeyEvent e) { changeCursor(e); }
            public void keyTyped(KeyEvent e) { changeCursor(e); }
            
        });
    }

    /**
	 * Tells whether or not this PlayPen instance is in debugging mode.
	 * Currently, this is controlled by log4j settings, but that may change
	 * in the future.
	 */
	public boolean isDebugEnabled() {
	    return logger.isDebugEnabled();
	}
	
	// --------------------- Utility methods -----------------------

	/**
	 * Calls setChildPositionImpl(child, p.x, p.y).
	 */
	public void setChildPosition(PlayPenComponent child, Point p) {
		setChildPositionImpl(child, p.x, p.y);
	}

	/**
	 * Calls setChildPositionImpl(child, x, y).
	 */
	public void setChildPosition(PlayPenComponent child, int x, int y) {
		setChildPositionImpl(child, x, y);
	}

	/**
	 * Scales the given X and Y co-ords from the visible point (x,y)
	 * to the actual internal location, and sets child's position
	 * accordingly.
	 *
	 * @param child a component in this PlayPen's content pane.
	 * @param x the apparent visible X co-ordinate
	 * @param y the apparent visible Y co-ordinate
	 */
	protected void setChildPositionImpl(PlayPenComponent child, int x, int y) {
		child.setLocation((int) ((double) x / zoom), (int) ((double) y / zoom));
	}

	/**
     * Returns the zoom in action that the mouse uses for this PlayPen. If none
     * has been set, it returns the default zoom in action from the
     * ArchitectFrame.
     */
	public Action getMouseZoomInAction(){
        if (zoomInAction == null) {
            return session.getArchitectFrame().getZoomInAction();
        }
        return zoomInAction;
    }
    
	/**
     * Sets the zoom in action for which the mouse uses for this PlayPen.
     * 
     * @param zoomInAction
     *            The zoom in action for the mouse to use in this PlayPen.
     */
	public void setMouseZoomInAction(Action zoomInAction){
        this.zoomInAction = zoomInAction;
    }
    
    /**
     * Returns the zoom out action that the mouse uses for this PlayPen. If none
     * has been set, it returns the default zoom in action from the
     * ArchitectFrame.
     */
	public Action getMouseZoomOutAction(){
        if (zoomOutAction == null) {
            return session.getArchitectFrame().getZoomOutAction();
        }
        return zoomOutAction;
    }
    
    /**
     * Sets the zoom out action for which the mouse uses for this PlayPen.
     * 
     * @param zoomOutAction
     *            The zoom out action for the mouse to use in this PlayPen.
     */
	public void setMouseZoomOutAction(Action zoomOutAction){
        this.zoomOutAction = zoomOutAction;
    }
    
    /**
     * Modifies the given point p in model space to apparent position in screen
     * space.
     * 
     * @param p
     *            The point in model space (the space where the actual
     *            components of the content pane live). THIS PARAMETER IS
     *            MODIFIED.
     * @return The given point p, which has been modified or null if p was null.
     */
	public Point zoomPoint(Point p) {
	    if (p == null) return null;
		p.x = (int) ((double) p.x * zoom);
		p.y = (int) ((double) p.y * zoom);
		return p;
	}

    /**
     * Modifies the given point p from apparent position in screen space to
     * model space.
     * 
     * @param p
     *            The point in visible screen space (the space where mouse
     *            events are reported). THIS PARAMETER IS MODIFIED.
     * @return The given point p, which has been modified or null if p was null.
     */
	public Point unzoomPoint(Point p) {
	    if (p == null) return null;
		p.x = (int) ((double) p.x / zoom);
		p.y = (int) ((double) p.y / zoom);
		return p;
	}

	/**
	 * Modifies the given rect p in model space to apparent position
	 * in screen space.
	 *
	 * @param r The rectangle in model space (the space where the actual
	 * components of the content pane live).  THIS PARAMETER IS MODIFIED.
	 * @return The given rect p, which has been modified.
	 */
	public Rectangle zoomRect(Rectangle r) {
		r.x = (int) ((double) r.x * zoom);
		r.y = (int) ((double) r.y * zoom);
		r.width = (int) ((double) r.width * zoom);
		r.height = (int) ((double) r.height * zoom);
		return r;
	}

	/**
	 * Modifies the given rect r from apparent position in screen
	 * space to model space.
	 *
	 * @param r The rectangle in visible screen space (the space where
	 * mouse events are reported).  THIS PARAMETER IS MODIFIED.
	 * @return The given rect p, which has been modified.
	 */
	public Rectangle unzoomRect(Rectangle r) {
		r.x = (int) ((double) r.x / zoom);
		r.y = (int) ((double) r.y / zoom);
		r.width = (int) ((double) r.width / zoom);
		r.height = (int) ((double) r.height / zoom);
		return r;
	}

	// --------------------- accessors and mutators ----------------------

	public void setZoom(double newZoom) {
		if (newZoom != zoom) {
			double oldZoom = zoom;
			zoom = newZoom;
			if(session.isEnterpriseSession()) {
	            session.getEnterpriseSession().putPref("zoom", zoom);
			} else {
		        UserSettings sprefs = session.getUserSettings().getSwingSettings();
		        if (sprefs != null) {
		            sprefs.setObject("zoom", new Double(zoom));
		        }
			}
			this.firePropertyChange("zoom", oldZoom, newZoom); //$NON-NLS-1$
			this.revalidate();
			this.repaint();
		}
	}

	public double getZoom() {
		return zoom;
	}

	public void setRenderingAntialiased(boolean v) {
	    if (v) {
	        antialiasSetting = RenderingHints.VALUE_ANTIALIAS_ON;
	    } else {
	        antialiasSetting = RenderingHints.VALUE_ANTIALIAS_OFF;
	    }
	    this.repaint();
	}

	public boolean isRenderingAntialiased() {
	    return antialiasSetting == RenderingHints.VALUE_ANTIALIAS_ON;
	}

	public PlayPenContentPane getContentPane() {
		return contentPane;
	}
	
	public void setContentPane(PlayPenContentPane pane) {
	    contentPane = pane;
	    pane.setPlayPen(this);
	}

	public void addRelationship(Relationship r) {
		addImpl(r, null);
	}

    /**
     * This method is primarily for loading project files. Use at your own risk!
     *
     * @param tp
     * @param point
     */
    public void addTablePane(TablePane tp, Point point) {
        addImpl(tp, point);
    }
    
    public void addLabel(PlayPenLabel label, Point point) {
        addImpl(label, point);
    }

    /**
     * This method is primarily for loading project files. Use at your own risk!
     * 
     * @param ppc
     *            The component to add.
     * @param point
     *            The location to add the component at, in logical coordinates.
     *            If you don't care where the component lands, or the
     *            component's position is constrained by other factors
     *            (Relationships are positioned relative to the two table panes
     *            they connect) then this argument can be null.
     */
    public void addPlayPenComponent(PlayPenComponent ppc, Point point) {
        addImpl(ppc, point);
    }

	/**
	 * Searches this PlayPen's children for a TablePane whose model is
	 * t.
	 *
	 * @return A reference to the TablePane that has t as a model, or
	 * null if no such TablePane is in the play pen.
	 */
	public TablePane findTablePane(SQLTable t) {
		return (TablePane) findPPComponent(t);
	}
	
	/**
	 * Searches this PlayPen's children for a PlayPenComponent with the
	 * given model.
	 * 
	 * @return A reference to the PlayPenComponent with the given
	 * model, or null if no such PlayPenComponent is in the play pen 
	 */
	public PlayPenComponent findPPComponent(Object model) {
	    for (PlayPenComponent ppc : contentPane.getChildren()) {            
            if (ppc.getModel() == model) {
                return ppc;
            }
        }
	    return null;
	}

	/**
	 * Returns a TablePane in this PlayPen whose name is <code>name</code>.
	 *
	 * <p>Warning: Unique names are not currently enforced in the
	 * PlayPen's database; results will be unpredictable if there is
	 * more than one table with the name you are searching for.
	 *
	 * <p>Implementation note: This method may benefit from a
	 * Map-based lookup rather than the current linear search
	 * algorithm.
	 *
	 * @return A reference to the TablePane whose model name is
	 * <code>name</code>, or <code>null</code> if no such TablePane is
	 * in the play pen.
	 */
	public TablePane findTablePaneByName(String name) {
		name = name.toLowerCase();
		for (PlayPenComponent c : contentPane.getChildren()) {			
			if (c instanceof TablePane
				&& ((TablePane) c).getModel().getName().toLowerCase().equals(name)) {
				return (TablePane) c;
			}
		}
		return null;
	}

	/**
	 * Searches this PlayPen's children for a Relationship whose model is
	 * r.
	 *
	 * @return A reference to the Relationsip that has r as a model, or
	 * null if no such Relationship is in the play pen.
	 */
	public Relationship findRelationship(SQLRelationship r) {
		return (Relationship) findPPComponent(r);
	}

    /**
     * Returns the already in use table names. Useful for
     * deleting tables so it can be removed from this list as well.
     */
    public Set<String> getTableNames () {
        return tableNames;
    }
    
    /**
     * Reconstructs the set of table names by going through all the tables.
     */
    public void resetTableNames() {
        tableNames.clear();
        for (TablePane tp : contentPane.getChildren(TablePane.class)) {
            tableNames.add(tp.getModel().getName().toLowerCase());
        }
    }
    
	/**
	 * Returns the number of components in this PlayPen's
	 * PlayPenContentPane.
	 */
	public int getPPComponentCount() {
		return contentPane.getChildren().size();
	}

	/**
     * Adds or reverse engineers a copy of the given source table to this playpen, using
     * preferredLocation as the layout constraint.  Tries to avoid
     * adding two tables with identical names.
     *
     * @return A reference to the newly-created TablePane.
     * @see SQLTable#inherit
     * @see PlayPenLayout#addLayoutComponent(Component,Object)
     */
    public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation, DuplicateProperties duplicateProperties) throws SQLObjectException {
        return importTableCopy(source, preferredLocation, duplicateProperties, true);
    }

    /**
	 * Adds or reverse engineers a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 * 
	 * @return A reference to the newly-created TablePane.
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addLayoutComponent(Component,Object)
	 */
	public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation, DuplicateProperties duplicateProperties, boolean assignTypes) throws SQLObjectException {
	    SQLTable newTable;
	    switch (duplicateProperties.getDefaultTransferStyle()) {
	    case REVERSE_ENGINEER:
	        newTable = source.createInheritingInstance(session.getTargetDatabase()); // adds newTable to db
	        break;
	    case COPY:
	        newTable = source.createCopy(session.getTargetDatabase(), duplicateProperties.isPreserveColumnSource());
	        break;
	    default:
	        throw new IllegalStateException("Unknown transfer style " + duplicateProperties.getDefaultTransferStyle());
	    }
	    
	    //need to add data sources as necessary if a SQLObject was copied and pasted from one session
	    //to another in the same context. Also need to correct the source columns to point to the 
	    //correct session's source database objects.
	    for (SQLColumn column : newTable.getColumns()) {
	        SQLColumn sourceColumn = newTable.getColumnByName(column.getName());
	        ASUtils.correctSourceColumn(sourceColumn, duplicateProperties, column, getSession().getDBTree());
	    }

	    // Although this method is called in AddObjectsTask.cleanup(), it
        // remains here so that tests will use it as well. Columns that have
        // upstream types are ignored, so this is safe.
	    if (assignTypes) {
	        String platform;
	        if (source.getParentDatabase() != null && source.getParentDatabase().getDataSource() != null) {
	            platform = source.getParentDatabase().getDataSource().getParentType().getName();
	        } else {
	            platform = SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM;
	        }
	        SQLColumn.assignTypes(newTable.getColumns(), newTable.getParentDatabase().getDataSource().getParentCollection(), platform, getSession());
	    }
	    boolean isAlreadyOnPlaypen = false;
		
		// ensure tablename is unique
		if (logger.isDebugEnabled()) logger.debug("before add: " + tableNames); //$NON-NLS-1$
		int suffix = uniqueTableSuffix(source.getName());
		if (suffix != 0) {
		    String newName = source.getName() + "_" + suffix;
		    newTable.setName(newName);
		    isAlreadyOnPlaypen = true;
		}
		if (logger.isDebugEnabled()) logger.debug("after add: " + tableNames); //$NON-NLS-1$

		TablePane tp = new TablePane(newTable, getContentPane());
		logger.info("adding table "+newTable); //$NON-NLS-1$
		addImpl(tp, preferredLocation);
		tp.revalidate();
		
		createRelationshipsFromPP(source, newTable, true, isAlreadyOnPlaypen, suffix);
		createRelationshipsFromPP(source, newTable, false, isAlreadyOnPlaypen, suffix);
		
		return tp;
	}
	
	public int uniqueTableSuffix(String base) {
	    int suffix = 0;
	    if (!tableNames.add(base.toLowerCase())) {
            boolean done = false;
            while (!done) {
                suffix++;
                done = tableNames.add(base.toLowerCase() + "_" + suffix); //$NON-NLS-1$
            }
        }
	    return suffix;
	}

	/**
     * Creates exported relationships if the importing tables exist in the
     * PlayPen if isPrimaryKeyTableNew is set to true. Otherwise, it creates
     * imported relationships if the exporting tables exist in the PlayPen if
     * isPrimaryKeyTableNew is set to false.
     * 
     * @param source
     *            SQLTable representation of the table in the source database
     * @param newTable
     *            Newly created SQLTable instance into where the relationships
     *            are copied.
     * @param isPrimaryKeyTableNew
     *            Adds exported key relationships if true, imported keys if
     *            false.
     * @param isAlreadyOnPlaypen
     *            If the new table is already on playpen, its name will be changed
     *            Then we need to make sure its relationships don't point to the old
     *            tables
     * @param suffix
     *            Indicating the number of the copies of the table we have already
     *            on the playpen
     * @throws SQLObjectException
     */
    private void createRelationshipsFromPP(SQLTable source, SQLTable newTable, boolean isPrimaryKeyTableNew, boolean isAlreadyOnPlaypen, int suffix) throws SQLObjectException {
        PlayPenContentPane contentPane;
        if (this.contentPane instanceof PlayPenContentPane) {
            contentPane = (PlayPenContentPane) this.contentPane;
        } else {
            throw new IllegalStateException("Must have a PlayPenContent to make releationships: this PlayPen has " + this.contentPane.getClass().getName());
        }
        // create exported relationships if the importing tables exist in pp
		Iterator<SQLRelationship> sourceKeys = null;
        if (isPrimaryKeyTableNew) {
            sourceKeys = source.getExportedKeys().iterator();
        } else {
            sourceKeys = SQLRelationship.getExportedKeys(source.getImportedKeys()).iterator();
        }
		while (sourceKeys.hasNext()) {
			SQLRelationship r = sourceKeys.next();
			
			// If relationship is self-referencing, then don't add it twice.
			if (r.getFkTable().equals(r.getPkTable()) && !isPrimaryKeyTableNew) continue;
			
			if (logger.isInfoEnabled()) {
				logger.info("Looking for fk table "+r.getFkTable().getName()+" in playpen"); //$NON-NLS-1$ //$NON-NLS-2$
			}

            TablePane tablePane =  null;
            
            if(!isAlreadyOnPlaypen) {
                if (isPrimaryKeyTableNew){
                    tablePane =findTablePaneByName(r.getFkTable().getName());
                } else {
                    tablePane =findTablePaneByName(r.getPkTable().getName());
                }
            }
            else {
                if (isPrimaryKeyTableNew){
                    tablePane =findTablePaneByName(r.getFkTable().getName()+"_"+suffix); //$NON-NLS-1$
                } else {
                    tablePane =findTablePaneByName(r.getPkTable().getName()+"_"+suffix); //$NON-NLS-1$
                }
            }

			if (tablePane != null) {
				logger.info("FOUND IT!"); //$NON-NLS-1$

				SQLRelationship newRel = new SQLRelationship();
				newRel.updateToMatch(r, true);
				
				SQLTable oldTable;
				
				if (r.getFkTable().equals(r.getPkTable())) {
    			    // Prevents relationships from attaching to the wrong table
                    // if a table with a self referencing relationship gets
                    // imported twice.
    			    oldTable = newTable;
				} else {
				    oldTable = tablePane.getModel();
				}
				
				if (isPrimaryKeyTableNew) {
				    newRel.attachRelationship(newTable,oldTable,false);
				} else {
				    newRel.attachRelationship(oldTable,newTable,false);
				}
				
				addImpl(new Relationship(newRel, contentPane), null);

				Iterator<? extends SQLObject> mappings = r.getChildren().iterator();
				while (mappings.hasNext()) {
					SQLRelationship.ColumnMapping m
						= (SQLRelationship.ColumnMapping) mappings.next();
					setupMapping(newTable, oldTable, newRel, m,isPrimaryKeyTableNew);
				}
			} else {
				logger.info("NOT FOUND"); //$NON-NLS-1$
			}
		}
    }

    private void setupMapping(SQLTable newTable, SQLTable otherTable, SQLRelationship newRel, SQLRelationship.ColumnMapping m, boolean newTableIsPk) throws SQLObjectException {
        SQLColumn pkCol = null;
        SQLColumn fkCol = null;

        if (newTableIsPk) {
            pkCol=newTable.getColumnByName(m.getPkColumn().getName());
            fkCol=otherTable.getColumnByName(m.getFkColumn().getName());

            if (pkCol == null) {
                // this shouldn't happen
                throw new IllegalStateException("Couldn't find pkCol "+m.getPkColumn().getName()+" in new table"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (fkCol == null) {
                // this might reasonably happen (user deleted the column)
                return;
            }
        } else {
            pkCol=otherTable.getColumnByName(m.getPkColumn().getName());
            fkCol=newTable.getColumnByName(m.getFkColumn().getName());
            if (fkCol == null) {
                // this shouldn't happen
                throw new IllegalStateException("Couldn't find fkCol "+m.getFkColumn().getName()+" in new table"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pkCol == null) {
                // this might reasonably happen (user deleted the column)
                return;
            }
        }

        fkCol.addReference();
        SQLRelationship.ColumnMapping newMapping
        	= new SQLRelationship.ColumnMapping();
        newMapping.setPkColumn(pkCol);
        newMapping.setFkColumn(fkCol);
        newRel.addChild(newMapping);
    }

	/**
	 * Calls {@link #importTableCopy} for each table contained in the given schema.
	 */
	public synchronized void addObjects(List<SQLObject> list, Point preferredLocation, SPSwingWorker nextProcess, TransferStyles transferStyle) throws SQLObjectException {
		ProgressMonitor pm
		 = new ProgressMonitor(this,
		                      Messages.getString("PlayPen.copyingObjectsToThePlaypen"), //$NON-NLS-1$
		                      "...", //$NON-NLS-1$
		                      0,
			                  100);
		
		AddObjectsTask t = new AddObjectsTask(list,
				preferredLocation, pm, session, transferStyle);
		t.setNextProcess(nextProcess);
		new Thread(t, "Objects-Adder").start(); //$NON-NLS-1$
	}

	protected class AddObjectsTask extends SPSwingWorker {
		
        private List<SQLObject> sqlObjects;
		private Point preferredLocation;
		private String errorMessage = null;
		private ProgressMonitor pm;

        private final TransferStyles transferStyle;

		public AddObjectsTask(List<SQLObject> sqlObjects,
				Point preferredLocation,
				ProgressMonitor pm,
                ArchitectSwingSession session,
                TransferStyles transferStyle) {
            super(session);
			this.sqlObjects = sqlObjects;
			this.preferredLocation = preferredLocation;
            this.transferStyle = transferStyle;
			ProgressWatcher.watchProgress(pm, this);
			this.pm = pm;
		}

		/**
		 * Combines the MonitorableWorker's canceled flag with the
		 * ProgressMonitor's.
		 */
		@Override
		public synchronized boolean isCancelled() {
			return super.isCancelled() || pm.isCanceled();
		}

		/**
		 * Makes sure all the stuff we want to add is populated.
		 */
		public void doStuff() {
			logger.info("AddObjectsTask starting on thread "+Thread.currentThread().getName()); //$NON-NLS-1$
			session.getArchitectFrame().getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			try {
			    Iterator<SQLObject> soIt = sqlObjects.iterator();
			    // first pass: Cause all of the SQLObjects between the given 
			    // ones and the table descendents to populate...
			    while (soIt.hasNext() && !isCancelled()) {
			        SQLObject so = soIt.next();
			        SQLObjectUtils.countTablesSnapshot(so);
			    }
			} catch (SQLObjectException e) {
                logger.error("Unexpected exception during populate", e); //$NON-NLS-1$
                setDoStuffException(e);
                errorMessage = "Unexpected exception during populate: " + e.getMessage(); //$NON-NLS-1$
            }
			
			//Second pass: count the tables. Done in the foreground to 
			//wait for the objects to be fully populated by pass 1.
			session.runInForeground(new Runnable() {
			    public void run() {
			        try {
			            int tableCount = 0;
			            Iterator<SQLObject> soIt = sqlObjects.iterator();
			            while (soIt.hasNext() && !isCancelled()) {
			                SQLObject so = soIt.next();
			                tableCount += SQLObjectUtils.countTablesSnapshot(so);
			            }
			            setJobSize(new Integer(tableCount));
			        } catch (SQLObjectException e) {
			            logger.error("Unexpected exception, objects should be populated by " +
			            		"this pass.", e); //$NON-NLS-1$
			            setDoStuffException(e);
			            errorMessage = "Unexpected exception, objects should be populated " +
			            		"by this pass: " + e.getMessage(); //$NON-NLS-1$
			        }
			    }
			});

			ensurePopulated(sqlObjects);

			logger.info("AddObjectsTask done"); //$NON-NLS-1$
		}

		/**
		 * Ensures the given objects and all their descendants are populated from the database before returning, unless
		 * this worker gets cancelled.
         * 
         * This method is normally called from a worker thread, so don't use any swing API on it.
		 *
		 * @param so
		 */
		private void ensurePopulated(List<? extends SQLObject> soList) {
			for (SQLObject so : soList) {
				if (isCancelled()) break;
				if (so instanceof SQLTable) {
				    //pushing updates to foreground as population happens on the foreground
				    //and this will keep the progress bar more honest with what is happening.
				    session.runInForeground(new Runnable(){
                        public void run() {
                            setProgress(getProgress() + 1);                            
                        }
                    });
				}
                ensurePopulated(so.getChildren());
			}
		}

		/**
		 * Displays error messages or invokes the next process in the chain on a new
		 * thread. The run method asks swing to invoke this method on the event dispatch
		 * thread after it's done.
		 */
		public void cleanup() {
			if (getDoStuffException() != null) {
                ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                        errorMessage, getDoStuffException());
				if (getNextProcess() != null) {
					setCancelled(true);
				}
			}

			session.getPlayPen().startCompoundEdit("Drag to Playpen"); //$NON-NLS-1$
			
			// Filter out objects that would lose ETL lineage against the user's will.
			ImportSafetyChecker checker = new ImportSafetyChecker(session);
			sqlObjects = checker.filterImportedItems(sqlObjects);		
			
			session.getPlayPen().getContentPane().begin("Drag to Playpen");
			try {

				// reset iterator
				Iterator<SQLObject> soIt = sqlObjects.iterator();

				// Track all columns added so we can assign types
				ArrayListMultimap<String, SQLColumn> addedColumns = ArrayListMultimap.create();
				
				resetTableNames();
				while (soIt.hasNext() && !isCancelled()) {
					SQLObject someData = soIt.next();
					DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(getSession(), someData);
					if (transferStyle == TransferStyles.COPY && duplicateProperties.isCanCopy()) {
					    duplicateProperties.setDefaultTransferStyle(transferStyle);
					} else if (transferStyle == TransferStyles.REVERSE_ENGINEER && duplicateProperties.isCanReverseEngineer()) {
					    duplicateProperties.setDefaultTransferStyle(transferStyle);
					}
					
					if (someData instanceof SQLTable) {
						TablePane tp = importTableCopy((SQLTable) someData, preferredLocation, duplicateProperties, false);
						setMessage(ArchitectUtils.truncateString(((SQLTable)someData).getName()));
                        preferredLocation.x += tp.getPreferredSize().width + 5;
                        
                        SQLDatabase dbAncestor = SQLPowerUtils.getAncestor(someData, SQLDatabase.class);
                        String platform;
                        if (dbAncestor == null) {
                            platform = null;
                        } else {
                            platform = dbAncestor.getDataSource().getParentType().getName();
                        }
                        addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
                        
                        increaseProgress();
					} else if (someData instanceof SQLSchema) {
						SQLSchema sourceSchema = (SQLSchema) someData;
						Iterator<? extends SQLObject> it = sourceSchema.getChildren().iterator();
						while (it.hasNext() && !isCancelled()) {
                            Object nextTable = it.next();
							SQLTable sourceTable = (SQLTable) nextTable;
							setMessage(ArchitectUtils.truncateString(sourceTable.getName()));
							TablePane tp = importTableCopy(sourceTable, preferredLocation, duplicateProperties, false);
							preferredLocation.x += tp.getPreferredSize().width + 5;
							
							String platform = SQLPowerUtils.getAncestor(someData, SQLDatabase.class).getDataSource().getParentType().getName();
	                        addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
							
							increaseProgress();
						}
					} else if (someData instanceof SQLCatalog) {
						SQLCatalog sourceCatalog = (SQLCatalog) someData;
						Iterator<? extends SQLObject> cit = sourceCatalog.getChildren().iterator();
						if (sourceCatalog.isSchemaContainer()) {
							while (cit.hasNext() && !isCancelled()) {
								SQLSchema sourceSchema = (SQLSchema) cit.next();
								Iterator<? extends SQLObject> it = sourceSchema.getChildren().iterator();
								while (it.hasNext() && !isCancelled()) {
									Object nextTable = it.next();
                                    SQLTable sourceTable = (SQLTable) nextTable;
									setMessage(ArchitectUtils.truncateString(sourceTable.getName()));
									TablePane tp = importTableCopy(sourceTable, preferredLocation, duplicateProperties, false);
									preferredLocation.x += tp.getPreferredSize().width + 5;
									
									String platform = SQLPowerUtils.getAncestor(someData, SQLDatabase.class).getDataSource().getParentType().getName();
		                            addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
									
									increaseProgress();
								}
							}
						} else {
							while (cit.hasNext() && !isCancelled()) {
                                Object nextTable = cit.next();
								SQLTable sourceTable = (SQLTable) nextTable;
								setMessage(ArchitectUtils.truncateString(sourceTable.getName()));
								TablePane tp = importTableCopy(sourceTable, preferredLocation, duplicateProperties, false);
								preferredLocation.x += tp.getPreferredSize().width + 5;
								
								String platform = SQLPowerUtils.getAncestor(someData, SQLDatabase.class).getDataSource().getParentType().getName();
	                            addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
								
								increaseProgress();
							}
						}
					} else {
						logger.error("Unknown object dropped in PlayPen: "+someData); //$NON-NLS-1$
					}
				}
				
				for (String platform : addedColumns.keySet()) {
				    SQLColumn.assignTypes(addedColumns.get(platform), session.getDataSources(), platform, session);
				}
				
				session.getPlayPen().getContentPane().commit();
			} catch (SQLObjectException e) {
			    session.getPlayPen().getContentPane().rollback(e.getMessage());
				ASUtils.showExceptionDialog(session,
                    "Unexpected Exception During Import", e); //$NON-NLS-1$
			} catch (Throwable e) {
			    session.getPlayPen().getContentPane().rollback(e.getMessage());
			    throw new RuntimeException(e);
			} finally {
				session.getArchitectFrame().getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				session.getPlayPen().endCompoundEdit("Ending multi-select"); //$NON-NLS-1$
			}
			
			// deals with bug 1333, when the user tries to add inaccessible objects to the PlayPen
			if (getJobSize() == 0) { 
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    JOptionPane.showMessageDialog(session.getArchitectFrame(),
	                            Messages.getString("PlayPen.noObjectsToImportFound"), Messages.getString("PlayPen.noObjectsToImportFoundDialogTitle"), //$NON-NLS-1$ //$NON-NLS-2$
	                            JOptionPane.WARNING_MESSAGE);
	                }
	            });
	        }
		}

	}

	// -------------------- SQLOBJECT EVENT SUPPORT ---------------------

	private int transactionCount = 0;
	
	private boolean childAdded = false;
	
	private boolean childRemoved = false;
	
	private boolean propertyChanged = false;
	
	/**
	 * Adds all the listeners that should be listening to events from
	 * the sqlobject hierarchy.  At this time only the play pen
	 * needs to listen.
	 */
	private void addHierarchyListeners(SPObject sqlObject)
	{
		SQLPowerUtils.listenToHierarchy(sqlObject, this);

	}

	/**
	 * Removes all the listeners that should be listening to events from
	 * the sqlobject hierarchy.  At this time only the play pen
	 * needs to be removed
	 */
	private void removeHierarchyListeners(SPObject sqlObject)
	{
		SQLPowerUtils.unlistenToHierarchy(sqlObject, this);
	}

	/**
	 * Listens for property changes in the model (tables
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void childAdded(SPChildEvent e) {
		logger.debug("SQLObject children got inserted: "+e); //$NON-NLS-1$
		SPObject child = e.getChild();
        addHierarchyListeners(child);
        
        if (!contentPane.isMagicEnabled() || !e.getSource().isMagicEnabled()) return;
        
        childAdded = true;
        boolean fireEvent = false;
		try {
            if (child instanceof SQLTable
                    || (child instanceof SQLRelationship
                            && ((SQLTable) e.getSource()).getExportedKeys().contains(child))) {
                fireEvent = true;

                PlayPenComponent ppc = removedComponents.get(child.getUUID());
                if (ppc != null) {
                    if (PlayPenContentPane.isDependentComponentType(ppc.getClass())) {
                        contentPane.addChild(ppc, contentPane.getFirstDependentComponentIndex());
                    } else {
                        contentPane.addChild(ppc, 0);
                    }

                }
            }
        } catch (SQLObjectException ex) {
            throw new RuntimeException(ex);
        }

		if (fireEvent) {
		    firePropertyChange("model.children", null, child); //$NON-NLS-1$
		}
	}

	/**
	 * Listens for property changes in the model (columns
	 * removed).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void childRemoved(SPChildEvent e) {
		logger.debug("SQLObject children got removed: "+e); //$NON-NLS-1$
		SPObject child = e.getChild();
		removeHierarchyListeners(child);
		
		if (!contentPane.isMagicEnabled() || !e.getSource().isMagicEnabled()) return;
		
		boolean foundRemovedComponent = false;

		try {
		    if (child instanceof SQLTable) {
		        for (int i = 0; i < contentPane.getChildren().size(); i++) {
		            PlayPenComponent c = contentPane.getChildren().get(i);
		            if (c instanceof TablePane) {
		                TablePane tp = (TablePane) c;
		                if (tp.getModel() == child) {
		                    removedComponents.put(tp.getModel().getUUID(), c);
		                    contentPane.removeChild(c);
		                    foundRemovedComponent = true;
		                }
		            }
		        }
		    } else if (child instanceof SQLRelationship || child instanceof SQLImportedKey) {		        
		        for (int i = 0; i < contentPane.getChildren().size(); i++) {
		            PlayPenComponent c = contentPane.getChildren().get(i);
		            if (c instanceof Relationship) {
		                Relationship r = (Relationship) c;
		                if (r.getModel() == child || (child instanceof SQLImportedKey && r.getModel() == ((SQLImportedKey) child).getRelationship())) {
		                    r.setSelected(false,SelectionEvent.SINGLE_SELECT);
		                    removedComponents.put(r.getModel().getUUID(), c);
		                    contentPane.removeChild(c);
		                    foundRemovedComponent = true;
		                }
		            }
		        }
		    }
		} catch (ObjectDependentException ex) {
		    throw new RuntimeException(ex);
		}

		if (foundRemovedComponent) {
		    childRemoved = true;
		    firePropertyChange("model.children", child, null); //$NON-NLS-1$
		}
	}

	/**
	 * Listens for property changes in the model (table
	 * properties modified).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void propertyChanged(PropertyChangeEvent e) {
	    revalidate();
	    this.firePropertyChange("model."+e.getPropertyName(), e.getOldValue(), e.getNewValue()); //$NON-NLS-1$
	}
	
	public void transactionStarted(TransactionEvent e) {
	    transactionCount++;
	}
	
	public void transactionEnded(TransactionEvent e) {
	    transactionCount--;
	    if (transactionCount == 0) {
	        if (childAdded || propertyChanged) {
	            this.revalidate();
	        }
	        if (childRemoved) {
	            this.repaint();
	        }
	        childAdded = false;
	        childRemoved = false;
	        propertyChanged = false;
	    } else if (transactionCount < 0) {
	        throw new IllegalStateException("Commit attempted while not in a transaction");
	    }
	}
	
	public void transactionRollback(TransactionEvent e) {
	    logger.error("Unable to rollback changes to the playpen");
	    transactionCount = 0;
	    childAdded = false;
	    childRemoved = false;
	    propertyChanged = false;
	}
	
    // ---------------------- SELECTION LISTENER ------------------------

    /**
     * Forwards the selection event <code>e</code> to all PlayPen
     * selection listeners.
     */
    public void itemSelected(SelectionEvent e) {
        fireSelectionEvent(e);
    }

    /**
     * Forwards the selection event <code>e</code> to all PlayPen
     * selection listeners.
     */
    public void itemDeselected(SelectionEvent e) {
        fireSelectionEvent(e);
    }

	// --------------- SELECTION METHODS ----------------

	/**
	 * Deselects all selectable items in the PlayPen.
	 */
	public void selectNone() {
	 	session.getDBTree().clearSelection();
 		for (PlayPenComponent c : contentPane.getChildren()) {
 			if (c instanceof Selectable) {
 				Selectable s = (Selectable) c;
 				s.setSelected(false,SelectionEvent.SINGLE_SELECT);
 			}
 		}
 		// for symmetry with selectAll, it would be tempting to change the
 		// state of the mouse listener here.  However, that would interfere
 		// with its operation (because it uses this method internally)
	}

	/**
	 * Selects all selectable items in the PlayPen.
	 */
	public void selectAll() {
 		for (PlayPenComponent c : contentPane.getChildren()) {
 			if (c instanceof Selectable) { 			    
 				Selectable s = (Selectable) c;
 				s.setSelected(true,SelectionEvent.SINGLE_SELECT);
 			}
 		}
 		mouseMode = MouseModeType.MULTI_SELECT;
//        updateDBTree();
	}


	/**
	 * Returns a read-only view of the set of selected children in the PlayPen.
	 */
	public List<PlayPenComponent> getSelectedItems() {
		// It would be possible to speed this up by maintaining a
		// cache of which children are selected, but the need would
		// have to be demonstrated first.
		List<PlayPenComponent> selected = new ArrayList<PlayPenComponent>();
 		for (PlayPenComponent c : contentPane.getChildren()) {
 		    if (c.isSelected()) {
 		        selected.add(c);
			}
		}
		return Collections.unmodifiableList(selected);
	}

	/**
	 * Returns a read-only view of the set of selected ContainerPane's in the PlayPen.
	 */
	public List <ContainerPane<?, ?> > getSelectedContainers() {
		ArrayList <ContainerPane<?, ?> > selected = new ArrayList<ContainerPane<?, ?> >();
 		for (ContainerPane<?, ?> tp : contentPane.getChildren(ContainerPane.class)) {
 		    if (tp.isSelected()) {
 		        selected.add(tp);
 		    }
		}
		return Collections.unmodifiableList(selected);
	}
	
	public List<DraggablePlayPenComponent> getSelectedDraggableComponents() {
	    ArrayList <DraggablePlayPenComponent> selected = new ArrayList<DraggablePlayPenComponent>();
        for (DraggablePlayPenComponent tp : contentPane.getChildren(DraggablePlayPenComponent.class)) {
            if (tp.isSelected()) {
                selected.add(tp);
            }
        }
        return Collections.unmodifiableList(selected);
	}

	/**
	 * Returns a read-only view of the set of selected relationships in the PlayPen.
	 */
	public List <Relationship>getSelectedRelationShips() {
		ArrayList<Relationship> selected = new ArrayList<Relationship>();
 		for (Relationship r : contentPane.getChildren(Relationship.class)) {
 		    if (r.isSelected()) {
 		        selected.add(r);
 		    }
		}
		return Collections.unmodifiableList(selected);
	}
	
	// --------------------- SELECTION EVENT SUPPORT ---------------------

	protected LinkedList<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}

	protected void fireSelectionEvent(SelectionEvent e) {
		if (e.getType() == SelectionEvent.SELECTION_EVENT) {
			for (int i = selectionListeners.size() - 1; i >= 0; i--) {
				selectionListeners.get(i).itemSelected(e);
			}
		} else if (e.getType() == SelectionEvent.DESELECTION_EVENT) {
		    for (int i = selectionListeners.size() - 1; i >= 0; i--) {
                selectionListeners.get(i).itemDeselected(e);
            }
		} else {
			throw new IllegalStateException("Unknown selection event type "+e.getType()); //$NON-NLS-1$
		}
	}
	
	// Cancel Support
	protected LinkedList<CancelableListener> cancelableListeners = new LinkedList<CancelableListener>();

	public void addCancelableListener(CancelableListener l) {
		cancelableListeners.add(l);
	}

	public void removeCancelableListener(CancelableListener l) {
		cancelableListeners.remove(l);
	}

	public void fireCancel(){
		for(int i = cancelableListeners.size()-1; i>=0;i--) {
			cancelableListeners.get(i).cancel();
		}
	}
	// Undo event support --------------------------------------

	protected LinkedList<CompoundEventListener> undoEventListeners = new LinkedList<CompoundEventListener>();

	public void addUndoEventListener(CompoundEventListener l) {
		undoEventListeners.add(l);
	}

	public void removeSelectionListener(CompoundEventListener l) {
		undoEventListeners.remove(l);
	}

	private void fireUndoCompoundEvent(TransactionEvent e) {
		Iterator<CompoundEventListener> it = undoEventListeners.iterator();

		if (e.getState().equals(TransactionState.START)) {
			while (it.hasNext()) {
				it.next().transactionStarted(e);
			}
		} else {
			while (it.hasNext()) {
				it.next().transactionEnded(e);
			}
		}

	}

	public void startCompoundEdit(String message){
		fireUndoCompoundEvent(TransactionEvent.createStartTransactionEvent(this, message));
	}

	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(TransactionEvent.createEndTransactionEvent(this));
	}
	// ------------------------------------- INNER CLASSES ----------------------------

	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.  Also retargets drops to the
	 * TablePanes when necessary.
	 */
	private static class PlayPenDropListener implements DropTargetListener {

		/**
		 * When the user moves over a container pane, its drop target's
		 * dragEnter method will be called, and this variable will
		 * reference it.  When the user moves off of a pane, its
		 * dragExit method will be called, and this variable will
		 * reference null (or a different table pane).
		 */
		private ContainerPane<?,?> tpTarget;

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer enters the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
			logger.debug("Drag enter"); //$NON-NLS-1$
			dragOver(dtde);
		}

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer has exited the operable part of the drop site for the
		 * DropTarget registered with this listener or escape has been pressed
		 */
		public void dragExit(DropTargetEvent dte) {
			logger.debug("Drag exit"); //$NON-NLS-1$
            if (tpTarget != null) {
            		tpTarget.dragExit(dte);
            }

		}

		/**
		 * Called when a drag operation is ongoing, while the mouse
		 * pointer is still over the operable part of the drop site for
		 * the DropTarget registered with this listener.
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			PlayPen pp = (PlayPen) dtde.getDropTargetContext().getComponent();
			Point sp = pp.unzoomPoint(new Point(dtde.getLocation()));
			PlayPenComponent ppc = pp.contentPane.getComponentAt(sp);
			ContainerPane<?, ?> tp;
			if (ppc instanceof ContainerPane<?, ?>) {
			    tp = (ContainerPane<?, ?>) ppc;
			} else {
			    tp = null;
			}

			if (tp != tpTarget) {
				if (tpTarget != null) {
					tpTarget.dragExit(dtde);
				}
				tpTarget = tp;
				if (tpTarget != null) {
					tpTarget.dragEnter(dtde);
				}
			}
			if (tpTarget != null) {
				tpTarget.dragOver(dtde);
			} else {
				dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE & dtde.getDropAction());
			}
		}

		/**
		 * Processes the drop action on the PlayPen (the DropTarget)
		 * or current target TablePane if there is one.
		 */
		public void drop(DropTargetDropEvent dtde) {
		    logger.debug("On drop, source actions are " + dtde.getSourceActions() + " and drop action is " + dtde.getDropAction());
			logger.info("Drop: I am over dtde="+dtde); //$NON-NLS-1$
			if (tpTarget != null) {
				tpTarget.drop(dtde);
				return;
			}
			
			if ((dtde.getSourceActions() & dtde.getDropAction()) == 0) {
			    dtde.rejectDrop();
			    return;
			}
			
			Transferable t = dtde.getTransferable();
			PlayPen playpen = (PlayPen) dtde.getDropTargetContext().getComponent();
			try {
			    Point dropLoc = playpen.unzoomPoint(new Point(dtde.getLocation()));
			    if (playpen.addTransferable(t, dropLoc, TransferStyles.REVERSE_ENGINEER)){			        
			        dtde.acceptDrop(DnDConstants.ACTION_COPY);
			        dtde.dropComplete(true);
			    } else {
			        dtde.rejectDrop();
			    }

			} catch (UnsupportedFlavorException ufe) {
			    logger.error(ufe);
			    dtde.rejectDrop();
			} catch (IOException ioe) {
			    logger.error(ioe);
			    dtde.rejectDrop();
			} catch (InvalidDnDOperationException ex) {
			    logger.error(ex);
			    dtde.rejectDrop();
			} catch (SQLObjectException ex) {
			    logger.error(ex);
			    dtde.rejectDrop();
			}
		}

		/**
		 * Called if the user has modified the current drop gesture.
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
			logger.debug("Drop Action Changed"); //$NON-NLS-1$
            // we don't care
		}

		/**
		 * Chooses the best import flavour from the flavors array for
		 * importing into c.  The current implementation actually just
		 * chooses the first acceptable flavour.
		 *
		 * @return The first acceptable DataFlavor in the flavors
		 * list, or null if no acceptable flavours are present.
		 */
		public DataFlavor bestImportFlavor(JComponent c, DataFlavor[] flavors) {
			DataFlavor best = null;
			logger.debug("PlayPenTransferHandler: can I import "+Arrays.asList(flavors)); //$NON-NLS-1$
 			for (int i = 0; i < flavors.length; i++) {
				String cls = flavors[i].getDefaultRepresentationClassAsString();
				logger.debug("representation class = "+cls); //$NON-NLS-1$
				logger.debug("mime type = "+flavors[i].getMimeType()); //$NON-NLS-1$
				logger.debug("type = "+flavors[i].getPrimaryType()); //$NON-NLS-1$
				logger.debug("subtype = "+flavors[i].getSubType()); //$NON-NLS-1$
				logger.debug("class = "+flavors[i].getParameter("class")); //$NON-NLS-1$ //$NON-NLS-2$
				logger.debug("isSerializedObject = "+flavors[i].isFlavorSerializedObjectType()); //$NON-NLS-1$
				logger.debug("isInputStream = "+flavors[i].isRepresentationClassInputStream()); //$NON-NLS-1$
				logger.debug("isRemoteObject = "+flavors[i].isFlavorRemoteObjectType()); //$NON-NLS-1$
				logger.debug("isLocalObject = "+flavors[i].getMimeType().equals(DataFlavor.javaJVMLocalObjectMimeType)); //$NON-NLS-1$


 				if (flavors[i].equals(SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR)) {
					logger.debug("YES"); //$NON-NLS-1$
					best = flavors[i];
				} else {
					logger.debug("NO!"); //$NON-NLS-1$
				}
 			}
 			return best;
		}

		/**
		 * This is set up this way because this DropTargetListener was
		 * derived from a TransferHandler.  It works, so no sense in
		 * changing it.
		 */
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			return bestImportFlavor(c, flavors) != null;
		}
	}

    /**
     * This method takes a transferable object and tries to add the SQLObjects
     * in the transferable to the play pen. If there is no transferable object
     * then if there is a string or list of strings in the transferable this
     * method will try to create SQLObjects for the transferred values.
     * This is package protected for the ArchitectFrame's benefit.
     * 
     * @param t
     *            The transferable to get objects from to add to the playpen
     * @param dropPoint
     *            The location where new objects will be added to the playpen
     * @return True if objects were added successfully to the playpen. False
     *         otherwise.
     * @throws IOException 
     * @throws UnsupportedFlavorException 
     * @throws SQLObjectException 
     */
	boolean addTransferable(Transferable t, Point dropPoint, TransferStyles transferStyle)
	throws UnsupportedFlavorException, IOException, SQLObjectException {
	    
	    if (t.isDataFlavorSupported(SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR)
	            || t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
	        
	        contentPane.begin("Adding transferrable to play pen");
	        try {

	            if (t.isDataFlavorSupported(SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR)) {
	                SQLObject[] paths = (SQLObject[]) t.getTransferData(
	                        SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR);
	                // turn into a Collection of SQLObjects to make this more generic
	                List<SQLObject> sqlObjects = new ArrayList<SQLObject>();
	                for (Object oo : paths) {
	                    if (oo instanceof SQLObject) {
	                        sqlObjects.add((SQLObject) oo);
	                    } else {
	                        logger.error("Unknown object dropped in PlayPen: "+oo); //$NON-NLS-1$
	                    }
	                }

	                // null: no next task is chained off this
	                addObjects(sqlObjects, dropPoint, null, transferStyle);
	                contentPane.commit();
	                return true;
	            } else {
	                String[] stringPieces = 
	                    ((String) t.getTransferData(DataFlavor.stringFlavor)).split("[\n\r\t]+");
	                List<SQLObject> sqlObjects = new ArrayList<SQLObject>();
	                for (String s : stringPieces) {
	                    if (s.length() > 0) {
	                        SQLTable newTable = new SQLTable();
	                        newTable.setName(s);
	                        newTable.initFolders(true);
	                        sqlObjects.add(newTable);
	                    }
	                }

	                addObjects(sqlObjects, dropPoint, null, transferStyle);
	                contentPane.commit();
	                return true;
	            }
	        } catch (Throwable e) {
	            contentPane.rollback("Error occurred: " + e.toString());
	            throw new RuntimeException(e);
	        }
	    }
	    
	    return false;
	}
	
	/**
     * This method takes a transferable object and tries to add the SQLObjects
     * in the transferable to the play pen. If there is no transferable object
     * then if there is a string or list of strings in the transferable this
     * method will try to create SQLObjects for the transferred values. The
     * objects will be placed at the mouse location if it is on the play pen
     * or at the origin if it is not.
     * 
     * @param t
     *            The transferable to get objects from to add to the playpen
     */
	public void pasteData(Transferable t) {
	    // check if user trying to paste table while copied table is still selected
        if (!getSelectedContainers().isEmpty()  &&  !isCopyingTable(t)) {
            for (ContainerPane<?, ?> cp : getSelectedContainers()) {
                cp.pasteData(t);
            }
            return;
        }
	    Point p = this.getMousePosition();
	    if (p == null) {
	        p = new Point(0, 0);
	    }
	    unzoomPoint(p);
	    try {
            if (!addTransferable(t, p, TransferStyles.COPY)) {
                JOptionPane.showMessageDialog(this, "Cannot paste the copied objects. Unknown object type.", "Cannot Paste Objects", JOptionPane.WARNING_MESSAGE);
            }
        } catch (UnsupportedFlavorException e) {
            throw new RuntimeException("Could not paste copied object type.", e);
        } catch (IOException e) {
            logger.error("The real IOException", e);
            throw new RuntimeException("Data copied changed while pasting.", e);
        } catch (SQLObjectException e) {
            throw new RuntimeException("Exception while pasting a SQLObject.", e);
        }
	}

	/**
	 * This method checks if user try to paste table while copied table is still selected instead of playpen
	 * @param t
	 * @return true if trying to paste table 
	 */
	private boolean isCopyingTable(Transferable t) {

	    boolean copyingTable = false;

	    List<? extends SQLObject> copyingItems;

	    for (ContainerPane<?, ?> cp : getSelectedContainers()) {
	        DataFlavor flavor = cp.bestImportFlavor(this, t.getTransferDataFlavors());

	        try {
	            if (flavor == SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR) {
	                copyingItems = Arrays.asList((SQLObject[]) t.getTransferData(flavor));
	                for(SQLObject o:copyingItems ) {
	                    if (o instanceof SQLTable) {
	                        copyingTable = true;
	                        break;
	                    }
	                }
	                if(copyingTable) break;
	            } 
	        } catch (UnsupportedFlavorException e) {
	            throw new RuntimeException("Cannot add items to a table of type " + flavor, e);
	        } catch (IOException e) {
	            throw new RuntimeException("Transfer type changed while adding it to the table", e);
	        } 
	    }

	    return copyingTable;
	}

    public class TablePaneDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {

            // ignore drag events that aren't from the left mouse button
            if (dge.getTriggerEvent() instanceof MouseEvent
               && (dge.getTriggerEvent().getModifiers() & InputEvent.BUTTON1_MASK) == 0)
                return;

            // ignore drag events if we're in the middle of a createRelationship
            // XXX this is backwards--the action should disable DnD on the playpen by setting a flag
            if (session.getArchitectFrame().createRelationshipIsActive()) {
                logger.debug("CreateRelationship() is active, short circuiting DnD."); //$NON-NLS-1$
                return;
            }

			if (draggingContainerPanes) {
				logger.debug(
						"TablePaneDragGestureListener: ignoring drag event " + //$NON-NLS-1$
						"because draggingTablePanes is true"); //$NON-NLS-1$
				return;
			}

			MouseEvent triggerEvent = (MouseEvent) dge.getTriggerEvent();
            PlayPenComponent c = contentPane.getComponentAt(unzoomPoint(triggerEvent.getPoint()));

			if ( c instanceof ContainerPane<?,?> ) {
				ContainerPane<?,?> tp = (ContainerPane<?,?>) c;

				Point dragOrigin = tp.getPlayPen().unzoomPoint(new Point(dge.getDragOrigin()));
				dragOrigin.x -= tp.getX();
				dragOrigin.y -= tp.getY();

				logger.debug("Recognized drag gesture on "+tp.getName()+"! origin="+dragOrigin); //$NON-NLS-1$ //$NON-NLS-2$

				Transferable transferableSelection = tp.createTransferableForSelection();
				if (transferableSelection != null) {
                    DnDLabel label = new DnDLabel(transferableSelection.toString());
                    Dimension labelSize = label.getPreferredSize();
                    label.setSize(labelSize);  // because a LayoutManager would normally do this
                    BufferedImage dragImage = new BufferedImage(
                            labelSize.width, labelSize.height,
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics2D imageGraphics = dragImage.createGraphics();
                    label.paint(imageGraphics);
                    imageGraphics.dispose();
                    dge.getSourceAsDragGestureRecognizer().setSourceActions(DnDConstants.ACTION_COPY_OR_MOVE);
                    dge.getDragSource().startDrag(
				            dge, null, dragImage, new Point(0, 0), transferableSelection, tp);
				}
			}
		}
	}

	/**
	 * A nice multi-line translucent label that produces a good drag-and-drop image.
	 */
	private static class DnDLabel extends JTextArea {
	
        private static final int BORDER_WIDTH = 5;

        DnDLabel(String text) {
            super(text);
            setOpaque(false);
            setBackground(new Color(0xcc333333, true));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
	    }
        
        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_WIDTH * 3, BORDER_WIDTH * 3);
            super.paint(g);
        }
	}
	
	/**
	 * The PPMouseListener class receives all mouse and mouse motion
	 * events in the PlayPen.  It tries to dispatch them to the
	 * ppcomponents, and also handles playpen-specific behaviour like
	 * rubber band selection and popup menu triggering.
	 */
	protected class PPMouseListener
		implements MouseListener, MouseMotionListener {


		/**
		 * This state is required by the mouseMoved method for
		 * resizing the rubber band in response to user input.
		 */
		protected Point rubberBandOrigin;
		// ------------------- MOUSE LISTENER INTERFACE ------------------

		public void mouseEntered(MouseEvent evt) {
			// doesn't matter
		}

		public void mouseExited(MouseEvent evt) {
			// doesn't matter
		}

		public void mouseClicked(MouseEvent evt) {
			Point p = evt.getPoint();
			unzoomPoint(p);
			PlayPenComponent c = contentPane.getComponentAt(p);
			
			if (c instanceof PlayPenComponent) {
			    c.handleMouseEvent(evt);
			} else {
                session.getArchitectFrame().getCreateIdentifyingRelationshipAction().cancel();
                session.getArchitectFrame().getCreateNonIdentifyingRelationshipAction().cancel();
//				maybeShowPopup(evt);
			}
//			updateDBTree();
		}


		public void mousePressed(MouseEvent evt) {
		    requestFocus();
			Point p = evt.getPoint();
			unzoomPoint(p);
			PlayPenComponent c = contentPane.getComponentAt(p);
            selectionInProgress = true;

			if (c instanceof PlayPenComponent) {
			    c.handleMouseEvent(evt);
			} else {
				if ((evt.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0  && !evt.isPopupTrigger()) {
					mouseMode = MouseModeType.IDLE;
					selectNone();
					rubberBandOrigin = new Point(p);
					rubberBand = new Rectangle(rubberBandOrigin.x, rubberBandOrigin.y, 0, 0);
				}
			}
			maybeShowPopup(evt);
		}

		public void mouseReleased(MouseEvent evt) {
		   	
		    draggingContainerPanes = false;
            selectionInProgress = false;

			if (rubberBand != null && evt.getButton() == MouseEvent.BUTTON1) {
			    Rectangle dirtyRegion = rubberBand;

			    rubberBandOrigin = null;
			    rubberBand = null;

			    zoomRect(dirtyRegion);
			    repaintRubberBandRegion(dirtyRegion);
			    if ( getSelectedItems().size() > 0 ) {
			        mouseMode = MouseModeType.MULTI_SELECT;
			    } else {
			        mouseMode = MouseModeType.IDLE;
			    }
			}
			maybeShowPopup(evt);
			repaint();
//            updateDBTree();
		}

		// ---------------- MOUSEMOTION LISTENER INTERFACE -----------------
		public void mouseDragged(MouseEvent evt) {
			mouseMoved(evt);
		}	

		public void mouseMoved(MouseEvent evt) {
			if (rubberBand != null) {
				// repaint old region in case of shrinkage
				Rectangle dirtyRegion = zoomRect(new Rectangle(rubberBand));

				Point p = unzoomPoint(evt.getPoint());
				rubberBand.setBounds(rubberBandOrigin.x, rubberBandOrigin.y, 0, 0);
				rubberBand.add(p);

				mouseMode = MouseModeType.RUBBERBAND_MOVE;
				// update selected items
				for (PlayPenComponent c : contentPane.getChildren()) {					 
					c.handleMouseEvent(evt);
				}

				// Add the new rubberband to the dirty region and grow
				// it in case the line is thick due to extreme zoom
				dirtyRegion.add(zoomRect(new Rectangle(rubberBand)));
				repaintRubberBandRegion(dirtyRegion);
			}
		}

		/**
		 * Asks the PlayPen to repaint the given region, which is in screen coordinates (not
		 * logical playpen coordinates).
		 *
		 * @param region The region to repaint.  If this comes from a user input event, you have
		 * to run the argument through the {@link #zoomRect(Rectangle)} method.
		 */
		private void repaintRubberBandRegion(Rectangle region) {
			Rectangle dirtyRegion = new Rectangle(region);
			dirtyRegion.x -= 3;
			dirtyRegion.y -= 3;
			dirtyRegion.width += 6;
			dirtyRegion.height += 6;
			repaint(dirtyRegion);
		}

		/**
		 * Shows the popup menus if appropriate.
		 */
		public void maybeShowPopup(MouseEvent evt) {
		    Point p = unzoomPoint(evt.getPoint());
		    PlayPenComponent c = contentPane.getComponentAt(p);
		    if(!mouseMode.equals(MouseModeType.CREATING_RELATIONSHIP) && 
		            !mouseMode.equals(MouseModeType.CREATING_TABLE)) {
    		    if (c != null) {
    		        if(!c.isBeingDragged()) {
        		        p.translate(-c.getX(), -c.getY());
        		        if (evt.isPopupTrigger() && !evt.isConsumed()) {
        		            c.showPopup(p);
        		        }
    		        }
    		    } else {
    		        if (evt.isPopupTrigger() && popupFactory != null) {
    		            JPanel pp = (JPanel) evt.getSource();
    		            //XXX we should let popupfactory to produce playpencomponent popup as well
    		            JPopupMenu popup = popupFactory.createPopupMenu(null);
    		            popup.show(pp, evt.getX(), evt.getY());
    		        }
    		    }
		    }
		}
	}

    public Rectangle getRubberBand() {
        return rubberBand;
    }
    
    /**
     * Returns true if there is a multi-select operation in progress. This method
     * is useful for selection listeners such as the selection synchronizer that should
     * not update their state while a selection is in progress.
     */
    public boolean isSelectionInProgress() {
        return selectionInProgress;
    }

    public boolean isDraggingTablePanes() {
        return draggingContainerPanes;
    }

    public void setDraggingContainerPanes(boolean draggingContainerPanes) {
        this.draggingContainerPanes = draggingContainerPanes;
    }

	// ---------- Floating Table Listener ------------

	/**
	 * Listens to mouse motion and moves the given component so it
	 * follows the mouse.  When the user lifts the mouse button, it
	 * stops moving the component, and unregisters itself as a
	 * listener.
	 */
	public static class FloatingContainerPaneListener extends  MouseInputAdapter {
	    
	    /**
	     * The max distance from the side the mouse can be to start an auto-scroll
	     */
	    private static final int AUTO_SCROLL_INSET = 20; 
	 
	    // no of units to be scrolled in each direction 
	    private Insets scrollUnits = new Insets(AUTO_SCROLL_INSET, AUTO_SCROLL_INSET, AUTO_SCROLL_INSET, AUTO_SCROLL_INSET);
	    
		private PlayPen pp;
		private Map<DraggablePlayPenComponent, Point> ppcToHandleMap = new HashMap<DraggablePlayPenComponent, Point>();
		private Map<DraggablePlayPenComponent, Point> ppcToPointMap = new HashMap<DraggablePlayPenComponent, Point>();

		/**
         * Creates a new mouse event handler that tracks mouse motion and moves
         * a container pane around on the play pen accordingly.
         * 
         * @param ppc
         *            The container pane that's going to be moved
         * @param handle
         *            The position relative to the container pane's top left
         *            corner where the mouse pointer should be during the drag
         *            operation. For a single container pane drag, this will
         *            normally be inside the container pane's bounds, but for a
         *            multi-drag, this coordinate will often be a large and/or
         *            negative offset for all but one of the floating objects
         *            (because the user clicked and dragged one of the selected
         *            tables).
         * @param addToPP
         *            A flag indicating whether the floating table has not yet
         *            been added to the playpen (i.e. it should be added when
         *            the user releases the mouse button). This is for "create
         *            table" type actions, and should be set to false for
         *            dragging existing objects.
         */
		public FloatingContainerPaneListener(PlayPen pp, Map<DraggablePlayPenComponent, Point> ppcToHandleMap) {
			this.pp = pp;
			Point pointerLocation = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(pointerLocation,pp);
			logger.debug("Adding floating container pane at:"+ pointerLocation); //$NON-NLS-1$
			
			this.ppcToHandleMap = new HashMap<DraggablePlayPenComponent, Point>(ppcToHandleMap);
			
			for (Entry<DraggablePlayPenComponent, Point> entry : ppcToHandleMap.entrySet()) {
			    DraggablePlayPenComponent ppc = entry.getKey();
			    Point handle = entry.getValue();
			    Point point = new Point(pointerLocation.x - handle.x, pointerLocation.y - handle.y);
			    
			    ppcToPointMap.put(ppc, point);
			}

			pp.addMouseMotionListener(this);
			pp.addMouseListener(this); // the click that ends this operation

			pp.cursorManager.tableDragStarted();
		}

		public void mouseMoved(MouseEvent e) {
			mouseDragged(e);
		}

		public void mouseDragged(MouseEvent e) {
		    for (Entry<DraggablePlayPenComponent, Point> entry : ppcToHandleMap.entrySet()) {
		        DraggablePlayPenComponent ppc = entry.getKey();
		        Point handle = entry.getValue();
		        Point p = ppcToPointMap.get(ppc);
		        
		        pp.zoomPoint(e.getPoint());
		        p.setLocation(e.getPoint().x - handle.x, e.getPoint().y - handle.y);
		        pp.setChildPosition(ppc, p);
		        JViewport viewport = (JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, pp);
		        if(viewport==null || pp.getSelectedItems().size() < 1) 
		            return; 

		        // Theoretically should re-validate after each scroll. But that would 
		        // cause the selected component to fall off the border.
		        pp.revalidate();
		        Point viewPos = viewport.getViewPosition(); 
		        Rectangle view = viewport.getViewRect();
		        int viewHeight = viewport.getHeight(); 
		        int viewWidth = viewport.getWidth(); 

		        // performs scrolling
		        Rectangle bounds = pp.zoomRect(ppc.getBounds());
		        if ((p.y - viewPos.y) < scrollUnits.top && viewPos.y > 0) { // scroll up
		            view.y = bounds.y - scrollUnits.top;
		        } if ((viewPos.y + viewHeight - p.y - bounds.height) < scrollUnits.bottom) { // scroll down 
		            view.y = bounds.y + bounds.height - viewHeight + scrollUnits.bottom;
		        } if ((p.x - viewPos.x) < scrollUnits.left && viewPos.x > 0) { // scroll left 
		            view.x = bounds.x - scrollUnits.left;
		        } if ((viewPos.x + viewWidth - p.x - bounds.width) < scrollUnits.right) { // scroll right 
		            view.x = bounds.x + bounds.width - viewWidth + scrollUnits.right;
		        }
		        logger.debug(viewport.getViewPosition());
		        pp.scrollRectToVisible(view);
		        // Necessary to stop tables from flashing.
		        if (ppc != null) {
		            ppc.repaint();
		        }
		    }
		}

		/**
		 * Anchors the tablepane and disposes this listener instance.
		 */
		public void mouseReleased(MouseEvent e) {
			cleanup();
			// normalize changes to table panes are part
			// of this compound edit, refer to bug 1592.
			pp.normalize();
			pp.revalidate();
			
			for (DraggablePlayPenComponent ppc : ppcToHandleMap.keySet()) {
			    if (ppc.isBeingDragged()) {
			        ppc.doneDragging();
			    }
			}
		}

		protected void cleanup() {
		    pp.cursorManager.placeModeFinished();
		    pp.cursorManager.tableDragFinished();
		    pp.removeMouseMotionListener(this);
		    pp.removeMouseListener(this);
		}
	}

	// -------------- Bring to Front / Send To Back ------------------
	public static class BringToFrontAction extends AbstractAction {

		protected PlayPen pp;

		public BringToFrontAction(PlayPen pp) {
			super(Messages.getString("PlayPen.bringToFrontActionName")); //$NON-NLS-1$
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
		    pp.getContentPane().begin("Bringing playpen components to front.");
		    
		    LinkedHashSet<PlayPenComponent> independentComponents = new LinkedHashSet<PlayPenComponent>();
		    LinkedHashSet<PlayPenComponent> dependentComponents = new LinkedHashSet<PlayPenComponent>();
		    
			for (PlayPenComponent c : pp.getSelectedItems()) {
			    if (PlayPenContentPane.isDependentComponentType(c.getClass())) {
			        if (dependentComponents.add(c)) {
			            try {
			                pp.getContentPane().removeChild(c);
			            } catch (ObjectDependentException ex) {
			                throw new RuntimeException(ex);
			            }
			        }
			    } else {
			        independentComponents.add(c);
			    }
			    
				if (c instanceof TablePane) {
				    for (Relationship r : pp.getContentPane().getChildren(Relationship.class)) {
				        if (c == r.getFkTable() || c == r.getPkTable()) {
				            if (dependentComponents.add(r)) {
				                try {
				                    pp.getContentPane().removeChild(r);
				                } catch (ObjectDependentException ex) {
				                    throw new RuntimeException(ex);
				                }
				            }
				        }
				    }
				}
			}
			
			for (PlayPenComponent c : independentComponents) {
                try {
                    pp.getContentPane().removeChild(c);
                } catch (ObjectDependentException ex) {
                    throw new RuntimeException(ex);
                }
			}
			
			for (PlayPenComponent c : independentComponents) {
			    pp.getContentPane().addChild(c, 0);
			}
			for (PlayPenComponent c : dependentComponents) {
			    pp.getContentPane().addChild(
			            c, pp.getContentPane().getFirstDependentComponentIndex());
			}
			
			pp.repaint();
			pp.getContentPane().commit();
		}
	}

	public static class SendToBackAction extends AbstractAction {

		protected PlayPen pp;

		public SendToBackAction(PlayPen pp) {
			super(Messages.getString("PlayPen.sendToBackActionName")); //$NON-NLS-1$
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
		    pp.getContentPane().begin("Sending playpen components to back");
		    
		    LinkedHashSet<PlayPenComponent> independentComponents = new LinkedHashSet<PlayPenComponent>();
		    LinkedHashSet<PlayPenComponent> dependentComponents = new LinkedHashSet<PlayPenComponent>();
		    
		    for (PlayPenComponent c : pp.getSelectedItems()) {
		        
		        if (PlayPenContentPane.isDependentComponentType(c.getClass())) {
		            if (dependentComponents.add(c)) {
		                try {
		                    pp.getContentPane().removeChild(c);
		                } catch (ObjectDependentException ex) {
		                    throw new RuntimeException(ex);
		                }
		            }
		            
		        } else {
		            independentComponents.add(c);
		        }
		        
                if (c instanceof TablePane) {
                    for (Relationship r : pp.getContentPane().getChildren(Relationship.class)) {
                        if (c == r.getFkTable() || c == r.getPkTable()) {
                            if (dependentComponents.add(r)) {
                                try {
                                    pp.getContentPane().removeChild(r);
                                } catch (ObjectDependentException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    }
                }
			}
		    
		    for (PlayPenComponent c : independentComponents) {
                try {
                    pp.getContentPane().removeChild(c);
                } catch (ObjectDependentException ex) {
                    throw new RuntimeException(ex);
                }
		    }
		    
		    for (PlayPenComponent c : independentComponents) {
		        pp.getContentPane().addChild(
		                c, pp.getContentPane().getFirstDependentComponentIndex());
		    }
		    for (PlayPenComponent c : dependentComponents) {
                pp.getContentPane().addChild(
                        c, pp.getContentPane().getChildren().size());
		    }
		    
			pp.repaint();
			pp.getContentPane().commit();
		}
	}

    /**
     * @return The font render context at the current zoom setting
     *         or the font render context defined by the setter.
     */
    public FontRenderContext getFontRenderContext() {
        
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        FontRenderContext frc = null;
        if (g2 != null) {
            g2.scale(zoom, zoom);
            frc = g2.getFontRenderContext();
            g2.dispose();
        } else {
            frc = fontRenderContext;
        }
        if (logger.isDebugEnabled()) logger.debug("Returning frc="+frc); //$NON-NLS-1$
        return frc;
    }
    
    public void setFontRenderContext(FontRenderContext frc) {
        fontRenderContext = frc;
    }

    /**
     * Selects the playpen component that represents the given SQLObject.
     * If the given SQL Object isn't in the playpen, this method has no effect.
     *
     * @param selection A list of SQLObjects, should only have SQLColumn, SQLTable or SQLRelationship.
     * @throws SQLObjectException 
     */
    public void selectObjects(List<? extends SPObject> selections) throws SQLObjectException {
        if (ignoreTreeSelection) return;
        ignoreTreeSelection = true;

        logger.debug("selecting: " + selections); //$NON-NLS-1$
        DBTree tree = session.getDBTree();
        
        // tables to add to select because of column selection 
        List<SPObject> colTables = new ArrayList<SPObject>();
        
        // objects that were already selected, only used for debugging
        List<SPObject> ignoredObjs = new ArrayList<SPObject>();

        for (SPObject obj : selections) {
            if (obj instanceof SQLColumn){
                //Since we cannot directly select a SQLColumn directly
                //from the playpen, there is a special case for it
                SQLColumn col = (SQLColumn) obj;
                SQLTable table = col.getParent();
                TablePane tablePane = findTablePane(table);
                if (tablePane != null) {
                    colTables.add(table);
                    // select the parent table
                    if (!tablePane.isSelected()) {
                        tablePane.setSelected(true,SelectionEvent.SINGLE_SELECT);
                    }
                    
                    // ensures the table is selected on the dbTree
                    TreePath tp = tree.getTreePathForNode(table);
                    if (!tree.isPathSelected(tp)) {
                        tree.addSelectionPath(tp);
                        tree.clearNonPlayPenSelections();
                        
                        // ensures column tree path is selected after the table
                        TreePath colPath = tree.getTreePathForNode(col);
                        tree.removeSelectionPath(colPath);
                        tree.addSelectionPath(colPath);
                    }
                    
                    // finally select the actual column
                    int index = table.getColumnIndex(col);
                    if (!tablePane.isItemSelected(index)) {
                        tablePane.selectItem(index);
                    } else {
                        ignoredObjs.add(col);
                    }
                } else {
                    ignoredObjs.add(col);
                    ignoredObjs.add(table);
                }
            } else if (obj instanceof SQLTable) {
                TablePane tp = findTablePane((SQLTable) obj);
                if (tp != null && !tp.isSelected()) {
                    tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
                } else {
                    ignoredObjs.add(obj);
                }
            } else if (obj instanceof SQLRelationship) {
                Relationship r = findRelationship((SQLRelationship) obj);
                if (r != null && !r.isSelected()) {
                    r.setSelected(true,SelectionEvent.SINGLE_SELECT);
                } else {
                    ignoredObjs.add(obj);
                }
                tree.clearNonPlayPenSelections();
            }
        }
        
        logger.debug("selectObjects ignoring: " + ignoredObjs); //$NON-NLS-1$
        logger.debug("selectObjects adding tables selections: " + colTables); //$NON-NLS-1$
        
        // deselects all other playpen components
        for (PlayPenComponent comp : getSelectedItems()) {
            if (comp instanceof TablePane) {
                TablePane tablePane = (TablePane) comp;
                if (!selections.contains(tablePane.getModel()) && !colTables.contains(tablePane.getModel())) {
                    tablePane.setSelected(false, SelectionEvent.SINGLE_SELECT);
                }
                
                // cannot deselect columns while going through the selected columns
                List<Integer> indices = new ArrayList<Integer>();
                for (SQLColumn col : tablePane.getSelectedItems()) {
                    if (!selections.contains(col) && col.getParent() != null) {
                        indices.add(col.getParent().getColumnIndex(col));
                    }
                }
                for (int index : indices) {
                    tablePane.deselectItem(index);
                }
            } else if (comp instanceof Relationship) {
                Relationship relationship = (Relationship) comp;
                if (!selections.contains(relationship.getModel())) {
                    relationship.setSelected(false, SelectionEvent.SINGLE_SELECT);
                }
            }
            
        }
        ignoreTreeSelection = false;
    }
    
    /**
     * Selects the playpen component that represents the given OLAPObjects.
     * If the given OLAPObjects aren't in the playpen, this method has no effect.
     *
     * @param selection A list of OLAPObjects.
     * @throws SQLObjectException 
     */
    public void selectObjects(List<OLAPObject> selections, OLAPTree tree) throws SQLObjectException {
        if (ignoreTreeSelection) return;
        ignoreTreeSelection = true;
        logger.debug("selecting: " + selections); //$NON-NLS-1$

        // Parent objects to select because a child object was selected.
        List<OLAPObject> extraSelections = new ArrayList<OLAPObject>();

        // Objects that were already selected, only used for debugging.
        List<OLAPObject> ignoredObjs = new ArrayList<OLAPObject>();

        for (OLAPObject obj : selections) {
            if (obj instanceof Cube) {
                selectCube((Cube) obj, ignoredObjs);
            } else if (obj instanceof VirtualCube) {
                selectVirtualCube((VirtualCube) obj, ignoredObjs);
            } else if (obj instanceof MondrianModel.Dimension || obj instanceof DimensionUsage) {
                selectDimension(obj, ignoredObjs, extraSelections, tree);
            } else if (obj instanceof Measure) {
                selectMeasure((Measure) obj, ignoredObjs, extraSelections, tree);
            } else if (obj instanceof VirtualCubeDimension || obj instanceof VirtualCubeMeasure) {
                selectItemFromVirtualCube(obj, ignoredObjs, extraSelections, tree);
            } else if (obj instanceof CubeUsage) {
                selectCubeUsage((CubeUsage) obj, ignoredObjs, extraSelections, tree);
            } else if (obj instanceof Hierarchy && obj.getParent() instanceof MondrianModel.Dimension
                    && obj.getParent().getParent() instanceof Schema) {
                // Only select hierarchies from the public dimensions because the ones inside
                // a cube do not show hierarchies on the playPen!
                selectHierarchy((Hierarchy) obj, ignoredObjs, extraSelections, tree);
            } else if (obj instanceof Level && obj.getParent() instanceof Hierarchy 
                    && obj.getParent().getParent() instanceof MondrianModel.Dimension
                    && obj.getParent().getParent().getParent() instanceof Schema) {
                // Only select levels from the public dimensions because the ones inside
                // a cube do not show levels on the playPen!
                selectLevel((Level) obj, ignoredObjs, extraSelections, tree);
            }

            logger.debug("selectObjects ignoring: " + ignoredObjs); //$NON-NLS-1$
            logger.debug("selectObjects adding tables selections: " + extraSelections); //$NON-NLS-1$

            // Deselects all other playpen components.
            for (PlayPenComponent comp : getSelectedItems()) {
                if (comp instanceof CubePane) {
                    CubePane cp = (CubePane) comp;
                    if (!selections.contains(cp.getModel()) && !extraSelections.contains(cp.getModel())) {
                        cp.setSelected(false, SelectionEvent.SINGLE_SELECT);
                    }

                    // Cannot deselect Objects while going through the selected items.
                    List<OLAPObject> oos = new ArrayList<OLAPObject>();

                    for (OLAPObject oo : cp.getSelectedItems()) {
                        if (!selections.contains(oo) && !extraSelections.contains(oo)) {
                            oos.add(oo);
                        }
                    }
                    for (OLAPObject oo : oos) {
                        cp.deselectItem(oo);
                    }
                } else if (comp instanceof VirtualCubePane) {
                    VirtualCubePane vcp = (VirtualCubePane) comp;
                    if (!selections.contains(vcp.getModel()) && !extraSelections.contains(vcp.getModel())) {
                        vcp.setSelected(false, SelectionEvent.SINGLE_SELECT);
                    }

                    // Cannot deselect Objects while going through the selected items.
                    List<OLAPObject> oos = new ArrayList<OLAPObject>();

                    for (OLAPObject oo : vcp.getSelectedItems()) {
                        if (!selections.contains(oo) && !extraSelections.contains(oo)) {
                            oos.add(oo);
                        }
                    }
                    for (OLAPObject oo : oos) {
                        vcp.deselectItem(oo);
                    }
                } else if (comp instanceof DimensionPane) {
                    DimensionPane dp = (DimensionPane) comp;
                    if (!selections.contains(dp.getModel()) && !extraSelections.contains(dp.getModel())) {
                        dp.setSelected(false, SelectionEvent.SINGLE_SELECT);
                    }

                    // Cannot deselect Objects while going through the selected items.
                    List<OLAPObject> oos = new ArrayList<OLAPObject>();

                    for (OLAPObject oo : dp.getSelectedItems()) {
                        if (!selections.contains(oo) && !extraSelections.contains(oo)) {
                            oos.add(oo);
                        }
                    }
                    
                    // Get the hierarchies from the sections and add them.
                    for (PaneSection<? extends OLAPObject> hs : dp.getSelectedSections()) {
                        if (hs instanceof HierarchySection) {
                            if (!selections.contains(((HierarchySection) hs).getHierarchy()) 
                                    && !extraSelections.contains(((HierarchySection) hs).getHierarchy())) {
                                oos.add(((HierarchySection) hs).getHierarchy());
                            }
                        }
                    }
                    for (OLAPObject oo : oos) {
                        if (oo instanceof Level) {
                            dp.deselectItem((Level) oo);
                        } else if (oo instanceof Hierarchy) {
                            dp.deselectSection(dp.findSection((Hierarchy) oo));
                        }
                    }
                } else if (comp instanceof UsageComponent) {
                    UsageComponent uc = (UsageComponent) comp;
                    if (!selections.contains(uc.getModel()) && !extraSelections.contains(uc.getModel())) {
                        uc.setSelected(false, SelectionEvent.SINGLE_SELECT);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown PlayPenComponent type " + comp.getClass() + "!");
                }

            }
        }
        ignoreTreeSelection = false;
    }
    
    /**
     * Uses the given CubeUsage to select the matching CubeUsage and
     * VirtualCubePane on the PlayPen. Also ensures the OLAPTree also selects
     * the cubeUsage and the virtualCube.
     * 
     * @param obj
     *            The CubeUsage to be selected in the playPen.
     * @param ignoredObjs
     *            A list of ingored objects used for debugging.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @return The VirtualCubePane that was selected or null if none was
     *         selected.
     */
    private VirtualCubePane selectCubeUsage(CubeUsage obj, List<OLAPObject> ignoredObjs, List<OLAPObject> extraSelections,
            OLAPTree tree) {
        if (obj.getParent() instanceof CubeUsages) {
            
            if (obj.getParent().getParent() instanceof VirtualCube) {
                VirtualCubePane vcp = selectVirtualCube((VirtualCube) obj.getParent().getParent(), ignoredObjs);
                if (vcp != null) {
                    selectParents(obj, vcp.getModel(), tree, extraSelections);
                    vcp.selectItem(obj);
                }
                return vcp;
            } else { 
                throw new IllegalStateException("Parent type " + obj.getParent().getParent().getClass() 
                        + " is not a valid parent for type " + obj.getParent().getClass() + "!");
            }
        } else {
            throw new IllegalStateException("Parent type " + obj.getParent().getClass() 
                    + " is not a valid parent for type " + obj.getClass() + "!");
        }
    }

    /**
     * Uses the given cube to select the matching CubePane on the PlayPen.
     * 
     * @param cube The Cube whose pane is to be selected.
     * @param ignoredObjs A list of ingored objects used for debugging.
     * @return The CubePane that was selected or null if none was selected.
     */
    private CubePane selectCube(Cube cube, List<OLAPObject> ignoredObjs) {
        CubePane cp = (CubePane)findPPComponent(cube);
        if (cp != null && !cp.isSelected()) {
            cp.setSelected(true, SelectionEvent.SINGLE_SELECT);
        } else {
            ignoredObjs.add(cube);
        }
        return cp;
    }
    
    /**
     * Uses the given virtualCube to select the matching VirtualCubePane on the PlayPen.
     * 
     * @param vCube The VirtualCube whose pane is to be selected.
     * @param ignoredObjs A list of ingored objects used for debugging.
     * @return The VirtualCubePane that was selected or null if none was selected.
     */
    private VirtualCubePane selectVirtualCube(VirtualCube vCube, List<OLAPObject> ignoredObjs) {
        VirtualCubePane vcp = (VirtualCubePane)findPPComponent(vCube);
        if (vcp != null && !vcp.isSelected()) {
            vcp.setSelected(true, SelectionEvent.SINGLE_SELECT);
        } else {
            ignoredObjs.add(vCube);
        }
        return vcp;
    }
    
    /**
     * Uses the given OLAPObject (which has to be Dimension or DimnesionUsage) to
     * select the matching CubePane or DimensionPane on the PlayPen. Also
     * ensures the OLAPTree also selects the dimension and it's parent if the
     * dimension's parent is a cube and not the schema.
     * 
     * @param obj
     *            The Dimension or DimensionUsage whose pane is to be selected.
     * @param ignoredObjs
     *            A list of ingored objects used for debugging.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @return The OLAPPane that was selected or null if none was selected.
     */
    private OLAPPane<?, ?> selectDimension(OLAPObject obj, List<OLAPObject> ignoredObjs, List<OLAPObject> extraSelections, OLAPTree tree) {
        if (obj.getParent() instanceof Cube) {
            CubePane cp = selectCube((Cube) obj.getParent(), ignoredObjs);
            if (cp != null) {
                selectParents(obj, cp.getModel(), tree, extraSelections);
                cp.selectItem(obj);
            }
            return cp;
        } else if (obj.getParent() instanceof Schema) {
            DimensionPane dp = (DimensionPane)findPPComponent(obj);
            if (dp != null && !dp.isSelected()) {
                dp.setSelected(true, SelectionEvent.SINGLE_SELECT);
            } else {
                ignoredObjs.add(obj);
            }
            return dp;
        } else {
            throw new IllegalStateException("Parent type " + obj.getParent().getClass() 
                    + " is not a valid parent for type " + obj.getClass() + "!");
        }
    }
    
    /**
     * Uses the given Measure to select the matching Measure and CubePane on the
     * PlayPen. Also ensures the OLAPTree also selects the measure and the cube.
     * 
     * @param measure
     *            The measure to be selected in the playPen.
     * @param ignoredObjs
     *            A list of ingored objects used for debugging.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @return The CubePane that was selected or null if none was selected.
     */
    private CubePane selectMeasure(Measure measure, List<OLAPObject> ignoredObjs, List<OLAPObject> extraSelections, OLAPTree tree) {
        if (measure.getParent() instanceof Cube) {
            CubePane cp = selectCube((Cube) measure.getParent(), ignoredObjs);
            if (cp != null) {
                selectParents(measure, cp.getModel(), tree, extraSelections);
                cp.selectItem(measure);
            }
            return cp;
        } else {
            throw new IllegalStateException("Parent type " + measure.getParent().getClass() 
                    + " is not a valid parent for type " + measure.getClass() + "!");
        }
    }
    
    /**
     * Uses the given VirtualCubeMeasure or VirtualCubeDimension to select the
     * matching Object and VirtualCubePane on the PlayPen. Also ensures the
     * OLAPTree also selects the object and the virtualCube.
     * 
     * @param obj
     *            The VirtualCubeMeasure or VirtualCubeDimension to be selected in
     *            the playPen.
     * @param ignoredObjs
     *            A list of ingored objects used for debugging.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @return The VirtualCubePane that was selected or null if none was
     *         selected.
     */
    private VirtualCubePane selectItemFromVirtualCube(OLAPObject obj, List<OLAPObject> ignoredObjs, List<OLAPObject> extraSelections, OLAPTree tree) {
        if (obj.getParent() instanceof VirtualCube) {
            VirtualCubePane vcp = selectVirtualCube((VirtualCube) obj.getParent(), ignoredObjs);
            if (vcp != null) {
                selectParents(obj, vcp.getModel(), tree, extraSelections);
                vcp.selectItem(obj);
            }
            return vcp;
        } else {
            throw new IllegalStateException("Parent type " + obj.getParent().getClass() 
                    + " is not a valid parent for type " + obj.getClass() + "!");
        }
    }
    
    /**
     * Uses the given Hierarchy to select the matching Hierarchy and it's
     * DimensionPane on the PlayPen. Also ensures the OLAPTree also selects the
     * hierarchy and the dimension.
     * 
     * @param hierarchy
     *            The hierarchy to be selected in the playPen.
     * @param ignoredObjs
     *            A list of ingored objects used for debugging.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @return The DimensionPane that was selected or null if none was selected.
     */
    private DimensionPane selectHierarchy(Hierarchy hierarchy, List<OLAPObject> ignoredObjs, List<OLAPObject> extraSelections, OLAPTree tree) {
        if (hierarchy.getParent() instanceof MondrianModel.Dimension) {
            DimensionPane dp = (DimensionPane)selectDimension((OLAPObject) hierarchy.getParent(), ignoredObjs, extraSelections, tree);
            if (dp != null) {
                selectParents(hierarchy, dp.getModel(), tree, extraSelections);
                dp.selectSection(dp.findSection((Hierarchy) hierarchy));
            } else {
                throw new NullPointerException("OLAPPane that contains " + hierarchy.getClass() + " not found.");
            }
            return dp;
        } else {
            throw new IllegalStateException("Parent type " + hierarchy.getParent().getClass() 
                    + " is not a valid parent for type " + hierarchy.getClass() + "!");
        }
    }
    
    /**
     * Uses the given Level to select the matching Level and DimensionPane on
     * the PlayPen. Also ensures the OLAPTree also selects the level and the
     * dimension.
     * 
     * @param level
     *            The level to be selected in the playPen.
     * @param ignoredObjs
     *            A list of ingored objects used for debugging.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @return The DimensionPane that was selected or null if none was selected.
     */
    private DimensionPane selectLevel(Level level, List<OLAPObject> ignoredObjs, List<OLAPObject> extraSelections, OLAPTree tree) {
        if (level.getParent() instanceof Hierarchy) {
            DimensionPane dp = (DimensionPane)selectDimension((OLAPObject) level.getParent().getParent(), ignoredObjs, extraSelections, tree);
            if (dp != null) {
                selectParents(level, dp.getModel(), tree, extraSelections);
                dp.selectItem((Level) level);
            } else {
                throw new NullPointerException("OLAPPane that contains " + level.getClass() + " not found.");
            }
            return dp;
        } else {
            throw new IllegalStateException("Parent type " + level.getParent().getClass() 
                    + " is not a valid parent for type " + level.getClass() + "!");
        }
    }
    
    /**
     * Uses the given OLAPObjects and selects them on the given OLAPTree.
     * 
     * @param obj
     *            The object to be selected on the Tree.
     * @param parent
     *            The object's parent to be selected on the Tree.
     * @param tree
     *            The OLAPTree assoicated with this PlayPen.
     * @param extraSelections
     *            A list of items that are selected, but not directly from the
     *            user.
     */
    private void selectParents(OLAPObject obj, OLAPObject parent, OLAPTree tree, List<OLAPObject> extraSelections) {
        // ensures the table is selected on the dbTree
        TreePath tp = tree.getTreePathForNode(parent);
        if (!tree.isPathSelected(tp)) {
            tree.addSelectionPath(tp);
            tree.clearNonPlayPenSelections();

            // ensures column tree path is selected after the table
            TreePath childPath = tree.getTreePathForNode(obj);
            tree.removeSelectionPath(childPath);
            tree.addSelectionPath(childPath);
        }
        extraSelections.add(parent);
    }

	public void setMouseMode(MouseModeType mouseMode) {
		this.mouseMode = mouseMode;
	}
	
    public MouseModeType getMouseMode() {
        return mouseMode;
    }

    public ArchitectSwingSession getSession() {
        return session;
    }
    
    public CursorManager getCursorManager() {
        return cursorManager;
    }

    public PopupMenuFactory getPopupFactory() {
        return popupFactory;
    }

    public void setPopupFactory(PopupMenuFactory popupFactory) {
        this.popupFactory = popupFactory;
    }

    public void setIgnoreTreeSelection(boolean value) {
        this.ignoreTreeSelection = value;
    }

    public boolean ignoreTreeSelection() {
        return ignoreTreeSelection;
    }
    

    /**
     * Scrolls the playpen to show the selected objects. The most left component
     * takes precedence if multiple objects are selected.
     * 
     */
    public void showSelected() {
        Rectangle r = null;
        Point minX = null;
        Point minY = null;
        
        for (PlayPenComponent comp : getSelectedItems()) {
            if (r == null) {
                // first component
                r = comp.getBounds();
                minX = comp.getLocation();
                minY = comp.getLocation();
            } else {
                r.add(comp.getBounds());
                if (comp.getX() < minX.getX()) {
                    minX = comp.getLocation();
                }
                if (comp.getY() < minY.getY()) {
                    minY = comp.getLocation();
                }
            }
        }
        
        Rectangle visibleRect = unzoomRect(this.getVisibleRect());
        // adjustments for when visible size is too small
        if (r.getHeight() > visibleRect.height) {
            r.setLocation(minY.getLocation());
            r.setSize(r.width, visibleRect.height);
        }
        if (r.getWidth() > visibleRect.width) {
            r.setLocation(minX.getLocation());
            r.setSize(visibleRect.width, r.height);
        }
        
        this.scrollRectToVisible(zoomRect(r));
    }
    
    public void updateTablePanes() {
        for (TablePane tp : contentPane.getChildren(TablePane.class)) {
            tp.updateHiddenColumns();
            tp.updateNameDisplay();
            tp.revalidate();
            tp.repaint();
        }
    }
    
    // PlayPen Lifecycle Event
    
    private final List<PlayPenLifecycleListener> lifecycleListeners = new ArrayList<PlayPenLifecycleListener>();
    
    public void addPlayPenLifecycleListener(PlayPenLifecycleListener ppll) {
        lifecycleListeners.add(ppll);
    }
    
    private void firePlayPenLifecycleEvent() {
        PlayPenLifecycleEvent evt = new PlayPenLifecycleEvent(this);
        for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
            lifecycleListeners.get(i).PlayPenLifeEnding(evt);
        }
    }
    
}
