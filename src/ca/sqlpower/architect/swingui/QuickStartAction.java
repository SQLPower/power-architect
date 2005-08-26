package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

public class QuickStartAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(QuickStartAction.class);
	
	protected JDialog d;

	public QuickStartAction() {
		super("Quick Start Wizard...",
			  ASUtils.createIcon("PLTransExport",
								 "PL Export Wizard",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Quick Start Wizard");
	}

	public void actionPerformed(ActionEvent e) {
		ArchitectFrame.getMainInstance().newProjectAction.actionPerformed(new ActionEvent(this,0,null));
		// always start from scratch
		d = new WizardDialog(ArchitectFrame.getMainInstance(),new QuickStartWizard());		
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true); 
	}
}
