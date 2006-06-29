package ca.sqlpower;

import prefs.AllPrefsTests;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ca.sqlpower.architect.ArchitectExceptionTest;
import ca.sqlpower.architect.ArchitectUtilsTest;
import ca.sqlpower.architect.JDBCClassLoaderTest;
import ca.sqlpower.architect.LogWriterTest;
import ca.sqlpower.architect.PLDotIniTest;
import ca.sqlpower.architect.PlDotIniListenersTest;
import ca.sqlpower.architect.SQLObjectMagicTest;
import ca.sqlpower.architect.SQLObjectTest;
import ca.sqlpower.architect.TestArchitectDataSource;
import ca.sqlpower.architect.TestFolder;
import ca.sqlpower.architect.TestSQLCatalog;
import ca.sqlpower.architect.TestSQLColumn;
import ca.sqlpower.architect.TestSQLDatabase;
import ca.sqlpower.architect.TestSQLRelationship;
import ca.sqlpower.architect.TestSQLTable;
import ca.sqlpower.architect.ddl.TestDDLUtils;
import ca.sqlpower.architect.ddl.TestDDLWarningsTableModel;
import ca.sqlpower.architect.diff.CompareSQLTest;
import ca.sqlpower.architect.diff.SQLComparatorTest;
import ca.sqlpower.architect.diff.SQLRelationshipComparatorTest;
import ca.sqlpower.architect.undo.TestSQLObjectChildrenInsert;

public class ArchitectBusinessTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for regress");
		
		// AllPrefsTests must be first, as it calls LoadFakeTestPrefs to load the static preferences initialization.
		
		//$JUnit-BEGIN$
		suite.addTest(AllPrefsTests.suite());
		suite.addTestSuite(ArchitectUtilsTest.class);
		suite.addTestSuite(SQLObjectTest.class);
		suite.addTestSuite(SQLObjectMagicTest.class);
		suite.addTest(TestSQLDatabase.suite());
		suite.addTestSuite(TestSQLCatalog.class);
		suite.addTestSuite(TestFolder.class);
		suite.addTest(TestSQLTable.suite());
		suite.addTest(TestSQLColumn.suite());
		suite.addTestSuite(TestSQLRelationship.class);
		suite.addTestSuite(ArchitectExceptionTest.class);
		suite.addTestSuite(PLDotIniTest.class);
		suite.addTestSuite(PlDotIniListenersTest.class);
		suite.addTestSuite(JDBCClassLoaderTest.class);
		suite.addTestSuite(LogWriterTest.class);
		suite.addTestSuite(TestDDLUtils.class);
		suite.addTestSuite(TestArchitectDataSource.class);
		suite.addTestSuite(TestSQLObjectChildrenInsert.class);
		suite.addTestSuite(CompareSQLTest.class);
		suite.addTestSuite(SQLComparatorTest.class);
		suite.addTestSuite(SQLRelationshipComparatorTest.class);
        suite.addTestSuite(TestDDLWarningsTableModel.class);
		//$JUnit-END$
		return suite;
	}
}