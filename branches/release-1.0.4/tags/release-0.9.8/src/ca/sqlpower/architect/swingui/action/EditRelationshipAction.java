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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.RelationshipEditPanel;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditRelationshipAction extends AbstractArchitectAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(EditRelationshipAction.class);

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected final DBTree dbt; 
	
	public EditRelationshipAction(ArchitectSwingSession session) {
		super(session, "Relationship Properties", "Relationship Properties", "edit_relationship");
		setEnabled(false);
        playpen.addSelectionListener(this);
        setupAction(playpen.getSelectedItems());
        dbt = frame.getDbTree();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = playpen.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(playpen, "Select a relationship (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(playpen, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof Relationship) {
				Relationship r = (Relationship) selection.get(0);
				makeDialog(r.getModel());
			} else {
				JOptionPane.showMessageDialog(playpen, "Please select the relationship you would like to edit.");
			}
		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
			TreePath [] selections = dbt.getSelectionPaths();
			if (selections.length != 1) {
				JOptionPane.showMessageDialog(dbt, "Please select the relationship you would like to edit.");
			} else {
				TreePath tp = selections[0];
				SQLObject so = (SQLObject) tp.getLastPathComponent();
				if (so instanceof SQLRelationship) {
					SQLRelationship sr = (SQLRelationship) so;
					makeDialog(sr);
				} else {
					JOptionPane.showMessageDialog(dbt, "Please select the relationship you would like to edit.");
				}
			}
		} else {
			// unrecognized action source, do nothing...
		}							
	}

	private void makeDialog(SQLRelationship sqr) {
		logger.debug ("making edit relationship dialog");
		final RelationshipEditPanel editPanel = new RelationshipEditPanel(session);
		final JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
				editPanel,
				frame, 
				"Relationship Properties", "OK");
		editPanel.setRelationship(sqr);
				
		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}

	public void setupAction(List selectedItems) {
		if (selectedItems.size() == 0) {
			setEnabled(false);
			logger.debug("Disabling edit relationship");
			putValue(SHORT_DESCRIPTION, "Edit Relationship");
		} else {
			Selectable item = (Selectable) selectedItems.get(0);
			if (item instanceof Relationship )				
				setEnabled(true);
		}
	}
		
	public void itemSelected(SelectionEvent e) {
		setupAction(playpen.getSelectedItems());
		
	}

	public void itemDeselected(SelectionEvent e) {
		setupAction(playpen.getSelectedItems());
	}
	
}
