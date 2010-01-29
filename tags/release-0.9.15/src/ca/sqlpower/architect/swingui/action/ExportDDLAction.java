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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.ConflictResolver;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLWarning;
import ca.sqlpower.architect.ddl.DDLWarningComponent;
import ca.sqlpower.architect.ddl.ObjectPropertyModificationDDLComponent;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DDLExportPanel;
import ca.sqlpower.architect.swingui.SQLScriptDialog;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSwingWorker;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;

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
                try {
                    if (ddlPanel.applyChanges()) {

                        DDLGenerator ddlg = session.getDDLGenerator();
                        ddlg.setTargetSchema(ddlPanel.getSchemaField().getText());
                        
                        boolean done = false;
                        while (!done) {
                            // generate DDL in order to come up with a list of warnings
                            ddlg.generateDDLScript(session.getTargetDatabase().getTables());
                            final List<DDLWarning> warnings = ddlg.getWarnings();
                            final JPanel outerPanel = new JPanel();
                            if (warnings.size() == 0) {
                                done = true;
                            } else {
                                final List<DDLWarningComponent> warningComponents = new ArrayList<DDLWarningComponent>();
                                DataEntryPanel dialogPanel = new DataEntryPanel() {
                                    
                                    public boolean applyChanges() {
                                        return false;
                                    }

                                    public void discardChanges() {
                                    }

                                    public JComponent getPanel() {
                                        outerPanel.setLayout(new BorderLayout());
                                        JTextArea explanation = new JTextArea(GENDDL_WARNINGS_EXPLANATION, 5, 60);
                                        explanation.setLineWrap(true);
                                        explanation.setWrapStyleWord(true);
                                        explanation.setEditable(false);
                                        explanation.setBackground(outerPanel.getBackground());
                                        outerPanel.add(explanation, BorderLayout.NORTH);
                                        JPanel listBoxPanel = new JPanel();
                                        listBoxPanel.setLayout(new GridLayout(0, 1, 5, 5));
                                       
                                        for (Object o : warnings) {
                                            DDLWarning ddlwarning = (DDLWarning) o;
                                            DDLWarningComponent ddlWarningComponent = new ObjectPropertyModificationDDLComponent(ddlwarning);
                                            listBoxPanel.add(ddlWarningComponent.getComponent());
                                            warningComponents.add(ddlWarningComponent);
                                        }
                                        
                                        JScrollPane sp = new JScrollPane(listBoxPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                                        if (warnings.size() > 9) {
                                            Dimension d = new Dimension(400,400);
                                            sp.setPreferredSize(d);
                                        }
                                        outerPanel.add(sp, BorderLayout.CENTER);
                                        return outerPanel;
                                    }

                                    public boolean hasUnsavedChanges() {
                                        // TODO return whether this panel has been changed
                                        return false;
                                    }
                                };
                                String[] options = {
                                        Messages.getString("ExportDDLAction.quickFixAllOption"), //$NON-NLS-1$
                                        Messages.getString("ExportDDLAction.ignoreWarningsOption"), //$NON-NLS-1$
                                        Messages.getString("ExportDDLAction.cancelOption"), //$NON-NLS-1$
                                        Messages.getString("ExportDDLAction.recheckOption") //$NON-NLS-1$
                                };

                                // This used to be a JOptionPane, but a resize bug made me change
                                // it to this. The whole thing is a disaster and will go away when
                                // critics are ready.
                                final JDialog warningDialog = new JDialog(frame, true);
                                warningDialog.setTitle(Messages.getString("ExportDDLAction.errorsInDDLDialogTitle"));
                                
                                ButtonBarBuilder bbb = new ButtonBarBuilder();
                                bbb.setDefaultButtonBarGapBorder();
                                final AtomicInteger dialogChoice = new AtomicInteger(-1);
                                for (int i = 0; i < options.length; i++) {
                                    final int ii = i;
                                    JButton button = new JButton(options[i]);
                                    button.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            dialogChoice.set(ii);
                                            warningDialog.dispose();
                                        }
                                    });
                                    bbb.addGridded(button);
                                }

                                JPanel p = new JPanel(new BorderLayout());
                                p.add(dialogPanel.getPanel(), BorderLayout.CENTER);
                                p.add(bbb.getPanel(), BorderLayout.SOUTH);
                                p.setBorder(Borders.DIALOG_BORDER);
                                
                                warningDialog.setContentPane(p);
                                warningDialog.pack();
                                warningDialog.setLocationRelativeTo(d);
                                warningDialog.setVisible(true); // modal dialog blocks
                                
                                logger.debug(dialogChoice.get());
                                switch (dialogChoice.get()) {
                                case 0:
                                    for (DDLWarning warning : warnings) {
                                        if (warning.isQuickFixable()) {
                                            warning.quickFix();
                                        }
                                    }
                                    break;
                                case 1:
                                    done = true;
                                    break;
                                case 2:     // "Cancel"
                                case -1:    // no button was pressed--kill dialog
                                    return true;
                                case 3: // apply all changes made
                                    for (DDLWarningComponent warningComponent : warningComponents) {
                                        warningComponent.applyChanges();
                                    }
                                    break;
                                }
                            } // end of main while loop
                        }
                        
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
                } catch (SQLException ex) {
                    ASUtils.showExceptionDialog
                        (session, 
                         Messages.getString("ExportDDLAction.errorGeneratingDDL"), ex); //$NON-NLS-1$
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog
                        (session,
                         Messages.getString("ExportDDLAction.errorGeneratingDDLScript"), ex); //$NON-NLS-1$
                }
                return Boolean.FALSE;
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
		List statements;


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
				DDLGenerator ddlg, List statements, ArchitectSwingSession session)
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
