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

import javax.swing.KeyStroke;

import ca.sqlpower.architect.olap.MondrianModel;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
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

    /**
     * Describes the location where the new level should be added.
     */
    private static class AddLocation {

        /**
         * The hierarchy to add the level into. If the level should be added to
         * a new hierarchy (normally this is because the dimension has no
         * hierarchies yet), this value should be null.
         */
        private final Hierarchy newParent;

        /**
         * The index inside the specified hierarchy at which to add the new
         * level.
         */
        private final int newIndex;

        /**
         * Creates a new add location.
         * 
         * @param newParent
         *            The hierarchy to add the level into. If the level should
         *            be added to a new hierarchy (normally this is because the
         *            dimension has no hierarchies yet), this value should be
         *            null.
         * @param newIndex
         *            The index inside the specified hierarchy at which to add
         *            the new level.
         */
        public AddLocation(Hierarchy newParent, int newIndex) {
            this.newParent = newParent;
            this.newIndex = newIndex;
        }
    }
    
    @Override
    protected Level addNewChild(DimensionPane pane) {
        Dimension d = pane.getModel();
        AddLocation addLocation = chooseAddLocation(pane);
        MondrianModel.Hierarchy newParent;
        if (addLocation.newParent == null) {
            newParent = new Hierarchy();
            newParent.setHasAll(true);
            newParent.setName(d.getName());
            d.addHierarchy(newParent);
            updateActionState(); // otherwise tooltip is stale and still says "new hierarchy"
        } else {
            newParent = addLocation.newParent;
        }
        
        Level l = new Level();
        int count = 1;
        while (!OLAPUtil.isNameUnique(newParent, Level.class, "New Level " + count)) {
            count++;
        }
        l.setName("New Level " + count);
        
        newParent.addLevel(addLocation.newIndex, l);
        
        return l;
    }

    private AddLocation chooseAddLocation(DimensionPane pane) {
        Hierarchy newParent;
        int newIndex;

        // If there are levels selected, we'll add after the last one
        List<Level> selectedLevels = pane.getSelectedLevels();
        if (!selectedLevels.isEmpty()) {
            Level addAfter = selectedLevels.get(selectedLevels.size() - 1);
            newParent = (Hierarchy) addAfter.getParent();
            newIndex = newParent.getLevels().indexOf(addAfter) + 1;

        } else {
            // no levels were selected, so we'll add to the end of the selected section
            // (or the first section if nothing was selected at all)
            Dimension d = pane.getModel();
            if (d.getHierarchies().isEmpty()) {
                newParent = null;
                newIndex = 0;
            } else {
                newParent = d.getHierarchies().get(0);
                for (int i = 0; i < d.getHierarchies().size(); i++) {
                    if (pane.isSectionSelected(pane.getSections().get(i))) {
                        newParent = d.getHierarchies().get(i);
                        break;
                    }
                }
                newIndex = newParent.getLevels().size();
            }
        }

        return new AddLocation(newParent, newIndex);
    }

    @Override
    protected DataEntryPanel createDataEntryPanel(Level model) {
        try {
            return new LevelEditPanel(model);
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }

    @Override
    protected void updateActionState() {
        List<PlayPenComponent> selectedItems = getPlaypen().getSelectedItems();
        String description;
        if (selectedItems.size() == 1 && selectedItems.get(0) instanceof DimensionPane) {
            setEnabled(true);
            DimensionPane pane = (DimensionPane) selectedItems.get(0);
            AddLocation addLocation = chooseAddLocation(pane);
            String parentName;
            if (addLocation.newParent == null) {
                parentName = "new hierarchy";
            } else {
                parentName = addLocation.newParent.getName();
            }
            description = "Add Level to " + parentName;
        } else {
            setEnabled(false);
            description = "Add Level to selected Hierarchy" + 
            " (" + ((KeyStroke) getValue(ACCELERATOR_KEY)).getKeyChar() + ")";
        }
        putValue(SHORT_DESCRIPTION, description);
    }
}
