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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.LockedColumnException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.swingui.PlayPen.FloatingContainerPaneListener;
import ca.sqlpower.architect.swingui.PlayPen.MouseModeType;
import ca.sqlpower.architect.swingui.event.ItemSelectionEvent;
import ca.sqlpower.architect.swingui.event.ItemSelectionListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;

/**
 * A playpen component that represents a model with a list of individually selectable items. 
 *
 * @param <T> Class of the model
 * @param <C> Class of the an item.
 */
public abstract class ContainerPane<T extends Object, C extends Object>
extends PlayPenComponent
implements DragSourceListener {
    
    /**
     * A special item index that represents the titlebar area.
     */
    public static final int ITEM_INDEX_TITLE = -1;

    /**
     * A special item index that means "no location."
     */
    public static final int ITEM_INDEX_NONE = -2;
    
    private static final Logger logger = Logger.getLogger(ContainerPane.class);
    
    private boolean rounded;
    private boolean dashed;
    
    protected T model;
    
    /**
     * How many pixels should be left between the surrounding box and the item name labels.
     */
    protected Insets margin = new Insets(1,1,1,1);

    /**
     * Tracks which items in this container are currently selected.
     */
    protected final Set<C> selectedItems = new HashSet<C>();

    protected ContainerPane(PlayPenContentPane parent) {
        super(parent);
        this.backgroundColor = new Color(240, 240, 240);
        this.foregroundColor = Color.BLACK;
        setOpaque(true);
    }

    @Override
    public T getModel() {
        return model;
    }

    @Override
    public void handleMouseEvent(MouseEvent evt) {
        PlayPen pp = getPlayPen();
        
        Point p = evt.getPoint();
        pp.unzoomPoint(p);
        p.translate(-getX(), -getY());

        if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
            if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON1){ 
                int selectedItemIndex = pointToItemIndex(p);
                if (selectedItemIndex > ITEM_INDEX_TITLE && componentPreviouslySelected){
                    deselectItem(selectedItemIndex);
                } else if (isSelected() && componentPreviouslySelected) {
                    setSelected(false, SelectionEvent.SINGLE_SELECT);
                }
            }
        } else if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            componentPreviouslySelected = false;
            int clickItem = pointToItemIndex(p);

            if (pp.getMouseMode() != MouseModeType.CREATING_TABLE) {
                if ((evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == 0) {
                    if (!isSelected() || pp.getMouseMode() == MouseModeType.IDLE) {
                        pp.setMouseMode(MouseModeType.SELECT_TABLE);
                        pp.selectNone();
                    }
                } else {
                    pp.setMouseMode(MouseModeType.MULTI_SELECT);
                }

                // Alt-click drags table no matter where you clicked
                if ((evt.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) {
                    clickItem = ITEM_INDEX_TITLE;
                }

                if (clickItem > ITEM_INDEX_TITLE &&
                        clickItem < getItems().size()) {

                    if ((evt.getModifiersEx() &
                            (InputEvent.SHIFT_DOWN_MASK |
                                    InputEvent.CTRL_DOWN_MASK)) == 0) {

                        if (!isItemSelected(clickItem) ){
                            deSelectEverythingElse(evt);
                            selectNone();
                        }
                        pp.setMouseMode(MouseModeType.SELECT_ITEM);
                    }
                    if (isItemSelected(clickItem)) {
                        componentPreviouslySelected = true;
                    } else {
                        selectItem(clickItem);
                    }

                    fireSelectionEvent(new SelectionEvent(this, SelectionEvent.SELECTION_EVENT,SelectionEvent.SINGLE_SELECT));
                    repaint();
                }
                if (isSelected()&& clickItem == ITEM_INDEX_TITLE){
                    componentPreviouslySelected = true;
                } else {
                    setSelected(true,SelectionEvent.SINGLE_SELECT);
                }
            }

            if (clickItem == ITEM_INDEX_TITLE && !pp.getSession().getArchitectFrame().createRelationshipIsActive()) {
                Iterator<ContainerPane<?, ?> > it = pp.getSelectedContainers().iterator();
                logger.debug("event point: " + p); //$NON-NLS-1$
                logger.debug("zoomed event point: " + pp.zoomPoint(new Point(p))); //$NON-NLS-1$
                pp.draggingTablePanes = true;

                while (it.hasNext()) {
                    // create FloatingContainerPaneListener for each selected item
                    ContainerPane<?, ?> cp = (ContainerPane<?, ?> )it.next();
                    logger.debug("(" + cp.getModel() + ") zoomed selected containerPane's point: " + cp.getLocationOnScreen()); //$NON-NLS-1$ //$NON-NLS-2$
                    logger.debug("(" + cp.getModel() + ") unzoomed selected containerPane's point: " + pp.unzoomPoint(cp.getLocationOnScreen())); //$NON-NLS-1$ //$NON-NLS-2$
                    /* the floating ContainerPane listener expects zoomed handles which are relative to
                               the location of the ContainerPane column which was clicked on.  */
                    Point clickedItem = getLocationOnScreen();
                    Point otherContainer = cp.getLocationOnScreen();
                    logger.debug("(" + cp.getModel() + ") translation x=" //$NON-NLS-1$ //$NON-NLS-2$
                            + (otherContainer.getX() - clickedItem.getX()) + ",y=" //$NON-NLS-1$
                            + (otherContainer.getY() - clickedItem.getY()));
                    Point handle = pp.zoomPoint(new Point(p));
                    handle.translate((int)(clickedItem.getX() - otherContainer.getX()), (int) (clickedItem.getY() - otherContainer.getY()));
                    new FloatingContainerPaneListener(pp, cp, handle);
                }
            }
        } else if (evt.getID() == MouseEvent.MOUSE_MOVED || evt.getID() == MouseEvent.MOUSE_DRAGGED) {
            setSelected(pp.rubberBand.intersects(getBounds(new Rectangle())),SelectionEvent.SINGLE_SELECT);
        } 
    }


    /**
     * Deselects everything <b>except</b> the selected item.  This method exists
     * to stop multiple selection events from propagating into the
     * CreateRelationshipAction listeners.
     */
    protected void deSelectEverythingElse (MouseEvent evt) {
        Iterator<? extends PlayPenComponent> it = getPlayPen().getSelectedContainers().iterator();
        while (it.hasNext()) {
            ContainerPane<?, ?> cp = (ContainerPane<?, ?> ) it.next();
            if (logger.isDebugEnabled()) {
                logger.debug("(" + getModel() + ") zoomed selected containerPane's located point: " + getLocationOnScreen()); //$NON-NLS-1$ //$NON-NLS-2$
                logger.debug("(" + cp.getModel() + ") zoomed iterating containerPane's point: " + cp.getLocationOnScreen()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!getLocationOnScreen().equals(cp.getLocationOnScreen())) { // equals operation might not work so good here
                // unselect
                logger.debug("found matching containerPane!"); //$NON-NLS-1$
                cp.setSelected(false,SelectionEvent.SINGLE_SELECT);
                cp.selectNone();
            }
        }

        // also de-select all the selected relationships
        it = getPlayPen().getSelectedRelationShips().iterator();
        while (it.hasNext()) {
            Relationship r = (Relationship) it.next();
            r.setSelected(false,SelectionEvent.SINGLE_SELECT);
        }
    }

    public Point getLocationOnScreen() {
        Point p = new Point();
        PlayPen pp = getPlayPen();
        getLocation(p);
        pp.zoomPoint(p);
        SwingUtilities.convertPointToScreen(p, pp);
        return p;
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
     * Gets the value of margin
     *
     * @return the value of margin
     */
    public Insets getMargin()  {
        return this.margin;
    }
    
    /**
     * Returns a list of the items to be displayed with the model.  
     */
    protected abstract List<C> getItems();
    
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
     * Overridden so that the items get deselected when the whole container
     * is deselected.
     */
    @Override
    public void setSelected(boolean isSelected, int multiSelectType) {
        if (isSelected == false) {
            selectNone();
        }
        super.setSelected(isSelected, multiSelectType);
    }

    // --------------------- item selection support --------------------
    
    @Deprecated
    public abstract int pointToItemIndex(Point p);

    /**
     * Deselects all items in this ContainerPane.
     */
    public void selectNone() {
        Set<C> previouslySelectedItems = new HashSet<C>(selectedItems);
        selectedItems.clear();
        fireItemsDeselected(previouslySelectedItems);
        repaint();
    }


    /**
     * Deselects the item, if i < 0, {@link #selectNone()} is called.
     * 
     * @param i index to {@link #getItems()}
     */
    public void deselectItem(int i) {
        if (i < 0) {
            selectNone();
        } else {
            C item = getItems().get(i);
            deselectItem(item);
        }
    }

    /**
     * Deselects the given item.
     * 
     * @param item the item to deselect.
     */
    public void deselectItem(C item) {
        selectedItems.remove(item);
        fireItemsDeselected(Collections.singleton(item));
        repaint();
    }

    /**
     * Selects the item, firing an ItemSelectionEvent. If i < 0,
     * {@link #selectNone()} is called.
     * 
     * @param i index to {@link #getItems()}
     */
    public void selectItem(int i) {
        if (i < 0) {
            selectNone();
        } else {
            C item = getItems().get(i);
            selectItem(item);
        }
    }

    /**
     * Selects the item, firing an ItemSelectionEvent.
     * 
     * @param item The item to select.
     */
    public void selectItem(C item) {
        selectedItems.add(item);
        fireItemsSelected(Collections.singleton(item));
        repaint();
    }

    /**
     * Returns true if the item at the given index is selected.
     * 
     * @param i index from {@link #getItems()}
     */
    public boolean isItemSelected(int i) {
        return selectedItems.contains(getItems().get(i));
    }

    /**
     * Returns true if the given item was selected in this container pane since
     * the last time {@link #selectNone()} was called, even if it has
     * subsequently been removed from the model. This comes in handy in event
     * listeners that want to know if a recently-removed item was selected at
     * the time it was removed.
     * 
     * @param item
     *            The item to check
     * @return true if item is currently selected, or was selected at the time
     *         it was removed.
     */
    public boolean isItemSelected(C item) {
        return selectedItems.contains(item);
    }
    
    /**
     * Returns a list of the items that are currently in the selection that
     * also currently exist in the model. Sometimes, especially when handling
     * remove events, you will want to know if the item that was just removed
     * used to be selected. In that case, use {@link #isItemSelected(Object)}.
     */
    public List<C> getSelectedItems() {
        List<C> selectedItems = new ArrayList<C>();
        for (int i=0; i < getItems().size(); i++) {
            if (isItemSelected(i)) {
                selectedItems.add(getItems().get(i));
            }
        }
        return selectedItems;
    }
    
    /**
     * Returns the index of the first selected item, or
     * {@link #ITEM_INDEX_NONE} if there are no selected items.
     */
    public int getSelectedItemIndex() {
        if (selectedItems.size() > 0) {
            return getItems().indexOf(selectedItems.toArray()[0]);
        }
        return ITEM_INDEX_NONE;
    }
    
    private final List<ItemSelectionListener<T, C>> itemSelectionListeners =
        new ArrayList<ItemSelectionListener<T,C>>();
    
    protected void fireItemsSelected(Set<C> items) {
        ItemSelectionEvent<T, C> e = new ItemSelectionEvent<T, C>(this, items);
        for (int i = itemSelectionListeners.size() - 1; i >= 0; i--) {
            itemSelectionListeners.get(i).itemsSelected(e);
        }
    }

    protected void fireItemsDeselected(Set<C> items) {
        ItemSelectionEvent<T, C> e = new ItemSelectionEvent<T, C>(this, items);
        for (int i = itemSelectionListeners.size() - 1; i >= 0; i--) {
            itemSelectionListeners.get(i).itemsDeselected(e);
        }
    }
    
    public void addItemSelectionListener(ItemSelectionListener<T, C> listener) {
        itemSelectionListeners.add(listener);
    }
    
    public void removeItemSelectionListener(ItemSelectionListener<T, C> listener) {
        itemSelectionListeners.remove(listener);
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
            tp.setInsertionPoint(ITEM_INDEX_NONE);
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
            Point loc = tp.getPlayPen().unzoomPoint(new Point(dtde.getLocation()));
            loc.x -= tp.getX();
            loc.y -= tp.getY();
            int idx = tp.pointToItemIndex(loc);
            tp.setInsertionPoint(idx);
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
                tp.setInsertionPoint(ITEM_INDEX_NONE);
            } else {
                try {
                    DBTree dbtree = pp.getSession().getSourceDatabases();  // XXX: bad
                    int insertionPoint = tp.pointToItemIndex(loc);

                    ArrayList<int[]> paths = (ArrayList<int[]>) t.getTransferData(importFlavor);
                    logger.debug("Importing items from tree: "+paths); //$NON-NLS-1$

                    // put the undo event adapter into a drag and drop state
                    pp.startCompoundEdit("Drag and Drop"); //$NON-NLS-1$

                    ArrayList<SQLObject> droppedItems = new ArrayList<SQLObject>();
                    for (int[] path : paths) {
                        droppedItems.add(DnDTreePathTransferable.getNodeForDnDPath((SQLObject) dbtree.getModel().getRoot(), path));
                    }

                    boolean success = false;
                    
                    //Check to see if the drag and drop will change the current relationship
                    List<SQLRelationship> importedKeys = tp.getModel().getImportedKeys();
                    
                    boolean newColumnsInPk = false;
                    if (insertionPoint == TablePane.COLUMN_INDEX_END_OF_PK) {
                        newColumnsInPk = true;
                    } else if (insertionPoint == TablePane.COLUMN_INDEX_START_OF_NON_PK) {
                        newColumnsInPk = false;
                    } else if (insertionPoint == ITEM_INDEX_TITLE) {
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
                    tp.setInsertionPoint(ITEM_INDEX_NONE);
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
    }
    
    /**
     * Creates a Transferable representation of the currently-selected items.
     * If there is nothing transferable selected, returns null.
     */
    public abstract Transferable createTransferableForSelection();
}
