package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.ConflictResolver;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLWarning;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.ddl.NameChangeWarning;

public class ExportDDLAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(ExportDDLAction.class);

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
							
							GenericDDLGenerator ddlg = architectFrame.project.getDDLGenerator();
							ddlg.setTargetSchema(ddlPanel.schemaField.getText());
							
							StringBuffer ddl = ddlg.generateDDL(architectFrame.playpen.getDatabase());
							List warnings = ddlg.getWarnings();
							if (warnings.size() > 0) {
								TableSorter sorter = new TableSorter(new DDLWarningTableModel(warnings));
								JTable warningTable = new JTable(sorter);
								sorter.setTableHeader(warningTable.getTableHeader());
								JOptionPane.showMessageDialog(d, new JScrollPane(warningTable), "Warnings in generated DDL", JOptionPane.WARNING_MESSAGE);
							}
							
							SQLDatabase ppdb = ArchitectFrame.getMainInstance().playpen.getDatabase();
							SQLScriptDialog ssd = 
								new SQLScriptDialog(d, "Preview SQL Script", "", false,
									ddlg.getDdlStatements(),
									ppdb.getDataSource(),
									true);
							MonitorableWorker scriptWorker = ssd.getExecuteTask();
							ConflictFinderProcess cfp = new ConflictFinderProcess(ssd, ppdb, ddlg, ddlg.getDdlStatements());
							ConflictResolverProcess crp = new ConflictResolverProcess(ssd, cfp);
							cfp.setNextProcess(crp);
							crp.setNextProcess(scriptWorker);
							ssd.setExecuteTask(cfp);
							ssd.setVisible(true);
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

	/**
	 * The ConflictFinderProcess uses a ConflictResolver (which it monitors with
	 * a progress bar) to locate objects in the target database which need to be
	 * removed before a set of DDL statements can be executed in it.
	 * 
	 * @author fuerth
	 */
	private class ConflictFinderProcess extends MonitorableWorker {

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
		 * @throws ArchitectException If there is a problem connecting to the target database
		 * @throws SQLException If the conflict resolver chokes
		 */
		public ConflictFinderProcess(JDialog parentDialog, SQLDatabase target,
				DDLGenerator ddlg, List statements)
			throws ArchitectException, SQLException {
			super();
			this.parentDialog = parentDialog;
			this.target = target;
			this.ddlg = ddlg;
			this.statements = statements;

			Connection con = target.getConnection();
			cr = new ConflictResolver(con, ddlg, statements);
		}
		
		/**
		 * @return True if and only if the user has asked for the conflicts to
		 *         be deleted.
		 */
		public boolean doesUserWantToDropConflicts() {
			return shouldDropConflicts;
		}

		/**
		 * This should run on its own thread (not the AWT event dispatch
		 * thread). It will take a while.
		 */
		public void doStuff() {
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
		}
		
		/**
		 * When the run() method is done, it schedules this method to be invoked on the AWT event
		 * dispatch thread.
		 */
		public void cleanup() {
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

		public Integer getJobSize() throws ArchitectException {
			return cr.getJobSize();
		}

		public String getMessage() {
			return cr.getMessage();
		}

		public int getProgress() throws ArchitectException {
			return cr.getProgress();
		}

		public boolean hasStarted() {
			return cr.hasStarted();
		}

		public boolean isFinished() throws ArchitectException {
			return cr.isFinished();
		}

		public void setCancelled(boolean cancelled) {
			cr.setCancelled(cancelled);
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
	private class ConflictResolverProcess extends MonitorableWorker {

		private JDialog parentDialog;
		private ConflictFinderProcess conflictFinder;

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
		public ConflictResolverProcess(JDialog d, ConflictFinderProcess cfp) {
			this.parentDialog = d;
			this.conflictFinder = cfp;
		}

		public void doStuff() {
			if (conflictFinder.doesUserWantToDropConflicts()) {
				cr = conflictFinder.getConflictResolver();
				cr.aboutToCallDropConflicting();
				try {
					cr.dropConflicting();
				} catch (SQLException ex) {
					logger.error("Error while dropping conflicting objects", ex);
					errorMessage = "Error while dropping conflicting objects:\n\n"+ex.getMessage();
					error = ex;
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

		public Integer getJobSize() throws ArchitectException {
			return cr.getJobSize();
		}

		public String getMessage() {
			return cr.getMessage();
		}

		public int getProgress() throws ArchitectException {
			return cr.getProgress();
		}

		public boolean hasStarted() {
			return cr.hasStarted();
		}

		public boolean isFinished() throws ArchitectException {
			return cr.isFinished();
		}

		public void setCancelled(boolean cancelled) {
			cr.setCancelled(cancelled);
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
			    if (w.getSubject().getParent() == null) {
			        return "(No Parent)";
			    } else {
			        return w.getSubject().getParent().getParent();
			    }
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
		
		public Class getColumnClass(int columnIndex) {
		    return String.class;
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			DDLWarning w = (DDLWarning) warnings.get(rowIndex);
		    return w instanceof NameChangeWarning && columnIndex == 4;
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			DDLWarning w = (DDLWarning) warnings.get(rowIndex);
			if (columnIndex == 4) {
			    w.setNewValue(aValue);
			}
		}
	}
}
