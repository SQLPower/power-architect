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

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * The KettleDataSourceOptionsPanel is an editor for all data source
 * options specific to the Architect's Kettle ETL integration features.
 */
public class KettleDataSourceOptionsPanel implements DataEntryPanel {

    private static final Logger logger = Logger.getLogger(KettleDataSourceOptionsPanel.class);
    
    /**
     * The panel that holds the GUI.
     */
    private final JPanel panel;
    
    /**
     * The data source for whose properties this panel is an editor.
     */
    private final SPDataSource dbcs;
    
    private JTextField kettleHostName;
    private JTextField kettlePort;
    private JTextField kettleDatabase;
    private JTextField kettleLogin;
    private JPasswordField kettlePassword;

    /**
     * Creates a panel for editing the Kettle-specific properties of
     * the given data source. It is not possible to change which data
     * source this new instance edits.
     * 
     * @param dbcs The data source to edit.  Null is not allowed.
     */
    public KettleDataSourceOptionsPanel(SPDataSource dbcs) {
        this.panel = buildKettleOptionsPanel();
        this.dbcs = dbcs;
        
        parentTypeChanged(dbcs.getParentType());
        
        kettleHostName.setText(dbcs.get(KettleOptions.KETTLE_HOSTNAME_KEY));
        kettlePort.setText(dbcs.get(KettleOptions.KETTLE_PORT_KEY));
        kettleDatabase.setText(dbcs.get(KettleOptions.KETTLE_DATABASE_KEY));
        kettleLogin.setText(dbcs.get(KettleOptions.KETTLE_REPOS_LOGIN_KEY));
        kettlePassword.setText(dbcs.get(KettleOptions.KETTLE_REPOS_PASSWORD_KEY));
    }
    
    /**
     * Creates a GUI panel for options which are required for interacting
     * with Kettle, and are not already covered on the general pref panel.
     */
    private JPanel buildKettleOptionsPanel() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, pref:grow"));
        builder.append("Hostname", kettleHostName = new JTextField());
        builder.append("Port", kettlePort = new JTextField());
        builder.append("Database", kettleDatabase = new JTextField());
        builder.append("Repository Login &Name", kettleLogin = new JTextField());
        builder.append("Repository &Password", kettlePassword = new JPasswordField());
        return builder.getPanel();
    }

    /**
     * Sets each of the database fields to be enabled only if their
     * data doesn't exist in the url.
     */
    public void parentTypeChanged(SPDataSourceType dsType) {
        Map<String, String> map = dsType.retrieveURLDefaults();
        logger.error(" The map is: " + map);
        if (map.containsKey(KettleOptions.KETTLE_HOSTNAME)) {
            kettleHostName.setEnabled(false);
        } else {
            kettleHostName.setEnabled(true);
        }
        if (map.containsKey(KettleOptions.KETTLE_PORT)) {
            kettlePort.setEnabled(false);
        } else {
            kettlePort.setEnabled(true);
        }
        if (map.containsKey(KettleOptions.KETTLE_DATABASE)) {
            kettleDatabase.setEnabled(false);
        } else {
            kettleDatabase.setEnabled(true);
        }
    }

    
    // --------- DataEntryPanel interface -----------
    
    public boolean applyChanges() {
        
        dbcs.put(KettleOptions.KETTLE_DATABASE_KEY, kettleDatabase.getText());
        dbcs.put(KettleOptions.KETTLE_PORT_KEY, kettlePort.getText());
        dbcs.put(KettleOptions.KETTLE_HOSTNAME_KEY, kettleHostName.getText());
        dbcs.put(KettleOptions.KETTLE_REPOS_LOGIN_KEY, kettleLogin.getText());
        dbcs.put(KettleOptions.KETTLE_REPOS_PASSWORD_KEY, new String(kettlePassword.getPassword()));

        return true;
    }

    public void discardChanges() {
        // nothing to chuck
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return false;
    }
}
