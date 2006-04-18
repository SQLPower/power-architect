/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;
import java.awt.Component;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.swingui.event.CatalogComboBoxListener;
import ca.sqlpower.architect.swingui.event.DatabaseComboBoxListener;
import ca.sqlpower.architect.swingui.event.NewDatabaseListener;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.PLSecurityManager;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.util.UnknownFreqCodeException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


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
	
	private ListCellRenderer dataSourceRenderer = new DataSourceRenderer();

	private JLabel label;
	
	public QuickStartPanel3 (QuickStartWizard wizard) {
		this.wizard = wizard;

		FormLayout layout = new FormLayout("10dlu, 80dlu,10dlu, 5dlu,fill:100dlu:grow, 10dlu, 40dlu,30dlu", //Columns
			"15dlu, 16dlu, 2dlu, 16dlu, 2dlu, 16dlu, 20dlu," + //the repository connection rows
			"16dlu, 2dlu,16dlu,2dlu,16dlu,2dlu,16dlu,2dlu,16dlu,5dlu,16dlu, 2dlu" //the project description rows 
				);
		
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		
		panel = new JPanel();
		repositoryConnectionsBox = new JComboBox(new ConnectionComboBoxModel());
		repositoryConnectionsBox.setRenderer(dataSourceRenderer);
		repositoryCatalogComboBox = new JComboBox();
		repositorySchemaComboBox = new JComboBox();
		progressBar = ((WizardDialog)wizard.getParentDialog()).getProgressBar();
		label = ((WizardDialog)wizard.getParentDialog()).getProgressLabel();
		label.setText("Loading Database.....");
		
		dcl = new DatabaseComboBoxListener(
								(JPanel)getPanel(),
								repositoryConnectionsBox,
								repositoryCatalogComboBox,
								repositorySchemaComboBox,
								progressBar );
		
		ArrayList l1 = new ArrayList();
		l1.add(label);
		dcl.setVisableInvisableList(l1);
		
		ArrayList l2 = new ArrayList();
		l2.add(((WizardDialog)wizard.getParentDialog()).getNextButton() );
		l2.add(((WizardDialog)wizard.getParentDialog()).getBackButton() );
		dcl.setDisableEnableList(l2);
		
		repositoryConnectionsBox.addActionListener(dcl);
		
			
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
		
				
		panel.add(pb.getPanel());			
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		logger.debug("Applying repository database changes to the PLExport object");
		PLExport plexp = wizard.getPlExport();	
		
		SQLDatabase database = dcl.getDatabase();
		SQLCatalog catalog = (SQLCatalog) repositoryCatalogComboBox.getSelectedItem();
		SQLSchema schema = (SQLSchema) repositorySchemaComboBox.getSelectedItem();
		
		if ( database == null || 
			repositoryConnectionsBox.getSelectedItem() == null){
			JOptionPane.showMessageDialog(getPanel(),
					"No Repository Database is selected, cannot continue.",
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		 try {
			Connection con = database.getConnection();
			
			 if (con == null) {
		    	JOptionPane.showMessageDialog(getPanel(),
						"Couldn't connect to repository database.",
						"Repository Database Connection Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			 
			PLSecurityManager sm = null;
			sm = new PLSecurityManager(con, 
		                database.getDataSource().get(ArchitectDataSource.PL_UID),
		                database.getDataSource().get(ArchitectDataSource.PL_PWD),
		                false);
			 
		    if (sm == null) {
		    	JOptionPane.showMessageDialog(getPanel(),
						"Couldn't login to repository database.",
						"Repository Database Login Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		    
		    StringBuffer sql = new StringBuffer("SELECT SCHEMA_VERSION FROM ");
			sql.append( DDLUtils.toQualifiedName(catalog==null?null:catalog.getName(),
											schema==null?null:schema.getName(),
											"DEF_PARAM"));
			Statement s = con.createStatement();
			ResultSet rs = null;
			Set set = new HashSet();
			try {
				logger.debug("TRY TO VERTIFY PL SCHEMA EXISTENT: " + sql.toString());
				rs = s.executeQuery(sql.toString());
				while (rs.next()) {
					set.add(rs.getString(1));
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(getPanel(),
						"This repository database.catalog.schema ["+
						database.getName()+
						(catalog==null?"":"."+catalog.getName())+
						(schema==null?"":"."+schema.getName())+
						"] Does not contain Power Loader dictionary tables",
						"Repository Database Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} finally {
				if (rs != null) {
					rs.close();				
				}
				if (s != null) {
					s.close();
				}
			}
			
			if ( set.size() == 0 ) {
				JOptionPane.showMessageDialog(getPanel(),
						"This Invalid Power Loader Schema",
						"Repository Database Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			
			sql = new StringBuffer("SELECT JOB_ID FROM ");
			sql.append( DDLUtils.toQualifiedName(catalog==null?null:catalog.getName(),
											schema==null?null:schema.getName(),
											"PL_JOB"));
			sql.append(" WHERE JOB_ID=");
			sql.append( SQL.quote(plJobId.getText().toUpperCase()));
			s = con.createStatement();
			rs = null;
			set = new HashSet();
			
			try {
				logger.debug("TRY TO CHECK DUPLICATE JOB_ID: " + sql.toString());
				rs = s.executeQuery(sql.toString());
				while (rs.next()) {
					set.add(rs.getString(1));
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(getPanel(),
						"This repository database.catalog.schema ["+
						database.getName()+
						(catalog==null?"":"."+catalog.getName())+
						(schema==null?"":"."+schema.getName())+
						"] Does not contain Power Loader dictionary tables",
						"Repository Database Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} finally {
				if (rs != null) {
					rs.close();				
				}
				if (s != null) {
					s.close();
				}
			}
			
			if ( set.size() != 0 ) {
				JOptionPane.showMessageDialog(getPanel(),
						"Duplicate Job Id " + plJobId.getText(),
						"Metadata Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			
	    } catch (PLSecurityException se) {
	    	JOptionPane.showMessageDialog(getPanel(),
					"Couldn't connect to repository database, "+se.getMessage(),
					"Repository Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
	    } catch (UnknownFreqCodeException e) {
	    	JOptionPane.showMessageDialog(getPanel(),
					"Couldn't connect to repository database, Frequence Code Setup error:"+
					e.getMessage(),
					"Repository Database Error", JOptionPane.ERROR_MESSAGE);
	    	return false;
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(getPanel(),
					"Couldn't connect to repository database, SQL ERROR:"+e.getMessage(),
					"Repository Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (ArchitectException e) {
			JOptionPane.showMessageDialog(getPanel(),
					e.getMessage(),
					"Repository Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		
		
		try {
			if ( database.isCatalogContainer() && database.getChildCount() > 0 && catalog == null )
				return false;
			
			if ( database.isSchemaContainer() && database.getChildCount() > 0 && schema == null )
				return false;
			
			if ( catalog != null && catalog.getChildCount() > 0 && catalog.isSchemaContainer() && schema == null )
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
		return ("Architect Quick Start - Step 3 of 5 - Select PL Repository");
	}
	
	public JComponent getPanel() {			
		return (JComponent) panel;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	


}

