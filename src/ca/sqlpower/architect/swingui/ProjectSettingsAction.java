package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class ProjectSettingsAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	/**
	 * The ArchitectFrame instance that owns this Action.
	 */
	protected ArchitectFrame af;
	
	public ProjectSettingsAction() {
		super("Project Settings...");
		putValue(SHORT_DESCRIPTION, "Project Settings");
	}

	public void actionPerformed(ActionEvent evt) {
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Project Settings");
		
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		final ProjectSettingsPanel settingsPanel = new ProjectSettingsPanel(af.getProject());
		cp.add(settingsPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					settingsPanel.applyChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					settingsPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		
		cp.add(buttonPanel, BorderLayout.SOUTH);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}

	public void setArchitectFrame(ArchitectFrame af) {
		this.af = af;
	}
}
