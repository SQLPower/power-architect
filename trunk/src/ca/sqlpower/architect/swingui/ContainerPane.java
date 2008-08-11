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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

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
public abstract class ContainerPane<T extends Object, C extends Object> extends PlayPenComponent {
    
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
     * A selected ContainerPane is one that the user has clicked on.  It
     * will appear more prominently than non-selected ContainerPane.
     */
    protected boolean selected;
    
    protected T model;
    
    /**
     * How many pixels should be left between the surrounding box and the item name labels.
     */
    protected Insets margin = new Insets(1,1,1,1);

    /**
     * Tracks which items in this container are currently selected.
     */
    private final Set<C> selectedItems = new HashSet<C>();

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
                        pp.setMouseMode(MouseModeType.SELECT_COLUMN);
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
    
    // --------------------- Selectable Methods ---------------------

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
    
    // --------------------- item selection support --------------------
    
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
     * @param i
     *            index to {@link #getItems()}
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
     * @param i index to {@link #getItems()}
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
}
