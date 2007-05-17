/*
 * Created on Jun 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;

import javax.swing.JDialog;

/**
 * @author jack
 *
 * These are the methods that a Wizard needs to implement.
 * 
 * Ideally, there should be some sort of Abstract container which
 * already has cancel, back, and next buttons defined in it.
 */
public interface ArchitectWizard {	
	public WizardPanel getNext();
	public WizardPanel getPrevious();
	public WizardPanel getCurrent();	
	public boolean isOnLastPanel();
	public boolean isOnExecutePanel();
	public boolean isOnFirstPanel();
	public String getTitle();
	public JDialog getParentDialog();
	public void setParentDialog(JDialog parentDialog);
}
