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

import java.io.ByteArrayInputStream;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

public class MemoryPreferencesTest extends TestCase {
	
    static {
        System.setProperty("java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
    }
    
	Preferences root = Preferences.userNodeForPackage(Object.class);
	
	public void testSystemPropsSettings() {
		assertEquals(MemoryPreferences.class,root.getClass());
	}
	
	public void testStrings() {
		MemoryPreferences mp = new MemoryPreferences((AbstractPreferences) root, "tupper");
		assertEquals("foo", mp.get("goo", "foo"));
		mp.put("foo", "google");
		assertEquals("google", mp.get("foo", "bleah"));
	}
	
	public void testInts() {
		MemoryPreferences mp = new MemoryPreferences((AbstractPreferences) root, "mackenzie");
		assertEquals(123, mp.getInt("goo", 123));
		mp.putInt("foo", 456);
		assertEquals(456, mp.getInt("foo", 42));
	}
	
	/**
	 * Test loading of XML properties.
	 */
	public void testLoad() throws Exception {
		// The Import code is shared static, so it should work even with our implementation
		String xml = 
			"<?xml version='1.0' encoding='UTF-8'?>" +
			"<!DOCTYPE preferences SYSTEM 'http://java.sun.com/dtd/preferences.dtd'>" +
			"<preferences EXTERNAL_XML_VERSION='1.0'>" +
			"<root type='user'>" +
			"	<map/>" +
			"	<node name='structure'>" +
			"		<map>" +
			"			<entry key='displayFontName' value='helvetica'/>" +
			"			<entry key='textFontName' value='times-roman'/>" +
			"		</map>" +
			"	</node>" +
			"</root>" +
			"</preferences>"			;
		ByteArrayInputStream rdr = new ByteArrayInputStream(xml.getBytes());
		Preferences.importPreferences(rdr);
		String val = Preferences.userRoot().node("structure").get("textFontName", "I give up");
		assertEquals("get value from xml-loaded prefs", "times-roman", val);
	}
	
	public void testKids() throws Exception {
		String[] kidNames = { "child1", "child2", "child3" };
		for (String kidName : kidNames) {
			root.node(kidName);
		}
		String[] resultNames = root.childrenNames();
		assertEquals("child name sizes", kidNames.length, resultNames.length);
		for (int i = 0; i < resultNames.length; i++){
			assertEquals("child names", kidNames[i], resultNames[i]);
		}
	}
}
