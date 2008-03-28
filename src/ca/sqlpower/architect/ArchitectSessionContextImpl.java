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

package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;

public class ArchitectSessionContextImpl implements ArchitectSessionContext {
    
    private static final Logger logger = Logger.getLogger(ArchitectSessionContextImpl.class);

    /**
     * The preferences node that user-specific preferences are stored in.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(ArchitectSessionContextImpl.class);
    
    /**
     * The parsed list of connections.
     */
    private DataSourceCollection plDotIni;
    
    /**
     * The location of the PL.INI file.
     */
    private String plDotIniPath;
    
    /**
     * All live sessions that exist in (and were created by) this conext.  Sessions
     * will be removed from this list when they fire their sessionClosing lifecycle
     * event.
     */
    private final Collection<ArchitectSession> sessions;
    
    /**
     * Creates a new session context.  You will normally only need one of these
     * per JVM, but there is no technical barrier to creating multiple contexts.
     * <p>
     * Important note: This constructor must be called on the Swing Event Dispatch
     * Thread.  See SwingUtilities.invokeLater() for a way of ensuring this method
     * is called on the proper thread.
     * @throws ArchitectException 
     */
    public ArchitectSessionContextImpl() throws ArchitectException {
        sessions = new HashSet<ArchitectSession>();
        
        ArchitectUtils.startup();

        ArchitectUtils.configureLog4j();

        setPlDotIniPath(prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null));

        while (!isPlDotIniPathValid()) {
            String message;
            String[] options = new String[] {"Browse", "Create"};
            if (getPlDotIniPath() == null) {
                message = "location is not set";
            } else if (new File(getPlDotIniPath()).isFile()) {
                message = "file \n\n\""+getPlDotIniPath()+"\"\n\n could not be read";
            } else {
                message = "file \n\n\""+getPlDotIniPath()+"\"\n\n does not exist";
            }
            int choice = JOptionPane.showOptionDialog(null,   // blocking wait
                    "The Architect keeps its list of database connections" +
                    "\nin a file called PL.INI.  Your PL.INI "+message+"." +
                    "\n\nYou can browse for an existing PL.INI file on your system" +
                    "\nor allow the Architect to create a new one in your home directory." +
                    "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
                    "\nan existing PL.INI in your Power*Loader installation directory.",
                    "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);
            File newPlIniFile;
            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new ArchitectException("Can't start without a pl.ini file");
            } else if (choice == 0) {
                
                // Don't use recent files menu for default dir here.. we're looking for PL.INI
                JFileChooser fc = new JFileChooser();
                
                fc.setFileFilter(SPSUtils.INI_FILE_FILTER);
                fc.setDialogTitle("Locate your PL.INI file");
                int fcChoice = fc.showOpenDialog(null);       // blocking wait
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    newPlIniFile = fc.getSelectedFile();
                } else {
                    newPlIniFile = null;
                }
            } else if (choice == 1) {
                newPlIniFile = new File(System.getProperty("user.home"), "pl.ini");
            } else
                throw new ArchitectException("Unexpected return from JOptionPane.showOptionDialog to get pl.ini");

            if (newPlIniFile != null) try {
                newPlIniFile.createNewFile();
                setPlDotIniPath(newPlIniFile.getPath());
            } catch (IOException e1) {
                logger.error("Caught IO exception while creating empty PL.INI at \""
                        +newPlIniFile.getPath()+"\"", e1);
                ASUtils.showExceptionDialogNoReport("Failed to create file \""+newPlIniFile.getPath()+"\".", e1);
            }
        }
    }
    
    public ArchitectSession createSession() throws ArchitectException {
        return createSessionImpl("New Project");
    }

    public ArchitectSession createSession(InputStream in) throws ArchitectException, IOException {
        ArchitectSession session = createSessionImpl("Loading...");
        session.getProject().load(in, getPlDotIni());
        return session;
    }
    
    /**
     * This is the one createSession() implementation to which all other overloads of
     * createSession() actually delegate their work.
     * <p>
     * This method tracks all sessions that have been successfully created in the
     * {@link #sessions} field.
     * 
     * @param projectName
     * @return
     * @throws ArchitectException
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    private ArchitectSession createSessionImpl(String projectName) throws ArchitectException {
        logger.debug("About to create a new session for project \"" + projectName + "\"");
        ArchitectSessionImpl session = new ArchitectSessionImpl(this, projectName);
        sessions.add(session);
        
        return session;
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public Collection<ArchitectSession> getSessions() {
        return sessions;
    }

    private boolean isPlDotIniPathValid() {
        logger.debug("Checking pl.ini path: "+getPlDotIniPath());
        String path = getPlDotIniPath();
        if (path == null) {
            return false;
        } else {
            File f = new File(path);
            return (f.canRead() && f.isFile());
        }
    }
    
    /**
     * Tries to read the plDotIni if it hasn't been done already.  If it can't be read,
     * returns null and leaves the plDotIni property as null as well. See {@link #plDotIni}.
     */
    public DataSourceCollection getPlDotIni() {
        String path = getPlDotIniPath();
        if (path == null) return null;
        
        if (plDotIni == null) {
            plDotIni = new PlDotIni();
            try {
                logger.debug("Reading PL.INI defaults");
                plDotIni.read(getClass().getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
            } catch (IOException e) {
                throw new ArchitectRuntimeException(new ArchitectException("Failed to read system resource default_database_types.ini",e));
            }
            try {
                if (plDotIni != null) {
                    logger.debug("Reading new PL.INI instance");
                    plDotIni.read(new File(path));
                }
            } catch (IOException e) {
                throw new ArchitectRuntimeException(new ArchitectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
        }
        return plDotIni;
    }
    
    /**
     * See {@link #plDotIniPath}.
     */
    public String getPlDotIniPath() {
        return plDotIniPath;
    }
    
    /**
     * Sets the plDotIniPath property, and nulls out the current plDotIni
     * if the given value differs from the existing one.  See {@link #plDotIniPath}.
     */
    public void setPlDotIniPath(String plDotIniPath) {
        logger.debug("PlDotIniPath changing from \""+this.plDotIniPath+"\" to \""+plDotIniPath+"\"");

        // important to short-circuit when the value is not different
        // (if we don't, the prefs panel doesn't save properly)
        if (this.plDotIniPath != null && this.plDotIniPath.equals(plDotIniPath)) {
            return;
        }
        this.plDotIniPath = plDotIniPath;
        this.plDotIni = null;
    }
    
    public List<SPDataSource> getConnections() {
        return getPlDotIni().getConnections();
    }
}
