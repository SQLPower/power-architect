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
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.RecentMenu;

/**
 * Minimally functional session context implementation that creates and returns an instance of
 * StubArchitectSwingSession on the createSession method calls
 */
public class TestingArchitectSwingSessionContext implements ArchitectSwingSessionContext {
    
    private Preferences prefs;
    private CoreUserSettings userSettings;
    private RecentMenu recent;
    private final List<ArchitectSession> sessions = new ArrayList<ArchitectSession>();
    
    /**
     * The parsed list of connections.
     */
    private DataSourceCollection<JDBCDataSource> plDotIni;
    
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
    
    public ArchitectSwingSession createSession() throws SQLObjectException {
        ArchitectSwingSession session = new TestingArchitectSwingSession(this);
        sessions.add(session);
        return session;
    }

    public ArchitectSession createSession(InputStream in) throws SQLObjectException, IOException {
        return this.createSession();
    }
    
    public ArchitectSwingSession createSession(boolean showGUI) throws SQLObjectException {
        return this.createSession();
    }
    
    public ArchitectSwingSession createSession(ArchitectSwingSession openingSession) throws SQLObjectException {
        return this.createSession();
    }
    
    public Collection<ArchitectSession> getSessions() {
        return sessions;
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

    public ArchitectSwingSession createSession(InputStream in, boolean showGUI) throws SQLObjectException, FileNotFoundException, IOException {
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

    public List<JDBCDataSource> getConnections() {
        return getPlDotIni().getConnections();
    }

    public DataSourceCollection<JDBCDataSource> getPlDotIni() {
        String path = getPlDotIniPath();
        if (path == null) return null;
        
        if (plDotIni == null) {
            PlDotIni<SPDataSource> newPlDotIni = new PlDotIni<SPDataSource>(SPDataSource.class);
            try {
                newPlDotIni.read(getClass().getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read system resource default_database_types.ini",e));
            }
            try {
                if (newPlDotIni != null) {
                    newPlDotIni.read(new File(path));
                }
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
            
            plDotIni = new SpecificDataSourceCollection<JDBCDataSource>(newPlDotIni, JDBCDataSource.class);
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

    public Transferable getClipboardContents() {
        throw new IllegalStateException("Getting clipboard contents not currently implemented.");
    }

    public void setClipboardContents(Transferable t) {
        throw new IllegalStateException("Setting clipboard contents not currently implemented.");        
    }
}
