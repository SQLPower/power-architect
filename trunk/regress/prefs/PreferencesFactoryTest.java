package prefs;

import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectFrame;

public class PreferencesFactoryTest extends TestCase {

	public final void testPreReqs() {
		PreferencesFactory.init();
		String n = System.getProperty(PreferencesFactory.PREFS_FACTORY_SYSTEM_PROPERTY);
		System.out.println(n);
		String message = "Did you remember to run with -D"+PreferencesFactory.PREFS_FACTORY_SYSTEM_PROPERTY+"="+PreferencesFactory.MY_CLASS_NAME+"?";
		System.out.println(message);
		assertEquals(message,
				PreferencesFactory.MY_CLASS_NAME, n);
		
	}
	/*
	 * Test method for 'regress.prefs.PreferencesFactory.systemRoot()'
	 */
	public final void testSystemRoot() {
		
		Object o = Preferences.systemRoot();
		assertNotNull(o);
		System.out.println(o);
	}

	/*
	 * Test method for 'regress.prefs.PreferencesFactory.userRoot()'
	 */
	public final void testUserRoot() {
		Object o = Preferences.userNodeForPackage(ArchitectFrame.class);
		assertNotNull(o);
		System.out.println(o);
	}

	@Override
	protected void tearDown() throws Exception {
	}
}
