package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.RelationshipEditPanel;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class EditRelationshipAction extends AbstractAction implements SelectionListener {
	private static final Logger logger = Logger.getLogger(EditRelationshipAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected DBTree dbt; 

	
	public EditRelationshipAction() {
		super("Relationship Properties",
			  ASUtils.createIcon("edit_relationship",
								 "Relationship Properties",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, ArchitectFrame.DEFAULT_ICON_SIZE)));
		putValue(SHORT_DESCRIPTION, "Relationship Properties");
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = pp.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(pp, "Select a relationship (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof Relationship) {
				Relationship r = (Relationship) selection.get(0);
				makeDialog(r.getModel());
			} else {
				JOptionPane.showMessageDialog(pp, "Please select the relationship you would like to edit.");
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
		final RelationshipEditPanel editPanel = new RelationshipEditPanel();
		final JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				editPanel,
				ArchitectFrame.getMainInstance(), 
				"Relationship Properties", "OK");
		editPanel.setRelationship(sqr);
				
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}

	public void setPlayPen(PlayPen pp) {
		if (this.pp != null) {
			this.pp.removeSelectionListener(this);
		} 
		this.pp=pp;
		pp.addSelectionListener(this);
		
		setupAction(pp.getSelectedItems());
	}

	public void setDBTree(DBTree newDBT) {
		this.dbt = newDBT;
		// do I need to add a selection listener here?
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
		setupAction(pp.getSelectedItems());
		
	}

	public void itemDeselected(SelectionEvent e) {
		setupAction(pp.getSelectedItems());
	}
	
}
