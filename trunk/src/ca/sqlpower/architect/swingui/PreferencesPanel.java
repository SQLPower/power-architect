package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

public class PreferencesPanel extends JPanel implements ArchitectPanel {

	/**
	 * The settings we're editting
	 */
	protected UserSettings us;

	protected JTextField plIniName;
	protected JButton plIniButton;

	public PreferencesPanel(UserSettings us) {
		this.us = us;
		setup();
		revertToUserSettings();
	}

	public void setup() {
		setLayout(new FormLayout());
		add(new JLabel("Power*Loader PL.INI File"));
		JPanel plIniPanel = new JPanel();
		plIniPanel.setLayout(new BorderLayout());
		plIniPanel.add(plIniName = new JTextField("",30), BorderLayout.CENTER);
		plIniPanel.add(plIniButton = new JButton(), BorderLayout.EAST);
		plIniButton.setAction(new ChooseIniFileAction());
		add(plIniPanel);
	}

	protected void revertToUserSettings() {
		plIniName.setText(us.getETLUserSettings().getPlDotIniPath());
	}

	public void applyChanges() {
		us.getETLUserSettings().setPlDotIniPath(plIniName.getText());
	}

	public void discardChanges() {
		revertToUserSettings();
	}

	protected class ChooseIniFileAction extends AbstractAction {
		public ChooseIniFileAction() {
			super("Browse...");
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();

			if (plIniName.getText() != null && plIniName.getText().length() > 0) {
				File initialLocation = new File(plIniName.getText());
				if (initialLocation.exists()) {
					fc.setCurrentDirectory(initialLocation);
				}
			}
			
			fc.addChoosableFileFilter(ASUtils.INI_FILE_FILTER);
			int returnVal = fc.showOpenDialog(PreferencesPanel.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				plIniName.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
	}
}
