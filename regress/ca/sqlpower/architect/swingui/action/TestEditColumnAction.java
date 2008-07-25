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

public class TestEditColumnAction extends TestCase {

	private EditColumnAction editColumn;
	private PlayPen pp;
	private TablePane tp;
	private Relationship r;
	private TablePane tp2;
	
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		editColumn = new EditColumnAction(session);
		pp = session.getArchitectFrame().getPlayPen();
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
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertFalse("Action should be not enabled", editColumn.isEnabled());		
	}
	
	public void testRelationshipSelected() {
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		editColumn.itemSelected(new SelectionEvent(r, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertFalse("Action should be disabled", editColumn.isEnabled());		
	}
	
	public void testTableAndRelationshipSelected() {
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		editColumn.itemSelected(new SelectionEvent(r, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertFalse("Action should be disabled", editColumn.isEnabled());		
		r.setSelected(false,SelectionEvent.SINGLE_SELECT);
		editColumn.itemSelected(new SelectionEvent(r, SelectionEvent.DESELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertFalse("Action should still be disabled", editColumn.isEnabled());
		tp.setSelected(false,SelectionEvent.SINGLE_SELECT);		
	}
	
	public void testColumnSelected() throws ArchitectException{
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertEquals("Editing col1", editColumn.getValue(EditColumnAction.SHORT_DESCRIPTION));		
		tp.selectItem(1);
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertTrue("Action not enabled", editColumn.isEnabled());		
		tp.selectNone();
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertFalse("No column is selected, only tables, should disable",editColumn.isEnabled());		
	}
	
	public void testTableAndColumnSelected(){
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		tp2.setSelected(true,SelectionEvent.SINGLE_SELECT);				
		editColumn.itemSelected(new SelectionEvent(tp2, SelectionEvent.SELECTION_EVENT, SelectionEvent.SINGLE_SELECT));		
		assertFalse("Action not enabled", editColumn.isEnabled());
		tp2.setSelected(false,SelectionEvent.SINGLE_SELECT);
		editColumn.itemSelected(new SelectionEvent(tp2, SelectionEvent.DESELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertTrue("Action not enabled", editColumn.isEnabled());		
		tp.selectNone();
		editColumn.itemSelected(new SelectionEvent(tp, SelectionEvent.DESELECTION_EVENT, SelectionEvent.SINGLE_SELECT));
		assertFalse("Only table is selected, should disable",editColumn.isEnabled());
	}

}
