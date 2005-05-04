package ca.sqlpower.architect.ddl;

import java.util.Properties;

/**
 * The DDLUserSettings class stores project-independent DDL settings.
 *
 * @see ca.sqlpower.architect.UserSettings
 * @see ca.sqlpower.architect.etl.ETLUserSettings
 * @see ca.sqlpower.architect.swingui.SwingUserSettings
 */
public class DDLUserSettings {

	// ------ PROPERTY LIST KEYS ------	

	private static final String PROP_DDL_LOG_PATH
		= "ca.sqlpower.architect.etl.DDLUserSettings.PROP_DDL_LOG_PATH";

	
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
	public DDLUserSettings() {
		props = new Properties();
	}

	public static DDLUserSettings createFromPropList(Properties props) {
		DDLUserSettings settings = new DDLUserSettings();
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

	public String getDDLLogPath() {
		if (props.getProperty(PROP_DDL_LOG_PATH) != null) {
			return props.getProperty(PROP_DDL_LOG_PATH);
		} else {
			// default to user.home + "ddl.log"			
			return System.getProperty("user.home") + System.getProperty("file.separator") + "ddl.log";
		}			
	}

	public void setDDLLogPath(String v) {
		props.setProperty(PROP_DDL_LOG_PATH, v);
	}

}
