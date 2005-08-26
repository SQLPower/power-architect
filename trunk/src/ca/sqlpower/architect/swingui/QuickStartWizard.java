/*
 * Created on Jun 22, 2005
 *
 * Manage the Export ETL workflow.  This class keeps track of the 
 * workflow state, and takes care of giving the framework panels
 * when it does a "back" or "next" command.
 */
package ca.sqlpower.architect.swingui;
import java.awt.Point;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.swingui.ExportDDLAction.ConflictFinderProcess;
import ca.sqlpower.architect.swingui.ExportDDLAction.ConflictResolverProcess;
import ca.sqlpower.architect.swingui.ExportDDLAction.DDLExecutor;
import ca.sqlpower.architect.swingui.ExportPLTransAction.ExportTxProcess;
import ca.sqlpower.architect.swingui.PlayPen.AddObjectsTask;


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
	
	public QuickStartWizard () {
		onLastPanel = false;
		panels = new LinkedList();
		currentPanel = new QuickStartPanel1(this);
		panels.add(currentPanel);
		title = "Power Architect Quick Start Wizard";
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
		if (panels.indexOf(currentPanel) == 2) {
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
	
	public void execute(JDialog d) {
		try {
			// short process, no need for seperate thread...
			addTargetDatabase(plExport.getTargetDataSource());
			addSourceDatabases(sourceTables);
			
			Map ddlGeneratorMap = ArchitectUtils.getDriverDDLGeneratorMap();			
			Class selectedGeneratorClass = (Class) ddlGeneratorMap.get(plExport.getTargetDataSource().getDriverClass());
			GenericDDLGenerator ddlg = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
			WizardDialog wizardDialog = (WizardDialog) d;
			PlayPen p = ArchitectFrame.getMainInstance().getProject().getPlayPen();
			ExportDDLAction eda = (ExportDDLAction) ArchitectFrame.getMainInstance().exportDDLAction;
			ExportPLTransAction epta = (ExportPLTransAction) ArchitectFrame.getMainInstance().exportPLTransAction;
									
			// 1. copy SQL Tables
			ProgressMonitor pm
			 = new ProgressMonitor(null,
			                      "Copying objects from DBTree",
			                      "...",
			                      0,
				                  100);			
			AddObjectsTask aot = p.new AddObjectsTask(sourceTables, 
					new Point(50,50),pm, d);
			
			// 1a. generate a list of statements
			List statements = new ArrayList();
			GenerateStatementsTask gst = new GenerateStatementsTask(statements,ddlg,p.getDatabase(),d);

			// 2. find conflicts
			ConflictFinderProcess cfp = eda.new ConflictFinderProcess(
					d, new SQLDatabase(plExport.getTargetDataSource()), 
					ddlg, statements, 
					wizardDialog.getProgressBar(), 
					wizardDialog.getProgressLabel());				

			// 3. resolve conflicts
			ConflictResolverProcess crp = eda.new ConflictResolverProcess(d, cfp, 
					wizardDialog.getProgressBar(), 
					wizardDialog.getProgressLabel());

			// 4. execute DDL 
			DDLExecutor eDDL = eda.new DDLExecutor(d, statements, 
					wizardDialog.getProgressBar(), 
					wizardDialog.getProgressLabel());
			
			// 5. export PL Transactions (and run PL Transactions (if requested)
			// got this far, so it's ok to run the PL Export thread
			ExportTxProcess etp = epta.new ExportTxProcess(plExport,d,
					wizardDialog.getProgressBar(),
					wizardDialog.getProgressLabel());
						
			// chain together the transactions
			aot.setNextProcess(gst);
			gst.setNextProcess(cfp);
			cfp.setNextProcess(crp);
			crp.setNextProcess(eDDL);
		    eDDL.setNextProcess(etp);
			
			// finally, kick off the process
			new Thread(aot, "Wizard-Objects-Adder").start();		
			
		} catch (Exception ex) {
			logger.error("problem running Quick Start Wizard",ex);
		}
	}
	
	private class GenerateStatementsTask implements Runnable {
		List statements;
		GenericDDLGenerator ddlg;
		SQLDatabase db;
		JDialog parentDialog;
		Runnable nextProcess;
		String errorMessage = null;
		
		
		GenerateStatementsTask (List statements, 
				GenericDDLGenerator ddlg,
				SQLDatabase db, 
				JDialog parentDialog) {
			this.statements = statements;
			this.ddlg = ddlg;
			this.db = db;
			this.parentDialog = parentDialog;
		}
		
		public void run() {
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
			SwingUtilities.invokeLater(new Runnable() {public void run(){runFinished();}});
		}
		
		/**
		 * Displays error messages or invokes the next process in the chain on a new
		 * thread. The run method asks swing to invoke this method on the event dispatch
		 * thread after it's done.
		 */
		public void runFinished() {
			if (errorMessage != null) {
				JOptionPane.showMessageDialog(parentDialog, errorMessage, "Error while generating DDL", JOptionPane.ERROR_MESSAGE);
				nextProcess = null;
			}
			if (nextProcess != null) {
				new Thread(nextProcess).start();
			}
		}
		/**
		 * @return Returns the nextProcess.
		 */
		public Runnable getNextProcess() {
			return nextProcess;
		}
		/**
		 * @param nextProcess The nextProcess to set.
		 */
		public void setNextProcess(Runnable nextProcess) {
			this.nextProcess = nextProcess;
		}
	}	

	
	/**
	 * Grab the playpen SQLdatabase object (which has already been created)
	 * and copy over the contents of the selected datasource. 
	 *
	 */
	private void addTargetDatabase(ArchitectDataSource target) {
		ArchitectDataSource tSpec = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase().getDataSource();
		ArchitectDataSource dbcs = plExport.getTargetDataSource();
    	tSpec.setDriverClass(dbcs.getDriverClass());
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
	private void addSourceDatabases(List sourceTables) {
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
					
				
		/* TODO:
		 * 
		 * 1. complete these utility methods
		 * 2. create the third panel:
		 *  
		 * - JTable showing all of the source tables
		 * - Target System Name/Schema (do we need to optionally ask for the Catalog?)
		 * - Repository System Name/Schema 
		 * - Run Engine
		 * - other PL submission parameters
		 * 
		 * 3. invoke the other routines (I think they are more or less decoupled from 
		 * their parent dialogs (though I think I do need to replicate/extract some logic 
		 * from the containing dialogs.
		 * 
		 * 
		 * 
		 * 
		 */
		
	
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
}
