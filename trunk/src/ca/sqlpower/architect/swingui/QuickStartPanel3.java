/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;
import java.awt.Component;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuickStartPanel3 implements WizardPanel {
	
	private static final Logger logger = Logger.getLogger(WizardPanel.class);

	private QuickStartWizard wizard;
	
	public QuickStartPanel3 (QuickStartWizard wizard) {
		this.wizard = wizard;
	}
	private Box box; // components laid out in here
		
	public JComponent getPanel() {
		if (box == null) {
			box = Box.createVerticalBox();	    	    
			box.add(Box.createVerticalStrut(50));

			// add some verbiage at the top
			JLabel verbiage = new JLabel();
			verbiage.setText("<html>Here is your summary.  Click Finish.");					
			verbiage.setAlignmentX(Component.CENTER_ALIGNMENT);
			box.add(verbiage);
			box.add(Box.createVerticalStrut(50));
			
		}
		return box;		
	}		
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 3 of 3 - Confirm Selections");
	}
}
