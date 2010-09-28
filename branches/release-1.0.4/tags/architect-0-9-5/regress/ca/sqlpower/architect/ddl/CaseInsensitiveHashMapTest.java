package ca.sqlpower.architect.ddl;

import java.util.Map;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;

import junit.framework.TestCase;

public class CaseInsensitiveHashMapTest extends TestCase {
    Map map = new CaseInsensitiveHashMap();
    
    public void testPutGet() {
        SQLObject o1 = new SQLTable();
        map.put("abc", o1);
        assertSame(map.get("aBC"), o1);
    }
    public void testNullGet() {
        assertNull(map.get(null));
    }
}
