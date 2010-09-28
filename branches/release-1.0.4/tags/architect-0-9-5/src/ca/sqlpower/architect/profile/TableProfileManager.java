package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

public class TableProfileManager implements ProfileManager {

    private static final Logger logger = Logger.getLogger(TableProfileManager.class);
    private TableProfileResult lastTableProfileResult= null;
    
    private final List<TableProfileResult> tableResults =
        new ArrayList<TableProfileResult>();
    private final Map<SQLTable,Integer> profileCounts = 
        new HashMap<SQLTable, Integer>();

    private ProfileSettings settings = new ProfileSettings();

    private void putResult(TableProfileResult profileResult) {
        tableResults.add(profileResult);
        fireProfileAdded(profileResult);
        incrementTableProfileCountCache(profileResult);
    }
    private void incrementTableProfileCountCache(TableProfileResult profileResult) {
        Integer profileCount = profileCounts.get(profileResult.getProfiledObject());
        if (profileCount == null) {
            profileCount = Integer.valueOf(1);
        } else {
            profileCount = Integer.valueOf(profileCount.intValue() +1);
        }
        profileCounts.put(profileResult.getProfiledObject(),profileCount);
    }
    
    private void decrementTableProfileCountCache(TableProfileResult profileResult) {
        Integer profileCount = profileCounts.get(profileResult.getProfiledObject());
        logger.debug("Decrementing from "+profileCount);
        if (profileCount == null) {
            throw new IllegalStateException("Cannot remove a table from our cache, it's not there!");
        } else {
            profileCount = Integer.valueOf(profileCount.intValue() -1);
        }
        if (profileCount > 0 ) {
            profileCounts.put(profileResult.getProfiledObject(),profileCount);
        } else {
            profileCounts.remove(profileResult.getProfiledObject());
        }
        logger.debug("Decrementing to "+profileCounts.get(profileResult.getProfiledObject()));
    }
    
    /**
     * Add profileResult to the most recent table profile result that was passed
     * to loadResult
     * 
     * NOTE: this is intended to be used only to load results this fires no events
     * @param profileResult
     */
    public void loadResult(ColumnProfileResult profileResult){
        lastTableProfileResult.addColumnProfileResult(profileResult);
    }
    
    /**
     * Adds profileResult to the listof table results.  It stores the profile result
     * so that column results can be attached later.
     * 
     * Note: this is intended for loading and is really not thread safe
     */
    public void loadResult(TableProfileResult profileResult){
        putResult(profileResult);
        lastTableProfileResult = profileResult;
    }
    
    public void loadManyResults(List results){
        tableResults.addAll(results);
        profileCounts.clear();
        for (TableProfileResult result:tableResults){
            incrementTableProfileCountCache(result);
        }
        fireProfilesAdded(results);
    }

    public List<TableProfileResult> getTableResults() {
        return Collections.unmodifiableList(tableResults);
    }
    /**
     * Checks if the table has at least 1 profile
     */
    public boolean isTableProfiled(SQLTable table) {
        return profileCounts.containsKey(table);
    }
    
    /**
     * Returns a collection of table profile results associated with the
     * given table. These profile results will probably differ by the
     * date that they were created. If there are no results found for the
     * given table, an empty collection will be returned.
     */
    public Collection<TableProfileResult> getTableResult(SQLTable t) {
        Collection<TableProfileResult> retCollection = new ArrayList<TableProfileResult>();
        for (TableProfileResult result : tableResults) {
            if (t == result.getProfiledObject()) {
                retCollection.add(result);
            }
        }
        
        return retCollection;
    }

    
    /**
     * Creates TableProfileResult objects for each of the tables in the
     * given list, then adds them to this ProfileManager in an unpopulated
     * state.  Then starts a new worker thread which will populate the results
     * one after the other.  It is likely that none of the profiles will be
     * populated yet by the time this method returns.
     *
     * @param obj The database object you want to profile.
     * @throws ArchitectException
     * @throws SQLException
     */
    public void asynchCreateProfiles(Collection<SQLTable> tables) throws SQLException, ArchitectException {

        // First, create all the result objects and add them to this manager
        // (so that they are visible and cancelable to the user)
        final List results = new ArrayList<TableProfileResult>();
        for (SQLTable t : tables) {
            TableProfileResult tpr = new TableProfileResult(t, this);
            results.add(tpr);
        }
        loadManyResults(results);
        
        // Now, populate them one after the other on a separate worker thread
        // (Please don't change this to do them all in parallel.. it will reduce
        // performance, plus it will make a tonne of connections to the database)
        new Thread() {
            public void run() {
                try {
                    for (TableProfileResult tpr : ((List<TableProfileResult>)results)) {
                        logger.debug("TableProfileManager.asynchCreateProfiles(): populate started");
                        tpr.populate();
                        logger.debug("populated: " + tpr);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // XXX save me
                }
            }
        }.start();
    }

    /**
     * Creates a new profile result for the given table, and adds it to
     * this ProfileManager.  The act of adding a profile causes this
     * ProfileManager to fire a profileAdded event.
     *
     * @param tables The database table(s) you want to profile.
     */
     public TableProfileResult createProfile(SQLTable table) throws SQLException, ArchitectException {
         TableProfileResult tableResult = new TableProfileResult(table, this);
         putResult(tableResult);
         tableResult.populate();
         return tableResult;
    }

     /**
      * Cancel any running profiles and remove all profiles from
      * the TableProfileManager
      */
     public void clear() {
         for (TableProfileResult tpr: tableResults){
             tpr.setCancelled(true);
         }
         tableResults.clear();
         profileCounts.clear();
         fireProfileChanged();
     }

    public boolean removeProfile(TableProfileResult result) {
        logger.debug("ProfileManager: removing profiling for object: " + result.getProfiledObject());
        if (tableResults.remove(result)) {
            result.setCancelled(true);
            decrementTableProfileCountCache(result);
            fireProfileRemoved(result);
            return true;
        }
        
        return false;
    }

    public ProfileSettings getProfileSettings() {
        return settings;
    }
    
    public void setProfileSettings(ProfileSettings settings) {
        this.settings = settings;
    }
    
    //==================================
    // ProfileManagerListeners
    //==================================
    List<ProfileChangeListener> listeners = new ArrayList<ProfileChangeListener>();

    public void addProfileChangeListener(ProfileChangeListener listener){
        listeners.add(listener);
    }

    public void removeProfileChangeListener(ProfileChangeListener listener){
        listeners.remove(listener);
    }

    private void fireProfileAdded(ProfileResult result){
        ProfileChangeEvent event = new ProfileChangeEvent(this, result);
        for (ProfileChangeListener listener: listeners){
            listener.profilesAdded(event);
        }
    }
    
    private void fireProfilesAdded(List<ProfileResult> addedResultsList) {
        ProfileChangeEvent event = new ProfileChangeEvent(this, addedResultsList);
        for (ProfileChangeListener listener: listeners){
            listener.profilesAdded(event);
        }
    }

    private void fireProfilesRemoved(List<ProfileResult> removedResultsList) {
        ProfileChangeEvent event = new ProfileChangeEvent(this, removedResultsList);
        for (ProfileChangeListener listener: listeners) {
            listener.profilesRemoved(event);
        }
    }    
    
    private void fireProfileRemoved(ProfileResult removed) {
        ProfileChangeEvent event = new ProfileChangeEvent(this, removed);
        for (ProfileChangeListener listener: listeners) {
            listener.profilesRemoved(event);
        }
    }

    private void fireProfileChanged(){
        List<ProfileResult> profileResultList = Collections.emptyList();
        ProfileChangeEvent event = new ProfileChangeEvent(this, profileResultList);
        for (ProfileChangeListener listener: listeners){
            listener.profileListChanged(event);
        }
    }


}
