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
 * load all business tests suites of the form test*
 */
public class ArchitectBusinessTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for regress");

		// AllPrefsTests must be first, as it calls LoadFakeTestPrefs to load the static preferences initialization.

		//$JUnit-BEGIN$
		suite.addTest(AllPrefsTests.suite());
		suite.addTest(TestSQLDatabase.suite());
		suite.addTestSuite(TestSQLCatalog.class);
		suite.addTestSuite(TestFolder.class);
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