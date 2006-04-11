package regress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import regress.ca.sqlpower.architect.layout.TestFruchtermanReingoldForceLayout;
import regress.ca.sqlpower.architect.swingui.CompareSchemaWorkerTest;
import regress.ca.sqlpower.architect.swingui.TestArchitectFrame;
import regress.ca.sqlpower.architect.swingui.TestAutoLayoutAction;
import regress.ca.sqlpower.architect.swingui.TestColumnEditPanel;
import regress.ca.sqlpower.architect.swingui.TestCompareDMPanel;
import regress.ca.sqlpower.architect.swingui.TestPlayPen;
import regress.ca.sqlpower.architect.swingui.TestRelationship;
import regress.ca.sqlpower.architect.swingui.TestSwingUIProject;
import regress.ca.sqlpower.architect.swingui.TestTablePane;
import regress.ca.sqlpower.architect.swingui.action.TestDeleteSelectedAction;
import regress.ca.sqlpower.architect.undo.TestSQLObjectUndoableEventAdapter;
import regress.ca.sqlpower.architect.undo.TestUndoManager;

public class ArchitectSwingTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Architect's Swing GUI");
		//$JUnit-BEGIN$		
		suite.addTestSuite(TestSwingUIProject.class);
		suite.addTestSuite(TestArchitectFrame.class);
		suite.addTestSuite(TestAutoLayoutAction.class);
		suite.addTestSuite(TestPlayPen.class);
		suite.addTestSuite(TestUndoManager.class);
		suite.addTestSuite(TestColumnEditPanel.class);
		suite.addTestSuite(TestSQLObjectUndoableEventAdapter.class);
		suite.addTestSuite(TestFruchtermanReingoldForceLayout.class);
		suite.addTestSuite(TestCompareDMPanel.class);
		suite.addTestSuite(TestTablePane.class);
		suite.addTestSuite(TestDeleteSelectedAction.class);
		suite.addTestSuite(TestRelationship.class);
		//$JUnit-END$
		return suite;
	}

}
