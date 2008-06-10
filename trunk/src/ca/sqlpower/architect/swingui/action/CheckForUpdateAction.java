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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JOptionPane;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

public class CheckForUpdateAction extends AbstractArchitectAction {
    
    private static final String WEBSITE_BASE_URL = "http://dhcp-126.sqlpower.ca:8080/sqlpower_website/page/";
    
    private ArchitectSwingSession session;
    private String mostCurrVersion;
    
    public CheckForUpdateAction(ArchitectSwingSession session) {
        super(session, "Check for Software Updates", "Check for Software Updates");
        this.session = session;
    }
    
    /**
     * This sends a request to the server to get a response of the most current version number
     */
    public void actionPerformed(ActionEvent e) {
        try {
            
            URL baseURL = new URL(WEBSITE_BASE_URL);
            URL url = new URL(baseURL, "check_for_architect_update?currVersion=" + ArchitectVersion.APP_VERSION);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setDoOutput(false);
            urlc.setDoInput(true);
            urlc.connect();

            // have to read in order to send request!
            Reader in = new InputStreamReader(urlc.getInputStream());
            StringBuilder responseString = new StringBuilder();
            char[] buf = new char[2000];
            while (in.read(buf) > 0) {
                responseString.append(buf);
            }
            in.close();
            urlc.disconnect();
            
            mostCurrVersion = responseString.toString();
            
            String[] version = mostCurrVersion.split("\\.");
            for(String seg : version) {
                System.out.println(seg);
            }
            
            // If the latest is less(not possible but included anyways) than the user's version, prompt update.
            if (Integer.parseInt(version[0]) <= Integer.parseInt(ArchitectVersion.APP_VERSION_MAJOR)) {
                if (Integer.parseInt(version[1]) <= Integer.parseInt(ArchitectVersion.APP_VERSION_MINOR)) {
                    if (Integer.parseInt(version[2].substring(0, version[2].indexOf("-"))) <= Integer.parseInt(ArchitectVersion.APP_VERSION_TINY)) {
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
        } catch (Exception evt) {
            throw new RuntimeException("Exception occured while trying to retreive version number", evt);
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
