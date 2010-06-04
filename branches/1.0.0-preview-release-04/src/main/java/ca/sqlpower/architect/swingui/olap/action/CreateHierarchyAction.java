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

package ca.sqlpower.architect.swingui.olap.action;

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.HierarchyEditPanel;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * Action for adding a hierarchy to the selected dimension.
 */
public class CreateHierarchyAction extends CreateOLAPChildAction<DimensionPane, Hierarchy> {

    public CreateHierarchyAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "Hierarchy", DimensionPane.class, "Dimension", 'h', OSUtils.HIERARCHY_ADD_ICON);
    }

    @Override
    protected Hierarchy addNewChild(DimensionPane pane) {
        Hierarchy h = new Hierarchy();
        
        int count = 1;
        while (!OLAPUtil.isNameUnique(pane.getModel(), Hierarchy.class, "New Hierarchy " + count)) {
            count++;
        }
        h.setName("New Hierarchy " + count);
        
        pane.getModel().addHierarchy(h);
        
        return h;
    }

    @Override
    protected DataEntryPanel createDataEntryPanel(Hierarchy model) {
        try {
            return new HierarchyEditPanel(model);
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }
    
}
