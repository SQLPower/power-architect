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

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.HierarchyEditPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * Action for adding a hierarchy to the selected dimension.
 */
public class CreateHierarchyAction extends AbstractArchitectAction {

    /**
     * Watches the playpen and sets the properties of this action according
     * to what's going on.
     */
    private class PlayPenWatcher implements SelectionListener {

        private void updateStatus() {
            List<PlayPenComponent> selectedItems = playpen.getSelectedItems();
            String description;
            if (selectedItems.size() == 1 && selectedItems.get(0) instanceof DimensionPane) {
                setEnabled(true);
                description = "Add a hierarchy to " + selectedItems.get(0).getName();
            } else {
                setEnabled(false);
                description = "Add hierarchy to selected dimension";
            }
            putValue(SHORT_DESCRIPTION, description);
        }
        
        public void itemDeselected(SelectionEvent e) {
            updateStatus();
        }

        public void itemSelected(SelectionEvent e) {
            updateStatus();
        }
        
    }
    
    public CreateHierarchyAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "New Hierarchy", null, null);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('h'));
        PlayPenWatcher ppw = new PlayPenWatcher();
        playpen.addSelectionListener(ppw);
        ppw.updateStatus();
    }

    public void actionPerformed(ActionEvent e) {
        List<PlayPenComponent> selectedItems = playpen.getSelectedItems();
        DimensionPane cp = (DimensionPane) selectedItems.get(0);
        final Dimension dimension = cp.getModel();
        final Hierarchy h = new Hierarchy();
        h.setName("New Hierarchy");
        dimension.addHierarchy(h);
        
        final HierarchyEditPanel hep = new HierarchyEditPanel(h);
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return hep.applyChanges();
            }
        };
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                dimension.removeHierarchy(h);
                return true;
            }
        };
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                hep,
                SwingUtilities.getWindowAncestor(playpen),
                "Hierarchy Properties",
                "OK",
                okCall,
                cancelCall);
        d.setLocationRelativeTo(playpen);
        d.setVisible(true);
    }
    
}
