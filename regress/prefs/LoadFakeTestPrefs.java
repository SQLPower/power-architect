package prefs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

public class LoadFakeTestPrefs extends TestCase {
	
	public void testLoad() throws Exception {

		InputStream rdr = new FileInputStream("testbed/statictestprefs.xml");
		Preferences.importPreferences(rdr);
		assertTrue("get value from xml-loaded prefs", Preferences.userRoot().getBoolean("staticTestPrefsLoaded", false));
		System.err.println("statictestprefs loaded OK");
	}
}
