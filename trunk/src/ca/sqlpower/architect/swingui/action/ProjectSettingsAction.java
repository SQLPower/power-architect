package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ProjectSettingsPanel;

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
		logger.debug(getValue(SHORT_DESCRIPTION) + " invoked");
		
		final ProjectSettingsPanel settingsPanel = new ProjectSettingsPanel(af.getProject());

		final JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				settingsPanel,
				ArchitectFrame.getMainInstance(),
				"Project Settings",
				ArchitectPanelBuilder.OK_BUTTON_LABEL );		

		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}

	public void setArchitectFrame(ArchitectFrame af) {
		this.af = af;
	}
}
