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

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.LevelEditPanel;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateLevelAction extends CreateOLAPChildAction<DimensionPane, Level> {

    public CreateLevelAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "Level", DimensionPane.class, "Hierarchy", 'l', OSUtils.LEVEL_ADD_ICON);
    }

    @Override
    protected Level addNewChild(DimensionPane pane) {
        // first, we have to find or make the parent hierarchy 
        Dimension d = pane.getModel();
        if (d.getHierarchies().size() == 0) {
            Hierarchy h = new Hierarchy();
            h.setHasAll(true);
            d.addHierarchy(h);
        }
        
        Hierarchy newParent;
        int newIndex;
        
        // If there are levels selected, we'll add after the last one
        List<Level> levels = pane.getSelectedLevels();
        if (!levels.isEmpty()) {
            Level addAfter = levels.get(levels.size() - 1);
            newParent = (Hierarchy) addAfter.getParent();
            newIndex = newParent.getLevels().indexOf(addAfter) + 1;

        } else {
            // no levels were selected, so we'll add to the end of the selected section
            // (or the first section if nothing was selected at all)
            newParent = d.getHierarchies().get(0);
            for (int i = 0; i < d.getHierarchies().size(); i++) {
                if (pane.isSectionSelected(pane.getSections().get(i))) {
                    newParent = d.getHierarchies().get(i);
                    break;
                }
            }
            newIndex = newParent.getLevels().size();
        }
        
        Level l = new Level();

        int count = 1;
        while (!OLAPUtil.isNameUnique(newParent, Level.class, "New Level " + count)) {
            count++;
        }
        l.setName("New Level " + count);
        
        newParent.addLevel(newIndex, l);
        return l;
    }

    @Override
    protected DataEntryPanel createDataEntryPanel(Level model) {
        try {
            return new LevelEditPanel(model);
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }

}
