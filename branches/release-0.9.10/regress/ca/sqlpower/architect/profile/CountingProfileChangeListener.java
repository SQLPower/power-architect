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
