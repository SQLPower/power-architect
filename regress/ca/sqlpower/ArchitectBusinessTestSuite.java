package ca.sqlpower;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import regress.ca.sqlpower.architect.ArchitectExceptionTest;
import regress.ca.sqlpower.architect.ArchitectUtilsTest;
import regress.ca.sqlpower.architect.JDBCClassLoaderTest;
import regress.ca.sqlpower.architect.LogWriterTest;
import regress.ca.sqlpower.architect.PLDotIniTest;
import regress.ca.sqlpower.architect.PlDotIniListenersTest;
import regress.ca.sqlpower.architect.SQLObjectMagicTest;
import regress.ca.sqlpower.architect.SQLObjectTest;
import regress.ca.sqlpower.architect.TestArchitectDataSource;
import regress.ca.sqlpower.architect.TestFolder;
import regress.ca.sqlpower.architect.TestSQLCatalog;
import regress.ca.sqlpower.architect.TestSQLColumn;
import regress.ca.sqlpower.architect.TestSQLDatabase;
import regress.ca.sqlpower.architect.TestSQLRelationship;
import regress.ca.sqlpower.architect.TestSQLTable;
import regress.ca.sqlpower.architect.ddl.TestDDLUtils;
import regress.ca.sqlpower.architect.diff.CompareSQLTest;
import regress.ca.sqlpower.architect.diff.SQLComparatorTest;
import regress.ca.sqlpower.architect.diff.SQLRelationshipComparatorTest;
import regress.ca.sqlpower.architect.undo.TestSQLObjectChildrenInsert;

public class ArchitectBusinessTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for regress");
		//$JUnit-BEGIN$
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
		//$JUnit-END$
		return suite;
	}
}