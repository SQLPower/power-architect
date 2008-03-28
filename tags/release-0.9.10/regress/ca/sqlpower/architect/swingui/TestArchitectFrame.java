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
package ca.sqlpower.architect.swingui;

import junit.framework.TestCase;

public class TestArchitectFrame extends TestCase {
	
	private ArchitectFrame af;
    private ArchitectSwingSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
		session = context.createSession();
        af = session.getArchitectFrame();
	}
	
	public void testAutoLayoutAction() {
		assertNotNull(af.getAutoLayoutAction());
		assertSame(session.getPlayPen(), af.getAutoLayoutAction().getPlayPen());
		
		// FIXME: should check that the toolbar button of the action exists!
	}
}
