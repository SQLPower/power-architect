package prefs;

import java.util.HashMap;
import java.util.Map;

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
}
