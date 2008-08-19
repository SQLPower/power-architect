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

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.OLAPPane;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * A generic abstract action that provides the bulk of the code required
 * in order to add an item to the selected pane in the play pen.  To use
 * it, create a subclass with a constructor that provides all the appropriate
 * settings in the super() call, and implement the small handful of abstract
 * methods declared here.
 *
 * @param <C> The type of item being added
 */
public abstract class CreateOLAPChildAction<C extends OLAPObject> extends AbstractArchitectAction {

    /**
     * Watches the playpen and sets the properties of this action according
     * to what's going on.
     */
    private class PlayPenWatcher implements SelectionListener {
        
        public PlayPenWatcher() {
        }
        
        private void updateStatus() {
            List<PlayPenComponent> selectedItems = playpen.getSelectedItems();
            String description;
            if (selectedItems.size() == 1 && paneClass.isInstance(selectedItems.get(0))) {
                setEnabled(true);
                description = "Add " + friendlyChildName + " to " + selectedItems.get(0).getName();
            } else {
                setEnabled(false);
                description = "Add " + friendlyChildName + " to selected " + friendlyParentName;
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
    
    private final String friendlyParentName;
    private final String friendlyChildName;
    private final Class<? extends OLAPPane<?, ? super C>> paneClass;

    /**
     * Creates a new generic adding action.
     * 
     * @param session
     *            The session the play pen belongs to.
     * @param olapPlayPen
     *            The play pen the item will be added to.
     * @param friendlyChildName
     *            The "friendly" name for the type of item being added.
     * @param paneClass
     *            The type of pane that must be selected in order to add an
     *            item. This action will disable itself unless there's one item
     *            selected, and it's of this type.
     * @param friendlyParentName
     *            The "friendly" name for the type the child is being added to.
     * @param accelKey
     *            The key character that should be used to invoke this action
     *            from the keyboard.
     */
    public CreateOLAPChildAction(ArchitectSwingSession session, PlayPen olapPlayPen,
            String friendlyChildName, Class<? extends OLAPPane<?, ? super C>> paneClass,
                    String friendlyParentName, char accelKey) {
        super(session, olapPlayPen, "New " + friendlyChildName, null, null);
        this.friendlyChildName = friendlyChildName;
        this.paneClass = paneClass;
        this.friendlyParentName = friendlyParentName;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelKey));
        PlayPenWatcher ppw = new PlayPenWatcher();
        playpen.addSelectionListener(ppw);
        ppw.updateStatus();
    }

    public void actionPerformed(ActionEvent e) {
        List<PlayPenComponent> selectedItems = playpen.getSelectedItems();
        Object pane = selectedItems.get(0);
        final OLAPObject parent = paneClass.cast(pane).getModel();
        final C child = createChildInstance();
        parent.addChild(child);
        
        final DataEntryPanel mep = createDataEntryPanel(child);
        
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return mep.applyChanges();
            }
        };
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                parent.removeChild(child);
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

    /**
     * Creates a new child of type C, and sets its default property values.
     * 
     * @return A new unparented child instance with reasonable default property
     *         values.
     */
    protected abstract C createChildInstance();

    /**
     * Creates a DataEntryPanel for editing the given child item.
     * 
     * @param model the item that should be edited in the data entry panel
     */
    protected abstract DataEntryPanel createDataEntryPanel(C model);
}
