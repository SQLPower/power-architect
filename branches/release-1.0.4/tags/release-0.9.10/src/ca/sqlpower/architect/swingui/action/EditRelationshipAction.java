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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ColumnMappingPanel;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.RelationshipEditPanel;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TabbedDataEntryPanel;
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
		logger.debug("making edit relationship dialog");
		
        RelationshipEditPanel editPanel = new RelationshipEditPanel(session);
		editPanel.setRelationship(sqr);

        ColumnMappingPanel mappingPanel = new ColumnMappingPanel(session, sqr);
        
        TabbedDataEntryPanel panel = new TabbedDataEntryPanel();
        panel.addTab("Relationship", editPanel);
        panel.addTab("Mappings", mappingPanel);
        
		final JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
				panel,
				frame, 
				"Relationship Properties", "OK");
				
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
