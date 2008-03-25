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
