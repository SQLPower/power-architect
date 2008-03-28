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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * All of the junit tests that we currently have for the Architect.  Combines
 * the non-standardly named business and gui tests with the auto tests which
 * picks up all the test classes whose name matches *Test.
 */
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