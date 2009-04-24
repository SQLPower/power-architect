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

import java.io.File;
import java.util.Arrays;
import java.util.prefs.Preferences;

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
        
        File tmp = File.createTempFile("Architect", "Test");
        ArchitectFrame.main(new String[]{tmp.getAbsolutePath()});
	}
	
	public void testAutoLayoutAction() {
		assertNotNull(af.getAutoLayoutAction());
		assertSame(session.getPlayPen(), af.getAutoLayoutAction().getPlayPen());
		
		// FIXME: should check that the toolbar button of the action exists!
	}
	
	/**
	 * Regression test for 1336. Loading a file from the command prompt should be remembered.
	 */
	public void testFileLoadFromCMDPrompt() throws Exception {
	    Preferences prefs = Preferences.userNodeForPackage(ArchitectSwingSessionImpl.class);

	    String recentFile = prefs.get("recentFile0", null);
	    
	    System.out.println(recentFile);
	    System.out.println("Key count " + prefs.keys().length);
	    System.out.println("Keys are " + Arrays.toString(prefs.keys()));
	}
}
