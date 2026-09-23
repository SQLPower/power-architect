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
	        File file = new File(".");
	        String path  = file.getAbsolutePath();
	        compiledTestPath = path.substring(0, path.length()-1) +"build_tests";      ;
	      }
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

                // External-database integration tests are excluded from the auto
                // suite: they need a live server or a specific external setup that
                // is not available in the standard CI/test environment (see also
                // the Gradle `exclude '**/SQLServerDDLGeneratorTest*'` in build.gradle).
                if (name.equals("ca.sqlpower.architect.ddl.SQLServerDDLGeneratorTest")
                        || name.equals("ca.sqlpower.architect.etl.kettle.KettleJobOutputToRepoTest")) {
                    return false;
                }

                // TestArchitectSwingWorker matches the *Test pattern but declares no
                // test methods, so the RecursiveTestSuite would otherwise fail with
                // "No tests found in ...". It is not a real test class.
                if (name.equals("ca.sqlpower.architect.swingui.TestArchitectSwingWorker")) {
                    return false;
                }

                // No reject conditions found, so...
                return true;
            }
        };

		return new RecursiveTestSuite(file, filt);
	}

}
