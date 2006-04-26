package prefs;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

public class MemoryPreferencesTest extends TestCase {

	public void testParts() {
		assertEquals("c", MemoryPreferences.getNamePart("a/b/c"));
		assertEquals("a/b", MemoryPreferences.getDirectoryPart("a/b/c"));
		assertEquals("/a/b", MemoryPreferences.getDirectoryPart("/a/b/c"));
		assertTrue(MemoryPreferences.isAbsolutePath("/a/b/c"));
		assertFalse(MemoryPreferences.isAbsolutePath("a/b/c"));
	}
	
	public void testStrings() {
		Map nodes = new HashMap();
		MemoryPreferences mp = new MemoryPreferences(nodes, true, "/a/b/c");
		assertEquals("foo", mp.get("goo", "foo"));
		mp.put("foo", "google");
		assertEquals("google", mp.get("foo", "bleah"));
	}
	
	public void testInts() {
		Map nodes = new HashMap();
		MemoryPreferences mp = new MemoryPreferences(nodes, true, "/a/b/c");
		assertEquals(123, mp.getInt("goo", 123));
		mp.putInt("foo", 456);
		assertEquals(456, mp.getInt("foo", 42));
	}
	
	/**
	 * You can NOT use the importPreferences() method with this implementation because we
	 * subclass Preferences directly, whereas Sun's implementation of Preferences requires
	 * you do subclass it via AbstractPreferences (this requirement is not explicitly documented, Sun).
	 */
	public void DONOTtestLoad() throws Exception {
		// The Import code is shared static, so it should work even with our implementation
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
			"<!DOCTYPE map SYSTEM 'http://java.sun.com/dtd/preferences.dtd'>" +
			"<map MAP_XML_VERSION='1.0'><entry key='displayFontName' value='helvetica'/>" +
			"<entry key='textFontName' value='times-roman'/></map>";
		ByteArrayInputStream rdr = new ByteArrayInputStream(xml.getBytes());
		Preferences.importPreferences(rdr);
		String val = Preferences.userRoot().get("helvetica", "I give up");
		System.out.println(val);
	}
}
