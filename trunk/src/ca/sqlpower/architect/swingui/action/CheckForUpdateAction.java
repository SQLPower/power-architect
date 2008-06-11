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
    private String mostCurrVersion;

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

            mostCurrVersion = properties.getProperty("app.version");
            
            if(mostCurrVersion == null || mostCurrVersion.equals("")) new Exception();
            String[] version = mostCurrVersion.split("\\.");

            // If the latest is less(not possible but included anyways) than the user's version, prompt update.
            if (Integer.parseInt(ArchitectVersion.APP_VERSION_MAJOR) >= Integer.parseInt(version[0])) {
                if (Integer.parseInt(ArchitectVersion.APP_VERSION_MINOR) >= Integer.parseInt(version[1])) {
                    if (Integer.parseInt(ArchitectVersion.APP_VERSION_TINY) >= Integer.parseInt(version[2].contains("-") ? 
                            version[2].substring(0, version[2].indexOf("-")) : version[2])) {
                        JOptionPane.showMessageDialog(this.session.getArchitectFrame(), "Congratulations, your copy of Power*Architect is up to date.",
                                "The latest version of Power*Architect is: " + mostCurrVersion, JOptionPane.INFORMATION_MESSAGE);
                        setEnabled(false);
                        return;
                    }
                    else promptUpdate();
                    return;
                }
                else promptUpdate();
                return;
            } 
            else promptUpdate();
            return;

        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this.session.getArchitectFrame(), "Failed to check for software update.",
                    "You version of Power*Arhitect is: " + ArchitectVersion.APP_VERSION, JOptionPane.ERROR_MESSAGE);
            logger.error("Fail to compare version number");
        }
    }

    /**
     * This method is to be modified later according to specifications of software update
     */
    private void promptUpdate() {
        JOptionPane.showMessageDialog(this.session.getArchitectFrame(), "You are using an early version of Power*Architect, " +
                "please visit our website for a software update.", "The latest version of Power*Architect is: " + 
                mostCurrVersion, JOptionPane.INFORMATION_MESSAGE);

    }
}
