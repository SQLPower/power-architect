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

    private static final String VERSION_FILE_URL = "http://dhcp-126.sqlpower.ca:8080/sqlpower_website/architect.version.properties";

    private ArchitectSwingSession session;
    private String versionPropertyString;

    public CheckForUpdateAction(ArchitectSwingSession session) {
        super(session, "Check for Software Updates", "Check for Software Updates");
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
            urlc.setRequestMethod("GET");
            urlc.setDoInput(true);
            urlc.setDoOutput(false);
            urlc.connect();
            InputStream propertyInputStream = urlc.getInputStream();
            Properties properties = new Properties();
            properties.load(propertyInputStream);

            versionPropertyString = properties.getProperty("app.version");
            ArchitectVersion latestVersion = new ArchitectVersion(versionPropertyString);
            ArchitectVersion userVersion = ArchitectVersion.getAppVersionObject();
            
            if (userVersion.compareTo(latestVersion) == -1) {
                promptUpdate();
                return;
            }
            else {
                JOptionPane.showMessageDialog(this.session.getArchitectFrame(), "Congratulations, your copy of Power*Architect is up to date.",
                  "The latest version of Power*Architect is: " + latestVersion.toString(), JOptionPane.INFORMATION_MESSAGE);
                  setEnabled(false);
                  return;
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this.session.getArchitectFrame(), "Failed to check for software updates",
                    "Your version of Power*Architect is: " + ArchitectVersion.APP_VERSION, JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            logger.error("Fail to compare version number");
        }
    }

    /**
     * This method is to be modified later according to specifications of software update
     */
    private void promptUpdate() {
        JOptionPane.showMessageDialog(this.session.getArchitectFrame(), "You are using an early version of Power*Architect, " +
                "please visit our website for the latest version.", "The latest version of Power*Architect is: " + 
                versionPropertyString, JOptionPane.INFORMATION_MESSAGE);

    }
}
