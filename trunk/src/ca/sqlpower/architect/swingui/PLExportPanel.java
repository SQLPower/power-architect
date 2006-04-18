package ca.sqlpower.architect.swingui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.etl.PLUtils;
import ca.sqlpower.architect.swingui.event.NewDatabaseListener;
import ca.sqlpower.sql.SQL;

public class PLExportPanel extends JPanel implements ArchitectPanel {

    /**
     * The EditRepositoryListener reacts to presses of the editRepository button.
     */
    public class EditRepositoryListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * The EditTargetListener reacts to presses of the editTarget button.
     */
    public class EditTargetListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
        }
    }

    private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	/**
	 * This is the PLExport whose properties this panel edits.
	 */
	protected PLExport plexp;

	protected JComboBox targetConnectionsBox;
	protected JTextField targetSchema;
	protected JTextField targetCatalog;
	protected JComboBox repositoryConnectionsBox;
	protected JTextField repositorySchema;
	protected JTextField plFolderName;
	protected JTextField plJobId;
	protected JTextField plJobDescription;
	protected JTextField plJobComment;

	private JButton newTargetButton;
	// private JButton editTargetButton;
	private JButton newRepositoryButton;
	// private JButton editRepositoryButton;
	private JCheckBox runPLEngine;

	// Watch PL.INI for changes
	protected javax.swing.Timer timer;
	protected String plDotIniPath;
	
	protected Map ddlGeneratorMap;
	protected TextPanel mainForm;
	
	public PLExportPanel() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		plDotIniPath = af.getUserSettings().getPlDotIniPath(); // is this bad?
		ddlGeneratorMap = ArchitectUtils.getDriverDDLGeneratorMap();
		
		targetConnectionsBox = new JComboBox(new ConnectionComboBoxModel());
		targetConnectionsBox.setRenderer(new DataSourceRenderer());
		targetConnectionsBox.addActionListener(new TargetListener());
		newTargetButton= new JButton("New");

		newTargetButton.addActionListener(
				new NewDatabaseListener(ArchitectFrame.getMainInstance(),
								"New Target Database",
								targetConnectionsBox));
		
		targetCatalog = new JTextField();
		targetSchema = new JTextField();

		// 
		repositoryConnectionsBox = new JComboBox(new ConnectionComboBoxModel());
		repositoryConnectionsBox.setRenderer(new DataSourceRenderer());
		repositoryConnectionsBox.addActionListener(new RepositoryListener());
		newRepositoryButton= new JButton("New");
		newRepositoryButton.addActionListener(new NewDatabaseListener(
						ArchitectFrame.getMainInstance(),
						"New Repository Connection",
						repositoryConnectionsBox));
		repositorySchema = new JTextField();

		//
		plFolderName = new JTextField();
		plJobId = new JTextField();
		plJobDescription = new JTextField();
		plJobComment = new JTextField();
		runPLEngine = new JCheckBox("Run PL Engine?");
		runPLEngine.setEnabled(false);

		JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        targetPanel.add(targetConnectionsBox);
		targetPanel.add(newTargetButton);

		JPanel repositoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        repositoryPanel.add(repositoryConnectionsBox);
		repositoryPanel.add(newRepositoryButton);
		
		JComponent[] fields = new JComponent[] {targetPanel,
													targetCatalog,		
													targetSchema,
													new JLabel("<html>&nbsp;</html>"),													
													repositoryPanel,
													repositorySchema,
													new JLabel("<html>&nbsp;</html>"),													
													plFolderName,
													plJobId,
													plJobDescription,
													plJobComment,
													runPLEngine };
		String[] labels = new String[] {"Target Connection",
											"Target Catalog",
											"Target Schema",
											"<html>&nbsp;</html>",
											"Repository Connection",
											"Repository Schema",											
											"<html>&nbsp;</html>",
											"PL Folder Name",
											"PL Job Id",
											"PL Job Description",
											"PL Job Comment",
											"<html>&nbsp;</html>"}; // run PL engine?

		char[] mnemonics = new char[] {'t', 'o', 's', 'z', 'r', 'p', 'y', 'f', 'j','d','c','e'};
		int[] widths = new int[] {18, 18, 18, 18, 18, 18, 18, 18, 18,18,18,10};
		String[] tips = new String[] {"Target Database Connection",
										  "Target Database Catalog",
					              		  "Target Database Schema/Owner",
										  "",
					              		  "Repository Database Connection",
										  "Repository Schema/Owner",
										  "",
										  "The folder name for transactions",
										  "The Job unique Id",
										  "The Job Description",
										  "Comment about the Job",
										  "Run Power Loader Engine?"};

		mainForm = new TextPanel(fields, labels, mnemonics, widths, tips);
		
		add(mainForm);
		

		/* This is messing things up, so take it out for now
		// new: add a swing timer to watch the PL.INI file and reload the database connections if
        // it notices any changes...
        timer = new javax.swing.Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
				if (PLUtils.plDotIniHasChanged(plDotIniPath)) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshTargetConnections();			
							refreshRepositoryConnections();			
						}
					});
				}
            }
        });	
		timer.start();
		*/
	}
	

	/**
	 * Sets a new PLExport object for this panel to edit.  All field
	 * values will be updated to reflect the status of the given
	 * PLExport object.
	 */
	public void setPLExport(PLExport plexp) {
		this.plexp = plexp;
		targetSchema.setText(plexp.getTargetSchema());
		targetConnectionsBox.setSelectedItem(plexp.getTargetDataSource());
		repositoryConnectionsBox.setSelectedItem(plexp.getRepositoryDataSource());
		plFolderName.setText(plexp.getFolderName());
		plJobId.setText(plexp.getJobId());
		plJobDescription.setText(plexp.getJobDescription());
		plJobComment.setText(plexp.getJobComment());		
	}
	
	/**
	 * Returns the PLExport object that this panel is editting.  Call
	 * applyChanges() to update it to the current values displayed on
	 * the panel.
	 */
	public PLExport getPLExport() {
		return plexp;
	}

	public class RepositoryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			logger.debug("event was fired");
			ArchitectDataSource dataSource = (ArchitectDataSource) repositoryConnectionsBox.getSelectedItem();
			if (dataSource == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				repositorySchema.setText(null);
   		    } else {
				runPLEngine.setEnabled(true);
				repositorySchema.setText(dataSource.get(ArchitectDataSource.PL_SCHEMA_OWNER));
			}
		}
	}
	
    /**
     * The TargetListener reacts to presses of the target button.
     */
    public class TargetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
			logger.debug("event was fired");
			ArchitectDataSource dataSource = (ArchitectDataSource) targetConnectionsBox.getSelectedItem();
			if (dataSource == null) {
				repositorySchema.setText(null);
   		    } else {
				targetSchema.setText(dataSource.get(ArchitectDataSource.PL_SCHEMA_OWNER));
			}
			
			// XXX: need to make labels an associative array so we're not
			// referring to an indexed property that could change its
			// index in the future:
			ArchitectDataSource ds = (ArchitectDataSource) targetConnectionsBox.getSelectedItem();
			
			JLabel catalogLabel = (JLabel) mainForm.getLabel(1);
			JTextField catalogField = (JTextField) mainForm.getField(1);
			JLabel schemaLabel = (JLabel) mainForm.getLabel(2);
			JTextField schemaField = (JTextField) mainForm.getField(2);
			
			boolean allIsWell = false;
			try {				
				if (ds != null) {
					Class selectedGeneratorClass = (Class) ddlGeneratorMap.get(ds.getDriverClass());
					if (selectedGeneratorClass != null) {
						GenericDDLGenerator newGen = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
						if (newGen != null) {
							allIsWell = true;
							if (newGen.getCatalogTerm() != null) {
								catalogLabel.setText(newGen.getCatalogTerm());
								catalogLabel.setEnabled(true);
								catalogField.setEnabled(true);
							} else {
								catalogLabel.setText("(no catalog)");
								catalogLabel.setEnabled(false);
								catalogField.setText(null);
								catalogField.setEnabled(false);
							}
							if (newGen.getSchemaTerm() != null) {
								schemaLabel.setText(newGen.getSchemaTerm());
								schemaLabel.setEnabled(true);
								schemaField.setEnabled(true);
							} else {
								schemaLabel.setText("(no schema)");
								schemaLabel.setEnabled(false);
								schemaField.setText(null);
								schemaField.setEnabled(false);
							}						
						}
					}
				}
			} catch (Exception ex) {				
				logger.error("Exception thrown when enabling/disabling target schema and catalog fields.", ex);
				allIsWell = false;
			}
			
			if (!allIsWell) {
				// disable the fields 
				catalogLabel.setText("(no catalog)");
				catalogLabel.setEnabled(false);
				catalogField.setText(null);
				catalogField.setEnabled(false);
				schemaLabel.setText("(no schema)");
				schemaLabel.setEnabled(false);
				schemaField.setText(null);
				schemaField.setEnabled(false);				
			}
        }
    }	
	
	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Converts the fields that contain PL Identifiers into the
	 * correct format (using PLUtils.toPLIdentifier) and then sets all
	 * the properties of plexp to their values in this panel's input
	 * fields.
	 */
	public boolean applyChanges() {
	    logger.debug("Applying changes to the PLExport object");
		plexp.setTargetDataSource((ArchitectDataSource)targetConnectionsBox.getSelectedItem());
		plexp.setRepositoryDataSource((ArchitectDataSource)repositoryConnectionsBox.getSelectedItem());
		// Don't mangle the owner and username fields -- some databases like Postgres are case sensitive
		plexp.setTargetSchema(targetSchema.getText());
		// repository schema does not need to be set here, it is set in the 
		// the Architect Data Source!
		
		plJobId.setText(PLUtils.toPLIdentifier(plJobId.getText()));
		plexp.setJobId(plJobId.getText());
		plFolderName.setText(PLUtils.toPLIdentifier(plFolderName.getText()));
		plexp.setFolderName(plFolderName.getText());
		plexp.setJobDescription(plJobDescription.getText());
		plexp.setJobComment(plJobComment.getText());	
		plexp.setRunPLEngine(runPLEngine.isSelected());
		
		// make sure the user selected a target database    
		if (plexp.getTargetDataSource() == null) {
			JOptionPane.showMessageDialog(this, "You have to select a Target database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (plexp.getRepositoryDataSource() == null) {
			JOptionPane.showMessageDialog(this, "You have to select a Repository database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// make sure user provided a PL Job Name
		if (plexp.getJobId().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "You have to specify the PowerLoader Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// make sure we have an engine!
		if (plexp.getRunPLEngine()) {
			String plEngineSpec = ArchitectFrame.getMainInstance().getUserSettings().getETLUserSettings().getPowerLoaderEnginePath(); 
			if (plEngineSpec == null || plEngineSpec.length() == 0) {
				// not set yet, so redirect the user to User Settings dialog
				JOptionPane.showMessageDialog
				(this,"Please specify the location of the PL Engine (powerloader_odbc.exe).");						
				//
				ArchitectFrame.getMainInstance().prefAction.showPreferencesDialog();
				return false;
			}
		}
		
		String dupIdMessage = null;
		try {
		    if (checkForDuplicateJobId(plexp) == true) {
		        dupIdMessage = "There is already a job called \""+
		        		plexp.getJobId()+"\".\n"+"Please choose a different job id.";
		    }
		} catch (SQLException ex) {
		    dupIdMessage = "There was a database error when checking for\n"+"duplicate job id:\n\n"+ex.getMessage();
		} catch (ArchitectException ex) {
		    dupIdMessage = "There was an application error when checking for\n"+"duplicate job id:\n\n"+ex.getMessage();
		}
		if (dupIdMessage != null) {
		    JOptionPane.showMessageDialog(this, dupIdMessage, "Error", JOptionPane.ERROR_MESSAGE);
		    return false;
		}
		
		return true;
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
        // nothing to discard
	}

	// ---------------- accessors and mutators ----------------
	
	public boolean isSelectedRunPLEngine(){
		return runPLEngine.isSelected();
	}

		
	


	public static boolean checkForDuplicateJobId(PLExport plExport) throws SQLException, ArchitectException {		
		SQLDatabase target = new SQLDatabase(plExport.getRepositoryDataSource());
		Connection con = null;
		Statement s = null;
		ResultSet rs = null;
		int count = 0;
		try {
			con = target.getConnection();
			s = con.createStatement();
			rs = s.executeQuery("SELECT COUNT(*) FROM pl_job WHERE job_id = " + SQL.quote(plExport.getJobId().toUpperCase()));
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException se) {
					logger.error("problem closing result set.",se);					
				}
			}
			if (s != null) {
				try {
					s.close();
				} catch (SQLException se) {
					logger.error("problem closing statement.",se);					
				}
			}
		}
		if (count == 0) {
			return false;
		} else {
			return true;
		}
	}


	public JPanel getPanel() {
		return this;
	}
	
}





