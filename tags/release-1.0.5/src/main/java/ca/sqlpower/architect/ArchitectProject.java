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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.object.annotation.ConstructorParameter.ParameterType;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.util.RunnableDispatcher;
import ca.sqlpower.util.SessionNotFoundException;
import ca.sqlpower.util.WorkspaceContainer;

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
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections
            .unmodifiableList(new ArrayList<Class<? extends SPObject>>(Arrays.asList(UserDefinedSQLType.class, 
                    SQLObjectRoot.class, ProfileManager.class, 
                    ProjectSettings.class, User.class, Group.class)));
    
    /**
     * There is a 1:1 ratio between the session and the project.
     */
    private ArchitectSession session;
    private final SQLObjectRoot rootObject;
    private ProfileManager profileManager;
    private ProjectSettings projectSettings;
    
    private List<User> users = new ArrayList<User>();
    private List<Group> groups = new ArrayList<Group>();
    private final List<UserDefinedSQLType> sqlTypes = new ArrayList<UserDefinedSQLType>();
    
    
    // Metadata property
    private String etlProcessDescription;
    
    /**
     * The current integrity watcher on the project.
     */
    private SourceObjectIntegrityWatcher currentWatcher;
    
    /**
     * Constructs an architect project. The init method must be called
     * immediately after creating a project.
     * <p>
     * This will also add a target database to the root object as required for
     * new projects.
     * 
     * @throws SQLObjectException
     */
    public ArchitectProject() throws SQLObjectException {
        this(new SQLObjectRoot(), null);
        SQLDatabase targetDatabase = new SQLDatabase();
        targetDatabase.setPlayPenDatabase(true);
        rootObject.addChild(targetDatabase, 0);
    }
    
    /**
     * The init method for this project must be called immediately after this
     * object is constructed.
     * <p>
     * This will rely on the target database being added from the persist calls
     * that creates this project.
     * <p>
     * The profile manager will be set to null with this constructor so if one
     * is needed it should be set at sometime in the future before use.
     * 
     * @param rootObject
     *            The root object that holds all of the source databases for the
     *            current project.
     */
    public ArchitectProject(SQLObjectRoot root) throws SQLObjectException {
        this(root, null);
    }

    /**
     * The init method for this project must be called immediately after this
     * object is constructed.
     * <p>
     * This will rely on the target database being added from the persist calls
     * that creates this project.
     * 
     * @param rootObject
     *            The root object that holds all of the source databases for the
     *            current project.
     * @param profileManager
     *            The default profile manager for this project. This may be null
     *            if it is set later or the profile manager is not used.
     * @param snapshotCollection
     *            An object that will hold all of the snapshots and copied types
     *            and domains for the current project.
     */
    @Constructor
    public ArchitectProject(
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="rootObject") SQLObjectRoot rootObject,
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="profileManager") ProfileManager profileManager) 
            throws SQLObjectException {
        this.rootObject = rootObject;
        rootObject.setParent(this);
        projectSettings = new ProjectSettings();
        projectSettings.setParent(this);
        if (profileManager != null) {
            setProfileManager(profileManager);
        }
        setName("Architect Project");
    }

    /**
     * Call this to initialize the session and the children of the project.
     */
    @Transient @Mutator
    public void setSession(ArchitectSession session) {
        if (this.session != null) {
            rootObject.removeSQLObjectPreEventListener(currentWatcher);
            currentWatcher = null;
        }
        this.session = session;
        if (this.session != null) {
            currentWatcher = new SourceObjectIntegrityWatcher(session);
            rootObject.addSQLObjectPreEventListener(currentWatcher);
        }
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
            SQLDatabase db = new SQLDatabase(ds);
            getRootObject().addChild(db);
            return db;
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transient @Accessor
    public SQLDatabase getTargetDatabase() {
        for (SQLDatabase db : rootObject.getChildren(SQLDatabase.class)) {
            if (db.isPlayPenDatabase()) {
                return db;
            }
        }
        throw new IllegalStateException("No target database!");
    }    
    
    @NonProperty
    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException {
        SQLObject root = getRootObject();
        SQLDatabase targetDB = getTargetDatabase();
        try {
            root.begin("Setting source database list");
            for (int i = root.getChildCount()-1; i >= 0; i--) {
                root.removeChild(root.getChild(i));
            }
            if (targetDB != null) {
                root.addChild(targetDB);
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
        if (child instanceof User) {
            return removeUser((User) child);
        } else if (child instanceof Group) {
            return removeGroup((Group) child);
        } else if (child instanceof UserDefinedSQLType) {
            return removeSQLType((UserDefinedSQLType) child);
        }
        return false;
    }
    
    public boolean removeUser(User child) {
        int index = users.indexOf(child);
        boolean removed = users.remove(child);
        if (removed) {
            fireChildRemoved(User.class, child, index);
            child.setParent(null);
        }
        return removed;
    }
    
    public boolean removeGroup(Group child) {
        int index = users.indexOf(child);
        boolean removed = groups.remove(child);
        if (removed) {
            fireChildRemoved(Group.class, child, index);
            child.setParent(null);
        }
        return removed;
    }
    
    public boolean removeSQLType(UserDefinedSQLType child) {
        int index = sqlTypes.indexOf(child);
        boolean removed = sqlTypes.remove(child);
        if (removed) {
            fireChildRemoved(UserDefinedSQLType.class, child, index);
            child.setParent(null);
        }
        return removed;
    }
    
    @Transient @Accessor
    public ArchitectSession getSession() throws SessionNotFoundException {
        if (session != null) {
            return session;
        } else {
            throw new SessionNotFoundException("The project has not been given a session yet. " +
            		"This should only happen during the construction of the project.");
        }
    }

    @Override @Transient @Accessor
    public WorkspaceContainer getWorkspaceContainer() {
        return getSession();
    }
    
    @Override @Transient @Accessor
    public RunnableDispatcher getRunnableDispatcher() {
        return getSession();
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    public List<SPObject> getChildren() {
        List<SPObject> allChildren = new ArrayList<SPObject>();
        // When changing this, ensure you maintain the order specified by allowedChildTypes
        allChildren.addAll(sqlTypes);
        allChildren.add(rootObject);
        if (profileManager != null) {
            allChildren.add(profileManager);
        }
        allChildren.add(projectSettings);
        allChildren.addAll(users);
        allChildren.addAll(groups);
        return Collections.unmodifiableList(allChildren);
    }
    
    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        rootObject.removeDependency(dependency);
        profileManager.removeDependency(dependency);
        //XXX This is missing calling to other children of this class.
    }
    
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof ProfileManager) {
            setProfileManager((ProfileManager) child);
        } else if (child instanceof ProjectSettings) {
            setProjectSettings((ProjectSettings) child);            
        } else if (child instanceof User) {
            addUser((User) child, index);
        } else if (child instanceof Group) {
            addGroup((Group) child, index);
        } else if (child instanceof UserDefinedSQLType) {
            addSQLType((UserDefinedSQLType) child, index);
        } else {
            throw new IllegalArgumentException("Cannot add child of type " + 
                    child.getClass() + " to the project once it has been created.");
        }
    }
    
    

    public void addSQLType(UserDefinedSQLType sqlType, int index) {
        sqlTypes.add(index, sqlType);
        sqlType.setParent(this);
        fireChildAdded(UserDefinedSQLType.class, sqlType, index);
    }
    
    @NonProperty
    public List<UserDefinedSQLType> getSqlTypes() {
        return Collections.unmodifiableList(sqlTypes);
    }
    
    public void addUser(User user, int index) {
        users.add(index, user);
        user.setParent(this);
        fireChildAdded(User.class, user, index);
    }
    
    protected List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
    
    public void addGroup(Group group, int index) {
        groups.add(index, group);
        group.setParent(this);
        fireChildAdded(Group.class, group, index);
    }
    
    protected List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    @NonProperty
    public void setProjectSettings(ProjectSettings settings) {
        ProjectSettings oldSettings = this.projectSettings;
        this.projectSettings = settings;        
        if (oldSettings != null) {
            fireChildRemoved(oldSettings.getClass(), oldSettings, 0);
        }
        fireChildAdded(settings.getClass(), settings, 0);
        settings.setParent(this);
    }
        
    @NonProperty
    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }
    
    @Accessor(isInteresting=true)
    public String getEtlProcessDescription() {
        return etlProcessDescription;
    }
    
    @Mutator
    public void setEtlProcessDescription(String etlProcessDescription) {
        String oldDescription = this.etlProcessDescription;
        this.etlProcessDescription = etlProcessDescription;
        firePropertyChange("etlProcessDescription", oldDescription, etlProcessDescription);
    }
    
}
