package prefs;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPrefsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for prefs");
		//$JUnit-BEGIN$
		suite.addTestSuite(LoadFakeTestPrefs.class);
		suite.addTestSuite(PreferencesFactoryTest.class);
		suite.addTestSuite(MemoryPreferencesTest.class);
		//$JUnit-END$
		return suite;
	}

}
