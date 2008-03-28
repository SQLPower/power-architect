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

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.TableProfileCreator;
import ca.sqlpower.swingui.DataEntryPanel;

public class ProjectSettingsPanel extends JPanel implements DataEntryPanel {
    private static final Logger logger = Logger.getLogger(ProjectSettingsPanel.class);
    
	/**
	 * The project whose settings we're editting.
	 */
	private ArchitectSwingSession session;

    /**
     * 
     */
	private JCheckBox saveEntireSource;
    
    /**
     * A profile manager setting: How many "top n" values to store.
     */
    private JTextField numberOfFreqValues;
    
    /**
     * A profile manager setting: Which profile creator to use.
     */
    private JComboBox profileMode;

    private JRadioButton rectilinearRelationships;
    private JRadioButton directRelationships;
    
	public ProjectSettingsPanel(ArchitectSwingSession session) {
		this.session = session;
		setup();
		revertToProjectSettings();
	}

	public void setup() {
		setLayout(new FormLayout());
		add(new JLabel("Snapshot Entire Source Database in Project File?"));
		add(saveEntireSource = new JCheckBox());

        add(new JLabel("Number of Common Values in Profiles:"));
        add(numberOfFreqValues = new JTextField("",6));
        
        add(new JLabel("Profile Creator Mode:"));
        add(profileMode = new JComboBox(session.getProfileManager().getProfileCreators().toArray()));
        
        add(new JLabel("Draw Relationships With:"));
        add(rectilinearRelationships = new JRadioButton("Rectilinear Lines"));
        add(new JLabel());
        add(directRelationships = new JRadioButton("Direct Lines"));
        ButtonGroup lineStyleGroup = new ButtonGroup();
        lineStyleGroup.add(rectilinearRelationships);
        lineStyleGroup.add(directRelationships);
	}

	private void revertToProjectSettings() {
        logger.debug("Reverting project options");
        numberOfFreqValues.setText(String.valueOf(session.getProfileManager().getDefaultProfileSettings().getTopNCount()));
        profileMode.setSelectedItem(session.getProfileManager().getCreator());
		saveEntireSource.setSelected(session.isSavingEntireSource());
        if (session.getRelationshipLinesDirect()) {
            directRelationships.setSelected(true);
        } else {
            rectilinearRelationships.setSelected(true);
        }
	}

	public boolean applyChanges() {
		session.setSavingEntireSource(saveEntireSource.isSelected());

        if ( numberOfFreqValues.getText().length() > 0 ) {
            try {
                session.getProfileManager().getDefaultProfileSettings().setTopNCount(Integer.valueOf(numberOfFreqValues.getText()));
            } catch ( NumberFormatException e ) {
                ASUtils.showExceptionDialogNoReport(this,
                        "Number Format Error", e);
            }
        }
        
        session.getProfileManager().setCreator((TableProfileCreator) profileMode.getSelectedItem());
        
        if (directRelationships.isSelected()) {
            session.setRelationshipLinesDirect(true);
        } else {
            session.setRelationshipLinesDirect(false);
        }
        
		return true;
	}

	public void discardChanges() {
	    // TODO revert the changes made
	}

	public JPanel getPanel() {
		return this;
	}

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

}
