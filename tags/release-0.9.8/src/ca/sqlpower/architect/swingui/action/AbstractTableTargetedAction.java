/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public abstract class AbstractTableTargetedAction extends AbstractArchitectAction implements SelectionListener {

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt; 
    
    public AbstractTableTargetedAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        super(session, actionName, actionDescription, iconResourceName);
        
        dbt = frame.getDbTree();
        if (dbt == null) throw new NullPointerException("Null db tree");
        
        playpen.addSelectionListener(this);
        
        setupAction(playpen.getSelectedItems());
    }
    
    public void actionPerformed(ActionEvent evt) {
        try {
            if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
                List selection = playpen.getSelectedItems();
                if (selection.size() < 1) {
                    JOptionPane.showMessageDialog(playpen, "Select a table (by clicking on it) and try again.");
                } else if (selection.size() > 1) {
                    JOptionPane.showMessageDialog(playpen, "You have selected multiple items, but you can only edit one at a time.");
                } else if (selection.get(0) instanceof TablePane) {
                    TablePane tp = (TablePane) selection.get(0);
                    processTablePane(tp);
                } else {
                    JOptionPane.showMessageDialog(playpen, "The selected item type is not recognised");
                }
            } else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
                TreePath [] selections = dbt.getSelectionPaths();
                if (selections == null || selections.length != 1) {
                    JOptionPane.showMessageDialog(dbt, "To indicate where you would like to insert a column, please select a single item.");
                } else {
                    TreePath tp = selections[0];
                    SQLObject so = (SQLObject) tp.getLastPathComponent();
                    processSQLObject(so);
                }
            } else {
                JOptionPane.showMessageDialog(
                        null, "InsertColumnAction: Unknown Action Command \"" + 
                        evt.getActionCommand() + "\"",
                        "Internal Architect Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ArchitectException ex) {
            ASUtils.showExceptionDialog(session, "Column could not be inserted:\n" + ex.getMessage(), ex);
        }
    }
    

    abstract void processTablePane(TablePane tp) throws ArchitectException;
    abstract void processSQLObject(SQLObject so) throws ArchitectException;

    
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
        setupAction(playpen.getSelectedItems());
        
    }

    public void itemDeselected(SelectionEvent e) {
        setupAction(playpen.getSelectedItems());
    }
}
