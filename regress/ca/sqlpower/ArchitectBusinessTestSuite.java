package regress;

import regress.ca.sqlpower.architect.ArchitectExceptionTest;
import regress.ca.sqlpower.architect.TestSQLColumn;
import regress.ca.sqlpower.architect.TestSQLDatabase;
import regress.ca.sqlpower.architect.swingui.SaveLoadTest;
import junit.framework.*;

public class ArchitectBusinessTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for regress");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestSQLDatabase.class);
		suite.addTestSuite(ArchitectExceptionTest.class);
		suite.addTestSuite(SaveLoadTest.class);
		suite.addTestSuite(TestSQLColumn.class);
		//$JUnit-END$
		return suite;
	}
}
