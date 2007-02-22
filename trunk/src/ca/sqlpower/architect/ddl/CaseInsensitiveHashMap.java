package ca.sqlpower.architect.ddl;

import java.util.HashMap;

import ca.sqlpower.architect.SQLObject;

public class CaseInsensitiveHashMap extends HashMap<String, SQLObject> {
        @Override
        public SQLObject put(String key, SQLObject value) {
            return super.put(safeUppercaseKey(key), value);
        }

        @Override
        public SQLObject get(Object key) {
            return super.get(safeUppercaseKey((String)key));
        }

        private static String safeUppercaseKey(String key) {
            return key == null ? null : key.toUpperCase();
        }
}
