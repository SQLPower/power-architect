/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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

		// Point this at the top-level of the output folder when running JUnit
	    // (i.e. java -Dca.sqlpower.architect.test.dir=/path/to/tests)
	    String compiledTestPath = System.getProperty("ca.sqlpower.architect.test.dir");
	    if (compiledTestPath == null) {
	        throw new RuntimeException(
	                "Please define the system property ca.sqlpower.architect.test.dir" +
	                " to point to the directory where your test classes were compiled" +
	                " to (the directory you specify must contain the \"ca\" directory)");
	    }
		File file = new File(compiledTestPath);
		if (!file.exists()) {
		    throw new RuntimeException("Given test root dir doesn't exist: " + 
		            compiledTestPath);
		}
		if (!new File(file, "ca").exists()) {
            throw new RuntimeException("Given test root dir is not valid: " + 
                    compiledTestPath + " (it doesn't contain a directory " +
                    		"called \"ca\")");
		}

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
