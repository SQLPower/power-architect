/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.etl.PLExport;


/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
	
	
	public QuickStartPanel2 (QuickStartWizard wizard) {
		this.wizard = wizard;
		wizard.setPlExport(new PLExport());	
		
		targetConnectionsBox = new JComboBox();
		targetCatalog = new JComboBox();
		targetSchema = new JComboBox();
		progressBar = new JProgressBar();
		
		targetCatalog.setEnabled(false);
		targetSchema.setEnabled(false);
		
		dcl = new DatabaseComboBoxListener(
				(JPanel)getPanel(),
				targetConnectionsBox,
				targetCatalog,
				targetSchema,
				progressBar);
				
		
		targetConnectionsBox.addActionListener(dcl);
		
		WizardDialog.refreshTargetConnections(targetConnectionsBox,
								dataSourceRenderer );
	
		
		JButton newTargetButton= new JButton("New");
		newTargetButton.addActionListener(new NewDatabaseListener(
							ArchitectFrame.getMainInstance(),
							"New Target Database",
							targetConnectionsBox ));
		
		targetCatalog.addActionListener(new CatalogComboBoxListener((JPanel)getPanel(), 
					targetConnectionsBox, targetCatalog, targetSchema));		

		JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        targetPanel.add(targetConnectionsBox);
		targetPanel.add(newTargetButton);
		
		
		progressBar.setVisible(false);
		JComponent[] fields = new JComponent[] {new JLabel("<html>&nbsp;</html>"),
												new JLabel("<html>&nbsp;</html>"),
													targetPanel,
													targetCatalog,		
													targetSchema,
													new JLabel("<html>&nbsp;</html>"),
													new JLabel("<html>&nbsp;</html>"),
													progressBar
													};
		String[] labels = new String[] {"", "",
											"Target Connection",
											"Target Catalog",
											"Target Schema",
											"","",""
											}; 

		char[] mnemonics = new char[] {'z','z','t', 'o', 's', 'z', 'z', 'z'};
		int[] widths = new int[] {18,18, 18, 18, 18, 18, 18, 18 };
		String[] tips = new String[] {"","","Target Database Connection",
										  "Target Database Catalog",
					              		  "Target Database Schema/Owner",
										  "","",""
										  };
						
		panel = new JPanel();
		TextPanel mainForm = new TextPanel(fields, labels, mnemonics, widths, tips);
		
		panel.add(mainForm);
		
	}
	
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
	
	
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	
	
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

		plexp.setTargetCatalog( catalog );
		plexp.setTargetSchema( schema );
		return true;
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 2 of 5 - Select PL Repository and Target");
	}


}
