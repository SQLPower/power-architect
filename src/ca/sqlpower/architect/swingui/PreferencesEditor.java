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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.action.EditTableAction;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DataSourceTypeEditor;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * This class is used to create and display the User Preferences dialog
 */
public class PreferencesEditor {
    
    private static final Logger logger = Logger.getLogger(EditTableAction.class);
    
    /**
     * The JDialog where we display the User Preferences
     */
    private JDialog d;
    
    private JTabbedPane tp;
    
    /**
     * If the dialog for the preferences editor hasn't been initialized, then initialize it
     * Otherwise, just set it to visible.
     * @param owner The Window that is the owner of this dialog
     * @param context The application swing context, which contains the user preferences to be edited
     * @return The dialog containing the preference editor
     */
    public Window showPreferencesDialog(Window owner, ArchitectSwingSessionContext context) {
        logger.debug("showPreferencesDialog"); //$NON-NLS-1$
        
        // XXX Can't easily use ArchitectPanelBuilder since this
        // contains a JTabbedPane which is not an ArchitectPanel.
        if (d == null) {
            d = SPSUtils.makeOwnedDialog(owner, Messages.getString("PreferencesEditor.userPreferencesDialogTitle")); //$NON-NLS-1$
            JPanel cp = new JPanel(new BorderLayout(12,12));
            tp = new JTabbedPane();
            cp.add(tp, BorderLayout.CENTER);
            cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
    
            final PreferencesPanel prefPanel = new PreferencesPanel(context);
            tp.add(Messages.getString("PreferencesEditor.generalSection"), prefPanel); //$NON-NLS-1$
    
            final DataSourceTypeEditor dsTypeEditor =
                new DataSourceTypeEditor(context.getPlDotIni());
    
            // Add the Kettle Options Panel as a tab to the SPDataSourceTypePanel
            
            final KettleDataSourceTypeOptionPanel kettleOptsPanel = new KettleDataSourceTypeOptionPanel();
            
            dsTypeEditor.addTab(Messages.getString("PreferencesEditor.kettleSection"), kettleOptsPanel); //$NON-NLS-1$
            
            tp.add(Messages.getString("PreferencesEditor.jdbcDriversSection"), dsTypeEditor.getPanel()); //$NON-NLS-1$
    
        
            JDefaultButton okButton = new JDefaultButton(DataEntryPanelBuilder.OK_BUTTON_LABEL);
            okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        prefPanel.applyChanges();
                        dsTypeEditor.applyChanges();
                        d.setVisible(false);
                    }
                });
        
            Action cancelAction = new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        prefPanel.discardChanges();
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
        }
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
    public Window showJDBCDriverPreferences(Window owner, ArchitectSwingSessionContext context) {
        Window w = showPreferencesDialog(owner, context);
        tp.setSelectedIndex(1);
        return w;
    }
}
