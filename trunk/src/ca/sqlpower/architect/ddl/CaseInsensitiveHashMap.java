package ca.sqlpower.architect.ddl;

import java.util.HashMap;

public class CaseInsensitiveHashMap extends HashMap<String, Object> {
        @Override
        public Object put(String key, Object value) {
            return super.put(safeUppercaseKey(key), value);
        }
        
        @Override
        public Object get(Object key) {
            return super.get(safeUppercaseKey((String)key));
        }
        
        private static String safeUppercaseKey(String key) {
            return key == null ? null : key.toUpperCase();
        }
}
