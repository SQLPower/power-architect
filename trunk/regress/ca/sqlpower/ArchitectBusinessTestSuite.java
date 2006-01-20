package regress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import regress.ca.sqlpower.architect.ArchitectExceptionTest;
import regress.ca.sqlpower.architect.JDBCClassLoaderTest;
import regress.ca.sqlpower.architect.LogWriterTest;
import regress.ca.sqlpower.architect.TestSQLColumn;
import regress.ca.sqlpower.architect.TestSQLDatabase;
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
		suite.addTestSuite(TestSQLColumn.class);
		suite.addTestSuite(JDBCClassLoaderTest.class);
		suite.addTestSuite(LogWriterTest.class);
		suite.addTestSuite(TestDDLUtils.class);
		suite.addTestSuite(TestSQLTable.class);
		//$JUnit-END$
		return suite;
	}
}
