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

import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class DimensionPane extends ContainerPane<SQLTable, SQLColumn> {
    
    private String dimensionName;
    
    private SQLTable dummyTable;
    
    private List<SQLColumn> columns;

    public DimensionPane(String dimensionName, SQLTable m, PlayPenContentPane parent) {
        super(parent);
        
        this.selectedItems = new HashSet<SQLColumn>();
        
        this.dimensionName = dimensionName;
        dummyTable = m;
        try {
            columns = m.getColumns();
        } catch (ArchitectException e) {
        }
        
        updateUI();
    }
    
    @Override
    protected List<SQLColumn> getItems() {
        return getColumns();
    }


    @Override
    public int pointToItemIndex(Point p) {
        return ((DimensionPaneUI) getUI()).pointToItemIndex(p);
    }
    
    // ---------------------- PlayPenComponent Overrides ----------------------
    // see also PlayPenComponent

    public void updateUI() {
        DimensionPaneUI ui = (DimensionPaneUI) BasicDimensionPaneUI.createUI(this);
        ui.installUI(this);
        setUI(ui);
    }

    public SQLTable getDummyTable() {
        return dummyTable;
    }

    private List<SQLColumn> getColumns() {
        return columns;
    }

    public String getDimensionName() {
        return dimensionName;
    }
    
    /**
     * Sets the new bounds and the new location of the dimensionPane,
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
     * @see #Dimension#setBoundsImpl()
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
    }

    @Override
    public String toString() {
        return "DimensionPane: " + model; //$NON-NLS-1$
    }
}
