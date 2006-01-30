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
import regress.ca.sqlpower.architect.TestFolder;
import regress.ca.sqlpower.architect.TestSQLCatalog;
import regress.ca.sqlpower.architect.TestSQLColumn;
import regress.ca.sqlpower.architect.TestSQLDatabase;
import regress.ca.sqlpower.architect.TestSQLRelationship;
import regress.ca.sqlpower.architect.TestSQLTable;
import regress.ca.sqlpower.architect.ddl.TestDDLUtils;
import regress.ca.sqlpower.architect.swingui.SaveLoadTest;
import regress.ca.sqlpower.architect.swingui.TestUndoManager;
import regress.ca.sqlpower.architect.swingui.TestColumnEditPanel;
import regress.ca.sqlpower.architect.undo.TestSQLObjectChildrenInsert;

public class ArchitectMegaTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Everything");
		//$JUnit-BEGIN$
		
		suite.addTest(ArchitectBusinessTestSuite.suite());
		suite.addTest(ArchitectSwingTestSuite.suite());
		//$JUnit-END$
		return suite;
	}
}