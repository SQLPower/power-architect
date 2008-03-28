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

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

/**
 * Minimally functional session context implementation that creates and returns an instance of
 * StubArchitectSwingSession on the createSession method calls
 */
public class TestingArchitectSwingSessionContext implements ArchitectSwingSessionContext {
    
    private Preferences prefs;
    private CoreUserSettings userSettings;
    private RecentMenu recent;
    
    /**
     * The parsed list of connections.
     */
    private DataSourceCollection plDotIni;
    
    /**
     * The location of the PL.INI file.
     */
    private String plDotIniPath;

    private static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    public TestingArchitectSwingSessionContext() throws IOException {
        prefs = Preferences.userNodeForPackage(ArchitectSwingSessionContextImpl.class);
        userSettings = new CoreUserSettings(prefs);
        recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
            }
        };

        File newPlIniFile = File.createTempFile("pl_test", "ini");
        newPlIniFile.deleteOnExit();
        setPlDotIniPath(newPlIniFile.getPath());
    }
    
    public ArchitectSwingSession createSession() throws ArchitectException {
        return new TestingArchitectSwingSession(this);
    }

    public ArchitectSession createSession(InputStream in) throws ArchitectException, IOException {
        return this.createSession();
    }
    
    public ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException {
        return this.createSession();
    }
    
    public ArchitectSwingSession createSession(ArchitectSwingSession openingSession) throws ArchitectException {
        return this.createSession();
    }
    
    public Collection<ArchitectSession> getSessions() {
        return null;
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public RecentMenu getRecentMenu() {
        return recent;
    }

    public CoreUserSettings getUserSettings() {
        return userSettings;
    }

    public boolean isMacOSX() {
        return MAC_OS_X;
    }

    public ArchitectSwingSession createSession(InputStream in, boolean showGUI) throws ArchitectException, FileNotFoundException, IOException {
        return null;
    }

    /**
     * Doesn't actually do anything!
     */
    public void closeAll() {
        // no op
    }

    /**
     * Always returns false.
     */
    public boolean getExitAfterAllSessionsClosed() {
        return false;
    }

    /**
     * Doesn't actually do anything!
     */
    public void setExitAfterAllSessionsClosed(boolean allowExit) {
        // no op
    }

    /**
     * Doesn't actually do anything!
     */
    public void showConnectionManager(Window owner) {
        // no op
    }

    /**
     * Doesn't actually do anything!
     */    
    public void showPreferenceDialog(Window owner) {
        // no op
    }

    public List<SPDataSource> getConnections() {
        return getPlDotIni().getConnections();
    }

    public DataSourceCollection getPlDotIni() {
        String path = getPlDotIniPath();
        if (path == null) return null;
        
        if (plDotIni == null) {
            plDotIni = new PlDotIni();
            try {
                plDotIni.read(getClass().getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
            } catch (IOException e) {
                throw new ArchitectRuntimeException(new ArchitectException("Failed to read system resource default_database_types.ini",e));
            }
            try {
                if (plDotIni != null) {
                    plDotIni.read(new File(path));
                }
            } catch (IOException e) {
                throw new ArchitectRuntimeException(new ArchitectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
        }
        return plDotIni;
    }

    public String getPlDotIniPath() {
        return plDotIniPath;
    }

    public void setPlDotIniPath(String plDotIniPath) {
        if (this.plDotIniPath != null && this.plDotIniPath.equals(plDotIniPath)) {
            return;
        }
        this.plDotIniPath = plDotIniPath;
        this.plDotIni = null;
    }
}
