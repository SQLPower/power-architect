package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

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

	public void applyChanges() {
		proj.setSavingEntireSource(saveEntireSource.isSelected());
	}

	public void discardChanges() {
		revertToProjectSettings();
	}

}
