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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.LockedColumnException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;

public class DeleteSelectedAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(DeleteSelectedAction.class);

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt;
    
    private TreeSelectionHandler treeSelectionHandler = new TreeSelectionHandler();

    public DeleteSelectedAction(ArchitectSwingSession session) throws ArchitectException {
        super(session, Messages.getString("DeleteSelectedAction.name"), Messages.getString("DeleteSelectedAction.description"), "delete"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);

        dbt = frame.getDbTree();
        dbt.addTreeSelectionListener(treeSelectionHandler);
        setupAction();
    }

    /**
     * This action takes care of handling Delete requests from the DBTree and Playpen.
     *  
     * If more than 1 components selected, it checks to see if any column is selected, 
     * in that case, we don't want tables with columns selected get deleted.
     *
     */
    public void actionPerformed(ActionEvent evt) {
        logger.debug("delete action detected!"); //$NON-NLS-1$
        logger.debug("ACTION COMMAND: " + evt.getActionCommand()); //$NON-NLS-1$

        if (dbt.getSelectionCount() < 1) {
            JOptionPane.showMessageDialog(playpen, Messages.getString("DeleteSelectedAction.noItemsToDelete")); //$NON-NLS-1$
            return;
        }
        
        List<SQLObject> deleteItems = getDeletableItems();

        int tCount = 0;
        int rCount = 0;
        int cCount = 0;
        int iCount = 0;

        for (SQLObject item : deleteItems) {
            if (item instanceof SQLColumn) {
                cCount++;
            } else if (item instanceof SQLTable) {
                tCount++;
            } else if (item instanceof SQLRelationship) {
                rCount++;
            } else if (item instanceof SQLIndex) {
                iCount++;
            } else {
                logger.warn("Unexpected type for deletable item: " + item.getClass());
            }
        }

        if (deleteItems.size() > 1) {
            int decision = JOptionPane.showConfirmDialog(frame,
                    Messages.getString("DeleteSelectedAction.multipleDeleteConfirmation", //$NON-NLS-1$
                            String.valueOf(tCount), String.valueOf(cCount),
                            String.valueOf(rCount), String.valueOf(iCount)),
                            Messages.getString("DeleteSelectedAction.multipleDeleteDialogTitle"), //$NON-NLS-1$
                            JOptionPane.YES_NO_OPTION);
            if (decision != JOptionPane.YES_OPTION ) {
                return;
            }
        }

        try {
            playpen.startCompoundEdit("Delete"); //$NON-NLS-1$

            // prevent tree selection during delete
            playpen.setIgnoreTreeSelection(true);

            //We deselect the components first because relationships might be already
            //deleted when one of the table that it was attached to got deleted.
            //Therefore deselecting them when it comes around in the item list would
            //cause an exception.

//            for (PlayPenComponent ppc : deleteItems){
//                ppc.setSelected(false,SelectionEvent.SINGLE_SELECT);
//            }
            
            for (SQLObject o : deleteItems) {
                try {
                    o.getParent().removeChild(o);
                } catch (LockedColumnException ex) {
                    int decision = JOptionPane.showConfirmDialog(playpen,
                            Messages.getString("DeleteSelectedAction.couldNotDeleteColumnContinueConfirmation", o.getName(), ex.getLockingRelationship().toString()), //$NON-NLS-1$
                            Messages.getString("DeleteSelectedAction.couldNotDeleteColumnDialogTitle"), //$NON-NLS-1$
                            JOptionPane.YES_NO_OPTION);
                    if (decision == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }
            
        } finally {
            playpen.endCompoundEdit("Ending multi-select"); //$NON-NLS-1$
            playpen.setIgnoreTreeSelection(false);
        }
    }

    /**
     * Extracts the list of items we should try to delete from the DBTree's
     * selection list.
     */
    private List<SQLObject> getDeletableItems() {
        TreePath[] selectionPaths = dbt.getSelectionPaths();
        if (selectionPaths == null) return Collections.emptyList();
        List <SQLObject> deleteItems = new ArrayList<SQLObject>(selectionPaths.length);
        for (int i = 0; i < selectionPaths.length; i++) {
            if (   selectionPaths[i].getPathCount() > 1 &&
                   selectionPaths[i].getPathComponent(1) == session.getTargetDatabase()) {
                deleteItems.add((SQLObject) selectionPaths[i].getLastPathComponent());
            } else {
                logger.debug("Skipping non-deletable object: " +
                        selectionPaths[i].getLastPathComponent());
            }
        }
        
        Set<SQLTable> tablesWithSelectedColumns = new HashSet<SQLTable>();
        
        for (ListIterator<SQLObject> it = deleteItems.listIterator(); it.hasNext(); ) {
            SQLObject item = it.next();
            if (item instanceof SQLColumn) {
                tablesWithSelectedColumns.add(((SQLColumn) item).getParentTable());
            } else if (item instanceof SQLTable) {
                // ok
            } else if (item instanceof SQLRelationship) {
                // ok
            } else if (item instanceof SQLIndex) {
                // ok
            } else {
                // The business model is not fancy enough to properly cope with
                // deletion of other types using the removeChild() method yet.
                // The item will get deleted, but the model's invariants will not
                // be maintained properly.
                // It is a goal to move in that direction, and as we go there, we
                // can relax this constraint.
                logger.debug("Not deleting selected tree item " + item);
                it.remove();
            }
        }

        // When a column is selected in the playpen, its parent table is also selected.
        // In this case, we want to delete the selected column(s) but NOT the parent table!
        deleteItems.removeAll(tablesWithSelectedColumns);

        return deleteItems;
    }

    private class TreeSelectionHandler implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            setupAction();
        }
    }

    /**
     * Updates the tooltip and enabledness of this action based on how many
     * items are selected. If there is only one selected item, tries to put its
     * name in the tooltip too!
     */
    private void setupAction() {
        String description;
        List<SQLObject> deletableItems = getDeletableItems();
        if (deletableItems.size() == 0) {
            setEnabled(false);
            description = Messages.getString("DeleteSelectedAction.deleteSelected"); //$NON-NLS-1$
        } else if (deletableItems.size() == 1) {
            setEnabled(true);
            SQLObject item = deletableItems.get(0);
            String name = item.getName();
            description = Messages.getString("DeleteSelectedAction.deleteItem", name); //$NON-NLS-1$
        } else {
            setEnabled(true);
            int numSelectedItems = deletableItems.size();
            description = Messages.getString("DeleteSelectedAction.deleteNumberOfItems", String.valueOf(numSelectedItems)); //$NON-NLS-1$
        }
        putValue(SHORT_DESCRIPTION, description + Messages.getString("DeleteSelectedAction.shortcut")); //$NON-NLS-1$
    }
}
