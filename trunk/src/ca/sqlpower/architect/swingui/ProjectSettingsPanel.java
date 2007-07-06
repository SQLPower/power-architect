package ca.sqlpower.architect.swingui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class ProjectSettingsPanel extends JPanel implements ArchitectPanel {
    private static final Logger logger = Logger.getLogger(ProjectSettingsPanel.class);
	/**
	 * The project whose settings we're editting.
	 */
	private SwingUIProject proj;
	
	private JCheckBox saveEntireSource;

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
        logger.debug("Reverting project options");
		saveEntireSource.setSelected(proj.isSavingEntireSource());
	}

	public boolean applyChanges() {
        logger.debug("Setting snapshot option to:"+saveEntireSource.isSelected());
		proj.setSavingEntireSource(saveEntireSource.isSelected());
        logger.debug("Project "+proj.getName() +" snapshot option is:"+proj.isSavingEntireSource());
		return true;
	}

	public void discardChanges() {
	
	}

	public JPanel getPanel() {
		return this;
	}

}
