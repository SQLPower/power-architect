package ca.sqlpower;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ca.sqlpower.architect.layout.TestFruchtermanReingoldForceLayout;
import ca.sqlpower.architect.swingui.TestArchitectFrame;
import ca.sqlpower.architect.swingui.TestAutoLayoutAction;
import ca.sqlpower.architect.swingui.TestBasicRelationshipUI;
import ca.sqlpower.architect.swingui.TestColumnEditPanel;
import ca.sqlpower.architect.swingui.TestCompareDMPanel;
import ca.sqlpower.architect.swingui.TestPlayPen;
import ca.sqlpower.architect.swingui.TestPlayPenComponent;
import ca.sqlpower.architect.swingui.TestRelationship;
import ca.sqlpower.architect.swingui.TestSwingUIProject;
import ca.sqlpower.architect.swingui.TestTableEditPane;
import ca.sqlpower.architect.swingui.TestTablePane;
import ca.sqlpower.architect.swingui.action.TestDeleteSelectedAction;
import ca.sqlpower.architect.undo.TestSQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.undo.TestUndoManager;

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
		suite.addTestSuite(TestBasicRelationshipUI.class);
		suite.addTestSuite(TestPlayPenComponent.class);

		suite.addTestSuite(TestTableEditPane.class);
		//$JUnit-END$
		return suite;
	}

}
