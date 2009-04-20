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
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.TableProfileCreator;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl.ColumnVisibility;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.layout.CellConstraints;

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
	    CellConstraints cc = new CellConstraints();
	    com.jgoodies.forms.layout.FormLayout layout = new com.jgoodies.forms.layout.FormLayout(
	            "pref,pref",  //$NON-NLS-1$
                "pref,4dlu,pref,4dlu,pref,4dlu,4dlu,pref,pref,4dlu,4dlu,pref,pref,4dlu,4dlu,pref,pref,4dlu,pref,pref,pref,pref,pref,pref,4dlu"); //$NON-NLS-1$
		setLayout(layout);
		int row = 1;
		add(new JLabel(Messages.getString("ProjectSettingsPanel.snapshotSourceDbOption")), cc.xy(1, row)); //$NON-NLS-1$
		add(saveEntireSource = new JCheckBox(), cc.xy(2, row));

		row+=2;
        add(new JLabel(Messages.getString("ProjectSettingsPanel.numCommonProfileValues")), cc.xy(1, row)); //$NON-NLS-1$
        add(numberOfFreqValues = new JTextField("",6), cc.xy(2, row)); //$NON-NLS-1$
        
        row+=2;
        add(new JLabel(Messages.getString("ProjectSettingsPanel.profileMode")), cc.xy(1, row)); //$NON-NLS-1$
        add(profileMode = new JComboBox(session.getProfileManager().getProfileCreators().toArray()), cc.xy(2, row));
        
        row+=2;
        add(new JSeparator(), cc.xyw(1, row, 2));
        
        row++;
        add(new JLabel(Messages.getString("ProjectSettingsPanel.relationshipLineStyle")), cc.xy(1, row)); //$NON-NLS-1$
        add(rectilinearRelationships = new JRadioButton(Messages.getString("ProjectSettingsPanel.rectilinearLineOption")), cc.xy(2, row)); //$NON-NLS-1$
        
        row++;
        add(new JLabel(), cc.xy(1, row));
        add(directRelationships = new JRadioButton(Messages.getString("ProjectSettingsPanel.directLineOption")), cc.xy(2, row)); //$NON-NLS-1$
        ButtonGroup lineStyleGroup = new ButtonGroup();
        lineStyleGroup.add(rectilinearRelationships);
        lineStyleGroup.add(directRelationships);
        
        row+=2;
        add(new JSeparator(), cc.xyw(1, row, 2));
        
        row++;
        add(new JLabel(Messages.getString("ProjectSettingsPanel.displayPhysicalOrLogical")), cc.xy(1, row)); //$NON-NLS-1$
        add(logicalNames = new JRadioButton(Messages.getString("ProjectSettingsPanel.displayLogicalNames")), cc.xy(2, row)); //$NON-NLS-1$
        
        row++;
        add(new JLabel(), cc.xy(1, row));
        add(physicalNames = new JRadioButton(Messages.getString("ProjectSettingsPanel.displayPhysicalNames")), cc.xy(2, row)); //$NON-NLS-1$
        ButtonGroup nameDisplay = new ButtonGroup();
        nameDisplay.add(logicalNames);
        nameDisplay.add(physicalNames);
        
        logger.info(row);
        row+=2;
        add(new JSeparator(), cc.xyw(1, row, 2));
        logger.info(row);
        
        row++;
        add(new JLabel(Messages.getString("ProjectSettingsPanel.visibilityOfRelationshipLabel")), cc.xy(1, row)); //$NON-NLS-1$
        add(hideRelationshipLabel = new JRadioButton(Messages.getString("ProjectSettingsPanel.hideRelationshipLabel")), cc.xy(2, row)); //$NON-NLS-1$
        
        
        row++;
        add(new JLabel(), cc.xy(1, row));
        add(displayRelationshipLabel = new JRadioButton(Messages.getString("ProjectSettingsPanel.displayRelationshipLabel")), cc.xy(2, row)); //$NON-NLS-1$
        ButtonGroup DisplayRelationshipLabel = new ButtonGroup();
        DisplayRelationshipLabel.add(displayRelationshipLabel);
        DisplayRelationshipLabel.add(hideRelationshipLabel);
        
        row+=2;
        add(new JSeparator(), cc.xyw(1, row, 2));
        
        row++;
        add(showAll = new JRadioButton(Messages.getString("ProjectSettingsPanel.showAll")), cc.xy(1, row)); //$NON-NLS-1$
        add(showPkTag = new JCheckBox(Messages.getString("ProjectSettingsPanel.showPKTags")), cc.xy(2, row)); //$NON-NLS-1$
        
        row++;
        add(showPkFkUniqueIndexed = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPKFKUniqueIndexed")), cc.xy(1, row)); //$NON-NLS-1$
        add(showFkTag = new JCheckBox(Messages.getString("ProjectSettingsPanel.showFKTags")), cc.xy(2, row)); //$NON-NLS-1$
        
        row++;
        add(showPkFkUnique = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPKFKUnique")), cc.xy(1, row)); //$NON-NLS-1$
        add(showAkTag = new JCheckBox(Messages.getString("ProjectSettingsPanel.showAKTags")), cc.xy(2, row)); //$NON-NLS-1$
        
        row++;
        add(showPkFk = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPKFK")), cc.xy(1, row)); //$NON-NLS-1$
        add(new JLabel(), cc.xy(2, row));
        
        row++;
        add(showPk = new JRadioButton(Messages.getString("ProjectSettingsPanel.showPK")), cc.xy(1, row)); //$NON-NLS-1$
        add(new JLabel(), cc.xy(2, row));
        
        ButtonGroup column_show_settings = new ButtonGroup();
        column_show_settings.add(showAll);
        column_show_settings.add(showPkFkUniqueIndexed);
        column_show_settings.add(showPkFkUnique);
        column_show_settings.add(showPkFk);
        column_show_settings.add(showPk);

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
		return this;
	}

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

}
