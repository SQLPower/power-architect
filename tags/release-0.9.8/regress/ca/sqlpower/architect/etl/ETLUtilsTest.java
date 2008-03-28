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
package ca.sqlpower.architect.etl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;

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
