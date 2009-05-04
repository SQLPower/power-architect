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

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A collection of components that present a series of SQL statements (a
 * "script") to the user, and allows the user to execute the script against a
 * pre-arranged target database, copy it to the system clipboard, or save it to
 * a file.
 */
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

	private SPSwingWorker executeTask;

    /**
     * Creates and packs a new SQL script dialog, but does not display it. Call
     * setVisible(true) to show the dialog, which will appear over or near the
     * given owner.
     * 
     * @param owner
     *            The dialog that owns this dialog. Can be null only if this
     *            dialog is not modal.
     * @param title
     *            The text to show in the dialog's title bar.
     * @param header
     *            The text to show inside the dialog body above the SQL
     *            statements.
     * @param modal
     *            Incidates if the generated dialog should be application-modal.
     *            If this is true, the owner parameter must not be null.
     * @param gen
     *            The DDL generator that supplies the SQL script. The script
     *            will be obtained by a call to
     *            {@link DDLGenerator#getDdlStatements()}.
     *            XXX: this should be a List of DDLStatement instead
     * @param targetDataSource
     *            The database to execute the statements in. This can be null,
     *            in which case the execute button will not function. Save and
     *            copy will still work.
     * @param closeParent
     *            If true, this dialog's owner will be closed when this dialog
     *            closes. XXX: this is probably not the best place for this feature
     * @param session
     *            The session that provides the SPSwingWorkerRegistry.
     *            XXX: this should be specified as a SwingWorkerRegistry
     */
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
		SPSUtils.makeJDialogCancellable(this, close);

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
		getRootPane().setDefaultButton(executeButton);

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

	public SPSwingWorker getExecuteTask() {
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
	public void setExecuteTask(SPSwingWorker v) {
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

	private class ExecuteSQLScriptWorker extends SPSwingWorker {

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
			if (isCancelled() || finished) return;

			SQLDatabase target = new SQLDatabase(targetDataSource);
			statusLabel.setText(Messages.getString("SQLScriptDialog.creatingObjectsInTargetDb") + target.getDataSource() ); //$NON-NLS-1$
			stmtsTried = 0;
			stmtsCompleted = 0;

			logger.debug("the Target Database is: " + target.getDataSource()); //$NON-NLS-1$

			Connection con;
			Statement stmt;

			try {
				con = target.getConnection();
			} catch (SQLObjectException ex) {
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

			try {
				logger.info("Starting DDL Generation at " + new java.util.Date(System.currentTimeMillis())); //$NON-NLS-1$
				logger.info("Database Target: " + target.getDataSource()); //$NON-NLS-1$
				logger.info("Playpen Dump: " + target.getDataSource()); //$NON-NLS-1$
				Iterator<DDLStatement> it = statements.iterator();
				while (it.hasNext() && !finished && !isCancelled()) {
					DDLStatement ddlStmt = it.next();
					try {
						stmtsTried++;
						logger.info("executing: " + ddlStmt.getSQLText()); //$NON-NLS-1$
						stmt.executeUpdate(ddlStmt.getSQLText());
						stmtsCompleted++;
					} catch (final SQLException ex) {
						final String fsql = ddlStmt.getSQLText() == null ? null : ddlStmt.getSQLText().trim();
						logger.info("sql statement failed: " + ex.getMessage()); //$NON-NLS-1$
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									JTextArea jta = new JTextArea(fsql);
									jta.setOpaque(false);
									jta.setEditable(false);
									JPanel jp = new JPanel(new BorderLayout(0, 10));
									jp.add(new JLabel(Messages.getString("SQLScriptDialog.sqlStatementFailed", ex.getMessage())), BorderLayout.NORTH);
									jp.add(jta, BorderLayout.CENTER);
									jp.add(new JLabel(Messages.getString("SQLScriptDialog.continuePrompt")), BorderLayout.SOUTH);
									int decision = JOptionPane.showConfirmDialog(
									        SQLScriptDialog.this,
									        jp,
									        Messages.getString("SQLScriptDialog.sqlFailure"), //$NON-NLS-1$
									        JOptionPane.YES_NO_OPTION);
									if (decision == JOptionPane.NO_OPTION) {
										logger.info("Export cancelled by user."); //$NON-NLS-1$
										cancelJob();
									}
								}
							});
						} catch (InterruptedException ex2) {
							logger.warn("DDL Worker was interrupted during InvokeAndWait", ex2); //$NON-NLS-1$
						} catch (InvocationTargetException ex2) {
							throw new RuntimeException(ex2);
						}

						if (isCancelled()) {
							finished = true;
							// don't return, we might as well display how many statements ended up being processed...
						}
					}
				}

			} catch (Exception exc){
				logger.info("Caught Unexpected Exception " + exc); //$NON-NLS-1$
				ASUtils.showExceptionDialog(
						session,
						Messages.getString("SQLScriptDialog.couldNotFinishSQL"), //$NON-NLS-1$
						exc);
			} finally {
				final String resultsMessage =
					(stmtsCompleted == 0 ? Messages.getString("SQLScriptDialog.didNotExecute", String.valueOf(stmtsTried)) : //$NON-NLS-1$
						Messages.getString("SQLScriptDialog.successfullyExecuted", String.valueOf(stmtsCompleted), String.valueOf(stmtsTried))); //$NON-NLS-1$
				logger.info(resultsMessage);
				JOptionPane.showMessageDialog(SQLScriptDialog.this, resultsMessage);
				// flush and close the LogWriter
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
