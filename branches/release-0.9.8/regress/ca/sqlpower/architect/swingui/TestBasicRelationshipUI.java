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

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class TestBasicRelationshipUI extends TestCase {
	Relationship rel;
	PlayPen pp;
	BasicRelationshipUI relUI;
	TablePane tp2;
	
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		pp = session.getPlayPen();
		SQLTable t1 = new SQLTable(pp.getDatabase(), true);
		pp.getDatabase().addChild(t1);
		TablePane tp1 =new TablePane(t1, pp);
		pp.addTablePane(tp1, new Point(0,-10));
		SQLTable t2 = new SQLTable(pp.getDatabase(), true);
		pp.getDatabase().addChild(t2);
		tp2 =new TablePane(t2, pp);
		pp.addTablePane(tp2, new Point(-10,0));
		SQLRelationship sqlrel = new SQLRelationship();
		sqlrel.attachRelationship(t1, t2, false);
		rel = new Relationship(pp,sqlrel);
		rel.setPkTable(tp1);
		rel.setFkTable(tp2);
		relUI= new IERelationshipUI();
		relUI.installUI(rel);
	}
	
	public void testComputeBounds() {
		Rectangle2D bounds = rel.getBounds();
		rel.setBounds(122312,123,1,1);
		relUI.computeBounds();
		assertFalse("This should not change the relationship's bounds",rel.getBounds().equals(bounds));
	}

}
