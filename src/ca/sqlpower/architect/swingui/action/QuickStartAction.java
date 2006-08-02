package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.QuickStartWizard;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.WizardDialog;

public class QuickStartAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(QuickStartAction.class);
	
	protected JDialog d;

	public QuickStartAction() {
		super("Quick Start Wizard...",
			  ASUtils.createIcon("PLTransExport",
								 "PL Export Wizard",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Quick Start Wizard");
	}

	public void actionPerformed(ActionEvent e) {
		logger.debug(getValue(SHORT_DESCRIPTION) + ": started");
		
		ArchitectFrame.getMainInstance().getNewProjectAction().actionPerformed(new ActionEvent(this,0,null));
		// always start from scratch
		d = new WizardDialog(ArchitectFrame.getMainInstance(),new QuickStartWizard());		
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true); 
	}
}
