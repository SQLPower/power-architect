package ca.sqlpower.architect.ddl;

import java.util.Map;

import junit.framework.TestCase;

public class CaseInsensitiveHashMapTest extends TestCase {
    Map map = new CaseInsensitiveHashMap();
    
    public void testPutGet() {
        Object o1 = new Object();
        map.put("abc", o1);
        assertSame(map.get("aBC"), o1);
    }
    public void testNullGet() {
        assertNull(map.get(null));
    }
}
