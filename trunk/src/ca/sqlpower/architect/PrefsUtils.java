package ca.sqlpower.architect;

import java.util.prefs.Preferences;

public class PrefsUtils {

	/**
	 * @param mainClassInstance
	 */
	public static Preferences getUserPrefsNode(Object mainClassInstance) {
		Class clazz;
		if (mainClassInstance instanceof Class)
			clazz = (Class)mainClassInstance;
		else
			clazz = mainClassInstance.getClass();
		return Preferences.userNodeForPackage(clazz);
	}
}
