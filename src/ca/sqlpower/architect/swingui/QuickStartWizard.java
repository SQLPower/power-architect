/*
 * Created on Jun 22, 2005
 *
 * Manage the Export ETL workflow.  This class keeps track of the 
 * workflow state, and takes care of giving the framework panels
 * when it does a "back" or "next" command.
 */
package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;


/**
 * @author jack
 */
public class QuickStartWizard implements ArchitectWizard {
	private static final Logger logger = Logger.getLogger(QuickStartWizard.class);

	boolean onLastPanel; 
	LinkedList panels;
	WizardPanel currentPanel;
	String title;
	
	// quick start model
	List sourceTables;
	PLExport plExport;
	private JTextArea resultOutput;
	
	private JDialog parentDialog;
	
	public QuickStartWizard () {
		onLastPanel = false;
		panels = new LinkedList();
		currentPanel = new QuickStartPanel1(this);
		panels.add(currentPanel);
		title = "Power Architect Quick Start Wizard";
		resultOutput = new JTextArea();
		resultOutput.setEditable(false);
	}
		
	/*
	 * Return the next Wizard Panel in the workflow.  
	 * 
	 * 1. Use currentPanel index to figure out where we are in the workflow.  
	 * 2. Create the next WizardPanel only if necessary.  
	 * 3. Complete the wizard if we detect that we're on the last panel.
	 */
	public WizardPanel getNext() {
		// panel has already been created
		if (panels.indexOf(currentPanel) < panels.size()-1) {
			currentPanel = (WizardPanel) panels.get(panels.indexOf(currentPanel)+1);			
		} else if (panels.indexOf(currentPanel) == 0) {
			currentPanel = new QuickStartPanel2(this);			
			panels.add(currentPanel);			
		} else if (panels.indexOf(currentPanel) == 1) {
			currentPanel = new QuickStartPanel3(this);			
			panels.add(currentPanel);
		}else if (panels.indexOf(currentPanel) == 2) {
			currentPanel = new QuickStartPanel4(this);			
			panels.add(currentPanel);			
		}else if (panels.indexOf(currentPanel) == 3) {
			currentPanel = new QuickStartPanel5(this);			
			panels.add(currentPanel);			
		} else {
			// can't get here, so throw a runtime exception
			throw new IllegalStateException("called getNext when Wizard was on last Panel");
		}
		return currentPanel;
	}
	
	public WizardPanel getPrevious() {
		if (panels.indexOf(currentPanel) > 0) {
			currentPanel = (WizardPanel) panels.get(panels.indexOf(currentPanel)-1);
			return currentPanel;
		} else {
			throw new IllegalStateException ("called getPrevious() when wizard panels had less than 2 items!");
		}		
	}

	public WizardPanel getCurrent() {
		return currentPanel;
	}
	
	public boolean isOnLastPanel() {
		if (panels.indexOf(currentPanel) == 4) {
			return true;
		} else {
			return false;		
		}
	}
	
	
	//We need this method to know when the execute task will need to be called
	public boolean isOnExecutePanel(){
		if (panels.indexOf(currentPanel) == 3) {
			return true;
		} else {
			return false;		
		}
		
	}
	
	public boolean isOnFirstPanel() {
		if (panels.indexOf(currentPanel) == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	public static class GenerateStatementsTask extends ArchitectSwingWorker {
		List statements;
		DDLGenerator ddlg;
		SQLDatabase db;
		JDialog parentDialog;
		String errorMessage = null;
		
		
		GenerateStatementsTask (List statements, 
				DDLGenerator ddlg,
				SQLDatabase db, 
				JDialog parentDialog) {
			this.statements = statements;
			this.ddlg = ddlg;
			this.db = db;
			this.parentDialog = parentDialog;
		}
		
		public void doStuff() {
			if ( !isCanceled()) {
				try {
					List list = ddlg.generateDDLStatements(db);
					logger.debug("generated statements are: " + list);
					// copy statements
					Iterator it = list.iterator();
					while (it.hasNext()) {
						statements.add(it.next());
					}
					
				} catch (Exception ex) {
					logger.error("Error while generating DDL", ex);
					errorMessage = "Error while generating DDL:\n\n"+ex.getMessage();
				}
			}
		}
		
		/**
		 * Displays error messages or invokes the next process in the chain on a new
		 * thread. The run method asks swing to invoke this method on the event dispatch
		 * thread after it's done.
		 */
		public void cleanup() {
			if (errorMessage != null) {
				JOptionPane.showMessageDialog(parentDialog, errorMessage, "Error while generating DDL", JOptionPane.ERROR_MESSAGE);
				setCancelled(true);
			}
		}
		
	}	

	
	/**
	 * Grab the playpen SQLdatabase object (which has already been created)
	 * and copy over the contents of the selected datasource. 
	 *
	 */
	public void addTargetDatabase(ArchitectDataSource target) {
		ArchitectDataSource tSpec = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase().getDataSource();
		ArchitectDataSource dbcs = plExport.getTargetDataSource();
    	tSpec.getParentType().setJdbcDriver(dbcs.getDriverClass());
    	tSpec.setUrl(dbcs.getUrl());
    	tSpec.setUser(dbcs.getUser());
    	tSpec.setPass(dbcs.getPass());
        tSpec.setPlSchema(dbcs.getPlSchema());
		tSpec.setPlDbType(dbcs.getPlDbType());
		tSpec.setOdbcDsn(dbcs.getOdbcDsn());
	}
	/*
	 * Based on the selected tables, generate a list of source
	 * databases.  Then add them to the DBTree.
	 * 
	 */
	void addSourceDatabases(List sourceTables) {
		Set s = new HashSet();
		Iterator it = sourceTables.iterator();
		while (it.hasNext()) {
			SQLTable t = (SQLTable) it.next();
			s.add(t.getParentDatabase());			
		}
		SQLObject root = (SQLObject) ArchitectFrame.getMainInstance().dbTree.getModel().getRoot();
		Iterator it2 = s.iterator();
		while (it2.hasNext()) {
			try {
				SQLDatabase source = (SQLDatabase) it2.next();
				SQLDatabase newDB = new SQLDatabase(source.getDataSource());		
				root.addChild(root.getChildCount(), newDB);
			} catch (ArchitectException ex) {
				logger.error("Couldn't add new database to tree", ex);
			}
		}
	}
										
	
	public String getTitle() {
		return title;
	}
	
	public static List getDatabases() {
		List list = new ArrayList();
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			list.add(new SQLDatabase((ArchitectDataSource) it.next()));
		}		
		return list;
	}
	
	/**
	 * @return Returns the sourceTables.
	 */
	public List getSourceTables() {
		return sourceTables;
	}
	/**
	 * @param sourceTables The sourceTables to set.
	 */
	public void setSourceTables(List sourceTables) {
		this.sourceTables = sourceTables;
	}
	/**
	 * @return Returns the plExport.
	 */
	public PLExport getPlExport() {
		return plExport;
	}
	/**
	 * @param plExport The plExport to set.
	 */
	public void setPlExport(PLExport plExport) {
		this.plExport = plExport;
	}

	public JDialog getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(JDialog parentDialog) {
		this.parentDialog = parentDialog;
	}

	public JTextArea getResultOutput() {
		return resultOutput;
	}

	public void setResultOutput(JTextArea resultOutput) {
		this.resultOutput = resultOutput;
	}
	
	public void UpdateTextArea () {
		WizardDialog wd = (WizardDialog)getParentDialog();
		
		wd.getNextButton().setEnabled(true);
		wd.getProgressBar().setVisible(false);
		wd.getProgressLabel().setVisible(false);
		StringBuffer text = new StringBuffer();
		
		for ( LabelValueBean lvb : plExport.getExportResultList() ) {
			text.append("  "+ lvb.getLabel() + "\n\t"+ lvb.getValue() + "\n");
		}
		resultOutput.setText(text.toString());
	}


}
