package ca.sqlpower.architect.etl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class ETLUtils {
    
    /** Just try and instantiate this class. */
    private ETLUtils() {}
    
    /**
     * Generates a map from source tables to all their target tables.
     * 
     * @param targetTables
     * @return A map where the keys consist of all source tables that have
     * at least one column which is a data source for the given list of
     * target tables, and the value set is all target tables.  If a particular
     * target table has no columns with source columns defined, it will be
     * in the mapping <tt>{ null -&gt; [tables, ...] }</tt>.
     * @throws ArchitectException 
     */
    public static Map<SQLTable, Collection<SQLTable>> 
                findTableLevelMappings(Collection<SQLTable> targetTables)
                throws ArchitectException {
        Map<SQLTable, Collection<SQLTable>> mappings = new HashMap<SQLTable, Collection<SQLTable>>();
        
        for (SQLTable t : targetTables) {
            Set<SQLTable> sources = new HashSet<SQLTable>();
            for (SQLColumn c : t.getColumns()) {
                SQLColumn sc = c.getSourceColumn();
                if (sc != null) {
                    sources.add(sc.getParentTable());
                } else {
                    sources.add(null);
                }
            }
            
            for (SQLTable st : sources) {
                Collection<SQLTable> targets = mappings.get(st);
                if (targets == null) {
                    targets = new ArrayList<SQLTable>();
                    mappings.put(st, targets);
                }
                targets.add(t);
            }
        }
        return mappings;
    }
}
