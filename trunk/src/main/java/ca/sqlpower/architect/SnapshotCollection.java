/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * This object contains all of the {@link SPObjectSnapshot}s and the copies of
 * the objects they represent. This is mainly for convenience to keep them
 * grouped together.
 */
public class SnapshotCollection extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections
            .unmodifiableList(new ArrayList<Class<? extends SPObject>>(Arrays.asList(UserDefinedSQLType.class, 
                    DomainCategory.class, SPObjectSnapshot.class)));

    /**
     * The list of all snapshots in our current system. This includes types,
     * domains, and domain category snapshots.
     */
    private final List<SPObjectSnapshot<?>> spobjectSnapshots = new ArrayList<SPObjectSnapshot<?>>();

    /**
     * List of all copies of {@link UserDefinedSQLType}s that existed at some
     * point in the system workspace and are in use in the project.
     */
    private final List<UserDefinedSQLType> udtSnapshots = new ArrayList<UserDefinedSQLType>();

    /**
     * List of all copies of {@link DomainCategory} that existed at some point
     * in the system workspace and are in use in the project.
     */
    private final List<DomainCategory> categorySnapshots = new ArrayList<DomainCategory>();
    
    public SnapshotCollection() {
        setName("Default snapshot collection");
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof SPObjectSnapshot<?>) {
            addSPObjectSnapshot((SPObjectSnapshot<?>) child, index);
        } else if (child instanceof UserDefinedSQLType) {
            addUDTSnapshot((UserDefinedSQLType) child, index);
        } else if (child instanceof DomainCategory) {
            addCategorySnapshot((DomainCategory) child, index);
        }
    }
    
    public void addCategorySnapshot(DomainCategory domainCategory, int index) {
        categorySnapshots.add(index, domainCategory);
        domainCategory.setParent(this);
        fireChildAdded(DomainCategory.class, domainCategory, index);
    }

    public void addUDTSnapshot(UserDefinedSQLType sqlType, int index) {
        udtSnapshots.add(index, sqlType);
        sqlType.setParent(this);
        fireChildAdded(UserDefinedSQLType.class, sqlType, index);
    }
    
    public boolean removeUDTSnapshot(UserDefinedSQLType child) {
        int index = udtSnapshots.indexOf(child);
        boolean removed = udtSnapshots.remove(child);
        if (removed) {
            fireChildRemoved(UserDefinedSQLType.class, child, index);
            child.setParent(null);
        }
        return removed;
    }
    
    public boolean removeCategorySnapshot(DomainCategory child) {
        int index = categorySnapshots.indexOf(child);
        boolean removed = categorySnapshots.remove(child);
        if (removed) {
            fireChildRemoved(DomainCategory.class, child, index);
            child.setParent(null);
        }
        return removed;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child instanceof SPObjectSnapshot<?>) {
            return removeSPObjectSnapshot((SPObjectSnapshot<?>) child);
        } else if (child instanceof UserDefinedSQLType) {
            return removeUDTSnapshot((UserDefinedSQLType) child);
        } else if (child instanceof DomainCategory) {
            return removeCategorySnapshot((DomainCategory) child);
        }
        return false;
    }

    @Transient @Accessor
    @Override
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    @Override
    public List<? extends SPObject> getChildren() {
        List<SPObject> children = new ArrayList<SPObject>();
        children.addAll(udtSnapshots);
        children.addAll(categorySnapshots);
        children.addAll(spobjectSnapshots);
        return Collections.unmodifiableList(children);
    }

    @NonBound
    @Override
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void removeDependency(SPObject dependency) {
        for (UserDefinedSQLType snapshot : udtSnapshots) {
            snapshot.removeDependency(dependency);
        }
        for (DomainCategory category : categorySnapshots) {
            category.removeDependency(dependency);
        }
        for (SPObjectSnapshot<?> snapshot : spobjectSnapshots) {
            snapshot.removeDependency(dependency);
        }        
    }

    public boolean removeSPObjectSnapshot(SPObjectSnapshot<?> child) {
        int index = spobjectSnapshots.indexOf(child);
        boolean removed = spobjectSnapshots.remove(child);
        if (removed) {
            fireChildRemoved(SPObjectSnapshot.class, child, index);
            child.setParent(null);
        }
        return removed;
    }
    
    public void addSPObjectSnapshot(SPObjectSnapshot<?> child, int index) {
        spobjectSnapshots.add(index, child);
        child.setParent(this);
        fireChildAdded(SPObjectSnapshot.class, child, index);        
    }
    
    @NonProperty
    public List<SPObjectSnapshot<?>> getSPObjectSnapshots() {
        return Collections.unmodifiableList(spobjectSnapshots); 
    }
}
