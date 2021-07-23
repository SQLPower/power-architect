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
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenLabel;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.ItemSelectionEvent;
import ca.sqlpower.architect.swingui.event.ItemSelectionListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.LockedColumnException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

public class DeleteSelectedAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(DeleteSelectedAction.class);

    private SelectionHandler selectionHandler = new SelectionHandler();

    public DeleteSelectedAction(ArchitectSwingSession session) throws SQLObjectException {
        super(session, Messages.getString("DeleteSelectedAction.name"), Messages.getString("DeleteSelectedAction.description"), "delete"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        putValue(ACTION_COMMAND_KEY, PlayPen.ACTION_COMMAND_SRC_PLAYPEN);

        getPlaypen().addSelectionListener(selectionHandler);
        setupAction();
    }
    
    public DeleteSelectedAction(ArchitectFrame frame) {
        super(frame, Messages.getString("DeleteSelectedAction.name"), Messages.getString("DeleteSelectedAction.description"), "delete"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        putValue(ACTION_COMMAND_KEY, PlayPen.ACTION_COMMAND_SRC_PLAYPEN);

        frame.addSelectionListener(selectionHandler);
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

        final TreePath[] selectionPaths = getSession().getDBTree().getSelectionPaths();
        final int selectionPathsSize = selectionPaths != null ? selectionPaths.length : 0;
        if (getPlaypen().getSelectedItems().size() + selectionPathsSize < 1) {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("DeleteSelectedAction.noItemsToDelete")); //$NON-NLS-1$
            return;
        }
        
        List<SPObject> deleteItems = retrieveDeletableItems();

        int tCount = 0;
        int rCount = 0;
        int cCount = 0;
        int iCount = 0;
        int lCount = 0;

        for (SPObject item : deleteItems) {
            if (item instanceof SQLColumn) {
                cCount++;
            } else if (item instanceof SQLTable) {
                tCount++;
                // to improve message, In case if table is selected to delete, then all children of selected table also deleted
                for (SPObject child :  ((SQLTable)item).getChildren()) {
                    if (!deleteItems.contains(child)) {
                        if (child instanceof SQLColumn ) {
                            cCount++;
                        } else if (child instanceof SQLRelationship) {
                            rCount++;
                        } else if (child instanceof SQLIndex) {
                            iCount++;
                        }
                    }
                }
            } else if (item instanceof SQLRelationship) {
                rCount++;
            } else if (item instanceof SQLIndex) {
                iCount++;
            } else if (item instanceof PlayPenLabel) {
                lCount++;
            } else {
                logger.warn("Unexpected type for deletable item: " + item.getClass());
            }
        }

        if (deleteItems.size() > 1) {
            int decision = JOptionPane.showConfirmDialog(frame,
                    Messages.getString("DeleteSelectedAction.multipleDeleteConfirmation", //$NON-NLS-1$
                            String.valueOf(tCount), String.valueOf(cCount),
                            String.valueOf(rCount), String.valueOf(iCount),
                            String.valueOf(lCount)),
                            Messages.getString("DeleteSelectedAction.multipleDeleteDialogTitle"), //$NON-NLS-1$
                            JOptionPane.YES_NO_OPTION);
            if (decision != JOptionPane.YES_OPTION ) {
                return;
            }
        }

        getPlaypen().getContentPane().begin("Delete");
        try {
            getPlaypen().startCompoundEdit("Delete"); //$NON-NLS-1$

            // prevent tree selection during delete
            getPlaypen().setIgnoreTreeSelection(true);

            //We deselect the components first because relationships might be already
            //deleted when one of the table that it was attached to got deleted.
            //Therefore deselecting them when it comes around in the item list would
            //cause an exception.

//            for (PlayPenComponent ppc : deleteItems){
//                ppc.setSelected(false,SelectionEvent.SINGLE_SELECT);
//            }
            
            for (SPObject o : deleteItems) {
                try {
                    if (o instanceof SQLIndex) {
                        SQLIndex index = (SQLIndex) o;
                        o.getParent().removeChild(o);
                    } else if (o instanceof PlayPenLabel) {
                        getPlaypen().getContentPane().removeChild(o);
                    } else {
                        //Side effect of removing a relationship's parent table is to remove the relationship
                        //causing this to fail if the relationship is removed immediately after.
                        if (o.getParent() != null && o.getParent() instanceof SQLObject &&  
                                ((SQLObject) o.getParent()).getChildrenWithoutPopulating().contains(o)) {
                            o.getParent().removeChild(o);
                        }
                    }
                } catch (LockedColumnException ex) {
                    int decision = JOptionPane.showConfirmDialog(getPlaypen(),
                            Messages.getString("DeleteSelectedAction.couldNotDeleteColumnContinueConfirmation", ex.getCol().getName(), ex.getLockingRelationship().toString()), //$NON-NLS-1$
                            Messages.getString("DeleteSelectedAction.couldNotDeleteColumnDialogTitle"), //$NON-NLS-1$
                            JOptionPane.YES_NO_OPTION);
                    if (decision == JOptionPane.NO_OPTION) {
                        getPlaypen().getContentPane().commit();
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    getPlaypen().getContentPane().rollback(e.toString());
                    throw new RuntimeException(e);
                } catch (ObjectDependentException e) {
                    getPlaypen().getContentPane().rollback(e.toString());
                    throw new RuntimeException(e);
                } catch (Throwable e) {
                    getPlaypen().getContentPane().rollback(e.toString());
                    throw new RuntimeException(e);
                }
            }
            getPlaypen().getContentPane().commit();
        } finally {
            getPlaypen().endCompoundEdit("Ending multi-select"); //$NON-NLS-1$
            getPlaypen().setIgnoreTreeSelection(false);
        }
    }

    /**
     * Extracts the list of items we should try to delete from the PlayPen's
     * selection list. Package private for testing.
     */
    List<SPObject> retrieveDeletableItems() {
        List<PlayPenComponent> selection = getPlaypen().getSelectedItems();
        List<SPObject> deleteItems = new ArrayList<SPObject>(selection.size());
        for (PlayPenComponent ppc : selection) {
            if (ppc instanceof TablePane) {
                TablePane tp = (TablePane) ppc;
                deleteItems.addAll(tp.getSelectedItems());
            } else if (ppc instanceof PlayPenLabel) {
                deleteItems.add(ppc);
            }
            Object model = ppc.getModel();
            if (model instanceof SQLObject) {
                deleteItems.add((SQLObject) model);
            }
        }
        
        // Invisible elements in the PlayPen but selected in the TreeView
        TreePath[] selectionPaths = getSession().getDBTree().getSelectionPaths();
        if (selectionPaths != null) {
            for (final TreePath tp : selectionPaths) {
                final Object c = tp.getLastPathComponent();
                if (c instanceof SQLIndex && !deleteItems.contains(c)) {
                    final SQLIndex index = (SQLIndex) c;
                    deleteItems.add(index);
                }
            }
        }
        
        Set<SQLTable> tablesWithSelectedColumns = new HashSet<SQLTable>();
        
        for (ListIterator<SPObject> it = deleteItems.listIterator(); it.hasNext(); ) {
            SPObject item = it.next();
            if (item instanceof SQLColumn) {
                tablesWithSelectedColumns.add(((SQLColumn) item).getParent());
            } else if (item instanceof SQLTable) {
                // ok
            } else if (item instanceof SQLRelationship) {
                // ok
            } else if (item instanceof SQLIndex) {
                // ok
            } else if (item instanceof PlayPenLabel) {
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

    @SuppressWarnings("unchecked")
    private class SelectionHandler implements SelectionListener, ItemSelectionListener {
        public void itemSelected(SelectionEvent e) {
            setupAction();
            if (e.getSource() instanceof ContainerPane) {
                ((ContainerPane) e.getSource()).addItemSelectionListener(this);
            }
        }
        public void itemDeselected(SelectionEvent e) {
            setupAction();
            if (e.getSource() instanceof ContainerPane) {
                ((ContainerPane) e.getSource()).removeItemSelectionListener(this);
            }
        }
        public void itemsDeselected(ItemSelectionEvent e) {
            setupAction();
        }
        public void itemsSelected(ItemSelectionEvent e) {
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
        List<SPObject> deletableItems = retrieveDeletableItems();
        if (deletableItems.size() == 0) {
            setEnabled(false);
            description = Messages.getString("DeleteSelectedAction.deleteSelected"); //$NON-NLS-1$
        } else if (deletableItems.size() == 1) {
            setEnabled(true);
            SPObject item = deletableItems.get(0);
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
