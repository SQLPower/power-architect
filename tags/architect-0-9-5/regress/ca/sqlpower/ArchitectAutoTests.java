package ca.sqlpower;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

import junit.framework.Test;

import com.gargoylesoftware.base.testing.RecursiveTestSuite;
import com.gargoylesoftware.base.testing.TestFilter;

/**
 * Use the RecursiveTestSuite to run all tests whose name
 * ends in "Test" (enforced by the suite itself) that are
 * not Abstract (enforced by the Filter).
 */
public class ArchitectAutoTests {

	/**
	 *  TRY to load system prefs factory before anybody else uses prefs.
	 */
	static {
		System.getProperties().setProperty(
			"java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
		System.err.println("Warning: Changed PreferencesFactory to in-memory version;");
	}

	public static Test suite() throws IOException {

		// Point this at the top-level of the output folder
        // XXX This path should not be baked into the code like this.
		File file = new File("build");

        TestFilter filt = new TestFilter() {
            
            public boolean accept(Class aClass) {
                
                if (Modifier.isAbstract(aClass.getModifiers())) {
                    return false;
                }

                String name = aClass.getName();
                if (name.endsWith("TestSuite")) {
                    return false;
                }

                // No reject conditions found, so...
                return true;
            }
        };

		return new RecursiveTestSuite(file, filt);
	}

}
