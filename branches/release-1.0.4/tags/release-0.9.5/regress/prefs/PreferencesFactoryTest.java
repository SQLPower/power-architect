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
package prefs;

import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectFrame;

public class PreferencesFactoryTest extends TestCase {
	
	public final void testPreReqs() {
		System.out.println("PreferencesFactoryTest.testPreReqs()");
		String n = System.getProperty(PreferencesFactory.PREFS_FACTORY_SYSTEM_PROPERTY);
		System.out.println(n);
		assertNotNull(MemoryPreferences.SYSTEM_PROPS_ERROR_MESSAGE, n);
		assertEquals(MemoryPreferences.SYSTEM_PROPS_ERROR_MESSAGE, PreferencesFactory.MY_CLASS_NAME, n);		
	}
	
	/*
	 * Test method for 'regress.prefs.PreferencesFactory.systemRoot()'
	 */
	public final void testSystemRoot() {
		System.out.println("PreferencesFactoryTest.testSystemRoot()");
		Object o = null;
		try {
			o = Preferences.systemRoot();
		} catch (Throwable bleah) {
			bleah.printStackTrace();
			return;
		}
		System.out.println("Default preferences.systemRoot = " + o);
		assertNotNull(o);
	}

	/*
	 * Test method for 'regress.prefs.PreferencesFactory.userRoot()'
	 */
	public final void testUserRoot() {
		System.out.println("PreferencesFactoryTest.testUserRoot()");
		Object o = Preferences.userNodeForPackage(ArchitectFrame.class);
		System.out.println(o);
		assertNotNull(o);
	}
}
