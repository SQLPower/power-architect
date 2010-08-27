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

package ca.sqlpower.architect.swingui.olap;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.DraggablePlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPen.FloatingContainerPaneListener;
import ca.sqlpower.architect.swingui.PlayPen.MouseModeType;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.DimensionPane.HierarchySection;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * A class that provides all the generic behaviour applicable to OLAP
 * playpen components that have titles and sections of selectable items.
 *
 * @param <T> The model's type 
 * @param <C> The item type. If there are mixed item types, this will be OLAPObject.
 */
public abstract class OLAPPane<T extends OLAPObject, C extends OLAPObject> extends ContainerPane<T, C> {

    private static final Logger logger = Logger.getLogger(OLAPPane.class);
    
    /**
     * The sections of this OLAP Pane. There must always be at least one section.
     * The set of sections is allowed to change at any time, but an appropriate
     * event will be fired when it does change.
     */
    protected final List<PaneSection<? extends C>> sections = new ArrayList<PaneSection<? extends C>>();
    
    /**
     * Tracks which scetions in this container are currently selected.
     */
    protected final Set<PaneSection<? extends C>> selectedSections = new HashSet<PaneSection<? extends C>>();

    /**
     * The point where a dropped item would be inserted if the drop were
     * to happen now. This is mostly important as a visual clue for the
     * user while dragging over this pane.
     */
    private PlayPenCoordinate<T, C> insertionPoint;

    /**
     * Creates a copy of this OLAP pane suitable for use with printing or
     * PDF generation. The new copy may not have all listeners set up properly
     * for interactive use.
     * 
     * @param copyMe
     *            the OLAP pane to copy.
     */
    protected OLAPPane(OLAPPane<T, C> copyMe, PlayPenContentPane parent) {
        super(copyMe, parent);
        sections.addAll(copyMe.sections); // XXX might need deep copy (could be tricky)
        // don't worry about preserving selections
    }
    
    protected OLAPPane(String name) {
        super(name);
    }
    
    protected OLAPPane(String name, PlayPenContentPane parent) {
        super(name, parent);
    }
    
    /**
     * Returns this pane's list of sections.
     */   
    @Transient @Accessor
    public List<PaneSection<? extends C>> getSections() {
        return sections;
    }

    @Override
    public String getModelName() {
        return OLAPUtil.nameFor(model);
    }

    @Override
    @Deprecated
    public int pointToItemIndex(Point p) {
        return getUI().pointToItemIndex(p);
    }
    
    /**
     * Translates the given point into a {@link PlayPenCoordinate}.
     * 
     * @param p The point to be translated.
     * 
     * @return The PlayPenCoordinate that represents the point.
     */
    public PlayPenCoordinate<T, C> pointToPPCoordinate(Point p) {
        return getUI().pointToPPCoordinate(p);
    }
    
    @Override
    @Transient @Accessor
    public OLAPPaneUI<T, C> getUI() {
        return (OLAPPaneUI<T, C>) super.getUI();
    }
    
    /**
     * Creates a edit dialog for the OLAPObject that is at the location
     * represented by the given coordinate.
     * 
     * @param coord
     *            Containas information about the OLAPObject that the edit
     *            dialog should be created for.
     * 
     * @return A DataEntryPanel for editting the OLAPObject, null if location is
     *         invalid.
     * @throws SQLObjectException
     *             If creating an edit dialog failed.
     */
    public abstract DataEntryPanel createEditDialog(PlayPenCoordinate<T, C> coord) throws SQLObjectException;
    
    @Override
    public void handleMouseEvent(MouseEvent evt) {
        PlayPen pp = getPlayPen();
        
        Point p = evt.getPoint();
        pp.unzoomPoint(p);
        p.translate(-getX(), -getY());

        // Type params removed to work around javac bug (it broke the nightly build)
        // was: PlayPenCoordinate<T, C> clickedCoor = pointToPPCoordinate(p);
        PlayPenCoordinate clickedCoor = pointToPPCoordinate(p);
        
        int clickedIndex = clickedCoor.getIndex();
        if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
            if (evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1) {
                try {
                    DataEntryPanel panel = createEditDialog(clickedCoor);
                    if (panel != null) {
                        OLAPObject editObject;
                        if (clickedCoor.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
                            // hierarchies are the only sections that we have edit dialogs for right now.
                            if (clickedCoor.getSection() instanceof HierarchySection) {
                                HierarchySection section = (HierarchySection) clickedCoor.getSection();
                                editObject = section.getHierarchy();
                            } else {
                                throw new IllegalStateException("Unhandled section type: " + clickedCoor.getSection());
                            }
                        } else if (clickedCoor.getIndex() == PlayPenCoordinate.ITEM_INDEX_TITLE) {
                            editObject = model;
                        } else {
                            // panel should've been null if the coordinate was invalid.
                            editObject = clickedCoor.getItem();
                        }
                        Window owner = SwingUtilities.getWindowAncestor(getPlayPen());
                        JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(panel, owner,
                                 editObject.getClass().getSimpleName() + " Properties", "OK");
                        dialog.setLocationRelativeTo(owner);
                        dialog.setVisible(true);
                    }
                } catch (Exception e) {
                    logger.debug("Error from creating edit dialog at coordinate: " + clickedCoor, e);
                    ASUtils.showExceptionDialogNoReport(SwingUtilities.getWindowAncestor(getPlayPen()),
                            "Failed to create edit dialog!", e);
                }
            }
        } else if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            componentPreviouslySelected = false;

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
                    clickedIndex = PlayPenCoordinate.ITEM_INDEX_TITLE;
                }

                if (clickedIndex == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
                    if ((evt.getModifiersEx() &
                            (InputEvent.SHIFT_DOWN_MASK |
                                    InputEvent.CTRL_DOWN_MASK)) == 0) {

                        if (!isSectionSelected(clickedCoor.getSection()) ){
                            deSelectEverythingElse(evt);
                            selectNone();
                        }
                        pp.setMouseMode(MouseModeType.SELECT_ITEM);
                    }
                    if (isSectionSelected(clickedCoor.getSection())) {
                        componentPreviouslySelected = true;
                    } else {
                        selectSection(clickedCoor.getSection());
                    }

                    fireSelectionEvent(new SelectionEvent(this, SelectionEvent.SELECTION_EVENT,SelectionEvent.SINGLE_SELECT));
                    repaint();
                } else if (clickedIndex > PlayPenCoordinate.ITEM_INDEX_TITLE) {

                    if ((evt.getModifiersEx() &
                            (InputEvent.SHIFT_DOWN_MASK |
                                    InputEvent.CTRL_DOWN_MASK)) == 0) {

                        if (!isItemSelected((C) clickedCoor.getItem()) ){
                            deSelectEverythingElse(evt);
                            selectNone();
                        }
                        pp.setMouseMode(MouseModeType.SELECT_ITEM);
                    }
                    if (isItemSelected((C) clickedCoor.getItem())) {
                        componentPreviouslySelected = true;
                    } else {
                        selectItem((C) clickedCoor.getItem());
                    }

                    fireSelectionEvent(new SelectionEvent(this, SelectionEvent.SELECTION_EVENT,SelectionEvent.SINGLE_SELECT));
                    repaint();
                }
                
                if (isSelected() && clickedIndex == PlayPenCoordinate.ITEM_INDEX_TITLE){
                    componentPreviouslySelected = true;
                } else {
                    setSelected(true,SelectionEvent.SINGLE_SELECT);
                }
            }

            if (clickedIndex == PlayPenCoordinate.ITEM_INDEX_TITLE && !pp.getSession().getArchitectFrame().createRelationshipIsActive()) {
                Iterator<ContainerPane<?, ?> > it = pp.getSelectedContainers().iterator();
                logger.debug("event point: " + p); //$NON-NLS-1$
                logger.debug("zoomed event point: " + pp.zoomPoint(new Point(p))); //$NON-NLS-1$
                pp.setDraggingContainerPanes(true);
                startedDragging();
                Map<DraggablePlayPenComponent, Point> ppcToHandleMap = new HashMap<DraggablePlayPenComponent, Point>();

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
                    
                    ppcToHandleMap.put(cp, handle);
                }
                new FloatingContainerPaneListener(pp, ppcToHandleMap);
            }
        } else if (evt.getID() == MouseEvent.MOUSE_MOVED || evt.getID() == MouseEvent.MOUSE_DRAGGED) {
            logger.debug("Mouse moved/dragged to " + evt.getPoint());
            setSelected(pp.getRubberBand().intersects(getBounds(new Rectangle())),SelectionEvent.SINGLE_SELECT);
        }
    }
    
    // -- Section selection methods -- //

    /**
     * Deselects the given section.
     * 
     * @param sect the section to deselect.
     */
    public void deselectSection(PaneSection<? extends C> sect) {
        selectedSections.remove(sect);
        // TODO make a firePlayPenCoordinateEvent and change this event to that
        fireItemsDeselected(Collections.singleton((C) null));
        repaint();
    }

    /**
     * Selects the section.
     * 
     */
    public void selectSection(PaneSection<? extends C> sect) {
        selectedSections.add(sect);
        // TODO make a firePlayPenCoordinateEvent and change this event to that
        fireItemsSelected(Collections.singleton((C) null));
        repaint();
    }

    /**
     * Returns true if the given section was selected in this olap pane.
     * 
     * @param sect
     *            The section to check
     * @return true if section is currently selected.
     */
    @NonBound
    public boolean isSectionSelected(PaneSection<? extends C> sect) {
        return selectedSections.contains(sect);
    }
    
    /**
     * Returns a list of the sections that are currently in the selection that
     * also currently exist in the model.
     */
    @NonBound
    public List<PaneSection<? extends C>> getSelectedSections() {
        List<PaneSection<? extends C>> selectedSects = new ArrayList<PaneSection<? extends C>>();
        for (PaneSection<? extends C> sect : getSections()) {
            if (isSectionSelected(sect)) {
                selectedSects.add(sect);
            }
        }
        return selectedSects;
    }
    
    
    /**
     * Returns a list of the sections that are currently in the selection that
     * also currently exist in the model. If a section title is selected,
     * the selection state of its items is not considered; the section itself
     * will be in the returned list and none of its items will. This is consistent
     * with other multi-select GUIs we looked at.
     */
    @NonBound
    public List<PlayPenCoordinate<T, C>> getSelectedCoordinates() {
        List<PlayPenCoordinate<T, C>> selection = new ArrayList<PlayPenCoordinate<T,C>>();
        for (PaneSection<? extends C> sect : getSections()) {
            if (isSectionSelected(sect)) {
                selection.add(new PlayPenCoordinate<T, C>(
                        this, sect, PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE, null));
            } else {
                int i = 0;
                for (C item : sect.getItems()) {
                    if (isItemSelected(item)) {
                        selection.add(new PlayPenCoordinate<T, C>(
                                this, sect, i, item));
                    }
                    i++;
                }
            }
        }
        return selection;
    }

   
    @Override
    public void selectNone() {
        Set<C> previouslySelectedItems = new HashSet<C>(selectedItems);
        selectedItems.clear();
        fireItemsDeselected(previouslySelectedItems);
        
        selectedSections.clear();
        repaint();
    }
    
    @Override
    @NonBound
    public JPopupMenu getPopup(Point p) {
        PlayPenCoordinate<T, C> pointToPPCoordinate = getUI().pointToPPCoordinate(p);
        List<OLAPObject> itemsFromPoint = getItemsFromCoordinates((List) Collections.singletonList(pointToPPCoordinate));
        if (!itemsFromPoint.isEmpty()) {
            return getPlayPen().getPopupFactory().createPopupMenu(itemsFromPoint.get(0));
        }
        return getPlayPen().getPopupFactory().createPopupMenu(this.getModel());
    }
    
    @Override
    public Transferable createTransferableForSelection() {
        
        // eclipse wouldn't let us do the obvious thing, so we're actually going to
        // make a copy of the list!
        List<PlayPenCoordinate<?, ?>> annoying = new ArrayList<PlayPenCoordinate<?, ?>>();
        for (PlayPenCoordinate<T, C> ridiculous : getSelectedCoordinates()) {
            annoying.add(ridiculous);
        }
        
        return new DnDOLAPTransferable(getPlayPen(), annoying);
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
        setInsertionPoint(null);
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
        setInsertionPoint(pointToPPCoordinate(loc));
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
        Schema schema = OLAPUtil.getSession(getModel()).getSchema();
        try {
            schema.begin("Drag and Drop");
            Transferable t = dtde.getTransferable();
            DataFlavor importFlavor = bestImportFlavor(null, t.getTransferDataFlavors());
            logger.debug("Import flavor: " + importFlavor);
            if (importFlavor == null) {
                dtde.rejectDrop();
                return;
            }
            List<List<Integer>> paths = (List<List<Integer>>) t.getTransferData(importFlavor);
            logger.debug("Paths = " + paths);
            List<PlayPenCoordinate<? extends OLAPObject, ? extends OLAPObject>> coords =
                DnDOLAPTransferable.resolve(getPlayPen(), paths);
            logger.debug("Resolved Paths = " + coords);
            List<OLAPObject> items = getItemsFromCoordinates(coords);
            List<C> acceptedItems = filterDroppableItems(items);
            logger.debug("Accepted Items = " + acceptedItems);
            
            // XXX we don't want to weaken the type here (PaneSection<C> would be better)
            // but without this, the whole thing collapses
            PaneSection<OLAPObject> insertSection = (PaneSection<OLAPObject>) getInsertionPoint().getSection();
            int insertIndex = getInsertionPoint().getIndex();

            if (insertSection == null && !sections.isEmpty()) {
                insertSection = (PaneSection<OLAPObject>) sections.get(0);
                insertIndex = insertSection.getItems().size();
            }

            for (C item : acceptedItems) {
                logger.debug("Trying to add " + item);
                if (item.getParent() != null) {

                    /*
                     * this is the index of the item we want to move, in the
                     * section we plan to move it _to_. This is only relevant
                     * when moving an item to a new place it the same section.
                     * If this DnD operation is from one section to a different
                     * section, removedItemIndex will be -1, which is expected
                     * and handled properly below.
                     * 
                     * Why all the fuss? If you are moving an item down within
                     * the section it came from, the insertion point's index
                     * has to be adjusted to account for the subsequent items
                     * shifting up to take the place of the removed item.
                     */
                    int removedItemIndex = 0;
                    if (insertSection != null) {
                       removedItemIndex = insertSection.getItems().indexOf(item);
                    }
                    logger.debug("Removed item index in target section: " + removedItemIndex);

                    if (insertSection != null && removedItemIndex >= 0 &&
                            insertSection.getItemType().isInstance(item) &&
                            insertIndex > removedItemIndex) {
                        insertIndex--;
                    }
                }

                insertIndex = dndRemoveAndAdd(insertSection, insertIndex, item);
                logger.debug("Moved object: " + item);
            }

            if (!acceptedItems.isEmpty()) {
                getPlayPen().selectNone();
                setSelected(true, SelectionEvent.SINGLE_SELECT);
                for (C item : acceptedItems) {
                    selectItem(item);
                }
            }
            
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            dtde.dropComplete(!acceptedItems.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            setInsertionPoint(null);
            schema.commit();
        }
    }

    /**
     * This will remove the item being dragged and dropped from its parent before
     * adding it to the new insertSection. Some panes need to do special actions
     * before or during the remove and add.
     */
    protected int dndRemoveAndAdd(PaneSection<OLAPObject> insertSection, int insertIndex, C item) {
        if (insertSection != null && insertIndex >= 0 && insertSection.getItemType().isInstance(item)) {
            try {
                item.getParent().removeChild(item);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            insertSection.addItem(insertIndex++, item);
        } else {
            transferInvalidIndexItem(item, insertSection);
        }
        return insertIndex;
    }
    
    /**
     * Returns a list of OLAPObjects that correspond to the given
     * PlayPenCoordinates.
     * 
     * @param coords
     *            The list of PlayPenCoordinates to convert.
     * @return A list of OLAPObjects that correspond to the given
     *         PlayPenCoordinates.
     */
    @NonBound
    protected List<OLAPObject> getItemsFromCoordinates(List<PlayPenCoordinate<? extends OLAPObject, ? extends OLAPObject>> coords) {
        List<OLAPObject> items = new ArrayList<OLAPObject>();
        for (PlayPenCoordinate<? extends OLAPObject, ? extends OLAPObject> coord : coords) {
            if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
                for (OLAPObject item : coord.getSection().getItems()) {
                    items.add(item);
                }
            } else if (coord.getIndex() >= 0) {
                if (coord.getItem() == null) {
                    throw new NullPointerException(
                            "Found a coordinate with nonnegative " +
                            "item index but null item: " + coord);
                }
                items.add(coord.getItem());
            }
        }
        return items;
    }
    
    /**
     * Handle Drag and Drop transfer for items with invalid index.
     * 
     * @param item The item to be transferred.
     * @param insertSection The section to be inserted into.
     */
    protected void transferInvalidIndexItem(OLAPObject item, PaneSection<OLAPObject> insertSection) {
        try {
            item.getParent().removeChild(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getModel().addChild(item, getModel().getChildren().size());
    }

    /**
     * Accepts a clump of items that have been dragged from elsewhere (normally
     * the tree or another pane). Implementations of this method are free to
     * pick and choose which items to import.
     * <p>
     * This method will always be called by the superclass in the context of a
     * compound edit on the schema this pane's model belongs to.
     * 
     * @param items
     *            The items that were dropped.
     * @return The list of items that can be dropped on this pane. It will be
     *         some subset of the given list of items.
     */
    protected abstract List<C> filterDroppableItems(List<OLAPObject> items);

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
        for (DataFlavor f : flavors) {
            if (f == DnDOLAPTransferable.PP_COORDINATE_FLAVOR) {
                return f;
            }
        }
        return null;
    }

    /**
     * Changes the insertion point and fires a property change to
     * that effect.
     * 
     * @param newIP The new insertion point. Null is allowed, and means
     * there shouldn't be a visible insertion point.
     */
    @Transient @Mutator
    public void setInsertionPoint(PlayPenCoordinate<T, C> newIP) {
        PlayPenCoordinate<T, C> oldIP = insertionPoint;
        insertionPoint = newIP;
        firePropertyChange("insertionPoint", oldIP, newIP);
    }
    
    /**
     * Returns the current insertion point. The insertion point is the
     * point directly above the item at the returned coordinate. Therefore,
     * the coordinate might be one item index beyond the last item in
     * the section.
     */
    @Transient @Accessor
    public PlayPenCoordinate<T, C> getInsertionPoint() {
        return insertionPoint;
    }
    
    /**
     * Returns all the UsageComponents in the play pen that this pane
     * is the "pane2" for. This is part of the LayoutNode interface.
     */
    @NonBound
    public final List<LayoutEdge> getInboundEdges() {
        List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
        
        for (PlayPenComponent ppc : getPlayPen().getContentPane().getChildren()) {
            if (ppc instanceof UsageComponent) {
                UsageComponent uc = (UsageComponent) ppc;
                if (uc.getPane2() == this) {
                    edges.add(uc);
                }
            }
        }
        
        return edges;
    }

    /**
     * Returns all the UsageComponents in the play pen that this pane
     * is the "pane1" for. This is part of the LayoutNode interface.
     */
    @NonBound
    public final List<LayoutEdge> getOutboundEdges() {
        List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
        
        for (PlayPenComponent ppc : getPlayPen().getContentPane().getChildren()) {
            if (ppc instanceof UsageComponent) {
                UsageComponent uc = (UsageComponent) ppc;
                if (uc.getPane1() == this) {
                    edges.add(uc);
                }
            }
        }
        
        return edges;
    }

}
