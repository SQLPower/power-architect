package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;

public class TablePane extends JComponent implements SQLObjectListener, java.io.Serializable {

	private static final Logger logger = Logger.getLogger(TablePane.class);

	protected DragGestureListener dgl;
	protected DragGestureRecognizer dgr;
	protected DragSource ds;

	/**
	 * A constant indicating the title label on a TablePane.
	 */
	public static final int COLUMN_INDEX_TITLE = -1;

	/**
	 * A constant indicating no column or title.
	 */
	public static final int COLUMN_INDEX_NONE = -2;

	/**
	 * This is the column index at which to the insertion point is
	 * currently rendered. Columns will be added after this column.
	 * If it is COLUMN_INDEX_NONE, no insertion point will be
	 * rendered and columns will be added at the bottom.
	 */
	protected int insertionPoint;

	/**
	 * How many pixels should be left between the surrounding box and
	 * the column name labels.
	 */
	protected Insets margin = new Insets(1,1,1,1);

	protected DropTarget dt;

	static {
		UIManager.put(TablePaneUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.BasicTablePaneUI");
	}

	private SQLTable model;

	public TablePane(SQLTable m) {
		setModel(m);
		setMinimumSize(new Dimension(100,200));
		setPreferredSize(new Dimension(100,200));
		dt = new DropTarget(this, new TablePaneDropListener());

		dgl = new TablePaneDragGestureListener();
		ds = new DragSource();
		dgr = getToolkit().createDragGestureRecognizer(MouseDragGestureRecognizer.class, ds, this, DnDConstants.ACTION_MOVE, dgl);
		setInsertionPoint(COLUMN_INDEX_NONE);
		updateUI();
	}

	public void setUI(TablePaneUI ui) {super.setUI(ui);}

    public void updateUI() {
		setUI((TablePaneUI)UIManager.getUI(this));
		invalidate();
    }

    public String getUIClassID() {
        return TablePaneUI.UI_CLASS_ID;
    }

	/**
	 * You must call this method when you are done with a TablePane
	 * component.  It unregisters this instance (and its UI delegate)
	 * on all event listener lists on which it was previously
	 * registered.
	 */
	public void destroy() {
		model.removeSQLObjectListener(this);
	}

	// -------------------- sqlobject event support ---------------------

	/**
	 * Listens for property changes in the model (columns
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

	// ----------------------- accessors and mutators --------------------------
	
	/**
	 * Gets the value of model
	 *
	 * @return the value of model
	 */
	public SQLTable getModel()  {
		return this.model;
	}

	/**
	 * Sets the value of model, removing this TablePane as a listener
	 * on the old model and installing it as a listener to the new
	 * model.
	 *
	 * @param argModel Value to assign to this.model
	 */
	public void setModel(SQLTable m) {
		SQLTable old = model;
        if (old != null) {
			old.removeSQLObjectListener(this);
		}

        if (m == null) {
			throw new IllegalArgumentException("model may not be null");
		} else {
            model = m;
		}
		model.addSQLObjectListener(this);
		setName("TablePanel: "+model.getShortDisplayName());

        firePropertyChange("model", old, model);
	}

	/**
	 * Gets the value of margin
	 *
	 * @return the value of margin
	 */
	public Insets getMargin()  {
		return this.margin;
	}

	/**
	 * Sets the value of margin
	 *
	 * @param argMargin Value to assign to this.margin
	 */
	public void setMargin(Insets argMargin) {
		Insets old = margin;
		this.margin = (Insets) argMargin.clone();
		firePropertyChange("margin", old, margin);
		revalidate();
	}

	public int getInsertionPoint() {
		return insertionPoint;
	}

	public void setInsertionPoint(int ip) {
		int old = insertionPoint;
		this.insertionPoint = ip;
		if (ip != old) {
			firePropertyChange("insertionPoint", old, insertionPoint);
			repaint();
		}
	}
	
	// ------------------ utility methods ---------------------

	/**
	 * Returns the index of the column that point p is on top of.  If
	 * p is on top of the table name, returns COLUMN_INDEX_TITLE.
	 * Otherwise, p is not over a column or title and the returned
	 * index is COLUMN_INDEX_NONE.
	 */
	public int pointToColumnIndex(Point p) throws ArchitectException {
		return ((TablePaneUI) ui).pointToColumnIndex(p);
	}

	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.
	 */
	public static class TablePaneDropListener implements DropTargetListener {

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
			((TablePane) dte.getDropTargetContext().getComponent()).setInsertionPoint(COLUMN_INDEX_NONE);
		}
		
		/**
		 * Called when a drag operation is ongoing, while the mouse
		 * pointer is still over the operable part of the drop site for
		 * the DropTarget registered with this listener.
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
			try {
				TablePane tp = (TablePane) dtde.getDropTargetContext().getComponent();
				tp.setInsertionPoint(tp.pointToColumnIndex(dtde.getLocation()));
			} catch (ArchitectException e) {
				logger.error("Got exception translating drag location", e);
			}
		}
		
		/**
		 * Called when the drag operation has terminated with a drop on
		 * the operable part of the drop site for the DropTarget
		 * registered with this listener.
		 */
		public void drop(DropTargetDropEvent dtde) {
			Transferable t = dtde.getTransferable();
			TablePane c = (TablePane) dtde.getDropTargetContext().getComponent();
			DataFlavor importFlavor = bestImportFlavor(c, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
				c.setInsertionPoint(COLUMN_INDEX_NONE);
			} else {
				try {
					Object someData = t.getTransferData(importFlavor);
					logger.debug("drop: got object of type "+someData.getClass().getName());
					if (someData instanceof SQLTable) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						c.getModel().inherit(c.pointToColumnIndex(dtde.getLocation()),
											 (SQLTable) someData);
						dtde.dropComplete(true);
						return;
					} else if (someData instanceof SQLColumn) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						SQLColumn column = (SQLColumn) someData;
						c.getModel().addColumn(c.pointToColumnIndex(dtde.getLocation()),
											   column);
						logger.debug("Added "+column.getColumnName()+" to table");
						dtde.dropComplete(true);
						return;
					} else if (someData instanceof SQLObject[]) {
						// needs work (should use addSchema())
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						SQLObject[] objects = (SQLObject[]) someData;
						int ip = c.pointToColumnIndex(dtde.getLocation());
						for (int i = 0; i < objects.length; i++) {
							if (objects[i] instanceof SQLColumn) {
								c.getModel().addColumn(ip + i, (SQLColumn) objects[i]);
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
				} finally {
					c.setInsertionPoint(COLUMN_INDEX_NONE);
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
			logger.debug("can I import "+Arrays.asList(flavors));
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

	public static class TablePaneDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {
			TablePane tp = (TablePane) dge.getComponent();
			int colIndex = COLUMN_INDEX_NONE;
			try {
				colIndex = tp.pointToColumnIndex(dge.getDragOrigin());
			} catch (ArchitectException e) {
				logger.error("Got exception while translating drag point", e);
			}
			logger.debug("Recognized drag gesture! col="+colIndex);
			if (colIndex == COLUMN_INDEX_TITLE) {
				TablePaneMover tpm = new TablePaneMover(tp, dge.getDragOrigin());
			} else if (colIndex >= 0) {
				// export column as DnD event
				logger.error("Dragging columns is not implemented yet");
			}
		}
	}

	/**
	 * The TablePaneMover class listens to mouse drag events and moves
	 * a ghost image of the TablePane around on the playpen.  When the
	 * mouse button is released, it moves the original TablePane,
	 * destroys the ghost component, and unregisters itself as a mouse
	 * listener.
	 */
	public static class TablePaneMover extends MouseInputAdapter {

		/**
		 * Used during move operations on the TablePane.  This ghost
		 * is a clone of the TablePane that we will move around in
		 * response to mouse drags.
		 *
		 * <p>XXX: It might be a better idea to change the ghost from
		 * being an actual TablePane to being a special Ghost class
		 * that is simply a picture of the original table pane.  It
		 * would be much lower overhead to drag such an image around
		 * the screen.  Also, there would be no worries about multiple
		 * ghosts accidentally accumulating on listener lists.
		 */
		public JComponent ghost;

		/**
		 * This is the location inside the moving component where the
		 * user grabbed it.
		 */
		public Point dragStart;

		/**
		 * Creates a ghost image of the given TablePane, hides the
		 * original TablePane, and adds this object to the TablePane
		 * as a mouse listener.
		 */
		public TablePaneMover(TablePane tp, Point dragStart) {
			this.dragStart = dragStart;
			tp.setVisible(false);
			ghost = new TablePane(tp.getModel());
			ghost.setFont(tp.getFont());  // XXX: this shouldn't be necessary (but it is!)
			ghost.setName(tp.getName()+" GHOST");
			((PlayPen) tp.getParent()).addGhost(ghost, tp.getLocation());
			tp.addMouseListener(this);
			tp.addMouseMotionListener(this);
		}

		/**
		 * Updates the location of the ghost.
		 */
		public void mouseDragged(MouseEvent evt) {
			TablePane tp = (TablePane) evt.getComponent();
			Point p = tp.getLocation();
			logger.debug("Moving. start="+p+"; evt location="+evt.getPoint());
			ghost.setLocation(p.x - dragStart.x + evt.getX(), p.y - dragStart.y + evt.getY());
		}
		
		/**
		 * Destroys the ghost, moves the original TablePane, and makes
		 * it visible again.
		 */
		public void mouseReleased(MouseEvent evt) {
			TablePane tp = (TablePane) evt.getComponent();
			Point p = tp.getLocation();
			tp.getParent().remove(ghost);
			((TablePane) ghost).destroy();
			ghost = null;  // XXX: not necessary since this TPM object will die soon
			tp.removeMouseListener(this);
			tp.removeMouseMotionListener(this);
			PlayPen playPen = (PlayPen) tp.getParent();
			playPen.remove(tp);
			Point location = new Point(p.x - dragStart.x + evt.getX(),
									   p.y - dragStart.y + evt.getY());
			tp.setVisible(true);
			playPen.add(tp, location);
			playPen.repaint();
		}
	}

}
