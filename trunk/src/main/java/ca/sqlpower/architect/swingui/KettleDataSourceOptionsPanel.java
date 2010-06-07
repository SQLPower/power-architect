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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

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
    public KettleDataSourceOptionsPanel(@Nonnull JDBCDataSource dbcs) {
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
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, pref:grow")); //$NON-NLS-1$
        builder.append(Messages.getString("KettleDataSourceOptionsPanel.hostname"), kettleHostName = new JTextField()); //$NON-NLS-1$
        builder.append(Messages.getString("KettleDataSourceOptionsPanel.port"), kettlePort = new JTextField()); //$NON-NLS-1$
        builder.append(Messages.getString("KettleDataSourceOptionsPanel.database"), kettleDatabase = new JTextField()); //$NON-NLS-1$
        builder.append(Messages.getString("KettleDataSourceOptionsPanel.repositoryLoginName"), kettleLogin = new JTextField()); //$NON-NLS-1$
        builder.append(Messages.getString("KettleDataSourceOptionsPanel.repositoryPassword"), kettlePassword = new JPasswordField()); //$NON-NLS-1$
        return builder.getPanel();
    }

    /**
     * Sets each of the database fields to be enabled only if their
     * data doesn't exist in the url.
     */
    public void parentTypeChanged(JDBCDataSourceType dsType) {
        Map<String, String> map = dsType.retrieveURLDefaults();
        logger.error(" The map is: " + map); //$NON-NLS-1$
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
        return true;
    }
}
