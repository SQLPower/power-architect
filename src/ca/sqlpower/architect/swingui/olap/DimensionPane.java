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

import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenContentPane;

public class DimensionPane extends OLAPPane<Dimension, Hierarchy> {
    
    public DimensionPane(Dimension m, PlayPenContentPane parent) {
        super(parent);
        this.model = m;
        sections.add(new PaneSectionImpl<Hierarchy>(model.getHierarchies(), null));
        updateUI();
    }
    
    @Override
    protected List<Hierarchy> getItems() {
        return model.getHierarchies();
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
}
