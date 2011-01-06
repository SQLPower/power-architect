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

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.util.TransactionEvent;

/**
 * Visual representation of a dimension. It keeps its sections in sync with the
 * hierarchies of the Dimension object, and the items in each section are the
 * levels of the corresponding hierarchy.
 */
public class DimensionPane extends OLAPPane<Dimension, OLAPObject> {
    
    private static final Logger logger = Logger.getLogger(DimensionPane.class);
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = PlayPenComponent.allowedChildTypes;

    private class HierarchyWatcher implements SPListener {

        public void childAdded(SPChildEvent e) {
            Hierarchy hierarchy = (Hierarchy) e.getChild();
            sections.add(e.getIndex(), new HierarchySection(hierarchy));
            revalidate();
        }

        public void childRemoved(SPChildEvent e) {
            sections.remove(e.getIndex());
            revalidate();
        }

        public void propertyChanged(PropertyChangeEvent evt) {
            //no-op
        }

        public void transactionEnded(TransactionEvent e) {
            //no-op            
        }

        public void transactionRollback(TransactionEvent e) {
            //no-op            
        }

        public void transactionStarted(TransactionEvent e) {
            //no-op            
        }
        
    }
    
    public class HierarchySection implements PaneSection<Level> {

        private final Hierarchy hierarchy;

        HierarchySection(Hierarchy hierarchy) {
            this.hierarchy = hierarchy;
            
        }
        
        public List<Level> getItems() {
            return hierarchy.getLevels();
        }

        public String getTitle() {
            // a null hierarchy name means it should use the dimension name
            if (hierarchy.getName() != null) {
                return hierarchy.getName();
            } else if (hierarchy.getParent() == null) {
                return "(Unnamed Hierarchy with no parent dimension)";
            } else {
                return hierarchy.getParent().getName();
            }
        }
        
        public Hierarchy getHierarchy() {
            return hierarchy;
        }
        
        public Class<Level> getItemType() {
            return Level.class;
        }

        public void addItem(int idx, Level item) {
            hierarchy.addLevel(idx, item);
        }

        public void addItem(Level item) {
            addItem(getItems().size(), item);
        }
        
    }

    private final HierarchyWatcher hierarchyWatcher = new HierarchyWatcher();
    
    public DimensionPane(DimensionPane copyMe, PlayPenContentPane parent) {
        super(copyMe, parent);
        updateUI();
    }
    
    @Constructor
    public DimensionPane(@ConstructorParameter(propertyName="model") Dimension m, 
            @ConstructorParameter(propertyName="parent") PlayPenContentPane parent) {
        super(m.getName(), parent);
        this.model = m;
        for (Hierarchy h : model.getHierarchies()) {
            sections.add(new HierarchySection(h));
        }
        model.addSPListener(hierarchyWatcher); // FIXME clean up listener reference
        setRounded(true);
        updateUI();
    }
    
    @Override
    protected List<OLAPObject> getItems() {
        List<OLAPObject> oos = new ArrayList<OLAPObject>();
        for (Hierarchy h : model.getHierarchies()) {
            oos.add(h);
            oos.addAll(h.getLevels());
        }
        return oos;
    }

    // ---------------------- PlayPenComponent Overrides ----------------------
    // see also PlayPenComponent

    public void updateUI() {
        ContainerPaneUI ui = (ContainerPaneUI) BasicDimensionPaneUI.createUI(this);
        ui.installUI(this);
        setUI(ui);
    }

    @Override
    public String toString() {
        return "DimensionPane: " + model; //$NON-NLS-1$
    }
    
    @Override
    public DataEntryPanel createEditDialog(PlayPenCoordinate<Dimension, OLAPObject> coord) throws SQLObjectException {
        DataEntryPanel panel;
        // TODO add getName() method to DataEntryPanel.
        if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_TITLE) {
            panel = new DimensionEditPanel(model);
        } else if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
            if (coord.getSection() instanceof HierarchySection) {
                panel = new HierarchyEditPanel(((HierarchySection) coord.getSection()).getHierarchy());
            } else {
                throw new IllegalArgumentException("Edit dialog for type " + coord.getSection().getClass() + " cannot be created!");
            }
        } else if (coord.getIndex() > PlayPenCoordinate.ITEM_INDEX_TITLE){
            if (coord.getItem() instanceof Level) {
                panel = new LevelEditPanel((Level) coord.getItem());
            } else {
                throw new IllegalArgumentException("Edit dialog for type " + coord.getItem().getClass() + " cannot be created!");
            }
        } else {
            panel = null;
        }
        
        return panel;
    }

    @Override
    protected List<OLAPObject> filterDroppableItems(List<OLAPObject> items) {
        List<OLAPObject> filtered = new ArrayList<OLAPObject>();
        for (OLAPObject item : items) {
            if (item instanceof Level) {
                filtered.add((Level) item);
            } else if (item instanceof Hierarchy) {
                filtered.add((Hierarchy) item);
            }
        }
        return filtered;
    }
    
    /**
     * Returns the HierarchySection that contains the given hierarchy.
     * Returns null if it was not found.
     */
    public HierarchySection findSection(Hierarchy hierarchy) {
        for (PaneSection<? extends OLAPObject> hs : sections) {
           if (hs instanceof HierarchySection) {
               if (((HierarchySection) hs).getHierarchy() == hierarchy){
                   return ((HierarchySection) hs);
               }
           }
        }
        return null;
    }
    
    /**
     * Returns a list of levels which are selected.
     */
    @NonBound
    public List<Level> getSelectedLevels() {
        List<Level> selectedItems = new ArrayList<Level>();
        for (int i=0; i < getItems().size(); i++) {
            if (isItemSelected(i) && getItems().get(i) instanceof Level) {
                selectedItems.add((Level) getItems().get(i));
            }
        }
        return selectedItems;
    }
    
    @Override
    @NonBound
    protected List<OLAPObject> getItemsFromCoordinates(
            List<PlayPenCoordinate<? extends OLAPObject, ? extends OLAPObject>> coords) {
        List<OLAPObject> items = new ArrayList<OLAPObject>();
        for (PlayPenCoordinate coord : coords) { // Type params removed to work around javac bug (it broke the nightly build)
            if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
                // Only add sections which are HierarchySections because they are
                // also OLAPObjects.  If it is a Hierarchy, then we do not want to
                // move its levels as well because they come with the Hierarchy
                // anyways.
                if(coord.getSection() instanceof HierarchySection) {
                    items.add(((HierarchySection) coord.getSection()).getHierarchy());
                } else {
                    JOptionPane.showMessageDialog(null,
                            coord.getSection() + " is not a Hierarchy Section.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
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
    
    @Override
    protected void transferInvalidIndexItem(OLAPObject item, PaneSection<OLAPObject> insertSection) {
        try {
            item.getParent().removeChild(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (item instanceof Level) {
            if (insertSection == null || sections.isEmpty()) {
                // If a pane has no sections, then we must add one to put the
                // item in. If it had no sections, must be a DimensionPane and
                // therefore has only HierarchySections.
                Hierarchy hier = new Hierarchy();
                hier.setHasAll(true);
                getModel().addChild(hier);
                insertSection = (PaneSection<OLAPObject>) sections.get(0);
            }
            insertSection.addItem(item);
        } else {
            getModel().addChild(item);
        }
    }

    @Override
    public void pasteData(Transferable t) {
        // TODO Auto-generated method stub
        
    }

}
