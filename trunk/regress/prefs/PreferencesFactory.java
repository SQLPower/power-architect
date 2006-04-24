package regress.prefs;

import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public class PreferencesFactory implements java.util.prefs.PreferencesFactory {

	public static final String PREFS_FACTORY_SYSTEM_PROPERTY = "java.util.prefs.PreferencesFactory";

	public static final String MY_CLASS_NAME = "regress.prefs.PreferencesFactory";

	static String referenceFactoryClassName = getPlatformDefaultFactory();

	static PreferencesFactory system;

	private static Logger logger = Logger.getLogger(PreferencesFactory.class);

	static void init() {
		System.out.println("In static init method, ref factory class is " + referenceFactoryClassName);
		System.setProperty(PREFS_FACTORY_SYSTEM_PROPERTY, MY_CLASS_NAME);
		try {
			system = (PreferencesFactory) Class.forName(referenceFactoryClassName).newInstance();
		} catch (Exception e) {
			logger.error("Could not create Reference PreferencesFactory!!!!!", e);
		}
	}

	public Preferences systemRoot() {
		return system.systemRoot();
	}

	public Preferences userRoot() {
		return system.systemRoot().node("TEST");
	}
	

	/**
	 * Return the class name of the "platform-specific system-wide default" Factory
	 */
	static String getPlatformDefaultFactory() {
		if (
		    System.getProperty("os.name").startsWith("Windows")) {
		    return "java.util.prefs.WindowsPreferencesFactory";
		} else if  (System.getProperty("os.name").startsWith("Mac OS X")) {
			// return Mac-specific preference factory (introduced with 1.5-b63)
			return "java.util.prefs.MacOSXPreferencesFactory";
		} else { return
		     "java.util.prefs.FileSystemPreferencesFactory";
		
		}
	}
}
