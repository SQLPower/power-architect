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
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLExceptionNode;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.action.CancelAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;


/**
 * The PlayPen is the main GUI component of the SQL*Power Architect.
 */
public class PlayPen extends JPanel
	implements java.io.Serializable, SQLObjectListener, SelectionListener, Scrollable {

    public interface CancelableListener {

		public void cancel();

	}

	private static Logger logger = Logger.getLogger(PlayPen.class);

	public enum MouseModeType {IDLE,
						CREATING_TABLE,
						CREATING_RELATIONSHIP,
						SELECT_TABLE,
						SELECT_RELATIONSHIP,
						SELECT_COLUMN,
						MULTI_SELECT,
						RUBBERBAND_MOVE}
	private MouseModeType mouseMode = MouseModeType.IDLE;

    /**
     * A simple class that encapsulates the logic for making the cursor image
     * look correct for the current activity.
     */
    public class CursorManager {
        
        private boolean draggingTable = false;
        private boolean dragAllModeActive = false;
        private boolean placeModeActive = false;
        
        public void tableDragStarted() {
            draggingTable = true;
            modifyCursorImage();
        }
        
        public void tableDragFinished() {
            draggingTable = false;
            modifyCursorImage();
        }
        
        public void dragAllModeStarted() {
            dragAllModeActive = true;
            modifyCursorImage();
        }
        
        public void dragAllModeFinished() {
            dragAllModeActive = false;
            modifyCursorImage();
        }

        public void placeModeStarted() {
            placeModeActive = true;
            modifyCursorImage();
        }

        public void placeModeFinished() {
            placeModeActive = false;
            modifyCursorImage();
        }
        
        /**
         * Sets the appropriate cursor type based on the current
         * state of this cursor manager.
         */
        private void modifyCursorImage() {
            if (dragAllModeActive || draggingTable) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else if (placeModeActive) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else {
                setCursor(null);
            }
        }
    }
    
    /**
     * The cursor manager for this play pen.
     */
    private final CursorManager cursorManager = new CursorManager();
    
	/**
	 * Links this PlayPen with an instance of PlayPenDropListener so
	 * users can drop stuff on the playpen.
	 */
	protected DropTarget dt;
	
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
	 * This dialog box is for editting the PlayPen's DB Connection spec.
	 */
	protected JDialog dbcsDialog;

	/**
     * used by mouseReleased to figure out if a DND operation just took place in the
     * playpen, so it can make a good choice about leaving a group of things selected
     * or deselecting everything except the TablePane that was clicked on.
     */
	protected boolean draggingTablePanes = false;


	/**
	 * A RenderingHints value of VALUE_ANTIALIAS_ON, VALUE_ANTIALIAS_OFF, or VALUE_ANTIALIAS_DEFAULT.
	 */
    private Object antialiasSetting = RenderingHints.VALUE_ANTIALIAS_DEFAULT;

	/**
	 * A graveyard for components that used to be associated with model
	 * components that are no longer in the model.  If the model components
	 * come back from the dead (thanks the the UndoManager), then the
	 * corresponding PlayPenComonent can be revived from this map.
	 *
	 * Allows the garbage collecter to clean up any components not in the undo manager
	 *
	 */
    private Map<SQLObject,PlayPenComponent> removedComponents = new WeakHashMap<SQLObject, PlayPenComponent>();

    /**
     * Tells whether or not this component will paint its contents.  This was
     * originally added to test the speed of the SpringLayout when it doesn't
     * have to repaint everything for every frame.  It might be useful for
     * other stuff later on too.
     */
    private boolean paintingEnabled = true;

	private TablePaneDragGestureListener dgl;
	private DragSource ds;

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
	
	/**
     * Creates a play pen with reasonable defaults.  If you are creating
     * this PlayPen for temporary use (as opposed to creating a session's
     * main PlayPen), don't forget to call {@link #destroy()} when you are
     * done with it.
     * 
     * @param session
     *            The session this play pen belongs to. Null is not allowed.
     */
	public PlayPen(ArchitectSwingSession session) {
        if (session == null) throw new NullPointerException("A null session is not allowed here."); //$NON-NLS-1$
		this.session = session;
		setDatabase(session.getTargetDatabase());
        zoom = 1.0;
        viewportPosition = new Point(0, 0);
		setBackground(java.awt.Color.white);
		contentPane = new PlayPenContentPane(this);
		setName("Play Pen"); //$NON-NLS-1$
		setMinimumSize(new Dimension(1,1));
		dt = new DropTarget(this, new PlayPenDropListener());
		bringToFrontAction = new BringToFrontAction(this);
		sendToBackAction = new SendToBackAction(this);
		ppMouseListener = new PPMouseListener();
		addMouseListener(ppMouseListener);
		addMouseMotionListener(ppMouseListener);
		addMouseWheelListener(ppMouseListener);

		dgl = new TablePaneDragGestureListener();
		ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dgl);
		logger.debug("DragGestureRecognizer motion threshold: " + getToolkit().getDesktopProperty("DnD.gestureMotionThreshold")); //$NON-NLS-1$ //$NON-NLS-2$
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

		this.antialiasSetting = pp.antialiasSetting;
		
		setFont(pp.getFont());
		this.setForeground(pp.getForeground());
		this.setBackground(pp.getBackground());
		
		for (int i = 0; i < pp.getContentPane().getComponentCount(); i++) {
			PlayPenComponent ppc = pp.getContentPane().getComponent(i);
			if (ppc instanceof TablePane) {
				TablePane tp = (TablePane) ppc;
				addImpl(new TablePane(tp, contentPane), ppc.getPreferredLocation(), contentPane.getComponentCount());
			}
		}

		for (int i = 0; i < pp.getContentPane().getComponentCount(); i++) {
			PlayPenComponent ppc = pp.getContentPane().getComponent(i);
			if (ppc instanceof Relationship) {
				Relationship rel = (Relationship) ppc;
				addImpl(new Relationship(rel, contentPane), ppc.getPreferredLocation(), contentPane.getComponentCount());
			}
		}
		setSize(getPreferredSize());
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
        try {
            removeHierarcyListeners(session.getTargetDatabase());
        } catch (ArchitectException ex) {
            logger.error("Couldn't unlisten this playpen from the database", ex); //$NON-NLS-1$
        }
    }

    /**
     * Returns a new list of all tables in this play pen. The list returned will
     * be your own private (shallow) copy, so you are free to modify it.
     */
    public List<SQLTable> getTables() throws ArchitectException {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        ArchitectUtils.extractTables(session.getTargetDatabase(),tables);
        return tables;

    }

	private final void setDatabase(SQLDatabase newdb) {
		if (newdb == null) throw new NullPointerException("db must be non-null"); //$NON-NLS-1$
		
		// Note, this also happens in CoreProject, but that's only helpful when loading a project file
		// And you get fireworks if you call setDataSource() on a non-playpen connection
		newdb.setPlayPenDatabase(true);

		SPDataSource dbcs = new SPDataSource(session.getContext().getPlDotIni());
        dbcs.setName(Messages.getString("PlayPen.notConfiguredDbcsName")); //$NON-NLS-1$
        dbcs.setDisplayName(Messages.getString("PlayPen.notConfiguredDbcsName")); //$NON-NLS-1$
        newdb.setDataSource(dbcs);

		try {
			ArchitectUtils.listenToHierarchy(this, newdb);
		} catch (ArchitectException ex) {
			logger.error("Couldn't listen to database", ex); //$NON-NLS-1$
		}
		tableNames = new HashSet<String>();
	}

    protected void setDatabaseConnection(SPDataSource dbcs){
        SPDataSource tSpec = session.getTargetDatabase().getDataSource();
        tSpec.setDisplayName(dbcs.getDisplayName());
        tSpec.getParentType().setJdbcDriver(dbcs.getDriverClass());
        tSpec.setUrl(dbcs.getUrl());
        tSpec.setUser(dbcs.getUser());
        tSpec.setPass(dbcs.getPass());
        tSpec.setPlSchema(dbcs.getPlSchema());
        tSpec.setPlDbType(dbcs.getPlDbType());
        tSpec.setOdbcDsn(dbcs.getOdbcDsn());
    }

    void setupKeyboardActions() {
        
        String KEY_DELETE_SELECTED = "ca.sqlpower.architect.swingui.PlayPen.KEY_DELETE_SELECTED"; //$NON-NLS-1$
        ArchitectFrame af = session.getArchitectFrame();

        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE_SELECTED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), KEY_DELETE_SELECTED);
        getActionMap().put(KEY_DELETE_SELECTED, af.getDeleteSelectedAction());
        if (af.getDeleteSelectedAction() == null) logger.warn("af.deleteSelectedAction is null!"); //$NON-NLS-1$

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL"); //$NON-NLS-1$
        getActionMap().put("CANCEL", new CancelAction(this)); //$NON-NLS-1$

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomToFitAction().getValue(Action.ACCELERATOR_KEY), "ZOOM TO FIT"); //$NON-NLS-1$
        getActionMap().put("ZOOM TO FIT", af.getZoomToFitAction()); //$NON-NLS-1$

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomInAction().getValue(Action.ACCELERATOR_KEY), "ZOOM IN"); //$NON-NLS-1$
        getActionMap().put("ZOOM IN", af.getZoomInAction()); //$NON-NLS-1$

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomOutAction().getValue(Action.ACCELERATOR_KEY), "ZOOM OUT"); //$NON-NLS-1$
        getActionMap().put("ZOOM OUT", af.getZoomOutAction()); //$NON-NLS-1$

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomResetAction().getValue(Action.ACCELERATOR_KEY), "ZOOM RESET"); //$NON-NLS-1$
        getActionMap().put("ZOOM RESET", af.getZoomResetAction()); //$NON-NLS-1$

        final Object KEY_SELECT_UPWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_UPWARD"; //$NON-NLS-1$
        final Object KEY_SELECT_DOWNWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_DOWNWARD"; //$NON-NLS-1$

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KEY_SELECT_UPWARD);
        getActionMap().put(KEY_SELECT_UPWARD, new AbstractAction() {
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
                        } catch (ArchitectException ex) {
                            throw new ArchitectRuntimeException(ex);
                        }
                    }
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KEY_SELECT_DOWNWARD);
        getActionMap().put(KEY_SELECT_DOWNWARD, new AbstractAction() {
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
                                } catch (ArchitectException ex) {
                                    throw new ArchitectRuntimeException(ex);
                                }
                            }
                        } catch (ArchitectException e1) {
                            logger.error("Could not get columns of "+ tp.getName(), e1); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        
        addKeyListener(new KeyListener() {

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
	 * Modifies the given point p in model space to apparent position
	 * in screen space.
	 *
	 * @param p The point in model space (the space where the actual
	 * components of the content pane live).  THIS PARAMETER IS MODIFIED.
	 * @return The given point p, which has been modified.
	 */
	public Point zoomPoint(Point p) {
		p.x = (int) ((double) p.x * zoom);
		p.y = (int) ((double) p.y * zoom);
		return p;
	}

	/**
	 * Modifies the given point p from apparent position in screen
	 * space to model space.
	 *
	 * @param p The point in visible screen space (the space where
	 * mouse events are reported).  THIS PARAMETER IS MODIFIED.
	 * @return The given point p, which has been modified.
	 */
	public Point unzoomPoint(Point p) {
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
			firePropertyChange("zoom", oldZoom, newZoom); //$NON-NLS-1$
			revalidate();
			repaint();
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
	    repaint();
	}

	public boolean isRenderingAntialiased() {
	    return antialiasSetting == RenderingHints.VALUE_ANTIALIAS_ON;
	}

	public PlayPenContentPane getContentPane() {
		return contentPane;
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
			logger.debug("minsize is: " + getMinimumSize()); //$NON-NLS-1$
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
		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			PlayPenComponent c = contentPane.getComponent(i);
			cbounds = c.getBounds(cbounds);
			minx = Math.min(cbounds.x, minx);
			miny = Math.min(cbounds.y, miny);
			maxx = Math.max(cbounds.x + cbounds.width , maxx);
			maxy = Math.max(cbounds.y + cbounds.height, maxy);
		}

		return new Dimension((int) ((double) Math.max(maxx - minx, getMinimumSize().width) * zoom),
				(int) ((double) Math.max(maxy - miny, getMinimumSize().height) * zoom));
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
	 * If some playpen components get dragged into a negative range all tables are then shifted
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
		for (TablePane tp : getTablePanes()) {
			minX = Math.min(minX, tp.getX());
			minY = Math.min(minY, tp.getY());
		}

		//Readjusts the table pane, since minX and min <= 0,
		//the adjustments of subtracting minX and/or minY makes sense.
		if ( minX < 0 || minY < 0 ) {
			for (TablePane tp : getTablePanes()) {
				tp.setLocation(tp.getX()-minX, tp.getY()-minY);
			}

			// This function may have expanded the playpen's minimum
			// and preferred sizes, so the original repaint region could be
			// too small!
			repaint();
		}
		normalizing = false;
	}

//	 get the position of the viewport that we are sitting in
	public Point getViewPosition() {
		Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
		if (c != null) {
			JViewport jvp = (JViewport) c;
			Point viewPosition = jvp.getViewPosition();
			logger.debug("view position is: " + viewPosition); //$NON-NLS-1$
			return viewPosition;
		} else {
			return new Point(0, 0);
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
        this.paintingEnabled = paintingEnabled;
    }

    /** See {@link #paintingEnabled}. */
    public boolean isPaintingEnabled() {
        return paintingEnabled;
    }

	public void paintComponent(Graphics g) {
	    if (!paintingEnabled) return;

		logger.debug("start of paintComponent, width="+getWidth()+",height="+getHeight()); //$NON-NLS-1$ //$NON-NLS-2$
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasSetting);

		if (isDebugEnabled()) {
			Rectangle clip = g2.getClipBounds();
			if (clip != null) {
				g2.setColor(Color.green);
				clip.width--;
				clip.height--;
				g2.draw(clip);
				g2.setColor(getBackground());
				logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
			} else {
				logger.debug("Null clipping region"); //$NON-NLS-1$
			}
		}

		Rectangle bounds = new Rectangle();
		AffineTransform backup = g2.getTransform();
		g2.scale(zoom, zoom);
		AffineTransform zoomedOrigin = g2.getTransform();

		// counting down so visual z-order matches click detection z-order
		for (int i = contentPane.getComponentCount() - 1; i >= 0; i--) {
			PlayPenComponent c = contentPane.getComponent(i);
			c.getBounds(bounds);
			if ( g2.hitClip(bounds.x, bounds.y, bounds.width, bounds.height)) {
				if (logger.isDebugEnabled()) logger.debug("Painting visible component "+c); //$NON-NLS-1$
				g2.translate(c.getLocation().x, c.getLocation().y);
				c.paint(g2);
				g2.setTransform(zoomedOrigin);
			} else {
				if (logger.isDebugEnabled()) logger.debug("paint: SKIPPING "+c); //$NON-NLS-1$
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

		logger.debug("end of paintComponent, width="+getWidth()+",height="+getHeight()); //$NON-NLS-1$ //$NON-NLS-2$

	}

	protected void addImpl(Component c, Object constraints, int index) {
		throw new UnsupportedOperationException("You can't add swing component for argument"); //$NON-NLS-1$
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
	protected void addImpl(PlayPenComponent c, Object constraints, int index) {
		if (c instanceof Relationship) {
			contentPane.add(c, contentPane.getFirstRelationIndex());
		} else if (c instanceof ContainerPane) {
			if (constraints instanceof Point) {
				c.setLocation((Point) constraints);
				contentPane.add(c, 0);
			} else {
				throw new IllegalArgumentException("Constraints must be a Point"); //$NON-NLS-1$
			}
			
			if (c instanceof TablePane) {
				// Makes drag and dropped tables show the proper columns
				((TablePane) c).updateHiddenColumns();
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

	public void addRelationship(Relationship r) {
		addImpl(r, null, getPPComponentCount());
	}

    /**
     * This method is primarily for loading project files. Use at your own risk!
     *
     * @param tp
     * @param point
     */
    public void addTablePane(TablePane tp, Point point) {
        addImpl(tp, point, getPPComponentCount());
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
        addImpl(ppc, point, getPPComponentCount());
    }

	/**
	 * Returns 0 because this PlayPen contains no Swing components
	 * directly.
	 *
	 * @deprecated Calling this method from Architect code is almost
	 * certainly a mistake, but it needs to exist for Swing to
	 * function correctly.  You probably want to use
	 * getPPComponentCount instead.
	 */
	public int getComponentCount() {
		return super.getComponentCount();
	}

	/**
	 * Throws IndexOutOfBoundsException becuase the PlayPen contains
	 * no Swing components directly.
	 *
	 * @deprecated Calling this method from Architect code is almost
	 * certainly a mistake, but it needs to exist for Swing to
	 * function correctly.  You probably want to use {@link
	 * #findTablePane}, {@link #getRelationships}, or {@link
	 * #getTablePanes} instead.
	 */
	public Component getComponent(int i) {
		return super.getComponent(i);
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
	 * Searches this PlayPen's children for a TablePane whose model is
	 * t.
	 *
	 * @return A reference to the TablePane that has t as a model, or
	 * null if no such TablePane is in the play pen.
	 */
	public TablePane findTablePane(SQLTable t) {
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			PlayPenComponent c = contentPane.getComponent(i);
			if (c instanceof TablePane
				&& ((TablePane) c).getModel() == t) {
				return (TablePane) c;
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
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			PlayPenComponent c = contentPane.getComponent(i);
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
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			PlayPenComponent c = contentPane.getComponent(i);
			if (c instanceof Relationship
				&& ((Relationship) c).getModel() == r) {
				return (Relationship) c;
			}
		}
		return null;
	}

	/**
	 * Returns a list of the Relationship gui components in this
	 * playpen.
	 */
	public List<Relationship> getRelationships() {
		LinkedList<Relationship> relationships = new LinkedList<Relationship>();
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			if (contentPane.getComponent(i) instanceof Relationship) {
				relationships.add((Relationship) contentPane.getComponent(i));
			}
		}
		return relationships;
	}

	/**
	 * Returns a list of the TablePane components in this playpen.
	 */
	public List<TablePane> getTablePanes() {
		LinkedList<TablePane> tablePanes = new LinkedList<TablePane>();
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			if (contentPane.getComponent(i) instanceof TablePane) {
				tablePanes.add((TablePane) contentPane.getComponent(i));
			}
		}
		return tablePanes;
	}

    /**
     * Returns the already in use table names. Useful for
     * deleting tables so it can be removed from this list as well.
     */
    public Set<String> getTableNames () {
        return tableNames;
    }
    
	/**
	 * Returns the number of components in this PlayPen's
	 * PlayPenContentPane.
	 */
	public int getPPComponentCount() {
		return contentPane.getComponentCount();
	}

	/**
	 * Adds a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 *
	 * @return A reference to the newly-created TablePane.
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addLayoutComponent(Component,Object)
	 */
	public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation) throws ArchitectException {
		SQLTable newTable = SQLTable.getDerivedInstance(source, session.getTargetDatabase()); // adds newTable to db
		String key = source.getName().toLowerCase();
		boolean isAlreadyOnPlaypen = false;
		int newSuffix = 0;

		// ensure tablename is unique
		if (logger.isDebugEnabled()) logger.debug("before add: " + tableNames); //$NON-NLS-1$
		if (!tableNames.add(key)) {
			boolean done = false;
			while (!done) {
				newSuffix++;
				done = tableNames.add(key+"_"+newSuffix); //$NON-NLS-1$
			}
			newTable.setName(source.getName()+"_"+newSuffix); //$NON-NLS-1$
			isAlreadyOnPlaypen = true;
		}
		if (logger.isDebugEnabled()) logger.debug("after add: " + tableNames); //$NON-NLS-1$

		TablePane tp = new TablePane(newTable, getContentPane());
		logger.info("adding table "+newTable); //$NON-NLS-1$
		addImpl(tp, preferredLocation, getPPComponentCount());
		tp.revalidate();

        createRelationshipsFromPP(source, newTable, true, isAlreadyOnPlaypen, newSuffix);
        createRelationshipsFromPP(source, newTable, false, isAlreadyOnPlaypen, newSuffix);
		return tp;
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
     * @throws ArchitectException
     */
    private void createRelationshipsFromPP(SQLTable source, SQLTable newTable, boolean isPrimaryKeyTableNew, boolean isAlreadyOnPlaypen, int suffix) throws ArchitectException {
        // create exported relationships if the importing tables exist in pp
		Iterator sourceKeys = null;
        if (isPrimaryKeyTableNew) {
            sourceKeys = source.getExportedKeys().iterator();
        } else {
            sourceKeys = source.getImportedKeys().iterator();
        }
		while (sourceKeys.hasNext()) {
		    Object next = sourceKeys.next();
		    if ( !(next instanceof SQLRelationship) ) continue;  // there could be SQLExceptionNodes here
			SQLRelationship r = (SQLRelationship) next;
			
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

				SQLRelationship newRel = new SQLRelationship(r);
				
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
				
				addImpl(new Relationship(newRel, getContentPane()),null,getPPComponentCount());

				Iterator mappings = r.getChildren().iterator();
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

    private void setupMapping(SQLTable newTable, SQLTable otherTable, SQLRelationship newRel, SQLRelationship.ColumnMapping m, boolean newTableIsPk) throws ArchitectException {
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
	public synchronized void addObjects(List list, Point preferredLocation, SPSwingWorker nextProcess) throws ArchitectException {
		ProgressMonitor pm
		 = new ProgressMonitor(this,
		                      Messages.getString("PlayPen.copyingObjectsToThePlaypen"), //$NON-NLS-1$
		                      "...", //$NON-NLS-1$
		                      0,
			                  100);
		AddObjectsTask t = new AddObjectsTask(list,
				preferredLocation, pm, session);
		t.setNextProcess(nextProcess);
		new Thread(t, "Objects-Adder").start(); //$NON-NLS-1$
	}

	protected class AddObjectsTask extends MonitorableWorker {
		private List<SQLObject> sqlObjects;
		private Point preferredLocation;
		private boolean hasStarted = false;
		private boolean finished = false;
		private String message = null;
		private int progress = 0;
		private Integer jobSize = null;
		private String errorMessage = null;
		private ProgressMonitor pm;

		public AddObjectsTask(List<SQLObject> sqlObjects,
				Point preferredLocation,
				ProgressMonitor pm,
                ArchitectSwingSession session) {
            super(session);
			this.sqlObjects = sqlObjects;
			this.preferredLocation = preferredLocation;
			finished = false;
			ProgressWatcher.watchProgress(pm, this);
			this.pm = pm;
		}

		public int getProgress() {
			return progress;
		}

		public Integer getJobSize() {
			return jobSize;
		}

		public boolean isFinished() {
			return finished;
		}

		public String getMessage() {
			return message;
		}

		/**
		 * Combines the MonitorableWorker's canceled flag with the
		 * ProgressMonitor's.
		 */
		@Override
		public synchronized boolean isCanceled() {
			return super.isCanceled() || pm.isCanceled();
		}

		/**
		 * Makes sure all the stuff we want to add is populated.
		 */
		public void doStuff () {
			logger.info("AddObjectsTask starting on thread "+Thread.currentThread().getName()); //$NON-NLS-1$

			try {
				hasStarted = true;
				int pmMax = 0;

				Iterator<SQLObject> soIt = sqlObjects.iterator();
				// first pass: figure out how much work we need to do...
				while (soIt.hasNext() && !isCanceled()) {
					pmMax += ArchitectUtils.countTablesSnapshot(soIt.next());
				}
				jobSize = new Integer(pmMax);

				ensurePopulated(sqlObjects);
			} catch (ArchitectException e) {
				logger.error("Unexpected exception during populate", e); //$NON-NLS-1$
                setDoStuffException(e);
				errorMessage = "Unexpected exception during populate: " + e.getMessage(); //$NON-NLS-1$
			} 
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
		private void ensurePopulated(List<SQLObject> soList) {
			for (SQLObject so : soList) {
				if (isCanceled()) break;
				try {
					if (so instanceof SQLTable) progress++;
					ensurePopulated(so.getChildren());
				} catch (ArchitectException e) {
                    errorMessage = "Couldn't get children of " + so; //$NON-NLS-1$
                    setDoStuffException(e);
					logger.error("Couldn't get children of " + so, e); //$NON-NLS-1$
				}
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

			try {

				// reset iterator
				Iterator<SQLObject> soIt = sqlObjects.iterator();

				while (soIt.hasNext() && !isCanceled()) {
					SQLObject someData = soIt.next();
					someData.fireDbStructureChanged();
					if (someData instanceof SQLTable) {
						TablePane tp = importTableCopy((SQLTable) someData, preferredLocation);
						message = ArchitectUtils.truncateString(((SQLTable)someData).getName());
						//TODO: the following 4 lines are to be removed
						SQLTable newTable = SQLTable.getDerivedInstance((SQLTable)someData, session.getTargetDatabase());
                        DimensionPane dp = new DimensionPane(newTable.getName(), newTable, contentPane);
                        preferredLocation.translate(100, 100);
                        addImpl(dp, preferredLocation, getPPComponentCount());

                        preferredLocation.x += tp.getPreferredSize().width + 5;
						progress++;
					} else if (someData instanceof SQLSchema) {
						SQLSchema sourceSchema = (SQLSchema) someData;
						Iterator it = sourceSchema.getChildren().iterator();
						while (it.hasNext() && !isCanceled()) {
                            Object nextTable = it.next();
                            if (nextTable instanceof SQLExceptionNode) continue;
							SQLTable sourceTable = (SQLTable) nextTable;
							message = ArchitectUtils.truncateString(sourceTable.getName());
							TablePane tp = importTableCopy(sourceTable, preferredLocation);
							preferredLocation.x += tp.getPreferredSize().width + 5;
							progress++;
						}
					} else if (someData instanceof SQLCatalog) {
						SQLCatalog sourceCatalog = (SQLCatalog) someData;
						Iterator cit = sourceCatalog.getChildren().iterator();
						if (sourceCatalog.isSchemaContainer()) {
							while (cit.hasNext() && !isCanceled()) {
								SQLSchema sourceSchema = (SQLSchema) cit.next();
								Iterator it = sourceSchema.getChildren().iterator();
								while (it.hasNext() && !isCanceled()) {
									Object nextTable = it.next();
                                    if (nextTable instanceof SQLExceptionNode) continue;
                                    SQLTable sourceTable = (SQLTable) nextTable;
									message = ArchitectUtils.truncateString(sourceTable.getName());
									TablePane tp = importTableCopy(sourceTable, preferredLocation);
									preferredLocation.x += tp.getPreferredSize().width + 5;
									progress++;
								}
							}
						} else {
							while (cit.hasNext() && !isCanceled()) {
                                Object nextTable = cit.next();
                                if (nextTable instanceof SQLExceptionNode) continue;
								SQLTable sourceTable = (SQLTable) nextTable;
								message = ArchitectUtils.truncateString(sourceTable.getName());
								TablePane tp = importTableCopy(sourceTable, preferredLocation);
								preferredLocation.x += tp.getPreferredSize().width + 5;
								progress++;
							}
						}
					} else {
						logger.error("Unknown object dropped in PlayPen: "+someData); //$NON-NLS-1$
					}
				}
			} catch (ArchitectException e) {
				ASUtils.showExceptionDialog(session,
                    "Unexpected Exception During Import", e); //$NON-NLS-1$
			} finally {
				finished = true;
				hasStarted = false;
				session.getPlayPen().endCompoundEdit("Ending multi-select"); //$NON-NLS-1$
			}
			
			// deals with bug 1333, when the user tries to add inaccessible objects to the PlayPen
			if (jobSize == 0) { 
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    JOptionPane.showMessageDialog(session.getArchitectFrame(),
	                            Messages.getString("PlayPen.noObjectsToImportFound"), Messages.getString("PlayPen.noObjectsToImportFoundDialogTitle"), //$NON-NLS-1$ //$NON-NLS-2$
	                            JOptionPane.WARNING_MESSAGE);
	                }
	            });
	        }
		}

		public boolean hasStarted () {
			return hasStarted;
		}
	}

	// -------------------- SQLOBJECT EVENT SUPPORT ---------------------

	/**
	 * Adds all the listeners that should be listining to events from
	 * the sqlobject hieracrchy.  At this time only the play pen
	 * needs to listen.
	 */
	private void addHierarcyListeners(SQLObject sqlObject) throws ArchitectException
	{
		ArchitectUtils.listenToHierarchy(this, sqlObject);

	}

	/**
	 * Removes all the listeners that should be listining to events from
	 * the sqlobject hieracrchy.  At this time only the play pen
	 * needs to be removed
	 */
	private void removeHierarcyListeners(SQLObject sqlObject) throws ArchitectException
	{
		ArchitectUtils.unlistenToHierarchy(this,sqlObject);
	}

	/**
	 * Listens for property changes in the model (tables
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		logger.debug("SQLObject children got inserted: "+e); //$NON-NLS-1$
		boolean fireEvent = false;
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				addHierarcyListeners(c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't listen to added object", ex); //$NON-NLS-1$
			}
			if (c[i] instanceof SQLTable
				|| (c[i] instanceof SQLRelationship
						&& (((SQLTable.Folder) e.getSource()).getType() == SQLTable.Folder.EXPORTED_KEYS))) {
				fireEvent = true;

				PlayPenComponent ppc = removedComponents.get(c[i]);
				if (ppc != null) {
					if (ppc instanceof Relationship) {
						contentPane.add(ppc, contentPane.getComponentCount());
					} else {
						contentPane.add(ppc, contentPane.getFirstRelationIndex());
					}

				}
			}
		}

		if (fireEvent) {
			firePropertyChange("model.children", null, null); //$NON-NLS-1$
			revalidate();
		}
	}

	/**
	 * Listens for property changes in the model (columns
	 * removed).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		logger.debug("SQLObject children got removed: "+e); //$NON-NLS-1$
		boolean foundRemovedComponent = false;
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				removeHierarcyListeners(c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't unlisten to removed object", ex); //$NON-NLS-1$
			}

			if (c[i] instanceof SQLTable) {
				for (int j = 0; j < contentPane.getComponentCount(); j++) {
					if (contentPane.getComponent(j) instanceof TablePane) {
						TablePane tp = (TablePane) contentPane.getComponent(j);
						if (tp.getModel() == c[i]) {
							removedComponents.put(tp.getModel(), contentPane.getComponent(j));
							contentPane.remove(j);
							foundRemovedComponent = true;
						}
					}
				}
			} else if (c[i] instanceof SQLRelationship) {
				for (int j = 0; j < contentPane.getComponentCount(); j++) {
					if (contentPane.getComponent(j) instanceof Relationship) {
						Relationship r = (Relationship) contentPane.getComponent(j);
						if (r.getModel() == c[i]) {
						    r.setSelected(false,SelectionEvent.SINGLE_SELECT);
							removedComponents.put(r.getModel(), contentPane.getComponent(j));
							contentPane.remove(j);
							foundRemovedComponent = true;
						}
					}
				}
			}
		}

		if (foundRemovedComponent) {
			firePropertyChange("model.children", null, null); //$NON-NLS-1$
			repaint();
		}
	}

	/**
	 * Listens for property changes in the model (table
	 * properties modified).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbObjectChanged(SQLObjectEvent e) {
		firePropertyChange("model."+e.getPropertyName(), null, null); //$NON-NLS-1$
		revalidate();
	}

	/**
	 * Listens for property changes in the model (significant
	 * structure change).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 *
	 * <p>NOTE: This is not currently implemented.
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		logger.debug("Playpen has recieved a db structure change this is unsupported at the moment"); //$NON-NLS-1$
		//throw new UnsupportedOperationException
		//	("FIXME: we have to make sure we're listening to the right objects now!");
		//firePropertyChange("model.children", null, null);
		//revalidate();
	}

	// --------------- SELECTION METHODS ----------------

	/**
	 * Deselects all selectable items in the PlayPen.
	 */
	public void selectNone() {
	 	session.getSourceDatabases().clearSelection();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Selectable) {
 				Selectable s = (Selectable) contentPane.getComponent(i);
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
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Selectable) {
 				Selectable s = (Selectable) contentPane.getComponent(i);
 				s.setSelected(true,SelectionEvent.SINGLE_SELECT);
 			}
 		}
 		mouseMode = MouseModeType.MULTI_SELECT;
        updateDBTree();
	}


	/**
	 * Returns a read-only view of the set of selected children in the PlayPen.
	 */
	public List <PlayPenComponent>getSelectedItems() {
		// It would be possible to speed this up by maintaining a
		// cache of which children are selected, but the need would
		// have to be demonstrated first.
		ArrayList selected = new ArrayList();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Selectable) {
				Selectable s = (Selectable) contentPane.getComponent(i);
				if (s.isSelected()) {
					selected.add(s);
				}
			}
		}
		return Collections.unmodifiableList(selected);
	}

	/**
	 * Returns a read-only view of the set of selected ContainerPane's in the PlayPen.
	 */
	public List <ContainerPane<?, ?> > getSelectedContainers() {
		ArrayList <ContainerPane<?, ?> > selected = new ArrayList<ContainerPane<?, ?> >();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof ContainerPane) {
 			   ContainerPane<?, ?> tp = (ContainerPane<?, ?> ) contentPane.getComponent(i);
				if (tp.isSelected()) {
					selected.add(tp);
				}
			}
		}
		return Collections.unmodifiableList(selected);
	}

	/**
	 * Returns a read-only view of the set of selected relationships in the PlayPen.
	 */
	public List <Relationship>getSelectedRelationShips() {
		ArrayList<Relationship> selected = new ArrayList<Relationship>();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Relationship) {
 				Relationship r = (Relationship) contentPane.getComponent(i);
 				if (r.isSelected()) {
					selected.add(r);
				}
			}
		}
		return Collections.unmodifiableList(selected);
	}

	// ---------------------- SELECTION LISTENER ------------------------

	/**
	 * Forwards the selection event <code>e</code> to all PlayPen
	 * selection listeners. Also selects the object on the DBTree.
	 */
	public void itemSelected(SelectionEvent e) {
	    fireSelectionEvent(e);
	}

	/**
	 * Forwards the selection event <code>e</code> to all PlayPen
	 * selection listeners. Also selects the object on the DBTree.
	 */
	public void itemDeselected(SelectionEvent e) {
	    fireSelectionEvent(e);
	}
	
	// --------------------- SELECTION EVENT SUPPORT ---------------------

	protected LinkedList selectionListeners = new LinkedList();

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}

	protected void fireSelectionEvent(SelectionEvent e) {
		Iterator it = selectionListeners.iterator();
		if (e.getType() == SelectionEvent.SELECTION_EVENT) {
			while (it.hasNext()) {
				((SelectionListener) it.next()).itemSelected(e);
			}
		} else if (e.getType() == SelectionEvent.DESELECTION_EVENT) {
			while (it.hasNext()) {
				((SelectionListener) it.next()).itemDeselected(e);
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

	protected LinkedList undoEventListeners = new LinkedList();

	public void addUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.add(l);
	}

	public void removeSelectionListener(UndoCompoundEventListener l) {
		undoEventListeners.remove(l);
	}

	private void fireUndoCompoundEvent(UndoCompoundEvent e) {
		Iterator it = undoEventListeners.iterator();

		if (e.getType().isStartEvent()) {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditStart(e);
			}
		} else {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditEnd(e);
			}
		}

	}

	public void startCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START,message));
	}

	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END,message));
	}
	// ------------------------------------- INNER CLASSES ----------------------------

	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.  Also retargets drops to the
	 * TablePanes when necessary.
	 */
	public class PlayPenDropListener implements DropTargetListener {

		/**
		 * When the user moves over a table pane, its drop target's
		 * dragEnter method will be called, and this variable will
		 * reference it.  When the user moves off of a table pane, its
		 * dragExit method will be called, and this variable will
		 * reference null (or a different table pane).
		 */
		protected TablePane tpTarget;

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
            		tpTarget.getDropTargetListener().dragExit(dte);
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
			TablePane tp = ppc != null && ppc instanceof TablePane ? (TablePane) ppc : null;

			if (tp != tpTarget) {
				if (tpTarget != null) {
					tpTarget.getDropTargetListener().dragExit(dtde);
				}
				tpTarget = tp;
				if (tpTarget != null) {
					tpTarget.getDropTargetListener().dragEnter(dtde);
				}
			}
			if (tpTarget != null) {
				tpTarget.getDropTargetListener().dragOver(dtde);
			} else {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
		}

		/**
		 * Processes the drop action on the PlayPen (the DropTarget)
		 * or current target TablePane if there is one.
		 */
		public void drop(DropTargetDropEvent dtde) {
			logger.info("Drop: I am over dtde="+dtde); //$NON-NLS-1$
			if (tpTarget != null) {
				tpTarget.getDropTargetListener().drop(dtde);
				return;
			}

			Transferable t = dtde.getTransferable();
			PlayPen playpen = (PlayPen) dtde.getDropTargetContext().getComponent();
			DataFlavor importFlavor = bestImportFlavor(playpen, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
			} else {
				try {

					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					Point dropLoc = playpen.unzoomPoint(new Point(dtde.getLocation()));
					ArrayList paths = (ArrayList) t.getTransferData(importFlavor);
					// turn into a Collection of SQLObjects to make this more generic
					Iterator it = paths.iterator();
					DBTree dbtree = session.getSourceDatabases();
					List sqlObjects = new ArrayList();
					while(it.hasNext()) {
						Object oo = dbtree.getNodeForDnDPath((int[])it.next());
						if (oo instanceof SQLObject) {
							sqlObjects.add(oo);
						} else {
							logger.error("Unknown object dropped in PlayPen: "+oo); //$NON-NLS-1$
						}
					}

					// null: no next task is chained off this
					playpen.addObjects(sqlObjects, dropLoc, null);
					dtde.dropComplete(true);

				} catch (UnsupportedFlavorException ufe) {
					logger.error(ufe);
					dtde.rejectDrop();
				} catch (IOException ioe) {
					logger.error(ioe);
					dtde.rejectDrop();
				} catch (InvalidDnDOperationException ex) {
					logger.error(ex);
					dtde.rejectDrop();
				} catch (ArchitectException ex) {
					logger.error(ex);
					dtde.rejectDrop();
				}
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


 				if (flavors[i].equals(DnDTreePathTransferable.TREEPATH_ARRAYLIST_FLAVOR)) {
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


	public class TablePaneDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {

			if (draggingTablePanes) {
				logger.debug(
						"TablePaneDragGestureListener: ignoring drag event " + //$NON-NLS-1$
						"because draggingTablePanes is true"); //$NON-NLS-1$
				return;
			}

			MouseEvent triggerEvent = (MouseEvent) dge.getTriggerEvent();
            PlayPenComponent c = contentPane.getComponentAt(unzoomPoint(triggerEvent.getPoint()));

			if ( c instanceof TablePane ) {
				TablePane tp = (TablePane) c;
				int colIndex = ContainerPane.ITEM_INDEX_NONE;

				Point dragOrigin = tp.getPlayPen().unzoomPoint(new Point(dge.getDragOrigin()));
				dragOrigin.x -= tp.getX();
				dragOrigin.y -= tp.getY();

				// ignore drag events that aren't from the left mouse button
				if (dge.getTriggerEvent() instanceof MouseEvent
				   && (dge.getTriggerEvent().getModifiers() & InputEvent.BUTTON1_MASK) == 0)
					return;

				// ignore drag events if we're in the middle of a createRelationship
				if (session.getArchitectFrame().createRelationshipIsActive()) {
					logger.debug("CreateRelationship() is active, short circuiting DnD."); //$NON-NLS-1$
					return;
				}

				colIndex = tp.pointToItemIndex(dragOrigin);
				logger.debug("Recognized drag gesture on "+tp.getName()+"! origin="+dragOrigin //$NON-NLS-1$ //$NON-NLS-2$
							 +"; col="+colIndex); //$NON-NLS-1$

				try {
					logger.debug("DGL: colIndex="+colIndex+",columnsSize="+tp.getModel().getColumns().size()); //$NON-NLS-1$ //$NON-NLS-2$
					if (colIndex == ContainerPane.ITEM_INDEX_TITLE) {
						// we don't use this because it often misses drags
						// that start near the edge of the titlebar
//						logger.debug("Discarding drag on titlebar (handled by mousePressed())");
//						draggingTablePanes = true;
						throw new UnsupportedOperationException("We don't use DnD for dragging table panes"); //$NON-NLS-1$
					} else if (colIndex >= 0 && colIndex < tp.getModel().getColumns().size()) {
						// export column as DnD event
						if (logger.isDebugEnabled()) {
							logger.debug("Exporting column "+colIndex+" with DnD"); //$NON-NLS-1$ //$NON-NLS-2$
						}

						tp.draggingColumn = tp.getModel().getColumn(colIndex);
						DBTree tree = session.getSourceDatabases();
						ArrayList paths = new ArrayList();
                        for (SQLColumn column: tp.getSelectedItems()) {

                            int[] path = tree.getDnDPathToNode(column);
                            if (logger.isDebugEnabled()) {
                                StringBuffer array = new StringBuffer();
                                for (int i = 0; i < path.length; i++) {
                                    array.append(path[i]);
                                    array.append(","); //$NON-NLS-1$
                                }
                                logger.debug("Path to dragged node: "+array); //$NON-NLS-1$
                            }
                            // export list of DnD-type tree paths
                            paths.add(path);
                        }
						logger.info("TablePaneDragGestureListener: exporting "+paths.size()+"-item list of DnD-type tree path"); //$NON-NLS-1$ //$NON-NLS-2$
						JLabel label = new JLabel(tp.getModel().getName()+"."+tp.draggingColumn.getName()); //$NON-NLS-1$
						Dimension labelSize = label.getPreferredSize();
						label.setSize(labelSize);  // because a LayoutManager would normally do this
						BufferedImage dragImage = new BufferedImage(labelSize.width, labelSize.height,
																  BufferedImage.TYPE_4BYTE_ABGR);
						Graphics2D imageGraphics = dragImage.createGraphics();
						// XXX: it would be nice to make this transparent, but initial attempts using AlphaComposite failed (on OS X)
						label.repaint();
						imageGraphics.dispose();
						dge.getDragSource().startDrag(dge, null, dragImage, new Point(0, 0),
													new DnDTreePathTransferable(paths), tp);
					}
				} catch (ArchitectException ex) {
					logger.error("Couldn't drag column", ex); //$NON-NLS-1$
					ASUtils.showExceptionDialog(session, Messages.getString("PlayPen.couldNotDragColumn"), ex); //$NON-NLS-1$
				}
			} else {
				return;
			}
		}
	}


	/**
	 * The PPMouseListener class receives all mouse and mouse motion
	 * events in the PlayPen.  It tries to dispatch them to the
	 * ppcomponents, and also handles playpen-specific behaviour like
	 * rubber band selection and popup menu triggering.
	 */
	protected class PPMouseListener
		implements MouseListener, MouseMotionListener, MouseWheelListener  {


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
				maybeShowPopup(evt);
			}
			updateDBTree();
		}


		public void mousePressed(MouseEvent evt) {
			requestFocus();
            maybeShowPopup(evt);
			Point p = evt.getPoint();
			unzoomPoint(p);
			PlayPenComponent c = contentPane.getComponentAt(p);
			
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
		}

		public void mouseReleased(MouseEvent evt) {
			draggingTablePanes = false;
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
            updateDBTree();
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
				for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
					PlayPenComponent c = contentPane.getComponent(i);
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
		    if (c != null) {
		        p.translate(-c.getX(), -c.getY());
		        if (evt.isPopupTrigger() && !evt.isConsumed()) {
		            c.showPopup(p);
		        }
		    } else {
		        if (evt.isPopupTrigger() && popupFactory != null) {
		            PlayPen pp = (PlayPen) evt.getSource();
		            JPopupMenu popup = popupFactory.createPopupMenu();
		            popup.show(pp, evt.getX(), evt.getY());
		        }
		    }
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			if ( (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0 ) {
				if ( e.getWheelRotation() > 0 )
					session.getArchitectFrame().getZoomInAction().actionPerformed(null);
				else
					session.getArchitectFrame().getZoomOutAction().actionPerformed(null);
			}
			else {
				MouseWheelListener[] ml = session.getArchitectFrame().splitPane.getRightComponent().getMouseWheelListeners();
				for ( MouseWheelListener m : ml )
					m.mouseWheelMoved(e);
			}
		}
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
	    private static final int AUTO_SCROLL_INSET = 25; 
	 
	    // no of units to be scrolled in each direction 
	    private Insets scrollUnits = new Insets(AUTO_SCROLL_INSET, AUTO_SCROLL_INSET, AUTO_SCROLL_INSET, AUTO_SCROLL_INSET);
	    
		private PlayPen pp;
		private ContainerPane<?, ?> cp;
		private Point handle;
		private Point p;

		/**
         * Creates a new mouse event handler that tracks mouse motion and moves
         * a container pane around on the play pen accordingly.
         * 
         * @param cp
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
		public FloatingContainerPaneListener(PlayPen pp, ContainerPane<?, ?> cp, Point handle) {
			this.pp = pp;
			Point pointerLocation = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(pointerLocation,pp);
			logger.debug("Adding floating container pane at:"+ pointerLocation); //$NON-NLS-1$
			p = new Point(pointerLocation.x - handle.x, pointerLocation.y - handle.y);

			this.cp = cp;
			this.handle = handle;

			pp.addMouseMotionListener(this);
			pp.addMouseListener(this); // the click that ends this operation

			pp.cursorManager.tableDragStarted();
			pp.startCompoundEdit("Move " + cp.getName()); //$NON-NLS-1$
		}

		public void mouseMoved(MouseEvent e) {
			mouseDragged(e);
		}

		public void mouseDragged(MouseEvent e) {
			pp.zoomPoint(e.getPoint());
			p.setLocation(e.getPoint().x - handle.x, e.getPoint().y - handle.y);
			pp.setChildPosition(cp, p);
			JViewport viewport = (JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, pp);
	        if(viewport==null || pp.getSelectedItems().size() < 1) 
	            return; 
	        
	        // Theoretically should re-validate after each scroll. But that would 
	        // cause the selected component to fall off the border.
	        pp.revalidate();
	        Point viewPos = viewport.getViewPosition(); 
	        Rectangle view = viewport.getViewRect();
	        int viewHeight = viewport.getExtentSize().height; 
	        int viewWidth = viewport.getExtentSize().width; 
	        
	        // performs scrolling 
	        if ((p.y - viewPos.y) < scrollUnits.top && viewPos.y > 0) { // scroll up 
	            view.y = cp.getBounds().y;
	        } if ((viewPos.y + viewHeight - p.y) < scrollUnits.bottom) { // scroll down 
	            view.y = cp.getBounds().y + cp.getBounds().height - viewHeight;
	        } if ((p.x - viewPos.x) < scrollUnits.left && viewPos.x > 0) { // scroll left 
	            view.x = cp.getBounds().x;
	        } if ((viewPos.x + viewWidth - p.x) < scrollUnits.right) { // scroll right 
	            view.x = cp.getBounds().x + cp.getBounds().width - viewWidth;
	        } 
	        logger.debug(viewport.getViewPosition());
	        pp.scrollRectToVisible(view);
	        // Necessary to stop tables from flashing.
	        if (cp != null) {
	            cp.repaint();
	        }
		}

		/**
		 * Anchors the tablepane and disposes this listener instance.
		 */
		public void mouseReleased(MouseEvent e) {
			cleanup();
		}

		protected void cleanup() {
			try {
	            pp.cursorManager.placeModeFinished();
	            pp.cursorManager.tableDragFinished();
	            pp.removeMouseMotionListener(this);
	            pp.removeMouseListener(this);
	            
	            // normalize changes to table panes are part
	            // of this compound edit, refer to bug 1592.
				pp.normalize();
				pp.revalidate();
			} finally {
			    pp.endCompoundEdit("Ending move for table "+cp.getName()); //$NON-NLS-1$
			}
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
			for (PlayPenComponent c : pp.getSelectedItems()) {
				pp.contentPane.remove(c);
				if (c instanceof Relationship) {
					pp.contentPane.add(c,pp.contentPane.getFirstRelationIndex());
				} else {
					pp.contentPane.add(c, 0);
				}
			}
			pp.repaint();
		}
	}

	public static class SendToBackAction extends AbstractAction {

		protected PlayPen pp;

		public SendToBackAction(PlayPen pp) {
			super(Messages.getString("PlayPen.sendToBackActionName")); //$NON-NLS-1$
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
			for (PlayPenComponent c : pp.getSelectedItems()) {
				pp.contentPane.remove(c);
				if (c instanceof Relationship) {
					pp.contentPane.add(c,pp.contentPane.getComponentCount());
				} else {
					pp.contentPane.add(c, pp.contentPane.getFirstRelationIndex());
				}
			}
			pp.repaint();
		}
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

    /**
     * @return The font render context at the current zoom setting
     *         or the font render context defined by the setter.
     */
    public FontRenderContext getFontRenderContext() {
        
        Graphics2D g2 = (Graphics2D) getGraphics();
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
     * @throws ArchitectException 
     */
    public void selectObjects(List<SQLObject> selections) throws ArchitectException {
        if (ignoreTreeSelection) return;
        ignoreTreeSelection = true;

        logger.debug("selecting: " + selections); //$NON-NLS-1$
        DBTree tree = session.getSourceDatabases();
        
        // tables to add to select because of column selection 
        List<SQLObject> colTables = new ArrayList<SQLObject>();
        
        // objects that were already selected, only used for debugging
        List<SQLObject> ignoredObjs = new ArrayList<SQLObject>();

        for (SQLObject obj : selections) {
            if (obj instanceof SQLColumn){
                //Since we cannot directly select a SQLColumn directly
                //from the playpen, there is a special case for it
                SQLColumn col = (SQLColumn) obj;
                SQLTable table = col.getParentTable();
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
                for (SQLColumn col : tablePane.selectedItems) {
                    if (!selections.contains(col) && col.getParentTable() != null) {
                        indices.add(col.getParentTable().getColumnIndex(col));
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

	public PlayPenContentPane getPlayPenContentPane() {
		return contentPane;
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
     * Synchronizes the dbtTree selection with the playpen selections
     * @throws ArchitectException 
     * 
     */
    public void updateDBTree() {
        if (ignoreTreeSelection) return;
        ignoreTreeSelection = true;
        DBTree tree = session.getSourceDatabases();
        tree.clearSelection();
        
        List<TreePath> selectionPaths = new ArrayList<TreePath>();
        boolean addedPaths = false;
        // Keep track of the last tree path
        TreePath lastPath = null;
        // finds all the TreePaths to select
        for (PlayPenComponent comp : getSelectedItems()) {
            TreePath tp = tree.getTreePathForNode((SQLObject) comp.getModel());
            if (!selectionPaths.contains(tp)) {
                selectionPaths.add(tp);
                addedPaths = true;
                lastPath = tp;
            }
            
            if (comp instanceof TablePane) {
                for (SQLColumn col :((TablePane) comp).getSelectedItems()) {
                    tp = tree.getTreePathForNode(col);
                    if (!selectionPaths.contains(tp)) {
                        selectionPaths.add(tp);
                        addedPaths = true;
                        lastPath = tp;
                    }
                }
            }
        }
        
        // Scroll to last tree path.
        if (lastPath != null) {
            tree.scrollPathToVisible(lastPath);
        }
        
        tree.setSelectionPaths(selectionPaths.toArray(new TreePath[selectionPaths.size()]));
        if (addedPaths) {
            tree.clearNonPlayPenSelections();
        }
        ignoreTreeSelection = false;
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
        
        Rectangle visibleRect = unzoomRect(getVisibleRect());
        // adjustments for when visible size is too small
        if (r.getHeight() > visibleRect.height) {
            r.setLocation(minY.getLocation());
            r.setSize(r.width, visibleRect.height);
        }
        if (r.getWidth() > visibleRect.width) {
            r.setLocation(minX.getLocation());
            r.setSize(visibleRect.width, r.height);
        }
        
        scrollRectToVisible(zoomRect(r));
    }
    
    public void updateHiddenColumns() {
        for (TablePane tp : getTablePanes()) {
            tp.updateHiddenColumns();
            tp.revalidate();
            tp.repaint();
        }
    }
}