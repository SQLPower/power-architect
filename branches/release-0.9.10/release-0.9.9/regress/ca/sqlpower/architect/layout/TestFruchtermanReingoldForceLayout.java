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
package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;

public class TestFruchtermanReingoldForceLayout extends TestCase {

	private PlayPen pp;
	private TablePane tp;
	private SQLTable table1;
	private SQLDatabase db;
	private Rectangle frame;
	private FruchtermanReingoldForceLayout layout;
    private TestingArchitectSwingSessionContext context;
	
	public void setUp() throws ArchitectException, IOException {
	    context = new TestingArchitectSwingSessionContext();
		db = new SQLDatabase();
		pp = new PlayPen(context.createSession());
		table1= new SQLTable(db,true);
		tp = new TablePane(table1,pp);
		pp.addTablePane(tp,new Point(10,10));
		layout = new FruchtermanReingoldForceLayout();
		frame = new Rectangle(new Point(),layout.getNewArea(pp.getTablePanes()));
	}
	
	public void testMagnitude() {
		assertEquals("Incorrect magnitude",5.0,layout.magnitude(new Point(3,4)));
		assertEquals("Incorrect magnitude",13.0,layout.magnitude(new Point(12,5)));
	}

	public void testIsDoneNoElem() throws ArchitectException {
		PlayPen p = new PlayPen(context.createSession());
		layout.setup(p.getTablePanes(),p.getRelationships(),frame);
		assertTrue(layout.isDone());
	}
	
	public void testIsDoneOneElem() {
		layout.setup(pp.getTablePanes(),pp.getRelationships(),frame);
		assertTrue(layout.isDone());
	}
	
	public void testDone() throws ArchitectException {
		SQLTable sqlTable2 = new SQLTable(db,true);
		TablePane t2 =new TablePane(sqlTable2,pp);
		pp.addTablePane(t2,new Point(23,243));
		layout.setup(pp.getTablePanes(),pp.getRelationships(),frame);
		assertFalse(layout.isDone());
		layout.done();
		assertTrue(layout.isDone());
	}

	

}
