package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ProjectSettingsPanel;

public class ProjectSettingsAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	public ProjectSettingsAction(ArchitectSwingSession session) {
        super(session, "Project Settings...", "Project Settings");
	}

	public void actionPerformed(ActionEvent evt) {
		logger.debug(getValue(SHORT_DESCRIPTION) + " invoked");
		
		final ProjectSettingsPanel settingsPanel = new ProjectSettingsPanel(session);

		final JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				settingsPanel,
				frame,
				"Project Settings",
				ArchitectPanelBuilder.OK_BUTTON_LABEL );		

		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
}
