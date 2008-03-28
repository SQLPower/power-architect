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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import prefs.AllPrefsTests;
import ca.sqlpower.architect.TestFolder;
import ca.sqlpower.architect.TestSQLCatalog;
import ca.sqlpower.architect.TestSQLColumn;
import ca.sqlpower.architect.TestSQLDatabase;
import ca.sqlpower.architect.TestSQLIndex;
import ca.sqlpower.architect.TestSQLIndexColumn;
import ca.sqlpower.architect.TestSQLRelationship;
import ca.sqlpower.architect.TestSQLTable;
import ca.sqlpower.architect.ddl.TestDDLUtils;
import ca.sqlpower.architect.undo.TestSQLObjectChildrenInsert;

/**
 * This suite consists of the business tests whose class names do not
 * conform to the standard junit class name format *Test.java. See
 * the {@link ArchitectAutoTests} class for the rest of the suite.
 */
public class ArchitectBusinessTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for regress");

		// AllPrefsTests must be first, as it calls LoadFakeTestPrefs to load the static preferences initialization.

		//$JUnit-BEGIN$
		suite.addTest(AllPrefsTests.suite());
		suite.addTest(TestSQLDatabase.suite());
		suite.addTestSuite(TestSQLCatalog.class);
        suite.addTest(TestFolder.suite());
		suite.addTest(TestSQLTable.suite());
		suite.addTest(TestSQLColumn.suite());
        suite.addTestSuite(TestSQLIndex.class);
        suite.addTestSuite(TestSQLIndexColumn.class);
		suite.addTestSuite(TestSQLRelationship.class);
		suite.addTestSuite(TestDDLUtils.class);
		suite.addTestSuite(TestSQLObjectChildrenInsert.class);
        

		//$JUnit-END$
		return suite;
	}
}