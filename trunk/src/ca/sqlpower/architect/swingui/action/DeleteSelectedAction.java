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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.LockedColumnException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class DeleteSelectedAction extends AbstractArchitectAction implements SelectionListener {
    private static final Logger logger = Logger.getLogger(DeleteSelectedAction.class);

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt;

    public DeleteSelectedAction(ArchitectSwingSession session) throws ArchitectException {
        super(session, Messages.getString("DeleteSelectedAction.name"), Messages.getString("DeleteSelectedAction.description"), "delete"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        setEnabled(false);

        playpen.addSelectionListener(this);
        setupAction(playpen.getSelectedItems());

        dbt = frame.getDbTree();
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

        logger.debug("delete action came from playpen"); //$NON-NLS-1$
        List <PlayPenComponent>items = playpen.getSelectedItems();
        List <TablePane> tablesWithColumns = new ArrayList<TablePane>();
                
        // list of all tables and relationships to be deleted
        List <PlayPenComponent> deleteItems = new ArrayList<PlayPenComponent>(); 

        
        if (items.size() < 1) {
            JOptionPane.showMessageDialog(playpen, Messages.getString("DeleteSelectedAction.noItemsToDelete")); //$NON-NLS-1$
        } else {
            // one or more items selected
            int tCount = 0;
            int rCount = playpen.getSelectedRelationShips().size();
            int cCount = 0;
            // count how many relationships and tables there are
            for(int i = 0; i < items.size(); i++) {
                deleteItems.add(items.get(i));
            }
            TablePane currTable;
            try {
                for (int i = 0; i < deleteItems.size(); i++){
                    if (deleteItems.get(i) instanceof TablePane) {

                        currTable = (TablePane) deleteItems.get(i);			                
                        if (currTable.getSelectedColumns().size() > 0){
                            cCount = cCount + currTable.getSelectedColumns().size();
                            tablesWithColumns.add(currTable);
                            deleteItems.remove(i);
                            i--;
                        } else {
                            tCount++;
                        }
                    }
                }
            } catch (ArchitectException e) {
                logger.error("Fail to remove tables with column(s) selected from the list"); //$NON-NLS-1$
            }

            if (items.size() > 1) {
                int decision = JOptionPane.showConfirmDialog(frame,
                        Messages.getString("DeleteSelectedAction.multipleDeleteConfirmation", String.valueOf(tCount), String.valueOf(cCount), String.valueOf(rCount)), //$NON-NLS-1$
                        Messages.getString("DeleteSelectedAction.multipleDeleteDialogTitle"), //$NON-NLS-1$
                        JOptionPane.YES_NO_OPTION);
                if (decision != JOptionPane.YES_OPTION ) {
                    return;
                }
            }
        } 

        playpen.startCompoundEdit("Delete"); //$NON-NLS-1$
        // prevent tree selection during delete
        playpen.setIgnoreTreeSelection(true);
        try {

            //We deselect the components first because relationships might be already
            //deleted when one of the table that it was attached to got deleted.
            //Therefore deselecting them when it comes around in the item list would
            //cause an exception.

            for (PlayPenComponent ppc : deleteItems){
                ppc.setSelected(false,SelectionEvent.SINGLE_SELECT);
            }
            List<List <SQLColumn> > listOfColumns = new ArrayList<List <SQLColumn> >();
            List<SQLColumn> selectedColumns = new ArrayList<SQLColumn>();

            for (int i = 0; i < tablesWithColumns.size(); i++) {
                TablePane colTable = tablesWithColumns.get(i);
                // make a list of columns to delete
                try {
                    selectedColumns = colTable.getSelectedColumns();
                    if (selectedColumns != null) {
                        listOfColumns.add(selectedColumns);
                    }
                } catch (ArchitectException ae) {
                    ASUtils.showExceptionDialog(session,
                            Messages.getString("DeleteSelectedAction.couldNotFindSelectedColumns") , ae); //$NON-NLS-1$
                    return;
                }
            }
            
            // deletes all columns
            for (int j = 0; j < listOfColumns.size(); j++) {
                List <SQLColumn> allCol = (List <SQLColumn>) listOfColumns.get(j);
                Iterator itCol = allCol.iterator();
                while (itCol.hasNext()) {
                    SQLColumn sc = (SQLColumn) itCol.next();
                    try {
                        tablesWithColumns.get(j).getModel().removeColumn(sc);
                    } catch (LockedColumnException ex) {
                        int decision = JOptionPane.showConfirmDialog(playpen,
                                Messages.getString("DeleteSelectedAction.couldNotDeleteColumnContinueConfirmation", sc.getName(), ex.getLockingRelationship().toString()), //$NON-NLS-1$
                                Messages.getString("DeleteSelectedAction.couldNotDeleteColumnDialogTitle"), //$NON-NLS-1$
                                JOptionPane.YES_NO_OPTION);
                        if (decision == JOptionPane.NO_OPTION) {
                            return;
                        }
                    } catch (ArchitectException e) {
                        logger.error("Unexpected exception encountered when attempting to delete column '"+ //$NON-NLS-1$
                                sc+"' of table '"+sc.getParentTable()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
                        ASUtils.showExceptionDialog(session, Messages.getString("DeleteSelectedAction.couldNotDeleteColumn"), e); //$NON-NLS-1$
                    }
                }
            }

            Iterator it = deleteItems.iterator();
            
            // deletes all tables and relationships
            while (it.hasNext()) {
                Selectable item = (Selectable) it.next();
                logger.debug("next item for delete is: " + item.getClass().getName()); //$NON-NLS-1$
                if (item instanceof TablePane) {
                    TablePane tp = (TablePane) item;
                    session.getTargetDatabase().removeChild(tp.getModel());
                    HashSet tableNames = playpen.getTableNames();
                    String remove = tp.getName().substring(11,tp.getName().length()-8);
                    tableNames.remove(remove.toLowerCase());
                } else if (item instanceof Relationship) {
                    Relationship r = (Relationship) item;
                    logger.debug("trying to delete relationship " + r); //$NON-NLS-1$
                    SQLRelationship sr = r.getModel();
                    sr.getPkTable().removeExportedKey(sr);
                } else {
                    JOptionPane.showMessageDialog((JComponent) item,
                    Messages.getString("DeleteSelectedAction.selectedItemTypeNotRecognized")); //$NON-NLS-1$
                }

            } 
        } finally {
            playpen.endCompoundEdit("Ending multi-select"); //$NON-NLS-1$
            playpen.setIgnoreTreeSelection(false);
        }
    }

    public void itemSelected(SelectionEvent e) {
        try {
            setupAction(playpen.getSelectedItems());
        } catch (ArchitectException e1) {
            throw new ArchitectRuntimeException(e1);
        }
    }

    public void itemDeselected(SelectionEvent e) {
        try {
            setupAction(playpen.getSelectedItems());
        } catch (ArchitectException e1) {
            throw new ArchitectRuntimeException(e1);
        }
    }

    /**
     * Updates the tooltip and enabledness of this action based on how
     * many items are in the selection list.  If there is only one
     * selected item, tries to put its name in the tooltip too!
     * @throws ArchitectException
     */
    private void setupAction(List selectedItems) throws ArchitectException {
        String Description;
        if (selectedItems.size() == 0) {
            setEnabled(false);
            Description = Messages.getString("DeleteSelectedAction.deleteSelected"); //$NON-NLS-1$
        } else if (selectedItems.size() == 1) {
            Selectable item = (Selectable) selectedItems.get(0);
            setEnabled(true);
            String name = Messages.getString("DeleteSelectedAction.selected"); //$NON-NLS-1$
            if (item instanceof TablePane) {
                TablePane tp = (TablePane) item;
                if (tp.getSelectedColumnIndex() >= 0) {
                    try {
                        List<SQLColumn> selectedColumns = tp.getSelectedColumns();
                        if (selectedColumns.size() > 1) {
                            name = selectedColumns.size()+Messages.getString("DeleteSelectedAction.numberOfSelectedItems"); //$NON-NLS-1$
                        } else {
                            name = tp.getModel().getColumn(tp.getSelectedColumnIndex()).getName();
                        }
                    } catch (ArchitectException ex) {
                        logger.error("Couldn't get selected column name", ex); //$NON-NLS-1$
                    }
                } else {
                    name = tp.getModel().getName();
                }
            } else if (item instanceof Relationship) {
                name = ((Relationship) item).getModel().getName();
            }
            Description = Messages.getString("DeleteSelectedAction.deleteItem")+name; //$NON-NLS-1$
        } else {
            setEnabled(true);
            int numSelectedItems =0;
            for (Object item : selectedItems) {
                numSelectedItems++;
                if (item instanceof TablePane) {
                    // Because the table pane is already counted we need to add one less
                    // than the columns unless there are no columns selected.  Then
                    // We need to add 0
                    numSelectedItems += Math.max(((TablePane) item).getSelectedColumns().size()-1, 0);
                }
            }
            Description = Messages.getString("DeleteSelectedAction.deleteNumberOfItems", String.valueOf(numSelectedItems)); //$NON-NLS-1$
        }
        putValue(SHORT_DESCRIPTION, Description + Messages.getString("DeleteSelectedAction.shortcut")); //$NON-NLS-1$
    }
}
