/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.etl.PLExport;

/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuickStartPanel4 implements WizardPanel {
	
	private String nextButtonName; 
	private static final Logger logger = Logger.getLogger(WizardPanel.class);

	private QuickStartWizard wizard;
	
	private JTextArea summaryOutput;
	private JScrollPane sp;
	
	public QuickStartPanel4 (QuickStartWizard wizard) {
		this.wizard = wizard;
	}	
	private PanelBuilder pb;
	
	private JPanel panel; // components laid out in here
		
	public JComponent getPanel() {
		
		
		if ( panel == null ){
			panel = new JPanel();
			FormLayout layout = new FormLayout("10dlu,min:grow,10dlu", 
					"20dlu,fill:100dlu:grow, 20dlu");						
			CellConstraints cc = new CellConstraints();
			pb = new PanelBuilder(layout, panel);
			pb.add(new JLabel ("Here is your summary, Click finish"), 
					cc.xy(2,1));
			summaryOutput = new JTextArea();
			sp = new JScrollPane(summaryOutput);						
			
			
			pb.add(sp, cc.xy(2,2));
			pb.add(new JLabel(""), cc.xy(2,3));											
		}
		setTextArea();
		
		return pb.getPanel();		
	}		
	
	
	public void setTextArea(){

		PLExport ple = wizard.getPlExport();
		
		StringBuffer text = new StringBuffer();
		text.append("These tables will be created in database: "+
				ple.getTargetDataSource().getName() +"\n");

		for (int ii =0; ii < wizard.getSourceTables().size(); ii++){			
			SQLTable t = (SQLTable) wizard.getSourceTables().get(ii);

			text.append( "  " +  (ii+1) + ".  " +  DDLUtils.toQualifiedName(
												ple.getTargetCatalog(),
												ple.getTargetSchema(),
												t.getName()) +"\n" );
		}
		
		text.append("\n");
		text.append("Job "+ ple.getJobId()+" will be created in database: "+
				ple.getRepositoryDataSource().getName() +"\n");
		summaryOutput.setText(text.toString());
		
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
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 4 of 5 - Confirm Selections");
	}
}
