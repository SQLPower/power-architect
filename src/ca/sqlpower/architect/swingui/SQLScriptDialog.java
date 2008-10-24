/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.LogWriter;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SQLScriptDialog extends JDialog {

	private static final Logger logger = Logger.getLogger(SQLScriptDialog.class);

	private List<DDLStatement> statements;
	private JProgressBar progressBar = new JProgressBar();

	private Component parent;
	private String header;

	private JLabel statusLabel;

	private SPDataSource targetDataSource;

	private JTextPane sqlScriptArea;
	private AbstractDocument sqlDoc;

	private boolean closeParent;
    
    private ArchitectSwingSession session;

	private MonitorableWorker executeTask;

	public SQLScriptDialog(Dialog owner, String title, String header, boolean modal,
			DDLGenerator gen, SPDataSource targetDataSource,
			boolean closeParent, ArchitectSwingSession session )
			throws HeadlessException {
		super(owner, title, modal);
        if (modal && owner == null) {
            JOptionPane.showMessageDialog(null,
                "Debug: This action tried to create a null-parented modal dialog"); //$NON-NLS-1$
        }
		statusLabel = new JLabel();
		parent = owner;
		this.header = header;
		this.statements = gen.getDdlStatements();
		this.targetDataSource = targetDataSource;
		this.closeParent = closeParent;
        this.session = session;
        this.executeTask = new ExecuteSQLScriptWorker(session);
		logger.info("The list size is :" + statements.size()); //$NON-NLS-1$
		add(buildPanel());
		pack();
		setLocationRelativeTo(parent);
	}

	private JPanel buildPanel() {
		FormLayout sqlLayout = new FormLayout(
				"4dlu, min:grow, 4dlu", //columns //$NON-NLS-1$
				"pref, 4dlu, pref, 6dlu, fill:300dlu:grow,6dlu, pref, 6dlu, pref"); //rows //$NON-NLS-1$

		CellConstraints cc = new CellConstraints();

		sqlDoc = new DefaultStyledDocument();

		SimpleAttributeSet att = new SimpleAttributeSet();
		StyleConstants.setForeground(att, Color.black);

		for (DDLStatement ddl : statements){
			try {
				sqlDoc.insertString(sqlDoc.getLength(), ddl.getSQLText()+ddl.getSqlTerminator(), att);
			} catch(BadLocationException e) {
				ASUtils.showExceptionDialogNoReport(parent,
						Messages.getString("SQLScriptDialog.couldNotCreateDocument"), e); //$NON-NLS-1$
				logger.error("Could not create document for results", e); //$NON-NLS-1$
			}
		}
		sqlScriptArea = new JTextPane();
		sqlScriptArea.setMargin(new Insets(6, 10, 4, 6));
		sqlScriptArea.setDocument(sqlDoc);
		sqlScriptArea.setEditable(false);
		sqlScriptArea.setAutoscrolls(true);
		JScrollPane sp = new JScrollPane(sqlScriptArea);

		Action copy = new CopyAction(sqlDoc);
		Action execute = null;
		
		logger.debug(targetDataSource.get(SPDataSource.PL_UID)); //$NON-NLS-1$
		
		execute = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
			    if (targetDataSource.get(SPDataSource.PL_UID) != null) {
			        new Thread(executeTask).start();
			        ProgressWatcher.watchProgress(progressBar, executeTask, statusLabel);
			    } else {
			        JOptionPane.showMessageDialog(SQLScriptDialog.this, 
			                Messages.getString("SQLScriptDialog.noTargetDb"), //$NON-NLS-1$
			                Messages.getString("SQLScriptDialog.couldNotExecuteDialogTitle"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			    }
			}
		};

		Action save = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {

				logger.info( "SQL_FILE_FILTER:"+ ((FileExtensionFilter) SPSUtils.SQL_FILE_FILTER).toString()); //$NON-NLS-1$

				SPSUtils.saveDocument(parent,sqlDoc,
						(FileExtensionFilter) SPSUtils.SQL_FILE_FILTER );
			}
		};
		CloseAction close = new CloseAction();
		close.setWhatToClose(this);


		ButtonBarBuilder barBuilder = new ButtonBarBuilder();
		JButton copyButton = new JButton(copy);
		copyButton.setText(Messages.getString("SQLScriptDialog.copyOption")); //$NON-NLS-1$
		barBuilder.addGridded (copyButton);
		barBuilder.addRelatedGap();
		barBuilder.addGlue();

		JButton executeButton = new JButton(execute);
		executeButton.setText(Messages.getString("SQLScriptDialog.executeOption")); //$NON-NLS-1$
		barBuilder.addGridded(executeButton);
		barBuilder.addRelatedGap();
		barBuilder.addGlue();

		JButton saveButton = new JButton(save);
		saveButton.setText(Messages.getString("SQLScriptDialog.saveOption")); //$NON-NLS-1$
		barBuilder.addGridded(saveButton);
		barBuilder.addRelatedGap();
		barBuilder.addGlue();

		addWindowListener(new CloseWindowAction());
		JButton closeButton = new JButton(close);
		closeButton.setText(Messages.getString("SQLScriptDialog.closeOption")); //$NON-NLS-1$
		barBuilder.addGridded(closeButton);

		PanelBuilder pb;

		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(sqlLayout) : new JPanel(sqlLayout);
		pb = new PanelBuilder(sqlLayout, panel);
		pb.setDefaultDialogBorder();
		pb.add(new JLabel(header), cc.xy(2, 1));

		if (targetDataSource != null) {
			pb.add(new JLabel(Messages.getString("SQLScriptDialog.yourTargetDbIs")+ targetDataSource.getName() ), cc.xy(2, 3)); //$NON-NLS-1$
		}
		pb.add(sp, cc.xy(2, 5));
    		pb.add(barBuilder.getPanel(), cc.xy(2, 7, "c,c")); //$NON-NLS-1$
		pb.add(progressBar, cc.xy(2, 9));

		return pb.getPanel();
	}

	public MonitorableWorker getExecuteTask() {
		return executeTask;
	}

	/**
	 * Changes the task that will be invoked by the "execute" button on this
	 * dialog.  If you want your task to run before the normal script execution,
	 * you should call {@link #getExecuteTask()} and chain that task onto the
	 * one you specify here.
	 *
	 * @param v The task to execute when the "execute" button is clicked.
	 */
	public void setExecuteTask(MonitorableWorker v) {
		executeTask = v;
	}

	// ============== Nested classes follow ================

	private class CopyAction extends AbstractAction {

		AbstractDocument doc;
		public CopyAction(AbstractDocument doc) {
			this.doc = doc;
		}

		public void actionPerformed(ActionEvent e) {

			try {
				StringSelection selection = new StringSelection(doc.getText(0,doc.getLength()));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,selection);
			} catch (BadLocationException e1) {
				logger.debug("Unable to get the text for copying"+ e1); //$NON-NLS-1$
			}

		}
	}
	
	/**
	 * An action that will close the parent dialog when this window
	 * is closed iff the closeParent variable was set when the dialog 
	 * was constructed 
	 *
	 */
	private class CloseWindowAction extends WindowAdapter {
	        public void windowClosing(WindowEvent e) {
	            if (closeParent) {
	                parent.setVisible(false);
	            }
	        }
	}

	private class CloseAction extends AbstractAction {

		Component c;

		public void setWhatToClose(Component c){
			this.c = c;
		}
		public void actionPerformed(ActionEvent e) {
			c.setVisible(false);
			if ( closeParent )
				parent.setVisible(false);
		}
	}

	private class ExecuteSQLScriptWorker extends MonitorableWorker {

        private int stmtsTried = 0;
		private int stmtsCompleted = 0;
		private boolean finished = false;
		private boolean hasStarted = false;

        public ExecuteSQLScriptWorker(ArchitectSwingSession session) {
		    super(session);
		}

		/**
		 * This method runs on a separate worker thread.
		 */
		public void doStuff() {

			finished = false;
			setCancelled(false);
			hasStarted = true;
			if (isCanceled() || finished) return;

			SQLDatabase target = new SQLDatabase(targetDataSource);
			statusLabel.setText(Messages.getString("SQLScriptDialog.creatingObjectsInTargetDb") + target.getDataSource() ); //$NON-NLS-1$
			stmtsTried = 0;
			stmtsCompleted = 0;

			logger.debug("the Target Database is: " + target.getDataSource()); //$NON-NLS-1$

			Connection con;
			Statement stmt;

			try {
				con = target.getConnection();
			} catch (ArchitectException ex) {
				finished = true;
				throw new RuntimeException(
						Messages.getString("SQLScriptDialog.couldNotConnectToTargetDb", ex.getMessage()), ex); //$NON-NLS-1$
			} catch (Exception ex) {
				finished = true;
				logger.error("Unexpected exception in DDL generation", ex); //$NON-NLS-1$
				throw new RuntimeException(Messages.getString("SQLScriptDialog.specifyATargetDb")); //$NON-NLS-1$
			}

			try {
				logger.debug("the connection thinks it is: " + con.getMetaData().getURL()); //$NON-NLS-1$
				stmt = con.createStatement();
			} catch (SQLException ex) {
				finished = true;
				throw new RuntimeException(Messages.getString("SQLScriptDialog.couldNotGenerateDDL", ex.getMessage())); //$NON-NLS-1$
			}

			LogWriter logWriter = null;

			try {
				logWriter = new LogWriter(session.getUserSettings().getDDLUserSettings().getString(DDLUserSettings.PROP_DDL_LOG_PATH,"")); //$NON-NLS-1$
			} catch (ArchitectException ex) {
				finished = true;
				final Exception fex = ex;
				throw new RuntimeException(Messages.getString("SQLScriptDialog.problemWithDDLLog") //$NON-NLS-1$
					+ fex.getMessage());
			}

			try {
				logWriter.info("Starting DDL Generation at " + new java.util.Date(System.currentTimeMillis())); //$NON-NLS-1$
				logWriter.info("Database Target: " + target.getDataSource()); //$NON-NLS-1$
				logWriter.info("Playpen Dump: " + target.getDataSource()); //$NON-NLS-1$
				Iterator it = statements.iterator();
				while (it.hasNext() && !finished && !isCanceled()) {
					DDLStatement ddlStmt = (DDLStatement) it.next();
					try {
						stmtsTried++;
						logWriter.info("executing: " + ddlStmt.getSQLText()); //$NON-NLS-1$
						stmt.executeUpdate(ddlStmt.getSQLText());
						stmtsCompleted++;
					} catch (SQLException ex) {
						final Exception fex = ex;
						final String fsql = ddlStmt.getSQLText();
						final LogWriter fLogWriter = logWriter;
						logWriter.info("sql statement failed: " + ex.getMessage()); //$NON-NLS-1$
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									JTextArea jta = new JTextArea(fsql,25,40);
									jta.setEditable(false);
									JScrollPane jsp = new JScrollPane(jta);
									JLabel errorLabel = new JLabel("<html>" + Messages.getString("SQLScriptDialog.sqlStatementFailed", fex.getMessage() + "<p>") + "</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									JPanel jp = new JPanel(new BorderLayout());
									jp.add(jsp,BorderLayout.CENTER);
									jp.add(errorLabel,BorderLayout.SOUTH);
									int decision = JOptionPane.showConfirmDialog
									(SQLScriptDialog.this, jp, Messages.getString("SQLScriptDialog.sqlFailure"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
									if (decision == JOptionPane.NO_OPTION) {
										fLogWriter.info("Export cancelled by user."); //$NON-NLS-1$
										cancelJob();
									}
								}
							});
						} catch (InterruptedException ex2) {
							logger.warn("DDL Worker was interrupted during InvokeAndWait", ex2); //$NON-NLS-1$
						} catch (InvocationTargetException ex2) {
							throw new RuntimeException(ex2);
						}

						if (isCanceled()) {
							finished = true;
							// don't return, we might as well display how many statements ended up being processed...
						}
					}
				}

			} catch (Exception exc){
				logWriter.info("Caught Unexpected Exception " + exc); //$NON-NLS-1$
				ASUtils.showExceptionDialog(
						session,
						Messages.getString("SQLScriptDialog.couldNotFinishSQL"), //$NON-NLS-1$
						exc);
			} finally {
				final String resultsMessage =
					(stmtsCompleted == 0 ? Messages.getString("SQLScriptDialog.didNotExecute", String.valueOf(stmtsTried)) : //$NON-NLS-1$
						Messages.getString("SQLScriptDialog.successfullyExecuted", String.valueOf(stmtsCompleted), String.valueOf(stmtsTried))); //$NON-NLS-1$
				logWriter.info(resultsMessage);
				JOptionPane.showMessageDialog(SQLScriptDialog.this, resultsMessage);
				// flush and close the LogWriter
				logWriter.flush();
				logWriter.close();
				try {
					if (stmt != null) stmt.close();
				} catch (SQLException ex) {
					logger.error("SQLException while closing statement", ex); //$NON-NLS-1$
				}
				try {
					if (con != null) con.close();
				} catch (SQLException ex) {
					logger.error("Couldn't close connection", ex); //$NON-NLS-1$
				}
			}

			finished = true;

		}

		/**
		 * Displays error messages or invokes the next process in the chain on a new
		 * thread. The run method asks swing to invoke this method on the event dispatch
		 * thread after it's done.
		 */
		public void cleanup() {

		}



		// ============= Monitorable Interface =============

		public Integer getJobSize() {
			if (statements != null) {
				return new Integer(statements.size());
			} else {
				return null;
			}
		}

		public int getProgress() {
			return stmtsTried;
		}

		public boolean isFinished() {
			return finished;
		}

		public String getMessage() {
			return null;
		}

		public void cancelJob() {
			this.setCancelled(true);
			finished = true;
		}


		public boolean hasStarted() {
			return hasStarted;
		}

		public void setHasStarted(boolean hasStarted) {
			this.hasStarted = hasStarted;
		}
	}
}
