package ca.sqlpower.architect.etl;

import ca.sqlpower.architect.AbstractUserSetting;

/**
 * The ETLUserSettings class stores project-independent ETL settings
 * that may differ from client machine to client machine, but not
 * project-to-project.
 *
 * @see ca.sqlpower.architect.CoreUserSettings
 * @see ca.sqlpower.architect.swingui.SwingUserSettings
 */
public class ETLUserSettings extends AbstractUserSetting {

	// ------ PROPERTY LIST KEYS ------
	
	public static final String PROP_PL_ENGINE_PATH
		= "ETLUserSettings.PROP_PL_ENGINE_PATH";
	
	public static final String PROP_ETL_LOG_PATH
		= "ETLUserSettings.PROP_ETL_LOG_PATH";

	
	// ------ CONSTRUCTORS/FACTORIES ------

	/**
	 * Creates a user settings instance initialised to the default
	 * values.
	 */
	public ETLUserSettings() {
		super();
	}
}
