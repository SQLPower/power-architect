/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * This {@link TreeModel} defines the hierarchy of {@link UserDefinedSQLType}s
 * and {@link DomainCategory}s under an {@link ArchitectProject}.
 * 
 */
public class SQLTypeTreeModel implements TreeModel {
    
    private static final Logger logger = Logger.getLogger(SQLTypeTreeModel.class);
    
    private final ArchitectSession session;

    /**
     * This {@link Comparator} can compare {@link UserDefinedSQLType} and
     * {@link DomainCategory} objects. {@link UserDefinedSQLType}s always come
     * after {@link DomainCategory}s.
     */
    private final Comparator<SPObject> typeComparator = new Comparator<SPObject>() {
        
        public int compare(SPObject spo1, SPObject spo2) {
            
            if (spo1 instanceof UserDefinedSQLType && spo2 instanceof UserDefinedSQLType) {
                return Integer.signum(spo1.getName().compareToIgnoreCase(spo2.getName()));
            } else if (spo1 instanceof DomainCategory && spo2 instanceof DomainCategory) {
                return Integer.signum(spo1.getName().compareToIgnoreCase(spo2.getName()));
            } else if (spo1 instanceof DomainCategory) {
                return -1;
            } else {
                return 1;
            }
        }
    };
    
    public SQLTypeTreeModel(ArchitectSession session) {
        this.session = session;
    }
    
    public SPObject getChild(Object parent, int index) {
        // If the parent is the delegate ArchitectProject, get the child at 
        // the specified index which should be of type UserDefinedSQLType 
        // or DomainCategory.
        if (session.getWorkspace() == parent) {
            return getChildren().get(index);
        
        // If the parent is DomainCategory, get the child at the specified 
        // index which should be of type UserDefinedSQLType.
        } else if (parent instanceof DomainCategory &&
                session.getDomainCategories().contains(parent)) {
            return getDomainTypes((DomainCategory) parent).get(index);
        }
        
        return null;
    }

    public int getChildCount(Object parent) {
        if (session.getWorkspace() == parent) {
            return getChildren().size();
        } else if (parent instanceof DomainCategory && 
                session.getDomainCategories().contains(parent)) {
            return getDomainTypes((DomainCategory) parent).size();
        }
        return 0;
    }

    /**
     * Returns the sorted {@link List} of {@link UserDefinedSQLType}s and
     * {@link DomainCategory}s that are children of a given project.
     */
    private List<? extends SPObject> getChildren() {
        List<SPObject> children = new ArrayList<SPObject>();
        children.addAll(session.getDomainCategories());
        children.addAll(session.getSQLTypes());
        Collections.sort(children, typeComparator);
        return Collections.unmodifiableList(children);
    }

    /**
     * Creates the sorted {@link List} of {@link UserDefinedSQLType}s under a
     * given {@link DomainCategory}.
     * 
     * @param category
     *            The {@link DomainCategory} to get the children from.
     * @return The {@link List} of {@link UserDefinedSQLType}s that are
     *         contained under the given {@link DomainCategory}.
     */
    private List<UserDefinedSQLType> getDomainTypes(DomainCategory category) {
        List<UserDefinedSQLType> children = new ArrayList<UserDefinedSQLType>();
        children.addAll(category.getChildren(UserDefinedSQLType.class));
        Collections.sort(children, typeComparator);
        return children;
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return -1;
        } else if (session.getWorkspace() == parent) {
            return getChildren().indexOf(child);
        } else if (parent instanceof DomainCategory &&
                session.getDomainCategories().contains(parent)) {
            if (child instanceof UserDefinedSQLType) {
                return getDomainTypes((DomainCategory) parent).indexOf(child);
            }
        }
        return -1;
    }

    public ArchitectProject getRoot() {
        return session.getWorkspace();
    }
    
    public boolean isLeaf(Object node) {
        return (node instanceof UserDefinedSQLType);
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        // no-op
    }

    public void removeTreeModelListener(TreeModelListener l) {
        // no-op
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // no-op
    }

}

