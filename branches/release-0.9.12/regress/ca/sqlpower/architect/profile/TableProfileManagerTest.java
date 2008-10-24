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
