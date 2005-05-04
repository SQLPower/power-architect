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
						ddlPanel.applyChanges();
						showPreview(architectFrame.project.getDDLGenerator(), d);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export DDL: "+ex.getMessage());
						logger.error("Got exception while exporting DDL", ex);

						// XXX: this won't always be the appropriate reaction.
						// should have a separate exception for "connection problems"
						ArchitectFrame.getMainInstance().playpen.showDbcsDialog();
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
			final JLabel progressLabel = new JLabel("Excecuting Statements...");
			progressLabel.setVisible(false);
			progressPanel.add(progressLabel);
			buttonPanel.add(progressPanel);

			final JButton executeButton = new JButton("Execute");
			executeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					    ExecuteDDLWorker worker = new ExecuteDDLWorker(d,ddlg);
    	                ProgressWatcher watcher = new ProgressWatcher(progressBar,worker,progressLabel);
						new javax.swing.Timer(50, watcher).start();
						new Thread(worker).start();								
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

	protected class ExecuteDDLWorker implements Monitorable {		
		JDialog dialog;
		List statements;
		int stmtsTried = 0;
		int stmtsCompleted = 0;
		boolean finished = false;
		boolean cancelled = false;
		GenericDDLGenerator ddlg;
		
		
		public ExecuteDDLWorker (JDialog dialog, GenericDDLGenerator ddlg) {
			this.dialog = dialog;
			this.ddlg = ddlg;
		}		

		public int getJobSize() throws ArchitectException {			
			if (statements != null) {
				return statements.size();
			} else {
				return 1000; // avoid divide by zero showing up in UI
			}
		}
		
		public int getProgress() throws ArchitectException {
			return stmtsTried;			
		}
		
		public boolean isFinished() throws ArchitectException {
			return finished;
		}

		public void cancelJob() {
			cancelled = true;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public void run() {	        

			finished = false;
			stmtsTried = 0;
			stmtsCompleted = 0;
			SQLDatabase target = architectFrame.playpen.getDatabase();
			Connection con;
			Statement stmt;

			try {
				con = target.getConnection();
			} catch (ArchitectException ex) {
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
				stmt = con.createStatement();
				statements = ddlg.generateDDLStatements(target);
			} catch (SQLException ex) {
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
			} catch (ArchitectException ex) {
				final Exception fex = ex;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog
							(dialog, "Couldn't generate DDL statements: "+fex.getMessage()
							 +"\nThe problem was detected internally to the Architect.");
					}
				});								
				finished = true;
				return;
			}
            
			Iterator it = statements.iterator();
			while (it.hasNext() && !finished) {
				String sql = (String) it.next();
				try {
					stmtsTried++;
					stmt.executeUpdate(sql);
					stmtsCompleted++;
				} catch (SQLException ex) {
					final Exception fex = ex;
					final String fsql = sql;
					try {
						SwingUtilities.invokeAndWait(new Runnable() {						
							// FIXME: this is definitely not going to work.  Need to figure out a way to 
                    	    // stop the worker thread from running until the result of this Dialog has returned...
							public void run() {
								int decision = JOptionPane.showConfirmDialog
									(dialog, "SQL statement failed: "+fex.getMessage()
									 +"\nThe statement was:\n"+fsql+"\nDo you want to continue?",
									 "SQL Failure", JOptionPane.YES_NO_OPTION);
								if (decision == JOptionPane.NO_OPTION) {
									cancelJob();
								}
							}
						});
					} catch (InterruptedException ex2) {
					} catch (InvocationTargetException ex2) {
						final Exception fex2 = ex2;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
							JOptionPane.showMessageDialog
								(dialog, "worker thread died: "+fex2.getMessage());
							}
						});
					}
								
					if (isCancelled()) {
						finished = true;
						// don't return, we might as well display how many statements ended up being processed...
					}
				}
			}
            
			try {
				stmt.close();
			} catch (SQLException ex) {
				logger.error("SQLException while closing statement", ex);
			}

            
			// show them what they've won!	
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(dialog, "Successfully executed "+stmtsCompleted
												  +" out of "+stmtsTried+" statements.");
				}
			});						
           	
			finished = true;
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
			return 3;
		}

		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return "Warning Type";
			case 1:
				return "Old Value";
			case 2:
				return "New Value";
			default:
				throw new IndexOutOfBoundsException("Requested column name "+columnIndex+" of "+getColumnCount());
			}
		}

		public Object getValueAt(int row, int column) {
			DDLWarning w = (DDLWarning) warnings.get(row);
			switch(column) {
			case 0:
				return w.getReason();
			case 1:
				return w.getOldValue();
			case 2:
				return w.getNewValue();
			default:
				throw new IndexOutOfBoundsException("Requested column "+column+" of "+getColumnCount());
			}
		}
	}
}
