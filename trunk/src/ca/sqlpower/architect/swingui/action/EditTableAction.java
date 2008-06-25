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
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.TableEditPanel;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditTableAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected final DBTree dbt; 
	
	public EditTableAction(ArchitectSwingSession session) {
	    super(session, Messages.getString("EditTableAction.name"), Messages.getString("EditTableAction.description"), "edit_table"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        dbt = frame.getDbTree();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = playpen.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(playpen, Messages.getString("EditTableAction.noTablesSelected")); //$NON-NLS-1$
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(playpen, Messages.getString("EditTableAction.multipleTablesSelected")); //$NON-NLS-1$
			} else if (selection.get(0) instanceof TablePane) {
				TablePane tp = (TablePane) selection.get(0);
				makeDialog(tp.getModel());				
			} else {
				JOptionPane.showMessageDialog(playpen, Messages.getString("EditTableAction.cannotRecognizeItem")); //$NON-NLS-1$
			}

		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
			TreePath [] selections = dbt.getSelectionPaths();
			logger.debug("selections length is: " + selections.length); //$NON-NLS-1$
			if (selections.length != 1) {
				JOptionPane.showMessageDialog(dbt, Messages.getString("EditTableAction.instructions")); //$NON-NLS-1$
			} else {
				TreePath tp = selections[0];
				SQLObject so = (SQLObject) tp.getLastPathComponent();
				SQLTable st = null;

				if (so instanceof SQLTable) {
					logger.debug("user clicked on table, so we shall try to edit the table properties."); //$NON-NLS-1$
					st = (SQLTable) so;
					makeDialog(st);	
				} else {
					JOptionPane.showMessageDialog(dbt, Messages.getString("EditTableAction.instructions")); //$NON-NLS-1$
				}
			}
		} else {
	  		// unknown action command source, do nothing
		}	
	}

	private JDialog editDialog;
	
	public void makeDialog(SQLTable table) {
		final TableEditPanel editPanel = new TableEditPanel(session, table);

		Callable<Boolean> okCall = new Callable<Boolean>() {
			public Boolean call() {
				//We need to see if the operation is successful, if
                //successful, we close down the dialog, if not, we need 
                //to return the dialog (hence why it is setVisible(!success))
                return new Boolean(editPanel.applyChanges());
				// XXX: also apply changes on mapping tab                
			}
		};

		Callable<Boolean> cancelCall = new Callable<Boolean>() {
			public Boolean call() {
				editPanel.discardChanges();
				// XXX: also discard changes on mapping tab
				return new Boolean(true);
			}
		};

		editDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
				editPanel, frame,
				Messages.getString("EditTableAction.dialogTitle"), Messages.getString("EditTableAction.okOption"), okCall, cancelCall); //$NON-NLS-1$ //$NON-NLS-2$
		editPanel.setEditDialog(editDialog);
		editDialog.pack();
		editDialog.setLocationRelativeTo(frame);
		editDialog.setVisible(true);
	}
}
