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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.util.BrowserUtil;

/**
 * Checks for a newer version available and prompts user to update when a newer version is found.
 *
 */
public class CheckForUpdateAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(CheckForUpdateAction.class);

    private static final String VERSION_FILE_URL = "http://power-architect.sqlpower.ca/current_version"; //$NON-NLS-1$

    private static final String UPDATE_URL = "http://download.sqlpower.ca/architect/current.html"; //$NON-NLS-1$

    private ArchitectSwingSession session;
    private String versionPropertyString;

    public CheckForUpdateAction(ArchitectSwingSession session) {
        super(session, Messages.getString("CheckForUpdateAction.name"), //$NON-NLS-1$
                Messages.getString("CheckForUpdateAction.description")); //$NON-NLS-1$
        this.session = session;
    }

    /**
     * Sends request to website to check for current version and displays result
     * message as appropriate.
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
            ArchitectVersion userVersion = ArchitectVersion.APP_VERSION;

            if (userVersion.compareTo(latestVersion) < 0) {
                promptUpdate();
            } else {
                JOptionPane.showMessageDialog(session.getArchitectFrame(), 
                        Messages.getString("CheckForUpdateAction.upToDate"), //$NON-NLS-1$
                        Messages.getString("CheckForUpdateAction.name"), //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            logger.error("Fail to retrieve current version number!"); //$NON-NLS-1$
            ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                    Messages.getString("CheckForUpdateAction.failedToCheckForUpdate"), ex); //$NON-NLS-1$
        }
    }

    /**
     * Prompts the user for update and opens browser to current version if accepted.
     */
    private void promptUpdate() {
        int response = JOptionPane.showConfirmDialog(session.getArchitectFrame(),
                Messages.getString("CheckForUpdateAction.newerVersionAvailable"), //$NON-NLS-1$
                Messages.getString("CheckForUpdateAction.name"), //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try {
                BrowserUtil.launch(UPDATE_URL);
            } catch (IOException e) {
                ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                        Messages.getString("CheckForUpdateAction.failedToUpdate"), e); //$NON-NLS-1$
            }
        }
    }
}
