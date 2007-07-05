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
package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

public class TableProfileManagerTest extends TestCase {
    TableProfileManager m;
    
    TableProfileResult r1;
    SQLTable t1;
    TableProfileResult r2;
    SQLTable t2;
    TableProfileResult r3;
    SQLTable t3;
    TableProfileResult r4;
    SQLTable t4;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m = new TableProfileManager();
        t1 = new SQLTable();
        t1.setName("t1");
        r1 = new TableProfileResult(t1,m);
        t2 = new SQLTable();
        t2.setName("t2");
        r2 = new TableProfileResult(t2,m);
        t3 = new SQLTable();
        t3.setName("t3");
        r3 = new TableProfileResult(t3,m);
        t4 = new SQLTable();
        t4.setName("t4");
        r4 = new TableProfileResult(t4,m);
    }
    
    public void testLoadCountInSync(){
        m.loadResult(r1);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        m.removeProfile(r1);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
    }
    
    public void testCreateCountInSync() throws SQLException, ArchitectException{
        TableProfileResult tpr = m.createProfile(t1);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        m.removeProfile(tpr);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
    }
       
    public void testLoadManyCountInSync(){
        List list = new ArrayList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
        m.loadManyResults(list);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        assertTrue("Table t2 not in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r1);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertTrue("Table t2 not in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r2);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r3);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertFalse("Table t3 in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r4);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertFalse("Table t3 in profile",m.isTableProfiled(t3));
        assertFalse("Table t4 in profile",m.isTableProfiled(t4));
    }
    
    public void testClearCountInSync(){
        List list = new ArrayList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
        m.loadManyResults(list);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        assertTrue("Table t2 not in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.clear();
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertFalse("Table t3 in profile",m.isTableProfiled(t3));
        assertFalse("Table t4 in profile",m.isTableProfiled(t4));
    }
    
    
}
