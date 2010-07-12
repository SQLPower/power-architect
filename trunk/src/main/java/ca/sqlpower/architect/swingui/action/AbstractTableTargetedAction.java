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
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public abstract class AbstractTableTargetedAction extends AbstractArchitectAction implements SelectionListener {

    public AbstractTableTargetedAction(
            ArchitectFrame frame,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        super(frame, actionName, actionDescription, iconResourceName);
        
        frame.addSelectionListener(this);
        
        setupAction(getPlaypen().getSelectedItems());
    }
    
    public void actionPerformed(ActionEvent evt) {
        try {
            DBTree dbt = getSession().getDBTree();
            if (evt.getActionCommand().equals(PlayPen.ACTION_COMMAND_SRC_PLAYPEN)) {
                List selection = getPlaypen().getSelectedItems();
                if (selection.size() < 1) {
                    JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("AbstractTableTargetedAction.selectTable")); //$NON-NLS-1$
                } else if (selection.size() > 1) {
                    JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("AbstractTableTargetedAction.multipleItemsSelected")); //$NON-NLS-1$
                } else if (selection.get(0) instanceof TablePane) {
                    TablePane tp = (TablePane) selection.get(0);
                    processTablePane(tp);
                } else {
                    JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("AbstractTableTargetedAction.selectedItemNotRecognized")); //$NON-NLS-1$
                }
            } else if (evt.getActionCommand().equals(DBTree.ACTION_COMMAND_SRC_DBTREE)) {
                TreePath [] selections = dbt.getSelectionPaths();
                // This statement ensures that there is only one item selected
                // except for the special case of SQLColumns, in which its
                // parent gets selected. Then, we need to use the column.
                if (selections != null && (selections.length == 1 || 
                                          (selections.length == 2 && selections[0].getLastPathComponent() instanceof SQLTable
                                                  && selections[1].getLastPathComponent() instanceof SQLColumn))) {
                    TreePath tp = selections[selections.length - 1];
                    SQLObject so = (SQLObject) tp.getLastPathComponent();
                    processSQLObject(so);
                } else {
                    JOptionPane.showMessageDialog(dbt, Messages.getString("AbstractTableTargetedAction.instructions")); //$NON-NLS-1$
                }
            } else {
                JOptionPane.showMessageDialog(
                        null, "InsertColumnAction: Unknown Action Command \"" +  //$NON-NLS-1$
                        evt.getActionCommand() + "\"", //$NON-NLS-1$
                        "Internal Architect Error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            }
        } catch (SQLObjectException ex) {
            ASUtils.showExceptionDialog(getSession(), Messages.getString("AbstractTableTargetedAction.columnCouldNotBeInserted") + ex.getMessage(), ex); //$NON-NLS-1$
        }
    }
    

    abstract void processTablePane(TablePane tp) throws SQLObjectException;
    abstract void processSQLObject(SQLObject so) throws SQLObjectException;

    
    public void setupAction(List selectedItems) {
        if (selectedItems.size() == 0) {
            disableAction();
        } else {
            Selectable item = (Selectable) selectedItems.get(0);
            if (item instanceof TablePane)              
                setEnabled(true);
        }
    }
    
    public abstract void disableAction();
        
    public void itemSelected(SelectionEvent e) {
        setupAction(getPlaypen().getSelectedItems());
        
    }

    public void itemDeselected(SelectionEvent e) {
        setupAction(getPlaypen().getSelectedItems());
    }
}
