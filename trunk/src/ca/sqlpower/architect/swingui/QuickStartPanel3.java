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
		
		JPanel repositoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        repositoryPanel.add(repositoryConnectionsBox);
		repositoryPanel.add(newRepositoryButton);

		JComponent[] fields = new JComponent[] {new JLabel("<html>&nbsp;</html>"),
												repositoryPanel,
												repositoryCatalogComboBox,
												repositorySchemaComboBox,												
												new JLabel("<html>&nbsp;</html>"),													
												plFolderName,
												plJobId,
												plJobDescription,
												plJobComment,
												runPLEngine, 
												progressBar};
		String[] labels = new String[] {"<html>&nbsp;</html>",
										"Repository Connection",
										"Repository Catalog",
										"Repository Schema",											
										"<html>&nbsp;</html>",
										"PL Folder Name",
										"PL Job Id",
										"PL Job Description",	
										"PL Job Comment",
										"<html>&nbsp;</html>",
										"<html>&nbsp;</html>"}; // run PL engine?
		char[] mnemonics = new char[] {'z', 'r','c', 'p', 'y', 'f', 'j','d','c','e','z'};
		int[] widths = new int[] {18, 18, 18, 18, 18, 18, 18,18,18,10,18};
		String[] tips = new String[] {"",
					              	"Repository Database Connection",
					              	"Repository Catalog/Owner",
					              	"Repository Schema/Owner",
					              	"",
					              	"The folder name for transactions",
					              	"The Job unique Id",
					              	"The Job Description",
					              	"Comment about the Job",
									"Run Power Loader Engine?",
									""};
		
		TextPanel mainPanel = new TextPanel(fields, labels, mnemonics, widths, tips);
		panel.add(mainPanel);			
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

