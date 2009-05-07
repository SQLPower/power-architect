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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;

public class CubePane extends OLAPPane<Cube, OLAPObject> {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CubePane.class);
    
    public CubePane(CubePane copyMe, PlayPenContentPane parent) {
        super(copyMe, parent);
        updateUI();
    }
    
    public CubePane(Cube model, PlayPenContentPane parent) {
        super(parent);
        this.model = model;
        
        PaneSection<CubeDimension> dimensionSection =
            new OLAPPaneSection<CubeDimension>(CubeDimension.class, model.getDimensions(), "Dimensions:") {

            public void addItem(int idx, CubeDimension item) {
                CubePane.this.model.addDimension(idx, item);
            }
        };
        
        PaneSection<Measure> measureSection = new OLAPPaneSection<Measure>(Measure.class, model.getMeasures(), "Measures:") {

            public void addItem(int idx, Measure item) {
                CubePane.this.model.addMeasure(idx, item);
            }
        };
        
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
    public DataEntryPanel createEditDialog(PlayPenCoordinate<Cube, OLAPObject> coord) throws SQLObjectException {
        DataEntryPanel panel;
        // TODO add getName() method to DataEntryPanel.
        if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_TITLE) {
            panel = new CubeEditPanel(model, getParent().getOwner().getSession());
        } else if (coord.getIndex() == PlayPenCoordinate.ITEM_INDEX_SECTION_TITLE) {
            panel = null;
        } else if (coord.getIndex() > PlayPenCoordinate.ITEM_INDEX_TITLE){
            if (coord.getItem() instanceof Dimension) {
                panel = new DimensionEditPanel((Dimension) coord.getItem());
            } else if (coord.getItem() instanceof Measure) {
                panel = new MeasureEditPanel((Measure) coord.getItem());
            } else if (coord.getItem() instanceof DimensionUsage) {
                UsageComponent usageComp = (UsageComponent) getPlayPen().findPPComponent(coord.getItem());
                Dimension dimension = (Dimension) usageComp.getPane1().getModel();
                panel = new DimensionEditPanel(dimension);
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
            if (item instanceof Measure || item instanceof Dimension || item instanceof DimensionUsage) {
                filtered.add(item);
            } else {
                logger.debug(" Ignoring dropped item of type " + item.getClass().getName() );
            }
        }
        return filtered;
    }
    
    @Override
    protected int dndRemoveAndAdd(PaneSection<OLAPObject> insertSection, int insertIndex, OLAPObject item) {
        OLAPObject newItem = item;
        if (item instanceof DimensionUsage) {
            newItem = new DimensionUsage((DimensionUsage) item);

            Dimension refDimension = OLAPUtil.findReferencedDimension((DimensionUsage) item);
            PlayPenContentPane contentPane = getParent();
            DimensionPane dimensionPane = null;
            for (int i = 0; i < contentPane.getComponentCount(); i++) {
                if (contentPane.getComponent(i).getModel() == refDimension) {
                    dimensionPane = (DimensionPane) contentPane.getComponent(i);
                    break;
                }
            }
            UsageComponent uc = new UsageComponent(contentPane, newItem, dimensionPane, this);
            contentPane.add(uc, contentPane.getComponentCount());
        }
        logger.debug(" Dragging and dropping item of type " + item.getClass().getName());
        if (insertSection != null && insertIndex >= 0 && insertSection.getItemType().isInstance(item)) {
            item.getParent().removeChild(item);
            insertSection.addItem(insertIndex++, newItem);
        } else {
            item.getParent().removeChild(item);
            getModel().addChild(newItem);
        }

        return insertIndex;
    }
    
    @Override
    public void pasteData(Transferable t) {
        // TODO Auto-generated method stub
        
    }

}
