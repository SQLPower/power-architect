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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.InsertionPointWatcher;
import ca.sqlpower.architect.ProjectSettings.ColumnVisibility;
import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.swingui.action.EditSpecificIndexAction;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.LockedColumnException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.ColorIcon;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

import com.google.common.collect.ArrayListMultimap;

public class TablePane extends ContainerPane<SQLTable, SQLColumn> {

	private static final Logger logger = Logger.getLogger(TablePane.class);
	
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
	public static final List<Class<? extends SPObject>> allowedChildTypes = PlayPenComponent.allowedChildTypes;

	/**
	 * A special column index that represents the gap between the last PK column and the PK line.
	 */
    public static final int COLUMN_INDEX_END_OF_PK = -3;

    /**
     * A special column index that represents the gap between the PK line and the first non-PK column.
     */
    public static final int COLUMN_INDEX_START_OF_NON_PK = -4;
    
    /**
     * This listener will disconnect this pane from the model if the pane is removed from the
     * container.
     */
    private final AbstractSPListener containerPaneListener = new AbstractSPListener() {
        public void childRemoved(SPChildEvent e) {
            if (e.getChild() == TablePane.this) {
                destroy();
            }
        }
     };

	/**
	 * This is the column index at which to the insertion point is
	 * currently rendered. Columns will be added after this column.
	 * If it is COLUMN_INDEX_NONE, no insertion point will be
	 * rendered and columns will be added at the bottom.
	 */
	protected int insertionPoint;

	/**
	 * Tracks which columns in this table are currently hidden.
	 */
	protected Set<SQLColumn> hiddenColumns;
	
	/**
	 * Keeps tracks of whether the user wants to see the logical names or the
	 * physical names.
	 */
	boolean usingLogicalNames;

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

    private boolean fullyQualifiedNameInHeader = false;

    SPListener columnListener = new ColumnListener();

    public TablePane(TablePane tp, PlayPenContentPane parent) {
		super(tp.getName(), parent);
		this.model = tp.getModel();
		this.margin = (Insets) tp.margin.clone();
		this.columnHighlight = new HashMap<SQLColumn,List<Color>>(tp.columnHighlight);
        setMinimumSize(tp.getMinimumSize());

		for (SQLColumn c : tp.getSelectedItems()) {
		    selectItem(c);
		}
		
		this.insertionPoint = tp.insertionPoint;
		this.draggingColumn = tp.draggingColumn;
		this.selected = false;

		this.hiddenColumns = new HashSet<SQLColumn>(tp.getHiddenColumns());
		
		this.foregroundColor = tp.getForegroundColor();
		this.backgroundColor = tp.getBackgroundColor();
		setDashed(tp.isDashed());
		setRounded(tp.isRounded());
		
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

    @Constructor
	public TablePane(@ConstructorParameter(propertyName="model") SQLTable m, 
	        @ConstructorParameter(propertyName="parent") PlayPenContentPane parent) {
        super(m.getName());
        setParent(parent);
        setModel(m);
        this.hiddenColumns = new HashSet<SQLColumn>();
        setInsertionPoint(ITEM_INDEX_NONE);	    
		updateUI();
	}
	
	@Override
	protected List<SQLColumn> getItems() {
	    try {
	        return model.getColumns();
	    } catch (SQLObjectException e) {
	        throw new SQLObjectRuntimeException(e);
	    }
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

	// ---------------------- utility methods ----------------------

    /**
     * You must call this method when you are adding a TablePane component after
     * the parent is defined. It will register the necessary listeners to all
     * necessary parties.
     */
    @Override
    public void connect() {
        //Disconnect first in case the object is already connected. This ensures
        //a listener isn't addd twice.
        destroy();
        
        SQLPowerUtils.listenToHierarchy(model, columnListener);
        getParent().addSPListener(containerPaneListener);
    }

	/**
	 * You must call this method when you are done with a TablePane
	 * component.  It unregisters this instance (and its UI delegate)
	 * on all event listener lists on which it was previously
	 * registered.
	 */
	private void destroy() {
	    SQLPowerUtils.unlistenToHierarchy(model, columnListener);
	    getParent().removeSPListener(containerPaneListener);
	}

	// -------------------- sqlobject event support ---------------------

    private class ColumnListener implements SPListener {

        /**
         * The column that was most recently removed from this table while it
         * was still selected.  This is kept here in case the column is subsequently
         * reinserted into the table (possibly at a different index) so it can be
         * selected when it comes back from the dead.
         */
        private SQLColumn mostRecentSelectedRemoval;
        
        /**
         * This tracks the item that was selected in place of the most recently removed
         * item. This is handy because we don't want to leave the newly-selected item
         * selected if we restore the previously-selected item (this happens when a column
         * index is changed by removing it and then adding it right back).
         */
        private SQLColumn mostRecentSelectedReplacement;

        /**
         * Listens for property changes in the model (columns
         * or relationships added).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a PropertyChangeEvent.
         */
        public void childAdded(SPChildEvent e) {
            if (e.getSource() == getModel() && e.getChildType() == SQLColumn.class) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Column inserted. Syncing select/highlight lists. New index="+e.getIndex()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                boolean wasSelectedPreviously = (e.getChild() == mostRecentSelectedRemoval);
                final SQLColumn column = (SQLColumn) e.getChild();
                if (wasSelectedPreviously) {
                    deselectItem(mostRecentSelectedReplacement);
                    selectItem(e.getIndex());
                    logger.debug("Restored most recent selection");
                } else {
                    logger.debug("Was not the most recent selection; not restoring");
                }
                // This is only supposed to work if we deselect the columns before selecting them
                // this if stops the insert from wiping out a highlighted column
                if (columnHighlight.get(column) == null) {
                    columnHighlight.put(column, new ArrayList<Color>());
                }
            }
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
            
            updateHiddenColumns();
            firePropertyChange("model.children", null, e.getChild()); //$NON-NLS-1$
            //revalidate();
        }

        /**
         * Listens for property changes in the model (columns
         * removed).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a PropertyChangeEvent.
         */
        public void childRemoved(SPChildEvent e) {
            if (e.getSource() == model && e.getChild() instanceof SQLColumn) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Column removed. Syncing select/highlight lists. Removed index="+e.getIndex()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                SQLColumn removedCol = (SQLColumn) e.getChild();
                if (isItemSelected(removedCol)) {
                    int removedIdx = e.getIndex();
                    deselectItem(removedCol);
                    mostRecentSelectedRemoval = removedCol;
                    if (getItems().isEmpty()) {
                        mostRecentSelectedReplacement = null;
                    } else {
                        mostRecentSelectedReplacement = getItems().get(Math.min(removedIdx, getItems().size() - 1));
                    }
                    selectItem(mostRecentSelectedReplacement);
                    logger.debug("Remembering as most recent selection: " + mostRecentSelectedRemoval);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Not remembering as recent selection. Selected items: " + getSelectedItems());
                    }
                }
            }
            
            // no matter where the event came from, we should no longer be listening to the removed children
            SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
//            updateNameDisplay();
            updateHiddenColumns();
            firePropertyChange("model.children", e.getChild(), null); //$NON-NLS-1$
            //revalidate();
        }

        /**
         * Listens for property changes in the model (columns
         * properties modified).  If this change affects the appearance of
         * this widget, we will notify all change listeners (the UI
         * delegate) with a ChangeEvent.
         */
        public void propertyChanged(PropertyChangeEvent e) {
            if (logger.isDebugEnabled()) {
                logger.debug("TablePane got object changed event." + //$NON-NLS-1$
                        "  Source="+e.getSource()+" Property="+e.getPropertyName()+ //$NON-NLS-1$ //$NON-NLS-2$
                        " oldVal="+e.getOldValue()+" newVal="+e.getNewValue() +  //$NON-NLS-1$ //$NON-NLS-2$
                        " selectedItems=" + getSelectedItems());
            }
//            updateNameDisplay();
            updateHiddenColumns();
            firePropertyChange("model."+e.getPropertyName(), e.getOldValue(), e.getNewValue()); //$NON-NLS-1$
            //repaint();
        }

        public void transactionEnded(TransactionEvent e) {
            // no-op
        }

        public void transactionRollback(TransactionEvent e) {
            // no-op            
        }

        public void transactionStarted(TransactionEvent e) {
            // no-op            
        }

    }

	// ----------------------- accessors and mutators --------------------------

	/**
	 * Attaches this table pane to the given table. Once attached, this table
	 * pane instance cannot be reused for a different table.
	 *
	 * @param m the table to attach to
	 */
	private void setModel(SQLTable m) {
		if (model != null) {
			throw new IllegalStateException("model already set to " + model); //$NON-NLS-1$
		}

		model = m;

		try {
			columnHighlight = new HashMap<SQLColumn,List<Color>>();
			for (SQLColumn column: model.getColumns()) {
				columnHighlight.put(column, new ArrayList<Color>());
			}
		} catch (SQLObjectException e) {
			logger.error("Error getting children on new model", e); //$NON-NLS-1$
		}

		connect();
	}

	@Override
	public String getModelName() {
	    if (model == null) {
	        return null;
	    } else {
	        return model.getName();
	    }
	}
	
	/**
	 * See {@link #insertionPoint}.
	 */
	@Transient @Accessor
	public int getInsertionPoint() {
		return insertionPoint;
	}

	/**
	 * See {@link #insertionPoint}.
	 */
	@Transient @Mutator
	public void setInsertionPoint(int ip) {
		int old = insertionPoint;
		this.insertionPoint = ip;
		if (ip != old) {
			firePropertyChange("insertionPoint", new Integer(old), new Integer(insertionPoint)); //$NON-NLS-1$
			repaint();
		}
	}

	public void updateHiddenColumns() {
	    hiddenColumns.clear();
	    ArchitectSwingSession session = getPlayPen().getSession();
	    ColumnVisibility choice = session.getColumnVisibility();

	    // if all the boxes are checked, then hide no columns, only these 3 need be
	    // checked. Draw a truth table if you don't believe me.
	    if(!choice.equals(ColumnVisibility.ALL)) {
	        // start with a list of all the columns, then remove the ones that 
	        // should be shown
	        hiddenColumns.addAll(getItems());
	        for (SQLColumn col : getItems()) {
	            if (col.isPrimaryKey()) {
	                hiddenColumns.remove(col);
	            } 
	            if (!choice.equals(ColumnVisibility.PK) && col.isForeignKey()) {
	                hiddenColumns.remove(col);
	            }
	            if (!choice.equals(ColumnVisibility.PK) && !choice.equals(ColumnVisibility.PK_FK) && col.isUniqueIndexed()) {
	                hiddenColumns.remove(col);
	            }
	            if (!choice.equals(ColumnVisibility.PK) && !choice.equals(ColumnVisibility.PK_FK) &&
	                    !choice.equals(ColumnVisibility.PK_FK_UNIQUE) && col.isIndexed()) {
	                hiddenColumns.remove(col);
	            }
	        }
	    }
	}
	
	@Transient @Accessor
	public boolean isUsingLogicalNames() {
	    PlayPen pp = getPlayPen();
	    if (pp != null) {
	        ArchitectSwingSession session = pp.getSession();
	        if (session != null) {
	            return session.isUsingLogicalNames();
	        }
	    }
	    return true;
	}
	
	public void updateNameDisplay() {
        ArchitectSwingSession session = getPlayPen().getSession();
        usingLogicalNames = session.isUsingLogicalNames();

    }

	// ------------------ utility methods ---------------------

	@Override
	@Deprecated
    public int pointToItemIndex(Point p) {
        return ((TablePaneUI) getUI()).pointToItemIndex(p);
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
    public int columnIndexToCentreY(int colidx) {
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
	 * @throws SQLObjectException If there are problems in the business model
	 */
	public boolean insertObjects(List<? extends SQLObject> items, int insertionPoint, boolean deleteSource) throws SQLObjectException {
		boolean newColumnsInPk = false;
		if (insertionPoint == COLUMN_INDEX_END_OF_PK) {
		    insertionPoint = getModel().getPkSize();
		    newColumnsInPk = true;
		} else if (insertionPoint == COLUMN_INDEX_START_OF_NON_PK) {
		    insertionPoint = getModel().getPkSize();
		    newColumnsInPk = false;
		} else if (insertionPoint == ITEM_INDEX_TITLE) {
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
			DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(getParent().getPlayPen().getSession(), someData);
			logger.debug("insertObjects: got item of type "+someData.getClass().getName()); //$NON-NLS-1$
			if (someData instanceof SQLTable) {
			    SQLTable table = (SQLTable) someData;
			    getModel().inherit(insertionPoint, table, duplicateProperties.getDefaultTransferStyle(), duplicateProperties.isPreserveColumnSource());
			    for (SQLColumn column : table.getColumns()) {
			        SQLColumn targetCol = getModel().getColumnByName(column.getName());
			        ASUtils.correctSourceColumn(column, duplicateProperties, targetCol, getPlayPen().getSession().getDBTree());
			    }
			} else if (someData instanceof SQLColumn) {
			    SQLColumn col = (SQLColumn) someData;
			    if (deleteSource) {
			        if (col.getParent() == getModel()) {
			            // moving column inside the same table
			            int oldIndex = col.getParent().getColumns().indexOf(col);
			            if (insertionPoint > oldIndex) {
			                insertionPoint--;
			            }
			            getModel().changeColumnIndex(oldIndex, insertionPoint, newColumnsInPk);
			        } else if (col.getParent().getParentDatabase() == getModel().getParentDatabase()) {
			            // moving column within playpen

			            InsertionPointWatcher<SQLTable> ipWatcher =
			                new InsertionPointWatcher<SQLTable>(getModel(), insertionPoint, SQLColumn.class);
			            col.getParent().removeColumn(col);
			            ipWatcher.dispose();

			            if (logger.isDebugEnabled()) {
			                logger.debug("Moving column '"+col.getName() //$NON-NLS-1$
			                        +"' to table '"+getModel().getName() //$NON-NLS-1$
			                        +"' at position "+ipWatcher.getInsertionPoint()); //$NON-NLS-1$
			            }
			            getModel().addColumn(col, newColumnsInPk, ipWatcher.getInsertionPoint());
			            // You need to disable the normalization otherwise it goes around
			            // the property change events and causes undo to fail when dragging
			            // into the primary key of a table
			            logger.debug("Column listeners are " + col.getSPListeners());

			        } else {
			            // importing column from a source database
			            getModel().inherit(insertionPoint, col, newColumnsInPk, duplicateProperties.getDefaultTransferStyle(), duplicateProperties.isPreserveColumnSource());
			            if (logger.isDebugEnabled()) logger.debug("Inherited "+col.getName()+" to table with precision " + col.getPrecision()); //$NON-NLS-1$ //$NON-NLS-2$
			        }

			    } else {
			        getModel().inherit(insertionPoint, col, newColumnsInPk, duplicateProperties.getDefaultTransferStyle(), duplicateProperties.isPreserveColumnSource());
			        if (logger.isDebugEnabled()) logger.debug("Inherited "+col.getName()+" to table with precision " + col.getPrecision()); //$NON-NLS-1$ //$NON-NLS-2$
			        ASUtils.correctSourceColumn(col, duplicateProperties, getModel().getColumnByName(col.getName()), getPlayPen().getSession().getDBTree());
			    }
			} else {
				return false;
			}
		}
		
		return true;
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
        if (columnHighlight.get(column) == null) {
            columnHighlight.put(column, new ArrayList<Color>());
        }
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
     * @throws SQLObjectException
     */
    @NonBound
    public Color getColumnHighlight(int i) throws SQLObjectException {
        return getColumnHighlight(model.getColumn(i));
    }

    @NonBound
    public Color getColumnHighlight(SQLColumn column) {
        logger.debug("Checking column "+column); //$NON-NLS-1$
        if (columnHighlight.get(column) == null || columnHighlight.get(column).isEmpty()) {
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

    @Transient @Mutator
    public void setFullyQualifiedNameInHeader(boolean fullyQualifiedNameInHeader) {
        this.fullyQualifiedNameInHeader = fullyQualifiedNameInHeader;
    }

    @Transient @Accessor
    public boolean isFullyQualifiedNameInHeader() {
        return fullyQualifiedNameInHeader;
    }


    // ------------------------ DROP TARGET LISTENER ------------------------

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
            logger.debug("DragEnter event on "+getName()); //$NON-NLS-1$
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
            logger.debug("DragExit event on "+getName()); //$NON-NLS-1$
        }
        setInsertionPoint(ITEM_INDEX_NONE);
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
            logger.debug("DragOver event on "+getName()+": "+dtde); //$NON-NLS-1$ //$NON-NLS-2$
            logger.debug("Drop Action = "+dtde.getDropAction()); //$NON-NLS-1$
            logger.debug("Source Actions = "+dtde.getSourceActions()); //$NON-NLS-1$
        }
        dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE & dtde.getDropAction());
        Point loc = getPlayPen().unzoomPoint(new Point(dtde.getLocation()));
        loc.x -= getX();
        loc.y -= getY();
        int idx = pointToItemIndex(loc);
        setInsertionPoint(idx);
    }
    
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        if (dsde.getDropSuccess()) {
            logger.debug("Succesful drop"); //$NON-NLS-1$
        } else {
            logger.debug("Unsuccesful drop"); //$NON-NLS-1$
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
        PlayPen pp = getPlayPen();
        Point loc = pp.unzoomPoint(new Point(dtde.getLocation()));
        loc.x -= getX();
        loc.y -= getY();

        logger.debug("Drop target drop event on "+getName()+": "+dtde); //$NON-NLS-1$ //$NON-NLS-2$
        Transferable t = dtde.getTransferable();
        DataFlavor importFlavor = bestImportFlavor(pp, t.getTransferDataFlavors());
        if (importFlavor == null) {
            dtde.rejectDrop();
            setInsertionPoint(ITEM_INDEX_NONE);
        } else {
            try {
                pp.startCompoundEdit("Drag and drop");
                if ((dtde.getSourceActions() & dtde.getDropAction()) == 0) {
                    dtde.rejectDrop();
                    return;
                }
                
                boolean success = false;
                success = addTransferable(loc, t, importFlavor, dtde.getDropAction() == DnDConstants.ACTION_MOVE);
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
                ASUtils.showExceptionDialogNoReport(getParent().getPlayPen(),
                        "Error processing drop operation", ex); //$NON-NLS-1$
            } finally {
                setInsertionPoint(ITEM_INDEX_NONE);
                pp.endCompoundEdit("Ending drag and drop");
            }
        }
    }


    /**
     * This will add data from a transferable to the current table if the importFlavor
     * given is appropriate for the table. The values will be at the given point in the
     * table. The values will be moved instead of copied if the movingObjects flag is true.
     * If the movingObjects flag is false then the data will be copied.
     */
    private boolean addTransferable(Point loc, Transferable t, DataFlavor importFlavor, boolean deleteSource)
            throws UnsupportedFlavorException, IOException, SQLObjectException {
        boolean success;
        PlayPen pp = getPlayPen();
        try {
            int insertionPoint = pointToItemIndex(loc);

            List<SQLObject> droppedItems;
            if (importFlavor == SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR) {
                droppedItems = Arrays.asList((SQLObject[]) t.getTransferData(importFlavor));
            } else if (importFlavor == DataFlavor.stringFlavor) {
                String[] stringPieces = ((String) t.getTransferData(DataFlavor.stringFlavor)).split("[\n\r\t]+");
                droppedItems = new ArrayList<SQLObject>();
                for (String s : stringPieces) {
                    if (s.length() > 0) {
                        SQLColumn newCol = new SQLColumn();
                        newCol.setName(s);
                        droppedItems.add(newCol);
                    }
                }
            } else {
                return false;
            }

            logger.debug("Importing items: " + droppedItems); //$NON-NLS-1$


            //Check to see if the drag and drop will change the current relationship
            List<SQLRelationship> importedKeys = SQLRelationship.getExportedKeys(getModel().getImportedKeys());

            boolean newColumnsInPk = false;
            if (insertionPoint == TablePane.COLUMN_INDEX_END_OF_PK) {
                newColumnsInPk = true;
            } else if (insertionPoint == TablePane.COLUMN_INDEX_START_OF_NON_PK) {
                newColumnsInPk = false;
            } else if (insertionPoint == ITEM_INDEX_TITLE) {
                newColumnsInPk = true;
            } else if (insertionPoint < 0) {
                newColumnsInPk = false;
            } else if (insertionPoint < getModel().getPkSize()) {
                newColumnsInPk = true;
            }

            try {
                ArrayListMultimap<String, SQLColumn> droppedColumns = ArrayListMultimap.create();
                for (SQLObject o : droppedItems) {
                    if (o instanceof SQLColumn) {
                        String fromDataSource;
                        SQLTable parent = ((SQLColumn) o).getParent();
                        if (parent != null) {
                            fromDataSource = parent.getParentDatabase().getDataSource().getParentType().getName();
                        } else {
                            fromDataSource = null;
                        }
                        droppedColumns.put(fromDataSource, (SQLColumn) o);
                    } else if (o instanceof SQLTable) {
                        droppedColumns.putAll(((SQLTable) o).getParentDatabase().getDataSource().getParentType().getName(), ((SQLTable) o).getChildren(SQLColumn.class));
                    }
                }
                
                for (int i = 0; i < importedKeys.size(); i++) {
                    // Not dealing with self-referencing tables right now.
                    if (importedKeys.get(i).getPkTable().equals(importedKeys.get(i).getFkTable())) continue;  
                    for (Entry<String, SQLColumn> entry : droppedColumns.entries()) {
                        if (importedKeys.get(i).containsFkColumn(entry.getValue())) {
                            importedKeys.get(i).setIdentifying(newColumnsInPk);
                            break;
                        }
                    }
                }
                
                DataSourceCollection<SPDataSource> dsCollection = getModel().getParentDatabase().getDataSource().getParentCollection();
                
                // Note that it is safe to assign types to previously assigned
                // columns, they will be ignored.
                for (String platform : droppedColumns.keySet()) {
                    SQLColumn.assignTypes(droppedColumns.get(platform), dsCollection, platform, getPlayPen().getSession());
                }
                
                ArchitectProject project = this.getParent().getParent();
                success = false;
                try {
                    project.begin("Inserting column(s) into table");
                    success = insertObjects(droppedItems, insertionPoint, deleteSource);
                    if (success) project.commit();
                } catch (Throwable e) {                  
                    if (e instanceof SQLObjectException) {
                        throw new SQLObjectException(e);
                    } else {
                        throw new RuntimeException(e);
                    }
                } finally {
                    if (!success) {
                        project.rollback("Was unsuccessful inserting objects into table");
                    }
                }
            } catch (LockedColumnException ex ) {
                if (logger.isDebugEnabled()) {
                    ex.printStackTrace();
                }
                JOptionPane.showConfirmDialog(pp,
                        "Could not delete the column " + //$NON-NLS-1$
                        ex.getCol().getName() +
                        " because it is part of\n" + //$NON-NLS-1$
                        "the relationship \""+ex.getLockingRelationship()+"\".\n\n", //$NON-NLS-1$ //$NON-NLS-2$
                        "Column is Locked", //$NON-NLS-1$
                        JOptionPane.CLOSED_OPTION);
                success = false;
            } 

        } finally {
            setInsertionPoint(ITEM_INDEX_NONE);
        }
        return success;
    }
    
    public void pasteData(Transferable t) {
        Point loc = getPlayPen().unzoomPoint(getPlayPen().getMousePosition());
        if (loc == null) {
            loc = new Point(0, getHeight());
        } else {
            loc.x -= getX();
            loc.y -= getY();
        }
        
        DataFlavor bestImportFlavor = null;
        try {
            getPlayPen().startCompoundEdit("Transfering data"); //$NON-NLS-1$
            
            bestImportFlavor = bestImportFlavor(getPlayPen(), t.getTransferDataFlavors());
            addTransferable(loc, t, bestImportFlavor, false);
        } catch (UnsupportedFlavorException e) {
            throw new RuntimeException("Cannot add items to a table of type " + bestImportFlavor, e);
        } catch (IOException e) {
            throw new RuntimeException("Transfer type changed while adding it to the table", e);
        } catch (SQLObjectException e) {
            throw new RuntimeException("Unknown object type to add to a table", e);
        } finally {
            getPlayPen().endCompoundEdit("End transfering data"); //$NON-NLS-1$
        }
    }

    /**
     * Called if the user has modified the current drop gesture.
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
        logger.debug("");
        
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


            if (flavors[i].equals(SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR)) {
                logger.debug("YES"); //$NON-NLS-1$
                return flavors[i];
            }
        }
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(DataFlavor.stringFlavor)) {
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

    @Transient @Accessor
    public List<LayoutEdge> getInboundEdges() {
        try {
            List<SQLRelationship> relationships = SQLRelationship.getExportedKeys(getModel().getImportedKeys());
            List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
            for (SQLRelationship r : relationships) {
                edges.add(getPlayPen().findRelationship(r));
            }
            return edges;
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }

    @Transient @Accessor
    public List<LayoutEdge> getOutboundEdges() {
        try {
            List<SQLRelationship> relationships = getModel().getExportedKeys();
            List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
            for (SQLRelationship r : relationships) {
                edges.add(getPlayPen().findRelationship(r));
            }
            return edges;
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }

    /**
     * Returns the number of hidden primary key columns
     */
    @Transient @Accessor
    public int getHiddenPkCount() {
        int count = 0;
        for (SQLColumn c : hiddenColumns) {
            if (c.isPrimaryKey()) {
                count++;
            }
        }
        return count;
    }
    
    @Transient @Accessor
    public boolean isShowPkTag(){
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) return session.isShowPkTag();
        }
        return true;
    }
    
    @Transient @Accessor
    public boolean isShowFkTag(){
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) return session.isShowFkTag();
        }
        return true;
    }
    
    @Transient @Accessor
    public boolean isShowAkTag(){
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) return session.isShowAkTag();
        }
        return true;
    }
    
    @Transient @Accessor
    public Set<SQLColumn> getHiddenColumns() {
        return hiddenColumns;
    }
    
    /**
     * Filters the list returned from {@link PlayPen#getSelectedContainers()}
     * and return a new unmodifiable list containing only instances of TablePane
     */
    private List<TablePane> getSelectedTablePanes() {
        List<TablePane> selectedTables = new ArrayList<TablePane>();
        for (ContainerPane<?, ?> cp : getPlayPen().getSelectedContainers()) {
            if (cp instanceof TablePane) {
                selectedTables.add((TablePane) cp);
            }
        }
        return Collections.unmodifiableList(selectedTables);
    }
    
    /**
     * Returns an instance of the popup menu with menu items exclusive to
     * manipulating tablepanes.
     */
    @Override @NonBound
    public JPopupMenu getPopup(Point p) {
        ArchitectFrame af = getPlayPen().getSession().getArchitectFrame();
        JPopupMenu tablePanePopup = new JPopupMenu();
        
        JMenuItem mi;
        
        mi = new JMenuItem();
        mi.setAction(af.getInsertIndexAction());
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);
        try {
            if (model != null && model.getIndices().size() > 0) {
                JMenu menu = new JMenu(Messages.getString("TablePane.indexPropertiesMenu")); //$NON-NLS-1$
                menu.setIcon(SPSUtils.createIcon("edit_index", Messages.getString("TablePane.editIndexTooltip"), ArchitectSwingSessionContext.ICON_SIZE)); //$NON-NLS-1$ //$NON-NLS-2$
                for (SQLIndex index : model.getIndices()) {
                    JMenuItem menuItem = new JMenuItem(new EditSpecificIndexAction(getPlayPen().getSession(), index));
                    menuItem.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
                    menu.add(menuItem);
                }
                tablePanePopup.add(menu);
            }
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        }

        tablePanePopup.addSeparator();

        mi = new JMenuItem();
        mi.setAction(af.getInsertColumnAction());
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);

        mi = new JMenuItem();
        mi.setAction(af.getEditColumnAction());
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);

        tablePanePopup.addSeparator();

        JMenu align = new JMenu(Messages.getString("TablePane.alignTablesMenu")); //$NON-NLS-1$
        mi = new JMenuItem();
        mi.setAction(af.getAlignTableHorizontalAction()); 
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        align.add(mi);
        
        
        mi = new JMenuItem();
        mi.setAction(af.getAlignTableVerticalAction());
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        align.add(mi);
        tablePanePopup.add(align);

        JMenu tableAppearance = new JMenu(Messages.getString("TablePane.tableAppearances")); //$NON-NLS-1$
        JMenu backgroundColours = new JMenu(Messages.getString("TableEditPanel.tableColourLabel")); //$NON-NLS-1$
        JMenu foregroundColours = new JMenu(Messages.getString("TableEditPanel.textColourLabel")); //$NON-NLS-1$
        for (final Color colour : ColourScheme.BACKGROUND_COLOURS) {
            Icon icon = new ColorIcon(60, 25, colour);
            mi = new JMenuItem(icon);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getPlayPen().startCompoundEdit("Started setting table colour"); //$NON-NLS-1$
                    for (TablePane tp : getSelectedTablePanes()) {
                        tp.setBackgroundColor(colour);
                    }
                    getPlayPen().endCompoundEdit("Finished setting table colour"); //$NON-NLS-1$
                }
            });
            backgroundColours.add(mi);
        }
        for (final Color colour : ColourScheme.FOREGROUND_COLOURS) {
            Icon icon = new ColorIcon(60, 25, colour);
            mi = new JMenuItem(icon);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getPlayPen().startCompoundEdit("Started setting text colour"); //$NON-NLS-1$
                    for (TablePane tp : getSelectedTablePanes()) {
                        tp.setForegroundColor(colour);
                    }
                    getPlayPen().endCompoundEdit("Finished setting text colour"); //$NON-NLS-1$
                }
            });
            foregroundColours.add(mi);
        }
        tableAppearance.add(backgroundColours);
        tableAppearance.add(foregroundColours);
        JCheckBoxMenuItem cmi = new JCheckBoxMenuItem(
                Messages.getString("TableEditPanel.dashedLinesLabel"), isDashed()); //$NON-NLS-1$
        cmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getPlayPen().startCompoundEdit("Started setting dashed lines."); //$NON-NLS-1$
                for (TablePane tp : getSelectedTablePanes()) {
                    tp.setDashed(((JCheckBoxMenuItem) (e.getSource())).isSelected());
                }
                getPlayPen().endCompoundEdit("Finished setting dashed lines."); //$NON-NLS-1$
            }
        });
        tableAppearance.add(cmi);
        cmi = new JCheckBoxMenuItem(
                Messages.getString("TableEditPanel.roundedCornersLabel"), isRounded()); //$NON-NLS-1$
        cmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getPlayPen().startCompoundEdit("Started setting rounded edges."); //$NON-NLS-1$
                for (TablePane tp : getSelectedTablePanes()) {
                    tp.setRounded(((JCheckBoxMenuItem) (e.getSource())).isSelected());
                }
                getPlayPen().endCompoundEdit("Finished setting rounded edges."); //$NON-NLS-1$
            }
        });
        tableAppearance.add(cmi);
        tablePanePopup.add(tableAppearance);
        
        mi = new JMenuItem();
        mi.setAction(af.getEditTableAction());
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);
        
        if (!getPlayPen().getSession().isEnterpriseSession()) {
            tablePanePopup.addSeparator();
            
            mi = new JMenuItem();
            mi.setAction(getPlayPen().bringToFrontAction);
            mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
            tablePanePopup.add(mi);

            mi = new JMenuItem();
            mi.setAction(getPlayPen().sendToBackAction);
            mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
            tablePanePopup.add(mi);
        }

        if (logger.isDebugEnabled()) {
            tablePanePopup.addSeparator();
            mi = new JMenuItem("Show listeners"); //$NON-NLS-1$
            mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
            mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        List<PlayPenComponent> selection = getPlayPen().getSelectedItems();
                        if (selection.size() == 1) {
                            TablePane tp = (TablePane) selection.get(0);
                            JOptionPane.showMessageDialog(getPlayPen(), new JScrollPane(new JList(tp.getModel().getSPListeners().toArray())));
                        } else {
                            JOptionPane.showMessageDialog(getPlayPen(), "You can only show listeners on one item at a time"); //$NON-NLS-1$
                        }
                    }
                });
            tablePanePopup.add(mi);

            mi = new JMenuItem("Show Selection List"); //$NON-NLS-1$
            mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
            mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        List<PlayPenComponent> selection = getPlayPen().getSelectedItems();
                        if (selection.size() == 1) {
                            TablePane tp = (TablePane) selection.get(0);
                            JOptionPane.showMessageDialog(getPlayPen(), new JScrollPane(new JList(tp.getSelectedItems().toArray())));
                        } else {
                            JOptionPane.showMessageDialog(getPlayPen(), "You can only show selected columns on one item at a time"); //$NON-NLS-1$
                        }
                    }
                });
            tablePanePopup.add(mi);
        }
        
        tablePanePopup.addSeparator();
        mi = new JMenuItem();
        mi.setAction(af.getDeleteSelectedAction());
        mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        tablePanePopup.add(mi);
        
        return tablePanePopup;
    }
    
    @Override
    public void handleMouseEvent(MouseEvent evt) {
        super.handleMouseEvent(evt);
        
        PlayPen pp = getPlayPen();

        Point p = evt.getPoint();
        pp.unzoomPoint(p);
        p.translate(-getX(), -getY());
        if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
            if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                int selectedColIndex = pointToItemIndex(p);
                if (evt.getClickCount() == 2) { // double click
                    if (isSelected()) {
                        ArchitectFrame af = pp.getSession().getArchitectFrame();
                        if (selectedColIndex == ITEM_INDEX_TITLE) {
                            af.getEditTableAction().actionPerformed
                            (new ActionEvent(TablePane.this, ActionEvent.ACTION_PERFORMED, PlayPen.ACTION_COMMAND_SRC_PLAYPEN));
                        } else if (selectedColIndex >= 0) {
                            af.getEditColumnAction().actionPerformed
                            (new ActionEvent(TablePane.this, ActionEvent.ACTION_PERFORMED, PlayPen.ACTION_COMMAND_SRC_PLAYPEN));
                        }
                    }
                }
            }
        }
    }

    public Transferable createTransferableForSelection() {
        if (getSelectedItems().isEmpty()) {
            return null;
        } else {
            return new SQLObjectSelection(new ArrayList<SQLObject>(getSelectedItems()));
        }
    }
}
