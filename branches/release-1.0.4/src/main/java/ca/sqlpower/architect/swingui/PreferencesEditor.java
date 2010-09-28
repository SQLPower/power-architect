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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.action.EditTableAction;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DataSourceTypeEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class is used to create and display the User Preferences dialog
 */
public class PreferencesEditor {
    
    private static final Logger logger = Logger.getLogger(EditTableAction.class);
    
    // TODO: Get these Icons!!!!!
    private static final ImageIcon SERVER_JDBCDRIVER_ICON = new ImageIcon(PreferencesEditor.class.getResource(""));
    private static final ImageIcon LOCAL_JDBCDRIVER_ICON = new ImageIcon(PreferencesEditor.class.getResource(""));
    
    /**
     * @param owner The Window that is the owner of this dialog
     * @param context The application swing context, which contains the user preferences to be edited
     * @return The dialog containing the preference editor
     */
    public Window showPreferencesDialog(Window owner, ArchitectSwingSession session) {
        logger.debug("showPreferencesDialog"); //$NON-NLS-1$
        
        // XXX Can't easily use ArchitectPanelBuilder since this
        // contains a JTabbedPane which is not an ArchitectPanel.
        
        final JDialog d = SPSUtils.makeOwnedDialog(owner, Messages.getString("PreferencesEditor.userPreferencesDialogTitle")); //$NON-NLS-1$
        JPanel cp = new JPanel(new BorderLayout(12,12));
        JTabbedPane  tp = new JTabbedPane();
        cp.add(tp, BorderLayout.CENTER);
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        final PreferencesPanel prefPanel = new PreferencesPanel(session.getContext());
        tp.add(Messages.getString("PreferencesEditor.generalSection"), prefPanel); //$NON-NLS-1$

        final DataSourceTypeEditor dsTypeEditor =
            new DataSourceTypeEditor(session.getDataSources(), owner, session.isEnterpriseSession());

        // Add the Kettle Options Panel as a tab to the SPDataSourceTypePanel
        final KettleDataSourceTypeOptionPanel kettleOptsPanel = new KettleDataSourceTypeOptionPanel();
        dsTypeEditor.addTab(Messages.getString("PreferencesEditor.kettleSection"), kettleOptsPanel); //$NON-NLS-1$
        
        final ArchitectPropertiesDataSourceTypeOptionPanel architectPropPanel = new ArchitectPropertiesDataSourceTypeOptionPanel();
        dsTypeEditor.addTab(Messages.getString("PreferencesEditor.propertiesSection"), architectPropPanel);
    
        JPanel p = new JPanel(new BorderLayout());
        p.add(createLabelPanel(session), BorderLayout.NORTH);
        p.add(dsTypeEditor.getPanel(), BorderLayout.CENTER);
        tp.add("Local " + Messages.getString("PreferencesEditor.jdbcDriversSection"), p); //$NON-NLS-1$
    
        final DefaultColumnPanel defaultColumnPanel = new DefaultColumnPanel(session.getContext());
        tp.add(Messages.getString("PreferencesEditor.defaultColumnSection"),defaultColumnPanel);
        
        JDefaultButton okButton = new JDefaultButton(DataEntryPanelBuilder.OK_BUTTON_LABEL);
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    prefPanel.applyChanges();
                    dsTypeEditor.applyChanges();
                    defaultColumnPanel.applyChanges();
                    d.setVisible(false);
                }
            });
    
        Action cancelAction = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    prefPanel.discardChanges();
                    dsTypeEditor.discardChanges();
                    defaultColumnPanel.discardChanges();
                    d.setVisible(false);
                }
        };
        cancelAction.putValue(Action.NAME, DataEntryPanelBuilder.CANCEL_BUTTON_LABEL);
        JButton cancelButton = new JButton(cancelAction);

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);

        SPSUtils.makeJDialogCancellable(d, cancelAction);
        d.getRootPane().setDefaultButton(okButton);
        cp.add(buttonPanel, BorderLayout.SOUTH);
        d.setContentPane(cp);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true); 
        return d;
    }
    
    /**
     * Similar to {@link #showPreferencesDialog(Window, ArchitectSwingSessionContext)} except it also
     * sets the selected tab to the JDBC preferences panel
     * @param owner The Window that is the owner of this dialog
     * @param context The application swing context, which contains the user preferences to be edited
     * @return The dialog containing the preference editor with the selected tab set to the JDBC preferences
     */
    public Window showJDBCDriverPreferences(Window owner, ArchitectSwingSession session) {
        logger.debug("showJDBCDriverPreferences"); //$NON-NLS-1$
        
        final JDialog d = SPSUtils.makeOwnedDialog(owner, Messages.getString("PreferencesEditor.jdbcDriversSection")); //$NON-NLS-1$
        
        JPanel cp = new JPanel(new BorderLayout(12,12));
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        cp.add(createLabelPanel(session), BorderLayout.NORTH);
        
        final DataSourceTypeEditor dsTypeEditor =
            new DataSourceTypeEditor(session.getDataSources(), owner, session.isEnterpriseSession());

        // Add the Kettle Options Panel as a tab to the SPDataSourceTypePanel
        final KettleDataSourceTypeOptionPanel kettleOptsPanel = new KettleDataSourceTypeOptionPanel();
        dsTypeEditor.addTab(Messages.getString("PreferencesEditor.kettleSection"), kettleOptsPanel); //$NON-NLS-1$
        
        final ArchitectPropertiesDataSourceTypeOptionPanel architectPropPanel = new ArchitectPropertiesDataSourceTypeOptionPanel();
        dsTypeEditor.addTab(Messages.getString("PreferencesEditor.propertiesSection"), architectPropPanel);
        cp.add(dsTypeEditor.getPanel(), BorderLayout.CENTER); //$NON-NLS-1$
    
        JDefaultButton okButton = new JDefaultButton(DataEntryPanelBuilder.OK_BUTTON_LABEL);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dsTypeEditor.applyChanges();
                d.setVisible(false);
            }
        });
    
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                dsTypeEditor.discardChanges();
                d.setVisible(false);
            }
        };
        
        cancelAction.putValue(Action.NAME, DataEntryPanelBuilder.CANCEL_BUTTON_LABEL);
        JButton cancelButton = new JButton(cancelAction);

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);

        SPSUtils.makeJDialogCancellable(d, cancelAction);
        d.getRootPane().setDefaultButton(okButton);
        cp.add(buttonPanel, BorderLayout.SOUTH);
        d.setContentPane(cp);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true); 
        return d;
    }
    
    private JPanel createLabelPanel(ArchitectSwingSession session) {
        FormLayout layout = new FormLayout("pref:grow, pref, pref:grow", "pref"); //$NON-NLS-1$ //$NON-NLS-2$
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        JLabel label = new JLabel();
        label.setText((session.isEnterpriseSession() ? 
                "Enterprise Server JDBCDrivers on " + ((ArchitectSwingSessionImpl) session).getServerName() 
                :"Local JDBCDrivers"));
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setIcon((session.isEnterpriseSession() ? SERVER_JDBCDRIVER_ICON : LOCAL_JDBCDRIVER_ICON));
        fb.add(label, "2, 1");
        
        return fb.getPanel();
    }
}
