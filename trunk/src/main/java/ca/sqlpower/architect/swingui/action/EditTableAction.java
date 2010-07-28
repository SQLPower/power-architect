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
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.TableEditPanel;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.DataEntryPanelChangeUtil;

/**
 * This {@link Action} create a {@link JDialog} with an embedded
 * {@link TableEditPanel} inside of it. This dialog is specifically tied to the
 * {@link SQLTable} it is editing and its corresponding {@link TablePane}.
 */
public class EditTableAction extends AbstractArchitectAction {
    
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

    /**
     * This {@link SPListener} listens to changes for the {@link SQLTable} this
     * {@link Action} is tied to to update the UI it creates accordingly.
     */
    private final SPListener sqlTableListener = new AbstractSPListener() {
        
        /**
         * Checks to see if its respective table is removed from playpen. If yes,
         * exit the editing dialog window.
         */
        public void childRemoved(SPChildEvent evt) {
            logger.debug("SQLObject children got removed: " + evt); //$NON-NLS-1$
            if (tableEditPanel.getTable() == evt.getChild()) {
                evt.getChild().removeSPListener(this);
                tableEditPanel.getTablePane().removeSPListener(tablePaneListener);
                tableEditPanel = null;
                if (editDialog != null) {
                    editDialog.dispose();
                    editDialog = null;
                }
            }
        }

        /**
         * Checks to see if any of the {@link SQLTable}'s properties that the
         * {@link TableEditPanel} is interested in has changed.
         */
        public void propertyChanged(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            
            boolean foundError = false;
            
            if (evt.getSource() == tableEditPanel.getTable()) {
                if (property.equals("name")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getLogicalName(), evt);
                } else if (property.equals("physicalName")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getPhysicalName(), evt);
                } else if (property.equals("pkName")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getPkName(), evt);
                } else if (property.equals("remarks")) {   
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getRemarks(), evt);
                }
            }
            if (foundError) {
                tableEditPanel.setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
            }
        }
    };
    
    private SPListener tablePaneListener = new AbstractSPListener() {
        public void propertyChanged(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            
            boolean foundError = false;
            
            if (evt.getSource() == tableEditPanel.getTablePane()) {
                if (property.equals("backgroundColor")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getBgColor(), evt);
                } else if (property.equals("foregroundColor")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getFgColor(), evt);
                } else if (property.equals("rounded")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getRounded(), evt);
                } else if (property.equals("dashed")) {
                    foundError = DataEntryPanelChangeUtil.incomingChange(tableEditPanel.getDashed(), evt);
                }
            }
            if (foundError) {
                tableEditPanel.setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
            }
        }
    };
	
    /**
     * The {@link JDialog} this {@link Action} creates.
     */
	protected JDialog editDialog;

    /**
     * The {@link TableEditPanel} that is embedded inside the
     * {@link #editDialog}.
     */
	private TableEditPanel tableEditPanel;

    /**
     * Creates a new {@link EditTableAction}.
     * 
     * @param frame
     *            The {@link ArchitectFrame} this {@link Action} is tied to.
     */
	public EditTableAction(ArchitectFrame frame) {
	    super(frame, Messages.getString("EditTableAction.name"), Messages.getString("EditTableAction.description"), "edit_table"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(PlayPen.ACTION_COMMAND_SRC_PLAYPEN)) {
			List<PlayPenComponent> selection = getPlaypen().getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("EditTableAction.noTablesSelected")); //$NON-NLS-1$
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("EditTableAction.multipleTablesSelected")); //$NON-NLS-1$
			} else if (selection.get(0) instanceof TablePane) {
				TablePane tp = (TablePane) selection.get(0);
				makeDialog(tp.getModel());				
			} else {
				JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("EditTableAction.cannotRecognizeItem")); //$NON-NLS-1$
			}

		} else if (evt.getActionCommand().equals(DBTree.ACTION_COMMAND_SRC_DBTREE)) {
			TreePath [] selections = getSession().getDBTree().getSelectionPaths();
			logger.debug("selections length is: " + selections.length); //$NON-NLS-1$
			if (selections.length == 1 || selections.length == 2) {
                SQLObject so = (SQLObject) selections[0].getLastPathComponent();
                SQLTable st = null;
                
                if (so instanceof SQLColumn && selections.length == 2) {
                    so = (SQLObject) selections[1].getLastPathComponent();
                }

                if (so instanceof SQLTable) {
                    logger.debug("user clicked on table, so we shall try to edit the table properties."); //$NON-NLS-1$
                    st = (SQLTable) so;
                    makeDialog(st); 
                } else {
                    JOptionPane.showMessageDialog(frame, Messages.getString("EditTableAction.instructions")); //$NON-NLS-1$
                }
			} else {
			    JOptionPane.showMessageDialog(frame, Messages.getString("EditTableAction.instructions")); //$NON-NLS-1$
			}
		} else {
	  		// unknown action command source, do nothing
		}	
	}

    /**
     * Makes a {@link JDialog} with a {@link TableEditPanel} embedded inside of
     * it, and sets it to {@link #editDialog}.
     * 
     * @param table
     *            The {@link SQLTable} that is being edited.
     */
	protected void makeDialog(SQLTable table) {
	    DataEntryPanel panel = makeDataEntryPanel(table);
		editDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
		        panel, frame,
				Messages.getString("EditTableAction.dialogTitle"), 
				DataEntryPanelBuilder.OK_BUTTON_LABEL); //$NON-NLS-1$ //$NON-NLS-2$
		editDialog.pack();
		editDialog.setLocationRelativeTo(frame);
		editDialog.setVisible(true);
	}

    /**
     * Makes a {@link DataEntryPanel} that is or contains the
     * {@link TableEditPanel} that edits the {@link SQLTable} to embed inside of
     * the {@link #editDialog} {@link JDialog}.
     * 
     * @param table
     *            The {@link SQLTable} that is being edited.
     * @return The {@link DataEntryPanel}.
     */
	protected DataEntryPanel makeDataEntryPanel(SQLTable table) {
	    return makeTableEditPanel(table);
	}

    /**
     * Makes a {@link TableEditPanel} that edits a {@link SQLTable}. If there
     * are any incoming changes to the {@link SQLTable} or its corresponding
     * {@link TablePane}, an error will be flagged on the panel notifying the
     * user to close and reopen the dialog to prevent conflicting changes.
     * 
     * @param table
     *            The {@link SQLTable} that is being edited.
     * @return The {@link TableEditPanel}.
     */
	protected TableEditPanel makeTableEditPanel(SQLTable table) {
	    tableEditPanel = new TableEditPanel(getSession(), table);
	    table.addSPListener(sqlTableListener);
	    tableEditPanel.getTablePane().addSPListener(tablePaneListener);
	    return tableEditPanel;
	}
	
}
