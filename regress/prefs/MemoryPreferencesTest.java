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
