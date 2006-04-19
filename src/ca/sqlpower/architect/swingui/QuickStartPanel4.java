package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.swingui.PlayPen.AddObjectsTask;
import ca.sqlpower.architect.swingui.QuickStartWizard.GenerateStatementsTask;
import ca.sqlpower.architect.swingui.action.ExportDDLAction;
import ca.sqlpower.architect.swingui.action.ExportPLTransAction;
import ca.sqlpower.architect.swingui.action.ExportDDLAction.ConflictFinderProcess;
import ca.sqlpower.architect.swingui.action.ExportDDLAction.ConflictResolverProcess;
import ca.sqlpower.architect.swingui.action.ExportPLTransAction.ExportTxProcess;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class QuickStartPanel4 implements WizardPanel {
	
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

		wizard.addSourceDatabases(wizard.getSourceTables());
		Map ddlGeneratorMap = ArchitectUtils.getDriverDDLGeneratorMap();
		Class selectedGeneratorClass = (Class) ddlGeneratorMap.get(
				wizard.getPlExport().getTargetDataSource().getDriverClass());
		if (selectedGeneratorClass == null)
		{
			JOptionPane.showMessageDialog(getPanel(),
					"Unable to create DDL Script for the target database.",
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		GenericDDLGenerator ddlg;
		try {
			ddlg = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
			ddlg.setTargetCatalog(wizard.getPlExport().getTargetCatalog());
			ddlg.setTargetSchema(wizard.getPlExport().getTargetSchema());
		
			ArchitectFrame.getMainInstance().getProject().setModified(false);
			ArchitectFrame.getMainInstance().newProjectAction.actionPerformed(
					new ActionEvent(this, 0, null));
			PlayPen p = ArchitectFrame.getMainInstance().getProject().getPlayPen();
			
			p.getDatabase().setDataSource(wizard.getPlExport().getTargetDataSource());

			
			ExportDDLAction eda = 
				(ExportDDLAction) ArchitectFrame.getMainInstance().exportDDLAction;
			ExportPLTransAction epta = 
				(ExportPLTransAction) ArchitectFrame.getMainInstance().exportPLTransAction;
	
			List statements = new ArrayList();
			
			
			
	
			// 1. copy SQL Tables
			ProgressMonitor pm = new ProgressMonitor(null,
					"Copying objects from DBTree", "...", 0, 100);
			AddObjectsTask aot = p.new AddObjectsTask(
					wizard.getSourceTables(), new Point(50, 50),
					pm, wizard.getParentDialog());
	
			// 1a. generate a list of statements
			GenerateStatementsTask gst = 
				new QuickStartWizard.GenerateStatementsTask(statements,
					ddlg,
					p.getDatabase(), 
					wizard.getParentDialog());

			// 2. get sql script worker 
			SQLScriptDialog ssd = new SQLScriptDialog(wizard.getParentDialog(),
					"Preview SQL Script",
					"The Architect will create these tables:", false,
					ddlg,
					wizard.getPlExport().getTargetDataSource(), false);
			MonitorableWorker scriptWorker = ssd.getExecuteTask();
			ssd.setStatementResultList(wizard.getPlExport().getExportResultList());

			
			// 3. create conflict finder ans resolver
			ConflictFinderProcess cfp;
			cfp = eda.new ConflictFinderProcess(
					wizard.getParentDialog(),
					new SQLDatabase(wizard.getPlExport().getTargetDataSource()),
					ddlg, statements);
			
			ConflictResolverProcess crp;
			crp = eda.new ConflictResolverProcess(
					wizard.getParentDialog(), cfp);
			
			// 5. export PL Transactions (and run PL Transactions (if requested)
			// got this far, so it's ok to run the PL Export thread
			ExportTxProcess etp = epta.new ExportTxProcess(
				wizard.getPlExport(),
				wizard.getParentDialog(),
				((WizardDialog)wizard.getParentDialog()).getProgressBar(),
				((WizardDialog)wizard.getParentDialog()).getProgressLabel());
			
			aot.setNextProcess(gst);
			gst.setNextProcess(cfp);
			cfp.setNextProcess(crp);
			crp.setNextProcess(scriptWorker);
			scriptWorker.setNextProcess(etp);
			
		
			((WizardDialog)wizard.getParentDialog()).getNextButton().setEnabled(false);
			new Thread(aot, "Wizard-Objects-Adder").start();
			
		} catch (InstantiationException e1) {
			logger.error("problem running Quick Start Wizard", e1);
		} catch (IllegalAccessException e1) {
			logger.error("problem running Quick Start Wizard", e1);
		} catch (ArchitectException e) {
			logger.error("problem running Quick Start Wizard", e);
		} catch (SQLException e) {
			logger.error("problem running Quick Start Wizard", e);
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}
	
	public String getTitle() {
		return ("Architect Quick Start - Step 4 of 5 - Confirm Selections");
	}
}
