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

import javax.annotation.Nonnull;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.util.SQLPowerUtils;

/**
 * {@link UserDefinedSQLType}s can be organized into categories. For example,
 * they can be based on which organizations use them (ex. A specific set of
 * domains specific to a company's data), or on which project they are for.
 */
public class DomainCategory extends AbstractSPObject {

    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
        Collections.<Class<? extends SPObject>>singletonList(UserDefinedSQLType.class);

    /**
     * Compares two domain categories on their properties except their UUID and
     * returns true if they all match. Does not look at children of the domain
     * category.
     */
    public static boolean areEqual(@Nonnull DomainCategory cat1, @Nonnull DomainCategory cat2) {
        return SQLPowerUtils.areEqual(cat1.getName(), cat2.getName());
    }

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
    
    /**
     * Calling this method will update all of the settable properties of
     * this object with the same properties as those in matchMe.
     */
    public void updateToMatch(DomainCategory matchMe) {
        setName(matchMe.getName());
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
