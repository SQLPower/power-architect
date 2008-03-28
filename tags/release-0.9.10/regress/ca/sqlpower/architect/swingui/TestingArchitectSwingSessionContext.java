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
