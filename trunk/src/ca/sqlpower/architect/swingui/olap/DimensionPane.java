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

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * Visual representation of a dimension. It keeps its sections in sync with the
 * hierarchies of the Dimension object, and the items in each section are the
 * levels of the corresponding hierarchy.
 */
public class DimensionPane extends OLAPPane<Dimension, Level> {
    

    private class HierarchyWatcher implements OLAPChildListener {

        public void olapChildAdded(OLAPChildEvent e) {
            Hierarchy hierarchy = (Hierarchy) e.getChild();
            sections.add(e.getIndex(), new HierarchySection(hierarchy));
            revalidate();
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            sections.remove(e.getIndex());
            revalidate();
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
            return hierarchy.getName();
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
    
    public DimensionPane(Dimension m, PlayPenContentPane parent) {
        super(parent);
        this.model = m;
        for (Hierarchy h : model.getHierarchies()) {
            sections.add(new HierarchySection(h));
        }
        model.addChildListener(hierarchyWatcher); // FIXME clean up listener reference
        setRounded(true);
        updateUI();
    }
    
    @Override
    protected List<Level> getItems() {
        List<Level> levels = new ArrayList<Level>();
        for (Hierarchy h : model.getHierarchies()) {
            levels.addAll(h.getLevels());
        }
        return levels;
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
    public DataEntryPanel createEditDialog(PlayPenCoordinate<Dimension, Level> coord) throws ArchitectException {
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
    protected List<Level> filterDroppableItems(List<OLAPObject> items) {
        List<Level> filtered = new ArrayList<Level>();
        for (OLAPObject item : items) {
            if (item instanceof Level) {
                filtered.add((Level) item);
            }
        }
        return filtered;
    }
    
    /**
     * Returns the HierarchySection that contains the given hierarchy.
     * Returns null if it was not found.
     */
    public HierarchySection findSection(Hierarchy hierarchy) {
        for (PaneSection<? extends Level> hs : sections) {
           if (hs instanceof HierarchySection) {
               if (((HierarchySection) hs).getHierarchy() == hierarchy){
                   return ((HierarchySection) hs);
               }
           }
        }
        return null;
    }
}
