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
	implements java.io.Serializable, SQLObjectListener, SelectionListener, ContainerListener {

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
	 * The child components of this playpen.
	 */
	protected ArrayList ppChildren;

	public PlayPen() {
		zoom = 1.0;
		ppChildren = new ArrayList();
		setLayout(new PlayPenLayout(this));
		setName("Play Pen");
		setMinimumSize(new Dimension(1,1));
		setBackground(java.awt.Color.white);
		dt = new DropTarget(this, new PlayPenDropListener());
		addContainerListener(this);
		setupTablePanePopup();
		setupPlayPenPopup();
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

	// --------------------- accessors and mutators ----------------------

	public void setZoom(double newZoom) {
		if (newZoom != zoom) {
			double oldZoom = zoom;
			zoom = newZoom;
			firePropertyChange("zoom", oldZoom, newZoom);
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
		Iterator it = ppChildren.iterator();
		while (it.hasNext()) {
			Component c = (Component) it.next();
			if (c.isVisible()) {
				cbounds = c.getBounds(cbounds);
				minx = Math.min(cbounds.x, minx);
				miny = Math.min(cbounds.y, miny);
				maxx = Math.max(cbounds.x + cbounds.width , maxx);
				maxy = Math.max(cbounds.y + cbounds.height, maxy);
			}
		}
		
		Dimension min = getMinimumSize();
		return new Dimension(Math.max(maxx - minx, min.width),
							 Math.max(maxy - miny, min.height));
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform backup = g2.getTransform();
		g2.scale(zoom, zoom);
		AffineTransform zoomedOrigin = g2.getTransform();
		super.paintComponent(g2);
		Iterator it = ppChildren.iterator();
		while (it.hasNext()) {
			Component c = (Component) it.next();
			logger.debug("painting "+c);
			if (c.isVisible()) {
				g2.translate(c.getLocation().x, c.getLocation().y);
				c.paint(g2);
				g2.setTransform(zoomedOrigin);
			}
		}
		g2.setTransform(backup);
	}

	/**
	 * Adds the given component to this PlayPen.  Does NOT add it to
	 * the Swing containment hierarchy. The playpen is a leaf in the
	 * hierarchy as far as swing is concerned. Only accepts
	 * Relationship and TablePane components.
	 */
	public void add(Component c, Object constraints) {
		if (c instanceof Relationship) {
			ppChildren.add(c);
		} else if (c instanceof TablePane) {
			if (constraints instanceof Point) {
				ppChildren.add(c);
				c.setLocation((Point) constraints);
			} else {
				throw new IllegalArgumentException("Constraints must be a Point");
			}
		} else {
			throw new IllegalArgumentException("TablePane can't contain components of type "
											   +c.getClass().getName());
		}
		c.setFont(getFont());
		logger.debug("Set font to "+getFont());
		c.setBackground(getBackground());
		logger.debug("Set background to "+getBackground());
		c.setForeground(getForeground());
		logger.debug("Set foreground to "+getForeground());
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
		addMouseListener(new PopupListener());
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
		for (int i = 0, n = getComponentCount(); i < n; i++) {
			if (getComponent(i) instanceof Relationship) {
				relationships.add(getComponent(i));
			}
		}
		return relationships;
	}

	/**
	 * Adds a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 *
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addComponent(Component,Object)
	 */
	public synchronized void importTableCopy(SQLTable source, Point preferredLocation) throws ArchitectException {
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
			this.preferredLocation = preferredLocation;
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
					importTableCopy(sourceTable, preferredLocation);
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
		new FloatingTableListener(this, tp, new Point(tp.getSize().width/2,0));
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
				for (int j = 0; j < getComponentCount(); j++) {
					if (getComponent(j) instanceof TablePane) {
						TablePane tp = (TablePane) getComponent(j);
						if (selectedChild == tp) selectedChild = null;
						if (tp.getModel() == c[i]) {
							remove(j);
							fireEvent = true;
						}
					}
				}
			} else if (c[i] instanceof SQLRelationship) {
				for (int j = 0; j < getComponentCount(); j++) {
					if (getComponent(j) instanceof Relationship) {
						Relationship r = (Relationship) getComponent(j);
						if (selectedChild == r) selectedChild = null;
						if (r.getModel() == c[i]) {
							remove(j);
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

	// FIXME: this doesn't work anymore because children aren't swing children

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
	 * at the current mouse position.
	 */
	public static class PlayPenDropListener implements DropTargetListener {

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
			//logger.debug("PlayPenDropTarget.dragOver()");
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		
		/**
		 * Called when the drag operation has terminated with a drop on
		 * the operable part of the drop site for the DropTarget
		 * registered with this listener.
		 */
		public void drop(DropTargetDropEvent dtde) {
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
					while (pathIt.hasNext()) {
						Object someData = dbtree.getNodeForDnDPath((int[]) pathIt.next());
						
						if (someData instanceof SQLTable) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							c.importTableCopy((SQLTable) someData, dtde.getLocation());
							dtde.dropComplete(true);
							return;
						} else if (someData instanceof SQLSchema) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							SQLSchema sourceSchema = (SQLSchema) someData;
							c.addSchema(sourceSchema, dtde.getLocation());
							dtde.dropComplete(true);
							return;
						} else if (someData instanceof SQLCatalog) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							SQLCatalog sourceCatalog = (SQLCatalog) someData;
							Iterator cit = sourceCatalog.getChildren().iterator();
							if (sourceCatalog.isSchemaContainer()) {
								while (cit.hasNext()) {
									SQLSchema sourceSchema = (SQLSchema) cit.next();
									c.addSchema(sourceSchema, dtde.getLocation());
								}
							} else {
								while (cit.hasNext()) {
									SQLTable sourceTable = (SQLTable) cit.next();
									c.importTableCopy(sourceTable, dtde.getLocation());
								}
							}
							dtde.dropComplete(true);
							return;
						} else if (someData instanceof SQLColumn) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							SQLColumn column = (SQLColumn) someData;
							JLabel colName = new JLabel(column.getColumnName());
							colName.setSize(colName.getPreferredSize());
							c.add(colName, dtde.getLocation());
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

	public static class PopupListener extends MouseAdapter {

		public void mouseClicked(MouseEvent evt) {
		}

		public void mousePressed(MouseEvent evt) {
			maybeShowPopup(evt);
		}

		public void mouseReleased(MouseEvent evt) {
			((PlayPen) evt.getSource()).selectNone();
			maybeShowPopup(evt);
		}

		public void maybeShowPopup(MouseEvent evt) {
			if (evt.isPopupTrigger() && !evt.isConsumed()) {
				PlayPen pp = (PlayPen) evt.getSource();
				pp.selectNone();
				pp.playPenPopup.show(pp, evt.getX(), evt.getY());
			}
		}
	}
	
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
			tp.setLocation(p);
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
