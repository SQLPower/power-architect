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
	
		
	public void actionPerformed(ActionEvent e) {
		
		final SQLDatabase souDB =playpen.getDatabase();
		final PLExport plexp = new PLExport();
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "PL Repository");
		JPanel plp = new JPanel(new BorderLayout(12,12));
		plp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); 
		
		final PLExportPanel plPanel = new PLExportPanel();
		plp.add(plPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (plPanel.getTargetDBCS() == null) {
						JOptionPane.showMessageDialog(plPanel, "You have to select a target database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (plPanel.getPlUserName().trim().length() == 0) {
						JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader User Name.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (plPanel.getPlJobId().trim().length() == 0) {
						JOptionPane.showMessageDialog(plPanel, "You have to specify the PowerLoader Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						//plPanel.applyChanges();
						plexp.setJobId(plPanel.getPlJobId());
						plexp.setFolderName(plPanel.getPlFolderName());
						plexp.setJobDescription(plPanel.getPlJobDescription());
						plexp.setJobComment(plPanel.getPlJobComment());
						plexp.setPlDBCS(plPanel.getTargetDBCS()); 
						plexp.setOutputTableOwner(plPanel.getPlOutputTableOwner());
						plexp.setPlUsername(plPanel.getPlUserName());
						plexp.setPlPassword(plPanel.getPlPassword());
					} catch (Exception ex) {
						String message = "Can't export Transaction: "+ex.getMessage();
						if (plPanel.isSelectedRunPLEngine()) {
							message += "\nThe engine will not run";
						}
						JOptionPane.showMessageDialog(architectFrame, message);
						logger.error("Got exception while exporting Trans", ex);
						return;
					}
					d.setVisible(false);
					try {
						plexp.export(souDB);
						if (plPanel.isSelectedRunPLEngine()) {
							logger.debug("run PL LOADER Engine");
							File plIni = new File(architectFrame.getUserSettings().getETLUserSettings().getPlDotIniPath());
							File plDir = plIni.getParentFile();
							File engineExe = new File(plDir, plPanel.getSelectedPlDatabase().getEngineExeutableName());
							StringBuffer wcomm = new StringBuffer(1000);
							wcomm.append(engineExe.getPath());
							wcomm.append(" USER_PROMPT=N");
							wcomm.append(" JOB=").append(plPanel.getPlJobId());
							wcomm.append(" USER=").append((plPanel.getTargetDBCS()).getUser()).append("/").append((plPanel.getTargetDBCS()).getPass());
							wcomm.append("@").append(plPanel.getSelectedPlDatabase().getTNSName());
							wcomm.append(" DEBUG=N SEND_EMAIL=N SKIP_PACKAGES=N CALC_DETAIL_STATS=N COMMIT_FREQ=100 APPEND_TO_JOB_LOG_IND=N");
							wcomm.append(" APPEND_TO_JOB_ERR_IND=N");
							wcomm.append(" SHOW_PROGRESS=100" );
							System.out.println(wcomm.toString());
							try {
								Process proc = Runtime.getRuntime().exec(wcomm.toString());
								JDialog d = new JDialog(architectFrame, "Power*Loader Engine");
								d.setContentPane(new EngineExecPanel(proc));
								d.pack();
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
		d.setVisible(true); 
	}
}
