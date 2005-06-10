package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;

public class TablePane 
	extends PlayPenComponent 
	implements SQLObjectListener, java.io.Serializable, Selectable, DragSourceListener, MouseListener {

	private static final Logger logger = Logger.getLogger(TablePane.class);

    /**
     * The playpen this component lives in.
     */
    private PlayPen parentPP;

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

	/**
	 * A selected TablePane is one that the user has clicked on.  It
	 * will appear more prominently than non-selected TablePanes.
	 */
	protected boolean selected;

	protected DropTarget dt;

	protected ArrayList columnSelection;

	/**
	 * During a drag operation where a column is being dragged from
	 * this TablePane, this variable points to the column being
	 * dragged.  At all other times, it should be null.
	 */
	protected SQLColumn draggingColumn;

	/** 
     * used by mouseReleased to figure out if a DND operation just took place in the
     * playpen, so it can make a good choice about leaving a group of things selected
     * or deselecting everything except the TablePane that was clicked on.
     */
	protected static boolean draggingTablePanes = false;

	static {
		UIManager.put(TablePaneUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.BasicTablePaneUI");
	}

	private SQLTable model;

    private PropertyChangeListener propertyChangeListener;

	public TablePane(SQLTable m, PlayPen parentPP) {
	    this.parentPP = parentPP;
		setModel(m);

		setOpaque(true);
		dt = new DropTarget(this, new TablePaneDropListener(this));

		dgl = new TablePaneDragGestureListener();
		ds = new DragSource();
		dgr = ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dgl);
		logger.info("motion threshold is: " + getToolkit().getDesktopProperty("DnD.gestureMotionThreshold"));		

		setInsertionPoint(COLUMN_INDEX_NONE);

		addMouseListener(this);

		setCurrentFontRenderContext(parentPP.getFontRenderContext());
		propertyChangeListener = new TPPropertyChangeListener();
		parentPP.addPropertyChangeListener(propertyChangeListener);

		updateUI();
	}


	// ---------------------- JComponent Overrides ----------------------
	// see also PlayPenComponent

	public void setUI(TablePaneUI ui) { super.setUI(ui); }

    public void updateUI() {
		setUI((TablePaneUI) UIManager.getUI(this));
		invalidate();
    }

    public String getUIClassID() {
        return TablePaneUI.UI_CLASS_ID;
    }

	public Point getLocationOnScreen() {
		Point p = new Point();
		PlayPen pp = getPlayPen();
		getLocation(p);
		pp.zoomPoint(p);
		SwingUtilities.convertPointToScreen(p, pp);
		return p;
	}
	
	// ---------------------- utility methods ----------------------

	/**
	 * You must call this method when you are done with a TablePane
	 * component.  It unregisters this instance (and its UI delegate)
	 * on all event listener lists on which it was previously
	 * registered.
	 *
	 * <p>FIXME: this should be done automatically when the SQLTable
	 * model is removed, because the TablePane shouldn't have to be
	 * destroyed separately of the model.
	 */
	public void destroy() {
		try {
			ArchitectUtils.unlistenToHierarchy(this, model);
		} catch (ArchitectException e) {
			logger.error("Caught exception while unlistening to all children", e);
		}
	}
	

	// -------------------- sqlobject event support ---------------------

	/**
	 * Listens for property changes in the model (columns
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		int ci[] = e.getChangedIndices();
		for (int i = 0; i < ci.length; i++) {
			columnSelection.add(ci[i], Boolean.FALSE);
		}
		try {
			ArchitectUtils.listenToHierarchy(this, e.getChildren());
		} catch (ArchitectException ex) {
			logger.error("Caught exception while listening to added children", ex);
		}
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
		if (e.getSource() == this.model.getColumnsFolder()) {
			int ci[] = e.getChangedIndices();
			for (int i = 0; i < ci.length; i++) {
				columnSelection.remove(ci[i]);
			}
			if (columnSelection.size() > 0) {
				selectNone();
				columnSelection.set(Math.min(ci[0], columnSelection.size()-1), Boolean.TRUE);
			}
		}
		try {
			ArchitectUtils.unlistenToHierarchy(this, e.getChildren());
			if (columnSelection.size() != this.model.getColumns().size()) {
				logger.error("Selection list and children are out of sync: selection="+columnSelection+"; children="+this.model.getColumns());
			}
		} catch (ArchitectException ex) {
			logger.error("Couldn't remove children", ex);
			JOptionPane.showMessageDialog(this, "Couldn't delete column: "+ex.getMessage());
		}
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
		repaint();
	}

	/**
	 * Listens for property changes in the model (significant
	 * structure change).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		if (e.getSource() == model.getColumnsFolder()) {
			int numCols = e.getChildren().length;
			columnSelection = new ArrayList(numCols);
			for (int i = 0; i < numCols; i++) {
				columnSelection.add(Boolean.FALSE);
			}
			firePropertyChange("model.children", null, null);
			revalidate();
		}
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
	 * @param m the new table model
	 */
	public void setModel(SQLTable m) {
		SQLTable old = model;
        if (old != null) {
			try {
				ArchitectUtils.listenToHierarchy(this, old);
			} catch (ArchitectException e) {
				logger.error("Caught exception while unlistening to old model", e);
			}
		}

        if (m == null) {
			throw new IllegalArgumentException("model may not be null");
		} else {
            model = m;
		}

		try {
			columnSelection = new ArrayList(m.getColumns().size());
			for (int i = 0; i < m.getColumns().size(); i++) {
				columnSelection.add(Boolean.FALSE);
			}
		} catch (ArchitectException e) {
			logger.error("Error getting children on new model", e);
		}

		try {
			ArchitectUtils.listenToHierarchy(this, model);
		} catch (ArchitectException e) {
			logger.error("Caught exception while listening to new model", e);
		}
		setName("TablePane: "+model.getShortDisplayName());

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

	/**
	 * See {@link #insertionPoint}.
	 */
	public int getInsertionPoint() {
		return insertionPoint;
	}

	/**
	 * See {@link #insertionPoint}.
	 */
	public void setInsertionPoint(int ip) {
		int old = insertionPoint;
		this.insertionPoint = ip;
		if (ip != old) {
			firePropertyChange("insertionPoint", old, insertionPoint);
			repaint();
		}
	}
	
	/**
	 * See {@link #selected}.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * See {@link #selected}.
	 */
	public void setSelected(boolean isSelected) {
		if (isSelected == false) {
			selectNone();
		}
		if (selected != isSelected) {
			selected = isSelected;
			fireSelectionEvent(new SelectionEvent(this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT));
			repaint();
		}
	}

	public DropTarget getDropTarget() {
		return dt;
	}

	// --------------------- column selection support --------------------

	public void selectNone() {
		for (int i = 0; i < columnSelection.size(); i++) {
			columnSelection.set(i, Boolean.FALSE);
		}
	}
	
	/**
	 * @param i The column to select.  If less than 0, {@link
	 * #selectNone()} is called rather than selecting a column.
	 */
	public void selectColumn(int i) {
		if (i < 0) {
			selectNone();
			return;
		}
		columnSelection.set(i, Boolean.TRUE);
		repaint();
	}

	public boolean isColumnSelected(int i) {
		try {
			return ((Boolean) columnSelection.get(i)).booleanValue();
		} catch (IndexOutOfBoundsException ex) {
			logger.error("Couldn't determine selected status of col "+i+" on table "+model.getName());
			return false;
		}
	}

	/**
	 * Returns the index of the first selected column, or
	 * COLUMN_INDEX_NONE if there are no selected columns.
	 */
	public int getSelectedColumnIndex() {
		ListIterator it = columnSelection.listIterator();
		while (it.hasNext()) {
			if (((Boolean) it.next()).booleanValue() == true) {
				return it.previousIndex();
			}
		}
		return COLUMN_INDEX_NONE;
	}

	// --------------------- SELECTION EVENT SUPPORT ---------------------

	protected LinkedList selectionListeners = new LinkedList();

    private FontRenderContext currentFontRederContext;

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}
	
	protected void fireSelectionEvent(SelectionEvent e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Notifying "+selectionListeners.size()
						 +" listeners of selection change");
		}
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

	// ------------------------ DROP TARGET LISTENER ------------------------

	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.
	 */
	public static class TablePaneDropListener implements DropTargetListener {
		
		protected TablePane tp;

		public TablePaneDropListener(TablePane tp) {
			this.tp = tp;
		}

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer enters the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 *
		 * <p>NOTE: This method is expected to be called from the
		 * PlayPen's dragOver method (not directly by Swing), and as
		 * such the DropTargetContext (and the mouse co-ordinates)
		 * will be of the PlayPen.
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
			if (logger.isDebugEnabled()) {
				logger.debug("DragEnter event on "+tp.getName());
			}
		}
		
		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer has exited the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 *
		 * <p>NOTE: This method is expected to be called from the
		 * PlayPen's dragOver method (not directly by Swing), and as
		 * such the DropTargetContext (and the mouse co-ordinates)
		 * will be of the PlayPen.
		 */
		public void dragExit(DropTargetEvent dte) {
			if (logger.isDebugEnabled()) {
				logger.debug("DragExit event on "+tp.getName());
			}
			tp.setInsertionPoint(COLUMN_INDEX_NONE);
		}
		
		/**
		 * Called when a drag operation is ongoing, while the mouse
		 * pointer is still over the operable part of the drop site for
		 * the DropTarget registered with this listener.
		 *
		 * <p>NOTE: This method is expected to be called from the
		 * PlayPen's dragOver method (not directly by Swing), and as
		 * such the DropTargetContext (and the mouse co-ordinates)
		 * will be of the PlayPen.
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			if (logger.isDebugEnabled()) {
				logger.debug("DragOver event on "+tp.getName()+": "+dtde);
				logger.debug("Drop Action = "+dtde.getDropAction());
				logger.debug("Source Actions = "+dtde.getSourceActions());
			}
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE & dtde.getDropAction());
			try {
				Point loc = tp.getPlayPen().unzoomPoint(new Point(dtde.getLocation()));
				loc.x -= tp.getX();
				loc.y -= tp.getY();
				int idx = tp.pointToColumnIndex(loc);
				if (idx < 0) idx = 0;
				tp.setInsertionPoint(idx);
			} catch (ArchitectException e) {
				logger.error("Got exception translating drag location", e);
			}
		}
		
		/**
		 * Called when the drag operation has terminated with a drop on
		 * the operable part of the drop site for the DropTarget
		 * registered with this listener.
		 *
		 * <p>NOTE: This method is expected to be called from the
		 * PlayPen's dragOver method (not directly by Swing), and as
		 * such the DropTargetContext (and the mouse co-ordinates)
		 * will be of the PlayPen.
		 */
		public void drop(DropTargetDropEvent dtde) {
			Point loc = tp.getPlayPen().unzoomPoint(new Point(dtde.getLocation()));
			loc.x -= tp.getX();
			loc.y -= tp.getY();

			logger.debug("Drop target drop event on "+tp.getName()+": "+dtde);
			Transferable t = dtde.getTransferable();
			DataFlavor importFlavor = bestImportFlavor(tp, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
				tp.setInsertionPoint(COLUMN_INDEX_NONE);
			} else {
				try {
					DBTree dbtree = ArchitectFrame.getMainInstance().dbTree;  // XXX: bad
					int insertionPoint = tp.pointToColumnIndex(loc);
					if (insertionPoint < 0) insertionPoint = 0;
					ArrayList paths = (ArrayList) t.getTransferData(importFlavor);
					logger.debug("Importing items from tree: "+paths);
					Iterator pathIt = paths.iterator();
					while (pathIt.hasNext()) {
						Object someData = dbtree.getNodeForDnDPath((int[]) pathIt.next());
						logger.debug("drop: got object of type "+someData.getClass().getName());
						if (someData instanceof SQLTable) {
							SQLTable table = (SQLTable) someData;
							if (table.getParentDatabase() == tp.getModel().getParentDatabase()) {
								// can't import table from target into target!!
								dtde.rejectDrop();
							} else {
								dtde.acceptDrop(DnDConstants.ACTION_COPY);
								tp.getModel().inherit(insertionPoint, table);
								dtde.dropComplete(true);
							}
						} else if (someData instanceof SQLColumn) {
							SQLColumn col = (SQLColumn) someData;
							if (col.getParentTable() == tp.getModel()) {
								// moving column inside the same table
								dtde.acceptDrop(DnDConstants.ACTION_MOVE);
								int oldIndex = col.getParent().getChildren().indexOf(col);
 								if (insertionPoint > oldIndex) {
 									insertionPoint--;
 								}
								tp.getModel().changeColumnIndex(oldIndex, insertionPoint);
								dtde.dropComplete(true);
							} else if (col.getParentTable().getParentDatabase()
								== tp.getModel().getParentDatabase()) {
								// moving column within playpen  
								dtde.acceptDrop(DnDConstants.ACTION_MOVE);
								// FIXME: change this to loop and support multiple column moves in the playpen?
                                // this might be very confusing for the user, so I'm not sure if we should support 
                                // this action.  It might be a better idea to forbid DnD if we detect that more 
                                // than one column is selected.
								col.getParentTable().removeColumn(col);
								logger.debug("Moving column '"+col.getName()
											 +"' to table '"+tp.getModel().getName()
											 +"' at position "+insertionPoint);
								tp.getModel().addColumn(insertionPoint, col);
								dtde.dropComplete(true);
							} else {
								// importing column from a source database
								dtde.acceptDrop(DnDConstants.ACTION_COPY);
								tp.getModel().inherit(insertionPoint, col);
								logger.debug("Inherited "+col.getColumnName()+" to table");
								dtde.dropComplete(true);
							}
						} else {
							dtde.rejectDrop();
						}
					}
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(tp, "Drop failed: "+ex.getMessage());
					logger.error("Error processing drop operation", ex);
					dtde.rejectDrop();
				} finally {
					tp.setInsertionPoint(COLUMN_INDEX_NONE);
				}
			}
		}
		
		/**
		 * Called if the user has modified the current drop gesture.
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
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


 				if (flavors[i].equals(DnDTreePathTransferable.TREEPATH_ARRAYLIST_FLAVOR)) {
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

			// XXX: this should not be necessary, but the recognizer
			// seems to see these points in PlayPen-space rather than
			// TablePane space!
			Point dragOrigin = tp.getPlayPen().unzoomPoint(new Point(dge.getDragOrigin()));
			dragOrigin.x -= tp.getX();
			dragOrigin.y -= tp.getY();

			// ignore drag events that aren't from the left mouse button
			if (dge.getTriggerEvent() instanceof MouseEvent
			   && (dge.getTriggerEvent().getModifiers() & InputEvent.BUTTON1_MASK) == 0)
				return;
			
			try {
				colIndex = tp.pointToColumnIndex(dragOrigin);
			} catch (ArchitectException e) {
				logger.error("Got exception while translating drag point", e);
			}
			logger.debug("Recognized drag gesture on "+tp.getName()+"! origin="+dragOrigin
						 +"; col="+colIndex);

			try {
				logger.debug("DGL: colIndex="+colIndex+",columnsSize="+tp.model.getColumns().size());
				if (colIndex == COLUMN_INDEX_TITLE) {
					// we don't use this because it often misses drags
					// that start near the edge of the titlebar
					logger.debug("Discarding drag on titlebar (handled by mousePressed())");
					draggingTablePanes = true;
				} else if (colIndex >= 0 && colIndex < tp.model.getColumns().size()) {
					// export column as DnD event
					if (logger.isDebugEnabled()) { 
						logger.debug("Exporting column "+colIndex+" with DnD");
					}
					tp.draggingColumn = tp.model.getColumn(colIndex);
					DBTree tree = ArchitectFrame.getMainInstance().dbTree;
					int[] path = tree.getDnDPathToNode(tp.draggingColumn);
					if (logger.isDebugEnabled()) {
						StringBuffer array = new StringBuffer();
						for (int i = 0; i < path.length; i++) {
							array.append(path[i]);
							array.append(",");
						}
						logger.debug("Path to dragged node: "+array);
					}
					// export list of DnD-type tree paths
					ArrayList paths = new ArrayList(1);
					paths.add(path);
					logger.info("DBTree: exporting 1-item list of DnD-type tree path");
					dge.getDragSource().startDrag
						(dge, 
						 null, //DragSource.DefaultCopyNoDrop, 
						 new DnDTreePathTransferable(paths),
						 tp);
				}
			} catch (ArchitectException ex) {
				logger.error("Couldn't drag column", ex);
				JOptionPane.showMessageDialog(tp, "Can't drag column: "+ex.getMessage());
			}
		}
	}

	// ---------------------- MOUSE LISTENER ----------------------

	/**
	 * Double-click support.
	 */
	public void mouseClicked(MouseEvent evt) {
		if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
			TablePane tp = (TablePane) evt.getSource();
			if (evt.getClickCount() == 2) { // double click
				if (tp.isSelected()) {
					ArchitectFrame af = ArchitectFrame.getMainInstance();
					int selectedColIndex = tp.getSelectedColumnIndex();
					if (selectedColIndex == COLUMN_INDEX_NONE) {
						af.editTableAction.actionPerformed
							(new ActionEvent(tp, ActionEvent.ACTION_PERFORMED, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN));
					} else if (selectedColIndex >= 0) {
						af.editColumnAction.actionPerformed
							(new ActionEvent(tp, ActionEvent.ACTION_PERFORMED, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN));
					}
				}
			}
		}
	}
	
	public void mousePressed(MouseEvent evt) {
		evt.getComponent().requestFocus();
		// make sure it was a left click?
		if ((evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
			TablePane tp = (TablePane) evt.getSource();
			// dragging
			try {
				PlayPen pp = (PlayPen) tp.getPlayPen();
				int clickCol = tp.pointToColumnIndex(evt.getPoint());		

				logger.debug("MP: clickCol="+clickCol+",columnsSize="+tp.model.getColumns().size());

				if ( (evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == 0) {
     				// 1. unconditionally de-select everything if this table is unselected					
					// 2. if the table was selected, de-select everything if the click was not on the 
                    //    column header of the table
					if (!tp.isSelected() || (clickCol > COLUMN_INDEX_TITLE && clickCol < tp.model.getColumns().size()) ) {
						pp.selectNone();
					}						
				}

				// re-select the table pane (fire new selection event when appropriate)
				tp.setSelected(true);

				// de-select columns if shift and ctrl were not pressed				
				if ( (evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == 0) {								
					tp.selectNone();
				}

				// select current column unconditionally
				if (clickCol < tp.model.getColumns().size()) {
					tp.selectColumn(clickCol);
				}

				// handle drag (but not if createRelationshipAction is active!)				
				if (clickCol == COLUMN_INDEX_TITLE && !ArchitectFrame.getMainInstance().createRelationshipIsActive()) {
					Iterator it = getPlayPen().getSelectedTables().iterator();
					logger.debug("event point: " + evt.getPoint());
					logger.debug("zoomed event point: " + getPlayPen().zoomPoint(evt.getPoint()));
					while (it.hasNext()) {
						// create FloatingTableListener for each selected item
						TablePane t3 = (TablePane)it.next();
						logger.debug("(" + t3.getModel().getTableName() + ") zoomed selected table point: " + t3.getLocationOnScreen());
						logger.debug("(" + t3.getModel().getTableName() + ") unzoomed selected table point: " + getPlayPen().unzoomPoint(t3.getLocationOnScreen()));
						/* the floating table listener expects zoomed handles which are relative to
                           the location of the table column which was clicked on.  */
						Point clickedColumn = tp.getLocationOnScreen();
						Point otherTable = t3.getLocationOnScreen();
						Point handle = getPlayPen().zoomPoint(new Point(evt.getPoint()));
						logger.debug("(" + t3.getModel().getTableName() + ") translation x=" 
                                      + (otherTable.getX() - clickedColumn.getX()) + ",y=" 
                                      + (otherTable.getY() - clickedColumn.getY()));
						handle.translate((int)(clickedColumn.getX() - otherTable.getX()), (int) (clickedColumn.getY() - otherTable.getY())); 																	
						new PlayPen.FloatingTableListener(getPlayPen(), t3, handle);
					}
				}				
			} catch (ArchitectException e) {
				logger.error("Exception converting point to column", e);
			}
		}		
		maybeShowPopup(evt);
	}
	
	/*
	 * 
     */ 
	public void mouseReleased(MouseEvent evt) {
		TablePane tp = (TablePane) evt.getSource();
		try {
			PlayPen pp = (PlayPen) tp.getPlayPen();
			int releaseLocation = tp.pointToColumnIndex(evt.getPoint());		
			// can't just do pp.selectNone() here and re-select the current item because that will
            // trigger a second selection event which we don't want.  So, iterate through and de-select
            // things manually instead...but only if we weren't shift clicking :)
			if (releaseLocation == COLUMN_INDEX_TITLE) {
				// don't deselect everything if we just finished DND operation
				if (draggingTablePanes) {
					draggingTablePanes = false;
				} else {					
					if ( (evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == 0) {
						deSelectEverythingElse(evt);
					}
				}
			}				
		} catch (ArchitectException e) {
			logger.error("Exception converting point to column", e);
		}
		maybeShowPopup(evt);
	}

	/*  
     * deselect everything _but_ the selected item.  this method exists
     * to stop multiple selection events from propagating into the 
     * CreateRelationshipAction listeners.
     */
	private void deSelectEverythingElse (MouseEvent evt) {
		TablePane tp = (TablePane) evt.getSource();
		Iterator it = getPlayPen().getSelectedTables().iterator();
		while (it.hasNext()) {
			TablePane t3 = (TablePane)it.next();
			logger.debug("(" + tp.getModel().getTableName() + ") zoomed selected table point: " + tp.getLocationOnScreen());
			logger.debug("(" + t3.getModel().getTableName() + ") zoomed iterator table point: " + t3.getLocationOnScreen());
			if (!tp.getLocationOnScreen().equals(t3.getLocationOnScreen())) { // equals operation might not work so good here
				// unselect
				logger.debug("found matching table!");
				t3.setSelected(false);
				t3.selectNone();
			}
		}
	}

	public void mouseEntered(MouseEvent evt) {
        // we don't do anything about this at the moment
	}

	public void mouseExited(MouseEvent evt) {
        // we don't do anything about this at the moment
	}
	
	public void maybeShowPopup(MouseEvent evt) {
		if (evt.isPopupTrigger() && !evt.isConsumed()) {
			TablePane tp = (TablePane) evt.getComponent();
			PlayPen pp = tp.getPlayPen();

			// this allows the right-click menu to work on multiple tables simultaneously
			if (!tp.isSelected()) {
				pp.selectNone();
				tp.setSelected(true);
			}

			try {
				// tp.selectNone(); // single column selection model for now
				int idx = tp.pointToColumnIndex(evt.getPoint());
				if (idx >= 0) {
					tp.selectColumn(idx);
				}
			} catch (ArchitectException e) {
				logger.error("Exception converting point to column", e);
				return;
			}
			logger.debug("about to show playpen tablepane popup...");
			tp.showPopup(pp.tablePanePopup, evt.getPoint());
		}
	}
	
	// --------------------- Drag Source Listener ------------------------
	public void dragEnter(DragSourceDragEvent dsde) {
        // don't care
	}

	public void dragOver(DragSourceDragEvent dsde) {
        // don't care
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
        // don't care
	}
		
	public void dragExit(DragSourceEvent dse) {
        // don't care
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess()) {
			logger.debug("Succesful drop");
		} else {
			logger.debug("Unsuccesful drop");
		}
		draggingColumn = null;
	}
	
	// -------------------- Property Change Listener ------------------

    /**
     * The TPPropertyChangeListener listens for property changes on related
     * components, and updates TablePane state as necessary.
     */
    private class TPPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == parentPP) {
                if ("zoom".equals(evt.getPropertyName())) {
                    setCurrentFontRenderContext(parentPP.getFontRenderContext());
                    revalidate();
                }
            }
        }

    }

    /**
     * Returns a FontRenderContext object that was created by the parent playpen.  This 
     * needs to be cached because this component is orphaned from
     * the real swing of things by the PlayPenContentPane, so its own getGraphics() method
     * always returns null.  (Quelle bummer).
     * 
     * @return The most recent font render context given to setRecentFontRenderContext.
     */
    public FontRenderContext getCurrentFontRederContext() {
        return currentFontRederContext;
    }

    /**
     * @param fontRenderContext A font render context that is being used to render this component.
     */
    public void setCurrentFontRenderContext(FontRenderContext fontRenderContext) {
        currentFontRederContext = fontRenderContext;
    }
}
