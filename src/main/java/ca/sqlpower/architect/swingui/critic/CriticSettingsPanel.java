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

package ca.sqlpower.architect.swingui.critic;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.sqlpower.architect.ddl.critic.CriticSettings;
import ca.sqlpower.architect.ddl.critic.CriticSettings.Severity;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel that allows editing a single set of critic settings.
 */
public class CriticSettingsPanel implements DataEntryPanel {

    /**
     * The critic settings the panel will allow editing of. The settings will
     * not change until apply changes has been called.
     */
    private final CriticSettings settings;
    
    /**
     * The main panel of this data entry panel.
     */
    private final JPanel panel;
    
    /**
     * Allows users to change the severity level on a critic.
     */
    private final JComboBox severityCombo;

    public CriticSettingsPanel(CriticSettings settings) {
        this.settings = settings;
        
        panel = new JPanel();
        severityCombo = new JComboBox(Severity.values());
        severityCombo.setSelectedItem(settings.getSeverity());
        //It would be nice if the layout used a pref:grow style for the first
        //column but it makes it difficult to set the preferred size correctly.
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("175dlu, 5dlu, pref"), panel);
        builder.append(settings.getCriticType().getSimpleName());
        builder.append(severityCombo);
    }

    public boolean applyChanges() {
        settings.setSeverity(((Severity) severityCombo.getSelectedItem()));
        return true;
    }

    public void discardChanges() {
        //do nothing
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        if (!((Severity) severityCombo.getSelectedItem()).equals(settings.getSeverity())) return true;
        return false;
    }

}
