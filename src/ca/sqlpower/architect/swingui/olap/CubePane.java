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
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.swingui.DataEntryPanel;

public class CubePane extends OLAPPane<Cube, OLAPObject> {

    public CubePane(Cube model, PlayPenContentPane parent) {
        super(parent);
        this.model = model;
        PaneSection<OLAPObject> dimensionSection = new PaneSectionImpl(model.getDimensions(), "Dimensions:");
        PaneSection<OLAPObject> measureSection = new PaneSectionImpl(model.getMeasures(), "Measures:");
        sections.add(dimensionSection);
        sections.add(measureSection);
        updateUI();
    }
    
    @Override
    protected List<OLAPObject> getItems() {
        return model.getChildren();
    }

    public void updateUI() {
        ContainerPaneUI ui = (ContainerPaneUI) BasicCubePaneUI.createUI();
        ui.installUI(this);
        setUI(ui);
    }

    @Override
    public String toString() {
        return "CubePane: " + model.getName(); //$NON-NLS-1$
    }

    @Override
    public DataEntryPanel createEditDialog(PlayPenCoordinate<Cube, OLAPObject> coord) throws ArchitectException {
        DataEntryPanel panel;
        // TODO add getName() method to DataEntryPanel.
        if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_TITLE) {
            panel = new CubeEditPanel(model);
        } else if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
            panel = null;
        } else if (coord.getIndex() > PlayPenCoordinate.ITEM_INDEX_TITLE){
            if (coord.getItem() instanceof Dimension) {
                panel = new DimensionEditPanel((Dimension) coord.getItem());
            } else if (coord.getItem() instanceof Measure) {
                panel = new MeasureEditPanel((Measure) coord.getItem());
            } else if (coord.getItem() instanceof DimensionUsage) {
                UsageComponent usageComp = (UsageComponent) getPlayPen().findPPComponent(coord.getItem());
                panel = new DimensionEditPanel((Dimension) usageComp.getPane1().getModel());
            } else {
                throw new IllegalArgumentException("Edit dialog for type " + coord.getItem().getClass() + " cannot be created!");
            }
        } else {
            panel = null;
        }
        
        return panel;
    }
    

}
