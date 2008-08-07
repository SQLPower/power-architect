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
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.List;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenContentPane;

public class CubePane extends ContainerPane<Cube, OLAPObject> {

    public CubePane(Cube model, PlayPenContentPane parent) {
        super(parent);
        
        this.selectedItems = new HashSet<OLAPObject>();
        this.model = model;
        updateUI();
    }
    
    @Override
    protected List<OLAPObject> getItems() {
        return model.getChildren();
    }


    @Override
    public int pointToItemIndex(Point p) {
        return ((ContainerPaneUI) getUI()).pointToItemIndex(p);
    }
    
    // ---------------------- PlayPenComponent Overrides ----------------------
    // see also PlayPenComponent

    public void updateUI() {
        ContainerPaneUI ui = (ContainerPaneUI) BasicCubePaneUI.createUI(this);
        ui.installUI(this);
        setUI(ui);
    }

    public Cube getCube() {
        return model;
    }

    public String getCubeName() {
        return model.getName();
    }
    
    /**
     * Sets the new bounds and the new location of the CubePane,
     * but also fires property change event associated with location change.
     */
    @Override
    protected void setBoundsImpl(int x, int y, int width, int height) { 
        Rectangle oldBounds = getBounds();
        super.setBoundsImpl(x, y, width, height);
        if (oldBounds.x != x || oldBounds.y != y) {
            firePropertyChange(new PropertyChangeEvent(this, "location", oldBounds.getLocation(), new Point(x,y)));
        }
    }

    /**
     * @see #CubePane#setBoundsImpl()
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
    }

    @Override
    public String toString() {
        return "CubePane: " + model.getName(); //$NON-NLS-1$
    }

}
