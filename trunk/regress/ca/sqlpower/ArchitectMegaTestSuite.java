package ca.sqlpower;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ArchitectMegaTestSuite extends TestCase {

    static {
        System.setProperty("java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
    }
    
	public static Test suite() throws IOException {
		TestSuite suite = new TestSuite("Test Everything");
		//$JUnit-BEGIN$
		
		suite.addTest(ArchitectBusinessTestSuite.suite());
		suite.addTest(ArchitectSwingTestSuite.suite());
        suite.addTest(ArchitectAutoTests.suite());
		//$JUnit-END$
		return suite;
	}
}