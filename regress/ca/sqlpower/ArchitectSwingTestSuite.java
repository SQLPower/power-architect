/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ca.sqlpower.architect.layout.TestFruchtermanReingoldForceLayout;
import ca.sqlpower.architect.swingui.TestArchitectFrame;
import ca.sqlpower.architect.swingui.TestAutoLayoutAction;
import ca.sqlpower.architect.swingui.TestBasicRelationshipUI;
import ca.sqlpower.architect.swingui.TestColumnEditPanel;
import ca.sqlpower.architect.swingui.TestDBTree;
import ca.sqlpower.architect.swingui.TestPlayPen;
import ca.sqlpower.architect.swingui.TestPlayPenComponent;
import ca.sqlpower.architect.swingui.TestRelationship;
import ca.sqlpower.architect.swingui.TestSwingUIProject;
import ca.sqlpower.architect.swingui.TestTableEditPane;
import ca.sqlpower.architect.swingui.TestTablePane;
import ca.sqlpower.architect.swingui.action.TestCreateRelationshipAction;
import ca.sqlpower.architect.swingui.action.TestDeleteSelectedAction;
import ca.sqlpower.architect.swingui.action.TestEditColumnAction;
import ca.sqlpower.architect.undo.TestSQLObjectUndoableEventAdapter;
import ca.sqlpower.architect.undo.TestUndoManager;

/**
 * This suite consists of the GUI tests whose class names do not
 * conform to the standard junit class name format *Test.java. See
 * the {@link ArchitectAutoTests} class for the rest of the suite.
 */
public class ArchitectSwingTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Architect's Swing GUI");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestArchitectFrame.class);
		suite.addTestSuite(TestAutoLayoutAction.class);
		suite.addTestSuite(TestBasicRelationshipUI.class);
		suite.addTestSuite(TestDBTree.class);
		suite.addTestSuite(TestColumnEditPanel.class);
		suite.addTestSuite(TestCreateRelationshipAction.class);
		suite.addTestSuite(TestDeleteSelectedAction.class);
		suite.addTestSuite(TestEditColumnAction.class);
		suite.addTestSuite(TestFruchtermanReingoldForceLayout.class);
		suite.addTestSuite(TestRelationship.class);
		suite.addTestSuite(TestPlayPen.class);
		suite.addTestSuite(TestPlayPenComponent.class);
		suite.addTestSuite(TestSwingUIProject.class);
		suite.addTestSuite(TestSQLObjectUndoableEventAdapter.class);
		suite.addTestSuite(TestTableEditPane.class);
		suite.addTestSuite(TestTablePane.class);
		suite.addTestSuite(TestUndoManager.class);
		//$JUnit-END$
		return suite;
	}

}
