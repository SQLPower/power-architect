package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

import ca.sqlpower.architect.ddl.*;
import ca.sqlpower.architect.etl.*;
import ca.sqlpower.architect.*;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.SQL;

import org.apache.log4j.Logger;

public class ExportPLTransAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(ExportPLTransAction.class);

	protected ArchitectFrame architectFrame;
	protected PlayPen playpen;

	/** The PLExport object that this action uses to create PL transactions. */
	protected PLExport plexp;

	/** The dialog box that this action uses to configure plexp. */
	protected JDialog d;

	/** Progress Bar to tell the user PL Export is still running */
	protected JProgressBar plCreateTxProgressBar;
	protected JLabel plCreateTxLabel;

	public ExportPLTransAction() {
		super("PL Transaction Export...",
			  ASUtils.createIcon("PLTransExport",
								 "PL Transaction Export",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "PL Transaction Export");
	}

	public void setPlayPen(PlayPen playpen) {
		this.playpen = playpen;
	}
	
		
	/**
	 * Sets up the dialog the first time it is called.  After that,
	 * just returns without doing anything.
	 *
	 * <p>Note: the <code>plexp</code> variable must be initialized before calling this method!
	 *
	 * @throws NullPointerException if <code>plexp</code> is null.
	 */
	public synchronized void setupDialog() {

		logger.debug("running setupDialog()");

		// always refresh Target Database (it might have changed)
		plexp.setTargetDataSource(ArchitectFrame.getMainInstance().getProject().getTargetDatabase().getDataSource());
		
		if (d != null) {
			refreshConnections();
			return;
		}

		d = new JDialog(ArchitectFrame.getMainInstance(),
						"Export ETL Transactions to PL Repository");

		// set export defaults if necessary
		if (plexp.getFolderName() == null || plexp.getFolderName().trim().length() == 0) {
			plexp.setFolderName(PLUtils.toPLIdentifier(architectFrame.getProject().getName()+"_FOLDER"));
		}

		if (plexp.getJobId() == null || plexp.getJobId().trim().length() == 0) {
			plexp.setJobId(PLUtils.toPLIdentifier(architectFrame.getProject().getName()+"_JOB"));
		}

		
		JPanel plp = new JPanel(new BorderLayout(12,12));
		plp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); 
		
		final PLExportPanel plPanel = new PLExportPanel();
		plPanel.setPLExport(plexp);
		plp.add(plPanel, BorderLayout.CENTER);
		
		// make an intermediate JPanel
		JPanel bottomPanel = new JPanel(new GridLayout(1,2,25,0)); // 25 pixel hgap		

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				plPanel.applyChanges();
				// make sure the user selected a target database    
				if (plexp.getTargetDataSource() == null) {
					JOptionPane.showMessageDialog(plPanel, "You have to select a Target database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (plexp.getRepositoryDataSource() == null) {
					JOptionPane.showMessageDialog(plPanel, "You have to select a Repository database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// make sure user provided a PL Job Name
				if (plexp.getJobId().trim().length() == 0) {
					JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// make sure we have an engine!
				if (plexp.getRunPLEngine()) {
					String plEngineSpec = architectFrame.getUserSettings().getETLUserSettings().getPowerLoaderEnginePath(); 
					if (plEngineSpec == null || plEngineSpec.length() == 0) {
						// not set yet, so redirect the user to User Settings dialog
						JOptionPane.showMessageDialog
						(plPanel,"Please specify the location of the PL Engine (powerloader_odbc.exe).");						
						//
						ArchitectFrame.getMainInstance().prefAction.showPreferencesDialog();
						return;
					}
				}
				
				String dupIdMessage = null;
				try {
				    if (checkForDuplicateJobId(plexp.getJobId()) == true) {
				        dupIdMessage = "There is already a job called \""+
				        		plexp.getJobId()+"\".\n"+"Please choose a different job id.";
				    }
				} catch (SQLException ex) {
				    dupIdMessage = "There was a database error when checking for\n"+"duplicate job id:\n\n"+ex.getMessage();
				} catch (ArchitectException ex) {
				    dupIdMessage = "There was an application error when checking for\n"+"duplicate job id:\n\n"+ex.getMessage();
				}
				if (dupIdMessage != null) {
				    JOptionPane.showMessageDialog(plPanel, dupIdMessage, "Error", JOptionPane.ERROR_MESSAGE);
				    return;
				}
				
				try {
					List targetDBWarnings = listMissingTargetTables();
					if (!targetDBWarnings.isEmpty()) {
						// modal dialog (hold things up until the user says YES or NO)
						JList warnings = new JList(targetDBWarnings.toArray());
						JPanel cp = new JPanel(new BorderLayout());
						cp.add(new JLabel("<html>The target database schema is not identical to your Architect schema.<br><br>Here are the differences:</html>"), BorderLayout.NORTH);
						cp.add(new JScrollPane(warnings), BorderLayout.CENTER);
						cp.add(new JLabel("Do you want to continue anyway?"), BorderLayout.SOUTH);
						int choice = JOptionPane.showConfirmDialog(playpen, cp, "Target Database Structure Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (choice == JOptionPane.NO_OPTION) {
							return;
						}
					}
				} catch (SQLException esql) {
					JOptionPane.showMessageDialog (architectFrame,"Can't export Transaction: "+esql.getMessage());
					logger.error("Got exception while exporting Trans", esql);
				} catch (ArchitectException arex){
					JOptionPane.showMessageDialog (architectFrame,"Can't export Transaction: "+arex.getMessage());
					logger.error("Got exception while exporting Trans",arex);
				}

				// got this far, so it's ok to run the PL Export thread
				ExportTxWorker worker = new ExportTxWorker(plexp,plPanel);
				new ProgressWatcher(plCreateTxProgressBar, plexp, plCreateTxLabel);
				new Thread(worker).start();
			}
		});
		buttonPanel.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					plPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);

		// stick in the progress bar here...
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));		
 	    plCreateTxProgressBar = new JProgressBar();
		plCreateTxProgressBar.setStringPainted(true); 
		progressPanel.add(plCreateTxProgressBar);		
	    plCreateTxLabel = new JLabel ("Exporting PL Transactions...");
		progressPanel.add(plCreateTxLabel);
		
		// figure out how much space this needs before setting 
		// child components to be invisible
		progressPanel.setPreferredSize(progressPanel.getPreferredSize());  
		plCreateTxProgressBar.setVisible(false);		
		plCreateTxLabel.setVisible(false);		

		bottomPanel.add(progressPanel); // left side, left justified
		bottomPanel.add(buttonPanel); // right side, right justified

		plp.add(bottomPanel, BorderLayout.SOUTH);
		
		d.setContentPane(plp);
		
		// experiment with preferred size crap:
		logger.debug("progressBar preferred size: " + plCreateTxProgressBar.getPreferredSize());
		logger.debug("progressPanel preferred size: " + progressPanel.getPreferredSize());
		logger.debug("bottomPanel preferred size: " + bottomPanel.getPreferredSize());
		logger.debug("plp preferred size: " + plp.getPreferredSize());
		logger.debug("d preferred size: " + d.getPreferredSize());
		d.pack();
		logger.debug("progressBar preferred size: " + plCreateTxProgressBar.getPreferredSize());
		logger.debug("progressPanel preferred size: " + progressPanel.getPreferredSize());
		logger.debug("bottomPanel preferred size: " + bottomPanel.getPreferredSize());
		logger.debug("plp preferred size: " + plp.getPreferredSize());
		logger.debug("d preferred size: " + d.getPreferredSize());
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
	}

	// turn this into an inline Runnable...
	protected class ExportTxWorker implements Runnable {
		PLExport plExport;
		PLExportPanel plPanel;

		public ExportTxWorker (PLExport plExport, PLExportPanel plPanel) {
			this.plExport = plExport;
			this.plPanel = plPanel;
		}		

		public void run() {
			// now implements Monitorable, so we can ask it how it's doing
			try {
				plExport.export(playpen.getDatabase());
				// if the user requested, try running the PL Job afterwards
				if (plExport.getRunPLEngine()) {
					logger.debug("Running PL Engine");
					File plEngine = new File(architectFrame.getUserSettings().getETLUserSettings().getPowerLoaderEnginePath());					
					File plDir = plEngine.getParentFile();
					File engineExe = new File(plDir, PLUtils.getEngineExecutableName(plexp.getRepositoryDataSource()));
					final StringBuffer commandLine = new StringBuffer(1000);
					commandLine.append(engineExe.getPath());
					commandLine.append(" USER_PROMPT=N");
					commandLine.append(" JOB=").append(plexp.getJobId());            	
					commandLine.append(" USER=").append(PLUtils.getEngineConnectString(plexp.getRepositoryDataSource()));
					commandLine.append(" DEBUG=N SEND_EMAIL=N SKIP_PACKAGES=N CALC_DETAIL_STATS=N COMMIT_FREQ=100 APPEND_TO_JOB_LOG_IND=N");
					commandLine.append(" APPEND_TO_JOB_ERR_IND=N");
					commandLine.append(" SHOW_PROGRESS=100" );
					commandLine.append(" SHOW_PROGRESS=10" );
					logger.debug(commandLine.toString());
					// worker thread must not talk to Swing directly...
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								final Process proc = Runtime.getRuntime().exec(commandLine.toString());
								final JDialog pld = new JDialog(architectFrame, "Power*Loader Engine");
									
								EngineExecPanel eep = new EngineExecPanel(commandLine.toString(), proc);
								pld.setContentPane(eep);
								
								JButton abortButton = new JButton(eep.getAbortAction());
								JButton closeButton = new JButton("Close");
                           		
								closeButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent evt) {
											pld.setVisible(false);
										}
									});
                           		
								JCheckBox scrollLockCheckBox = new JCheckBox(eep.getScrollBarLockAction());
            	       			
								JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
								buttonPanel.add(abortButton);
								buttonPanel.add(closeButton);
								buttonPanel.add(scrollLockCheckBox);
								eep.add(buttonPanel, BorderLayout.SOUTH);
								
								pld.pack();
								pld.setLocationRelativeTo(plPanel);
								pld.setVisible(true);
							} catch (IOException ie){
								JOptionPane.showMessageDialog(playpen, "Unexpected Exception running Engine:\n"+ie);
								logger.error("IOException while trying to run engine.",ie);
							}
						}
					});
				}
			} catch (PLSecurityException ex) {
				final Exception fex = ex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export Transaction: "+fex.getMessage());
						logger.error("Got exception while exporting Trans", fex);	
					}
				});
			} catch (SQLException esql) {
				final Exception fesql = esql;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export Transaction: "+fesql.getMessage());
						logger.error("Got exception while exporting Trans", fesql);
					}
				});
			} catch (ArchitectException arex){
				final Exception farex = arex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export Transaction: "+farex.getMessage());
						logger.error("Got exception while exporting Trans", farex);
					}
				});
			} finally {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						d.setVisible(false);
					}
				});
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		plexp = architectFrame.getProject().getPLExport();
		setupDialog();
		d.setVisible(true); 
	}

	/**
	 * Checks for missing tables in the target database.  Returns a
	 * list of table names that need to be created.
	 */
	public List listMissingTargetTables() throws SQLException, ArchitectException {
		List missingTables = new LinkedList();
		Iterator targetTableIt = playpen.getDatabase().getTables().iterator();
		while (targetTableIt.hasNext()) {
			SQLTable t = (SQLTable) targetTableIt.next();
			String tableStatus = checkTargetTable(t);
			if (tableStatus != null) {
				missingTables.add(tableStatus);
			}
		}
		return missingTables;
	}

	/**
	 * Checks for the existence of the given table in the actual
	 * target database, and also compares its columns to those of the
	 * actual table (if the table exists in the target database).
	 *
	 * @return A short message describing the differences between the
	 * given table <code>t</code> and its counterpart in the physical
	 * target database.  If the actual target table is identical to
	 * <code>t</code>, returns <code>null</code>.
	 */
	protected String checkTargetTable(SQLTable t) throws SQLException, ArchitectException {
		GenericDDLGenerator ddlg = architectFrame.getProject().getDDLGenerator();
		logger.debug("DDLG class is: " + ddlg.getClass().getName());
		String tableName = ddlg.toIdentifier(t.getName());
		List ourColumns = new ArrayList();
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			ourColumns.add(ddlg.toIdentifier(c.getName()).toLowerCase());
		}

		List actualColumns = new ArrayList();
		Connection con = t.getParentDatabase().getConnection();
		DatabaseMetaData dbmd = con.getMetaData();
		ResultSet rs = null;
		try {
			logger.debug("Fetching columns of "+plexp.getTargetSchema()+"."+tableName);
			rs = dbmd.getColumns(null, plexp.getTargetSchema(), tableName, null);
			while (rs.next()) {
				actualColumns.add(rs.getString(4).toLowerCase()); // column name
			}
		} finally {
			if (rs != null) rs.close();
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("   ourColumns = "+ourColumns);
			logger.debug("actualColumns = "+actualColumns);
		}
		if (actualColumns.isEmpty()) {
			return "Target table \""+tableName+"\" does not exist";
		} else {
			if (actualColumns.containsAll(ourColumns)) {
				return null;
			} else {
				return "Target table \""+tableName+"\" exists but is missing columns";
			}
		}
	}

	public static boolean checkForDuplicateJobId(String jobId) throws SQLException, ArchitectException {
		PLExport plExport = ArchitectFrame.getMainInstance().getProject().getPLExport();		
		
		SQLDatabase target = new SQLDatabase(plExport.getRepositoryDataSource());
		Connection con = null;
		Statement s = null;
		ResultSet rs = null;
		int count = 0;
		try {
			con = target.getConnection();
			s = con.createStatement();
			rs = s.executeQuery("SELECT COUNT(*) FROM pl_job WHERE job_id = " + SQL.quote(jobId.toUpperCase()));
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
	
	private void refreshConnections() {
		PLExportPanel plep = null;
		boolean found = false;
		int ii = 0;
		java.awt.Component [] panels = d.getContentPane().getComponents();
		// figure out which panel is the PLExportPanel
		while (!found && ii < panels.length) {
			if (panels [ii] instanceof PLExportPanel) {
				logger.debug("content pane class:" + panels[ii].getClass().getName());
				plep = (PLExportPanel) panels [ii];
				found = true;
			}
		}
		// call the refresh method
		if (plep != null) { 
			logger.debug("refreshing PL Export JDBC connection list");
			plep.refreshTargetConnections();
			plep.refreshRepositoryConnections();
		}
	}
}