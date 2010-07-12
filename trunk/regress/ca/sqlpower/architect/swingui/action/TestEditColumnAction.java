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
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

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
		pp = session.getPlayPen();
		SQLTable tm = new SQLTable(session.getTargetDatabase(), true);
		session.getTargetDatabase().addTable(tm);
		tp = new TablePane(tm, pp.getContentPane());
		tp.getModel().setName("Table1");
		tp.getModel().addColumn(new SQLColumn(tp.getModel(),"col1",Types.INTEGER,1,1));
		tp.getModel().addColumn(new SQLColumn(tp.getModel(),"col2",Types.INTEGER,1,1));
		
		SQLTable tm2 = new SQLTable(session.getTargetDatabase(), true);
		session.getTargetDatabase().addTable(tm2);
		tp2 = new TablePane(tm2, pp.getContentPane());
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

	public void testTableSelected() throws SQLObjectException{
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action should be not enabled", editColumn.isEnabled());		
	}
	
	public void testRelationshipSelected() {
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action should be disabled", editColumn.isEnabled());		
	}
	
	public void testTableAndRelationshipSelected() {
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		r.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action should be disabled", editColumn.isEnabled());		
		r.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertFalse("Action should still be disabled", editColumn.isEnabled());
		tp.setSelected(false,SelectionEvent.SINGLE_SELECT);		
	}
	
	public void testColumnSelected() throws SQLObjectException{
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		assertEquals("Editing col1", editColumn.getValue(EditColumnAction.SHORT_DESCRIPTION));		
		tp.selectItem(1);
		assertTrue("Action not enabled", editColumn.isEnabled());		
		tp.selectNone();
		assertFalse("No column is selected, only tables, should disable",editColumn.isEnabled());		
	}
	
	public void testTableAndColumnSelected(){
		assertFalse("Action enabled with no items",editColumn.isEnabled());
		tp.setSelected(true,SelectionEvent.SINGLE_SELECT);
		tp.selectItem(0);
		tp2.setSelected(true,SelectionEvent.SINGLE_SELECT);				
		assertFalse("Action not enabled", editColumn.isEnabled());
		tp2.setSelected(false,SelectionEvent.SINGLE_SELECT);
		assertTrue("Action not enabled", editColumn.isEnabled());		
		tp.selectNone();
		assertFalse("Only table is selected, should disable",editColumn.isEnabled());
	}

}
