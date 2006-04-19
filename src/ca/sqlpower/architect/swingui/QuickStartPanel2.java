package ca.sqlpower.architect.swingui;

import java.sql.Connection;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.swingui.event.CatalogComboBoxListener;
import ca.sqlpower.architect.swingui.event.DatabaseComboBoxListener;
import ca.sqlpower.architect.swingui.event.NewDatabaseListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class QuickStartPanel2 implements WizardPanel {

	private static final Logger logger = Logger.getLogger(QuickStartPanel2.class);

	private QuickStartWizard wizard;
	private JPanel panel;
	
	private JComboBox targetConnectionsBox;
	private JComboBox targetCatalog;
	private JComboBox targetSchema;
	private JProgressBar progressBar;
	
	private SQLDatabase database;
	private SQLCatalog catalog;
	private SQLSchema schema;
	DatabaseComboBoxListener dcl;

	private JLabel label;
	
	public QuickStartPanel2 (QuickStartWizard wizard) {
		this.wizard = wizard;
		wizard.setPlExport(new PLExport());				
		
		targetConnectionsBox = new JComboBox();
		targetConnectionsBox.setModel( new ConnectionComboBoxModel());
		targetConnectionsBox.setRenderer(dataSourceRenderer);
		
		targetCatalog = new JComboBox();
		targetSchema = new JComboBox();
		progressBar = ((WizardDialog)wizard.getParentDialog()).getProgressBar();
		label = ((WizardDialog)wizard.getParentDialog()).getProgressLabel();
		label.setText("Loading Database.....");
		
		targetCatalog.setEnabled(false);
		targetSchema.setEnabled(false);
		
		dcl = new DatabaseComboBoxListener(
				(JPanel)getPanel(),
				targetConnectionsBox,
				targetCatalog,
				targetSchema,
				progressBar);
		ArrayList l1 = new ArrayList();
		l1.add(label);
		dcl.setVisableInvisableList(l1);
		
		ArrayList l2 = new ArrayList();
		l2.add(((WizardDialog)wizard.getParentDialog()).getNextButton() );
		l2.add(((WizardDialog)wizard.getParentDialog()).getBackButton() );
		dcl.setDisableEnableList(l2);
		
		targetConnectionsBox.addActionListener(dcl);
		
			
		JButton newTargetButton= new JButton(" New ");
		newTargetButton.addActionListener(new NewDatabaseListener(
							ArchitectFrame.getMainInstance(),
							"New Target Database",
							targetConnectionsBox ));
		
		targetCatalog.addActionListener(new CatalogComboBoxListener((JPanel)getPanel(), 
					targetConnectionsBox, targetCatalog, targetSchema));		

		
		FormLayout layout = new FormLayout("10dlu, 80dlu,10dlu, 5dlu,fill:100dlu:grow, 10dlu, 40dlu,30dlu", //Columns
										"30dlu, 20dlu, 4dlu, 20dlu, 4dlu, 20dlu, 30dlu, 20dlu");
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();		
		pb.add(new JLabel("Target Connection"), cc.xy(2,2,"r,c"));
		pb.add(targetConnectionsBox, cc.xyw(4,2,2));
		pb.add(newTargetButton, cc.xy(7,2));
		pb.add(new JLabel("Target Catalog"), cc.xy(2,4, "r,c"));		
		pb.add(targetCatalog, cc.xyw(4,4,2));
		pb.add(new JLabel("Target Schema"), cc.xy(2,6, "r,c"));		
		pb.add(targetSchema, cc.xyw(4,6,2));
				

		panel = new JPanel();
		panel.add(pb.getPanel());
		
	}
	
	private ListCellRenderer dataSourceRenderer = new DataSourceRenderer();
	
	
	
	public JComponent getPanel() {			
		return (JComponent) panel;
	}			
		
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		logger.debug("Applying target database changes to the PLExport object");
		PLExport plexp = wizard.getPlExport();	
		plexp.setTargetDataSource((ArchitectDataSource)targetConnectionsBox.getSelectedItem());
		database = dcl.getDatabase();
		
		
		 try {
			Connection con = database.getConnection();
		 
		    if (con == null) {
		    	JOptionPane.showMessageDialog(getPanel(),
						"Couldn't connect to target database.",
						"Database Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (ArchitectException e) {
			JOptionPane.showMessageDialog(getPanel(),
					e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		
		
		
		catalog = (SQLCatalog) targetCatalog.getSelectedItem();
		schema = (SQLSchema) targetSchema.getSelectedItem();
		
		if ( database == null || 
			targetConnectionsBox.getSelectedItem() == null){
			JOptionPane.showMessageDialog(getPanel(),
					"No Database is selected, cannot continue.",
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		try {
			if ( database.isCatalogContainer() && catalog == null )
				return false;
			
			if ( database.isSchemaContainer() && schema == null )
				return false;
			
			if ( catalog != null && catalog.isSchemaContainer() && schema == null )
				return false;
			
		} catch (ArchitectException e) {
			JOptionPane.showMessageDialog(getPanel(),
					"Database populate Erorr",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		plexp.setTargetDataSource(database.getDataSource());
		plexp.setTargetCatalog( catalog );
		plexp.setTargetSchema( schema );
		
		wizard.addTargetDatabase(database.getDataSource());
		
		return true;
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 2 of 5 - Select Target");
	}


}
