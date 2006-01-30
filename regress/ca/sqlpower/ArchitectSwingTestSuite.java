package regress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import regress.ca.sqlpower.architect.swingui.SaveLoadTest;
import regress.ca.sqlpower.architect.swingui.TestArchitectFrame;
import regress.ca.sqlpower.architect.swingui.TestAutoLayoutAction;
import regress.ca.sqlpower.architect.swingui.TestColumnEditPanel;
import regress.ca.sqlpower.architect.swingui.TestPlayPen;
import regress.ca.sqlpower.architect.swingui.TestSwingUIProject;
import regress.ca.sqlpower.architect.swingui.TestUndoManager;

public class ArchitectSwingTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Architect's Swing GUI");
		//$JUnit-BEGIN$
		suite.addTestSuite(SaveLoadTest.class);			// could be merged into TestSwingUIProject
		suite.addTestSuite(TestSwingUIProject.class);
		suite.addTestSuite(TestArchitectFrame.class);
		suite.addTestSuite(TestAutoLayoutAction.class);
		suite.addTestSuite(TestPlayPen.class);
		suite.addTestSuite(TestUndoManager.class);
		suite.addTestSuite(TestColumnEditPanel.class);
		//$JUnit-END$
		return suite;
	}

}
