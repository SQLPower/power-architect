/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;
import javax.swing.*;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.etl.PLUtils;


/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuickStartPanel2 implements WizardPanel {

	private static final Logger logger = Logger.getLogger(WizardPanel.class);

	private QuickStartWizard wizard;
	public PLExportPanel plExportPanel;
	
	public QuickStartPanel2 (QuickStartWizard wizard) {
		this.wizard = wizard;
		wizard.setPlExport(new PLExport());		
		plExportPanel = new PLExportPanel();		
		if (wizard.getPlExport().getFolderName() == null || wizard.getPlExport().getFolderName().trim().length() == 0) {
			wizard.getPlExport().setFolderName(PLUtils.toPLIdentifier(ArchitectFrame.getMainInstance().getProject().getName()+"_FOLDER"));
		}
		if (wizard.getPlExport().getJobId() == null || wizard.getPlExport().getJobId().trim().length() == 0) {
			wizard.getPlExport().setJobId(PLUtils.toPLIdentifier(ArchitectFrame.getMainInstance().getProject().getName()+"_JOB"));
		}
		plExportPanel.setPLExport(wizard.getPlExport());		
	}
	
	public JComponent getPanel() {			
		return (JComponent) plExportPanel;
	}			
		
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		return plExportPanel.applyChanges();
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 2 of 3 - Select PL Repository and Target");
	}	
}
