/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.ConflictResolver;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLWarning;
import ca.sqlpower.architect.ddl.DDLWarningComponent;
import ca.sqlpower.architect.ddl.DDLWarningComponentFactory;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DDLExportPanel;
import ca.sqlpower.architect.swingui.SQLScriptDialog;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.MonitorableWorker;

public class ExportDDLAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(ExportDDLAction.class);

	private static final String GENDDL_WARNINGS_EXPLANATION =
        "Errors:\n" +
        "The DDL could not be generated because the following error(s) were detected. " +
        "You need to correct all the errors before we can generate DDL for you. " +
        "Some errors may have their 'QuickFix' button enabled; holding the mouse over these buttons will tell you what the " +
        "suggested quick-fix is. If you are OK with the suggestion, press the QuickFix button, " +
        "otherwise, make the change yourself using the GUI controls following the message.";

	public ExportDDLAction(ArchitectSwingSession session) {
		super(session, "Forward Engineer...", "Forward Engineer SQL Script", "fwdSQL");
	}

	private JDialog d;

    public void actionPerformed(ActionEvent e) {

        final DDLExportPanel ddlPanel = new DDLExportPanel(session);

        Callable<Boolean> okCall, cancelCall;
        okCall = new Callable<Boolean>() {
            public Boolean call() {
                try {
                    if (ddlPanel.applyChanges()) {

                        GenericDDLGenerator ddlg = session.getDDLGenerator();
                        ddlg.setTargetSchema(ddlPanel.getSchemaField().getText());
                        
                        boolean done = false;
                        while (!done) {
                            // generate DDL in order to come up with a list of warnings
                            ddlg.generateDDL(session.getPlayPen().getDatabase());
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
                                            DDLWarningComponent ddlWarningComponent = DDLWarningComponentFactory.createComponent(ddlwarning);
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
                                };
                                String[] options = {
                                        "QuickFix All",
                                        "Ignore Warnings",
                                        "Cancel",
                                        "Recheck"
                                };

                                int dialogChoice = JOptionPane.showOptionDialog(
                                        frame,
                                        dialogPanel.getPanel(),
                                        "Errors in generated DDL",
                                        JOptionPane.DEFAULT_OPTION,
                                        JOptionPane.ERROR_MESSAGE,
                                        null,   // JOptionPane gets icon from owningComponent,
                                        options,
                                        options[options.length - 1]);    // blocking
                                logger.debug(dialogChoice);
                                switch (dialogChoice) {
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
                                case -1:    // Kill dialog
                                	break;
                                case 3: // apply all changes made
                                    for (DDLWarningComponent warningComponent : warningComponents) {
                                        warningComponent.applyChanges();
                                    }
                                    break;
                                }
                            }
                        }
                        
                        SQLDatabase ppdb = new SQLDatabase(ddlPanel.getTargetDB());
                        SQLScriptDialog ssd =
                            new SQLScriptDialog(d, "Preview SQL Script", "", false,
                                    ddlg,
                                    ppdb.getDataSource(),
                                    true,
                                    session);
                        MonitorableWorker scriptWorker = ssd.getExecuteTask();
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
                         "An error ocurred while trying to generate" +
                         " the DDL script.", ex);
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog
                        (session,
                         "An error occurred while generating the script.", ex);
                }
                return new Boolean(false);
            }
        };



        cancelCall = new Callable<Boolean>() {
            public Boolean call() {
                ddlPanel.discardChanges();
                return new Boolean(true);
            }
        };
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                ddlPanel,
                frame,
                "Forward Engineer SQL Script", "OK",
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
	public class ConflictFinderProcess extends MonitorableWorker {

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
		 * @throws ArchitectException If there is a problem connecting to the target database
		 * @throws SQLException If the conflict resolver chokes
		 */
		public ConflictFinderProcess(JDialog parentDialog, SQLDatabase target,
				DDLGenerator ddlg, List statements, ArchitectSwingSession session)
			throws ArchitectException {
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

			if (this.isCanceled()) return;
            
            // First, test if it's possible to connect to the target database
            Connection con = null;
            try {
                con = target.getConnection();
            } catch (Exception ex) {
                error = ex;
                errorMessage = "Failed to connect to target database. Please check your connection settings.";
                return;
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        logger.error("Failed to close connection. This exception is getting squashed:", ex);
                    }
                }
            }
            
            // Now do the actual work
			try {
				cr.findConflicting();
			} catch (Exception ex) {
				error = ex;
				errorMessage = "Unexpected exception while searching for existing database objects";
				logger.error("Unexpected exception setting up DDL generation", ex);
			}
		}

		/**
		 * After the doStuff() method is done, this method will be invoked on the AWT event
		 * dispatch thread.
		 */
		public void cleanup() {
			if (!SwingUtilities.isEventDispatchThread()) {
				logger.error("runFinished is running on the wrong thread!");
			}
			if (errorMessage != null) {
                if (error != null) {
                    ASUtils.showExceptionDialogNoReport(parentDialog, errorMessage, error);
                } else {
                    JOptionPane.showMessageDialog(parentDialog, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                }
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
					this.setCancelled(true);
				}
			}

		}

		public ConflictResolver getConflictResolver() {
			return cr;
		}



		public Integer getJobSize() {
			return cr.getJobSize();
		}

		public String getMessage() {
			return cr.getMessage();
		}

		public int getProgress() {
			return cr.getProgress();
		}

		public boolean hasStarted() {
			return cr.hasStarted();
		}

		public boolean isFinished() {
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
	public class ConflictResolverProcess extends MonitorableWorker {

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
			if (isCanceled())
				return;
			if (conflictFinder.doesUserWantToDropConflicts()) {
				cr = conflictFinder.getConflictResolver();
				cr.aboutToCallDropConflicting();
				try {
					cr.dropConflicting();
				} catch (Exception ex) {
					logger.error("Error while dropping conflicting objects", ex);
					errorMessage = "Error while dropping conflicting objects:\n\n"+ex.getMessage();
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
                    "Error Dropping Conflicts: "+errorMessage, error);
				setCancelled(true);
			}
		}

		public Integer getJobSize() {
			return cr.getJobSize();
		}

		public String getMessage() {
			return cr.getMessage();
		}

		public int getProgress() {
			return cr.getProgress();
		}

		public boolean hasStarted() {
			return cr.hasStarted();
		}

		public boolean isFinished() {
			return cr.isFinished();
		}


	}


}
