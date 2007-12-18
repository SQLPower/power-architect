/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
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
import java.awt.event.ActionListener;
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
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
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
import ca.sqlpower.architect.layout.LineStraightenerLayout;
import ca.sqlpower.architect.swingui.Relationship.RelationshipDecorationMover;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;
import ca.sqlpower.architect.swingui.action.CancelAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
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

	/**
	 * The ActionMap key for the action that deletes the selected
	 * object in this TablePane.
	 */
	public static final String KEY_DELETE_SELECTED
		= "ca.sqlpower.architect.swingui.PlayPen.KEY_DELETE_SELECTED";

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
	 * Maps table names (Strings) to Integers.  Useful for making up
	 * new table names if two tables of the same name are added todrag
	 * this playpen.
	 */
	protected HashSet tableNames;

	/**
	 * This is the shared popup menu that applies to right-clicks on
	 * any TablePane in the PlayPen.
	 */
	protected JPopupMenu tablePanePopup;

	protected JPopupMenu playPenPopup;

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
	private final ArchitectSwingSession session;
    
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
        if (session == null) throw new NullPointerException("A null session is not allowed here.");
		this.session = session;
		setDatabase(session.getTargetDatabase());
        zoom = 1.0;
		setBackground(java.awt.Color.white);
		contentPane = new PlayPenContentPane(this);
		setName("Play Pen");
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
		logger.debug("DragGestureRecognizer motion threshold: " + getToolkit().getDesktopProperty("DnD.gestureMotionThreshold"));
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
            logger.error("Couldn't unlisten this playpen from the database");
        }
    }

    /**
     * Returns a new list of all tables in this play pen. The list returned will
     * be your own private (shallow) copy, so you are free to modify it.
     */
    public List<SQLTable> getTables() throws ArchitectException {
        List<SQLTable> tables = new ArrayList();
        ArchitectUtils.extractTables(session.getTargetDatabase(),tables);
        return tables;

    }

	private final void setDatabase(SQLDatabase newdb) {
		if (newdb == null) throw new NullPointerException("db must be non-null");
		
		// Note, this also happens in CoreProject, but that's only helpful when loading a project file
		// And you get fireworks if you call setDataSource() on a non-playpen connection
		newdb.setPlayPenDatabase(true);

		SPDataSource dbcs = new SPDataSource(session.getContext().getPlDotIni());
        dbcs.setName("Not Configured");
        dbcs.setDisplayName("Not Configured");
        newdb.setDataSource(dbcs);

		try {
			ArchitectUtils.listenToHierarchy(this, newdb);
		} catch (ArchitectException ex) {
			logger.error("Couldn't listen to database", ex);
		}
		tableNames = new HashSet();
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

	/**
	 * This routine is called by the PlayPen constructor after it has
	 * set up all the Action instances.  It adds all the necessary
	 * items and action listeners to the TablePane popup menu.
     *
     * Note: if an action is shared with the DBTree, make sure you
     * set the action command in its parent menu item so that the
     * action can figure out what the source of the Action was.
	 */
	void setupTablePanePopup() {
		ArchitectFrame af = session.getArchitectFrame();
		tablePanePopup = new JPopupMenu();

		JMenuItem mi;
        
        mi = new JMenuItem();
        mi.setAction(af.getInsertIndexAction());
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);

        tablePanePopup.addSeparator();
        
		mi = new JMenuItem();
		mi.setAction(af.getEditColumnAction());
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(af.getInsertColumnAction());
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		tablePanePopup.addSeparator();

		mi = new JMenuItem();
		mi.setAction(af.getEditTableAction());
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(bringToFrontAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(sendToBackAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		tablePanePopup.addSeparator();

		mi = new JMenuItem();
		mi.setAction(af.getDeleteSelectedAction());
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		if (logger.isDebugEnabled()) {
			tablePanePopup.addSeparator();
			mi = new JMenuItem("Show listeners");
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						List selection = getSelectedItems();
						if (selection.size() == 1) {
							TablePane tp = (TablePane) selection.get(0);
							JOptionPane.showMessageDialog(PlayPen.this, new JScrollPane(new JList(new java.util.Vector(tp.getModel().getSQLObjectListeners()))));
						} else {
							JOptionPane.showMessageDialog(PlayPen.this, "You can only show listeners on one item at a time");
						}
					}
				});
			tablePanePopup.add(mi);

			mi = new JMenuItem("Show Selection List");
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						List selection = getSelectedItems();
						if (selection.size() == 1) {
							TablePane tp = (TablePane) selection.get(0);
							JOptionPane.showMessageDialog(PlayPen.this, new JScrollPane(new JList(new java.util.Vector(tp.columnSelection))));
						} else {
							JOptionPane.showMessageDialog(PlayPen.this, "You can only show selected columns on one item at a time");
						}
					}
				});
			tablePanePopup.add(mi);
		}
	}

	void setupKeyboardActions() {
		ArchitectFrame af = session.getArchitectFrame();

		InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE_SELECTED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), KEY_DELETE_SELECTED);
		getActionMap().put(KEY_DELETE_SELECTED, af.getDeleteSelectedAction());
		if (af.getDeleteSelectedAction() == null) logger.warn("af.deleteSelectedAction is null!");

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL");
		getActionMap().put("CANCEL", new CancelAction(this));

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomToFitAction().getValue(Action.ACCELERATOR_KEY), "ZOOM TO FIT");
        getActionMap().put("ZOOM TO FIT", af.getZoomToFitAction());

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomInAction().getValue(Action.ACCELERATOR_KEY), "ZOOM IN");
        getActionMap().put("ZOOM IN", af.getZoomInAction());

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomOutAction().getValue(Action.ACCELERATOR_KEY), "ZOOM OUT");
        getActionMap().put("ZOOM OUT", af.getZoomOutAction());

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getZoomResetAction().getValue(Action.ACCELERATOR_KEY), "ZOOM RESET");
        getActionMap().put("ZOOM RESET", af.getZoomResetAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getCreateTableAction().getValue(Action.ACCELERATOR_KEY), "NEW TABLE");
		getActionMap().put("NEW TABLE", af.getCreateTableAction());

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getInsertColumnAction().getValue(Action.ACCELERATOR_KEY), "NEW COLUMN");
        getActionMap().put("NEW COLUMN", af.getInsertColumnAction());
        
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getCreateIdentifyingRelationshipAction().getValue(Action.ACCELERATOR_KEY), "NEW IDENTIFYING RELATION");
		getActionMap().put("NEW IDENTIFYING RELATION", af.getCreateIdentifyingRelationshipAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getCreateNonIdentifyingRelationshipAction().getValue(Action.ACCELERATOR_KEY), "NEW NON IDENTIFYING RELATION");
		getActionMap().put("NEW NON IDENTIFYING RELATION", af.getCreateNonIdentifyingRelationshipAction());

		final Object KEY_SELECT_UPWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_UPWARD";
		final Object KEY_SELECT_DOWNWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_DOWNWARD";
		final Object KEY_EDIT_SELECTION = "ca.sqlpower.architect.PlayPen.KEY_EDIT_SELECTION";

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KEY_SELECT_UPWARD);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KEY_SELECT_DOWNWARD);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KEY_EDIT_SELECTION);

		getActionMap().put(KEY_SELECT_UPWARD, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				List items = getSelectedItems();
				if (items.size() == 1) {
					PlayPenComponent item = (PlayPenComponent) items.get(0);
					if (item instanceof TablePane) {
						TablePane tp = (TablePane) item;
						int oldIndex = tp.getSelectedColumnIndex();
						if (oldIndex > 0) {
							tp.selectNone();
							tp.selectColumn(oldIndex - 1);
						}
					}
				}
			}
		});

		getActionMap().put(KEY_SELECT_DOWNWARD, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				List items = getSelectedItems();
				if (items.size() == 1) {
					PlayPenComponent item = (PlayPenComponent) items.get(0);
					if (item instanceof TablePane) {
						TablePane tp = (TablePane) item;
						int oldIndex = tp.getSelectedColumnIndex();

						try {
							if (oldIndex < tp.getModel().getColumns().size() - 1) {
								tp.selectNone();
								tp.selectColumn(oldIndex + 1);
							}
						} catch (ArchitectException e1) {
							logger.error("Could not get columns of "+ tp.getName(), e1);
						}
					}
				}
			}
		});

		getActionMap().put(KEY_EDIT_SELECTION, new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				ActionEvent ev = new ActionEvent(e.getSource(), e.getID(),
								ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN,
								e.getWhen(), e.getModifiers());
				session.getArchitectFrame().getEditColumnAction().actionPerformed(ev);
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
			firePropertyChange("zoom", oldZoom, newZoom);
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
			logger.debug("minsize is: " + getMinimumSize());
			logger.debug("unzoomed userDim is: " + unzoomPoint(new Point(usedSpace.width,usedSpace.height)));
			logger.debug("zoom="+zoom+",usedSpace size is " + usedSpace);
		}

		if (ppSize != null) {
			logger.debug("preferred size is ppSize (viewport size was null): " + ppSize);
			return ppSize;
		} else {
			logger.debug("preferred size is usedSpace: " + usedSpace);
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
			logger.debug("viewport size is: " + jvp.getSize());
			return jvp.getSize();
		} else {
			logger.debug("viewport size is NULL");
			return null;
		}
	}


	// set the size of the viewport that we are sitting in (return null if there isn't one);
	public void setViewportSize(int width, int height) {
		Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
		if (c != null) {
			JViewport jvp = (JViewport) c;
			logger.debug("viewport size set to: " + width + "," + height);
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
		Iterator it = getTablePanes().iterator();
		while (it.hasNext()) {
			TablePane tp = (TablePane) it.next();
			minX = Math.min(minX, tp.getX());
			minY = Math.min(minY, tp.getY());
		}

		//Readjusts the table pane, since minX and min <= 0,
		//the adjustments of subtracting minX and/or minY makes sense.
		if ( minX < 0 || minY < 0 ) {
			it = getTablePanes().iterator();
			while (it.hasNext()) {
				TablePane tp = (TablePane) it.next();
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
			logger.debug("view position is: " + viewPosition);
			return viewPosition;
		} else {
			return null;
		}
	}

	// set the position of the viewport that we are sitting in
	public void setViewPosition(Point p ) {
		Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
		if (c != null) {
			JViewport jvp = (JViewport) c;
			logger.debug("view position set to: " + p);
			jvp.setViewPosition(p);
		}
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

		logger.debug("start of paintComponent, width="+getWidth()+",height="+getHeight());
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasSetting);

		if (logger.isDebugEnabled()) {
			Rectangle clip = g2.getClipBounds();
			if (clip != null) {
				g2.setColor(Color.green);
				clip.width--;
				clip.height--;
				g2.draw(clip);
				g2.setColor(getBackground());
				logger.debug("Clipping region: "+g2.getClip());
			} else {
				logger.debug("Null clipping region");
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
				if (logger.isDebugEnabled()) logger.debug("Painting visible component "+c);
				g2.translate(c.getLocation().x, c.getLocation().y);
				c.paint(g2);
				g2.setTransform(zoomedOrigin);
			} else {
				if (logger.isDebugEnabled()) logger.debug("paint: SKIPPING "+c);
			}
		}

		if (rubberBand != null && !rubberBand.isEmpty()) {
			if (logger.isDebugEnabled()) logger.debug("painting rubber band "+rubberBand);
			g2.setColor(rubberBandColor);
			Composite backupComp = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			g2.fillRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
			g2.setComposite(backupComp);
			g2.drawRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
		}

		g2.setTransform(backup);

		logger.debug("end of paintComponent, width="+getWidth()+",height="+getHeight());

	}

	protected void addImpl(Component c, Object constraints, int index) {
		throw new UnsupportedOperationException("You can't add swing component for argument");
	}

	/**
	 * Adds the given component to this PlayPen's content pane.  Does
	 * NOT add it to the Swing containment hierarchy. The playpen is a
	 * leaf in the hierarchy as far as swing is concerned.
	 *
	 * @param c The component to add.  The PlayPen only accepts
	 * Relationship and TablePane components.
	 * @param constraints The Point at which to add the component
	 * @param index ignored for now, but would normally specify the
	 * index of insertion for c in the child list.
	 */
	protected void addImpl(PlayPenComponent c, Object constraints, int index) {
		if (c instanceof Relationship) {
			contentPane.add(c, contentPane.getFirstRelationIndex());
		} else if (c instanceof TablePane) {
			if (constraints instanceof Point) {
				c.setLocation((Point) constraints);
				contentPane.add(c, 0);
			} else {
				throw new IllegalArgumentException("Constraints must be a Point");
			}
		} else {
			throw new IllegalArgumentException("PlayPen can't contain components of type "
											   +c.getClass().getName());
		}
		Dimension size = c.getPreferredSize();
		c.setSize(size);
		logger.debug("Set size to "+size);
		logger.debug("Final state looks like "+c);
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

	// ------------------- Right-click popup menu for playpen -----------------------
	protected void setupPlayPenPopup() {
		ArchitectFrame af = session.getArchitectFrame();
		playPenPopup = new JPopupMenu();

		JMenuItem mi = new JMenuItem();
		mi.setAction(af.getCreateTableAction());
		playPenPopup.add(mi);

        mi = new JMenuItem();
        Icon icon = new ImageIcon(ClassLoader.getSystemResource("icons/famfamfam/wrench.png"));
        AutoLayoutAction layoutAction = new AutoLayoutAction(session, "Straighten Lines", "Sraighten Relationship Lines Where Possible", icon);
        layoutAction.setLayout(new LineStraightenerLayout());
        mi.setAction(layoutAction);
        playPenPopup.add(mi);
        
		if (logger.isDebugEnabled()) {
			playPenPopup.addSeparator();
			mi = new JMenuItem("Show Relationships");
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						JOptionPane.showMessageDialog(PlayPen.this, new JScrollPane(new JList(new java.util.Vector(getRelationships()))));
					}
				});
			playPenPopup.add(mi);

			mi = new JMenuItem("Show PlayPen Components");
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						StringBuffer componentList = new StringBuffer();
						for (int i = 0; i < contentPane.getComponentCount(); i++) {
							PlayPenComponent c = contentPane.getComponent(i);
							componentList.append(c).append("["+c.getModel()+"]\n");
						}
						JOptionPane.showMessageDialog(PlayPen.this, new JScrollPane(new JTextArea(componentList.toString())));
					}
				});
			playPenPopup.add(mi);

			mi = new JMenuItem("Show Undo Vector");
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						JOptionPane.showMessageDialog(PlayPen.this, new JScrollPane(new JTextArea(session.getUndoManager().printUndoVector())));
					}
				});
			playPenPopup.add(mi);
		}
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
    public HashSet getTableNames () {
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

		// ensure tablename is unique
		if (logger.isDebugEnabled()) logger.debug("before add: " + tableNames);
		if (!tableNames.add(key)) {
			boolean done = false;
			int newSuffix = 0;
			while (!done) {
				newSuffix++;
				done = tableNames.add(key+"_"+newSuffix);
			}
			newTable.setName(source.getName()+"_"+newSuffix);
		}
		if (logger.isDebugEnabled()) logger.debug("after add: " + tableNames);

		TablePane tp = new TablePane(newTable, this);

		logger.info("adding table "+newTable);
		addImpl(tp, preferredLocation,getPPComponentCount());
		tp.revalidate();

        createRelationshipsFromPP(source, newTable,true);
        createRelationshipsFromPP(source, newTable,false);
		return tp;
	}

    private void createRelationshipsFromPP(SQLTable source, SQLTable newTable, boolean isPrimaryKeyTableNew) throws ArchitectException {
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
			if (logger.isInfoEnabled()) {
				logger.info("Looking for fk table "+r.getFkTable().getName()+" in playpen");
			}

            TablePane tablePane =  null;
            if (isPrimaryKeyTableNew){
                tablePane =findTablePaneByName(r.getFkTable().getName());
            } else {
                tablePane =findTablePaneByName(r.getPkTable().getName());
            }

			if (tablePane != null) {
				logger.info("FOUND IT!");
				SQLTable oldTable = tablePane.getModel();

				SQLRelationship newRel = new SQLRelationship();
				newRel.setName(r.getName());
				newRel.setIdentifying(true);
                if (isPrimaryKeyTableNew) {
                    newRel.attachRelationship(newTable,oldTable,false);
                } else {
                    newRel.attachRelationship(oldTable,newTable,false);
                }
				addImpl(new Relationship(this, newRel),null,getPPComponentCount());

				Iterator mappings = r.getChildren().iterator();
				while (mappings.hasNext()) {
					SQLRelationship.ColumnMapping m
						= (SQLRelationship.ColumnMapping) mappings.next();
					setupMapping(newTable, oldTable, newRel, m,isPrimaryKeyTableNew);
				}
			} else {
				logger.info("NOT FOUND");
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
                throw new IllegalStateException("Couldn't find pkCol "+m.getPkColumn().getName()+" in new table");
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
                throw new IllegalStateException("Couldn't find fkCol "+m.getFkColumn().getName()+" in new table");
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
		                      "Copying objects to the playpen",
		                      "...",
		                      0,
			                  100);
		AddObjectsTask t = new AddObjectsTask(list,
				preferredLocation, pm, null, session);
		t.setNextProcess(nextProcess);
		new Thread(t, "Objects-Adder").start();
	}

	protected class AddObjectsTask extends MonitorableWorker {
		private List<SQLObject> sqlObjects;
		private Point preferredLocation;
		private JDialog parentDialog;
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
				JDialog parentDialog, 
                ArchitectSwingSession session) {
            super(session);
			this.sqlObjects = sqlObjects;
			this.preferredLocation = preferredLocation;
			this.parentDialog = parentDialog;
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
			logger.info("AddObjectsTask starting on thread "+Thread.currentThread().getName());

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
				logger.error("Unexpected exception during populate", e);
                setDoStuffException(e);
				errorMessage = "Unexpected exception during populate: " + e.getMessage();
			} 
			logger.info("AddObjectsTask done");
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
                    errorMessage = "Couldn't get children of " + so;
                    setDoStuffException(e);
					logger.error("Couldn't get children of " + so, e);
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

			session.getPlayPen().startCompoundEdit("Drag to Playpen");

			try {

				// reset iterator
				Iterator<SQLObject> soIt = sqlObjects.iterator();

				while (soIt.hasNext() && !isCanceled()) {
					SQLObject someData = soIt.next();
					someData.fireDbStructureChanged();
					if (someData instanceof SQLTable) {
						TablePane tp = importTableCopy((SQLTable) someData, preferredLocation);
						message = ArchitectUtils.truncateString(((SQLTable)someData).getName());
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
						logger.error("Unknown object dropped in PlayPen: "+someData);
					}
				}
			} catch (ArchitectException e) {
				ASUtils.showExceptionDialog(session,
                    "Unexpected Exception During Import", e);
			} finally {
				finished = true;
				hasStarted = false;
				session.getPlayPen().endCompoundEdit("Ending multi-select");
			}
			
			// deals with bug 1333, when the user tries to add inaccessible objects to the PlayPen
			if (jobSize == 0) { 
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    JOptionPane.showMessageDialog(session.getArchitectFrame(),
	                            "Could not find any objects to add to the PlayPen.", "No objects added",
	                            JOptionPane.WARNING_MESSAGE);
	                }
	            });
	        }
		}

		public boolean hasStarted () {
			return hasStarted;
		}
	}

	/**
	 * Adds the given table pane to the playpen, and adds its model to
	 * the database.  The new table will follow the mouse until the
	 * user clicks.
	 */
	public void addFloating(TablePane tp) {
		new FloatingTableListener(this, tp, zoomPoint(new Point(tp.getSize().width/2,0)),true);
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
		logger.debug("SQLObject children got inserted: "+e);
		boolean fireEvent = false;
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				addHierarcyListeners(c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't listen to added object", ex);
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
			firePropertyChange("model.children", null, null);
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
		logger.debug("SQLObject children got removed: "+e);
		boolean foundRemovedComponent = false;
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				removeHierarcyListeners(c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't unlisten to removed object", ex);
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
			firePropertyChange("model.children", null, null);
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
		firePropertyChange("model."+e.getPropertyName(), null, null);
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
		logger.debug("Playpen has recieved a db structure change this is unsupported at the moment");
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
	 * Returns a read-only view of the set of selected table in the PlayPen.
	 */
	public List <TablePane> getSelectedTables() {
		ArrayList <TablePane> selected = new ArrayList<TablePane>();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof TablePane) {
				TablePane tp = (TablePane) contentPane.getComponent(i);
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
			throw new IllegalStateException("Unknown selection event type "+e.getType());
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
			logger.debug("Drag enter");
			dragOver(dtde);
		}

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer has exited the operable part of the drop site for the
		 * DropTarget registered with this listener or escape has been pressed
		 */
		public void dragExit(DropTargetEvent dte) {
			logger.debug("Drag exit");
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
			logger.info("Drop: I am over dtde="+dtde);
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
							logger.error("Unknown object dropped in PlayPen: "+oo);
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
			logger.debug("Drop Action Changed");
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
			logger.debug("PlayPenTransferHandler: can I import "+Arrays.asList(flavors));
 			for (int i = 0; i < flavors.length; i++) {
				String cls = flavors[i].getDefaultRepresentationClassAsString();
				logger.debug("representation class = "+cls);
				logger.debug("mime type = "+flavors[i].getMimeType());
				logger.debug("type = "+flavors[i].getPrimaryType());
				logger.debug("subtype = "+flavors[i].getSubType());
				logger.debug("class = "+flavors[i].getParameter("class"));
				logger.debug("isSerializedObject = "+flavors[i].isFlavorSerializedObjectType());
				logger.debug("isInputStream = "+flavors[i].isRepresentationClassInputStream());
				logger.debug("isRemoteObject = "+flavors[i].isFlavorRemoteObjectType());
				logger.debug("isLocalObject = "+flavors[i].getMimeType().equals(DataFlavor.javaJVMLocalObjectMimeType));


 				if (flavors[i].equals(DnDTreePathTransferable.TREEPATH_ARRAYLIST_FLAVOR)) {
					logger.debug("YES");
					best = flavors[i];
				} else {
					logger.debug("NO!");
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
						"TablePaneDragGestureListener: ignoring drag event " +
						"because draggingTablePanes is true");
				return;
			}

			PlayPenComponent c = contentPane.getComponentAt(
					unzoomPoint(((MouseEvent) dge.getTriggerEvent()).getPoint()));

			if ( c instanceof TablePane ) {
				TablePane tp = (TablePane) c;
				int colIndex = TablePane.COLUMN_INDEX_NONE;

				Point dragOrigin = tp.getPlayPen().unzoomPoint(new Point(dge.getDragOrigin()));
				dragOrigin.x -= tp.getX();
				dragOrigin.y -= tp.getY();

				// ignore drag events that aren't from the left mouse button
				if (dge.getTriggerEvent() instanceof MouseEvent
				   && (dge.getTriggerEvent().getModifiers() & InputEvent.BUTTON1_MASK) == 0)
					return;

				// ignore drag events if we're in the middle of a createRelationship
				if (session.getArchitectFrame().createRelationshipIsActive()) {
					logger.debug("CreateRelationship() is active, short circuiting DnD.");
					return;
				}

				try {
					colIndex = tp.pointToColumnIndex(dragOrigin);
				} catch (ArchitectException e) {
					logger.error("Got exception while translating drag point", e);
				}
				logger.debug("Recognized drag gesture on "+tp.getName()+"! origin="+dragOrigin
							 +"; col="+colIndex);

				try {
					logger.debug("DGL: colIndex="+colIndex+",columnsSize="+tp.getModel().getColumns().size());
					if (colIndex == TablePane.COLUMN_INDEX_TITLE) {
						// we don't use this because it often misses drags
						// that start near the edge of the titlebar
//						logger.debug("Discarding drag on titlebar (handled by mousePressed())");
//						draggingTablePanes = true;
						throw new UnsupportedOperationException("We don't use DnD for dragging table panes");
					} else if (colIndex >= 0 && colIndex < tp.getModel().getColumns().size()) {
						// export column as DnD event
						if (logger.isDebugEnabled()) {
							logger.debug("Exporting column "+colIndex+" with DnD");
						}

						tp.draggingColumn = tp.getModel().getColumn(colIndex);
						DBTree tree = session.getSourceDatabases();
						ArrayList paths = new ArrayList();
                        for (SQLColumn column: tp.getSelectedColumns()) {

                            int[] path = tree.getDnDPathToNode(column);
                            if (logger.isDebugEnabled()) {
                                StringBuffer array = new StringBuffer();
                                for (int i = 0; i < path.length; i++) {
                                    array.append(path[i]);
                                    array.append(",");
                                }
                                logger.debug("Path to dragged node: "+array);
                            }
                            // export list of DnD-type tree paths
                            paths.add(path);
                        }
						logger.info("TablePaneDragGestureListener: exporting "+paths.size()+"-item list of DnD-type tree path");
						JLabel label = new JLabel(tp.getModel().getName()+"."+tp.draggingColumn.getName());
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
					logger.error("Couldn't drag column", ex);
					ASUtils.showExceptionDialog(session, "Couldn't drag column.", ex);
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
        private boolean componentPreviouslySelected;
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
			if (c != null) p.translate(-c.getX(), -c.getY());
			if ( c instanceof Relationship) {
			    if (evt.getClickCount() == 2) {
					session.getArchitectFrame().getEditRelationshipAction().actionPerformed
					(new ActionEvent(evt.getSource(),
							ActionEvent.ACTION_PERFORMED,
							ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN));
				} else if(evt.getClickCount()==1){
				    if (c.isSelected()&& componentPreviouslySelected)c.setSelected(false,SelectionEvent.SINGLE_SELECT);
                }
          		session.getArchitectFrame().getCreateIdentifyingRelationshipAction().cancel();
				session.getArchitectFrame().getCreateNonIdentifyingRelationshipAction().cancel();
			} else if ( c instanceof TablePane ) {
				TablePane tp = (TablePane) c;
				if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
				    try {
				        int selectedColIndex = tp.pointToColumnIndex(p);
				        if (evt.getClickCount() == 2) { // double click
				            if (tp.isSelected()) {
				                ArchitectFrame af = session.getArchitectFrame();
				                if (selectedColIndex == TablePane.COLUMN_INDEX_TITLE) {
				                    af.getEditTableAction().actionPerformed
				                    (new ActionEvent(tp, ActionEvent.ACTION_PERFORMED, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN));
				                } else if (selectedColIndex >= 0) {
				                    af.getEditColumnAction().actionPerformed
				                    (new ActionEvent(tp, ActionEvent.ACTION_PERFORMED, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN));
				                }
				            }
				        } else if(evt.getClickCount()==1) {
				            logger.debug("Col index "+selectedColIndex);
				            if (selectedColIndex > TablePane.COLUMN_INDEX_TITLE && componentPreviouslySelected){
				                ((TablePane)c).deselectColumn(selectedColIndex);
				            } else if (c.isSelected()&& componentPreviouslySelected) {
				                c.setSelected(false,SelectionEvent.SINGLE_SELECT);
				            }
				        }
				    } catch (ArchitectException e) {
				        logger.error("Exception converting point to column", e);
				    }
				}
			} else {
                session.getArchitectFrame().getCreateIdentifyingRelationshipAction().cancel();
                session.getArchitectFrame().getCreateNonIdentifyingRelationshipAction().cancel();
				maybeShowPopup(evt);
			}
		}


		public void mousePressed(MouseEvent evt) {
            componentPreviouslySelected = false;
			requestFocus();
            maybeShowPopup(evt);
			Point p = evt.getPoint();
			unzoomPoint(p);
			PlayPenComponent c = contentPane.getComponentAt(p);
			if (c != null) p.translate(-c.getX(), -c.getY());

            if (c instanceof Relationship) {

				Relationship r = (Relationship) c;
				PlayPen pp = (PlayPen) r.getPlayPen();

				if ( mouseMode == MouseModeType.CREATING_RELATIONSHIP ) {
				} else {

					if ( (evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) != 0) {
						mouseMode = MouseModeType.MULTI_SELECT;
					} else {
						mouseMode = MouseModeType.SELECT_RELATIONSHIP;
						if ( !r.isSelected() ) {
							pp.selectNone();
						}
					}
				}
                if (r.isSelected()) {
                    componentPreviouslySelected = true;
                } else {
                    r.setSelected(true,SelectionEvent.SINGLE_SELECT);
                }


				// moving pk/fk decoration
				boolean overPkDec = ((RelationshipUI) r.getUI()).isOverPkDecoration(p);
                boolean overFkDec = ((RelationshipUI) r.getUI()).isOverFkDecoration(p);
				if (overPkDec || overFkDec && SwingUtilities.isLeftMouseButton(evt)) {
					new RelationshipDecorationMover(r, overPkDec);
				}
			} else if (c instanceof TablePane) {
				evt.getComponent().requestFocus();
				TablePane tp = (TablePane) c;
				PlayPen pp = (PlayPen) tp.getPlayPen();
				try {
					int clickCol = tp.pointToColumnIndex(p);

					if ( mouseMode == MouseModeType.CREATING_TABLE ) {
					}
					else {
						if ( (evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == 0) {
							if ( !tp.isSelected() || mouseMode == MouseModeType.IDLE ) {
								mouseMode = MouseModeType.SELECT_TABLE;
								pp.selectNone();
							}
						}
						else {
							mouseMode = MouseModeType.MULTI_SELECT;
						}

                        // Alt-click drags table no matter where you clicked
                        if ( (evt.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) {
                            clickCol = TablePane.COLUMN_INDEX_TITLE;
                        }
                        
						if ( clickCol > TablePane.COLUMN_INDEX_TITLE &&
							 clickCol < tp.getModel().getColumns().size()) {

							if ( (evt.getModifiersEx() &
									(InputEvent.SHIFT_DOWN_MASK |
									 InputEvent.CTRL_DOWN_MASK)) == 0) {

								if ( !tp.isColumnSelected(clickCol) ){
									tp.deSelectEverythingElse(evt);
									tp.selectNone();
								}
								mouseMode = MouseModeType.SELECT_COLUMN;
							}
                            if (tp.isColumnSelected(clickCol)) {
                                componentPreviouslySelected = true;
                            } else {
                                tp.selectColumn(clickCol);
                            }

							tp.fireSelectionEvent(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT,SelectionEvent.SINGLE_SELECT));
							tp.repaint();
						}
                        if (tp.isSelected()&& clickCol == TablePane.COLUMN_INDEX_TITLE){
                            componentPreviouslySelected = true;
                        } else {
                            tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
                        }
					}



					if (clickCol == TablePane.COLUMN_INDEX_TITLE && !session.getArchitectFrame().createRelationshipIsActive()) {
						Iterator it = pp.getSelectedTables().iterator();
						logger.debug("event point: " + p);
						logger.debug("zoomed event point: " + pp.zoomPoint(new Point(p)));
						draggingTablePanes = true;

						while (it.hasNext()) {
							// create FloatingTableListener for each selected item
							TablePane t3 = (TablePane)it.next();
							logger.debug("(" + t3.getModel().getName() + ") zoomed selected table point: " + t3.getLocationOnScreen());
							logger.debug("(" + t3.getModel().getName() + ") unzoomed selected table point: " + pp.unzoomPoint(t3.getLocationOnScreen()));
							/* the floating table listener expects zoomed handles which are relative to
	                           the location of the table column which was clicked on.  */
							Point clickedColumn = tp.getLocationOnScreen();
							Point otherTable = t3.getLocationOnScreen();
							Point handle = pp.zoomPoint(new Point(p));
							logger.debug("(" + t3.getModel().getName() + ") translation x="
	                                      + (otherTable.getX() - clickedColumn.getX()) + ",y="
	                                      + (otherTable.getY() - clickedColumn.getY()));
							handle.translate((int)(clickedColumn.getX() - otherTable.getX()), (int) (clickedColumn.getY() - otherTable.getY()));
							new PlayPen.FloatingTableListener(pp, t3, handle,false);
						}
					}
				} catch (ArchitectException e) {
					logger.error("Exception converting point to column", e);
				}
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
			if (rubberBand != null) {
				if (evt.getButton() == MouseEvent.BUTTON1) {
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
			}
			maybeShowPopup(evt);

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
				Rectangle temp = new Rectangle();  // avoids multiple allocations in getBounds
				for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
					PlayPenComponent c = contentPane.getComponent(i);
					if (c instanceof Relationship) {
					    // relationship is non-rectangular so we can't use getBounds for intersection testing
					    ((Relationship) c).setSelected(((Relationship) c).intersects(rubberBand),SelectionEvent.SINGLE_SELECT);
					} else if (c instanceof Selectable) {
						((Selectable) c).setSelected(rubberBand.intersects(c.getBounds(temp)),SelectionEvent.SINGLE_SELECT);
					}
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
		 * Shows the playpen popup menu if appropriate.
		 */
		public boolean maybeShowPopup(MouseEvent evt) {

			setupTablePanePopup();
			setupPlayPenPopup();

			Point p = evt.getPoint();
			unzoomPoint(p);
			PlayPenComponent c = contentPane.getComponentAt(p);
			if (c != null) p.translate(-c.getX(), -c.getY());

			if ( c instanceof Relationship) {
				if (evt.isPopupTrigger() && !evt.isConsumed()) {
					Relationship r = (Relationship) c;
					r.setSelected(true,SelectionEvent.SINGLE_SELECT);
					r.showPopup(r.getPopup(), p);
					return true;
				}
			} else if ( c instanceof TablePane ) {
				TablePane tp = (TablePane) c;
				if (evt.isPopupTrigger() && !evt.isConsumed()) {
					PlayPen pp = tp.getPlayPen();

					try {
						tp.selectNone(); // single column selection model for now
						int idx = tp.pointToColumnIndex(p);
						if (idx >= 0) {
							tp.selectColumn(idx);
						}
					} catch (ArchitectException e) {
						logger.error("Exception converting point to column", e);
						return false;
					}
					logger.debug("about to show playpen tablepane popup...");
					tp.showPopup(pp.tablePanePopup, p);
					return true;
				}
			} else {
				PlayPen pp = (PlayPen) evt.getSource();
				if (evt.isPopupTrigger()) {
					pp.playPenPopup.show(pp, evt.getX(), evt.getY());
					return true;
				}
			}
			return false;
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
	public static class FloatingTableListener extends  MouseInputAdapter implements CancelableListener  {
		private PlayPen pp;
		private TablePane tp;
		private Point handle;
		private Point p;

		/**
		 * If true, we will add the given TablePane to the play pen once the user clicks,
		 * and add its model to the playpen's database.
		 */
		private boolean addToPP;

		public FloatingTableListener(PlayPen pp, TablePane tp, Point handle, boolean addToPP) {
			this.pp = pp;
			this.addToPP = addToPP;
			PointerInfo pi = MouseInfo.getPointerInfo();

			Point startLocation = pi.getLocation();
			SwingUtilities.convertPointFromScreen(startLocation,pp);
			logger.debug("Adding floating table at:"+ startLocation);
			p = pp.zoomPoint(startLocation);

			this.tp = tp;
			this.handle = handle;

			pp.addMouseMotionListener(this);
			pp.addMouseListener(this); // the click that ends this operation
			pp.addCancelableListener(this);

			//In order to get here, the user must let go of the mouse click
            //on the create new table icon.  If the user decides to click on the
            //icon but let go elsewhere, the create table action will not occur
            if (addToPP) {
				pp.cursorManager.placeModeStarted();
			} else {
                pp.cursorManager.tableDragStarted();
				pp.startCompoundEdit("Move"+tp.getName());
			}
		}

		public void mouseMoved(MouseEvent e) {
			mouseDragged(e);
		}

		public void mouseDragged(MouseEvent e) {
			pp.zoomPoint(e.getPoint());
			p = new Point(e.getPoint().x - handle.x, e.getPoint().y - handle.y);
			pp.setChildPosition(tp, p);
		}

		/**
		 * Anchors the tablepane and disposes this listener instance.
		 */
		public void mouseReleased(MouseEvent e) {
			cleanup(false);
		}

		public void cancel() {
			cleanup(true);
		}

		protected void cleanup(boolean cancelled) {
			try {
				if (addToPP && !cancelled) {
					pp.unzoomPoint(p);
					logger.debug("Placing table at: " + p);
					pp.addImpl(tp, p, pp.getPPComponentCount());
					try {
						pp.getSession().getTargetDatabase().addChild(tp.getModel());
						pp.selectNone();
						tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
						pp.mouseMode = MouseModeType.SELECT_TABLE;
										
						final ArchitectFrame frame = pp.getSession().getArchitectFrame();
						final TableEditPanel editPanel = new TableEditPanel(pp.getSession(), tp.getModel()) {
						    @Override
						    public void discardChanges() {
						        pp.getSession().getTargetDatabase().removeChild(tp.getModel());
                                pp.getTableNames().remove(tp.getModel().getName());
						    }
						};

	                    
						JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
						        editPanel, frame,
						        "Table Properties", "OK");
						
						d.pack();
						d.setLocationRelativeTo(frame);
						d.setVisible(true);
					} catch (ArchitectException e) {
						logger.error("Couldn't add table \"" + tp.getModel() + "\" to play pen:", e);
						SPSUtils.showExceptionDialogNoReport(pp.getSession().getArchitectFrame(), "Failed to add table.", e);
						return;
					}
				}
			} finally {
				if (!addToPP) {
					pp.endCompoundEdit("Ending move for table "+tp.getName());
				}
			}

            pp.cursorManager.placeModeFinished();
            pp.cursorManager.tableDragFinished();
			pp.removeMouseMotionListener(this);
			pp.removeMouseListener(this);
			pp.removeCancelableListener(this);
            pp.normalize();
			pp.revalidate();
		}
	}

	// -------------- Bring to Front / Send To Back ------------------
	public static class BringToFrontAction extends AbstractAction {

		protected PlayPen pp;

		public BringToFrontAction(PlayPen pp) {
			super("Bring to Front");
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
			List items = pp.getSelectedItems();
			Iterator it = items.iterator();
			while (it.hasNext()) {
				PlayPenComponent c = (PlayPenComponent) it.next();
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
			super("Send to Back");
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
			List items = pp.getSelectedItems();
			Iterator it = items.iterator();
			while (it.hasNext()) {
				PlayPenComponent c = (PlayPenComponent) it.next();
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
     * @return The font render context at the current zoom setting.
     */
    public FontRenderContext getFontRenderContext() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        FontRenderContext frc = null;
        if (g2 != null) {
            g2.scale(zoom, zoom);
            frc = g2.getFontRenderContext();
            g2.dispose();
        }
        if (logger.isDebugEnabled()) logger.debug("Returning frc="+frc);
        return frc;
    }

    /**
     * Selects the playpen component that represents the given SQLObject,
     * and scrolls the viewport to ensure the component is visible on-screen.
     * If the given SQL Object isn't in the playpen, this method has no effect.
     *
     * @param selection Either a SQLTable or SQLRelationship object.
     */
    public void selectAndShow(SQLObject selection) {
        if (selection instanceof SQLTable) {
            TablePane tp = findTablePane((SQLTable) selection);
            if (tp != null) {
                selectNone();
                tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
                Rectangle scrollTo = tp.getBounds();
                zoomRect(scrollTo);
                scrollRectToVisible(scrollTo);
            }
        } else if (selection instanceof SQLRelationship) {
            Relationship r = findRelationship((SQLRelationship) selection);
            if (r != null) {
                selectNone();
                r.setSelected(true,SelectionEvent.SINGLE_SELECT);
                Rectangle scrollTo = r.getBounds();
                zoomRect(scrollTo);
                scrollRectToVisible(scrollTo);
            }
        }

    }

	public PlayPenContentPane getPlayPenContentPane() {
		return contentPane;
	}

	public void setMouseMode(MouseModeType mouseMode) {
		this.mouseMode = mouseMode;
	}

    public ArchitectSwingSession getSession() {
        return session;
    }
    
    public CursorManager getCursorManager() {
        return cursorManager;
    }

}
