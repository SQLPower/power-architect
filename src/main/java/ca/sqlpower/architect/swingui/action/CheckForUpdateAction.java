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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;

/**
 * Checks for a newer version available and prompts user to update when a newer version is found.
 *
 */
public class CheckForUpdateAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(CheckForUpdateAction.class);

    /**
     * GitHub API endpoint for the latest published release of Power*Architect.
     */
    private static final String VERSION_FILE_URL = "https://api.github.com/repos/SQLPower/power-architect/releases/latest"; //$NON-NLS-1$

    /**
     * The public releases page, opened in the browser when an update is accepted.
     */
    private static final String UPDATE_URL = "https://github.com/SQLPower/power-architect/releases"; //$NON-NLS-1$

    /**
     * Matches the {@code "tag_name":"..."} field in the GitHub API JSON response.
     */
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\""); //$NON-NLS-1$

    private String versionPropertyString;

    private HttpURLConnection urlc;
    private InputStream propertyInputStream;

    public CheckForUpdateAction(ArchitectFrame frame) {
        super(frame, Messages.getString("CheckForUpdateAction.name"), //$NON-NLS-1$
                Messages.getString("CheckForUpdateAction.description")); //$NON-NLS-1$
    }

    /**
     * Sends request to website to check for current version and displays result
     * message as appropriate.
     */
    public void actionPerformed(ActionEvent e) {
        checkForUpdate(true);
    }
    
    public void checkForUpdate(boolean verbose) {
        try {
            URL url = new URL(VERSION_FILE_URL);
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setAllowUserInteraction(false);
            urlc.setRequestMethod("GET"); //$NON-NLS-1$
            urlc.setRequestProperty("Accept", "application/vnd.github+json"); //$NON-NLS-1$
            urlc.setRequestProperty("User-Agent", "Power-Architect/" + ArchitectVersion.APP_FULL_VERSION); //$NON-NLS-1$
            urlc.setConnectTimeout(15000);
            urlc.setReadTimeout(15000);
            urlc.setDoInput(true);
            urlc.setDoOutput(false);
            urlc.connect();

            int responseCode = urlc.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub API returned HTTP " + responseCode); //$NON-NLS-1$
            }

            propertyInputStream = urlc.getInputStream();
            String body = readBody(propertyInputStream);

            String tagName = extractTagName(body);
            if (tagName == null) {
                throw new IOException("No tag_name found in GitHub releases response"); //$NON-NLS-1$
            }

            versionPropertyString = tagName;
            ArchitectVersion latestVersion = new ArchitectVersion(versionPropertyString);
            ArchitectVersion userVersion = ArchitectVersion.APP_FULL_VERSION;

            if (userVersion.compareTo(latestVersion) < 0) {
                promptUpdate();
            } else if (verbose) {
                JOptionPane.showMessageDialog(getSession().getArchitectFrame(), 
                        Messages.getString("CheckForUpdateAction.upToDate"), //$NON-NLS-1$
                        Messages.getString("CheckForUpdateAction.name"), //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            logger.error("Fail to retrieve current version number!"); //$NON-NLS-1$
            if (verbose) {
                ASUtils.showExceptionDialogNoReport(getSession().getArchitectFrame(),
                        Messages.getString("CheckForUpdateAction.failedToCheckForUpdate"), ex); //$NON-NLS-1$
            }
        } catch (NullPointerException e) {
            logger.error("Fail to retrieve current version number!"); //$NON-NLS-1$
            SPSUtils.showExceptionDialogNoReport(getSession().getArchitectFrame(), 
                    Messages.getString("CheckForUpdateAction.failedToCheckForUpdate"), 
                    "Fail to retrieve current version number!", e);
        } finally {
            urlc.disconnect();
            try {
                if (propertyInputStream != null) {
                    propertyInputStream.close();
                }
            } catch (IOException ex2) {
                logger.error("Exception while trying to close input stream."); //$NON-NLS-1$
                throw new RuntimeException(ex2);
            }
        }
    }

    /**
     * Reads the response body as a UTF-8 string.
     */
    private static String readBody(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    /**
     * Extracts the {@code tag_name} from a GitHub API releases/latest JSON body.
     * A leading {@code v} on the tag (e.g. {@code v1.0.9}) is stripped so the
     * result can be parsed by {@link ArchitectVersion}. Returns null if the
     * field is absent.
     */
    static String extractTagName(String jsonBody) {
        Matcher m = TAG_NAME_PATTERN.matcher(jsonBody);
        if (!m.find()) {
            return null;
        }
        String tag = m.group(1);
        if (tag.startsWith("v")) { //$NON-NLS-1$
            tag = tag.substring(1);
        }
        return tag;
    }

    /**
     * Prompts the user for update and opens browser to current version if accepted.
     */
    private void promptUpdate() {
        int response = JOptionPane.showConfirmDialog(getSession().getArchitectFrame(),
                Messages.getString("CheckForUpdateAction.newerVersionAvailable"), //$NON-NLS-1$
                Messages.getString("CheckForUpdateAction.name"), //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try {
                BrowserUtil.launch(UPDATE_URL);
            } catch (IOException e) {
                ASUtils.showExceptionDialogNoReport(getSession().getArchitectFrame(),
                        Messages.getString("CheckForUpdateAction.failedToUpdate"), e); //$NON-NLS-1$
            }
        }
    }
}
