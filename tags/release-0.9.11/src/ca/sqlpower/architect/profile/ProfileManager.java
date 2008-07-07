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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;

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
     * Schedules a profile to be populated on a separate worker thread. You will
     * not normally need to call this method, since it is done for you by
     * {@link #asynchCreateProfiles(Collection)}. The use case is for re-trying
     * profile results that have been canceled by the user before they managed
     * to fully populate.
     * 
     * @param result
     *            The result object to reschedule for profiling. It must already
     *            be owned by this profile manager, and must not be in the
     *            process of profiling, and also not already finished profiling.
     */
    public Future<TableProfileResult> scheduleProfile(TableProfileResult result);
    
    /**
     * Removes a single TableProfileResult from the List of TableProfileResults
     * that this ProfileManager keeps track of. If the remove was successful then
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
    
    /**
     * Closes the Executor service. This stops it from running any further jobs.
     */
    public void close();

    /**
     * Returns a list of the different types of profile creators.
     * @return a List of the possible profile creators
     */
    public List<TableProfileCreator> getProfileCreators();
    
    /**
     * Returns the current profile creator in use.
     * @return the currently used profile creator object
     */
    public TableProfileCreator getCreator();
    
    /**
     * Sets the profile manager to use the given profile creator.
     * @param tpc the profile creator to use, must not be null.
     */
    public void setCreator(TableProfileCreator tpc);
}
