package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class EditRelationshipAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(EditRelationshipAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public EditRelationshipAction() {
		super("Relationship Properties",
			  ASUtils.createIcon("RelationshipProperties",
								 "Relationship Properties",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Relationship Properties");
	}

	public void actionPerformed(ActionEvent evt) {
		List selection = pp.getSelectedItems();
		if (selection.size() < 1) {
			JOptionPane.showMessageDialog(pp, "Select a relationship (by clicking on it) and try again.");
		} else if (selection.size() > 1) {
			JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
		} else if (selection.get(0) instanceof Relationship) {
			Relationship r = (Relationship) selection.get(0);
			
			final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
										  "Relationship Properties");
			JPanel cp = new JPanel(new BorderLayout(12,12));
			cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			final RelationshipEditPanel editPanel = new RelationshipEditPanel();
			editPanel.setRelationship(r.getModel());
			cp.add(editPanel, BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						editPanel.applyChanges();
						d.setVisible(false);
					}
				});
			buttonPanel.add(okButton);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						editPanel.discardChanges();
						d.setVisible(false);
					}
				});
			buttonPanel.add(cancelButton);
			
			cp.add(buttonPanel, BorderLayout.SOUTH);
			
			d.setContentPane(cp);
			d.pack();
			d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
			d.setVisible(true);
			
		} else {
			JOptionPane.showMessageDialog(pp, "The selected item type is not recognised");
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
