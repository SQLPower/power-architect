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

package ca.sqlpower.architect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.util.SPSession;

/**
 * 
 * This class is the root object of an ArchitectSession. There is an ArchitectProject
 * for every ArchitectSession. The ArchitectProject, and all its children, will be
 * listened to and persisted to the JCR. This includes the SQL object tree,
 * the profile manager.
 *
 */

public class ArchitectProject extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    @SuppressWarnings("unchecked")
    public static List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
                Arrays.asList(SQLObjectRoot.class, ProfileManager.class, DDLGenerator.class, SQLDatabase.class)));
    
    /**
     * There is a 1:1 ratio between the session and the project.
     */
    private ArchitectSession session;
    private SQLObjectRoot rootObject;
    private ProfileManager profileManager;  
    private SQLDatabase db;
    
    private DDLGenerator ddlGenerator;    
    
    /**
     * Constructs an architect project. The init method must be called immediately
     * after creating a project.
     * @throws SQLObjectException
     */
    @Constructor
    public ArchitectProject() 
            throws SQLObjectException {
        this.rootObject = new SQLObjectRoot();
        this.db = new SQLDatabase();
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new SQLObjectException("SQL Error in ddlGenerator",e);
        }
        
    }

    /**
     * Call this to initialize the session and the children of the project.
     */
    public void init(ArchitectSession session) {
        this.session = session;
        rootObject.addSQLObjectPreEventListener(new SourceObjectIntegrityWatcher(session));                        
        rootObject.setParent(this);
        db.setParent(this);
        ddlGenerator.setParent(this);
        
    }
    
    /**
     * Returns the top level object in the SQLObject hierarchy.
     * It has no parent and its children are SQLDatabase's.
     */
    @NonProperty
    public SQLObjectRoot getRootObject() {
        return rootObject;
    }
    
    @NonProperty
    public ProfileManager getProfileManager() {
        return profileManager;
    }
    
    @NonProperty
    public SQLDatabase getDatabase(JDBCDataSource ds) {
        try {
            for (SQLDatabase obj : getRootObject().getChildren(SQLDatabase.class)) {
                if (obj.getDataSource().equals(ds)) {
                    return (SQLDatabase) obj;
                }
            }
            if (db.getDataSource().equals(ds)) {
                return db;
            }
            SQLDatabase db = new SQLDatabase(ds);
            getRootObject().addChild(db);
            return db;
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        }
    }
    
    @NonProperty
    public SQLDatabase getTargetDatabase() {
        return db;
    }    

    @NonProperty
    public DDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }
    
    @NonProperty
    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException {
        SQLObject root = getRootObject();
        try {
            root.begin("Setting source database list");
            for (int i = root.getChildCount()-1; i >= 0; i--) {
                root.removeChild(root.getChild(i));
            }
            for (SQLDatabase db : databases) {
                root.addChild(db);
            }
            root.commit();
        } catch (IllegalArgumentException e) {
            root.rollback("Could not remove child: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ObjectDependentException e) {
            root.rollback("Could not remove child: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @NonProperty
    public void setDDLGenerator(DDLGenerator generator) {
        DDLGenerator oldDDLG = ddlGenerator;
        ddlGenerator = generator;
        if (oldDDLG != null) {
            fireChildRemoved(DDLGenerator.class, oldDDLG, 0);
        }
        fireChildAdded(DDLGenerator.class, generator, 0);
    }
    
    @NonProperty
    public void setProfileManager(ProfileManager manager) {
        ProfileManager oldManager = profileManager;
        profileManager = manager;
        if (oldManager != null) {
            fireChildRemoved(ProfileManager.class, oldManager, 0);
        }
        fireChildAdded(ProfileManager.class, manager, 0);
        profileManager.setParent(this);
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }        
    
    @Transient @Accessor
    public SPSession getSession() {
        return session;
    }

    public boolean allowsChildren() {
        return true;
    }
    
    public int childPositionOffset(Class<? extends SPObject> childType) {
        
        if (SQLObjectRoot.class.isAssignableFrom(childType)) {
            return 0;
        } else if (ProfileManager.class.isAssignableFrom(childType)) {
            return 1;
        } else if (DDLGenerator.class.isAssignableFrom(childType)) {
            return 2;
        } else if (SQLDatabase.class.isAssignableFrom(childType)) {
            return 3;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    public List<SPObject> getChildren() {
        List<SPObject> allChildren = new ArrayList<SPObject>();
        allChildren.add(rootObject);
        allChildren.add(profileManager);
        allChildren.add(ddlGenerator); 
        allChildren.add(db);
        return allChildren;
    }
    
    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        db.removeDependency(dependency);
        rootObject.removeDependency(dependency);
        profileManager.removeDependency(dependency);
        ddlGenerator.removeDependency(dependency);
    }
    
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof SQLObjectRoot) {
            rootObject = (SQLObjectRoot) child;
        } else {
            super.addChildImpl(child, index);
        }
    }
}
