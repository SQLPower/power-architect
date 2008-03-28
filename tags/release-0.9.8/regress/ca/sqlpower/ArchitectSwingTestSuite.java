/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import ca.sqlpower.architect.swingui.action.TestDeleteSelectedAction;
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
		suite.addTestSuite(TestDeleteSelectedAction.class);
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
