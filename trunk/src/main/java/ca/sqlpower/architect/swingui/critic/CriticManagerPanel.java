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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.sqlpower.architect.ddl.critic.CriticGrouping;
import ca.sqlpower.architect.ddl.critic.CriticManager;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The {@link CriticManager} and its settings can be configured from this panel.
 */
public class CriticManagerPanel implements DataEntryPanel {
    
    /**
     * The main panel of this class that contains all of the configuration
     * settings for the manager.
     */
    private final JPanel mainPanel;

    /**
     * The collection of grouping panels that define the settings of each group
     * of critic settings.
     */
    private final List<CriticGroupingPanel> groupingPanels = new ArrayList<CriticGroupingPanel>();

    public CriticManagerPanel(ArchitectSwingSession session) {
        
        mainPanel = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref"));
        
        final CriticManager criticManager = session.getWorkspace().getCriticManager();
        for (CriticGrouping grouping : criticManager.getCriticGroupings()) {
            CriticGroupingPanel criticGroupingPanel = new CriticGroupingPanel(grouping);
            builder.append(criticGroupingPanel.getPanel());
            builder.nextLine();
            groupingPanels.add(criticGroupingPanel);
        }
        DefaultFormBuilder outerBuilder = new DefaultFormBuilder(
                new FormLayout("pref:grow", "min(pref;400dlu):grow"), mainPanel);
        outerBuilder.append(new JScrollPane(builder.getPanel()));
        outerBuilder.nextLine();
        ButtonBarBuilder2 buttonBar = new ButtonBarBuilder2();
        buttonBar.addButton(new AbstractAction("Restore Defaults") {
            public void actionPerformed(ActionEvent e) {
                if (doApplyChanges()) {
                    criticManager.loadDefaults();
                }
            }
        });
        buttonBar.addButton(new AbstractAction("Set As Defaults") {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (doApplyChanges()) {
                        criticManager.saveAsDefaults();
                    }
                } catch (BackingStoreException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        buttonBar.addGlue();
        outerBuilder.append(buttonBar.getPanel());
    }
    
    public boolean applyChanges() {
        boolean changesApplied = doApplyChanges();
        if (changesApplied) {
            cleanup();
        }
        return changesApplied;
    }

    private boolean doApplyChanges() {
        for (CriticGroupingPanel panel : groupingPanels) {
            if (!panel.applyChanges()) {
                return false;
            }
        }
        return true;
    }

    public void discardChanges() {
        for (CriticGroupingPanel panel : groupingPanels) {
            panel.discardChanges();
        }
        cleanup();
    }

    public JComponent getPanel() {
        return mainPanel;
    }

    public boolean hasUnsavedChanges() {
        for (CriticGroupingPanel panel : groupingPanels) {
            if (panel.hasUnsavedChanges()) return true;
        }
        return false;
    }

    private void cleanup() {
        for (CriticGroupingPanel panel : groupingPanels) {
            panel.cleanup();
        }
    }
}
