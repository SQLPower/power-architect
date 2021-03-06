/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.etl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;

public class ETLUtilsTest extends TestCase {
    
    // source tables
    private SQLDatabase sdb1;
    private SQLTable st1;
    private SQLTable st2;
    private SQLTable st3;
    
    // target tables
    private SQLDatabase tdb;
    private SQLTable tt1;
    private SQLTable tt2;
    private SQLTable tt3;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        sdb1 = new SQLDatabase();
        st1 = new SQLTable(sdb1, "st1", null, "TABLE", true);
        st1.addColumn(new SQLColumn(st1, "st1_col1", Types.VARCHAR, 3, 4));
        st1.addColumn(new SQLColumn(st1, "st1_col2", Types.VARCHAR, 3, 4));
        st1.addColumn(new SQLColumn(st1, "st1_col3", Types.VARCHAR, 3, 4));
        sdb1.addChild(st1);
        st2 = new SQLTable(sdb1, "st2", null, "TABLE", true);
        st2.addColumn(new SQLColumn(st2, "st2_col1", Types.VARCHAR, 3, 4));
        st2.addColumn(new SQLColumn(st2, "st2_col2", Types.VARCHAR, 3, 4));
        st2.addColumn(new SQLColumn(st2, "st2_col3", Types.VARCHAR, 3, 4));
        sdb1.addChild(st2);
        st3 = new SQLTable(sdb1, "st3", null, "TABLE", true);
        st3.addColumn(new SQLColumn(st3, "st3_col1", Types.VARCHAR, 3, 4));
        st3.addColumn(new SQLColumn(st3, "st3_col2", Types.VARCHAR, 3, 4));
        st3.addColumn(new SQLColumn(st3, "st3_col3", Types.VARCHAR, 3, 4));
        sdb1.addChild(st3);
        
        
        tdb = new SQLDatabase();
        
        // a table with the "normal case" mappings
        tt1 = new SQLTable(sdb1, "tt1", null, "TABLE", true);
        tt1.addColumn(new SQLColumn(tt1, "tt1_col1", Types.VARCHAR, 3, 4));
        tt1.getColumn(0).setSourceColumn(st1.getColumn(0));
        tt1.addColumn(new SQLColumn(tt1, "tt1_col2", Types.VARCHAR, 3, 4));
        tt1.getColumn(1).setSourceColumn(st1.getColumn(1));
        tt1.addColumn(new SQLColumn(tt1, "tt1_col3", Types.VARCHAR, 3, 4));
        tt1.getColumn(2).setSourceColumn(st1.getColumn(2));
        tdb.addChild(tt1);
        
        // a table with no mappings
        tt2 = new SQLTable(sdb1, "tt2", null, "TABLE", true);
        tt2.addColumn(new SQLColumn(tt2, "tt2_col1", Types.VARCHAR, 3, 4));
        tt2.addColumn(new SQLColumn(tt2, "tt2_col2", Types.VARCHAR, 3, 4));
        tt2.addColumn(new SQLColumn(tt2, "tt2_col3", Types.VARCHAR, 3, 4));
        tdb.addChild(tt2);
        
        // a table with a mixed bag of mappings
        tt3 = new SQLTable(sdb1, "tt3", null, "TABLE", true);
        tt3.addColumn(new SQLColumn(tt3, "tt3_col1", Types.VARCHAR, 3, 4));
        tt3.addColumn(new SQLColumn(tt3, "tt3_col2", Types.VARCHAR, 3, 4));
        tt3.addColumn(new SQLColumn(tt3, "tt3_col3", Types.VARCHAR, 3, 4));
        tt3.getColumn(0).setSourceColumn(st1.getColumn(0));
        tt3.getColumn(1).setSourceColumn(st2.getColumn(1));
        tdb.addChild(tt3);
    }
    
    public void testFindMappingsNormal() throws Exception {
        List<SQLTable> target = new ArrayList();
        target.add(tt1);
        
        Map<SQLTable, Collection<SQLTable>> mappings =
            ETLUtils.findTableLevelMappings(target);
        
        assertEquals(1, mappings.size());
        assertNotNull(mappings.containsKey(st1));
        assertEquals(1, mappings.get(st1).size());
        assertTrue(mappings.get(st1).contains(tt1));
    }
    
    public void testFindMappingsNoSource() throws Exception {
        List<SQLTable> target = new ArrayList();
        target.add(tt2);
        
        Map<SQLTable, Collection<SQLTable>> mappings =
            ETLUtils.findTableLevelMappings(target);
        
        assertEquals(1, mappings.size());
        assertNotNull(mappings.containsKey(null));
        assertTrue(mappings.get(null).contains(tt2));
    }
    
    public void testFindMappingsMultiSource() throws Exception {
        List<SQLTable> target = new ArrayList();
        target.add(tt3);
        
        Map<SQLTable, Collection<SQLTable>> mappings =
            ETLUtils.findTableLevelMappings(target);
        
        assertEquals(3, mappings.size());
        assertNotNull(mappings.containsKey(st1));
        assertEquals(1, mappings.get(st1).size());
        assertTrue(mappings.get(st1).contains(tt3));
        
        assertNotNull(mappings.containsKey(st2));
        assertEquals(1, mappings.get(st2).size());
        assertTrue(mappings.get(st2).contains(tt3));
   
        assertNotNull(mappings.containsKey(null));
        assertEquals(1, mappings.get(null).size());
        assertTrue(mappings.get(null).contains(tt3));
    }
}
