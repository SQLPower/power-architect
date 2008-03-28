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
package ca.sqlpower.architect.swingui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.layout.BasicTreeAutoLayout;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;

public class TestAutoLayoutAction extends TestCase {
	
	private BasicTreeAutoLayout layout;
	private AutoLayoutAction layoutAction;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
		ArchitectSwingSession session = context.createSession();
        PlayPen pp = session.getPlayPen();
		layoutAction = new AutoLayoutAction(session, "Tree Layout", "Basic tree layout", "auto_layout");
		layout = new BasicTreeAutoLayout();
		pp.repaint();
		//action = af.getAutoLayoutAction();
		//layoutAction.setPlayPen(pp); Shouldn't have to do this anymore
		layoutAction.setLayout(layout);
		layoutAction.setAnimationEnabled(false);
	}
	
	public void testIcon() {
		assertNotNull(layoutAction.getValue(AbstractAction.SMALL_ICON));
	}
	
	public void testNoOverlaps() throws ArchitectException {
		PlayPen pp = layoutAction.getPlayPen();
		SQLDatabase ppdb = pp.getSession().getTargetDatabase();
		SQLTable t1 = new SQLTable(ppdb, "This is the name of the first table", "", "TABLE", true);
		SQLTable t2 = new SQLTable(ppdb, "This table is way cooler than the first one", "", "TABLE", true);
		
		TablePane tp1 = new TablePane(t1, pp);
		TablePane tp2 = new TablePane(t2, pp);
		
		pp.addTablePane(tp1, new Point(10,10));
		pp.addTablePane(tp2, new Point(20,20));
		
		// they start off overlapping
		assertTrue(tp1.getBounds().intersects(tp2.getBounds()));
		
		layoutAction.actionPerformed(new ActionEvent(this, 0, null));
		
		// they end up separated
		assertFalse(tp1.getBounds().intersects(tp2.getBounds()));
	}
	
	public void testNoCrossingLinesEasy() throws ArchitectException {
		PlayPen pp = layoutAction.getPlayPen();
		SQLDatabase ppdb = pp.getSession().getTargetDatabase();
		
		SQLTable tables[] = new SQLTable[4];
		TablePane tablePanes[] = new TablePane[tables.length];
		
		for (int i = 0; i < tables.length; i++) {
			tables[i] = new SQLTable(ppdb, "Table "+i, "", "TABLE", true);
			tablePanes[i] = new TablePane(tables[i], pp);
		}
		
		pp.addTablePane(tablePanes[0], new Point(100, 0));
		pp.addTablePane(tablePanes[1], new Point(300, 100));
		pp.addTablePane(tablePanes[2], new Point(150, 200));
		pp.addTablePane(tablePanes[3], new Point(0, 100));
		
		SQLRelationship sr1 = new SQLRelationship();
		sr1.attachRelationship(tables[0],tables[2],false);

		SQLRelationship sr2 = new SQLRelationship();
		sr2.attachRelationship(tables[1],tables[3],false);

		pp.setVisible(true);
		Relationship r1 = new Relationship(pp, sr1);
		Relationship r2 = new Relationship(pp, sr2);
		
		pp.addRelationship(r1);
		pp.addRelationship(r2);
		Graphics2D g = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).createGraphics();
		// the relationships init their paths only when painted
		r1.paint(g);
		r2.paint(g);
		
		
		// check that the relationships start out crossed
		assertTrue(((RelationshipUI) r1.getUI()).intersectsShape(((RelationshipUI) r2.getUI()).getShape()));

		
		// check that neither of the relationships intersects any of the 4 tables to start
		Rectangle b = new Rectangle();
		for (int i = 0; i < tablePanes.length; i++) {
			tablePanes[i].getBounds(b);
			if (tablePanes[i] != r1.getPkTable() && tablePanes[i] != r1.getFkTable()) {
				assertFalse("Table "+i+" crosses r1", ((RelationshipUI) r1.getUI()).intersects(b));
			}
			if (tablePanes[i] != r2.getPkTable() && tablePanes[i] != r2.getFkTable()) {
				assertFalse("Table "+i+" crosses r2", ((RelationshipUI) r2.getUI()).intersects(b));
			}
		}
		
		layoutAction.actionPerformed(new ActionEvent(this, 0, null));
        
		// make the paths update
		r1.paint(g);
		r2.paint(g);
		g.dispose();
		// check that the relationships are uncrossed
		assertFalse(((RelationshipUI) r1.getUI()).intersectsShape(((RelationshipUI) r2.getUI()).getShape()));

		
		// check that neither of the relationships intersects any of the 4 tables to start
		for (int i = 0; i < tablePanes.length; i++) {
			tablePanes[i].getBounds(b);
			if (tablePanes[i] != r1.getPkTable() && tablePanes[i] != r1.getFkTable()) {
				assertFalse("Table "+i+" crosses r1", ((RelationshipUI) r1.getUI()).intersects(b));
			}
			if (tablePanes[i] != r2.getPkTable() && tablePanes[i] != r2.getFkTable()) {
				assertFalse("Table "+i+" crosses r2", ((RelationshipUI) r2.getUI()).intersects(b));
			}
		}

	}
}
