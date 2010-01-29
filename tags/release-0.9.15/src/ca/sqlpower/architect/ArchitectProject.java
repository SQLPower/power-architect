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
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
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
 * the profile manager, forward engineering settings, and compare DM settings.
 *
 */

public class ArchitectProject extends AbstractSPObject {
    
    private final ArchitectSession session;
    private final SQLObjectRoot rootObject;
    private ProfileManagerImpl profileManager;  
    private SQLDatabase db;
    
    private DDLGenerator ddlGenerator;    
    
    public ArchitectProject(ArchitectSession session) throws SQLObjectException {
        this.session = session;
        this.rootObject = new SQLObjectRoot();
        this.db = new SQLDatabase();
        
        rootObject.addSQLObjectPreEventListener(new SourceObjectIntegrityWatcher(session));                        
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new SQLObjectException("SQL Error in ddlGenerator",e);
        }
        
        rootObject.setParent(this);
        db.setParent(this);
        ddlGenerator.setParent(this);
        
    }
    
    /**
     * Returns the top level object in the SQLObject hierarchy.
     * It has no parent and its children are SQLDatabase's.
     */
    public SQLObjectRoot getRootObject() {
        return rootObject;
    }
    
    public ProfileManagerImpl getProfileManager() {
        return profileManager;
    }
    
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
    
    public SQLDatabase getTargetDatabase() {
        return db;
    }    

    public DDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }
    
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
    
    public void setDDLGenerator(DDLGenerator generator) {
        ddlGenerator = generator;
    }
    
    public void setProfileManager(ProfileManagerImpl manager) {
        profileManager = manager;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }        
    
    public SPSession getSession() {
        return session;
    }

    public boolean allowsChildren() {
        return true;
    }
    
    public int childPositionOffset(Class<? extends SPObject> childType) {
        
        if (childType.isAssignableFrom(SQLObjectRoot.class)) {
            return 0;
        } else if (childType.isAssignableFrom(ProfileManager.class)) {
            return 1;
        } else if (childType.isAssignableFrom(DDLGenerator.class)) {
            return 2;
        } else if (childType.isAssignableFrom(SQLDatabase.class)) {
            return 3;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>();
        childTypes.add(SQLObjectRoot.class);
        childTypes.add(ProfileManager.class);
        childTypes.add(DDLGenerator.class);
        childTypes.add(SQLDatabase.class);
        return childTypes;
    }

    public List<SPObject> getChildren() {
        List<SPObject> allChildren = new ArrayList<SPObject>();
        allChildren.add(rootObject);
        allChildren.add(profileManager);
        allChildren.add(ddlGenerator); 
        allChildren.add(db);
        return allChildren;
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        
    }

}
