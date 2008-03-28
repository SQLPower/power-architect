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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

/**
 * The profile manager implementation for tables. Provides a simple
 * implementation of the API for properly creating profiles using a separate
 * worker thread.
 * <p>
 * As of this writing, this is the only non-stub profile manager in the
 * Architect. When and if we make more kinds of profile managers, there will be
 * large swaths of code in this class that could be moved into an abstract base
 * profile manager.
 * 
 * @version $Id$
 */
public class TableProfileManager implements ProfileManager {

    private static final Logger logger = Logger.getLogger(TableProfileManager.class);
    
    /**
     * The current list of listeners who want to know when the contents
     * of this profile manager change.
     */
    private final List<ProfileChangeListener> profileChangeListeners = new ArrayList<ProfileChangeListener>();

    /**
     * All profile results in this profile manager.
     */
    private final List<TableProfileResult> results = new ArrayList<TableProfileResult>();
    
    /**
     * The defaults that new profile results will be created with.
     */
    private ProfileSettings defaultProfileSettings = new ProfileSettings();
    
    /**
     * The Profile Executor manages the thread that actually does the work
     * of creating the profiles.
     */
    private ExecutorService profileExecutor = Executors.newSingleThreadExecutor();

    /**
     * A Callable interface which populates a single profile result then returns it.
     * Profile results don't throw the exceptions their populate() methods encounter,
     * but this callable wrapper knows about that, and digs up and rethrows the
     * exceptions encountered by populate().  This is more normal, and is also
     * compatible with the ExecutorService implementation provided in the standard
     * Java library (it handles exceptions and restarts the work queue properly).
     */
    private class ProfileResultCallable implements Callable<TableProfileResult> {
        
        /**
         * The table profile result this Callable populates.
         */
        private TableProfileResult tpr;

        ProfileResultCallable(TableProfileResult tpr) {
            if (tpr == null) throw new NullPointerException("Can't populate a null profile result!");
            this.tpr = tpr;
        }
        
        /**
         * Populates the profile result, throwing any exceptions encountered.
         */
        public TableProfileResult call() throws Exception {
            tpr.populate();
            if (tpr.getException() != null) {
                throw tpr.getException();
            }
            return tpr;
        }
    }
    
    /* docs inherited from interface */
    public TableProfileResult createProfile(SQLTable table) throws ArchitectException {
        TableProfileResult tpr = new TableProfileResult(table, this, getDefaultProfileSettings());
        results.add(tpr);
        fireProfileAdded(tpr);
        
        try {
            profileExecutor.submit(new ProfileResultCallable(tpr)).get();
            assert (tpr.isFinished());
        } catch (InterruptedException ex) {
            logger.info("Profiling was interrupted (likely because this manager is being shut down)");
        } catch (ExecutionException ex) {
            throw new ArchitectException("Profile execution failed", ex);
        }
        
        return tpr;
    }

    /* docs inherited from interface */
    public Collection<Future<TableProfileResult>> asynchCreateProfiles(Collection<SQLTable> tables) {
        
        List<TableProfileResult> profiles = new ArrayList<TableProfileResult>();
        for (SQLTable t : tables) {
            profiles.add(new TableProfileResult(t, this, getDefaultProfileSettings()));
        }
        
        results.addAll(profiles);
        fireProfilesAdded(profiles);
        
        List<Future<TableProfileResult>> results = new ArrayList<Future<TableProfileResult>>();
        for (TableProfileResult tpr : profiles) {
            results.add(scheduleProfile(tpr));
        }
        return results;
    }

    /* docs inherited from interface */
    public Future<TableProfileResult> scheduleProfile(TableProfileResult result) {
        return profileExecutor.submit(new ProfileResultCallable(result));
    }
    
    /* docs inherited from interface */
    public void clear() {
        List<TableProfileResult> oldResults = new ArrayList<TableProfileResult>(results);
        results.clear();
        fireProfilesRemoved(oldResults);
    }

    /* docs inherited from interface */
    public List<TableProfileResult> getResults() {
        // this could be optimized by caching the current result list snapshot, but enh.
        return Collections.unmodifiableList(new ArrayList<TableProfileResult>(results));
    }

    /* docs inherited from interface */
    public List<TableProfileResult> getResults(SQLTable t) {
        List<TableProfileResult> someResults = new ArrayList<TableProfileResult>();
        for (TableProfileResult tpr : results) {
            if (tpr.getProfiledObject().equals(t)) {
                someResults.add(tpr);
            }
        }
        return Collections.unmodifiableList(someResults);
    }

    /* docs inherited from interface */
    public boolean removeProfile(TableProfileResult victim) {
        boolean removed = results.remove(victim);
        if (removed) {
            fireProfileRemoved(victim);
        }
        return removed;
    }

    /* docs inherited from interface */
    public ProfileSettings getDefaultProfileSettings() {
        return defaultProfileSettings;
    }

    /* docs inherited from interface */
    public void setDefaultProfileSettings(ProfileSettings settings) {
        defaultProfileSettings = settings;
    }

    /* docs inherited from interface */
    public void setProcessingOrder(List<TableProfileResult> tpr) {
        
    }

    /**
     * This is a hook designed so the SwingUIProject can insert profile results
     * into this profile manager as it is reading in a project file.  It is
     * not appropriate to use otherwise.
     * <p>
     * This method fires an event every time it adds a table profile result, because
     * not doing so makes it necessary to create the profile manager view after
     * loading in the profile results, and it is impossible to guarantee that policy
     * from here.
     * <p>
     * The idea is, the SwingUIProject stores all profile results in a flat space
     * (table and column results are sibling elements) so it needs our help to
     * put everything back together into the original hierarchy.  This method hangs
     * onto all TableProfileResult objects given to it, and ignores all other result
     * types, assuming the client code will do the appropriate hookups.
     * <p>
     * You might be asking yourself, "why not store the profile results in the
     * same hierarchy as they had when they were originally created, so we don't
     * need any more error-prone code to recreate what we already had and then
     * threw away?  Beside reducing bugs, it would eliminate the need for this public
     * method and accompanying docs that warn you against using it."  If so, please
     * apply to SQL Power at hr (@) sqlpower.ca. 
     */
    public void loadResult(ProfileResult pr) {
        if (pr instanceof TableProfileResult) {
            TableProfileResult tpr = (TableProfileResult) pr;
            results.add(tpr);
            fireProfileAdded(tpr);
        }
        // the column results will get added to the table result by
        // the project's profile result factory class
    }
    
    /**
     * Adds the given listener to this profile manager.  The listener will be notified
     * of additions and removals of results in this profile manager.
     */
    public void addProfileChangeListener(ProfileChangeListener listener) {
        profileChangeListeners.add(listener);
    }

    /**
     * Removes the given listener.  After removal, the listener will no longer be notified
     * of changes to this profile manager.
     */
    public void removeProfileChangeListener(ProfileChangeListener listener) {
        profileChangeListeners.remove(listener);
    }

    /**
     * Creates and fires a "profilesAdded" event for the given profile result.
     */
    private void fireProfileAdded(TableProfileResult tpr) {
        if (tpr == null) throw new NullPointerException("Can't fire event for adding null profile");
        ProfileChangeEvent e = new ProfileChangeEvent(this, tpr);
        for (int i = profileChangeListeners.size() - 1; i >= 0; i--) {
            profileChangeListeners.get(i).profilesAdded(e);
        }
    }

    /**
     * Creates and fires a "profilesAdded" event for the given profile results.
     */
    private void fireProfilesAdded(List<TableProfileResult> results) {
        if (results == null) throw new NullPointerException("Can't fire event for null profile list");
        ProfileChangeEvent e = new ProfileChangeEvent(this, results);
        for (int i = profileChangeListeners.size() - 1; i >= 0; i--) {
            profileChangeListeners.get(i).profilesAdded(e);
        }
    }

    /**
     * Creates and fires a "profilesRemoved" event for the given profile result.
     */
    private void fireProfileRemoved(TableProfileResult victim) {
        if (victim == null) throw new NullPointerException("Can't fire event for removing null profile");
        ProfileChangeEvent e = new ProfileChangeEvent(this, victim);
        for (int i = profileChangeListeners.size() - 1; i >= 0; i--) {
            profileChangeListeners.get(i).profilesRemoved(e);
        }
    }

    /**
     * Creates and fires a "profilesRemoved" event for the given list of profile results.
     */
    private void fireProfilesRemoved(List<TableProfileResult> removedList) {
        if (removedList == null) throw new NullPointerException("Null list not allowed");
        ProfileChangeEvent e = new ProfileChangeEvent(this, removedList);
        for (int i = profileChangeListeners.size() - 1; i >= 0; i--) {
            profileChangeListeners.get(i).profilesRemoved(e);
        }
    }

}
