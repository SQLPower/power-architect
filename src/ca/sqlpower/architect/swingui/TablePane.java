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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.InsertionPointWatcher;
import ca.sqlpower.architect.LockedColumnException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.layout.LayoutNode;
import ca.sqlpower.architect.swingui.action.EditSpecificIndexAction;
import ca.sqlpower.architect.swingui.event.PlayPenComponentMovedEvent;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.swingui.ColorIcon;
import ca.sqlpower.swingui.SPSUtils;

public class TablePane
	extends PlayPenComponent
	implements Serializable, Selectable, DragSourceListener, LayoutNode {

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
	protected Set<SQLColumn> selectedColumns;
	
	/**
	 * Tracks which columns in this table are currently hidden.
	 */
	protected Set<SQLColumn> hiddenColumns;

	/**
	 * Tracks current highlight colours of the columns in this table.
	 */
	protected Map<SQLColumn, List<Color>> columnHighlight;

	/**
	 * During a drag operation where a column is being dragged from
	 * this TablePane, this variable points to the column being
	 * dragged.  At all other times, it should be null.
	 */
	protected SQLColumn draggingColumn;
	
	private boolean rounded = false;
	private boolean dashed = false;

    private boolean fullyQualifiedNameInHeader = false;

	static {
		UIManager.put(TablePaneUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.BasicTablePaneUI"); //$NON-NLS-1$
	}

	private SQLTable model;

    SQLObjectListener columnListener = new ColumnListener();

    public TablePane(TablePane tp, PlayPenContentPane parent) {
		super(parent);
		this.model = tp.model;
		this.selectionListeners = new ArrayList<SelectionListener>();
		this.dtl = new TablePaneDropListener(this);
		this.margin = (Insets) tp.margin.clone();
		this.selectedColumns = new HashSet<SQLColumn>(tp.selectedColumns);
		this.columnHighlight = new HashMap<SQLColumn,List<Color>>(tp.columnHighlight);
		
		this.insertionPoint = tp.insertionPoint;
		this.draggingColumn = tp.draggingColumn;
		this.selected = false;

		this.hiddenColumns = new HashSet<SQLColumn>(tp.getHiddenColumns());
		
		this.foregroundColor = tp.getForegroundColor();
		this.backgroundColor = tp.getBackgroundColor();
		this.dashed = tp.isDashed();
		this.rounded = tp.isRounded();
		
		try {
			PlayPenComponentUI newUi = tp.getUI().getClass().newInstance();
			newUi.installUI(this);
			setUI(newUi);
		} catch (InstantiationException e) {
			throw new RuntimeException("Woops, couldn't invoke no-args constructor of "+tp.getUI().getClass().getName()); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Woops, couldn't access no-args constructor of "+tp.getUI().getClass().getName()); //$NON-NLS-1$
		}
    }


	public TablePane(SQLTable m, PlayPenContentPane parent) {
	    super(parent);
	    this.hiddenColumns = new HashSet<SQLColumn>();
	    this.backgroundColor = new Color(240, 240, 240);
	    this.foregroundColor = Color.BLACK;
	    setModel(m);
	    setOpaque(true);
	    setInsertionPoint(COLUMN_INDEX_NONE);

	    //dt = new DropTarget(parentPP, new TablePaneDropListener(this));
	    dtl = new TablePaneDropListener(this);

		updateUI();
	}

	@Override
	public String toString() {
		return "TablePane: "+model; //$NON-NLS-1$
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
			ArchitectUtils.unlistenToHierarchy(columnListener, model);
		} catch (ArchitectException e) {
			logger.error("Caught exception while unlistening to all children", e); //$NON-NLS-1$
		}
	}

	// -------------------- sqlobject event support ---------------------

    private class ColumnListener implements SQLObjectListener {

        /**
         * The column that was most recently removed from this table while it
         * was still selected.  This is kept here in case the column is subsequently
         * reinserted into the table (possibly at a different index) so it can be
         * selected when it comes back from the dead.
         */
        private SQLColumn mostRecentSelectedRemoval;

        /**
         * Listens for property changes in the model (columns
         * or relationships added).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a PropertyChangeEvent.
         */
        public void dbChildrenInserted(SQLObjectEvent e) {
            if (e.getSource() == getModel().getColumnsFolder()) {
                int ci[] = e.getChangedIndices();

                if (logger.isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    for (int i : ci) {
                        sb.append(i).append(' ');
                    }
                    logger.debug("Columns inserted. Syncing select/highlight lists. New indices=["+sb+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                for (int i = 0; i < ci.length; i++) {
                    boolean wasSelectedPreviously = (e.getChildren()[i] == mostRecentSelectedRemoval);
                    final SQLColumn column = (SQLColumn) e.getChildren()[i];
                    if (wasSelectedPreviously) {
                        selectedColumns.add(column);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                selectColumnOnTree(column);
                            }
                        });
                    }
                    // This is only supposed to work if we deselect the columns before selecting them
                    // this if stops the insert from wiping out a highlighted column
                    if (columnHighlight.get(column) == null) {
                        columnHighlight.put(column, new ArrayList<Color>());
                    }
                }
            }
            try {
                ArchitectUtils.listenToHierarchy(this, e.getChildren());
            } catch (ArchitectException ex) {
                logger.error("Caught exception while listening to added children", ex); //$NON-NLS-1$
            }
            
            updateHiddenColumns();
            firePropertyChange("model.children", null, null); //$NON-NLS-1$
            //revalidate();
        }

        /**
         * Listens for property changes in the model (columns
         * removed).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a PropertyChangeEvent.
         */
        public void dbChildrenRemoved(SQLObjectEvent e) {
            if (e.getSource() == model.getColumnsFolder()) {
                int ci[] = e.getChangedIndices();
                if (logger.isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    for (int i : ci) {
                        sb.append(i).append(' ');
                    }
                    logger.debug("Columns removed. Syncing select/highlight lists. Removed indices=["+sb+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                for (int i = 0; i < ci.length; i++) {
                    if (selectedColumns.contains(e.getChildren()[i])) {
                        mostRecentSelectedRemoval = (SQLColumn) e.getChildren()[i];
                    }
                    selectedColumns.remove(e.getChildren()[i]);
                }
                try {
                    int size = model.getColumns().size();
                    if (size > 0 ) {
                        selectNone();
                    }
                } catch (ArchitectException ex) {
                    throw new ArchitectRuntimeException(ex);
                }
            }
            
            // no matter where the event came from, we should no longer be listening to the removed children
            try {
                ArchitectUtils.unlistenToHierarchy(this, e.getChildren());
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
            updateHiddenColumns();
            firePropertyChange("model.children", null, null); //$NON-NLS-1$
            //revalidate();
        }

        /**
         * Listens for property changes in the model (columns
         * properties modified).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a ChangeEvent.
         */
        public void dbObjectChanged(SQLObjectEvent e) {
            if (logger.isDebugEnabled()) {
                logger.debug("TablePane got object changed event." + //$NON-NLS-1$
                        "  Source="+e.getSource()+" Property="+e.getPropertyName()+ //$NON-NLS-1$ //$NON-NLS-2$
                        " oldVal="+e.getOldValue()+" newVal="+e.getNewValue()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateHiddenColumns();
            firePropertyChange("model."+e.getPropertyName(), null, null); //$NON-NLS-1$
            //repaint();
        }

        /**
         * Listens for property changes in the model (significant
         * structure change).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a ChangeEvent.
         */
        public void dbStructureChanged(SQLObjectEvent e) {
            logger.debug("TablePane got db structure change event. source="+e.getSource()); //$NON-NLS-1$
            if (e.getSource() == model.getColumnsFolder()) {
                int numCols = e.getChildren().length;
                selectedColumns = new HashSet<SQLColumn>(numCols);
                columnHighlight = new HashMap<SQLColumn,List<Color>>();
                try {
                    for (SQLColumn child :((List<SQLColumn>) model.getColumnsFolder().getChildren())) {
                        columnHighlight.put(child,new ArrayList<Color>());
                    }
                } catch (ArchitectException e1) {
                    throw new ArchitectRuntimeException(e1);
                }
                updateHiddenColumns();
                firePropertyChange("model.children", null, null); //$NON-NLS-1$
                //revalidate();
            }
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
			throw new IllegalArgumentException("model may not be null"); //$NON-NLS-1$
		} else {
			model = m;
		}

		if (old != null) {
			try {
				ArchitectUtils.unlistenToHierarchy(columnListener, old);
			} catch (ArchitectException e) {
				logger.error("Caught exception while unlistening to old model", e); //$NON-NLS-1$
			}
		}


		try {
		    selectedColumns = new HashSet<SQLColumn>(m.getColumns().size());
			columnHighlight = new HashMap<SQLColumn,List<Color>>();
			for (SQLColumn column: model.getColumns()) {
				columnHighlight.put(column, new ArrayList<Color>());
			}
		} catch (ArchitectException e) {
			logger.error("Error getting children on new model", e); //$NON-NLS-1$
		}

		try {
			ArchitectUtils.listenToHierarchy(columnListener, model);
		} catch (ArchitectException e) {
			logger.error("Caught exception while listening to new model", e); //$NON-NLS-1$
		}
		setName("TablePane: "+model.getShortDisplayName()); //$NON-NLS-1$

        firePropertyChange("model", old, model); //$NON-NLS-1$
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
		firePropertyChange("margin", old, margin); //$NON-NLS-1$
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
			firePropertyChange("insertionPoint", new Integer(old), new Integer(insertionPoint)); //$NON-NLS-1$
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
	public void setSelected(boolean isSelected, int multiSelectType) {
		if (isSelected == false) {
			selectNone();
		}
		if (selected != isSelected) {
			selected = isSelected;
			fireSelectionEvent(new SelectionEvent(this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT,multiSelectType));
			repaint();
		}
	}

	public DropTargetListener getDropTargetListener() {
		return dtl;
	}

	// --------------------- column selection support --------------------

    /**
     * Deselects all columns in this tablepane.
     */
	public void selectNone() {
	    List<SQLColumn> deselectCols = new ArrayList<SQLColumn>(selectedColumns);
	    for (SQLColumn col : deselectCols) {
	        deselectColumnOnTree(col);
	    }
	    selectedColumns.clear();
		repaint();
	}


    /**
     * @param i The column to deselect.  If less than 0, {@link
     * #selectNone()} is called.
     */
    public void deselectColumn(int i) {
        if (i < 0) {
            selectNone();
            return;
        }
        try {
            selectedColumns.remove(model.getColumn(i));
            deselectColumnOnTree(model.getColumn(i));
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
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
	    try {
    	    selectedColumns.add(model.getColumn(i));
    	    selectColumnOnTree(model.getColumn(i));
	    } catch (ArchitectException ex) {
	        throw new ArchitectRuntimeException(ex);
	    }
	    
		repaint();
	}
	
	private void selectColumnOnTree(SQLColumn col) {
	    if (getPlayPen().ignoreTreeSelection()) return;
	    getPlayPen().setIgnoreTreeSelection(true);
	    logger.debug("selecting column on tree: " + col); //$NON-NLS-1$
	    
	    DBTree tree = getPlayPen().getSession().getSourceDatabases();
	    TreePath tp = tree.getTreePathForNode(col.getParentTable());

	    if (!tree.isPathSelected(tp)) {
	        tree.addSelectionPath(tp);
	    }
	     
	    tp = tree.getTreePathForNode(col);
	    if (!tree.isPathSelected(tp)) {
	        tree.addSelectionPath(tp);
	        tree.clearNonPlayPenSelections();
	    }
	    getPlayPen().setIgnoreTreeSelection(false);
	}

	private void deselectColumnOnTree(SQLColumn col) {
	    if (getPlayPen().ignoreTreeSelection()) return;
        getPlayPen().setIgnoreTreeSelection(true);
	    logger.debug("deselecting column on tree: " + col); //$NON-NLS-1$
	    
	    DBTree tree = getPlayPen().getSession().getSourceDatabases();
	    TreePath tp = tree.getTreePathForNode(col);
	    if (tree.isPathSelected(tp)) {
	        tree.removeSelectionPath(tp);
	    }
	    getPlayPen().setIgnoreTreeSelection(false);
	}

	/**
	 * return true if the column in tablepane is selected
	 * @param i column index
	 * @return true if the column in tablepane is selected
	 */
	public boolean isColumnSelected(int i) {
		try {
		    return selectedColumns.contains(model.getColumn(i));
		} catch (ArchitectException ex) {
		    throw new ArchitectRuntimeException(ex);
		}
	}

	/**
	 * Returns the index of the first selected column, or
	 * COLUMN_INDEX_NONE if there are no selected columns.
	 */
	public int getSelectedColumnIndex() {
		if (selectedColumns.size() > 0) {
		    try {
		        return model.getColumns().indexOf(selectedColumns.toArray()[0]);
		    } catch (ArchitectException ex) {
		        throw new ArchitectRuntimeException(ex);
		    }
		}
		return COLUMN_INDEX_NONE;
	}

	public void updateHiddenColumns() {
	    try {
	        hiddenColumns.clear();
	        ArchitectSwingSession session = getPlayPen().getSession();
	        
	        // if all the boxes are checked, then hide no columns, only these 3 need be
	        // checked. Draw a truth table if you don't believe me.
	        if (!(session.isShowForeign() && session.isShowIndexed() 
	                && session.isShowTheRest())) {
	            
	            // start with a list of all the columns, then remove the ones that 
	            // should be shown
	            hiddenColumns.addAll(getModel().getColumns());
	            for (SQLColumn col : getModel().getColumns()) {
	                if (session.isShowPrimary() && col.isPrimaryKey()) {
	                    hiddenColumns.remove(col);
	                } else if (session.isShowForeign() && col.isForeignKey()) {
	                    hiddenColumns.remove(col);
	                } else if (session.isShowIndexed() && col.isIndexed()) {
	                    hiddenColumns.remove(col);
	                } else if (session.isShowUnique() && col.isUniqueIndexed()) {
	                    hiddenColumns.remove(col);
	                } else if (session.isShowTheRest() && !(col.isPrimaryKey() || col.isForeignKey() 
	                        || col.isIndexed())) {
	                    // unique index not checked because it is a subset of index
	                    hiddenColumns.remove(col);
	                }
	            }
	        }
	    } catch (ArchitectException ex) {
	        throw new ArchitectRuntimeException(ex); 
	    }
	}

	/**
	 * Returns the list of selected column(s).
	 * @throws ArchitectException
	 */
	public List<SQLColumn> getSelectedColumns() throws ArchitectException {
	    List<SQLColumn> selectedColumns = new ArrayList<SQLColumn>();
	    for (int i=0; i < getModel().getColumns().size(); i++) {
	        if (isColumnSelected(i)) {
	            selectedColumns.add(getModel().getColumn(i));
	        }
	    }
	    return selectedColumns;
	}

	// --------------------- SELECTION EVENT SUPPORT ---------------------

	protected List<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();

	public void addSelectionListener(SelectionListener l) {
	    selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
	    selectionListeners.remove(l);
	}

	protected void fireSelectionEvent(SelectionEvent e) {
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
     * Returns the centre Y coordinate of the given column index.  The
     * special {@link #COLUMN_INDEX_TITLE} value for <tt>colidx</tt>
     * will produce the central Y coordinate for the title bar.
     * 
     * @param colidx the column number to get the central Y coordinate of.
     * @return The Y coordinate at the visual centre point of the
     * given column.  If the requested column index is out of range, the
     * value <tt>-1</tt> is returned.
     */
    public int columnIndexToCentreY(int colidx) throws ArchitectException {
        return ((TablePaneUI) getUI()).columnIndexToCentreY(colidx);
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
	public boolean insertObjects(List<? extends SQLObject> items, int insertionPoint) throws ArchitectException {
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
			logger.debug("insertObjects: got item of type "+someData.getClass().getName()); //$NON-NLS-1$
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
                    
                    InsertionPointWatcher<SQLTable.Folder<SQLColumn>> ipWatcher =
                        new InsertionPointWatcher<SQLTable.Folder<SQLColumn>>(getModel().getColumnsFolder(), insertionPoint);
					col.getParentTable().removeColumn(col);
                    ipWatcher.dispose();
                    
					if (logger.isDebugEnabled()) {
						logger.debug("Moving column '"+col.getName() //$NON-NLS-1$
								+"' to table '"+getModel().getName() //$NON-NLS-1$
								+"' at position "+ipWatcher.getInsertionPoint()); //$NON-NLS-1$
					}
					try {
                        // You need to disable the magic (really the normalization) otherwise it goes around
                        // the property change events and causes undo to fail when dragging into the primary
                        // key of a table
					    getModel().setMagicEnabled(false);
					    getModel().addColumn(ipWatcher.getInsertionPoint(), col);

					    if (newColumnsInPk) {
					        col.setPrimaryKeySeq(new Integer(ipWatcher.getInsertionPoint()));
					    } else {
					        col.setPrimaryKeySeq(null);
					    }
					} finally {
                        getModel().setMagicEnabled(true);
					}
				} else {
					// importing column from a source database
					getModel().inherit(insertionPoint, col, newColumnsInPk);
					if (logger.isDebugEnabled()) logger.debug("Inherited "+col.getName()+" to table with precision " + col.getPrecision()); //$NON-NLS-1$ //$NON-NLS-2$
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
				logger.debug("DragEnter event on "+tp.getName()); //$NON-NLS-1$
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
				logger.debug("DragExit event on "+tp.getName()); //$NON-NLS-1$
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
				logger.debug("DragOver event on "+tp.getName()+": "+dtde); //$NON-NLS-1$ //$NON-NLS-2$
				logger.debug("Drop Action = "+dtde.getDropAction()); //$NON-NLS-1$
				logger.debug("Source Actions = "+dtde.getSourceActions()); //$NON-NLS-1$
			}
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE & dtde.getDropAction());
			try {
				Point loc = tp.getPlayPen().unzoomPoint(new Point(dtde.getLocation()));
				loc.x -= tp.getX();
				loc.y -= tp.getY();
				int idx = tp.pointToColumnIndex(loc);
				tp.setInsertionPoint(idx);
			} catch (ArchitectException e) {
				logger.error("Got exception translating drag location", e); //$NON-NLS-1$
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

			logger.debug("Drop target drop event on "+tp.getName()+": "+dtde); //$NON-NLS-1$ //$NON-NLS-2$
			Transferable t = dtde.getTransferable();
			DataFlavor importFlavor = bestImportFlavor(pp, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
				tp.setInsertionPoint(COLUMN_INDEX_NONE);
			} else {
				try {
					DBTree dbtree = pp.getSession().getSourceDatabases();  // XXX: bad
					int insertionPoint = tp.pointToColumnIndex(loc);

					ArrayList<int[]> paths = (ArrayList<int[]>) t.getTransferData(importFlavor);
					logger.debug("Importing items from tree: "+paths); //$NON-NLS-1$

					// put the undo event adapter into a drag and drop state
					pp.startCompoundEdit("Drag and Drop"); //$NON-NLS-1$

					ArrayList<SQLObject> droppedItems = new ArrayList<SQLObject>();
					for (int[] path : paths) {
						droppedItems.add(dbtree.getNodeForDnDPath(path));
					}

                    boolean success = false;
                    
                    //Check to see if the drag and drop will change the current relationship
                    List<SQLRelationship> importedKeys = tp.getModel().getImportedKeys();
                    
                    boolean newColumnsInPk = false;
                    if (insertionPoint == COLUMN_INDEX_END_OF_PK) {
                        newColumnsInPk = true;
                    } else if (insertionPoint == COLUMN_INDEX_START_OF_NON_PK) {
                        newColumnsInPk = false;
                    } else if (insertionPoint == COLUMN_INDEX_TITLE) {
                        newColumnsInPk = true;
                    } else if (insertionPoint < 0) {
                        newColumnsInPk = false;
                    } else if (insertionPoint < tp.getModel().getPkSize()) {
                        newColumnsInPk = true;
                    }

                    try {
                        for (int i = 0; i < importedKeys.size(); i++) {
                            // Not dealing with self-referencing tables right now.
                            if (importedKeys.get(i).getPkTable().equals(importedKeys.get(i).getFkTable())) continue;  
                            for (int j = 0; j < droppedItems.size(); j++) {
                                if (importedKeys.get(i).containsFkColumn((SQLColumn)(droppedItems.get(j)))) {
                                    importedKeys.get(i).setIdentifying(newColumnsInPk);
                                    break;
                                }
                            }
                        }
                        success = tp.insertObjects(droppedItems, insertionPoint);
                    } catch (LockedColumnException ex ) {
                        JOptionPane.showConfirmDialog(pp,
                                "Could not delete the column " + //$NON-NLS-1$
                                ex.getCol().getName() +
                                " because it is part of\n" + //$NON-NLS-1$
                                "the relationship \""+ex.getLockingRelationship()+"\".\n\n", //$NON-NLS-1$ //$NON-NLS-2$
                                "Column is Locked", //$NON-NLS-1$
                                JOptionPane.CLOSED_OPTION);
                        success = false;
                    } 

					if (success) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY); // XXX: not always true
					} else {
						dtde.rejectDrop();
					}
					dtde.dropComplete(success);
				} catch (Exception ex) {
					logger.error("Error processing drop operation", ex); //$NON-NLS-1$
					dtde.rejectDrop();
					dtde.dropComplete(false);
					ASUtils.showExceptionDialogNoReport(tp.getParent().getOwner(),
                        "Error processing drop operation", ex); //$NON-NLS-1$
                } finally {
					tp.setInsertionPoint(COLUMN_INDEX_NONE);
					try {
                        tp.getModel().normalizePrimaryKey();
                    } catch (ArchitectException e) {
                        logger.error("Error processing normalize PrimaryKey", e); //$NON-NLS-1$
                        ASUtils.showExceptionDialogNoReport(tp.getParent().getOwner(),
                                "Error processing normalize PrimaryKey after processing drop operation", e); //$NON-NLS-1$
                    }

					// put the undo event adapter into a regular state
					pp.endCompoundEdit("End drag and drop"); //$NON-NLS-1$
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
			logger.debug("can I import "+Arrays.asList(flavors)); //$NON-NLS-1$
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
 					return flavors[i];
				}
 			}
			logger.debug("NO!"); //$NON-NLS-1$
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
			    logger.debug("(" + getModel().getName() + ") zoomed selected table point: " + getLocationOnScreen()); //$NON-NLS-1$ //$NON-NLS-2$
			    logger.debug("(" + t3.getModel().getName() + ") zoomed iterator table point: " + t3.getLocationOnScreen()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!getLocationOnScreen().equals(t3.getLocationOnScreen())) { // equals operation might not work so good here
				// unselect
				logger.debug("found matching table!"); //$NON-NLS-1$
				t3.setSelected(false,SelectionEvent.SINGLE_SELECT);
				t3.selectNone();
			}
		}

		// also de-select all the selected relationships
		it = getPlayPen().getSelectedRelationShips().iterator();
		while (it.hasNext()) {
			Relationship r = (Relationship) it.next();
			r.setSelected(false,SelectionEvent.SINGLE_SELECT);
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
			logger.debug("Succesful drop"); //$NON-NLS-1$
		} else {
			logger.debug("Unsuccesful drop"); //$NON-NLS-1$
		}
		draggingColumn = null;
	}

    /**
     * Changes the foreground colour of a column.  This is useful when outside forces
     * want to colour in a column.
     * <p>
     * When highlighting for the given column is no longer desired, remove the
     * highlight with a call to {@link #removeColumnHighlight(SQLColumn, Color)}.
     *
     * @param i The column index to recolour
     * @param colour The new colour to show the column in.
     */
    public void addColumnHighlight(SQLColumn column, Color colour) {
        columnHighlight.get(column).add(colour);
        repaint(); // XXX: should constrain repaint region to column i
    }

    /**
     * Removes the given colour highlight from the given column.  This method
     * should be called once and only once for each corresponding invocation
     * of {@link #addColumnHighlight(SQLColumn, Color)} with the same arguments.
     */
    public void removeColumnHighlight(SQLColumn column, Color colour) {
        columnHighlight.get(column).remove(colour);
        repaint();
    }

    /**
     * Returns the current highlight colour for a particular column.
     *
     * @param i The index of the column in question
     * @return The current highlight colour for the column at index i in this table.
     *   null indicates the current tablepane foreground colour will be used.
     * @throws ArchitectException
     */
    public Color getColumnHighlight(int i) throws ArchitectException {

        return getColumnHighlight(model.getColumn(i));
    }

    public Color getColumnHighlight(SQLColumn column) {
        logger.debug("Checking column "+column); //$NON-NLS-1$
        if (columnHighlight.get(column).isEmpty()) {
            return getForegroundColor();
        } else {
            float[] rgbsum = new float[3];
            for (Color c : columnHighlight.get(column)) {
                float[] comps = c.getRGBColorComponents(new float[3]);
                rgbsum[0] += comps[0];
                rgbsum[1] += comps[1];
                rgbsum[2] += comps[2];
            }
            float sz = columnHighlight.get(column).size();
            return new Color(rgbsum[0]/sz, rgbsum[1]/sz, rgbsum[2]/sz);
        }
    }

    public void setFullyQualifiedNameInHeader(boolean fullyQualifiedNameInHeader) {
        this.fullyQualifiedNameInHeader = fullyQualifiedNameInHeader;
    }

    public boolean isFullyQualifiedNameInHeader() {
        return fullyQualifiedNameInHeader;
    }


    // ------- LayoutNode methods that we didn't already happen to implement --------


    public String getNodeName() {
        return getName();
    }

    public List<LayoutEdge> getInboundEdges() {
        try {
            List<SQLRelationship> relationships = getModel().getImportedKeys();
            List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
            for (SQLRelationship r : relationships) {
                edges.add(getPlayPen().findRelationship(r));
            }
            return edges;
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }

    public List<LayoutEdge> getOutboundEdges() {
        try {
            List<SQLRelationship> relationships = getModel().getExportedKeys();
            List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
            for (SQLRelationship r : relationships) {
                edges.add(getPlayPen().findRelationship(r));
            }
            return edges;
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }

    /**
     * Indicates whether the corners are rounded. 
     */
    public boolean isRounded() {
        return rounded;
    }

    /**
     * Sets whether the corners are rounded. 
     */
    public void setRounded(boolean isRounded) {
        boolean oldValue = rounded;
        rounded = isRounded;
        firePropertyChange("rounded", oldValue, isRounded); //$NON-NLS-1$
    }

    /**
     * Indicates whether the lines are dashed/normal. 
     */
    public boolean isDashed() {
        return dashed;
    }

    /**
     * Sets whether the lines are dashed. 
     */
    public void setDashed(boolean isDashed) {
        boolean oldValue = dashed;
        dashed = isDashed;
        firePropertyChange("dashed", oldValue, isDashed); //$NON-NLS-1$
    }

    /**
     * Returns the number of hidden primary key columns
     */
    public int getHiddenPkCount() {
        int count = 0;
        for (SQLColumn c : hiddenColumns) {
            if (c.getPrimaryKeySeq() != null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Not only sets the new bounds and the new location
     * of the tablepane, but also fires specific events associated with
     * each change.
     */
    @Override
    protected void setBoundsImpl(int x, int y, int width, int height) { 
        Rectangle oldBounds = getBounds();
        super.setBoundsImpl(x, y, width, height);
        if (oldBounds.x != x || oldBounds.y != y) {
            firePropertyChange(new PlayPenComponentMovedEvent(this, oldBounds.getLocation(), new Point(x,y)));
        }
        if (oldBounds.width != width || oldBounds.height != height) {
            firePropertyChange(new PlayPenComponentResizedEvent());
        }
    }

    /**
     * @see TablePane.setBoundsImpl()
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
    }
    
    public boolean isShowPkTag(){
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) return session.isShowPkTag();
        }
        return true;
    }
    
    public boolean isShowFkTag(){
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) return session.isShowFkTag();
        }
        return true;
    }
    
    public boolean isShowAkTag(){
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) return session.isShowAkTag();
        }
        return true;
    }
    
    public Set<SQLColumn> getHiddenColumns() {
        return hiddenColumns;
    }
    
    /**
     * Returns an instance of the popup menu with menu items exclusive to
     * manipulating tablepanes.
     */
    @Override
    public JPopupMenu getPopup() {
        ArchitectFrame af = getPlayPen().getSession().getArchitectFrame();
        JPopupMenu tablePanePopup = new JPopupMenu();
        
        JMenuItem mi;
        
        mi = new JMenuItem();
        mi.setAction(af.getInsertIndexAction());
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);
        try {
            if (model != null && model.getIndicesFolder().getChildCount() > 0) {
                JMenu menu = new JMenu(Messages.getString("TablePane.indexPropertiesMenu")); //$NON-NLS-1$
                menu.setIcon(SPSUtils.createIcon("edit_index", Messages.getString("TablePane.editIndexTooltip"), ArchitectSwingSessionContext.ICON_SIZE)); //$NON-NLS-1$ //$NON-NLS-2$
                for (SQLIndex index : model.getIndices()) {
                    JMenuItem menuItem = new JMenuItem(new EditSpecificIndexAction(getPlayPen().getSession(), index));
                    menuItem.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
                    menu.add(menuItem);
                }
                tablePanePopup.add(menu);
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }

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

        JMenu tableAppearance = new JMenu(Messages.getString("TablePane.tableAppearances")); //$NON-NLS-1$
        JMenu backgroundColours = new JMenu(Messages.getString("TableEditPanel.tableColourLabel")); //$NON-NLS-1$
        JMenu foregroundColours = new JMenu(Messages.getString("TableEditPanel.textColourLabel")); //$NON-NLS-1$
        for (final Color colour : ColorScheme.BACKGROUND_COLOURS) {
            Icon icon = new ColorIcon(60, 25, colour);
            mi = new JMenuItem(icon);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    List<TablePane> tables = getPlayPen().getSelectedTables();
                    getPlayPen().startCompoundEdit("Started setting table colour"); //$NON-NLS-1$
                    for (TablePane tp : tables) {
                        tp.setBackgroundColor(colour);
                    }
                    getPlayPen().endCompoundEdit("Finished setting table colour"); //$NON-NLS-1$
                }
            });
            backgroundColours.add(mi);
        }
        for (final Color colour : ColorScheme.FOREGROUND_COLOURS) {
            Icon icon = new ColorIcon(60, 25, colour);
            mi = new JMenuItem(icon);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    List<TablePane> tables = getPlayPen().getSelectedTables();
                    getPlayPen().startCompoundEdit("Started setting text colour"); //$NON-NLS-1$
                    for (TablePane tp : tables) {
                        tp.setForegroundColor(colour);
                    }
                    getPlayPen().endCompoundEdit("Finished setting text colour"); //$NON-NLS-1$
                }
            });
            foregroundColours.add(mi);
        }
        tableAppearance.add(backgroundColours);
        tableAppearance.add(foregroundColours);
        if (getPlayPen().findTablePane(model) != null) {
            JCheckBoxMenuItem cmi = new JCheckBoxMenuItem(
                    Messages.getString("TableEditPanel.dashedLinesLabel"), getPlayPen().findTablePane(model).isDashed()); //$NON-NLS-1$
            cmi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    List<TablePane> tables = getPlayPen().getSelectedTables();
                    getPlayPen().startCompoundEdit("Started setting dashed lines."); //$NON-NLS-1$
                    for (TablePane tp : tables) {
                        tp.setDashed(((JCheckBoxMenuItem) (e.getSource())).isSelected());
                    }
                    getPlayPen().endCompoundEdit("Finished setting dashed lines."); //$NON-NLS-1$
                }
            });
            tableAppearance.add(cmi);
            cmi = new JCheckBoxMenuItem(
                    Messages.getString("TableEditPanel.roundedCornersLabel"), getPlayPen().findTablePane(model).isRounded()); //$NON-NLS-1$
            cmi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    List<TablePane> tables = getPlayPen().getSelectedTables();
                    getPlayPen().startCompoundEdit("Started setting rounded edges."); //$NON-NLS-1$
                    for (TablePane tp : tables) {
                        tp.setRounded(((JCheckBoxMenuItem) (e.getSource())).isSelected());
                    }
                    getPlayPen().endCompoundEdit("Finished setting rounded edges."); //$NON-NLS-1$
                }
            });
            tableAppearance.add(cmi);
        }
        tablePanePopup.add(tableAppearance);

        tablePanePopup.addSeparator();

        mi = new JMenuItem();
        mi.setAction(getPlayPen().bringToFrontAction);
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);

        mi = new JMenuItem();
        mi.setAction(getPlayPen().sendToBackAction);
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);

        JMenu align = new JMenu(Messages.getString("TablePane.alignTablesMenu")); //$NON-NLS-1$
        mi = new JMenuItem();
        mi.setAction(af.getAlignTableHorizontalAction()); 
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        align.add(mi);
        
        
        mi = new JMenuItem();
        mi.setAction(af.getAlignTableVerticalAction());
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        align.add(mi);
        tablePanePopup.add(align);
        
        tablePanePopup.addSeparator();
        
        mi = new JMenuItem();
        mi.setAction(af.getDeleteSelectedAction());
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);
        

        if (logger.isDebugEnabled()) {
            tablePanePopup.addSeparator();
            mi = new JMenuItem("Show listeners"); //$NON-NLS-1$
            mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
            mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        List<PlayPenComponent> selection = getPlayPen().getSelectedItems();
                        if (selection.size() == 1) {
                            TablePane tp = (TablePane) selection.get(0);
                            JOptionPane.showMessageDialog(getPlayPen(), new JScrollPane(new JList(tp.getModel().getSQLObjectListeners().toArray())));
                        } else {
                            JOptionPane.showMessageDialog(getPlayPen(), "You can only show listeners on one item at a time"); //$NON-NLS-1$
                        }
                    }
                });
            tablePanePopup.add(mi);

            mi = new JMenuItem("Show Selection List"); //$NON-NLS-1$
            mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
            mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        List<PlayPenComponent> selection = getPlayPen().getSelectedItems();
                        if (selection.size() == 1) {
                            TablePane tp = (TablePane) selection.get(0);
                            JOptionPane.showMessageDialog(getPlayPen(), new JScrollPane(new JList(tp.selectedColumns.toArray())));
                        } else {
                            JOptionPane.showMessageDialog(getPlayPen(), "You can only show selected columns on one item at a time"); //$NON-NLS-1$
                        }
                    }
                });
            tablePanePopup.add(mi);
        }
        
        return tablePanePopup;
    }
}
