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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.util.MonitorableImpl;

public abstract class AbstractProfileResult<T extends SQLObject>
    implements Comparable<AbstractProfileResult>, ProfileResult<T> {

    private static final Logger logger = Logger.getLogger(AbstractProfileResult.class);
    
    private T profiledObject;
    private long createEndTime = -1L;
    private long createStartTime = -1L;
    private Exception ex;
    // Monitorables
    private ProfileSettings settings;
    private int progress = 0;
    private Integer jobSize = null;
    private String message = null;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean finished = false;

    /**
     * Creates a new ProfileResult which will hold profile data about the given SQL Object.
     *
     * @param profiledObject The object that this profile data refers to.  Must not be null.
     */
    public AbstractProfileResult(T profiledObject) {
        if (profiledObject == null) throw new NullPointerException("The profiled object has to be non-null");
        this.profiledObject = profiledObject;
    }

    /**
     * A generic template for populating a profile result.  Calls {@link #doProfile()}
     * to perform the actual work of populating this profile result.
     */
    public void populate() {
        try {
            message = getProfiledObject().getName();
            if (!cancelled) {
                initialize();
                doProfile();
            }
        } catch (Exception ex) {
            setException(ex);
            logger.error("Profile failed. Saving exception:", ex);
        } finally {
            finish();
            progress++;
        }
    }

    /**
     * This method is the hook for subclasses to perform their specific
     * profiling activity.  The template method {@link #populate(MonitorableImpl)}
     * calls this method at the appropriate time.
     */
    protected abstract void doProfile() throws SQLException, ArchitectException;
    
    void initialize() {
        started = true;
        finished = false;
        setCreateStartTime(System.currentTimeMillis());
    }

    /**
     *  This method sets the end time of the profile result and sets finished to true
     */
    public void finish(long endTime) {
        setCreateEndTime(endTime);
        finished = true;
    }
    void finish() {
        finish(System.currentTimeMillis());
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#getProfiledObject()
     */
    public T getProfiledObject() {
        return profiledObject;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#getCreateStartTime()
     */
    public long getCreateStartTime() {
        return createStartTime;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#setCreateStartTime(long)
     */
    public void setCreateStartTime(long createStartTime) {
        this.createStartTime = createStartTime;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#getTimeToCreate()
     */
    public long getTimeToCreate() {
        return createEndTime-createStartTime;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#setCreateEndTime(long)
     */
    public void setCreateEndTime(long createEndTime) {
        this.createEndTime = createEndTime;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#getCreateEndTime()
     */
    public long getCreateEndTime() {
        return createEndTime;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.profile.ProfileResultInterface#getException()
     */
    public Exception getException() {
        return ex;
    }

    /**
     * If a subclass runs into problems populating itself, it
     * can call this method to store the exception for later inspection
     * by client code.
     */
    protected void setException(Exception ex) {
        this.ex = ex;
    }

    public synchronized boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * If you want to abort this profile operation, call this method
     * with an argument of <tt>true</tt>.  If this profile has not yet
     * started populating before it is cancelled, populate will not attempt
     * any work when it is called.  If this profile is already
     * populated, cancelling it will have no effect.
     */
    public synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    /**
     * Specifies the amount of work that needs to be done in order to populate
     * this profile (From the Monitorable interface). A null value indicates the
     * amount of work is not yet known.
     */
    public synchronized Integer getJobSize() {
        return jobSize;
    }
 
    /**
     * The current message for this profile result's populate progress.  From
     * the Monitorable interface.
     */
    public synchronized String getMessage() {
        return message;
    }

    /**
     * Returns the current amount of progress that has been made toward the
     * goal of populating this profile result.  The number is on a scale of
     * 0..jobSize.
     */
    public synchronized int getProgress() {
        return progress;
    }
 
    /**
     * Returns true if populate() has been called; false otherwise.
     */
    public synchronized boolean hasStarted() {
        return started;
    }

    /**
     * Returns true if populate() has completed without being
     * cancelled, either successfully or with error.
     */
    public synchronized boolean isFinished() {
        return finished;
    }
    
    /**
     * Compares this Profile Result based on the following attributes, in the following
     * priority:
     *
     * <ol>
     *  <li>The profiled object's database name
     *  <li>The profiled object's catalog name
     *  <li>The profiled object's schema name
     *  <li>The profiled object's table name
     *  <li>The profiled object's column name
     *  <li>The profile's createEndTime
     *  <li>The profile's createStartTime
     * </ol>
     *
     * If any of those attributes are null or not applicable, they will count as
     * coming before any non-null value.
     *
     * @param o Another ProfileResult to compare with.
     * @return -1 if this comes before o; 0 if this and o are the same; 1 if this comes after o.
     */
    public final int compareTo(AbstractProfileResult o) {

        SQLObject po = this.getProfiledObject();
        SQLObject opo = o.getProfiledObject();

        int diff;
        SQLObject so1, so2;

        // database name
        so1 = ArchitectUtils.getAncestor(po, SQLDatabase.class);
        so2 = ArchitectUtils.getAncestor(opo, SQLDatabase.class);
        if (so1 == null && so2 != null) diff = -1;
        else if (so1 != null && so2 == null) diff = 1;
        else if (so1 != null && so2 != null) diff = so1.getName().compareTo(so2.getName());
        else diff = 0;
        if (diff != 0) return diff;

        // catalog name
        so1 = ArchitectUtils.getAncestor(po, SQLCatalog.class);
        so2 = ArchitectUtils.getAncestor(opo, SQLCatalog.class);
        if (so1 == null && so2 != null) diff = -1;
        else if (so1 != null && so2 == null) diff = 1;
        else if (so1 != null && so2 != null) diff = so1.getName().compareTo(so2.getName());
        else diff = 0;
        if (diff != 0) return diff;

        // schema name
        so1 = ArchitectUtils.getAncestor(po, SQLSchema.class);
        so2 = ArchitectUtils.getAncestor(opo, SQLSchema.class);
        if (so1 == null && so2 != null) diff = -1;
        else if (so1 != null && so2 == null) diff = 1;
        else if (so1 != null && so2 != null) diff = so1.getName().compareTo(so2.getName());
        else diff = 0;
        if (diff != 0) return diff;

        // table name
        so1 = ArchitectUtils.getAncestor(po, SQLTable.class);
        so2 = ArchitectUtils.getAncestor(opo, SQLTable.class);
        if (so1 == null && so2 != null) diff = -1;
        else if (so1 != null && so2 == null) diff = 1;
        else if (so1 != null && so2 != null) diff = so1.getName().compareTo(so2.getName());
        else diff = 0;
        if (diff != 0) return diff;

        // column name
        so1 = ArchitectUtils.getAncestor(po, SQLColumn.class);
        so2 = ArchitectUtils.getAncestor(opo, SQLColumn.class);
        if (so1 == null && so2 != null) diff = -1;
        else if (so1 != null && so2 == null) diff = 1;
        else if (so1 != null && so2 != null) diff = so1.getName().compareTo(so2.getName());
        else diff = 0;
        if (diff != 0) return diff;

        // profile create date
        if (this.createEndTime > o.createEndTime) return 1;
        else if (this.createEndTime < o.createEndTime) return -1;

        if (this.createStartTime > o.createStartTime) return 1;
        else if (this.createStartTime < o.createStartTime) return -1;
        else return 0;
    }

    /**
     * Tests for equality with obj.  To be considered equal, obj must be a subtype
     * of ProfileResult and compareTo(obj) must return 0.
     */
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof AbstractProfileResult)) {
            return false;
        }
        AbstractProfileResult o = (AbstractProfileResult) obj;
        return (compareTo(o) == 0);
    }

    /**
     * Generates a hash code consistent with the equals() method.
     */
    @Override
    public final int hashCode() {
        int hash = 17;
        SQLObject po = getProfiledObject();
        SQLObject so;

        so = ArchitectUtils.getAncestor(po, SQLDatabase.class);
        if (so != null) hash *= so.getName().hashCode();

        so = ArchitectUtils.getAncestor(po, SQLCatalog.class);
        if (so != null) hash *= so.getName().hashCode();

        so = ArchitectUtils.getAncestor(po, SQLSchema.class);
        if (so != null) hash *= so.getName().hashCode();

        so = ArchitectUtils.getAncestor(po, SQLTable.class);
        if (so != null) hash *= so.getName().hashCode();

        so = ArchitectUtils.getAncestor(po, SQLColumn.class);
        if (so != null) hash *= so.getName().hashCode();

        hash *= createEndTime;

        hash *= createStartTime;

        return hash;
    }

    public ProfileSettings getSettings() {
        return settings;
    }

    public void setSettings(ProfileSettings settings) {
        this.settings = settings;
    }

}
