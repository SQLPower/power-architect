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
		if (d != null) return;
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
				if (plexp.getPlDBCS() == null) {
					JOptionPane.showMessageDialog(plPanel, "You have to select a target database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// make sure they provided a user name
				if (plexp.getPlUsername().trim().length() == 0) {
					JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader User Name.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				} // make sure user provided a PL Job Name
				if (plexp.getJobId().trim().length() == 0) {
					JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
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
				plexp.prepareToStart(); // sets finished to false
	   	        ProgressWatcher watcher = new ProgressWatcher(plCreateTxProgressBar,plexp,plCreateTxLabel);
				new javax.swing.Timer(50, watcher).start();
				new Thread(worker).start();								
			}
		});
		buttonPanel.add(okButton);

		//
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
		plCreateTxProgressBar.setVisible(false);		
		progressPanel.add(plCreateTxProgressBar);		
	    plCreateTxLabel = new JLabel ("Exporting PL Transactions...");
		plCreateTxLabel.setVisible(false);
		progressPanel.add(plCreateTxLabel);

		bottomPanel.add(progressPanel); // left side, left justified
		bottomPanel.add(buttonPanel); // right side, right justified

		plp.add(bottomPanel, BorderLayout.SOUTH);
		
		d.setContentPane(plp);
		d.pack();
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
				if (plPanel.isSelectedRunPLEngine()) {
					logger.debug("Running PL Engine");
					File plIni = new File(architectFrame.getUserSettings().getETLUserSettings().getPlDotIniPath());
					File plDir = plIni.getParentFile();
					File engineExe = new File(plDir, plPanel.getPLConnectionSpec().getEngineExecutableName());
					final StringBuffer commandLine = new StringBuffer(1000);
					commandLine.append(engineExe.getPath());
					commandLine.append(" USER_PROMPT=N");
					commandLine.append(" JOB=").append(plexp.getJobId());            	
					commandLine.append(" USER=").append(plPanel.getPLConnectionSpec().getEngineConnectString());
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
								logger.error(ie);
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
			logger.debug("Fetching columns of "+plexp.getOutputTableOwner()+"."+tableName);
			rs = dbmd.getColumns(null, plexp.getOutputTableOwner(), tableName, null);
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
}
