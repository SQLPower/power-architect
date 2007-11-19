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
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

public class PreferencesPanel extends JPanel implements DataEntryPanel {

	/**
	 * The settings we're editing
	 */
	private CoreUserSettings us;

	private JTextField plIniName;
	private JButton plIniButton;

	private JTextField etlLogFileName;
	private JButton etlLogFileButton;

	private JTextField ddlLogFileName;
	private JButton ddlLogFileButton;

	private JRadioButton playPenAntialiasOn;
	private JRadioButton playPenAntialiasOff;

    private JRadioButton exceptionReportOn;
    private JRadioButton exceptionReportOff;

    private JRadioButton showWelcomeOn;
    private JRadioButton showWelcomeOff;

    private final ArchitectSwingSessionContext context;

	public PreferencesPanel(ArchitectSwingSessionContext context) {
		this.context = context;
        this.us = context.getUserSettings();
        
		setup();
		revertToUserSettings();
	}

	public void setup() {
		setLayout(new FormLayout(5,5));
		// line 1
		add(new JLabel("PL.INI File"));
		JPanel plIniPanel = new JPanel();
		plIniPanel.setLayout(new BorderLayout());
		plIniPanel.add(plIniName = new JTextField("",35), BorderLayout.WEST);
		plIniPanel.add(plIniButton = new JButton(), BorderLayout.EAST);
		plIniButton.setAction(new ChooseFileAction(plIniName,SPSUtils.INI_FILE_FILTER,"Browse..."));
		add(plIniPanel);
		// line 2
		add(new JLabel("ETL Log File"));
		JPanel etlLogFilePanel = new JPanel();
		etlLogFilePanel.setLayout(new BorderLayout());
		etlLogFilePanel.add(etlLogFileName = new JTextField("",35), BorderLayout.WEST);
		etlLogFilePanel.add(etlLogFileButton = new JButton(), BorderLayout.EAST);
		etlLogFileButton.setAction(new ChooseFileAction(etlLogFileName,SPSUtils.LOG_FILE_FILTER,"Browse..."));
		add(etlLogFilePanel);

		// line 3
		add(new JLabel("Forward Engineering Log File"));
		JPanel ddlLogFilePanel = new JPanel();
		ddlLogFilePanel.setLayout(new BorderLayout());
		ddlLogFilePanel.add(ddlLogFileName = new JTextField("",35), BorderLayout.WEST);
		ddlLogFilePanel.add(ddlLogFileButton = new JButton(), BorderLayout.EAST);
		ddlLogFileButton.setAction(new ChooseFileAction(ddlLogFileName,SPSUtils.LOG_FILE_FILTER,"Browse..."));
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

        //line 5
        add(new JLabel("Error Reporting"));
        JPanel exceptionReportPanel = new JPanel();
        exceptionReportPanel.setLayout(new FlowLayout());
        ButtonGroup exceptionReportGroup = new ButtonGroup();
        exceptionReportGroup.add(exceptionReportOn = new JRadioButton("On"));
        exceptionReportGroup.add(exceptionReportOff = new JRadioButton("Off"));
        exceptionReportPanel.add(exceptionReportOn);
        exceptionReportPanel.add(exceptionReportOff);
        add(exceptionReportPanel);
        //line 6
        add(new JLabel("Show Welcome Screen"));
        JPanel showWelcomePanel = new JPanel();
        showWelcomePanel.setLayout(new FlowLayout());
        ButtonGroup showWelcomeGroup = new ButtonGroup();
        showWelcomeGroup.add(showWelcomeOn = new JRadioButton("Yes"));
        showWelcomeGroup.add(showWelcomeOff = new JRadioButton("No"));
        showWelcomePanel.add(showWelcomeOn);
        showWelcomePanel.add(showWelcomeOff);
        add(showWelcomePanel);
	}

	protected void revertToUserSettings() {
		plIniName.setText(context.getPlDotIniPath());
		etlLogFileName.setText(us.getETLUserSettings().getString(ETLUserSettings.PROP_ETL_LOG_PATH,""));
		ddlLogFileName.setText(us.getDDLUserSettings().getString(DDLUserSettings.PROP_DDL_LOG_PATH,""));
		if (us.getSwingSettings().getBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false)) {
		    playPenAntialiasOn.setSelected(true);
		} else {
		    playPenAntialiasOff.setSelected(true);
		}
        if (us.getSwingSettings().getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true)) {
            showWelcomeOn.setSelected(true);
        } else {
            showWelcomeOff.setSelected(true);
        }
        if (us.getQfaUserSettings().getBoolean(QFAUserSettings.EXCEPTION_REPORTING, true)) {
            exceptionReportOn.setSelected(true);
        } else {
            exceptionReportOff.setSelected(true);
        }
	}

	public boolean applyChanges() {
		context.setPlDotIniPath(plIniName.getText());
		us.getETLUserSettings().setString(ETLUserSettings.PROP_ETL_LOG_PATH,etlLogFileName.getText());
		us.getDDLUserSettings().setString(DDLUserSettings.PROP_DDL_LOG_PATH,ddlLogFileName.getText());
		us.getSwingSettings().setBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, playPenAntialiasOn.isSelected());
        us.getSwingSettings().setBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, showWelcomeOn.isSelected());
        us.getQfaUserSettings().setBoolean(QFAUserSettings.EXCEPTION_REPORTING, exceptionReportOn.isSelected());
        for (ArchitectSession session: context.getSessions()) {
            ((ArchitectSwingSession)session).getPlayPen().setRenderingAntialiased(playPenAntialiasOn.isSelected());
        }
		return true;
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

	public JPanel getPanel() {
		return this;
	}

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return false;
    }
}
