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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.ConflictResolver;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticismBucket;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DDLExportPanel;
import ca.sqlpower.architect.swingui.SQLScriptDialog;
import ca.sqlpower.architect.swingui.critic.CriticSwingUtil;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.table.TableUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ExportDDLAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(ExportDDLAction.class);

	private static final String GENDDL_WARNINGS_EXPLANATION =
        Messages.getString("ExportDDLAction.errorsInstructions"); //$NON-NLS-1$
	
	private JDialog d;

	public ExportDDLAction(final ArchitectSwingSession session) {
		super(session, Messages.getString("ExportDDLAction.name"), Messages.getString("ExportDDLAction.description"), "fwdSQL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

    public void actionPerformed(ActionEvent e) {
        final DDLExportPanel ddlPanel = new DDLExportPanel(session);

        Callable<Boolean> okCall, cancelCall;
        okCall = new Callable<Boolean>() {
            public Boolean call() {
                if (ddlPanel.applyChanges()) {

                    DDLGenerator ddlg = ddlPanel.getGenerator();
                    ddlg.setTargetSchema(ddlPanel.getSchemaField().getText());

                    checkErrorsAndGenerateDDL(ddlg);

                }
                return Boolean.TRUE;
            }

            /**
             * This method will run the known critics over the target database
             * that we want to generate a DDL script for and display a dialog
             * containing errors if any are found. The user will then have the
             * choice to fix their data model or continue on ignoring the
             * current set of errors.
             * <p>
             * This method will also generate the DDL script using the
             * generateAndDisplayDDL method.
             */
            private void checkErrorsAndGenerateDDL(final DDLGenerator ddlg) {
                List<Criticism> criticisms = session.getWorkspace().getCriticManager().
                    criticize(ddlg.getClass(), session.getTargetDatabase());
                if (criticisms.isEmpty()) {
                    try {
                        generateAndDisplayDDL(ddlPanel, ddlg);
                    } catch (Exception ex) {
                        ASUtils.showExceptionDialog
                        (session,
                         Messages.getString("ExportDDLAction.errorGeneratingDDLScript"), ex); //$NON-NLS-1$
                    }
                } else {
                    //build warning dialog
                    final JDialog warningDialog = new JDialog(frame);
                    JPanel mainPanel = new JPanel();
                    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref"), mainPanel);
                    builder.setDefaultDialogBorder();
                    JTextArea explanation = new JTextArea(GENDDL_WARNINGS_EXPLANATION, 5, 60);
                    explanation.setLineWrap(true);
                    explanation.setWrapStyleWord(true);
                    explanation.setEditable(false);
                    explanation.setBackground(mainPanel.getBackground());
                    builder.append(explanation);
                    builder.nextLine();
                    
                    final CriticismBucket bucket = new CriticismBucket();
                    bucket.updateCriticismsToMatch(criticisms);
                    JTable errorTable = CriticSwingUtil.createCriticTable(session, bucket);
                    builder.append(new JScrollPane(errorTable));
                    builder.nextLine();
                    
                    JButton quickFixButton = new JButton(
                            new AbstractAction(Messages.getString("ExportDDLAction.quickFixAllOption")) {  //$NON-NLS-1$
                        public void actionPerformed(ActionEvent e) {
                            warningDialog.dispose();
                            for (Criticism criticism : bucket.getCriticisms()) {
                                if (!criticism.getFixes().isEmpty()) {
                                    //applying the first one each time as there is no 
                                    //decision what to apply by the user for this case
                                    criticism.getFixes().get(0).apply();
                                }
                            }
                            checkErrorsAndGenerateDDL(ddlg);
                        }
                    });
                    JButton ignoreButton = new JButton(new AbstractAction(
                            Messages.getString("ExportDDLAction.ignoreWarningsOption")) { //$NON-NLS-1$
                        public void actionPerformed(ActionEvent e) {
                            warningDialog.dispose();
                            try {
                                generateAndDisplayDDL(ddlPanel, ddlg);
                            } catch (Exception ex) {
                                ASUtils.showExceptionDialog
                                (session,
                                 Messages.getString("ExportDDLAction.errorGeneratingDDLScript"), ex); //$NON-NLS-1$
                            }                        }
                    });
                    JButton cancelButton = new JButton(new AbstractAction(
                            Messages.getString("ExportDDLAction.cancelOption")) { //$NON-NLS-1$
                        public void actionPerformed(ActionEvent e) {
                            //just dispose of the dialog and end this.
                            warningDialog.dispose();
                        }
                    });
                    JButton recheckButton = new JButton(new AbstractAction(
                            Messages.getString("ExportDDLAction.recheckOption")) { //$NON-NLS-1$
                        public void actionPerformed(ActionEvent e) {
                            warningDialog.dispose();
                            checkErrorsAndGenerateDDL(ddlg);
                        }
                    });
                    
                    ButtonBarBuilder buttonBar = new ButtonBarBuilder();
                    buttonBar.addGlue();
                    buttonBar.addGriddedButtons(new JButton[] {quickFixButton, 
                            ignoreButton, cancelButton, recheckButton});
                    
                    builder.append(buttonBar.getPanel());
                    warningDialog.add(mainPanel);
                    
                    warningDialog.pack();
                    TableUtils.fitColumnWidths(errorTable, 10);
                    warningDialog.setLocationRelativeTo(frame);
                    warningDialog.setVisible(true);
                }
            }

            /**
             * This method is used for generating and displaying the DDL script
             * for the current target database using the given DDL generator.
             */
            private void generateAndDisplayDDL(final DDLExportPanel ddlPanel, DDLGenerator ddlg) throws SQLException,
            SQLObjectException {
                ddlg.generateDDLScript(session.getTargetDatabase().getTables());

                SQLDatabase ppdb = new SQLDatabase(ddlPanel.getTargetDB());
                SQLScriptDialog ssd =
                    new SQLScriptDialog(d, Messages.getString("ExportDDLAction.previewSQLScriptDialogTitle"), "", false, //$NON-NLS-1$ //$NON-NLS-2$
                            ddlg,
                            ppdb.getDataSource(),
                            true,
                            session);
                SPSwingWorker scriptWorker = ssd.getExecuteTask();
                ConflictFinderProcess cfp = new ConflictFinderProcess(ssd, ppdb, ddlg, ddlg.getDdlStatements(), session);
                ConflictResolverProcess crp = new ConflictResolverProcess(ssd, cfp, session);
                cfp.setNextProcess(crp);
                crp.setNextProcess(scriptWorker);
                ssd.setExecuteTask(cfp);
                ssd.setVisible(true);
            }
        };

        cancelCall = new Callable<Boolean>() {
            public Boolean call() {
                ddlPanel.discardChanges();
                return Boolean.TRUE;
            }
        };
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                ddlPanel,
                frame,
                Messages.getString("ExportDDLAction.forwardEngineerSQLDialogTitle"), Messages.getString("ExportDDLAction.okOption"), //$NON-NLS-1$ //$NON-NLS-2$
                okCall, cancelCall);

        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

	/**
	 * The ConflictFinderProcess uses a ConflictResolver (which it monitors with
	 * a progress bar) to locate objects in the target database which need to be
	 * removed before a set of DDL statements can be executed in it.
	 *
	 * @author fuerth
	 */
	public class ConflictFinderProcess extends SPSwingWorker {

		JDialog parentDialog;
		SQLDatabase target;
		DDLGenerator ddlg;
		List<DDLStatement> statements;


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

		/**
		 * @param parentDialog The JDialog we're doing this in.
		 * @param target The target database (where to search for the conflicts).
		 * @param ddlg The DDL Generator that we're using.
		 * @throws SQLObjectException If there is a problem connecting to the target database
		 * @throws SQLException If the conflict resolver chokes
		 */
		public ConflictFinderProcess(JDialog parentDialog, SQLDatabase target,
				DDLGenerator ddlg, List<DDLStatement> statements, ArchitectSwingSession session)
			throws SQLObjectException {
			super(session);
			this.parentDialog = parentDialog;
			this.target = target;
			this.ddlg = ddlg;
			this.statements = statements;

			cr = new ConflictResolver(target, ddlg, statements);
		}

		/**
		 * @return True if and only if the user has asked for the conflicts to
		 *         be deleted.
		 */
		public boolean doesUserWantToDropConflicts() {
			return shouldDropConflicts;
		}

		/**
		 * This method is called on its own thread (not the AWT event dispatch
		 * thread). It will take a while.
		 */
		public void doStuff() {

			if (this.isCancelled()) return;
            
            // First, test if it's possible to connect to the target database
            Connection con = null;
            try {
                con = target.getConnection();
            } catch (Exception ex) {
                error = ex;
                errorMessage = Messages.getString("ExportDDLAction.failedToConnectToDb"); //$NON-NLS-1$
                return;
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        logger.error("Failed to close connection. This exception is getting squashed:", ex); //$NON-NLS-1$
                    }
                }
            }
            
            // Now do the actual work
			try {
				cr.findConflicting();
			} catch (Exception ex) {
				error = ex;
				errorMessage = Messages.getString("ExportDDLAction.unexpectedException"); //$NON-NLS-1$
				logger.error("Unexpected exception setting up DDL generation", ex); //$NON-NLS-1$
			}
		}

		/**
		 * After the doStuff() method is done, this method will be invoked on the AWT event
		 * dispatch thread.
		 */
		public void cleanup() {
			if (!SwingUtilities.isEventDispatchThread()) {
				logger.error("runFinished is running on the wrong thread!"); //$NON-NLS-1$
			}
			if (errorMessage != null) {
                if (error != null) {
                    ASUtils.showExceptionDialogNoReport(parentDialog, errorMessage, error);
                } else {
                    JOptionPane.showMessageDialog(parentDialog, errorMessage, Messages.getString("ExportDDLAction.errorMessageDialogTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
                }
			} else if (!cr.isEmpty()) {
				Object[] messages = new Object[3];
				messages[0] = Messages.getString("ExportDDLAction.conflictingObjectsInDatabase"); //$NON-NLS-1$
				JTextArea conflictsPane = new JTextArea(cr.toConflictTree());
				conflictsPane.setRows(15);
				conflictsPane.setEditable(false);
				messages[1] = new JScrollPane(conflictsPane);
				messages[2] = Messages.getString("ExportDDLAction.dropConflictingObjectsConfirmation"); //$NON-NLS-1$
				int choice = JOptionPane.showConfirmDialog(
						parentDialog,
						messages,
						Messages.getString("ExportDDLAction.conflictingObjectsInDatabaseDialogTitle"), //$NON-NLS-1$
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					shouldDropConflicts = true;
				} else if (choice == JOptionPane.NO_OPTION) {
					shouldDropConflicts = false;
				} else if (choice == JOptionPane.CANCEL_OPTION) {
					shouldDropConflicts = false;
					this.setCancelled(true);
				}
			}

		}

		public ConflictResolver getConflictResolver() {
			return cr;
		}


		@Override
		protected Integer getJobSizeImpl() {
			return cr.getJobSize();
		}

		@Override
		protected String getMessageImpl() {
			return cr.getMessage();
		}

		@Override
		protected int getProgressImpl() {
			return cr.getProgress();
		}

		@Override
		protected boolean hasStartedImpl() {
			return cr.hasStarted();
		}

		@Override
		protected boolean isFinishedImpl() {
			return cr.isFinished();
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
	public class ConflictResolverProcess extends SPSwingWorker {

		private JDialog parentDialog;
		private ConflictFinderProcess conflictFinder;

		private ConflictResolver cr;

		private String errorMessage;
		private Exception error;

		/**
		 * @param d The dialog we anchor popup messages to
		 * @param cfp The conflict finder we extract the conflict list from
		 * @param progressBar The progress bar we show our progress in
		 * @param progressLabel The label where we say what we're doing
		 */
		public ConflictResolverProcess(JDialog d, ConflictFinderProcess cfp, ArchitectSwingSession session) {
			super(session);
            this.parentDialog = d;
			this.conflictFinder = cfp;
		}

		public void doStuff() {
			if (isCancelled())
				return;
			if (conflictFinder.doesUserWantToDropConflicts()) {
				cr = conflictFinder.getConflictResolver();
				cr.aboutToCallDropConflicting();
				try {
					cr.dropConflicting();
				} catch (Exception ex) {
					logger.error("Error while dropping conflicting objects", ex); //$NON-NLS-1$
					errorMessage = Messages.getString("ExportDDLAction.errorDroppingConflictingObjects")+ex.getMessage(); //$NON-NLS-1$
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
				ASUtils.showExceptionDialogNoReport(parentDialog,
                    Messages.getString("ExportDDLAction.errorDroppingConflictingObjects")+errorMessage, error); //$NON-NLS-1$
				setCancelled(true);
			}
		}

		@Override
		protected Integer getJobSizeImpl() {
			return cr.getJobSize();
		}

		@Override
		protected String getMessageImpl() {
			return cr.getMessage();
		}

		@Override
		protected int getProgressImpl() {
			return cr.getProgress();
		}

		@Override
		protected boolean hasStartedImpl() {
			return cr.hasStarted();
		}

		@Override
		protected boolean isFinishedImpl() {
			return cr.isFinished();
		}


	}


}
