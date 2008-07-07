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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;

/**
 * The TabbedDataEntryPanel aggregates one or more DataEntryPanel
 * instances into a single DataEntryPanel with one tab for each
 * sub-panel.  It provides applyChanges and discardChanges methods
 * that "do the right thing" by broadcasting the corresponding request
 * to all the panels being managed by the current instance.
 */
public class TabbedDataEntryPanel implements DataEntryPanel {

    private static final Logger logger = Logger.getLogger(TabbedDataEntryPanel.class);
    
    /**
     * The tabbed pane that holds all the GUIs added via addTab().
     */
    private final JTabbedPane tabbedPane = new JTabbedPane();
    
    /**
     * All of the data entry panels represented in the tabbed pane.
     */
    private final List<DataEntryPanel> panels = new ArrayList<DataEntryPanel>();
    
    public TabbedDataEntryPanel() {
    }
    
    /**
     * Adds the given panel as a tab of this panel.
     * 
     * @param name The string to attach to the tab for the given panel
     * @param panel The panel to manage
     */
    public void addTab(String name, DataEntryPanel panel) {
        tabbedPane.addTab(name, panel.getPanel());
        panels.add(panel);
    }
    
    /**
     * Applies changes to every panel managed by this instance.
     * 
     * @return True if and only if each panel's applyChanges() returned success.
     */
    public boolean applyChanges() {
        logger.debug("Applying changes..."); //$NON-NLS-1$
        boolean success = true;
        for (DataEntryPanel panel : panels) {
            logger.debug("       ... on " + panel); //$NON-NLS-1$
            success &= panel.applyChanges();
        }
        // FIXME: how can we roll back changes if one of the updates fails?
        return success;
    }

    /**
     * Discards changes on every panel managed by this instance.
     */
    public void discardChanges() {
        for (DataEntryPanel panel : panels) {
            panel.discardChanges();
        }
    }

    /**
     * Returns the tabbed pane that aggregates all the panels managed
     * by this instance.
     */
    public JComponent getPanel() {
        return tabbedPane;
    }

    public boolean hasUnsavedChanges() {
        for (DataEntryPanel panel : panels) {
            if (panel.hasUnsavedChanges()) {
                return true;
            }
        }
        return false;
    }

}
