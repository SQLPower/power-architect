package ca.sqlpower.architect;

import java.util.List;
import java.util.LinkedList;

import ca.sqlpower.architect.swingui.SwingUserSettings;  // slight breech of MVC
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.sql.DBConnectionSpec;

public class UserSettings {

	/**
	 * A List of DBConnectionSpec objects.  The goal is to remember
	 * all databases the current user has ever connected to.
	 */
	protected List dbConnections;

	/**
	 * A List of pathnames (in platform-dependant notation) to JAR
	 * files that the user's JDBC drivers come from.
	 */
	protected List driverJarList;

	/**
	 * GUI-related settings.  This technically shouldn't be here
	 * (model is referencing view stuff) but it didn't seem right to
	 * make the settings file primarily swing-specific with a
	 * reference to the general architect prefs.
	 */
	protected SwingUserSettings swingSettings;

	/**
	 * ETL-related settings.  This is not a design problem like
	 * swingSettings is, since ETL is part of the app's core
	 * functionality.
	 */
	protected ETLUserSettings etlUserSettings;
	
	public UserSettings() {
		super();
		dbConnections = new LinkedList();
		swingSettings = new SwingUserSettings();
		etlUserSettings = new ETLUserSettings();
		driverJarList = new LinkedList();
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

	public ETLUserSettings getETLUserSettings() {
		return etlUserSettings;
	}

	public void setETLUserSettings(ETLUserSettings v) {
		etlUserSettings = v;
	}

	public List getDriverJarList() {
		return driverJarList;
	}

	public void setDriverJarList(List v) {
		driverJarList = v;
	}
}
