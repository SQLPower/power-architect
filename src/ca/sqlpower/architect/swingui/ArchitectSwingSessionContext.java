package ca.sqlpower.architect.swingui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.CoreUserSettings;

/**
 * The ArchitectSwingSessionContext interface specifies a set of
 * properties and methods for creating new Architect Swing Sessions.
 * Additionally, the session context is the gateway to information
 * that is specific to the current user's environment (as opposed
 * to information that is attached to specific projects, which is
 * stored in the session).
 */
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
     * Creates a new session within this parent context.  This will cause an
     * Architect Frame to appear on the user's desktop with a new empty project
     * in it.
     * <p>
     * <b>Important note:</b> If showGUI is true, this method must be called on
     * the Swing Event Dispatch Thread.  See SwingUtilities.invokeLater() for a
     * way of ensuring this method is called on the proper thread.
     * 
     * @param showGUI True if you want this session to have its own (visible)\
     * ArchitectFrame instance; false for an invisible session.
     * @return The new session
     */
    public abstract ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException;
    
    /**
     * Creates a new session by loading the Architect XML project description
     * from the given input stream.
     * 
     * @param in The input stream to read the XML data from
     * @param showGUI True if you want this session to have its own (visible)\
     * ArchitectFrame instance; false for an invisible session.
     * @return The new session
     */
    public abstract ArchitectSwingSession createSession(InputStream in, boolean showGUI) throws ArchitectException, IOException;

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