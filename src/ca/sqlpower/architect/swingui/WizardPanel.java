/*
 * Containing Panel for the Wizard Interface
 */
package ca.sqlpower.architect.swingui;
import javax.swing.JComponent;

/**
 * @author jack
 */
public interface WizardPanel extends ArchitectPanel {

	/**
	 * The NEXT or DONE button in a Wizard Panel's parent container 
	 * should invoke this method.  
	 */
	public boolean validate();

	/**
	 * returns this panel to the parent container which is managing this wizard
	 * 
	 * @return
	 */
	public JComponent getPanel();
	
	/**
	 * Make it easy for the parent Wizard to set its title based on what
	 * child wizard panel is being used.
	 * 
	 * @return
	 */
	public String getTitle();
	
	/**
	 * Should be invoked in the Panel constructor to get data from 
	 * the model into the view.
	 */
	public void loadForm();
	
}
