package ca.sqlpower.architect;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

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
public class ArchitectSession {
	protected static ArchitectSession instance;
	protected UserSettings userSettings;
	protected JDBCClassLoader jdbcClassLoader;
	protected List<String> driverJarList;

	protected ArchitectSession() {
		driverJarList = new LinkedList();
		jdbcClassLoader = new JDBCClassLoader(this);
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
			instance = new ArchitectSession();
		}
		return instance;
	}

	// --------------- accessors and mutators ------------------

	/**
	 * See {@link #userSettings}.
	 *
	 * @return the value of userSettings
	 */
	public UserSettings getUserSettings()  {
		return this.userSettings;
	}

	/**
	 * See {@link #userSettings}.
	 *
	 * @param argUserSettings Value to assign to this.userSettings
	 */
	public void setUserSettings(UserSettings argUserSettings) {
		this.userSettings = argUserSettings;
	}

	public List<String> getDriverJarList() {
		return Collections.unmodifiableList(driverJarList);
	}

	/**
	 * Adds an entry to the list of JDBC driver JAR files.  The
	 * ConfigFile class uses this when loading the user settings file.
	 */
	public boolean addDriverJar(String fullPath) {
		return driverJarList.add(fullPath);
	}
	public boolean removeDriverJar(String fullPath) {
		return driverJarList.remove(fullPath);
	}
	
	public void clearDriverJarList() {
		driverJarList.clear();
	}

	public JDBCClassLoader getJDBCClassLoader() {
		return jdbcClassLoader;
	}

}
