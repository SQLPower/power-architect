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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;

public class ProjectSettingsPanel extends JPanel implements DataEntryPanel {
    private static final Logger logger = Logger.getLogger(ProjectSettingsPanel.class);
	/**
	 * The project whose settings we're editting.
	 */
	private ArchitectSwingSession session;

	private JCheckBox saveEntireSource;
    private JCheckBox clearProfile;
    private JTextField numberOfFreqValue;

	public ProjectSettingsPanel(ArchitectSwingSession session) {
		this.session = session;
		setup();
		revertToProjectSettings();
	}

	public void setup() {
		setLayout(new FormLayout());
		add(new JLabel("Snapshot entire source database in project file?"));
		add(saveEntireSource = new JCheckBox());

        add(new JLabel("Don't save Profile results in the project?"));
        add(clearProfile=new JCheckBox());
        clearProfile.setToolTipText(
               "Warning: this only removes current profiles");

        add(new JLabel("Number of Most Frequent Value in Profile:"));
        add( numberOfFreqValue=new JTextField(String.valueOf(session.getProfileManager().getDefaultProfileSettings().getTopNCount()),6));
	}

	protected void revertToProjectSettings() {
        logger.debug("Reverting project options");
		saveEntireSource.setSelected(session.isSavingEntireSource());
	}

	public boolean applyChanges() {
        logger.debug("Setting snapshot option to:"+saveEntireSource.isSelected());
		session.setSavingEntireSource(saveEntireSource.isSelected());
        logger.debug("Project "+session.getName() +" snapshot option is:"+session.isSavingEntireSource());

        // This is a mistake! This is an action, not a project setting.
        // It should be changed to a setting, and the Save code changed
        // to honor it.
        if ( clearProfile.isSelected() ) {
            session.getProfileManager().clear();
        }

        if ( numberOfFreqValue.getText().length() > 0 ) {
            try {
                session.getProfileManager().getDefaultProfileSettings().setTopNCount(Integer.valueOf(numberOfFreqValue.getText()));
            } catch ( NumberFormatException e ) {
                ASUtils.showExceptionDialogNoReport(this,
                        "Number Format Error", e);
            }
        }
		return true;
	}

	public void discardChanges() {

	}

	public JPanel getPanel() {
		return this;
	}

}
