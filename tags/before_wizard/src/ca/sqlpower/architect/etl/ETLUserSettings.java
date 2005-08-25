package ca.sqlpower.architect.etl;

import java.util.Properties;

/**
 * The ETLUserSettings class stores project-independent ETL settings
 * that may differ from client machine to client machine, but not
 * project-to-project.
 *
 * @see ca.sqlpower.architect.UserSettings
 * @see ca.sqlpower.architect.swingui.SwingUserSettings
 */
public class ETLUserSettings {

	// ------ PROPERTY LIST KEYS ------
	
	private static final String PROP_PL_ENGINE_PATH
	= "ca.sqlpower.architect.etl.ETLUserSettings.PROP_PL_ENGINE_PATH";
	
	private static final String PROP_ETL_LOG_PATH
		= "ca.sqlpower.architect.etl.ETLUserSettings.PROP_ETL_LOG_PATH";

	
	// ------ INSTANCE VARIABLES ------

	/**
	 * A copy of the property list we were constructed with (empty if
	 * created from the public no-args constructor).
	 *
	 * <p>It is necessary to retain the initial list of properties this
	 * instance was constructed with because a newer version of the
	 * architect may have written additional properties which we don't
	 * want to drop when we save back to disk.
	 */
	protected Properties props;


	// ------ CONSTRUCTORS/FACTORIES ------

	/**
	 * Creates a user settings instance initialised to the default
	 * values.
	 */
	public ETLUserSettings() {
		props = new Properties();
	}

	public static ETLUserSettings createFromPropList(Properties props) {
		ETLUserSettings settings = new ETLUserSettings();
		settings.props.putAll(props);
		return settings;
	}

	// ------- INSTANCE METHODS -------

	/**
	 * Creates a Properties list and stores all settings to it in a
	 * string representation.  This method is only intended for the
	 * UserSettings class to serialize an instance of ETLUserSettings
	 * without having to know all the properties and how to convert
	 * them.  For normal getting/setting of properties, use the
	 * getXXX/setXXX methods.
	 */
	public Properties toPropList() {
		Properties propList = new Properties();
		propList.putAll(props);
		return propList;
	}

	/**
	 * This method is only intended for the UserSettings class to
	 * deserialize an instance of ETLUserSettings without having to
	 * know how to set the properties using their individual setXXX
	 * methods.
	 */
	public void putProperty(String name, String value) {
		props.setProperty(name, value);
	}

	// ------- ACCESSORS and MUTATORS -------


	public String getPowerLoaderEnginePath() {
		return props.getProperty(PROP_PL_ENGINE_PATH);
	}

	public void setPowerLoaderEnginePath(String v) {
		props.setProperty(PROP_PL_ENGINE_PATH, v);
	}
	
	
	// send back a default if nothing has been set yet
	public String getETLLogPath() {
		if (props.getProperty(PROP_ETL_LOG_PATH) != null) {
			return props.getProperty(PROP_ETL_LOG_PATH);
		} else {
			// default to user.home + "etl.log"			
			return System.getProperty("user.home") + System.getProperty("file.separator") + "etl.log";
		}
	}

	public void setETLLogPath(String v) {
		props.setProperty(PROP_ETL_LOG_PATH, v);
	}

}
