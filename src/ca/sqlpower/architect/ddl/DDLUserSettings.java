package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.AbstractUserSetting;

/**
 * The DDLUserSettings class stores project-independent DDL settings.
 *
 * @see ca.sqlpower.architect.CoreUserSettings
 * @see ca.sqlpower.architect.etl.ETLUserSettings
 * @see ca.sqlpower.architect.swingui.SwingUserSettings
 */
public class DDLUserSettings extends AbstractUserSetting{

	// ------ PROPERTY LIST KEYS ------	

	public static final String PROP_DDL_LOG_PATH
		= "DDLUserSettings.PROP_DDL_LOG_PATH";

	

	// ------ CONSTRUCTORS/FACTORIES ------

	/**
	 * Creates a user settings instance initialised to the default
	 * values.
	 */
	public DDLUserSettings() {
		super();
	}
}
