package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import ca.sqlpower.architect.*;
import ca.sqlpower.architect.ddl.*;
import org.apache.log4j.Logger;

public class ExportDDLAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(ExportDDLAction.class);

	// to be created and destroyed when needed 
	protected LogWriter logWriter = null;

	protected ArchitectFrame architectFrame;

	public ExportDDLAction() {
		super("Forward Engineer...",
			  ASUtils.createIcon("ForwardEngineer",
								 "Forward Engineer",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "Forward Engineer SQL Script");
	}

	public void actionPerformed(ActionEvent e) {
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Forward Engineer SQL Script");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		final DDLExportPanel ddlPanel = new DDLExportPanel(architectFrame.project);
		cp.add(ddlPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						if (ddlPanel.applyChanges()) {
							showPreview(architectFrame.project.getDDLGenerator(), d);
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export DDL: "+ex.getMessage());
						logger.error("Got exception while exporting DDL", ex);

					}
				}
			});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					ddlPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		
		cp.add(buttonPanel, BorderLayout.SOUTH);
		
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}

	protected void showPreview(GenericDDLGenerator ddlGen, JDialog parentDialog) {
		final GenericDDLGenerator ddlg = ddlGen;
		final JDialog parent = parentDialog;
		final JDialog d = new JDialog(parent, "DDL Preview");
		try {
			JPanel cp = new JPanel(new BorderLayout(12, 12));
			cp.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			StringBuffer ddl = ddlg.generateDDL(architectFrame.playpen.getDatabase());
			List warnings = ddlg.getWarnings();
			if (warnings.size() > 0) {
				TableSorter sorter = new TableSorter(new DDLWarningTableModel(warnings));
				JTable warningTable = new JTable(sorter);
				sorter.setTableHeader(warningTable.getTableHeader());
				JOptionPane.showMessageDialog(parent, new JScrollPane(warningTable), "Warnings in generated DDL", JOptionPane.WARNING_MESSAGE);
			}
			final JTextArea ddlArea = new JTextArea(ddl.toString(), 25, 60);
			ddlArea.setEditable(false); // XXX: will make this editable in the future
			cp.add(new JScrollPane(ddlArea), BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JProgressBar progressBar = new JProgressBar();
			progressBar.setStringPainted(true); 
			progressBar.setVisible(false);		
			progressPanel.add(progressBar);
			final JLabel progressLabel = new JLabel("Starting...");
			progressLabel.setVisible(false);
			progressPanel.add(progressLabel);
			buttonPanel.add(progressPanel);

			final JButton executeButton = new JButton("Execute");
			executeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						executeProcess(ddlg, progressBar, progressLabel, d);
					}
			});
			buttonPanel.add(executeButton);

			final JButton saveButton = new JButton("Save");
			saveButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.addChoosableFileFilter(ASUtils.SQL_FILE_FILTER);
						fc.setSelectedFile(ddlg.getFile());
						int rv = fc.showSaveDialog(d);
						if (rv == JFileChooser.APPROVE_OPTION) {
							ddlg.setFile(fc.getSelectedFile());
							BufferedWriter out = null;
							try {
								out = new BufferedWriter(new FileWriter(ddlg.getFile()));
								out.write(ddlArea.getText());
							} catch (IOException ex) {
								JOptionPane.showMessageDialog(d, "Couldn't save DDL:\n"
															  +ex.getMessage());
							} finally {
								try {
									if (out != null) out.close();
								} catch (IOException ioex) {
									logger.error("Couldn't close file in finally clause", ioex);
								}
								d.setVisible(false);
								parent.setVisible(false);
							}
						}
					}
				});
			buttonPanel.add(saveButton);
											
			final JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						d.setVisible(false);
						parent.setVisible(false);
					}
				});
			buttonPanel.add(cancelButton);
			cp.add(buttonPanel, BorderLayout.SOUTH);

			d.setContentPane(cp);
			d.pack();
			d.setLocationRelativeTo(parent);
			d.setVisible(true);
		} catch (Exception e) {
			logger.error("Couldn't Generate DDL", e);
			JOptionPane.showMessageDialog(parent, "Couldn't Generate DDL:\n"+e.getMessage());
		}
	}
	
	public void executeProcess(GenericDDLGenerator ddlg, 
			                   JProgressBar progressBar,
							   JLabel progressLabel,
							   JDialog parentDialog) {
	    // create a chain of worker processes then start the first one
		JDialog d = parentDialog;
		try {
			SQLDatabase targetDB = ArchitectFrame.getMainInstance().playpen.getDatabase();
			List statements = ddlg.generateDDLStatements(targetDB);
			logger.debug("generated statements are: " + statements);
			
			// Create the processes in the order they will be run
			ConflictFinderProcess cfp = new ConflictFinderProcess(
					d, targetDB, ddlg, statements, progressBar, progressLabel);
			ConflictResolverProcess crp = new ConflictResolverProcess(d, cfp, progressBar, progressLabel);
			DDLExecutor eDDL = new DDLExecutor(d, statements, progressBar, progressLabel) ;
			
			// Link the processes to establish the ordering
			cfp.setNextProcess(crp);
			crp.setNextProcess(eDDL);
			
			new Thread(cfp).start();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(d, 
					"Couldn't start forward engineering:\n\n"
					+ex.getClass().getName()+"\n"
					+ex.getMessage()+"\n",
					"Error", JOptionPane.ERROR_MESSAGE);				
		}	
	}

	/**
	 * The ConflictFinderProcess uses a ConflictResolver (which it monitors with a progress bar)
	 * to locate objects in the target database which need to be removed before a set of DDL statements
	 * can be executed in it.
	 * 
	 * @author fuerth
	 */
	protected class ConflictFinderProcess implements Runnable {

		JDialog parentDialog;
		SQLDatabase target;
		DDLGenerator ddlg;
		List statements;
		Runnable nextProcess;
		
		/**
		 * If something goes wrong in the constructor, it will ensure the run() method
		 * doesn't do anything by setting this flag to true.
		 */
		boolean cancelled = false;
		
		/**
		 * This Conflict Resolver is created and populated by the run() method.
		 */
		ConflictResolver cr;

		/**
		 * If something goes wrong in the run() method, the runFinished() method will
		 * be given a message to display in this string.
		 */
		String errorMessage;
		
		/**
		 * If something throws an exception in the run() method, it is saved here and
		 * displayed in runFinished().
		 */
		Throwable error;
		private boolean shouldDropConflicts;
		private boolean userWantsToDeleteConflicts;
		
		/**
		 * @param parentDialog The JDialog we're doing this in.
		 * @param target The target database (where to search for the conflicts).
		 * @param ddlg The DDL Generator that we're using.
		 * @param nextProcess The next process to launch if everything goes ok.
		 * @throws ArchitectException If there is a problem connecting to the target database
		 * @throws SQLException If the conflict resolver chokes
		 */
		public ConflictFinderProcess(JDialog parentDialog, SQLDatabase target,
				DDLGenerator ddlg, List statements, JProgressBar progressBar,
				JLabel label) throws ArchitectException, SQLException {
			super();
			this.parentDialog = parentDialog;
			this.target = target;
			this.ddlg = ddlg;
			this.statements = statements;

			label.setText("Searching for conflicts...");
			Connection con = target.getConnection();
			cr = new ConflictResolver(con, ddlg, statements);
			new ProgressWatcher(progressBar, cr, label);
		}
		
		/**
		 * @return True if and only if the user has asked for the conflicts to be deleted.
		 */
		public boolean doesUserWantToDropConflicts() {
			return shouldDropConflicts;
		}

		/**
		 * This should run on its own thread (not the AWT event dispatch thread).  It will take a while.
		 */
		public void run() {
			if (cancelled) return;
			try {
				cr.findConflicting();
			} catch (Exception ex) {
				error = ex;
				errorMessage = "You have to specify a target database connection"
					+"\nbefore executing this script.";
				logger.error("Unexpected exception setting up DDL generation", ex);
				nextProcess = new Runnable() {public void run() {ArchitectFrame.getMainInstance().playpen.showDbcsDialog();}};
			}
			SwingUtilities.invokeLater(new Runnable() {public void run() { runFinished(); }});
		}
		
		/**
		 * When the run() method is done, it schedules this method to be invoked on the AWT event
		 * dispatch thread.
		 */
		private void runFinished() {
			if (!SwingUtilities.isEventDispatchThread()) {
				logger.error("runFinished is running on the wrong thread!");
			}
			if (errorMessage != null) {
				JOptionPane.showMessageDialog(parentDialog, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			} else if (!cr.isEmpty()) {
				Object[] messages = new Object[3];
				messages[0] = "The following objects in the target database"
							 +"\nconflict with those you wish to create:";
				JTextArea conflictsPane = new JTextArea(cr.toConflictTree());
				conflictsPane.setRows(15);
				conflictsPane.setEditable(false);
				messages[1] = new JScrollPane(conflictsPane);
				messages[2] = "Do you want the Architect to drop these objects"
							 +"\nbefore attempting to create the new ones?";
				int choice = JOptionPane.showConfirmDialog(
						parentDialog,
						messages,
						"Conflicting Objects Found",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					shouldDropConflicts = true;
				} else if (choice == JOptionPane.NO_OPTION) {
					shouldDropConflicts = false;
				} else if (choice == JOptionPane.CANCEL_OPTION) {
					shouldDropConflicts = false;
					nextProcess = null;
				}
			}
			
			if (nextProcess != null) {
				new Thread(nextProcess).start();
			}
		}

		public ConflictResolver getConflictResolver() {
			return cr;
		}
		
		public void setNextProcess(Runnable v) {
			this.nextProcess = v;
		}
	}

	/**
	 * The ConflictResolverProcess grabs a conflict resolver from the conflict
	 * finder process, checks if the user said to delete the conflicts, then
	 * asks it to remove the conflicting items while monitoring the progress.
	 * 
	 * @author fuerth
	 * @version $Id$
	 */
	protected class ConflictResolverProcess implements Runnable {

		private JDialog parentDialog;
		private ConflictFinderProcess conflictFinder;
		private JProgressBar progressBar;
		private JLabel progressLabel;

		private ConflictResolver cr;
		private String errorMessage;
		private SQLException error;
		private Runnable nextProcess;
		
		/**
		 * @param d The dialog we anchor popup messages to
		 * @param cfp The conflict finder we extract the conflict list from
		 * @param progressBar The progress bar we show our progress in
		 * @param progressLabel The label where we say what we're doing
		 */
		public ConflictResolverProcess(JDialog d, ConflictFinderProcess cfp, JProgressBar progressBar, JLabel progressLabel) {
			this.parentDialog = d;
			this.conflictFinder = cfp;
			this.progressBar = progressBar;
			this.progressLabel = progressLabel;
		}

		public void run() {
			if (conflictFinder.doesUserWantToDropConflicts()) {
				progressLabel.setText("Deleting Conflicts...");
				cr = conflictFinder.getConflictResolver();
				cr.aboutToCallDropConflicting();
				new ProgressWatcher(progressBar, cr, progressLabel);
				try {
					cr.dropConflicting();
				} catch (SQLException ex) {
					logger.error("Error while dropping conflicting objects", ex);
					errorMessage = "Error while dropping conflicting objects:\n\n"+ex.getMessage();
					error = ex;
				}
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
				JOptionPane.showMessageDialog(parentDialog, errorMessage, "Error Dropping Conflicts", JOptionPane.ERROR_MESSAGE);
				nextProcess = null;
			}
			if (nextProcess != null) {
				new Thread(nextProcess).start();
			}
		}
		/**
		 * See {@link #nextProcess}.
		 */
		public Runnable getNextProcess() {
			return nextProcess;
		}
		/**
		 * See {@link #nextProcess}.
		 */
		public void setNextProcess(Runnable nextProcess) {
			this.nextProcess = nextProcess;
		}
	}
	
	protected class DDLExecutor implements Monitorable, Runnable {		
		
		JDialog dialog;
		List statements;
		JProgressBar progressBar;		
		JLabel label;
		Runnable nextProcess;
		
		int stmtsTried = 0;
		int stmtsCompleted = 0;
		boolean finished = false;
		boolean cancelled = false;
		boolean hasStarted = false;
		boolean allIsWell = true; // TODO: consolidate error messages into a single block?
				
		public DDLExecutor (JDialog dialog, List statements, JProgressBar progressBar, JLabel label) {
			this.dialog = dialog;
			this.statements = statements;
			this.progressBar = progressBar;
			this.label = label;			
		}		

		public Integer getJobSize() throws ArchitectException {			
			if (statements != null) {
				return new Integer(statements.size());
			} else {
				return null;
			}
		}
		
		public int getProgress() throws ArchitectException {
			return stmtsTried;			
		}
		
		public boolean isFinished() throws ArchitectException {
			return finished;
		}
		
		public String getMessage() {
			return null;
		}

		public void cancelJob() {
			cancelled = true;
            finished = true;
		}

		public boolean isCancelled() {
			return cancelled;
		}
		
		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}

		/**
		 * This method runs on a separate worker thread.
		 */
		public void run() {
			hasStarted = true;
			if (cancelled || finished) return;
			label.setText("Creating objects in target database...");
			ProgressWatcher pw = new ProgressWatcher(progressBar, this, label);
			stmtsTried = 0;
			stmtsCompleted = 0;
			SQLDatabase target = architectFrame.playpen.getDatabase();
			// FIXME: for some reason, the SQLTable object is not receiving property
            // change events when something in the underlying DBCS changes; it doesn't
            // seem to matter if it changes on the DBTree or Playpen side.

			logger.debug("the Target Database is: " + target.getDataSource());

			Connection con;
			Statement stmt;

			try {
				con = target.getConnection();
			} catch (ArchitectException ex) {
				allIsWell = false;
				final Exception fex = ex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(dialog, "Couldn't connect to target database: "+fex.getMessage()
							 +"\nPlease check the connection settings and try again.");
						ArchitectFrame.getMainInstance().playpen.showDbcsDialog();
					}
				});								
				finished = true;
				return;
			} catch (Exception ex) {
				allIsWell = false;
				logger.error("Unexpected exception in DDL generation", ex);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(dialog, "You have to specify a target database connection"
							 +"\nbefore executing this script.");
						ArchitectFrame.getMainInstance().playpen.showDbcsDialog();
					}
				});								
				finished = true;
				return;
			}
            
			try {
				logger.debug("the connection thinks it is: " + con.getMetaData().getURL());
				stmt = con.createStatement();
			} catch (SQLException ex) {
				allIsWell = false;
				final Exception fex = ex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(dialog, "Couldn't generate DDL statements: "+fex.getMessage()
							 +"\nThe problem was reported by the target database.");
					}
				});								
				finished = true;
				return;
			}
			
			try {			
				logWriter = new LogWriter(ArchitectSession.getInstance().getUserSettings().getDDLUserSettings().getDDLLogPath());			
				logWriter.info("Starting DDL Generation at " + new java.util.Date(System.currentTimeMillis()));
				logWriter.info("Database Target: " + target.getDataSource());
				logWriter.info("Playpen Dump: " + target.getDataSource());
				

				Iterator it = statements.iterator();
				while (it.hasNext() && !finished) {
					DDLStatement ddlStmt = (DDLStatement) it.next();
					try {
						stmtsTried++;
						logWriter.info("executing: " + ddlStmt.getSQLText());		
						stmt.executeUpdate(ddlStmt.getSQLText());
						stmtsCompleted++;
					} catch (SQLException ex) {
						allIsWell = false;						
						final Exception fex = ex;
						final String fsql = ddlStmt.getSQLText();
						logWriter.info("sql statement failed: " + ex.getMessage());
						try {
							SwingUtilities.invokeAndWait(new Runnable() {						
								public void run() {
									JTextArea jta = new JTextArea(fsql,25,40);
									jta.setEditable(false);
									JScrollPane jsp = new JScrollPane(jta);
									JLabel errorLabel = new JLabel("<html>This SQL statement failed: "+fex.getMessage()
											+"<p>Do you want to continue?</html>");
									JPanel jp = new JPanel(new BorderLayout());
									jp.add(jsp,BorderLayout.CENTER);
									jp.add(errorLabel,BorderLayout.SOUTH);
									int decision = JOptionPane.showConfirmDialog
									(dialog, jp, "SQL Failure", JOptionPane.YES_NO_OPTION);
									if (decision == JOptionPane.NO_OPTION) {
										logWriter.info("Export cancelled by user.");
										cancelJob();
									}
								}
							});
						} catch (InterruptedException ex2) {
							allIsWell = false;
							logger.warn("DDL Worker was interrupted during InvokeAndWait", ex2);
						} catch (InvocationTargetException ex2) {
							allIsWell = false;							
							final Exception fex2 = ex2;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									JOptionPane.showMessageDialog
									(dialog, "Worker thread died: "+fex2.getMessage());
								}
							});
						}
						
						if (isCancelled()) {
							finished = true;
							// don't return, we might as well display how many statements ended up being processed...
						}
					} 
				}
				
				logWriter.info("Successfully executed "+stmtsCompleted+" out of "+stmtsTried+" statements.");
				
			} catch (ArchitectException ex) {
				allIsWell = false;				
				final Exception fex = ex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
						(dialog, "A problem with the DDL log file prevented\n"
								+"DDL generation from running:\n\n"
								+fex.getMessage());
					}
				});
				finished = true;
			} finally {
				// flush and close the LogWriter
				logWriter.flush();
				logWriter.close();
				logWriter=null;
			}
			
			try {
				stmt.close();
			} catch (SQLException ex) {
				logger.error("SQLException while closing statement", ex);
			}			
			
			// show them what they've won!	
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String message =  "Successfully executed "+stmtsCompleted+" out of "+stmtsTried+" statements.";
					if (stmtsCompleted == 0 && stmtsTried > 0) {
						message += ("\nBetter luck next time!");
					}
					JOptionPane.showMessageDialog(dialog, message);
				}
			});
			
			finished = true;
			
			SwingUtilities.invokeLater(new Runnable() {public void run(){runFinished();}});
		}
		
		/**
		 * Displays error messages or invokes the next process in the chain on a new
		 * thread. The run method asks swing to invoke this method on the event dispatch
		 * thread after it's done.
		 */
		public void runFinished() {
			if (allIsWell) {
				if (nextProcess != null) {
					new Thread(nextProcess).start();
				}
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
		/**
		 * @return Returns the hasStarted.
		 */
		public boolean hasStarted() {
			return hasStarted;
		}
		/**
		 * @param hasStarted The hasStarted to set.
		 */
		public void setHasStarted(boolean hasStarted) {
			this.hasStarted = hasStarted;
		}
	}

	public static class DDLWarningTableModel extends AbstractTableModel {
		protected List warnings;

		public DDLWarningTableModel(List warnings) {
			this.warnings = warnings;
		}

		public int getRowCount() {
			return warnings.size();
		}

		public int getColumnCount() {
			return 5;
		}

		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return "Parent";
			case 1:
				return "Name";
			case 2:
				return "Warning Type";
			case 3:
				return "Old Value";
			case 4:
				return "New Value";
			default:
				throw new IndexOutOfBoundsException("Requested column name "+columnIndex+" of "+getColumnCount());
			}
		}

		public Object getValueAt(int row, int column) {
			DDLWarning w = (DDLWarning) warnings.get(row);
			switch(column) {
			case 0:
				return w.getSubject().getParent().getParent(); // don't know how reliable this will be...
			case 1:
				return w.getSubject().getName();
			case 2:
				return w.getReason();
			case 3:
				return w.getOldValue();
			case 4:
				return w.getNewValue();
			default:
				throw new IndexOutOfBoundsException("Requested column "+column+" of "+getColumnCount());
			}
		}
	}
}
