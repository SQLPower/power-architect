/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.etl.PLExport;


/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuickStartPanel3 implements WizardPanel {

	private static final Logger logger = Logger.getLogger(WizardPanel.class);

	private QuickStartWizard wizard;
	public PLExportPanel plExportPanel;
	private JComboBox repositoryConnectionsBox;
	
	private JPanel panel;
	 
	private JComboBox repositoryCatalogComboBox;
	private JComboBox repositorySchemaComboBox;
	DatabaseComboBoxListener dcl;
	
	private JTextField plFolderName;
	private JTextField plJobId;	
	private JTextField plJobComment;
	private JTextField plJobDescription;
	
	private JCheckBox runPLEngine;
	private JProgressBar progressBar;
	
	private ListCellRenderer dataSourceRenderer = new DefaultListCellRenderer() {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			ArchitectDataSource ds = (ArchitectDataSource) value;
			String label;
			if (ds == null) {
				label = "(Choose a Connection)";
			} else {
				label = ds.getName();
			}
			return super.getListCellRendererComponent(list, label, index,
					isSelected, cellHasFocus);
		}
	};
	
	public QuickStartPanel3 (QuickStartWizard wizard) {
		this.wizard = wizard;

		FormLayout layout = new FormLayout("10dlu, 80dlu,10dlu, 5dlu,fill:100dlu:grow, 10dlu, 40dlu,30dlu", //Columns
			"15dlu, 16dlu, 2dlu, 16dlu, 2dlu, 16dlu, 20dlu," + //the repository connection rows
			"16dlu, 2dlu,16dlu,2dlu,16dlu,2dlu,16dlu,2dlu,16dlu,5dlu,16dlu, 2dlu" //the project description rows 
				);
		
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		
		panel = new JPanel();
		repositoryConnectionsBox = new JComboBox();
		repositoryCatalogComboBox = new JComboBox();
		repositorySchemaComboBox = new JComboBox();
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		
		
		
		dcl = new DatabaseComboBoxListener(
								(JPanel)getPanel(),
								repositoryConnectionsBox,
								repositoryCatalogComboBox,
								repositorySchemaComboBox,
								progressBar);
				
		
		repositoryConnectionsBox.addActionListener(dcl);
		
		WizardDialog.refreshTargetConnections(repositoryConnectionsBox,
								dataSourceRenderer );
		
		repositoryCatalogComboBox.addActionListener(new CatalogComboBoxListener(
					(JPanel)getPanel(),repositoryConnectionsBox, 
					repositoryCatalogComboBox, repositorySchemaComboBox));
		
		JButton newRepositoryButton= new JButton("New");
		
		newRepositoryButton.addActionListener(new NewDatabaseListener(
							ArchitectFrame.getMainInstance(),
							"New Repository Database",
							repositoryConnectionsBox ));
		
		plFolderName = new JTextField();
		plJobId = new JTextField();
		plJobDescription = new JTextField();
		plJobComment = new JTextField();
		runPLEngine = new JCheckBox("Run PL Engine?");
		runPLEngine.setEnabled(false);		
		
		
		pb.add(new JLabel("Repository Connection"), cc.xy(2,2,"r,c"));
		pb.add(repositoryConnectionsBox, cc.xyw(4,2,2));
		pb.add(newRepositoryButton, cc.xy(7,2));
		pb.add(new JLabel("Repository Catalog"), cc.xy(2,4, "r,c"));		
		pb.add(repositoryCatalogComboBox, cc.xyw(4,4,2));
		pb.add(new JLabel("Repository Schema"), cc.xy(2,6, "r,c"));		
		pb.add(repositorySchemaComboBox, cc.xyw(4,6,2));
		pb.add(new JLabel("PL FolderName"), cc.xy(2,8,"r,c"));
		pb.add(plFolderName, cc.xyw(4,8,4));
		pb.add(new JLabel("PL Job Id"), cc.xy(2,10,"r,c"));
		pb.add(plJobId, cc.xyw(4,10,4));
		pb.add(new JLabel("PL Job Description"), cc.xy(2,12,"r,c"));
		pb.add(plJobDescription, cc.xyw(4,12,4));
		pb.add(new JLabel("PL Job Comment"), cc.xy(2,14,"r,c"));
		pb.add(plJobComment, cc.xyw(4,14,4));
		pb.add(runPLEngine, cc.xyw(4,16,2));
		pb.add(progressBar, cc.xyw(4,18,4));
				
		panel.add(pb.getPanel());			
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		logger.debug("Applying target database changes to the PLExport object");
		PLExport plexp = wizard.getPlExport();	
		plexp.setTargetDataSource((ArchitectDataSource)repositoryConnectionsBox.getSelectedItem());
		
		SQLDatabase database = dcl.getDatabase();
		SQLCatalog catalog = (SQLCatalog) repositoryCatalogComboBox.getSelectedItem();
		SQLSchema schema = (SQLSchema) repositorySchemaComboBox.getSelectedItem();
		
		if ( database == null || 
			repositoryConnectionsBox.getSelectedItem() == null){
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

		if ( plJobId.getText().trim().length() == 0){
			JOptionPane.showMessageDialog(getPanel(),
										"You must enter a Job ID", 
										"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		plexp.setRepositoryDataSource((ArchitectDataSource) 
										repositoryConnectionsBox.getSelectedItem());
		plexp.setRepositoryCatalog( catalog );
		plexp.setRepositorySchema( schema );
		plexp.setJobComment(plJobComment.getText());
		plexp.setJobDescription(plJobDescription.getText());
		plexp.setFolderName(plFolderName.getText());
		plexp.setJobId(plJobId.getText());
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 3 of5 - Select PL Repository and Target");
	}
	
	public JComponent getPanel() {			
		return (JComponent) panel;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	


}

