package ca.sqlpower.architect.swingui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class QuickStartPanel5 implements WizardPanel {
	
	private static final Logger logger = Logger.getLogger(WizardPanel.class);

	private QuickStartWizard wizard;
	private JScrollPane sp;

	public QuickStartPanel5 (QuickStartWizard wizard) {
		this.wizard = wizard;
		wizard.getResultOutput().setText("Please wait....");
	}	
	private PanelBuilder pb;
	
	private JPanel panel; // components laid out in here
		
	public JComponent getPanel() {
		
		
		if ( panel == null ){
			logger.debug("getPanel: constructing JPanel");
			panel = new JPanel();
			FormLayout layout = new FormLayout("10dlu,min:grow,10dlu", 
					"20dlu,fill:100dlu:grow, 20dlu");						
			CellConstraints cc = new CellConstraints();
			pb = new PanelBuilder(layout, panel);
			pb.add(new JLabel ("Here is your summary, Click finish"), 
					cc.xy(2,1));
			sp = new JScrollPane(wizard.getResultOutput());						
			
			
			pb.add(sp, cc.xy(2,2));
			pb.add(new JLabel(""), cc.xy(2,3));											
		}
		
		return pb.getPanel();		
	}		
	
	

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		wizard.getParentDialog().setVisible(false);
		return true;
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 5 of 5 - Confirm Selections");
	}
	
	
	
}
