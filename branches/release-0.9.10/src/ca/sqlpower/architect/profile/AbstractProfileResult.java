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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.event.ProfileResultEvent;
import ca.sqlpower.architect.profile.event.ProfileResultListener;

/**
 * Base class for storing profile results that relate to a database object.
 * Provides mostly bookkeeping information and the infrastructure for event
 * support. Subclasses extend this class by providing additional attributes that
 * make sense for the type of object they profile.
 * 
 * @param <T>
 *            The type of DatabaseObject this profile pertains to. For example,
 *            SQLTable or SQLColumn.
 */
public abstract class AbstractProfileResult<T extends SQLObject>
    implements Comparable<AbstractProfileResult>, ProfileResult<T> {

    private static final Logger logger = Logger.getLogger(AbstractProfileResult.class);
    
    private T profiledObject;
    private long createEndTime = -1L;
    private long createStartTime = -1L;
    private Exception ex;
    private ProfileSettings settings;

    /**
     * A list of ProfileResultListeners that should be notified of the
     * status of the progress of the profiling operation on this profile result
     */
    private final List<ProfileResultListener> profileResultListeners
    = new ArrayList<ProfileResultListener>();
    
    /**
     * Creates a new ProfileResult which will hold profile data about the given SQL Object.
     *
     * @param profiledObject The object that this profile data refers to.  Must not be null.
     */
    public AbstractProfileResult(T profiledObject) {
        if (profiledObject == null) throw new NullPointerException("The profiled object has to be non-null");
        this.profiledObject = profiledObject;
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
     * If an exception is encountered while populating this profile result,
     * it should be stored here for later inspection by client code.
     */
    public void setException(Exception ex) {
        this.ex = ex;
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

    public void addProfileResultListener(ProfileResultListener listener) {
        profileResultListeners.add(listener);
    }
    
    public void removeProfileResultListener(ProfileResultListener listener) {
        profileResultListeners.remove(listener);
    }
    
    protected final void fireProfileStarted() {
        logger.debug("Firing profile started event for " + profiledObject);
        ProfileResultEvent event = new ProfileResultEvent(this);
        for (int i = profileResultListeners.size() - 1; i >= 0; i--) {
            profileResultListeners.get(i).profileStarted(event);
        }
    }
    
    protected final void fireProfileFinished() {
        logger.debug("Firing profile finished event for " + profiledObject);
        ProfileResultEvent event = new ProfileResultEvent(this);
        for (int i = profileResultListeners.size() - 1; i >= 0; i--) {
            profileResultListeners.get(i).profileFinished(event);
        }
    }
    
    protected final void fireProfileCancelled() {
        logger.debug("Firing profile cancelled event for " + profiledObject);
        ProfileResultEvent event = new ProfileResultEvent(this);
        for (int i = profileResultListeners.size() - 1; i >= 0; i--) {
            profileResultListeners.get(i).profileCancelled(event);
        }
    }
}
