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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

/**
 * An interface for classes that create and keep track of ProfileResults.
 * For now it can only be a list of TableProfileResults; some day we might
 * want to make it a generic type.
 */
public interface ProfileManager {

    /**
     * Returns an unmodifiable snapshot of the list of all TableProfileResults that this
     * ProfileManager keeps track of. You may get multiple ProfileResults
     * for one table since you are allowed to have multiple profiles of a
     * single table that come from different times the profiling was done.
     */
    List<TableProfileResult> getResults();

    /**
     * Returns an unmodifiable list of all TableProfileResults for the given
     * table. You may get multiple ProfileResults for one table since you are
     * allowed to have multiple profiles of a single table that come from
     * different times the profiling was done.  Returns an empty list if there
     * are no profiles for the given table.
     */
    List<TableProfileResult> getResults(SQLTable t);

    /**
     * Creates a new profile result for the given table, and adds it to
     * this ProfileManager.  The act of adding a profile causes this
     * ProfileManager to fire a profileAdded event.
     * <p>
     * This method operates synchronously, so it will not return until the
     * profile has been created (which could be several seconds to several
     * minutes).  For use from a Swing GUI, consider
     * {@link #asynchCreateProfiles(Collection)}.
     *
     * @param tables The database table(s) you want to profile.
     */
    public TableProfileResult createProfile(SQLTable table) throws SQLException, ArchitectException;

    /**
     * Creates TableProfileResult objects for each of the tables in the
     * given list, then adds them to this ProfileManager in an unpopulated
     * state.  Then starts a new worker thread which will populate the results
     * one after the other.  It is likely that none of the profiles will be
     * populated yet by the time this method returns.
     */
    public Collection<Future<TableProfileResult>> asynchCreateProfiles(Collection<SQLTable> tables);

    /**
     * Removes a single TableProfileResult from the List of TableProfileResults
     * that this ProfileManager keeps track of. If the remove was sucessful then
     * this method will fire a ProfileRemoved event and return true.
     */
    public boolean removeProfile(TableProfileResult victim);

    /**
     * Removes all contents of the list of TableProfileResults that this
     * ProfileManager keeps track of and fires a ProfileChanged event.
     */
    public void clear();
    
    /**
     * Returns the current default settings for new profile runs in this Profile Manager.
     */
    public ProfileSettings getDefaultProfileSettings();
    
    /**
     * Sets the defaults for new profiles created by this profile manager.
     */
    public void setDefaultProfileSettings(ProfileSettings settings);
    
    /**
     * Adds a ProfileChangeListener to this ProfileManager.
     */
    public void addProfileChangeListener(ProfileChangeListener listener);

    /**
     * Removes a ProfileChangeListener to this ProfileManager.
     */
    public void removeProfileChangeListener(ProfileChangeListener listener);

    /**
     * Modifies the order in which profile results will be calculated.
     * 
     * @param tpr A list of pending profile results that were already
     * part of this profile manager, but have not been calculated yet.
     */
    public void setProcessingOrder(List<TableProfileResult> tpr);
}
