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

import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.LevelEditPanel;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateLevelAction extends CreateOLAPChildAction<DimensionPane, Level> {

    public CreateLevelAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "Level", DimensionPane.class, "Hierarchy", 'l');
    }

    @Override
    protected Level addNewChild(DimensionPane pane) {
        // first, we have to find or make the parent hierarchy 
        Dimension d = pane.getModel();
        if (d.getHierarchies().size() == 0) {
            d.addHierarchy(new Hierarchy());
        }
        Hierarchy newParent;
        List<Level> selectedItems = pane.getSelectedItems();
        if (selectedItems.size() == 0) {
            newParent = d.getHierarchies().get(0);
        } else {
            newParent = (Hierarchy) selectedItems.get(0).getParent();
        }
        
        Level l = new Level();
        l.setName("New Level");
        newParent.addLevel(l);
        return l;
    }

    @Override
    protected DataEntryPanel createDataEntryPanel(Level model) {
        try {
            return new LevelEditPanel(model);
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }

}
