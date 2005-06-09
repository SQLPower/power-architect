package ca.sqlpower.architect.swingui;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class SwingUserSettings {

	private static Logger logger = Logger.getLogger(SwingUserSettings.class);

	public static final String DIVIDER_LOCATION 
		= "ca.sqlpower.architect.swing.SwingUserSettings.DIVIDER_LOCATION";

	public static final String MAIN_FRAME_X
		= "ca.sqlpower.architect.swing.SwingUserSettings.MAIN_FRAME_X";

	public static final String MAIN_FRAME_Y
		= "ca.sqlpower.architect.swing.SwingUserSettings.MAIN_FRAME_Y";

	public static final String MAIN_FRAME_WIDTH
		= "ca.sqlpower.architect.swing.SwingUserSettings.MAIN_FRAME_WIDTH";

	public static final String MAIN_FRAME_HEIGHT
		= "ca.sqlpower.architect.swing.SwingUserSettings.MAIN_FRAME_HEIGHT";

	public static final String ICON_SIZE
		= "ca.sqlpower.architect.swing.SwingUserSettings.ICON_SIZE";

    public static final String PLAYPEN_RENDER_ANTIALIASED
    		= "ca.sqlpower.architect.swing.SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED";

	protected Map settings;

	public SwingUserSettings() {
		super();
		settings = new HashMap();
	}

	/**
	 * Gets the named property from the settings map.  If the value in
	 * the map is a Number, the value is obtained by calling
	 * <code>intValue()</code> on it.  If it is a String, it is
	 * converted with <code>Integer.parseInt()</code>. Otherwise, the
	 * default value is returned a warning is logged using
	 * <code>logger</code>.  If there is no such value in the map,
	 * the default is returned without logging a warning.
	 */
	public int getInt(String propName, int defaultValue) {
		Object result = settings.get(propName);
		if (result == null) {
			return defaultValue;
		} else if (result instanceof Number) {
			return ((Number) result).intValue();
		} else if (result instanceof String) {
			try {
				return Integer.parseInt((String) result);
			} catch (NumberFormatException e) {
				logger.warn("Couldn't parse value '"+result
							+"' as integer for property '"+propName+"'");
			}
		}			
		logger.warn("Using default value for integer property '"+propName+"' because of unknown data type for existing value");
		return defaultValue;
	}

	public void setInt(String propName, int value) {
		settings.put(propName, new Integer(value));
	}

	/**
	 * Gets the named property from the settings map.  If the value in
	 * the map is a Boolean, the value is obtained by calling
	 * <code>booleanValue()</code> on it.  If it is a String, it is
	 * converted with <code>Boolean.parseBoolean()</code>. Otherwise, the
	 * default value is returned and a warning is logged using
	 * <code>logger</code>.  If there is no such value in the map,
	 * the default is returned without logging a warning.
	 */
	public boolean getBoolean(String propName, boolean defaultValue) {
		Object result = settings.get(propName);
		if (result == null) {
			return defaultValue;
		} else if (result instanceof Boolean) {
			return ((Boolean) result).booleanValue();
		} else if (result instanceof String) {
		    return Boolean.valueOf((String) result).booleanValue();
		}			
		logger.warn("Using default value for boolean property '"+propName+"' because of unknown data type for existing value");
		return defaultValue;
	}

	public void setBoolean(String propName, boolean value) {
		settings.put(propName, new Boolean(value));
	}

	public void setObject(String propName, Object value) {
		settings.put(propName, value);
	}

	public void putSetting(String propName, String propClassName, String propValue) {
		Object prop = null;
		if (propClassName.equals("java.lang.Integer")) {
			try {
				prop = new Integer(propValue);
			} catch (NumberFormatException e) {
				logger.warn("Invalid integer setting "+propName+"="+propValue);
				return;
			}
		} else if (propClassName.equals("java.lang.Boolean")) {
		    prop = new Boolean(propValue);
		} else {
			logger.warn("Unknown property class "+propClassName);
			return;
		}
		settings.put(propName, prop);
	}

	public String getString(String propName, String defaultValue) {
		Object result = settings.get(propName);
		if (result == null) {
			return defaultValue;
		} else {
			return result.toString();
		}	
	}

	public Object getObject(String propName, Object defaultValue) {
		Object result = settings.get(propName);
		if (result == null) {
			return defaultValue;
		} else {
			return result;
		}	
	}

	/**
	 * Returns the names of all settings currently held by this
	 * SwingUserSettings object.  They will all be Strings.
	 */
	public Set getSettingNames() {
		return settings.keySet();
	}
}
