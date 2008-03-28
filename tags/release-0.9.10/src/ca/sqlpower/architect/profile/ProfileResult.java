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

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.profile.event.ProfileResultListener;

/**
 * A ProfileResult is an interface for populating profile data about
 * database objects (for example, tables and columns), and for monitoring
 * the progress of the profiling operation, which can often take considerable
 * time to calculate.
 * 
 * @param T The type of SQLObject that this profile result calculates
 * and holds results for.
 */
public interface ProfileResult<T extends SQLObject> {
    
    /**
     * Returns the SQLObject that is profiled by this ProfileResult.
     */
    public abstract T getProfiledObject();

    /**
     * Returns the date and time that this ProfileResult started
     * profiling the profiled object.
     */
    public abstract long getCreateStartTime();

    /**
     * Returns the time it took to create this ProfileResult in milliseconds.
     */
    public abstract long getTimeToCreate();

    /**
     * Returns the date and time that this ProfileResult finished profiling
     * the profiled object.
     */
    public abstract long getCreateEndTime();

    /**
     * Returns the Exception that occured during the profiling of the
     * profiled object. If this method returns null then the profiled
     * object is not done populating yet or it has sucessfully populated 
     * without throwing an Exception.
     */
    public abstract Exception getException();
    
    /**
     * Add a ProfileResultListener that should be notified of changes in the
     * status of this ProfileResult's progress during a profile operation 
     */
    public void addProfileResultListener(ProfileResultListener listener);

    /**
     * Remove a ProfileResultListener from this ProfileResult's 
     * collection of ProfileResultListeners
     */
    public void removeProfileResultListener(ProfileResultListener listener);
}