package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;

public class PlayPen extends JPanel implements java.io.Serializable, SQLObjectListener {

	private static Logger logger = Logger.getLogger(PlayPen.class);

	/**
	 * Links this PlayPen with an instance of PlayPenDropListener so
	 * users can drop stuff on the playpen.
	 */
	protected DropTarget dt;

	/**
	 * This database is the container of all the SQLObjects in this
	 * playpen.  Items added via the addTable, addSchema, ... methods
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

	public PlayPen(SQLDatabase db) {
		super();
		if (db == null) throw new NullPointerException("db must be non-null");
		this.db = db;
		db.addSQLObjectListener(this);
		setLayout(new PlayPenLayout(this));
		setName("Play Pen");
		setMinimumSize(new Dimension(200,200));
		setBackground(java.awt.Color.white);
		setOpaque(true);
		dt = new DropTarget(this, new PlayPenDropListener());
		tableNames = new HashMap();
		setupTablePanePopup();
	}

	/**
	 * This routine is called by the constructor.  It adds all the
	 * necessary items and action listeners to the TablePane popup
	 * menu.
	 */
	protected void setupTablePanePopup() {
		tablePanePopup = new JPopupMenu();
		JMenuItem mi = new JMenuItem("Insert Column");
		//mi.setAction(new InsertColumnAction());
		tablePanePopup.add(mi);

		mi = new JMenuItem("Edit Column");
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(new DeleteColumnAction(this));
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(new DeleteTableAction(this));
		tablePanePopup.add(mi);

		mi = new JMenuItem("Create Relationship");
		tablePanePopup.add(mi);

		tablePanePopup.addSeparator();
		
		mi = new JMenuItem("Show listeners");
		mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					TablePane tp = (TablePane) getSelectedChild();
					JOptionPane.showMessageDialog(tp, new JScrollPane(new JList(new java.util.Vector(tp.getModel().getSQLObjectListeners()))));
				}
			});
		tablePanePopup.add(mi);
	}

	public static class PlayPenLayout implements LayoutManager2 {

		/**
		 * This is the PlayPen that we are managing the layout for.
		 */
		PlayPen parent;

		public PlayPenLayout(PlayPen parent) {
			this.parent = parent;
		}

		/**
		 * Does nothing.  Use the Object-style constraints, not String.
		 *
		 * @throws UnsupportedOperationException if called.
		 */
		public void addLayoutComponent(String name, Component comp) {
			throw new UnsupportedOperationException("Use addLayoutComponent(Component,Object) instead");
		}

		/**
		 * Positions the new component near the given point.
		 *
		 * @param comp the component which has been added
		 * @param position A java.awt.Point, near which the object
		 * should be positioned.  It will not overlap existing
		 * components in this play pen.  If this argument is null, the
		 * layout manager will do nothing for this component addition.
		 */
		public void addLayoutComponent(Component comp,
									   Object position) {
			if (position == null) return;
			Point pos = (Point) position;
			comp.setSize(comp.getPreferredSize());
			int nh = comp.getHeight();
			int nw = comp.getWidth();
			logger.debug("new comp x="+pos.x+"; y="+pos.y+"; w="+nw+"; h="+nh);

			RangeList rl = new RangeList();
			Rectangle cbounds = null;
			for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
				Component c = parent.getComponent(i);
				if (c.isVisible() && c != comp) {
					cbounds = c.getBounds(cbounds);
					if ( ! ( (cbounds.y+cbounds.height < pos.y) 
							 || (pos.y + nh < cbounds.y)
                           )
                       ) {
						logger.debug("blocking "+c.getName());
						rl.blockOut(cbounds.x, cbounds.width);
					} else {
						logger.debug("IGNORING "+c.getName());
					}
				}
			}
			
			logger.debug("final range list: "+rl);
			logger.debug("rightGap = max("+rl.findGapToRight(pos.x, nw)+","+pos.x+")");

			int rightGap = Math.max(rl.findGapToRight(pos.x, nw), pos.x);
			int leftGap = rl.findGapToLeft(pos.x, nw);

			logger.debug("pos.x = "+pos.x+"; rightGap = "+rightGap+"; leftGap = "+leftGap);
			if (rightGap - pos.x <= pos.x - leftGap) {
				pos.x = rightGap;
			} else {
				pos.x = leftGap;
			}
			comp.setLocation(pos);

			if (pos.x < 0) {
				translateAllComponents(Math.abs(pos.x), 0, false);
			}
			
			parent.scrollRectToVisible(comp.getBounds());
		}

		/**
		 * Translates all components left and down by the specified
		 * amounts.
		 *
		 * @param scrollToCompensate if true, this method tries to
		 * make it appear that the components didn't move by scrolling
		 * the viewport by the same amount as the components were
		 * translated.  If false, no scrolling is attempted.
		 */
		protected void translateAllComponents(int xdist, int ydist, boolean scrollToCompensate) {
			synchronized (parent) {
				Rectangle visibleArea = null;
				if (scrollToCompensate) {
					parent.getVisibleRect();
				}
				
				Point p = new Point();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					JComponent c = (JComponent) parent.getComponent(i);
					p = c.getLocation(p);
					p.x += xdist;
					p.y += ydist;
					c.setLocation(p);
				}
				
				if (scrollToCompensate) {
					visibleArea.x += xdist;
					visibleArea.y += ydist;
					parent.scrollRectToVisible(visibleArea);
				}
			}
		}

		/**
		 * Does nothing.
		 */
		public void removeLayoutComponent(Component comp) {
			logger.debug("PlayPenLayout.removeLayoutComponent");
		}

		/**
		 * Calculates the smallest rectangle that will completely
		 * enclose the visible components inside parent.
		 */
		public Dimension preferredLayoutSize(Container parent) {
			Rectangle cbounds = null;
			//int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = 0, maxy = 0;
			int minx = 0, miny = 0, maxx = 0, maxy = 0;
			for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
				Component c = parent.getComponent(i);
				if (c.isVisible()) {
					cbounds = c.getBounds(cbounds);
					minx = Math.min(cbounds.x, minx);
					miny = Math.min(cbounds.y, miny);
					maxx = Math.max(cbounds.x + cbounds.width , maxx);
					maxy = Math.max(cbounds.y + cbounds.height, maxy);
				}
			}

			Dimension min = parent.getMinimumSize();
			return new Dimension(Math.max(maxx - minx, min.width),
								 Math.max(maxy - miny, min.height));
		}

		/**
		 * Identical to {@link #preferredLayoutSize(Container)}.
		 */
		public Dimension minimumLayoutSize(Container parent) {
			return preferredLayoutSize(parent);
		}

		/**
		 * Identical to {@link #preferredLayoutSize(Container)}.
		 */
		public Dimension maximumLayoutSize(Container target) {
			return preferredLayoutSize(target);
		}

		public float getLayoutAlignmentX(Container target) {
			return 0.5f;
		}

		public float getLayoutAlignmentY(Container target) {
			return 0.5f;
		}

		/**
		 * Discards cached layout information.  Currently this is a no-op.
		 */
		public void invalidateLayout(Container target) {
			return;
		}

		/**
		 * Does nothing!  Components will stay put.
		 */
		public void layoutContainer(Container parent) {
			logger.debug("PlayPenLayout.layoutContainer");
		}

		protected static class RangeList {

			List blocks;

			public RangeList() {
				blocks = new LinkedList();

				// blockOut needs non-empty list with something at far right side
				blocks.add(new Block(Integer.MAX_VALUE, 0));
			}

			public void blockOut(int start, int length) {
				Block block = new Block(start, length);
				//logger.debug("blockOut "+block+": before "+blocks);
				ListIterator it = blocks.listIterator();
				while (it.hasNext()) {
					Block nextBlock = (Block) it.next();
					if (nextBlock.start > start) {
						it.previous();
						it.add(block);
						break;
					}
				}
				//logger.debug("blockOut "+block+": after  "+blocks);
			}

			public int findGapToRight(int start, int length) {
				int origStart = start;
				Iterator it = blocks.iterator();
				while (it.hasNext()) {
					Block block = (Block) it.next();

					if ( (start + length) < block.start ) {
						// current gap fits at right-hand side.. done!
						if (start < origStart) {
							throw new IllegalStateException("Start < origStart!");
						}
						return start;
					} else {
						// increase start past this block if applicable
						start = Math.max(block.start + block.length, start);
					}

				}
				return start;
			}

			public int findGapToLeft(int start, int length) {
				int closestLeftGap = Integer.MIN_VALUE;
				int prevBlockEnd = Integer.MIN_VALUE;
				Iterator it = blocks.iterator();
				while (it.hasNext()) {
					Block block = (Block) it.next();
					if ( (prevBlockEnd < block.start - length)
						 && (block.start - length <= start) ) {
						closestLeftGap = block.start - length;
					}
					if ( block.start > start ) {
						// we have reached a block to the right of start
						break;
					}
					prevBlockEnd = block.start + block.length;
				}

				// if we're still at one of the sentinel values, return the mouse location
 				if (closestLeftGap == Integer.MIN_VALUE) {
 					return start;
 				} else {
					// otherwise, the answer is correct
					return closestLeftGap;
				}
			}
			
			public String toString() {
				return blocks.toString();
			}

			protected static class Block {
				public int start;
				public int length;
				public Block(int start, int length) {
					this.start = start;
					this.length = length;
				}
				public String toString() {
					return "("+start+","+length+")";
				}
			}
		}
	}
	
	/**
	 * Works under limited circumstances. Use {@link #addTable} instead.
	 */
	public void add(Component c, Object constraints) {
		if (constraints instanceof Point) {
			super.add(c, constraints);
		} else {
			throw new IllegalArgumentException("Constraints must be a Point");
		}
	}

	/**
	 * Adds a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 *
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addComponent(Component,Object)
	 */
	public synchronized void addTable(SQLTable source, Point preferredLocation) throws ArchitectException {
		SQLTable newTable = SQLTable.getDerivedInstance(source, db);
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
		super.add(tp, preferredLocation);
		tp.revalidate();
	}

	/**
	 * Calls {@link #addTable} for each table contained in the given schema.
	 */
	public synchronized void addSchema(SQLSchema source, Point preferredLocation) throws ArchitectException {
		AddSchemaTask t = new AddSchemaTask(source, preferredLocation);
		new Thread(t, "Schema-Adder").start();
	}

	/**
	 * Adds the given component to this playpen as a ghost.  A ghost
	 * is a transient object that helps the user visualise drag
	 * events.
	 */
	public synchronized void addGhost(JComponent ghost, Point location) {
		super.add(ghost, null);
		ghost.setLocation(location);
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
				while (it.hasNext()) {
					SQLTable sourceTable = (SQLTable) it.next();
					pm.setNote(sourceTable.getTableName());
					addTable(sourceTable, preferredLocation);
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

	// -------------------- SQLOBJECT EVENT SUPPORT ---------------------

	/**
	 * Listens for property changes in the model (tables
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		firePropertyChange("model.children", null, null);
		revalidate();
	}

	/**
	 * Listens for property changes in the model (columns
	 * removed).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		firePropertyChange("model.children", null, null);
		revalidate();
	}

	/**
	 * Listens for property changes in the model (columns
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
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		firePropertyChange("model.children", null, null);
		revalidate();
	}

	// --------------- SELECTION METHODS ----------------

	/**
	 * Deselects all selectable items in the PlayPen.
	 */
	public void selectNone() {
		for (int i = 0, n = getComponentCount(); i < n; i++) {
			if (getComponent(i) instanceof Selectable) {
				Selectable s = (Selectable) getComponent(i);
				s.setSelected(false);
			}
		}
	}

	/**
	 * Returns the first selected child in the PlayPen.
	 */
	public Selectable getSelectedChild() {
		for (int i = 0, n = getComponentCount(); i < n; i++) {
			if (getComponent(i) instanceof Selectable) {
				Selectable s = (Selectable) getComponent(i);
				if (s.isSelected()) return s;
			}
		}
		return null;
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
					Object someData = t.getTransferData(importFlavor);
					logger.debug("MyJTreeTransferHandler.importData: got object of type "+someData.getClass().getName());
					if (someData instanceof SQLTable) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						c.addTable((SQLTable) someData, dtde.getLocation());
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
								c.addTable(sourceTable, dtde.getLocation());
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
					} else if (someData instanceof SQLObject[]) {
						// needs work (should use addSchema())
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						SQLObject[] objects = (SQLObject[]) someData;
						for (int i = 0; i < objects.length; i++) {
							if (objects[i] instanceof SQLTable) {
								TablePane tp = new TablePane((SQLTable) objects[i]);
								c.add(tp, dtde.getLocation());
								tp.revalidate();
							}
						}
						dtde.dropComplete(true);
						return;
					} else {
						dtde.rejectDrop();
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


 				if (flavors[i].equals(SQLObjectTransferable.flavor)
					|| flavors[i].equals(SQLObjectListTransferable.flavor)) {
					logger.debug("YES");
 					return flavors[i];
				}
 			}
			logger.debug("NO!");
 			return null;
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
}
