package ca.sqlpower.architect.swingui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProjectSettingsPanel extends JPanel implements ArchitectPanel {

	/**
	 * The project whose settings we're editting.
	 */
	protected SwingUIProject proj;

	protected JCheckBox saveEntireSource;

	public ProjectSettingsPanel(SwingUIProject proj) {
		this.proj = proj;
		setup();
		revertToProjectSettings();
	}

	public void setup() {
		setLayout(new FormLayout());
		add(new JLabel("Snapshot entire source database in project file?"));
		add(saveEntireSource = new JCheckBox());
	}

	protected void revertToProjectSettings() {
		saveEntireSource.setSelected(proj.isSavingEntireSource());
	}

	public boolean applyChanges() {
		proj.setSavingEntireSource(saveEntireSource.isSelected());
		return true;
	}

	public void discardChanges() {
		revertToProjectSettings();
	}

	public JPanel getPanel() {
		return this;
	}

}
