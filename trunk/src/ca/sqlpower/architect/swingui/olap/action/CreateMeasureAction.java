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
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.MeasureEditPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class CreateMeasureAction extends AbstractArchitectAction {

    /**
     * Watches the playpen and sets the properties of this action according
     * to what's going on.
     */
    private class PlayPenWatcher implements SelectionListener {

        private void updateStatus() {
            List<PlayPenComponent> selectedItems = playpen.getSelectedItems();
            String description;
            if (selectedItems.size() == 1 && selectedItems.get(0) instanceof CubePane) {
                setEnabled(true);
                description = "Add measure to " + selectedItems.get(0).getName();
            } else {
                setEnabled(false);
                description = "Add measure to selected cube";
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
    
    public CreateMeasureAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "New Measure", null, null);
        PlayPenWatcher ppw = new PlayPenWatcher();
        playpen.addSelectionListener(ppw);
        ppw.updateStatus();
    }

    public void actionPerformed(ActionEvent e) {
        List<PlayPenComponent> selectedItems = playpen.getSelectedItems();
        CubePane cp = (CubePane) selectedItems.get(0);
        final Cube cube = cp.getModel();
        final Measure m = new Measure();
        m.setName("New Measure");
        cube.addMeasure(m);
        
        final MeasureEditPanel mep = new MeasureEditPanel(m);
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return mep.applyChanges();
            }
        };
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                cube.removeMeasure(m);
                return true;
            }
        };
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                mep,
                SwingUtilities.getWindowAncestor(playpen),
                "Measure Properties",
                "OK",
                okCall,
                cancelCall);
        d.setLocationRelativeTo(playpen);
        d.setVisible(true);
    }
    
}
