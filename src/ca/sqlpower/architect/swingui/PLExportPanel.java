package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.etl.PLUtils;
import ca.sqlpower.architect.swingui.event.DatabaseComboBoxListener;
import ca.sqlpower.sql.SQL;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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

	protected DatabaseComboBoxListener dcl;
	protected DatabaseSelector target;
	protected DatabaseSelector repository;
	
	private JProgressBar progressBar;
	private JLabel label;
	
	protected JTextField plFolderName;
	protected JTextField plJobId;
	protected JTextField plJobDescription;
	protected JTextField plJobComment;

	
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
		


		progressBar = new JProgressBar();
		label = new JLabel();
		label.setText("Loading Database.....");
		progressBar.setVisible(false);
		label.setVisible(false);
		target = new DatabaseSelector(progressBar,label,this.getPanel());
		repository = new DatabaseSelector(progressBar,label,this.getPanel());
		
		plFolderName = new JTextField();
		plJobId = new JTextField();
		plJobDescription = new JTextField();
		plJobComment = new JTextField();
		runPLEngine = new JCheckBox("Run PL Engine?");
			
		FormLayout layout = new FormLayout("10dlu, 80dlu,10dlu, 5dlu,fill:100dlu:grow, 10dlu, 40dlu,30dlu", //Columns
		"4dlu, 20dlu, 1dlu, 20dlu, 1dlu, 20dlu, " + // Target Connection
		"4dlu, 20dlu,1dlu, 20dlu, 1dlu, 20dlu," + 	//Repository Connection
		"4dlu, 20dlu, 1dlu, 20dlu, 1dlu, 20dlu, 1dlu, 20dlu," +
		"4dlu, 20dlu,4dlu, 20dlu");
		
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();		
		pb.add(new JLabel("Target Connection"), cc.xy(2,2,"r,c"));
		pb.add(target.getConnectionsBox(), cc.xyw(4,2,2));
		pb.add(target.getNewButton(), cc.xy(7,2));
		pb.add(new JLabel("Target Catalog"), cc.xy(2,4, "r,c"));		
		pb.add(target.getCatalogBox(), cc.xyw(4,4,2));
		pb.add(new JLabel("Target Schema"), cc.xy(2,6, "r,c"));		
		pb.add(target.getSchemaBox(), cc.xyw(4,6,2));
		pb.add(new JLabel("Repository Connection"), cc.xy(2,8,"r,c"));
		pb.add(repository.getConnectionsBox(), cc.xyw(4,8,2));
		pb.add(repository.getNewButton(), cc.xy(7,8));
		pb.add(new JLabel("Repository Catalog"), cc.xy(2,10, "r,c"));		
		pb.add(repository.getCatalogBox(), cc.xyw(4,10,2));
		pb.add(new JLabel("Repository Schema"), cc.xy(2,12, "r,c"));		
		pb.add(repository.getSchemaBox(), cc.xyw(4,12,2));
		pb.add(new JLabel("PL Folder Name"), cc.xy(2,14, "r,c"));		
		pb.add(plFolderName, cc.xyw(4,14,2));
		pb.add(new JLabel("PL Job Id"), cc.xy(2,16, "r,c"));		
		pb.add(plJobId, cc.xyw(4,16,2));
		pb.add(new JLabel("PL Job Description"), cc.xy(2,18, "r,c"));		
		pb.add(plJobDescription, cc.xyw(4,18,2));
		pb.add(new JLabel("PL Job Comment"), cc.xy(2,20, "r,c"));		
		pb.add(plJobComment, cc.xyw(4,20,2));		
		pb.add(runPLEngine, cc.xyw(4,22,2));
		pb.add(label, cc.xy(2,24, "r,c"));		
		pb.add(progressBar, cc.xyw(4,24,2));
		
			
		add(pb.getPanel());
	}
	

	/**
	 * Sets a new PLExport object for this panel to edit.  All field
	 * values will be updated to reflect the status of the given
	 * PLExport object.
	 */
	public void setPLExport(PLExport plexp) {
		this.plexp = plexp;
		
		target.getConnectionsBox().setSelectedItem(plexp.getTargetDataSource());
		repository.getConnectionsBox().setSelectedItem(plexp.getRepositoryDataSource());
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

	
	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Converts the fields that contain PL Identifiers into the
	 * correct format (using PLUtils.toPLIdentifier) and then sets all
	 * the properties of plexp to their values in this panel's input
	 * fields.
	 */
	public boolean applyChanges() {
	    logger.debug("Applying changes to the PLExport object");
		plexp.setTargetDataSource((ArchitectDataSource)(target.getConnectionsBox().getSelectedItem()));
		plexp.setRepositoryDataSource((ArchitectDataSource)repository.getConnectionsBox().getSelectedItem());
		// Don't mangle the owner and username fields -- some databases like Postgres are case sensitive
		if (target.getSchemaBox().isEnabled()){
			plexp.setTargetSchema((target.getSchemaBox().getSelectedItem()).toString());
		}
		if (repository.getSchemaBox().isEnabled()){
			plexp.setRepositorySchema((repository.getSchemaBox().getSelectedItem()).toString());
		}
		if (target.getCatalogBox().isEnabled()){
			plexp.setTargetCatalog((target.getCatalogBox().getSelectedItem()).toString());
		}
		if (repository.getCatalogBox().isEnabled()){
			plexp.setRepositoryCatalog((repository.getCatalogBox().getSelectedItem()).toString());
		}
		
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
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
					logger.error("problem closing connection.",se);					
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





