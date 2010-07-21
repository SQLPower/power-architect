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

import javax.swing.Icon;
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
public abstract class CreateOLAPChildAction<P extends OLAPPane<?, ?>, C extends OLAPObject> extends AbstractArchitectAction {

    /**
     * Watches the playpen and sets the properties of this action according
     * to what's going on.
     */
    private class PlayPenWatcher implements SelectionListener {
        
        public void itemDeselected(SelectionEvent e) {
            updateActionState();
        }

        public void itemSelected(SelectionEvent e) {
            updateActionState();
        }
        
    }
    
    private final String friendlyParentName;
    private final String friendlyChildName;
    private final Class<P> paneClass;

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
     * @param icon
     *            The icon for this action.
     */
    public CreateOLAPChildAction(ArchitectSwingSession session, PlayPen olapPlayPen,
            String friendlyChildName, Class<P> paneClass,
            String friendlyParentName, char accelKey, Icon icon) {
        super(session, olapPlayPen, "New " + friendlyChildName + "...", null, icon);
        this.friendlyChildName = friendlyChildName;
        this.paneClass = paneClass;
        this.friendlyParentName = friendlyParentName;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelKey));
        PlayPenWatcher ppw = new PlayPenWatcher();
        getPlaypen().addSelectionListener(ppw);
        updateActionState();
    }

    public void actionPerformed(ActionEvent e) {
        List<PlayPenComponent> selectedItems = getPlaypen().getSelectedItems();
        final P pane = paneClass.cast(selectedItems.get(0));
        
        // workaround for javac problem (eclipse doesn't need the cast)
        ((OLAPPane<?,?>) pane).getModel().begin("Add " + friendlyChildName);
        final C child = addNewChild(pane);
        
        final DataEntryPanel mep = createDataEntryPanel(child);
        
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                boolean applied = mep.applyChanges();
                ((OLAPPane<?,?>) pane).getModel().commit();  // another javac workaround
                return applied;
            }
        };
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                child.getParent().removeChild(child);
                ((OLAPPane<?,?>) pane).getModel().commit();  // another javac workaround
                return true;
            }
        };
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                mep,
                SwingUtilities.getWindowAncestor(getPlaypen()),
                friendlyChildName + " Properties",
                DataEntryPanelBuilder.OK_BUTTON_LABEL,
                okCall,
                cancelCall);
        d.setLocationRelativeTo(getPlaypen());
        d.setVisible(true);
    }

    /**
     * Creates a new child of type C, sets good default values for its
     * properties, and adds it to the model as appropriate.
     * 
     * @return A new child instance with reasonable default property values,
     *         which has been added into the business model tree in the
     *         appropriate place.
     */
    protected abstract C addNewChild(P selectedPane);

    /**
     * Creates a DataEntryPanel for editing the given child item.
     * 
     * @param model the item that should be edited in the data entry panel
     */
    protected abstract DataEntryPanel createDataEntryPanel(C model);
 
    /**
     * This method is called whenever the selection changes in the OLAP playpen.
     * If you override this method, you should call {@link #setEnabled(boolean)}
     * and {@link #putValue(String, Object)} with a key of SHORT_DESCRIPTION in
     * order to update the tooltip for this action.
     * <p>
     * Note that the default implementation is normally acceptable; there are only
     * a few cases (such as levels, which get added under hierarchies in dimension
     * panes) where overriding is necessary.
     */
    protected void updateActionState() {
        List<PlayPenComponent> selectedItems = getPlaypen().getSelectedItems();
        String description;
        if (selectedItems.size() == 1 && paneClass.isInstance(selectedItems.get(0))) {
            setEnabled(true);
            description = "Add " + friendlyChildName + " to " + selectedItems.get(0).getName();
        } else {
            setEnabled(false);
            description = "Add " + friendlyChildName + " to selected " + friendlyParentName + 
            " (" + ((KeyStroke) getValue(ACCELERATOR_KEY)).getKeyChar() + ")";
        }
        putValue(SHORT_DESCRIPTION, description);
    }
}
