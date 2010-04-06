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
package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public class TestFruchtermanReingoldForceLayout extends TestCase {

	private PlayPen pp;
	private TablePane tp;
	private SQLTable table1;
	private SQLDatabase db;
	private Rectangle frame;
	private FruchtermanReingoldForceLayout layout;
    private TestingArchitectSwingSessionContext context;
	
	public void setUp() throws SQLObjectException, IOException {
	    context = new TestingArchitectSwingSessionContext();
		db = new SQLDatabase();
		final ArchitectSwingSession session = context.createSession();
        pp = new PlayPen(session);
		table1= new SQLTable(db,true);
		tp = new TablePane(table1,pp.getContentPane());
		pp.addTablePane(tp,new Point(10,10));
		layout = new FruchtermanReingoldForceLayout();
		frame = new Rectangle(new Point(),layout.getNewArea(pp.getContentPane().getChildren(TablePane.class)));
	}
	
	public void testMagnitude() {
		assertEquals("Incorrect magnitude",5.0,layout.magnitude(new Point(3,4)));
		assertEquals("Incorrect magnitude",13.0,layout.magnitude(new Point(12,5)));
	}

	public void testIsDoneNoElem() throws SQLObjectException {
		final ArchitectSwingSession session = context.createSession();
        PlayPenContentPane pane = new PlayPen(session).getContentPane();
		layout.setup(pane.getChildren(TablePane.class),pane.getChildren(Relationship.class),frame);
		assertTrue(layout.isDone());
	}
	
	public void testIsDoneOneElem() {
	    PlayPenContentPane pane = pp.getContentPane();
		layout.setup(pane.getChildren(TablePane.class), pane.getChildren(Relationship.class),frame);
		assertTrue(layout.isDone());
	}
	
	public void testDone() throws SQLObjectException {
	    PlayPenContentPane pane = pp.getContentPane();
		SQLTable sqlTable2 = new SQLTable(db,true);
		TablePane t2 =new TablePane(sqlTable2,pane);
		pp.addTablePane(t2,new Point(23,243));
		layout.setup(pane.getChildren(TablePane.class),pane.getChildren(Relationship.class),frame);
		assertFalse(layout.isDone());
		layout.done();
		assertTrue(layout.isDone());
	}

	

}
