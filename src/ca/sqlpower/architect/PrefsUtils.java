package ca.sqlpower.architect;

import java.util.prefs.Preferences;

public class PrefsUtils {

	/** Gets the Preferences node for a package, given one class in the package
	 * (either by reference or by reference to its Class object).
	 * @param mainClassInstance Either the object, or the object's Class object.
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
