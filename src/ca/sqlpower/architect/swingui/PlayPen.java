package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;
import ca.sqlpower.sql.DBConnectionSpec;

public class PlayPen extends JPanel
	implements java.io.Serializable, SQLObjectListener, SelectionListener, ContainerListener, MouseListener, MouseMotionListener {

	private static Logger logger = Logger.getLogger(PlayPen.class);

	/**
	 * Links this PlayPen with an instance of PlayPenDropListener so
	 * users can drop stuff on the playpen.
	 */
	protected DropTarget dt;

	/**
	 * This database is the container of all the SQLObjects in this
	 * playpen.  Items added via the importTableCopy, addSchema, ... methods
	 * will be added into this database.
	 */
	protected SQLDatabase db;
	
	/**
	 * Maps table names (Strings) to Integers.  Useful for making up
	 * new table names if two tables of the same name are added to
	 * this playpen.
	 */
	protected HashMap tableNames;
	
	/**
	 * This is the shared popup menu that applies to right-clicks on
	 * any TablePane in the PlayPen.
	 */
	protected JPopupMenu tablePanePopup;

	protected JPopupMenu playPenPopup;

	/**
	 * The visual magnification factor for this playpen.
	 */
	protected double zoom;

	/**
	 * Contains the child components of this playpen.
	 */
	protected PlayPenContentPane contentPane;

	public PlayPen() {
		zoom = 1.0;
		setBackground(java.awt.Color.white);
		contentPane = new PlayPenContentPane(this);
		contentPane.addContainerListener(this);
		contentPane.setFont(getFont());
		contentPane.setForeground(getForeground());
		contentPane.setBackground(getBackground());
		setLayout(null);
		setName("Play Pen");
		setMinimumSize(new Dimension(1,1));
		dt = new DropTarget(this, new PlayPenDropListener());
		setupTablePanePopup();
		setupPlayPenPopup();
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public PlayPen(SQLDatabase db) {
		this();
		setDatabase(db);
	}

	public SQLDatabase getDatabase() {
		return db;
	}

	public void setDatabase(SQLDatabase newdb) {
		if (newdb == null) throw new NullPointerException("db must be non-null");
		this.db = newdb;
		db.setIgnoreReset(true);
		if (db.getConnectionSpec() == null) {
			DBConnectionSpec dbcs = new DBConnectionSpec();
			dbcs.setName("Target Database");
			dbcs.setDisplayName("Target Database");
			db.setConnectionSpec(dbcs);
		}
		try {
			ArchitectUtils.listenToHierarchy(this, db);
		} catch (ArchitectException ex) {
			logger.error("Couldn't listen to database", ex);
		}
		tableNames = new HashMap();
	}

	/**
	 * This routine is called by the PlayPen constructor after it has
	 * set up all the Action instances.  It adds all the necessary
	 * items and action listeners to the TablePane popup menu.
	 */
	void setupTablePanePopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		tablePanePopup = new JPopupMenu();

		JMenuItem mi;

		mi = new JMenuItem();
		mi.setAction(af.editColumnAction);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(af.insertColumnAction);
		tablePanePopup.add(mi);

		tablePanePopup.addSeparator();

		mi = new JMenuItem();
		mi.setAction(af.editTableAction);
		tablePanePopup.add(mi);
		
		tablePanePopup.addSeparator();

		mi = new JMenuItem();
		mi.setAction(af.deleteSelectedAction);
		tablePanePopup.add(mi);

		if (logger.isDebugEnabled()) {
			tablePanePopup.addSeparator();
			mi = new JMenuItem("Show listeners");
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TablePane tp = (TablePane) getSelection();
						JOptionPane.showMessageDialog(tp, new JScrollPane(new JList(new java.util.Vector(tp.getModel().getSQLObjectListeners()))));
					}
				});
			tablePanePopup.add(mi);

			mi = new JMenuItem("Show Selection List");
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TablePane tp = (TablePane) getSelection();
						JOptionPane.showMessageDialog(tp, new JScrollPane(new JList(new java.util.Vector(tp.columnSelection))));
					}
				});
			tablePanePopup.add(mi);
		}
	}
	
	// --------------------- Utility methods -----------------------

	/**
	 * Calls setChildPositionImpl(child, p.x, p.y).
	 */ 
	public void setChildPosition(JComponent child, Point p) {
		setChildPositionImpl(child, p.x, p.y);
	}

	/**
	 * Calls setChildPositionImpl(child, x, y).
	 */ 
	public void setChildPosition(JComponent child, int x, int y) {
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
	protected void setChildPositionImpl(JComponent child, int x, int y) {
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

	// -------------------------- JComponent overrides ---------------------------

	/**
	 * Calculates the smallest rectangle that will completely
	 * enclose the visible components.
	 */
	public Dimension getPreferredSize() {
		Rectangle cbounds = null;
		//int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = 0, maxy = 0;
		int minx = 0, miny = 0, maxx = 0, maxy = 0;
		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			Component c = contentPane.getComponent(i);
			if (c.isVisible()) {
				cbounds = c.getBounds(cbounds);
				minx = Math.min(cbounds.x, minx);
				miny = Math.min(cbounds.y, miny);
				maxx = Math.max(cbounds.x + cbounds.width , maxx);
				maxy = Math.max(cbounds.y + cbounds.height, maxy);
			}
		}
		
		Dimension min = getMinimumSize();
		return new Dimension((int) ((double) Math.max(maxx - minx, min.width) * zoom),
							 (int) ((double) Math.max(maxy - miny, min.height) * zoom));
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

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
			Component c = contentPane.getComponent(i);
			logger.debug("painting "+c);
			c.getBounds(bounds);
			if (c.isVisible() && g2.hitClip(bounds.x, bounds.y, bounds.width, bounds.height)) {
				if (logger.isDebugEnabled()) logger.debug("Painting visible component "+c);
				g2.translate(c.getLocation().x, c.getLocation().y);
				c.paint(g2);
				g2.setTransform(zoomedOrigin);
			}
		}
		g2.setTransform(backup);
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
	protected void addImpl(Component c, Object constraints, int index) {
		if (c instanceof Relationship) {
			contentPane.add(c);
		} else if (c instanceof TablePane) {
			if (constraints instanceof Point) {
				contentPane.add(c);
				c.setLocation((Point) constraints);
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
		c.setVisible(true);
		logger.debug("Final state looks like "+c);
	}

	// ------------------- Right-click popup menu for playpen -----------------------
	protected void setupPlayPenPopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		playPenPopup = new JPopupMenu();

 		JMenuItem mi = new JMenuItem();
		mi.setAction(chooseDBCSAction);
		playPenPopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(af.createTableAction);
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
		}
	}

	public Action chooseDBCSAction = new AbstractAction("Database Output Selection") {
			public void actionPerformed(ActionEvent e) {
				final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
											  "Target Database Connection");
				JPanel cp = new JPanel(new BorderLayout(12,12));
				cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
				final DBCSPanel dbcsPanel = new DBCSPanel();
				dbcsPanel.setDbcs(db.getConnectionSpec());
				cp.add(dbcsPanel, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcsPanel.applyChanges();
							d.setVisible(false);
						}
					});
				buttonPanel.add(okButton);

				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcsPanel.discardChanges();
							d.setVisible(false);
						}
					});
				buttonPanel.add(cancelButton);

				cp.add(buttonPanel, BorderLayout.SOUTH);

				d.setContentPane(cp);
				d.pack();
				d.setVisible(true);
			}
		};

	/**
	 * Searches this PlayPen's children for a TablePane whose model is
	 * t.
	 *
	 * @return A reference to the TablePane that has t as a model, or
	 * null if no such TablePane is in the play pen.
	 */
	public TablePane findTablePane(SQLTable t) {
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c instanceof TablePane 
				&& ((TablePane) c).getModel() == t) {
				return (TablePane) c;
			}
		}
		return null;
	}

	/**
	 * Returns a list of the Relationship gui components in this
	 * playpen.
	 */
	public List getRelationships() {
		LinkedList relationships = new LinkedList();
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			if (contentPane.getComponent(i) instanceof Relationship) {
				relationships.add(contentPane.getComponent(i));
			}
		}
		return relationships;
	}

	/**
	 * Adds a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 *
	 * @return A reference to the newly-created TablePane.
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addComponent(Component,Object)
	 */
	public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation) throws ArchitectException {
		SQLTable newTable = SQLTable.getDerivedInstance(source, db); // adds newTable to db
		String key = source.getTableName().toLowerCase();
		Integer suffix = (Integer) tableNames.get(key);
		if (suffix == null) {
			tableNames.put(key, new Integer(0));
		} else {
			int newSuffix = suffix.intValue()+1;
			tableNames.put(key, new Integer(newSuffix));
			newTable.setTableName(source.getTableName()+"_"+newSuffix);
		}
		TablePane tp = new TablePane(newTable);
		
		logger.info("adding table "+newTable);
		add(tp, preferredLocation);
		tp.revalidate();
		return tp;
	}

	/**
	 * Calls {@link #importTableCopy} for each table contained in the given schema.
	 */
	public synchronized void addSchema(SQLSchema source, Point preferredLocation) throws ArchitectException {
		AddSchemaTask t = new AddSchemaTask(source, preferredLocation);
		new Thread(t, "Schema-Adder").start();
	}

	private class AddSchemaTask implements Runnable {
		SQLSchema source;
		Point preferredLocation;

		public AddSchemaTask(SQLSchema source, Point preferredLocation) {
			this.source = source;
			this.preferredLocation = new Point(preferredLocation);
		}

		public void run() {
			logger.info("AddSchemaTask starting on thread "+Thread.currentThread().getName());
			ProgressMonitor pm = null;
			try {
				pm = new ProgressMonitor
					(PlayPen.this,
					 "Copying schema "+source.getShortDisplayName(),
					 "...",
					 0,
					 source.getChildCount());
				int i = 0;
				Iterator it = source.getChildren().iterator();
				while (it.hasNext() && !pm.isCanceled()) {
					SQLTable sourceTable = (SQLTable) it.next();
					pm.setNote(sourceTable.getTableName());
					TablePane tp = importTableCopy(sourceTable, preferredLocation);
					preferredLocation.x += tp.getPreferredSize().width + 5;
					pm.setProgress(i++);
				}
			} catch (ArchitectException e) {
				e.printStackTrace();
			} finally {
				if (pm != null) pm.close();
			}
			logger.info("AddSchemaTask done");
		}
	}

	/**
	 * Adds the given table pane to the playpen, and adds its model to
	 * the database.  The new table will follow the mouse until the
	 * user clicks.
	 */
	public void addFloating(TablePane tp) {
		db.addChild(tp.getModel());
		tp.setVisible(false);
		add(tp, new Point(0,0));
		new FloatingTableListener(this, tp, zoomPoint(new Point(tp.getSize().width/2,0)));
	}

	// -------------------- SQLOBJECT EVENT SUPPORT ---------------------

	/**
	 * Listens for property changes in the model (tables
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		logger.debug("SQLObject children got inserted: "+e);
		boolean fireEvent = false;
		SQLObject o = e.getSQLSource();
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				ArchitectUtils.listenToHierarchy(this, c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't listen to added object", ex);
			}
			if (c[i] instanceof SQLTable
				|| c[i] instanceof SQLRelationship) {
				fireEvent = true;
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
		boolean fireEvent = false;
		SQLObject o = e.getSQLSource();
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				ArchitectUtils.unlistenToHierarchy(this, c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't unlisten to removed object", ex);
			}

			if (c[i] instanceof SQLTable) {
				for (int j = 0; j < contentPane.getComponentCount(); j++) {
					if (contentPane.getComponent(j) instanceof TablePane) {
						TablePane tp = (TablePane) contentPane.getComponent(j);
						if (selectedChild == tp) selectedChild = null;
						if (tp.getModel() == c[i]) {
							contentPane.remove(j);
							fireEvent = true;
						}
					}
				}
			} else if (c[i] instanceof SQLRelationship) {
				for (int j = 0; j < contentPane.getComponentCount(); j++) {
					if (contentPane.getComponent(j) instanceof Relationship) {
						Relationship r = (Relationship) contentPane.getComponent(j);
						if (selectedChild == r) selectedChild = null;
						if (r.getModel() == c[i]) {
							contentPane.remove(j);
							fireEvent = true;
						}
					}
				}
			}
		}

		if (fireEvent) {
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
		throw new UnsupportedOperationException
			("FIXME: we have to make sure we're listening to the right objects now!");
		//firePropertyChange("model.children", null, null);
		//revalidate();
	}

	// --------------- SELECTION METHODS ----------------

	protected Selectable selectedChild; // XXX: should be List so multiselection is possible!

	/**
	 * Deselects all selectable items in the PlayPen. XXX: only single selection for now!
	 */
	public void selectNone() {
// 		for (int i = 0, n = getComponentCount(); i < n; i++) {
// 			if (getComponent(i) instanceof Selectable) {
// 				Selectable s = (Selectable) getComponent(i);
// 				s.setSelected(false);
// 			}
// 		}
		if (selectedChild != null) {
			selectedChild.setSelected(false);
			selectedChild = null;
		}
	}

	/**
	 * Returns the first selected child in the PlayPen. XXX: only single selection for now!
	 */
	public Selectable getSelection() {
// 		for (int i = 0, n = getComponentCount(); i < n; i++) {
// 			if (getComponent(i) instanceof Selectable) {
// 				Selectable s = (Selectable) getComponent(i);
// 				if (s.isSelected()) return s;
// 			}
// 		}
		return selectedChild;
	}

	public void setSelection(Selectable s) {
		selectNone();
		s.setSelected(true);
		selectedChild = s;
		fireSelectionEvent(s);
	}

	// --------------------------- CONTAINER LISTENER -------------------------

	/**
	 * Unregisters this PlayPen as a SelectionListener if the
	 * removed component is Selectable.
	 */
	public void componentRemoved(ContainerEvent e) {
		if (e.getChild() instanceof Selectable) {
			((Selectable) e.getChild()).removeSelectionListener(this);
		}
	}

	/**
	 * Registers this PlayPen as a SelectionListener if the added
	 * component is Selectable.
	 */
	public void componentAdded(ContainerEvent e) {
		((JComponent) e.getChild()).revalidate();
		if (e.getChild() instanceof Selectable) {
			((Selectable) e.getChild()).addSelectionListener(this);
		}
	}

	// ---------------------- SELECTION LISTENER ------------------------
	
	/**
	 * Saves a reference to the selected child, then fires e to all
	 * PlayPen selection listeners.
	 */
	public void itemSelected(SelectionEvent e) {
		if (e.getSelectedItem().isSelected()) {
			logger.debug("Child "+e.getSelectedItem()+" is now selected");
			selectedChild = e.getSelectedItem();
		} else {
			selectedChild = null;
		}
		fireSelectionEvent(e.getSelectedItem());
	}

	// --------------------- SELECTION EVENT SUPPORT ---------------------

	protected LinkedList selectionListeners = new LinkedList();

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}
	
	protected void fireSelectionEvent(Selectable source) {
		SelectionEvent e = new SelectionEvent(source);
		Iterator it = selectionListeners.iterator();
		while (it.hasNext()) {
			((SelectionListener) it.next()).itemSelected(e);
		}
	}

	// ------------------------------------- INNER CLASSES ----------------------------

	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.  Also retargets drops to the
	 * TablePanes when necessary.
	 */
	public static class PlayPenDropListener implements DropTargetListener {

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
			dragOver(dtde);
		}
		
		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer has exited the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragExit(DropTargetEvent dte) {
		}
		
		/**
		 * Called when a drag operation is ongoing, while the mouse
		 * pointer is still over the operable part of the drop site for
		 * the DropTarget registered with this listener.
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			PlayPen pp = (PlayPen) dtde.getDropTargetContext().getComponent();
			Point sp = pp.unzoomPoint(new Point(dtde.getLocation()));
			Component ppc = pp.contentPane.getComponentAt(sp);
			TablePane tp = ppc != null && ppc instanceof TablePane ? (TablePane) ppc : null;
			if (tp != tpTarget) {
				if (tpTarget != null) {
					tpTarget.getDropTarget().dragExit(dtde);
				}
				tpTarget = tp;
				if (tpTarget != null) {
					tpTarget.getDropTarget().dragEnter(dtde);
				}
			}
			if (tpTarget != null) {
				tpTarget.getDropTarget().dragOver(dtde);
			} else {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
		}
		
		/**
		 * Processes the drop action on the PlayPen (the DropTarget)
		 * or current target TablePane if there is one.
		 */
		public void drop(DropTargetDropEvent dtde) {
			if (tpTarget != null) {
				tpTarget.getDropTarget().drop(dtde);
				return;
			}

			Transferable t = dtde.getTransferable();
			PlayPen c = (PlayPen) dtde.getDropTargetContext().getComponent();
			DataFlavor importFlavor = bestImportFlavor(c, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
			} else {
				try {
					DBTree dbtree = ArchitectFrame.getMainInstance().dbTree; // XXX: this is bad
					ArrayList paths = (ArrayList) t.getTransferData(importFlavor);
					Iterator pathIt = paths.iterator();
					Point dropLoc = c.unzoomPoint(new Point(dtde.getLocation()));
					while (pathIt.hasNext()) {
						Object someData = dbtree.getNodeForDnDPath((int[]) pathIt.next());
						
						if (someData instanceof SQLTable) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							c.importTableCopy((SQLTable) someData, dropLoc);
							dtde.dropComplete(true);
							return;
						} else if (someData instanceof SQLSchema) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							SQLSchema sourceSchema = (SQLSchema) someData;
							c.addSchema(sourceSchema, dropLoc);
							dtde.dropComplete(true);
							return;
						} else if (someData instanceof SQLCatalog) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							SQLCatalog sourceCatalog = (SQLCatalog) someData;
							Iterator cit = sourceCatalog.getChildren().iterator();
							if (sourceCatalog.isSchemaContainer()) {
								while (cit.hasNext()) {
									SQLSchema sourceSchema = (SQLSchema) cit.next();
									c.addSchema(sourceSchema, dropLoc);
								}
							} else {
								while (cit.hasNext()) {
									SQLTable sourceTable = (SQLTable) cit.next();
									c.importTableCopy(sourceTable, dropLoc);
								}
							}
							dtde.dropComplete(true);
							return;
						} else if (someData instanceof SQLColumn) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							SQLColumn column = (SQLColumn) someData;
							JLabel colName = new JLabel(column.getColumnName());
							colName.setSize(colName.getPreferredSize());
							c.add(colName, dropLoc);
							logger.debug("Added "+column.getColumnName()+" to playpen (temporary, only for testing)");
							colName.revalidate();
							dtde.dropComplete(true);
							return;
						} else {
							dtde.rejectDrop();
						}
					}
				} catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
					dtde.rejectDrop();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					dtde.rejectDrop();
				} catch (InvalidDnDOperationException ex) {
					ex.printStackTrace();
					dtde.rejectDrop();
				} catch (ArchitectException ex) {
					ex.printStackTrace();
					dtde.rejectDrop();
				}
			}
		}
		
		/**
		 * Called if the user has modified the current drop gesture.
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
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


 				if (flavors[i].equals(DnDTreePathTransferable.flavor)) {
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

	// ------------------- MOUSE LISTENER INTERFACE ------------------

	public void mouseEntered(MouseEvent evt) {
		retargetToContentPane(evt);
	}

	public void mouseExited(MouseEvent evt) {
		retargetToContentPane(evt);
	}

	public void mouseClicked(MouseEvent evt) {
		if (!retargetToContentPane(evt)) {
			maybeShowPopup(evt);
		}
	}
	
	public void mousePressed(MouseEvent evt) {
		if (!retargetToContentPane(evt)) {
			maybeShowPopup(evt);
		}
	}
	
	public void mouseReleased(MouseEvent evt) {
		if (!retargetToContentPane(evt)) {
			((PlayPen) evt.getSource()).selectNone();
			maybeShowPopup(evt);
		}
	}
	
	// ---------------- MOUSE MOTION LISTENER INTERFACE -----------------
	public void mouseDragged(MouseEvent evt) {
		retargetToContentPane(evt);
	}

	public void mouseMoved(MouseEvent evt) {
		retargetToContentPane(evt);
	}

	/**
	 * Handles retargetting of mouse events.
	 */
	public boolean retargetToContentPane(MouseEvent evt) {
		PlayPen pp = (PlayPen) evt.getSource();
		return pp.contentPane.delegateEvent(evt);
	}

	/**
	 * Shows the playpen popup menu if appropriate.
	 */
	public boolean maybeShowPopup(MouseEvent evt) {
		PlayPen pp = (PlayPen) evt.getSource();
		if (evt.isPopupTrigger()) {
			pp.selectNone();
			pp.playPenPopup.show(pp, evt.getX(), evt.getY());
			return true;
		} else {
			return false;
		}
	}

	// ---------- Floating Table Listener ------------
	
	/**
	 * Listens to mouse motion and moves the given component so it
	 * follows the mouse.  When the user lifts the mouse button, it
	 * stops moving the component, and unregisters itself as a
	 * listener.
	 */
	public static class FloatingTableListener extends MouseInputAdapter {
		PlayPen pp;
		TablePane tp;
		Point handle;

		public FloatingTableListener(PlayPen pp, TablePane tp, Point handle) {
			this.pp = pp;
			this.tp = tp;
			this.handle = handle;
			pp.addMouseMotionListener(this);
			pp.addMouseListener(this); // the click that ends this operation
			pp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}

		public void mouseMoved(MouseEvent e) {
			mouseDragged(e);
		}

		public void mouseDragged(MouseEvent e) {
			tp.setVisible(true);
			Point p = new Point(e.getPoint().x - handle.x, e.getPoint().y - handle.y);
			pp.setChildPosition(tp, p);
			pp.repaint(); // FIXME: should use a contentPane approach. it would automatically want to redraw
		}

		/**
		 * Anchors the tablepane and disposes this listener instance.
		 */
		public void mouseReleased(MouseEvent e) {
			cleanup();
		}

		protected void cleanup() {
			pp.setCursor(null);
			pp.removeMouseMotionListener(this);
			pp.removeMouseListener(this);
			pp.revalidate();
		}
	}
}
