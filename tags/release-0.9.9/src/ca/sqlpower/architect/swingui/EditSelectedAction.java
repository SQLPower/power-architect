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

package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class EditSelectedAction extends AbstractArchitectAction implements SelectionListener {
    private ArchitectSwingSession session;
    private PlayPen playpen;
    private static final Logger logger = Logger.getLogger(EditSelectedAction.class);
    
    public EditSelectedAction(ArchitectSwingSession session) throws ArchitectException {
        super(session, "Edit Selected", "Edit Selected", "edit_column");
        
        this.session = session;
        playpen = session.getPlayPen();
        
        playpen.addSelectionListener(this);
        setupAction(playpen.getSelectedItems());
    }

    public void actionPerformed(ActionEvent e) {
        List selection = playpen.getSelectedItems();
        if (selection.size() == 0) {
            //no need to bug the user if they pressed enter
            return;
        }
        if (selection.size() < 1) {
            JOptionPane.showMessageDialog(playpen, "Please select an item");
        } else if (selection.size() > 1) {
            JOptionPane.showMessageDialog(playpen, "You have selected multiple items, but you can only edit one at a time.");
        } else if (selection.get(0) instanceof TablePane) {
            TablePane tp = (TablePane) selection.get(0);
            try {
                List<SQLColumn> selectedCols = tp.getSelectedColumns();
                if (selectedCols.size() == 0) {
                    //look for the relation ship action commands
                    session.getArchitectFrame().getEditTableAction().actionPerformed(e);
                } else if (selectedCols.size() == 1) {
                    session.getArchitectFrame().getEditColumnAction().actionPerformed(e);
                } else {
                    JOptionPane.showMessageDialog(playpen, "Please select one and only one column");
                    return;
                }
            } catch (ArchitectException ex) {
                JOptionPane.showMessageDialog(playpen, "Error opening editor");
            }
        } else if (selection.get(0) instanceof Relationship) {
            session.getArchitectFrame().getEditRelationshipAction().actionPerformed(e);
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
            Description = "Edit Selected";
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
            Description = "Edit "+name;
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
            Description = "Edit "+numSelectedItems+" items";
        }
        putValue(SHORT_DESCRIPTION, Description + " (Shortcut enter)");
    }

}
