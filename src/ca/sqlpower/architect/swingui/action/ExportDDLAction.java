package ca.sqlpower.architect.swingui.action;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DDLExportPanel;
import ca.sqlpower.architect.swingui.MonitorableWorker;
import ca.sqlpower.architect.swingui.SQLScriptDialog;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class ExportDDLAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(ExportDDLAction.class);

	private static final String GENDDL_WARNINGS_EXPLANATION =
        "Errors:\n" +
        "The DDL could not be generated because the following error(s) were detected. " +
        "You need to correct all the errors before we can generate DDL for you. " +
        "Some errors may have their 'QuickFix' button enabled; holding the mouse over these buttons will tell you what the " +
        "suggested quick-fix is. If you are OK with the suggestion, press the QuickFix button, " +
        "otherwise, make the change yourself using the GUI controls following the message.";

	protected ArchitectFrame architectFrame;

	public ExportDDLAction() {
		super("Forward Engineer...",
			  ASUtils.createIcon("ForwardEngineer",
								 "Forward Engineer",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "Forward Engineer SQL Script");
	}

	private JDialog d;

    public void actionPerformed(ActionEvent e) {

        final DDLExportPanel ddlPanel = new DDLExportPanel(architectFrame.getProject());

        Action okAction, cancelAction;
        okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (ddlPanel.applyChanges()) {

                        GenericDDLGenerator ddlg = architectFrame.getProject().getDDLGenerator();
                        ddlg.setTargetSchema(ddlPanel.getSchemaField().getText());

                        boolean done = false;
                        while (!done) {
                            // generate DDL in order to come up with a list of warnings
                            ddlg.generateDDL(architectFrame.getProject().getPlayPen().getDatabase());
                            final List<DDLWarning> warnings = ddlg.getWarnings();
                            final JPanel outerPanel = new JPanel();
                            if (warnings.size() == 0) {
                                done = true;
                            } else {
                                final List<DDLWarningComponent> warningComponents = new ArrayList<DDLWarningComponent>();
                                ArchitectPanel dialogPanel = new ArchitectPanel() {
                                    
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
                                        outerPanel.add(new JScrollPane(listBoxPanel), BorderLayout.CENTER);
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
                                        ArchitectFrame.getMainInstance(),
                                        dialogPanel.getPanel(),
                                        "Errors in generated DDL",
                                        JOptionPane.DEFAULT_OPTION,
                                        JOptionPane.ERROR_MESSAGE,
                                        null,   // JOptionPane gets icon from owningComponent,
                                        options,
                                        options[options.length - 1]);    // blocking
                                System.out.println(dialogChoice);
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
                                    return;
                                case 3: // apply all changes made
                                    for (DDLWarningComponent warningComponent : warningComponents) {
                                        warningComponent.applyChanges();
                                    }
                                    break;
                                }
                            }
                        }

                        SQLDatabase ppdb = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase();
                        SQLScriptDialog ssd =
                            new SQLScriptDialog(d, "Preview SQL Script", "", false,
                                    ddlg,
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
                            "Can't export DDL: " + ex);
                    logger.error("Got exception while exporting DDL", ex);

                }
            }

            private void generateAnyway() {
                System.out.println("generateAnyway()");
            }


        };



        cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                ddlPanel.discardChanges();
                d.setVisible(false);
            }
        };
        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                ddlPanel,
                ArchitectFrame.getMainInstance(),
                "Forward Engineer SQL Script", "OK",
                okAction, cancelAction);

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
				DDLGenerator ddlg, List statements)
			throws ArchitectException, SQLException {
			super();
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
		 * This should run on its own thread (not the AWT event dispatch
		 * thread). It will take a while.
		 */
		public void doStuff() {

			if (this.isCanceled()) return;
			try {
				cr.findConflicting();

			} catch (Exception ex) {
				error = ex;
				errorMessage = "You have to specify a target database connection"
					+"\nbefore executing this script.";
				logger.error("Unexpected exception setting up DDL generation", ex);

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
					this.setCancelled(true);
				}
			}

		}

		public ConflictResolver getConflictResolver() {
			return cr;
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
		public ConflictResolverProcess(JDialog d, ConflictFinderProcess cfp) {
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
				ASUtils.showExceptionDialog(parentDialog,
                    "Error Dropping Conflicts: "+errorMessage, error, new ArchitectExceptionReportFactory());
				setCancelled(true);
			}
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


	}


}
