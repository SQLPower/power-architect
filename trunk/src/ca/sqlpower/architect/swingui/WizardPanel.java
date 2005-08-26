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
		
}
