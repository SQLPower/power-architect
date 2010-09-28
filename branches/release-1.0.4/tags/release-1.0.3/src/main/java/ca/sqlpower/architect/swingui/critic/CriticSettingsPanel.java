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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.StarterPlatformTypes;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
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
    protected final CriticAndSettings settings;
    
    /**
     * The main panel of this data entry panel.
     */
    protected final JPanel panel;
    
    /**
     * Allows users to change the severity level on a critic.
     */
    protected final JComboBox severityCombo;

    /**
     * The listeners in this list will be notified when there is a change to the
     * model that causes the UI to update. Components that may need to repaint
     * on these changes can listen and be notified here.
     */
    protected final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    /**
     * Updates the severity combo box due to changes to the model.
     */
    private final SPListener severitySettingListener = new AbstractSPListener() {
        public void propertyChanged(java.beans.PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("severity")) {
                Object oldSeverity = severityCombo.getSelectedItem();
                severityCombo.setSelectedItem(evt.getNewValue());
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(new PropertyChangeEvent(CriticSettingsPanel.this, 
                            "severity", oldSeverity, evt.getNewValue()));
                }
            }
        }
    };

    public CriticSettingsPanel(CriticAndSettings settings) {
        this.settings = settings;
        
        panel = new JPanel();
        severityCombo = new JComboBox(Severity.values());
        if (settings.getPlatformType().equals(StarterPlatformTypes.CONFIGURATION.getName())) {
            severityCombo.removeItem(Severity.WARNING);
        }
        severityCombo.setSelectedItem(settings.getSeverity());
        //It would be nice if the layout used a pref:grow style for the first
        //column but it makes it difficult to set the preferred size correctly.
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("fill:pref:grow, 5dlu, pref"), panel);
        builder.append(new JLabel(settings.getName()));
        builder.append(severityCombo);
        
        settings.addSPListener(severitySettingListener);
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

    public void cleanup() {
        settings.removeSPListener(severitySettingListener);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    public CriticAndSettings getSettings() {
        return settings;
    }
}
