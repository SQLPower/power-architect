package ca.sqlpower.architect.swingui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.CoreUserSettings;

public interface ArchitectSwingSessionContext {

    /**
     * The size, in pixels, of the icons in the toolbar.  This used to be managed as a
     * user preference, but we no longer maintain two sets of icons at different pixel
     * sizes.
     */
    public static final int ICON_SIZE = 16;


    /**
     * The URL where the Architect support forum is available.
     */
    static final String FORUM_URL = "http://www.sqlpower.ca/forum/";
    
    /**
     * The URL where there is more information about finding and configuring
     * JDBC drivers.
     */
    static final String DRIVERS_URL = "http://www.sqlpower.ca/forum/posts/list/401.page";
    
    /**
     * Creates a new session within this parent context.  This will cause an
     * Architect Frame to appear on the user's desktop with a new empty project
     * in it.
     * <p>
     * Important note: This method must be called on the Swing Event Dispatch
     * Thread.  See SwingUtilities.invokeLater() for a way of ensuring this method
     * is called on the proper thread.
     * @throws ArchitectException 
     */
    public abstract ArchitectSwingSession createSession() throws ArchitectException;

    /**
     * Reads the given file, then creates a new session. Uses the showGUI parameter to 
     * determine whether or not to show a new ArchitectFrame for this session
     * 
     * @param projectFile
     * @param showGUI
     * @return
     * @throws ArchitectException
     * @throws FileNotFoundException 
     */
    public abstract void createSession(File projectFile, boolean showGUI) throws ArchitectException, FileNotFoundException,  IOException;

    /**
     * Creates a new session which houses the given project.  This is the createSession()
     * method that all the others delegate to.
     * 
     * @param project
     * @return
     * @throws ArchitectException
     */
    public abstract ArchitectSwingSession createSession(String projectName) throws ArchitectException;

    /**
     * Returns true iff this context is running on a Mac OS X machine.  Some
     * UI features are different under that platform to increase the illusion
     * that the Architect is a native application.
     */
    public abstract boolean isMacOSX();

    /**
     * Returns the recent files menu.
     */
    public abstract RecentMenu getRecentMenu();

    /**
     * Returns the user preferences node associated with this context.
     */
    public abstract Preferences getPrefs();

    /**
     * Gets the user settings for this session 
     */
    public abstract CoreUserSettings getUserSettings();

    /**
     * Returns a collection containing all the sessions from this context. 
     */
    public Collection<ArchitectSwingSession> getSessions();
}