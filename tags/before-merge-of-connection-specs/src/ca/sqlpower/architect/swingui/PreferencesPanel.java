package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class PreferencesPanel extends JPanel implements ArchitectPanel {

	/**
	 * The settings we're editing
	 */
	protected UserSettings us;

	protected JTextField plIniName;
	protected JButton plIniButton;

	protected JTextField etlLogFileName;
	protected JButton etlLogFileButton;

	protected JTextField ddlLogFileName;
	protected JButton ddlLogFileButton;

	protected JRadioButton playPenAntialiasOn;
	protected JRadioButton playPenAntialiasOff;

	public PreferencesPanel(UserSettings us) {
		this.us = us;
		setup();
		revertToUserSettings();
	}

	public void setup() {
		setLayout(new FormLayout(5,5));
		// line 1
		add(new JLabel("Power*Loader PL.INI File"));
		JPanel plIniPanel = new JPanel();
		plIniPanel.setLayout(new BorderLayout());
		plIniPanel.add(plIniName = new JTextField("",30), BorderLayout.WEST);
		plIniPanel.add(plIniButton = new JButton(), BorderLayout.EAST);
		plIniButton.setAction(new ChooseFileAction(plIniName,ASUtils.INI_FILE_FILTER,"Browse..."));
		add(plIniPanel);
		// line 2
		add(new JLabel("ETL Log File"));
		JPanel etlLogFilePanel = new JPanel();
		etlLogFilePanel.setLayout(new BorderLayout());
		etlLogFilePanel.add(etlLogFileName = new JTextField("",30), BorderLayout.WEST);
		etlLogFilePanel.add(etlLogFileButton = new JButton(), BorderLayout.EAST);
		etlLogFileButton.setAction(new ChooseFileAction(etlLogFileName,ASUtils.LOG_FILE_FILTER,"Browse..."));
		add(etlLogFilePanel);
		// line 3
		add(new JLabel("Forward Engineering Log File"));
		JPanel ddlLogFilePanel = new JPanel();
		ddlLogFilePanel.setLayout(new BorderLayout());
		ddlLogFilePanel.add(ddlLogFileName = new JTextField("",30), BorderLayout.WEST);
		ddlLogFilePanel.add(ddlLogFileButton = new JButton(), BorderLayout.EAST);
		ddlLogFileButton.setAction(new ChooseFileAction(ddlLogFileName,ASUtils.LOG_FILE_FILTER,"Browse..."));
		add(ddlLogFilePanel);
		// line 4
		add(new JLabel("Antialiased Rendering in PlayPen"));
		JPanel playPenAntialiasPanel = new JPanel();
		playPenAntialiasPanel.setLayout(new FlowLayout());
		ButtonGroup playPenAntialiasGroup = new ButtonGroup();
		playPenAntialiasGroup.add(playPenAntialiasOn = new JRadioButton("On"));
		playPenAntialiasGroup.add(playPenAntialiasOff = new JRadioButton("Off"));
		playPenAntialiasPanel.add(playPenAntialiasOn);
		playPenAntialiasPanel.add(playPenAntialiasOff);
		add(playPenAntialiasPanel);
	}

	protected void revertToUserSettings() {
		plIniName.setText(us.getETLUserSettings().getPlDotIniPath());
		etlLogFileName.setText(us.getETLUserSettings().getETLLogPath());
		ddlLogFileName.setText(us.getDDLUserSettings().getDDLLogPath());
		if (us.getSwingSettings().getBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false)) {
		    playPenAntialiasOn.setSelected(true);
		} else {
		    playPenAntialiasOff.setSelected(true);
		}
	}

	public void applyChanges() {
		us.getETLUserSettings().setPlDotIniPath(plIniName.getText());
		us.getETLUserSettings().setETLLogPath(etlLogFileName.getText());
		us.getDDLUserSettings().setDDLLogPath(ddlLogFileName.getText());
		us.getSwingSettings().setBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, playPenAntialiasOn.isSelected());
		ArchitectFrame.getMainInstance().getProject().getPlayPen().setRenderingAntialiased(playPenAntialiasOn.isSelected());
	}

	public void discardChanges() {
		revertToUserSettings();
	}

	// generic action for browse buttons
	protected class ChooseFileAction extends AbstractAction {		
		JTextField fileName;
		FileFilter filter;
		public ChooseFileAction(JTextField fileName, FileFilter filter, String buttonText) {
			super(buttonText);
			this.fileName = fileName;
			this.filter = filter;
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			if (fileName.getText() != null && fileName.getText().length() > 0) {
				File initialLocation = new File(fileName.getText());
				if (initialLocation.exists()) {
					fc.setCurrentDirectory(initialLocation);
				}
			}			
			fc.addChoosableFileFilter(filter);
			int returnVal = fc.showOpenDialog(PreferencesPanel.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				fileName.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
	}
}
