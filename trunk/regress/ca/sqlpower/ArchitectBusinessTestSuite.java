package regress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import regress.ca.sqlpower.architect.ArchitectExceptionTest;
import regress.ca.sqlpower.architect.JDBCClassLoaderTest;
import regress.ca.sqlpower.architect.LogWriterTest;
import regress.ca.sqlpower.architect.PLDotIniTest;
import regress.ca.sqlpower.architect.SQLObjectTest;
import regress.ca.sqlpower.architect.TestArchitectDataSource;
import regress.ca.sqlpower.architect.TestSQLColumn;
import regress.ca.sqlpower.architect.TestSQLDatabase;
import regress.ca.sqlpower.architect.TestSQLRelationship;
import regress.ca.sqlpower.architect.TestSQLTable;
import regress.ca.sqlpower.architect.ddl.TestDDLUtils;
import regress.ca.sqlpower.architect.swingui.SaveLoadTest;

public class ArchitectBusinessTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for regress");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestSQLDatabase.class);
		suite.addTestSuite(ArchitectExceptionTest.class);
		suite.addTestSuite(SaveLoadTest.class);
		suite.addTestSuite(PLDotIniTest.class);
		suite.addTest(TestSQLColumn.suite());
		suite.addTestSuite(JDBCClassLoaderTest.class);
		suite.addTestSuite(LogWriterTest.class);
		suite.addTestSuite(TestDDLUtils.class);
		suite.addTestSuite(SQLObjectTest.class);
		suite.addTestSuite(TestSQLTable.class);
		suite.addTestSuite(TestSQLRelationship.class);
		suite.addTestSuite(TestArchitectDataSource.class);
		//$JUnit-END$
		return suite;
	}
}
