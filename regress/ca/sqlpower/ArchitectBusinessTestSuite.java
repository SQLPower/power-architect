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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import prefs.AllPrefsTests;
import ca.sqlpower.architect.TestArchitectDataSource;
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
		suite.addTestSuite(TestArchitectDataSource.class);
		suite.addTestSuite(TestSQLObjectChildrenInsert.class);
        

		//$JUnit-END$
		return suite;
	}
}