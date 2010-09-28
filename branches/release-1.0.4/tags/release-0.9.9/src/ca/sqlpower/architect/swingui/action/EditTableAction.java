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
	    super(session, "Table Properties...", "Table Properties", "TableProperties");
        dbt = frame.getDbTree();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = playpen.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(playpen, "Select a table (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(playpen, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof TablePane) {
				TablePane tp = (TablePane) selection.get(0);
				makeDialog(tp.getModel());				
			} else {
				JOptionPane.showMessageDialog(playpen, "The selected item type is not recognised");
			}

		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
			TreePath [] selections = dbt.getSelectionPaths();
			logger.debug("selections length is: " + selections.length);
			if (selections.length != 1) {
				JOptionPane.showMessageDialog(dbt, "To indicate which table you like edit, please select a single table header.");
			} else {
				TreePath tp = selections[0];
				SQLObject so = (SQLObject) tp.getLastPathComponent();
				SQLTable st = null;

				if (so instanceof SQLTable) {
					logger.debug("user clicked on table, so we shall try to edit the table properties.");
					st = (SQLTable) so;
					makeDialog(st);	
				} else {
					JOptionPane.showMessageDialog(dbt, "To indicate which table you like edit, please select a single table header.");
				}
			}
		} else {
	  		// unknown action command source, do nothing
		}	
	}

	private JDialog d;
	
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

		d = DataEntryPanelBuilder.createDataEntryPanelDialog(
				editPanel, frame,
				"Table Properties", "OK", okCall, cancelCall);

		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
}
