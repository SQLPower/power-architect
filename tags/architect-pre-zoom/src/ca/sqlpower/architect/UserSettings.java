package ca.sqlpower.architect;

import java.util.List;
import java.util.LinkedList;

import ca.sqlpower.architect.swingui.SwingUserSettings;  // slight breech of MVC
import ca.sqlpower.sql.DBConnectionSpec;

public class UserSettings {

	/**
	 * A List of DBConnectionSpec objects.  The goal is to remember
	 * all databases the current user has ever connected to.
	 */
	protected List dbConnections;

	/**
	 * GUI-related settings.  This technically shouldn't be here
	 * (model is referencing view stuff) but it didn't seem right to
	 * make the settings file primarily swing-specific with a
	 * reference to the general architect prefs.
	 */
	protected SwingUserSettings swingSettings;

	public UserSettings() {
		super();
		dbConnections = new LinkedList();
		swingSettings = new SwingUserSettings();
	}
	
	public void addConnection(DBConnectionSpec dbcs) {
		dbConnections.add(dbcs);
	}

	public List getConnections() {
		return dbConnections;
	}

	public SwingUserSettings getSwingSettings() {
		return swingSettings;
	}

	public void setSwingSettings(SwingUserSettings sprefs) {
		swingSettings = sprefs;
	}
}
