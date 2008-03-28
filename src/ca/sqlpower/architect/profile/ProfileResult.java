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