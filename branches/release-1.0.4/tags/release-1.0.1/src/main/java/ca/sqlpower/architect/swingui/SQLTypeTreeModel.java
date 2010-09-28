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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This {@link TreeModel} defines the hierarchy of {@link UserDefinedSQLType}s
 * and {@link DomainCategory}s under an {@link ArchitectProject}.
 * 
 */
public class SQLTypeTreeModel implements TreeModel {
    
    private static final Logger logger = Logger.getLogger(SQLTypeTreeModel.class);
    
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
    
    /**
     * The {@link ArchitectProject} that is the root of this tree.
     */
    private final ArchitectProject root;

    /**
     * The {@link List} of {@link UserDefinedSQLType}s reflective of the types
     * in the {@link ArchitectProject} when the constructor of this tree model
     * is called. Types are not directly retrieved from the
     * {@link ArchitectProject} as it may have changed after creation of this
     * model.
     */
    private final List<UserDefinedSQLType> sqlTypes;

    /**
     * The {@link List} of {@link DomainCategory} names reflective of the
     * categories and domains in the {@link ArchitectProject} when the
     * constructor of this tree model is called. The category names are mapped
     * to a collection of the domains that are under them as the domains
     * returned by the session could be a combination of actual system domains
     * and snapshot domains in the project. In this case the domains are under
     * different parents but should represent being under the same parent.
     * <p>
     * Domains are not directly retrieved from the {@link ArchitectProject} as
     * it may have changed after creation of this model.
     */
    private final Multimap<String, UserDefinedSQLType> domainCategoryMap = ArrayListMultimap.create();

    /**
     * The list of names of domain categories sorted to be in alphabetical order.
     * All domain categories known to this tree model will be in this list.
     */
    private final ArrayList<String> domainCategoryNameList; 

    /**
     * Creates a new SQLTypeTreeModel.
     * 
     * @param session
     *            The {@link ArchitectSession} to retrieve the
     *            {@link ArchitectProject}, and its child
     *            {@link UserDefinedSQLType}s and {@link DomainCategory}s from.
     */
    public SQLTypeTreeModel(ArchitectSession session) {
        root = session.getWorkspace();
        sqlTypes = new ArrayList<UserDefinedSQLType>(session.getSQLTypes());
        Collections.sort(sqlTypes, typeComparator);
        List<UserDefinedSQLType> domains = session.getDomains();
        for (UserDefinedSQLType domain : domains) {
            domainCategoryMap.put(domain.getParent().getName(), domain);
        }
        domainCategoryNameList = new ArrayList<String>(domainCategoryMap.keySet());
        Collections.sort(domainCategoryNameList);
    }
    
    public Object getChild(Object parent, int index) {
        // If the parent is the delegate ArchitectProject, get the child at 
        // the specified index which should be of type UserDefinedSQLType 
        // or DomainCategory.
        if (root == parent) {
            if (index < domainCategoryNameList.size()) {
                //Making a temporary domain category so the icon on the tree renderer is correct.
                return new DomainCategory(domainCategoryNameList.get(index));
            } else if (index < domainCategoryNameList.size() + sqlTypes.size()) {
                return sqlTypes.get(index - domainCategoryNameList.size());
            }
        
        // If the parent is DomainCategory, get the child at the specified 
        // index which should be of type UserDefinedSQLType.
        } else if (parent instanceof DomainCategory &&
                domainCategoryMap.keySet().contains(((DomainCategory) parent).getName())) {
            return getDomainTypes((DomainCategory) parent).get(index);
        }
        
        return null;
    }

    public int getChildCount(Object parent) {
        if (root == parent) {
            return domainCategoryNameList.size() + sqlTypes.size();
        } else if (parent instanceof DomainCategory && 
                domainCategoryMap.keySet().contains(((DomainCategory) parent).getName())) {
            return getDomainTypes((DomainCategory) parent).size();
        }
        return 0;
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
        List<UserDefinedSQLType> children = new ArrayList<UserDefinedSQLType>(
                domainCategoryMap.get(category.getName()));
        Collections.sort(children, typeComparator);
        return Collections.unmodifiableList(children);
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return -1;
        } else if (root == parent) {
            if (domainCategoryNameList.contains(child)) {
                return domainCategoryNameList.indexOf(child);
            } else if (sqlTypes.contains(child)) {
                return domainCategoryNameList.size() + sqlTypes.indexOf(child);
            }
        } else if (parent instanceof DomainCategory &&
                domainCategoryMap.keySet().contains(((DomainCategory) parent))) {
            if (child instanceof UserDefinedSQLType) {
                return getDomainTypes((DomainCategory) parent).indexOf(child);
            }
        }
        return -1;
    }

    public ArchitectProject getRoot() {
        return root;
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

