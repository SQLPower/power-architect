package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import javax.swing.*;
import ca.sqlpower.architect.ddl.*;
import ca.sqlpower.architect.etl.*;
import ca.sqlpower.architect.*;
import ca.sqlpower.security.PLSecurityException;
import org.apache.log4j.Logger;

public class ExportPLTransAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(ExportPLTransAction.class);

	protected ArchitectFrame architectFrame;
	protected PlayPen playpen;

	/** The PLExport object that this action uses to create PL transactions. */
	protected PLExport plexp;

	/** The dialog box that this action uses to configure plexp. */
	protected JDialog d;

	public ExportPLTransAction() {
		super("PL Transaction Export...",
			  ASUtils.createIcon("PLTransExport",
								 "PL Transaction Export",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "PL Transaction Export");
	}

	public void setPlayPen(PlayPen playpen) {
		this.playpen = playpen;
	}
	
		
	/**
	 * Sets up the dialog the first time it is called.  After that,
	 * just returns without doing anything.
	 *
	 * <p>Note: the <code>plexp</code> variable must be initialized before calling this method!
	 *
	 * @throws NullPointerException if <code>plexp</code> is null.
	 */
	public synchronized void setupDialog() {
		if (d != null) return;
		d = new JDialog(ArchitectFrame.getMainInstance(),
						"Export ETL Transactions to PL Repository");

		// set export defaults if necessary
		if (plexp.getFolderName() == null || plexp.getFolderName().trim().length() == 0) {
			plexp.setFolderName(PLUtils.toPLIdentifier(architectFrame.getProject().getName()+"_FOLDER"));
		}

		if (plexp.getJobId() == null || plexp.getJobId().trim().length() == 0) {
			plexp.setJobId(PLUtils.toPLIdentifier(architectFrame.getProject().getName()+"_JOB"));
		}

		
		JPanel plp = new JPanel(new BorderLayout(12,12));
		plp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); 
		
		final PLExportPanel plPanel = new PLExportPanel();
		plPanel.setPLExport(plexp);
		plp.add(plPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					plPanel.applyChanges();
					if (plexp.getPlDBCS() == null) {
						JOptionPane.showMessageDialog(plPanel, "You have to select a target database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (plexp.getPlUsername().trim().length() == 0) {
						JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader User Name.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (plexp.getJobId().trim().length() == 0) {
						JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						plexp.export(playpen.getDatabase());
						if (plPanel.isSelectedRunPLEngine()) {
							logger.debug("run PL LOADER Engine");
							File plIni = new File(architectFrame.getUserSettings().getETLUserSettings().getPlDotIniPath());
							File plDir = plIni.getParentFile();
							File engineExe = new File(plDir, plPanel.getPLConnectionSpec().getEngineExeutableName());
							StringBuffer commandLine = new StringBuffer(1000);
							commandLine.append(engineExe.getPath());
							commandLine.append(" USER_PROMPT=N");
							commandLine.append(" JOB=").append(plexp.getJobId());

							commandLine.append(" USER=").append(plPanel.getPLConnectionSpec().getEngineConnectString());
							commandLine.append(" DEBUG=N SEND_EMAIL=N SKIP_PACKAGES=N CALC_DETAIL_STATS=N COMMIT_FREQ=100 APPEND_TO_JOB_LOG_IND=N");
							commandLine.append(" APPEND_TO_JOB_ERR_IND=N");
							commandLine.append(" SHOW_PROGRESS=100" );
							logger.debug(commandLine.toString());
							try {
								Process proc = Runtime.getRuntime().exec(commandLine.toString());
								JDialog d = new JDialog(architectFrame, "Power*Loader Engine");
								d.setContentPane(new EngineExecPanel(commandLine.toString(), proc));
								d.pack();
								d.setLocationRelativeTo(plPanel);
								d.setVisible(true);
							} catch (IOException ie){
								JOptionPane.showMessageDialog(playpen, "Unexpected Exception running Engine:\n"+ie);
								logger.error(ie);
							}	
						}
					} catch (PLSecurityException ex) {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export Transaction: "+ex.getMessage());
						logger.error("Got exception while exporting Trans", ex);
					} catch (SQLException esql) {
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export Transaction: "+esql.getMessage());
						logger.error("Got exception while exporting Trans", esql);
					} catch (ArchitectException arex){
						JOptionPane.showMessageDialog
							(architectFrame,
							 "Can't export Transaction: "+arex.getMessage());
						logger.error("Got exception while exporting Trans", arex);
					} finally {
						d.setVisible(false);
					}
				}
			});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					plPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		
		plp.add(buttonPanel, BorderLayout.SOUTH);
		
		d.setContentPane(plp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
	}

	public void actionPerformed(ActionEvent e) {
		plexp = architectFrame.getProject().getPLExport();
		setupDialog();
		d.setVisible(true); 
	}

	/**
	 * Checks for missing tables in the target database.  Returns a
	 * list of table names that need to be created.
	 */
	public java.util.List listMissingTargetTables() {

		logger.error("listMissingTargetTables is not implemented");
		return java.util.Collections.EMPTY_LIST;
	}
}
