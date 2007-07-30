/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
