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
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsages;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;

public class VirtualCubePane extends OLAPPane<VirtualCube, OLAPObject> {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = PlayPenComponent.allowedChildTypes;

    public VirtualCubePane(VirtualCubePane copyMe, PlayPenContentPane parent) {
        super(copyMe, parent);
        updateUI();
    }
    
    @Constructor
    public VirtualCubePane(@ConstructorParameter(propertyName="model") VirtualCube model, 
            @ConstructorParameter(propertyName="parent") PlayPenContentPane parent) {
        super(model.getName(), parent);
        this.model = model;
        if (model.getCubeUsage() == null) {
            model.setCubeUsage(new CubeUsages());
        }
        PaneSection<CubeUsage> cubeSection =
            new OLAPPaneSection<CubeUsage>(CubeUsage.class, model.getCubeUsage().getCubeUsages(), "Cube Usages:") {

            public void addItem(int idx, CubeUsage item) {
                VirtualCubePane.this.model.getCubeUsage().addCubeUsage(idx, item);
            }
        };
        PaneSection<VirtualCubeDimension> dimensionSection =
            new OLAPPaneSection<VirtualCubeDimension>(VirtualCubeDimension.class, model.getDimensions(), "Dimensions:") {

            public void addItem(int idx, VirtualCubeDimension item) {
                VirtualCubePane.this.model.addDimension(idx, item);
            }
        };
        PaneSection<VirtualCubeMeasure> measureSection =
            new OLAPPaneSection<VirtualCubeMeasure>(VirtualCubeMeasure.class, model.getMeasures(), "Measures:") {
            
            public void addItem(int idx, VirtualCubeMeasure item) {
                VirtualCubePane.this.model.addMeasure(idx, item);
            }
        };
        sections.add(cubeSection);
        sections.add(dimensionSection);
        sections.add(measureSection);
        setDashed(true);
        updateUI();
    }
    
    @Override
    @NonBound
    protected List<OLAPObject> getItems() {
        // Return the children and all the cubeUsages.
        List<OLAPObject> items = new ArrayList<OLAPObject>();
        items.addAll(model.getCubeUsage().getCubeUsages());
        items.addAll(model.getChildren(OLAPObject.class));
        return items;
    }

    public void updateUI() {
        ContainerPaneUI ui = (ContainerPaneUI) BasicVirtualCubePaneUI.createUI();
        ui.installUI(this);
        setUI(ui);
    }

    @Override
    public String toString() {
        return "VirtualCubePane: " + model.getName(); //$NON-NLS-1$
    }

    @Override
    public DataEntryPanel createEditDialog(PlayPenCoordinate<VirtualCube, OLAPObject> coord) throws SQLObjectException {
        DataEntryPanel panel;
        if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_TITLE) {
            // TODO add getName() method to DataEntryPanel.
            panel = new VirtualCubeEditPanel(model);
        } else if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
            panel = null;
        } else if (coord.getIndex() > PlayPenCoordinate.ITEM_INDEX_TITLE){
            if (coord.getItem() instanceof CubeUsage) {
                CubeUsage cu = (CubeUsage) coord.getItem();
                Cube c = OLAPUtil.findReferencedCube(model, cu);
                if (c == null) throw new NullPointerException("Couldn't find cube!");
                panel = new CubeEditPanel(c, getPlayPen(), getPlayPen().getSession());
            } else if (coord.getItem() instanceof VirtualCubeDimension) {
                VirtualCubeDimension vcd = (VirtualCubeDimension) coord.getItem();
                Dimension d = OLAPUtil.findReferencedDimension(vcd);
                if (d == null) throw new NullPointerException("Couldn't find dimension!");
                panel = new DimensionEditPanel(d);
            } else if (coord.getItem() instanceof VirtualCubeMeasure) {
                // there's no direct relation between a virtual cube measure and the original measure...
                panel = null;
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
            if (item instanceof CubeUsage ||
                    item instanceof VirtualCubeDimension ||
                    item instanceof VirtualCubeMeasure) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    @Override
    public void pasteData(Transferable t) {
        // TODO Auto-generated method stub
        
    }
}