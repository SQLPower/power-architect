package ca.sqlpower.architect.swingui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.CoreUserSettings;

/**
 * Minimally functional session context implementation that creates and returns an instance of
 * StubArchitectSwingSession on the createSession method calls
 */
public class TestingArchitectSwingSessionContext implements ArchitectSwingSessionContext {
    
    private Preferences prefs;
    private CoreUserSettings userSettings;
    private RecentMenu recent;

    private static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    public TestingArchitectSwingSessionContext() throws IOException {
        prefs = Preferences.userNodeForPackage(ArchitectSwingSessionContextImpl.class);
        userSettings = new CoreUserSettings(prefs);
        recent = new RecentMenu(this) {
            @Override
            public void loadFile(String fileName) throws IOException {
            }
        };

        File newPlIniFile = File.createTempFile("pl_test", "ini");
        newPlIniFile.deleteOnExit();
        userSettings.setPlDotIniPath(newPlIniFile.getPath());
    }
    
    public ArchitectSwingSession createSession() throws ArchitectException {
        return new TestingArchitectSwingSession(this);
    }

    public ArchitectSwingSession createSession(String projectName) throws ArchitectException {
        return this.createSession();
    }
    
    public Collection<ArchitectSwingSession> getSessions() {
        // TODO Auto-generated method stub
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

    public void createSession(File projectFile, boolean showGUI) throws ArchitectException, FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        
    }
}
