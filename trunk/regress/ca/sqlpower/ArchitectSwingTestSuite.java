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
import ca.sqlpower.architect.swingui.TestDBTree;
import ca.sqlpower.architect.swingui.TestPlayPen;
import ca.sqlpower.architect.swingui.TestPlayPenComponent;
import ca.sqlpower.architect.swingui.TestRelationship;
import ca.sqlpower.architect.swingui.TestSwingUIProject;
import ca.sqlpower.architect.swingui.TestTableEditPane;
import ca.sqlpower.architect.swingui.TestTablePane;
import ca.sqlpower.architect.swingui.action.TestDeleteSelectedAction;
import ca.sqlpower.architect.swingui.table.DateRendererTest;
import ca.sqlpower.architect.swingui.table.DecimalRendererTest;
import ca.sqlpower.architect.swingui.table.PercentRendererTest;
import ca.sqlpower.architect.swingui.table.SQLObjectRendererTest;
import ca.sqlpower.architect.undo.TestSQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.undo.TestUndoManager;

public class ArchitectSwingTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Architect's Swing GUI");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestArchitectFrame.class);
		suite.addTestSuite(TestAutoLayoutAction.class);
		suite.addTestSuite(TestBasicRelationshipUI.class);
		suite.addTestSuite(TestDBTree.class);
		suite.addTestSuite(TestColumnEditPanel.class);
		suite.addTestSuite(TestDeleteSelectedAction.class);
		suite.addTestSuite(TestCompareDMPanel.class);
		suite.addTestSuite(TestFruchtermanReingoldForceLayout.class);
		suite.addTestSuite(TestRelationship.class);
		suite.addTestSuite(TestPlayPen.class);
		suite.addTestSuite(TestPlayPenComponent.class);
        suite.addTestSuite(DateRendererTest.class);
        suite.addTestSuite(DecimalRendererTest.class);
        suite.addTestSuite(PercentRendererTest.class);
        suite.addTestSuite(SQLObjectRendererTest.class);
		suite.addTestSuite(TestSwingUIProject.class);
		suite.addTestSuite(TestSQLObjectUndoableEventAdapter.class);
		suite.addTestSuite(TestTableEditPane.class);
		suite.addTestSuite(TestTablePane.class);
		suite.addTestSuite(TestUndoManager.class);
		//$JUnit-END$
		return suite;
	}

}
