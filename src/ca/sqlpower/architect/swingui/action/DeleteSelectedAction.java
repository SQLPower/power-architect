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
        super(session, "Delete Selected", "Delete Selected", "delete");
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
        logger.debug("delete action detected!");
        logger.debug("ACTION COMMAND: " + evt.getActionCommand());

        logger.debug("delete action came from playpen");
        List <PlayPenComponent>items = playpen.getSelectedItems();
        List <TablePane> tablesWithColumns = new ArrayList<TablePane>();
                
        // list of all tables and relationships to be deleted
        List <PlayPenComponent> deleteItems = new ArrayList<PlayPenComponent>(); 

        boolean deletingColumns = false;

        if (items.size() < 1) {
            JOptionPane.showMessageDialog(playpen, "No items to delete!");
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
                            deletingColumns = true;
                            i--;
                        } else {
                            tCount++;
                        }
                    }
                }
            } catch (ArchitectException e) {
                logger.error("Fail to remove tables with column(s) selected from the list");
            }

            if (items.size() > 1) {
                int decision = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to delete these "
                        +tCount+" tables, " + cCount + " columns and "+rCount+" relationships?",
                        "Multiple Delete",
                        JOptionPane.YES_NO_OPTION);
                if (decision != JOptionPane.YES_OPTION ) {
                    return;
                }
            }
        } 

        playpen.startCompoundEdit("Delete");
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
                            "Could not find selected columns." , ae);
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
                                "Could not delete the column " + sc.getName() + " because it is part of\n" +
                                "the relationship \""+ex.getLockingRelationship()+"\".\n\n" +
                                "Continue deleting remaining selected columns?",
                                "Column is Locked",
                                JOptionPane.YES_NO_OPTION);
                        if (decision == JOptionPane.NO_OPTION) {
                            return;
                        }
                    } catch (ArchitectException e) {
                        logger.error("Unexpected exception encountered when attempting to delete column '"+
                                sc+"' of table '"+sc.getParentTable()+"'");
                        ASUtils.showExceptionDialog(session, "Could not delete the column", e);
                    }
                }
            }

            Iterator it = deleteItems.iterator();
            
            // deletes all tables and relationships
            while (it.hasNext()) {
                Selectable item = (Selectable) it.next();
                logger.debug("next item for delete is: " + item.getClass().getName());
                if (item instanceof TablePane) {
                    TablePane tp = (TablePane) item;
                    session.getTargetDatabase().removeChild(tp.getModel());
                    HashSet tableNames = playpen.getTableNames();
                    String remove = tp.getName().substring(11,tp.getName().length()-8);
                    tableNames.remove(remove.toLowerCase());
                } else if (item instanceof Relationship) {
                    Relationship r = (Relationship) item;
                    logger.debug("trying to delete relationship " + r);
                    SQLRelationship sr = r.getModel();
                    sr.getPkTable().removeExportedKey(sr);
                } else {
                    JOptionPane.showMessageDialog((JComponent) item,
                    "The selected item type is not recognised");
                }

            } 
        } finally {
            playpen.endCompoundEdit("Ending multi-select");
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
            Description = "Delete Selected";
        } else if (selectedItems.size() == 1) {
            Selectable item = (Selectable) selectedItems.get(0);
            setEnabled(true);
            String name = "Selected";
            if (item instanceof TablePane) {
                TablePane tp = (TablePane) item;
                if (tp.getSelectedColumnIndex() >= 0) {
                    try {
                        List<SQLColumn> selectedColumns = tp.getSelectedColumns();
                        if (selectedColumns.size() > 1) {
                            name = selectedColumns.size()+" items";
                        } else {
                            name = tp.getModel().getColumn(tp.getSelectedColumnIndex()).getName();
                        }
                    } catch (ArchitectException ex) {
                        logger.error("Couldn't get selected column name", ex);
                    }
                } else {
                    name = tp.getModel().getName();
                }
            } else if (item instanceof Relationship) {
                name = ((Relationship) item).getModel().getName();
            }
            Description = "Delete "+name;
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
            Description = "Delete "+numSelectedItems+" items";
        }
        putValue(SHORT_DESCRIPTION, Description + " (Shortcut delete)");
    }
}
