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

package ca.sqlpower.architect.enterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * {@link UserDefinedSQLType}s can be organized into categories. For example,
 * they can be based on which organizations use them (ex. A specific set of
 * domains specific to a company's data), or on which project they are for.
 */
public class DomainCategory extends AbstractSPObject {

    public static final List<Class<? extends SPObject>> allowedChildTypes =
        Collections.<Class<? extends SPObject>>singletonList(UserDefinedSQLType.class);

    /**
     * A {@link List} of {@link UserDefinedSQLType}s available under this category
     */
    private List<UserDefinedSQLType> children = new ArrayList<UserDefinedSQLType>();
    
    /**
     * Creates a DomainCategory with the given name.
     * 
     * @param name
     *            The name for this {@link DomainCategory}
     */
    @Constructor
    public DomainCategory(@ConstructorParameter(propertyName = "name") String name) {
        setName(name);
    }
    
    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType == UserDefinedSQLType.class) {
            return 0;
        } else {
            throw new IllegalArgumentException("DomainCategory cannot have children of type " + childType);
        }
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public List<? extends SPObject> getChildren() {
        return children;
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        // no-op
    }

    @Override
    protected void addChildImpl(SPObject child, int index) {
        children.add(index, (UserDefinedSQLType) child);
        child.setParent(this);
        fireChildAdded(UserDefinedSQLType.class, child, index);
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        int index = children.indexOf(child);
        boolean childRemoved = children.remove((UserDefinedSQLType) child);
        if (childRemoved) {
            fireChildRemoved(UserDefinedSQLType.class, child, index);
            child.setParent(null);
        }
        return childRemoved;
    }
}
