package ca.sqlpower.architect;


import ca.sqlpower.architect.profile.TableProfileManager;

/**
 * The ArchitectSession class represents a single user's session with
 * the architect.  If using the Swing UI (currently this is the only
 * option, but that is subject to change), the ArchitectFrame has a
 * 1:1 relationship with an ArchitectSession.
 *
 * <p>The ArchitectSession is currently a singleton, but that is
 * subject to change if the Architect moves to an embeddable API
 * interface.  In that case, the getInstance method will change or
 * disappear, and more classes will require an ArchitectSession
 * argument in their constructors.
 *
 * @version $Id$
 * @author fuerth
 */
public class ArchitectSessionImpl implements ArchitectSession {
    
    protected static ArchitectSession instance;
	protected CoreUserSettings userSettings;
    private TableProfileManager profileManager;

	public ArchitectSessionImpl() {
        profileManager = new TableProfileManager();
	}

	/**
	 * Gets the single ArchitectSession instance for this JVM.
	 *
	 * <p>Note: in the future, the ArchitectSession may no longer be a
	 * singleton (for example, if the Architect gets a servlet or RMI
	 * interface).  In that case, getInstance will necessarily change
	 * or disappear.
	 */
	public static synchronized ArchitectSession getInstance() {
		if (instance == null) {
			instance = new ArchitectSessionImpl();
		}
		return instance;
	}

	// --------------- accessors and mutators ------------------

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.ArchitectSession#getUserSettings()
     */
	public CoreUserSettings getUserSettings()  {
		return this.userSettings;
	}

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.ArchitectSession#setUserSettings(ca.sqlpower.architect.CoreUserSettings)
     */
	public void setUserSettings(CoreUserSettings argUserSettings) {
		this.userSettings = argUserSettings;
	}

    public TableProfileManager getProfileManager() {
        return profileManager;
    }
}
