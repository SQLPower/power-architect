/*
 * Created on Jul 30, 2007
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.profile;

import ca.sqlpower.architect.profile.event.ProfileChangeEvent;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;

/**
 * The CountingProfileChangeListener just counts how many times
 * each of the listener methods was called.
 */
public class CountingProfileChangeListener implements ProfileChangeListener {

    /**
     * The number of add events processed.
     */
    private int addedEventCount;
    
    /**
     * The number of remove events processed.
     */
    private int removedEventCount;
    
    /**
     * The number of change events processed.
     */
    private int changedEventCount;
    
    /**
     * The most recent add, remove, or change event processed.
     */
    private ProfileChangeEvent mostRecentEvent;

    public void profilesAdded(ProfileChangeEvent e) {
        addedEventCount++;
        mostRecentEvent = e;
    }

    public void profilesRemoved(ProfileChangeEvent e) {
        removedEventCount++;
        mostRecentEvent = e;
    }

    public void profileListChanged(ProfileChangeEvent e) {
        changedEventCount++;
        mostRecentEvent = e;
    }

    public int getAddedEventCount() {
        return addedEventCount;
    }
    
    public int getChangedEventCount() {
        return changedEventCount;
    }
    
    public int getRemovedEventCount() {
        return removedEventCount;
    }
    
    public ProfileChangeEvent getMostRecentEvent() {
        return mostRecentEvent;
    }
}
