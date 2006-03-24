package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class TablePane 
	extends PlayPenComponent 
	implements SQLObjectListener, java.io.Serializable, Selectable, DragSourceListener {

	private static final Logger logger = Logger.getLogger(TablePane.class);

	

	/**
	 * A special column index that represents the titlebar area.
	 */
	public static final int COLUMN_INDEX_TITLE = -1;

	/**
	 * A special column index that means "no location."
	 */
	public static final int COLUMN_INDEX_NONE = -2;
	
	/**
	 * A special column index that represents the gap between the last PK column and the PK line.
	 */
    public static final int COLUMN_INDEX_END_OF_PK = -3;

    /**
     * A special column index that represents the gap between the PK line and the first non-PK column.
     */
    public static final int COLUMN_INDEX_START_OF_NON_PK = -4;


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

	protected DropTargetListener dtl;

	/**
	 * Tracks which columns in this table are currently selected.
	 */
	protected ArrayList columnSelection;
	
	/**
	 * Tracks current highlight colours of the columns in this table.
	 */
	protected ArrayList columnHighlight;

	/**
	 * During a drag operation where a column is being dragged from
	 * this TablePane, this variable points to the column being
	 * dragged.  At all other times, it should be null.
	 */
	protected SQLColumn draggingColumn;

	
	static {
		UIManager.put(TablePaneUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.BasicTablePaneUI");
	}

	private SQLTable model;

    public TablePane(SQLTable m, PlayPen parentPP) {	    
    		super(parentPP.getPlayPenContentPane());
		setModel(m);
		setOpaque(true);
		setInsertionPoint(COLUMN_INDEX_NONE);

		
		//dt = new DropTarget(parentPP, new TablePaneDropListener(this));
		dtl = new TablePaneDropListener(this);
		
		updateUI();
	}


	// ---------------------- PlayPenComponent Overrides ----------------------
	// see also PlayPenComponent

    public void updateUI() {
    		TablePaneUI ui = (TablePaneUI) BasicTablePaneUI.createUI(this);
    		ui.installUI(this);
		setUI(ui);
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
	 * or relationships added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a PropertyChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		if (e.getSource() == getModel().getColumnsFolder()) {
			int ci[] = e.getChangedIndices();
			for (int i = 0; i < ci.length; i++) {
				columnSelection.add(ci[i], Boolean.FALSE);
				columnHighlight.add(ci[i], null);
			}
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
	 * delegate) with a PropertyChangeEvent.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		if (e.getSource() == this.model.getColumnsFolder()) {
			int ci[] = e.getChangedIndices();
			for (int i = 0; i < ci.length; i++) {
			    columnSelection.remove(ci[i]);
			    columnHighlight.remove(ci[i]);
			}
			if (columnSelection.size() > 0) {
				selectNone();
				columnSelection.set(Math.min(ci[0], columnSelection.size()-1), Boolean.TRUE);
			}
		}
		try {
			ArchitectUtils.unlistenToHierarchy(this, e.getChildren());
			if (columnSelection.size() != this.model.getColumns().size()) {
				logger.error("Repairing out-of-sync selection list: selection="+columnSelection+"; children="+this.model.getColumns());
				columnSelection = new ArrayList();
				for (int j = 0; j < model.getColumns().size(); j++) {
				    columnSelection.add(Boolean.FALSE);
				}
			}
			if (columnHighlight.size() != this.model.getColumns().size()) {
				logger.error("Repairing out-of-sync highlight list: highlights="+columnHighlight+"; children="+this.model.getColumns());
				columnHighlight = new ArrayList();
				for (int j = 0; j < model.getColumns().size(); j++) {
				    columnHighlight.add(null);
				}
			}
		} catch (ArchitectException ex) {
			logger.error("Couldn't remove children", ex);
			JOptionPane.showMessageDialog(getPlayPen(), "Couldn't delete column: "+ex.getMessage());
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
			columnHighlight = new ArrayList(numCols);
			for (int i = 0; i < numCols; i++) {
				columnHighlight.add(null);
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
		
		if (m == null) {
			throw new IllegalArgumentException("model may not be null");
		} else {
			model = m;
		}

		if (old != null) {
			try {
				ArchitectUtils.unlistenToHierarchy(this, old);
			} catch (ArchitectException e) {
				logger.error("Caught exception while unlistening to old model", e);
			}
		}


		try {
			columnSelection = new ArrayList(m.getColumns().size());
			for (int i = 0; i < m.getColumns().size(); i++) {
				columnSelection.add(Boolean.FALSE);
			}
			columnHighlight = new ArrayList(m.getColumns().size());
			for (int i = 0; i < m.getColumns().size(); i++) {
				columnHighlight.add(null);
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

	public DropTargetListener getDropTargetListener() {
		return dtl;
	}

	// --------------------- column selection support --------------------

	public void selectNone() {
		for (int i = 0; i < columnSelection.size(); i++) {
			columnSelection.set(i, Boolean.FALSE);
		}
		repaint();
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
		return ((TablePaneUI) getUI()).pointToColumnIndex(p);
	}

	/**
	 * Inserts the list of SQLObjects into this table at the specified location.
	 * 
	 * @param items A list of SQLTable and/or SQLColumn objects.  Other types are not allowed. 
	 * @param insertionPoint The position that the first item in the item list should go into.
	 * This can be a nonnegative integer to specify a position in the column list, or one
	 * of the constants COLUMN_INDEX_END_OF_PK or COLUMN_INDEX_START_OF_NON_PK to indicate a special position.
	 * @return True if the insert worked; false otherwise
	 * @throws ArchitectException If there are problems in the business model
	 */
	public boolean insertObjects(List<SQLObject> items, int insertionPoint) throws ArchitectException {
		boolean newColumnsInPk = false;
		if (insertionPoint == COLUMN_INDEX_END_OF_PK) {
		    insertionPoint = getModel().getPkSize();
		    newColumnsInPk = true;
		} else if (insertionPoint == COLUMN_INDEX_START_OF_NON_PK) {
		    insertionPoint = getModel().getPkSize();
		    newColumnsInPk = false;
		} else if (insertionPoint == COLUMN_INDEX_TITLE) {
		    insertionPoint = 0;
		    newColumnsInPk = true;
		} else if (insertionPoint < 0) {
		    insertionPoint = getModel().getColumns().size();
		    newColumnsInPk = false;
		} else if (insertionPoint < getModel().getPkSize()) {
		    newColumnsInPk = true;
		}

		for (int i = items.size()-1; i >= 0; i--) {
			SQLObject someData = items.get(i);
			logger.debug("insertObjects: got item of type "+someData.getClass().getName());
			if (someData instanceof SQLTable) {
				SQLTable table = (SQLTable) someData;
				if (table.getParentDatabase() == getModel().getParentDatabase()) {
					// can't import table from target into target!!
					return false;
				} else {
					getModel().inherit(insertionPoint, table);
				}
			} else if (someData instanceof SQLColumn) {
				SQLColumn col = (SQLColumn) someData;
				if (col.getParentTable() == getModel()) {
					// moving column inside the same table
					int oldIndex = col.getParentTable().getColumns().indexOf(col);
					if (insertionPoint > oldIndex) {
						insertionPoint--;
					}
					getModel().changeColumnIndex(oldIndex, insertionPoint, newColumnsInPk);
				} else if (col.getParentTable().getParentDatabase() == getModel().getParentDatabase()) {
					// moving column within playpen
					col.getParentTable().removeColumn(col);
					if (logger.isDebugEnabled()) {
						logger.debug("Moving column '"+col.getName()
								+"' to table '"+getModel().getName()
								+"' at position "+insertionPoint);
					}
					getModel().addColumn(insertionPoint, col);
					
					if (newColumnsInPk) {
					    col.setPrimaryKeySeq(new Integer(insertionPoint));
					} else {
					    col.setPrimaryKeySeq(null);
					}
				} else {
					// importing column from a source database
					getModel().inherit(insertionPoint, col, newColumnsInPk);
					if (logger.isDebugEnabled()) logger.debug("Inherited "+col.getName()+" to table");
				}
			} else {
				return false;
			}
		}
		return true;
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
			PlayPen pp = tp.getPlayPen();
			Point loc = pp.unzoomPoint(new Point(dtde.getLocation()));
			loc.x -= tp.getX();
			loc.y -= tp.getY();

			logger.debug("Drop target drop event on "+tp.getName()+": "+dtde);
			Transferable t = dtde.getTransferable();
			DataFlavor importFlavor = bestImportFlavor(pp, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
				tp.setInsertionPoint(COLUMN_INDEX_NONE);
			} else {
				try {
					DBTree dbtree = ArchitectFrame.getMainInstance().dbTree;  // XXX: bad
					int insertionPoint = tp.pointToColumnIndex(loc);

					ArrayList<int[]> paths = (ArrayList<int[]>) t.getTransferData(importFlavor);
					logger.debug("Importing items from tree: "+paths);
					
					// put the undo event adapter into a drag and drop state
					ArchitectFrame.getMainInstance().playpen.fireUndoCompoundEvent(
							new UndoCompoundEvent(
							this,EventTypes.DRAG_AND_DROP_START, "Starting drag and drop"));
					
					ArrayList<SQLObject> droppedItems = new ArrayList<SQLObject>();
					for (int[] path : paths) {
						droppedItems.add(dbtree.getNodeForDnDPath(path));
					}

					boolean success = tp.insertObjects(droppedItems, insertionPoint);
					if (success) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY); // XXX: not always true
					} else {
						dtde.rejectDrop();
					}
					dtde.dropComplete(success);
				} catch (Exception ex) {
				    // Trying to show this dialog sometimes hangs the app in OS X
					//JOptionPane.showMessageDialog(tp, "Drop failed: "+ex.getMessage());
					logger.error("Error processing drop operation", ex);
					dtde.rejectDrop();
					dtde.dropComplete(false);
				} finally {
					tp.setInsertionPoint(COLUMN_INDEX_NONE);
					tp.getModel().normalizePrimaryKey();
					
					// put the undo event adapter into a regular state
					ArchitectFrame.getMainInstance().playpen.fireUndoCompoundEvent(
							new UndoCompoundEvent(
							this,EventTypes.DRAG_AND_DROP_END, "End drag and drop"));
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

	/**
     * Deselects everything <b>except</b> the selected item.  This method exists
     * to stop multiple selection events from propogating into the 
     * CreateRelationshipAction listeners.
     */
	void deSelectEverythingElse (MouseEvent evt) {
		Iterator it = getPlayPen().getSelectedTables().iterator();
		while (it.hasNext()) {
			TablePane t3 = (TablePane) it.next();
			if (logger.isDebugEnabled()) {
			    logger.debug("(" + getModel().getName() + ") zoomed selected table point: " + getLocationOnScreen());
			    logger.debug("(" + t3.getModel().getName() + ") zoomed iterator table point: " + t3.getLocationOnScreen());
			}
			if (!getLocationOnScreen().equals(t3.getLocationOnScreen())) { // equals operation might not work so good here
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
    
    /**
     * Changes the foreground colour of a column.  This is useful when outside forces
     * want to colour in a column.
     * 
     * @param i The column index to recolour
     * @param c The new colour to show the column in.  null means use this TablePane's current
     * foreground colour.
     */
    public void setColumnHighlight(int i, Color c) {
        columnHighlight.set(i, c);
        repaint(); // XXX: should constrain repaint region to column i
    }
    
    /**
     * Returns the current highlight colour for a particular column.
     * 
     * @param i The index of the column in question
     * @return The current highlight colour for the column at index i in this table.
     *   null indicates the current tablepane foreground colour will be used.
     */
    public Color getColumnHighlight(int i) {
        return (Color) columnHighlight.get(i);
    }
}
