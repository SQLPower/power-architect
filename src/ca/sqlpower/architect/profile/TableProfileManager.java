package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

public class TableProfileManager implements ProfileManager {

    private static final Logger logger = Logger.getLogger(TableProfileManager.class);
    private TableProfileResult lastTableProfileResult= null;
    
    private final List<TableProfileResult> tableResults =
        new ArrayList<TableProfileResult>();

    private ProfileSettings settings = new ProfileSettings();

    private void putResult(TableProfileResult profileResult) {
        tableResults.add(profileResult);
        fireProfileAdded(profileResult);
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

    public List<TableProfileResult> getTableResults() {
        return Collections.unmodifiableList(tableResults);
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
        final List<TableProfileResult> results = new ArrayList<TableProfileResult>();
        for (SQLTable t : tables) {
            TableProfileResult tpr = new TableProfileResult(t, this);
            results.add(tpr);
            putResult(tpr);
        }
        
        // Now, populate them one after the other on a separate worker thread
        // (Please don't change this to do them all in parallel.. it will reduce
        // performance, plus it will make a tonne of connections to the database)
        new Thread() {
            public void run() {
                try {
                    for (TableProfileResult tpr : results) {
                        System.out.println("TableProfileManager.asynchCreateProfiles(): populate started");
                        tpr.populate();
                        System.out.println("populated: " + tpr);
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

     
     public void clear() {
         tableResults.clear();
         fireProfileChanged();
     }

    public boolean removeProfile(TableProfileResult result) {
        System.out.println("ProfileManager: removing object: " + result);
        if (tableResults.remove(result)) {
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
            listener.profileAdded(event);
        }
    }

    private void fireProfileRemoved(ProfileResult removed) {
        ProfileChangeEvent event = new ProfileChangeEvent(this, removed);
        for (ProfileChangeListener listener: listeners) {
            listener.profileRemoved(event);
        }
    }

    private void fireProfileChanged(){
        ProfileChangeEvent event = new ProfileChangeEvent(this, null);
        for (ProfileChangeListener listener: listeners){
            listener.profileListChanged(event);
        }
    }


}
