/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        logger.debug("showPreferencesDialog");
        
        // XXX Can't easily use ArchitectPanelBuilder since this
        // contains a JTabbedPane which is not an ArchitectPanel.
        if (d == null) {
            d = SPSUtils.makeOwnedDialog(owner, "User Preferences");
            JPanel cp = new JPanel(new BorderLayout(12,12));
            tp = new JTabbedPane();
            cp.add(tp, BorderLayout.CENTER);
            cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
    
            final PreferencesPanel prefPanel = new PreferencesPanel(context);
            tp.add("General", prefPanel);
    
            final DataSourceTypeEditor dsTypeEditor =
                new DataSourceTypeEditor(context.getPlDotIni());
    
            // Add the Kettle Options Panel as a tab to the SPDataSourceTypePanel
            
            final KettleDataSourceTypeOptionPanel kettleOptsPanel = new KettleDataSourceTypeOptionPanel();
            
            dsTypeEditor.addTab("Kettle", kettleOptsPanel);
            
            tp.add("JDBC Drivers", dsTypeEditor.getPanel());
    
        
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
