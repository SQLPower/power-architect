package prefs;

import java.util.prefs.Preferences;

import ca.sqlpower.architect.swingui.ArchitectFrame;

import junit.framework.TestCase;

public class PreferencesFactoryTest extends TestCase {
	
	public final void testPreReqs() {
		System.out.println("PreferencesFactoryTest.testPreReqs()");
		String n = System.getProperty(PreferencesFactory.PREFS_FACTORY_SYSTEM_PROPERTY);
		System.out.println(n);
		String message = "Did you remember to run with -D"+PreferencesFactory.PREFS_FACTORY_SYSTEM_PROPERTY+"="+PreferencesFactory.MY_CLASS_NAME+"?";
		assertNotNull(message, n);
		assertEquals(message, PreferencesFactory.MY_CLASS_NAME, n);		
	}
	
	/*
	 * Test method for 'regress.prefs.PreferencesFactory.systemRoot()'
	 */
	public final void testSystemRoot() {
		System.out.println("PreferencesFactoryTest.testSystemRoot()");
		Object o = null;
		try {
			o = Preferences.systemRoot();
		} catch (Throwable bleah) {
			bleah.printStackTrace();
			return;
		}
		System.out.println("Default preferences.systemRoot = " + o);
		assertNotNull(o);
	}

	/*
	 * Test method for 'regress.prefs.PreferencesFactory.userRoot()'
	 */
	public final void testUserRoot() {
		System.out.println("PreferencesFactoryTest.testUserRoot()");
		Object o = Preferences.userNodeForPackage(ArchitectFrame.class);
		System.out.println(o);
		assertNotNull(o);
	}
}
