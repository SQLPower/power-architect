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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianModel;
import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentUI;

public class BasicCubePaneUI extends OLAPPaneUI {

 private static Logger logger = Logger.getLogger(BasicCubePaneUI.class);
    
    private final ModelEventHandler modelEventHandler = new ModelEventHandler();
    
    public static PlayPenComponentUI createUI() {
        return new BasicCubePaneUI();
    }

    public void installUI(PlayPenComponent c) {
        super.installUI((CubePane) c);
        CubePane cube = (CubePane) containerPane; 
        PaneSection dimensionSection = new PaneSectionImpl(cube.getModel().getDimensions(), "Dimensions");
        PaneSection measureSection = new PaneSectionImpl(cube.getModel().getMeasures(), "Measures");
        paneSections.add(dimensionSection);
        paneSections.add(measureSection);
        OLAPUtil.listenToHierarchy(cube.getModel(), modelEventHandler, modelEventHandler);
    }

    public void uninstallUI(PlayPenComponent c) {
        OLAPUtil.unlistenToHierarchy(containerPane.getModel(), modelEventHandler, modelEventHandler);
    }
    
    protected String getOLAPChildObjectName(OLAPObject oo) {
        if (oo instanceof MondrianModel.Dimension) {
            return ((MondrianModel.Dimension) (oo)).getName();
        } else if (oo instanceof MondrianModel.Measure) {
            return ((MondrianModel.Measure) (oo)).getName();
        } else {
            throw new IllegalArgumentException("Given object of type: " + oo.getClass() + " is not a child of Cube: " + containerPane.getName());
        }
    }

    @Override
    public int pointToItemIndex(Point p) {
        CubePane cube = (CubePane) containerPane;
        Font font = cube.getFont();
        FontMetrics metrics = cube.getFontMetrics(font);
        int fontHeight = metrics.getHeight();

        int numDims = cube.getCube().getDimensions().size();
        int firstDimStart = cube.getInsets().top + fontHeight * 2 + GAP + BOX_LINE_THICKNESS * 2 + TABLE_GAP + cube.getMargin().top;
        
        int numMeasures = cube.getCube().getMeasures().size();
        int firstMeasureStart = firstDimStart + numDims * fontHeight + GAP + fontHeight + TABLE_GAP + cube.getMargin().top;

        if (logger.isDebugEnabled()) logger.debug("p.y = "+p.y); //$NON-NLS-1$
        
        int returnVal;
        
        logger.debug("font height: " + fontHeight + ", firstColStart: " + firstDimStart); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (p.y < 0) {
            logger.debug("y<0"); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        } else if (p.y <= fontHeight) {
            logger.debug("y<=fontHeight = "+fontHeight); //$NON-NLS-1$
            returnVal = ContainerPane.ITEM_INDEX_TITLE;
        } else if (p.y > firstDimStart && p.y <= firstDimStart + numDims * fontHeight) {
            returnVal = (p.y - firstDimStart) / fontHeight;
        } else if (p.y > firstMeasureStart && p.y <= firstMeasureStart + numMeasures * fontHeight) {
            returnVal = (p.y - firstMeasureStart) / fontHeight;
        } else {
            returnVal = ContainerPane.ITEM_INDEX_NONE;
        }
        logger.debug("pointToColumnIndex return value is " + returnVal); //$NON-NLS-1$
        return returnVal;
    }
    
    private class ModelEventHandler implements PropertyChangeListener, OLAPChildListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if ("name".equals(evt.getPropertyName())) {
                // note this could be the name of the cube or any of its child objects,
                // since we have property change listeners on every object in the subtree under cube
                containerPane.revalidate();
            }
        }

        public void olapChildAdded(OLAPChildEvent e) {
            OLAPUtil.listenToHierarchy(e.getChild(), this, this);
            containerPane.revalidate();
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            OLAPUtil.unlistenToHierarchy(e.getChild(), this, this);
            containerPane.revalidate();
        }
        
    }
}
