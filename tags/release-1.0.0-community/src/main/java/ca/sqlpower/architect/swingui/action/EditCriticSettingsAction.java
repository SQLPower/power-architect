/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.JDialog;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.architect.swingui.critic.CriticManagerPanel;
import ca.sqlpower.architect.swingui.critic.CriticSettingsPanelFactory;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * This action will display the critic manager editor panel. With this panel the
 * user can set all of the settings of all of the critics the manager knows
 * about.
 */
public class EditCriticSettingsAction extends AbstractArchitectAction {

    private CriticSettingsPanelFactory settingsPanelFactory = new CriticSettingsPanelFactory();
    
    public EditCriticSettingsAction(ArchitectFrame frame) {
        super(frame, "Validation Manager...", "Display the settings of the validation framework.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CriticManagerPanel criticManagerPanel = new CriticManagerPanel(getSession(), settingsPanelFactory);
        JDialog criticManagerDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                criticManagerPanel, frame, 
                Messages.getString("ArchitectFrame.criticManagerName"), DataEntryPanelBuilder.OK_BUTTON_LABEL);
        criticManagerDialog.pack();
        criticManagerDialog.setVisible(true);
    }

    /**
     * Sets the settings panel factory. Cannot be null.
     */
    public void setSettingsPanelFactory(@Nonnull CriticSettingsPanelFactory settingsPanelFactory) {
        this.settingsPanelFactory = settingsPanelFactory;
    }

}
