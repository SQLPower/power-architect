package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.event.*;
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
		super("Edit Relationship");
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof Relationship) {
			Relationship r = (Relationship) invoker;
			
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
			d.setVisible(true);
			
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
