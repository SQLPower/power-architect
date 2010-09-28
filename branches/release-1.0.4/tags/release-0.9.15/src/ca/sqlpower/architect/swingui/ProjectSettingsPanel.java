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
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl.ColumnVisibility;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ProjectSettingsPanel extends JPanel implements DataEntryPanel {
    private static final Logger logger = Logger.getLogger(ProjectSettingsPanel.class);
    
	/**
	 * The project whose settings we're editting.
	 */
	private ArchitectSwingSession session;
	
    /**
     * The panel that contains the editor components.
     */
    private JPanel panel;

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
    
    private JRadioButton displayRelationshipLabel;
    private JRadioButton hideRelationshipLabel;
    
    private JCheckBox showPkTag;
    private JCheckBox showFkTag;
    private JCheckBox showAkTag;
    
    private JRadioButton showAll;
    private JRadioButton showPkFkUniqueIndexed;
    private JRadioButton showPkFkUnique;
    private JRadioButton showPkFk;
    private JRadioButton showPk;
    
    private JRadioButton physicalNames;
    private JRadioButton logicalNames;
    
	public ProjectSettingsPanel(ArchitectSwingSession session) {
		this.session = session;
		setup();
		revertToProjectSettings();
	}

	public void setup() {
	    com.jgoodies.forms.layout.FormLayout layout = new com.jgoodies.forms.layout.FormLayout(
	            "pref,4dlu,pref");  //$NON-NLS-1$
	    DefaultFormBuilder fb = new DefaultFormBuilder(layout, new JPanel());
	    setLayout(layout);
		fb.append(Messages.getString("ProjectSettingsPanel.snapshotSourceDbOption"), saveEntireSource = new JCheckBox()); //$NON-NLS-1$
		fb.nextLine();
		fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
		
		fb.append(Messages.getString("ProjectSettingsPanel.numCommonProfileValues"), numberOfFreqValues = new JTextField("",6)); //$NON-NLS-1$
        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        fb.append(Messages.getString("ProjectSettingsPanel.profileMode"), profileMode = new JComboBox(session.getProfileManager().getProfileCreators().toArray())); //$NON-NLS-1$
        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
       
        fb.append(new JLabel(Messages.getString("ProjectSettingsPanel.relationshipLineStyle"))); //$NON-NLS-1$
        fb.append(rectilinearRelationships = new JRadioButton(Messages.getString("ProjectSettingsPanel.rectilinearLineOption"))); //$NON-NLS-1$
        
        fb.nextLine();
        
        fb.append("", directRelationships = new JRadioButton(Messages.getString("ProjectSettingsPanel.directLineOption"))); //$NON-NLS-1$
        ButtonGroup lineStyleGroup = new ButtonGroup();
        lineStyleGroup.add(rectilinearRelationships);
        lineStyleGroup.add(directRelationships);
        
        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        fb.append(new JLabel(Messages.getString("ProjectSettingsPanel.displayPhysicalOrLogical"))); //$NON-NLS-1$
        fb.append(logicalNames = new JRadioButton(Messages.getString("ProjectSettingsPanel.displayLogicalNames"))); //$NON-NLS-1$
        fb.nextLine();
        
        fb.append("", physicalNames = new JRadioButton(Messages.getString("ProjectSettingsPanel.displayPhysicalNames"))); //$NON-NLS-1$
        ButtonGroup nameDisplay = new ButtonGroup();
        nameDisplay.add(logicalNames);
        nameDisplay.add(physicalNames);
        
        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        fb.append(new JLabel(Messages.getString("ProjectSettingsPanel.visibilityOfRelationshipLabel"))); //$NON-NLS-1$
        fb.append(displayRelationshipLabel = new JRadioButton(Messages.getString("ProjectSettingsPanel.displayRelationshipLabel"))); //$NON-NLS-1$
       
        fb.nextLine();
        
        fb.append("", hideRelationshipLabel = new JRadioButton(Messages.getString("ProjectSettingsPanel.hideRelationshipLabel"))); //$NON-NLS-1$
        ButtonGroup DisplayRelationshipLabel = new ButtonGroup();
        DisplayRelationshipLabel.add(displayRelationshipLabel);
        DisplayRelationshipLabel.add(hideRelationshipLabel);
        
        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        fb.append(showAll = new JRadioButton(Messages.getString("ProjectSettingsPanel.showAll"))); //$NON-NLS-1$
        fb.append(showPkTag = new JCheckBox(Messages.getString("ProjectSettingsPanel.showPKTags"))); //$NON-NLS-1$
        
        fb.nextLine();
        
        fb.append(showPkFkUniqueIndexed = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPKFKUniqueIndexed"))); //$NON-NLS-1$
        fb.append(showFkTag = new JCheckBox(Messages.getString("ProjectSettingsPanel.showFKTags"))); //$NON-NLS-1$
        
        fb.nextLine();
        
        fb.append(showPkFkUnique = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPKFKUnique"))); //$NON-NLS-1$
        fb.append(showAkTag = new JCheckBox(Messages.getString("ProjectSettingsPanel.showAKTags"))); //$NON-NLS-1$
        
        fb.nextLine();
        
        fb.append(showPkFk = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPKFK"))); //$NON-NLS-1$
        
        fb.nextLine();
        
        fb.append(showPk = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPK"))); //$NON-NLS-1$
        
        fb.nextLine();
        
        ButtonGroup column_show_settings = new ButtonGroup();
        column_show_settings.add(showAll);
        column_show_settings.add(showPkFkUniqueIndexed);
        column_show_settings.add(showPkFkUnique);
        column_show_settings.add(showPkFk);
        column_show_settings.add(showPk);
        
        fb.setDefaultDialogBorder();
        this.panel = fb.getPanel();
	}

	private void revertToProjectSettings() {
        logger.debug("Reverting project options"); //$NON-NLS-1$
        numberOfFreqValues.setText(String.valueOf(session.getProfileManager().getDefaultProfileSettings().getTopNCount()));
        profileMode.setSelectedItem(session.getProfileManager().getCreator());
		saveEntireSource.setSelected(session.isSavingEntireSource());
        if (session.getRelationshipLinesDirect()) {
            directRelationships.setSelected(true);
        } else {
            rectilinearRelationships.setSelected(true);
        }
        if (session.isUsingLogicalNames()) {
            logicalNames.setSelected(true);
        } else {
            physicalNames.setSelected(true);
        }
        if (session.isDisplayRelationshipLabel()) {
            displayRelationshipLabel.setSelected(true);
        } else {
            hideRelationshipLabel.setSelected(true);
        }
        
            showPkTag.setSelected(session.isShowPkTag());
            showFkTag.setSelected(session.isShowFkTag());
            showAkTag.setSelected(session.isShowAkTag());
            
        ColumnVisibility choice = session.getColumnVisibility();
        
        switch (choice) {
        case ALL:
            showAll.setSelected(true);
            break;
        case PK:
            showPk.setSelected(true);
            break;
        case PK_FK:
            showPkFk.setSelected(true);
            break;
        case PK_FK_UNIQUE:
            showPkFkUnique.setSelected(true);
            break;
        case PK_FK_UNIQUE_INDEXED:
            showPkFkUniqueIndexed.setSelected(true);
            break;
        }
    }

	public boolean applyChanges() {
		session.setSavingEntireSource(saveEntireSource.isSelected());

        if ( numberOfFreqValues.getText().length() > 0 ) {
            try {
                session.getProfileManager().getDefaultProfileSettings().setTopNCount(Integer.valueOf(numberOfFreqValues.getText()));
            } catch ( NumberFormatException e ) {
                ASUtils.showExceptionDialogNoReport(this,
                        "Number Format Error", e); //$NON-NLS-1$
            }
        }
        
        session.getProfileManager().setCreator((TableProfileCreator) profileMode.getSelectedItem());
        
        if (directRelationships.isSelected()) {
            session.setRelationshipLinesDirect(true);
        } else {
            session.setRelationshipLinesDirect(false);
        }
        
        if (logicalNames.isSelected()) {
            session.setUsingLogicalNames(true);
        } else {
            session.setUsingLogicalNames(false);
        }
        
        if (displayRelationshipLabel.isSelected()) {
            session.setDisplayRelationshipLabel(true);
        } else {
            session.setDisplayRelationshipLabel(false);
        }
        
        session.setShowPkTag(showPkTag.isSelected());
        session.setShowFkTag(showFkTag.isSelected());
        session.setShowAkTag(showAkTag.isSelected());
        
        if (showAll.isSelected()) {
            session.setColumnVisibility(ColumnVisibility.ALL);
        }
        if (showPk.isSelected()) {
            session.setColumnVisibility(ColumnVisibility.PK);
        }
        if (showPkFk.isSelected()) {
            session.setColumnVisibility(ColumnVisibility.PK_FK);
        }
        if (showPkFkUnique.isSelected()) {
            session.setColumnVisibility(ColumnVisibility.PK_FK_UNIQUE);
        }
        if (showPkFkUniqueIndexed.isSelected()) {
            session.setColumnVisibility(ColumnVisibility.PK_FK_UNIQUE_INDEXED);
        }
        
        // XXX this refresh should be handled via a property change event on the session
        session.getPlayPen().updateTablePanes();
        
        return true;
	}

	public void discardChanges() {
	    // TODO revert the changes made
	}

	public JPanel getPanel() {
		return this.panel;
	}

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

}
