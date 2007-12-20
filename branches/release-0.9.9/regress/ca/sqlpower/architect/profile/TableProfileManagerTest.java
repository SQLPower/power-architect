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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

public class TableProfileManagerTest extends TestProfileBase {

    // see setUp in superclass. it's pretty fancy.
    
    public void testGetSpecificProfileResult() throws SQLException, ArchitectException{
        int oldResultCount = pm.getResults(t1).size();
        pm.createProfile(t1);
        assertEquals(oldResultCount + 1, pm.getResults(t1).size());
    }

    public void testResultListUnmodifiable() {
        List<TableProfileResult> results = pm.getResults();
        try {
            results.add(null);
            fail("List modification succeeded");
        } catch (UnsupportedOperationException ex) {
            // list is not modifiable
            return;
        }
    }
    
    public void testCreateProfile() throws Exception {
        int oldResultCount = pm.getResults().size();
        pm.createProfile(t1);
        List<TableProfileResult> results = pm.getResults();
        assertEquals(oldResultCount + 1, results.size());
        assertSame(t1, results.get(0).getProfiledObject());
    }

    public void testCreateProfileFiresEvent() throws Exception {
        CountingProfileChangeListener listener = new CountingProfileChangeListener();
        pm.addProfileChangeListener(listener );
        pm.createProfile(t1);
        assertEquals(1, listener.getAddedEventCount());
        assertEquals(0, listener.getRemovedEventCount());
        assertEquals(0, listener.getChangedEventCount());
    }

    public void testRemoveProfileFiresEvent() throws Exception {
        TableProfileResult tpr = pm.createProfile(t1);
        
        CountingProfileChangeListener listener = new CountingProfileChangeListener();
        pm.addProfileChangeListener(listener);
        
        pm.removeProfile(tpr);
        
        assertEquals(0, listener.getAddedEventCount());
        assertEquals(1, listener.getRemovedEventCount());
        assertEquals(0, listener.getChangedEventCount());
    }

    /**
     * Tests that clear empties out the results of the profile manager.
     */
    public void testClear()  throws Exception {
        
        pm.clear();
        
        assertEquals(0, pm.getResults().size());
        assertEquals(0, pm.getResults(t1).size());
        assertEquals(0, pm.getResults(t2).size());
    }

    /**
     * The profile result list returned by getResults() should not change
     * as the profile manager's contents change. This tests for that.
     */
    public void testGetResultsIsSnapshot() throws Exception {
        List<TableProfileResult> snapshot = pm.getResults();
        int oldSize = snapshot.size();
        
        pm.createProfile(t1);
        
        assertEquals(oldSize, snapshot.size());
    }
    
    /**
     * Tests that clear fires the proper events.
     */
    public void testClearFiresEvents() throws Exception {
        
        CountingProfileChangeListener l = new CountingProfileChangeListener();
        pm.addProfileChangeListener(l);

        List<TableProfileResult> oldResults = pm.getResults();
        pm.clear();
        
        assertEquals(0, l.getAddedEventCount());
        assertEquals(1, l.getRemovedEventCount());
        assertEquals(0, l.getChangedEventCount());

        final List<ProfileResult> eventResults = l.getMostRecentEvent().getProfileResults();
        assertEquals(oldResults.size(), eventResults.size());
        for (TableProfileResult tpr : oldResults) {
            assertTrue("Remove event was missing result object for " + tpr.getProfiledObject().getName(),
                    eventResults.contains(tpr));
        }
    }
    
    public void testAsynchCreateProfiles() throws Exception {
        SQLTable garbageTable = new SQLTable();
        garbageTable.setName("Not profilable");
        List<SQLTable> tables = Arrays.asList(t1, garbageTable, t2, t3);
        
        int oldCount = pm.getResults().size();
        
        Collection<Future<TableProfileResult>> futures = pm.asynchCreateProfiles(tables);
        
        assertEquals("Unpopulated results should be added right away",
                oldCount + tables.size(), pm.getResults().size());
        
        int failureCount = 0;
        for (Future<TableProfileResult> f : futures) {
            try {
                TableProfileResult tpr = f.get();
                assertTrue(tpr.getProgressMonitor().isFinished());
            } catch (ExecutionException ex) {
                // this is expected, but only for the garbage table
                failureCount++;
            }
        }
        assertEquals(1, failureCount);
        assertNotNull(pm.getResults(garbageTable).get(0).getException());
    }
    
    public void testAsynchCreateProfilesFiresEvent() throws Exception {
        SQLTable garbageTable = new SQLTable();
        garbageTable.setName("Not profilable");
        List<SQLTable> tables = Arrays.asList(t1, garbageTable, t2, t3);
        CountingProfileChangeListener l = new CountingProfileChangeListener();
        pm.addProfileChangeListener(l);
        
        pm.asynchCreateProfiles(tables);
        
        assertEquals(1, l.getAddedEventCount());
        assertEquals(0, l.getRemovedEventCount());
        assertEquals(0, l.getChangedEventCount());
        
        assertEquals(4, l.getMostRecentEvent().getProfileResults().size());
    }
}
