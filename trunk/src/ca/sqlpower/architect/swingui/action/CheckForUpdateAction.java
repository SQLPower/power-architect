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

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

public class CheckForUpdateAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(CheckForUpdateAction.class);

    private static final String VERSION_FILE_URL = "http://power-architect.sqlpower.ca/current_version"; //$NON-NLS-1$

    private ArchitectSwingSession session;
    private String versionPropertyString;

    public CheckForUpdateAction(ArchitectSwingSession session) {
        super(session, Messages.getString("CheckForUpdateAction.name"), Messages.getString("CheckForUpdateAction.description")); //$NON-NLS-1$ //$NON-NLS-2$
        this.session = session;
    }

    /**
     * This sends a request to get access to architect.version.properties
     */
    public void actionPerformed(ActionEvent e) {
        try {
            URL url = new URL(VERSION_FILE_URL);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setAllowUserInteraction(false);
            urlc.setRequestMethod("GET"); //$NON-NLS-1$
            urlc.setDoInput(true);
            urlc.setDoOutput(false);
            urlc.connect();
            InputStream propertyInputStream = urlc.getInputStream();
            Properties properties = new Properties();
            properties.load(propertyInputStream);

            versionPropertyString = properties.getProperty("app.version"); //$NON-NLS-1$
            ArchitectVersion latestVersion = new ArchitectVersion(versionPropertyString);
            ArchitectVersion userVersion = ArchitectVersion.getAppVersionObject();
            
            if (userVersion.compareTo(latestVersion) == -1) {
                promptUpdate();
                return;
            }
            else {
                JOptionPane.showMessageDialog(this.session.getArchitectFrame(), Messages.getString("CheckForUpdateAction.upToDate"), //$NON-NLS-1$
                  Messages.getString("CheckForUpdateAction.latestVersionIs", latestVersion.toString()), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                  setEnabled(false);
                  return;
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this.session.getArchitectFrame(), Messages.getString("CheckForUpdateAction.failedToUpdate"), //$NON-NLS-1$
                    Messages.getString("CheckForUpdateAction.yourVersionIs", ArchitectVersion.APP_VERSION), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            ex.printStackTrace();
            logger.error("Fail to compare version number"); //$NON-NLS-1$
        }
    }

    /**
     * This method is to be modified later according to specifications of software update
     */
    private void promptUpdate() {
        JOptionPane.showMessageDialog(this.session.getArchitectFrame(), Messages.getString("CheckForUpdateAction.updatedVersionAvailable") + //$NON-NLS-1$
                Messages.getString("CheckForUpdateAction.updateInstructions"), Messages.getString("CheckForUpdateAction.latestVersionIs",  //$NON-NLS-1$ //$NON-NLS-2$
                versionPropertyString), JOptionPane.INFORMATION_MESSAGE);

    }
}
