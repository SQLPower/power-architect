package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
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

			final JButton executeButton = new JButton("Execute");
			executeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SQLDatabase target = architectFrame.playpen.getDatabase();
						Connection con;
						Statement stmt;
						try {
							con = target.getConnection();
						} catch (ArchitectException ex) {
							JOptionPane.showMessageDialog
								(d, "Couldn't connect to target database: "+ex.getMessage()
								 +"\nPlease check the connection settings and try again.");
							architectFrame.getMainInstance().playpen.showDbcsDialog();
							return;
						} catch (Exception ex) {
							JOptionPane.showMessageDialog
								(d, "You have to specify a target database connection"
								 +"\nbefore executing this script.");
							logger.error("Unexpected exception in DDL generation", ex);
							architectFrame.getMainInstance().playpen.showDbcsDialog();
							return;
						}

						List statements;
						try {
							stmt = con.createStatement();
							statements = ddlg.generateDDLStatements(target);
						} catch (SQLException ex) {
							JOptionPane.showMessageDialog
								(d, "Couldn't generate DDL statements: "+ex.getMessage()
								 +"\nThe problem was reported by the target database.");
							return;
						} catch (ArchitectException ex) {
							JOptionPane.showMessageDialog
								(d, "Couldn't generate DDL statements: "+ex.getMessage()
								 +"\nThe problem was detected internally to the Architect.");
							return;
						}

						int stmtsTried = 0;
						int stmtsCompleted = 0;
						Iterator it = statements.iterator();
						while (it.hasNext()) {
							String sql = (String) it.next();
							try {
								stmtsTried++;
								stmt.executeUpdate(sql);
								stmtsCompleted++;
							} catch (SQLException ex) {
								int decision = JOptionPane.showConfirmDialog
									(d, "SQL statement failed: "+ex.getMessage()
									 +"\nThe statement was:\n"+sql+"\nDo you want to continue?",
									 "SQL Failure", JOptionPane.YES_NO_OPTION);
								if (decision == JOptionPane.NO_OPTION) {
									return;
								}
							}
						}

						try {
							stmt.close();
						} catch (SQLException ex) {
							logger.error("SQLException while closing statement", ex);
						}

						JOptionPane.showMessageDialog(d, "Successfully executed "+stmtsCompleted
													  +" out of "+stmtsTried+" statements.");
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
											
			final JButton cancelButton = new JButton("Cancel");
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
