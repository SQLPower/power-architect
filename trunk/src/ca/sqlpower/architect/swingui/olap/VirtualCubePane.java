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

import java.util.List;

import ca.sqlpower.architect.ArchitectException;
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
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.swingui.DataEntryPanel;

public class VirtualCubePane extends OLAPPane<VirtualCube, OLAPObject> {

    public VirtualCubePane(VirtualCube model, PlayPenContentPane parent) {
        super(parent);
        this.model = model;
        if (model.getCubeUsage() == null) {
            model.setCubeUsage(new CubeUsages());
        }
        PaneSection<OLAPObject> cubeSection = new PaneSectionImpl(model.getCubeUsage().getCubeUsages(), "Cube Usages:");
        PaneSection<OLAPObject> dimensionSection = new PaneSectionImpl(model.getDimensions(), "Dimensions:");
        PaneSection<OLAPObject> measureSection = new PaneSectionImpl(model.getMeasures(), "Measures:");
        sections.add(cubeSection);
        sections.add(dimensionSection);
        sections.add(measureSection);
        setDashed(true);
        updateUI();
    }
    
    @Override
    protected List<OLAPObject> getItems() {
        return model.getChildren();
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
    public DataEntryPanel createEditDialog(PlayPenCoordinate<VirtualCube, OLAPObject> coord) throws ArchitectException {
        DataEntryPanel panel;
        if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_TITLE) {
            // TODO add getName() method to DataEntryPanel.
            panel = new VirtualCubeEditPanel(model);
        } else if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
            panel = null;
        } else if (coord.getIndex() > PlayPenCoordinate.ITEM_INDEX_TITLE){
            if (coord.getItem() instanceof CubeUsage) {
                CubeUsage cu = (CubeUsage) coord.getItem();
                Cube c = OLAPUtil.findReferencedCube(cu);
                if (c == null) throw new NullPointerException("Couldn't find cube!");
                panel = new CubeEditPanel(c);
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
}
