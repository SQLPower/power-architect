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
package ca.sqlpower.architect.swingui.action;

import java.awt.Point;
import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;

public class TestDeleteSelectedAction extends TestCase {
	
	private DeleteSelectedAction deleteAction;
	private PlayPen pp;
	private TablePane tp;
	private Relationship r;
	private TablePane tp2;
    private ArchitectSwingSession session;
	
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        session = context.createSession();
		deleteAction = new DeleteSelectedAction(session);
		pp = session.getPlayPen();
		tp = new TablePane(new SQLTable(session.getTargetDatabase(),true),pp.getContentPane());
		tp.getModel().setName("Table1");
		tp.getModel().addColumn(new SQLColumn(tp.getModel(),"col1",Types.INTEGER,1,1));
		tp.getModel().addColumn(new SQLColumn(tp.getModel(),"col2",Types.INTEGER,1,1));
		
		tp2 = new TablePane(new SQLTable(session.getTargetDatabase(),true),pp.getContentPane());
		tp2.getModel().setName("Table2");
		tp2.getModel().addColumn(new SQLColumn(tp.getModel(),"col1",Types.INTEGER,1,1));
		tp2.getModel().addColumn(new SQLColumn(tp.getModel(),"col2",Types.INTEGER,1,1));
		
		SQLRelationship sqlRelationship = new SQLRelationship();
		pp.addTablePane(tp,new Point());
		pp.addTablePane(tp2, new Point());
		sqlRelationship.attachRelationship(tp.getModel(),tp.getModel(),false);
		r = new Relationship(sqlRelationship,pp.getContentPane());
		pp.addRelationship(r);
	}

	public void testTableSelected() throws ArchitectException{
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertEquals(1, session.getArchitectFrame().getDbTree().getSelectionCount());
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("Incorrect Tooltip", "Delete Table1 (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
	}
	
	public void testRelationshipSelected() {
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled", deleteAction.isEnabled());
		r.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
	}
	
	public void testTableAndRelationshipSelected() {
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("Delete 2 items (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		r.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled when we still have an enabled component", deleteAction.isEnabled());
		tp.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
	}
	
	public void testColumnSelected() throws ArchitectException{
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		assertEquals("Delete col1 (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.selectItem(1);
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("tooltip incorrect for two selected columns","Delete 2 items (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.selectNone();
		assertTrue("Action not enable when columns unselected, but table selected",deleteAction.isEnabled());		
	}
	
	public void testTableAndColumnSelected(){
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		tp2.setSelected(true,SelectionEvent.SINGLE_SELECT);		
		assertEquals("Delete 2 items (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp2.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("tooltip incorrect for two selected columns","Delete col1 (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp.selectNone();
		assertTrue("Action not enable when columns unselected, but table selected",deleteAction.isEnabled());
	}
	
	public void testTableAndRelationshipAndColumnSelected(){
		assertFalse("Action enabled with no items",deleteAction.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		tp2.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled", deleteAction.isEnabled());
		assertEquals("Delete 3 items (Shortcut delete)",deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		r.setSelected(false,SelectionEvent.SINGLE_SELECT);
		tp.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled when we still have an enabled component", deleteAction.isEnabled());
		assertEquals("Delete Table2 (Shortcut delete)", deleteAction.getValue(DeleteSelectedAction.SHORT_DESCRIPTION));
		tp2.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertFalse ("Nothing is selected", deleteAction.isEnabled());
	}
}
