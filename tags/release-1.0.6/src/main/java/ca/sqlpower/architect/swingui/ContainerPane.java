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
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.LayoutNode;
import ca.sqlpower.architect.swingui.PlayPen.MouseModeType;
import ca.sqlpower.architect.swingui.event.ItemSelectionEvent;
import ca.sqlpower.architect.swingui.event.ItemSelectionListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.swingui.SPSUtils;

/**
 * A playpen component that represents a model with a list of individually selectable items. 
 *
 * @param <T> Class of the model
 * @param <C> Class of the an item.
 */
public abstract class ContainerPane<T, C>
extends DraggablePlayPenComponent
implements DragSourceListener, LayoutNode {
    
    /**
     * A special item index that represents the titlebar area.
     */
    public static final int ITEM_INDEX_TITLE = -1;

    /**
     * A special item index that means "no location."
     */
    public static final int ITEM_INDEX_NONE = -2;
    
    private static final Logger logger = Logger.getLogger(ContainerPane.class);
    
    /**
     * Contains the last selected Item
     */
    private C previousSelectedItem;
    
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

    /**
     * Creates a copy of this container pane suitable for use with printing or
     * PDF generation. The new copy may not have all listeners set up properly
     * for interactive use.
     * 
     * @param copyMe
     *            the container pane to copy.
     */
    protected ContainerPane(ContainerPane<T, C> copyMe, PlayPenContentPane parent) {
        super(copyMe, parent);
        dashed = copyMe.dashed;
        // itemSelectionListeners should not be copied
        if (copyMe.margin != null) {
            margin = new Insets(
                    copyMe.margin.top, copyMe.margin.left,
                    copyMe.margin.bottom, copyMe.margin.right);
        }
        model = copyMe.model;
        rounded = copyMe.rounded;
    }
    
    protected ContainerPane(String name) {
        super(name);
        this.backgroundColor = new Color(240, 240, 240);
        this.foregroundColor = Color.BLACK;
        setOpaque(true);
    }
    
    protected ContainerPane(String name, PlayPenContentPane parent) {
        this(name);
        setParent(parent);
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
                if(selectedItemIndex >= 0 && selectedItemIndex < getItems().size()) {
                    previousSelectedItem = getItems().get(selectedItemIndex);
                }
                if (selectedItemIndex > ITEM_INDEX_TITLE && componentPreviouslySelected){
                    if(evt.isControlDown()) {
                        deselectItem(selectedItemIndex);
                        fireSelectionEvent(new SelectionEvent(this, SelectionEvent.DESELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
                    } else {
                        selectNone();
                        selectItem(selectedItemIndex);
                        fireSelectionEvent(new SelectionEvent(this, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
                    }
                } else if (isSelected() && componentPreviouslySelected) {
                    setSelected(false, SelectionEvent.SINGLE_SELECT);
                }
            }
        } else if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            componentPreviouslySelected = false;
            int clickItem = pointToItemIndex(p);

            if (pp.getMouseMode() != MouseModeType.CREATING_TABLE) {
                if ((evt.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | SPSUtils.MULTISELECT_MASK)) == 0) {
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
                    int previousSelectedIndex;
                    //This is since dragging columns within the same table does
                    // not update the previouslySelectedItem
                    if (isItemSelected(previousSelectedItem)) {    
                        previousSelectedIndex = getItems().indexOf(previousSelectedItem);
                    } else {
                        previousSelectedIndex = getSelectedItemIndex();
                    }

                    if ((evt.getModifiersEx() &
                            (InputEvent.SHIFT_DOWN_MASK | SPSUtils.MULTISELECT_MASK)) == 0) {
                        if (!isItemSelected(clickItem)){
                            selectNone();
                        }
                        pp.setMouseMode(MouseModeType.SELECT_ITEM);
                    } else if (((evt.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) && getSelectedItems().size() > 0 &&
                            previousSelectedIndex > ITEM_INDEX_TITLE) {
                        int start = Math.min(previousSelectedIndex, clickItem);
                        int end = Math.max(previousSelectedIndex, clickItem);
                        logger.debug("Start: " +start+ " , End: " +end+ " , Total size: " +getItems().size());
                        for (int i = 0; i < getItems().size(); i++) {
                            if (i > start && i < end ) {
                                selectItem(i);
                                fireSelectionEvent(new SelectionEvent(this, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
                            }
                        }
                    }

                    if (isItemSelected(clickItem)) {
                        componentPreviouslySelected = true;
                    } else {
                        selectItem(clickItem);
                        fireSelectionEvent(new SelectionEvent(this, SelectionEvent.SELECTION_EVENT,SelectionEvent.SINGLE_SELECT));
                        repaint();
                        previousSelectedItem = getItems().get(clickItem);
                    }
                }
                if (isSelected()&& clickItem == ITEM_INDEX_TITLE){
                    componentPreviouslySelected = true;
                } else {
                    setSelected(true,SelectionEvent.SINGLE_SELECT);
                }
            }

            if (clickItem == ITEM_INDEX_TITLE && 
                    !pp.getSession().getArchitectFrame().createRelationshipIsActive() &&
                    evt.getButton() == MouseEvent.BUTTON1) {
                setupDrag(p);
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

    /**
     * Sets the value of margin
     *
     * @param argMargin Value to assign to this.margin
     */
    @Transient @Mutator
    public void setMargin(Insets argMargin) {
        Insets old = margin;
        this.margin = (Insets) argMargin.clone();
        firePropertyChange("margin", old, margin); //$NON-NLS-1$
    }
    

    /**
     * Gets the value of margin
     *
     * @return the value of margin
     */
    @Transient @Accessor
    public Insets getMargin()  {
        return this.margin;
    }
    
    /**
     * Returns a list of the items to be displayed with the model.  
     */
    @Transient @Accessor
    protected abstract List<C> getItems();
    
    /**
     * Indicates whether the corners are rounded. 
     */
    @Accessor
    public boolean isRounded() {
        return rounded;
    }

    /**
     * Sets whether the corners are rounded. 
     */
    @Mutator
    public void setRounded(boolean isRounded) {
        boolean oldValue = rounded;
        rounded = isRounded;
        firePropertyChange("rounded", oldValue, isRounded); //$NON-NLS-1$
    }

    /**
     * Indicates whether the lines are dashed/normal. 
     */
    @Accessor
    public boolean isDashed() {
        return dashed;
    }

    /**
     * Sets whether the lines are dashed. 
     */
    @Mutator
    public void setDashed(boolean isDashed) {
        boolean oldValue = dashed;
        dashed = isDashed;
        firePropertyChange("dashed", oldValue, isDashed); //$NON-NLS-1$
    }

    /**
     * Overridden so that the items get deselected when the whole container
     * is deselected.
     */
    @Override @Transient @Mutator
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
    @NonBound
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
    @NonBound
    public boolean isItemSelected(C item) {
        return selectedItems.contains(item);
    }
    
    /**
     * Returns a list of the items that are currently in the selection that
     * also currently exist in the model. Sometimes, especially when handling
     * remove events, you will want to know if the item that was just removed
     * used to be selected. In that case, use {@link #isItemSelected(Object)}.
     */
    @Transient @Accessor
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
    @Transient @Accessor
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
        logger.debug("Drop target drop event on "+getName()+": "+dtde); //$NON-NLS-1$ //$NON-NLS-2$
        dtde.rejectDrop();
    }

    /**
     * Called if the user has modified the current drop gesture.
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // we don't care
    }

    /**
     * Chooses the best import flavour from the flavors array for
     * importing into c.
     *
     * @return The first acceptable DataFlavor in the flavors
     * list, or null if no acceptable flavours are present.
     */
    public DataFlavor bestImportFlavor(JComponent c, DataFlavor[] flavors) {
        return null;
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
    
    /**
     * This adds the data in a transferable to the container pane if the transferable
     * contains a valid flavour.
     */
    public abstract void pasteData(Transferable t);
    
    /**
     * Simple implementation for LayoutNode interface. Simply calls getName().
     */
    @Transient @Accessor
    public String getNodeName() {
        return getName();
    }
    
}
