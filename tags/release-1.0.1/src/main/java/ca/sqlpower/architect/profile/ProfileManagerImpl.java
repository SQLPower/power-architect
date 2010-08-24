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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.enterprise.ArchitectPersisterSuperConverter;
import ca.sqlpower.architect.enterprise.ArchitectSessionPersister;
import ca.sqlpower.architect.profile.event.ProfileChangeEvent;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectPreEvent;
import ca.sqlpower.sqlobject.SQLObjectPreEventListener;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.SessionNotFoundException;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

/**
 * The default ProfileManager implementation. Creates profiles of tables,
 * optionally using a separate worker thread.
 * 
 * @version $Id$
 */
public class ProfileManagerImpl extends AbstractSPObject implements ProfileManager {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
                Arrays.asList(ProfileSettings.class, TableProfileResult.class)));
    
    /**
     * Watches the session's root object, and reacts when SQLDatabase items
     * are removed. In that case, it ensures there are no dangling references
     * from the profiled tables back to the removed database or its children.
     * If there are, the user is asked to decide to either cancel the operation
     * or allow the ETL lineage (SQLColumn.sourceColumn) references to be broken.
     */
    private class DatabaseRemovalWatcher implements SQLObjectPreEventListener {

        public void dbChildrenPreRemove(SQLObjectPreEvent e) {
            logger.debug("Pre-remove on profile manager");
            UserPrompter up = getParent().getSession().createUserPrompter(
                    "{0} tables have been profiled from the database {1}.\n" +
                    "\n" +
                    "If you proceed, the profiling information from the database" +
                    " will be removed.", UserPromptType.BOOLEAN, UserPromptOptions.OK_NOTOK_CANCEL, UserPromptResponse.OK, 
                    Boolean.TRUE, "Remove Profiles", "Keep Profiles", "Cancel");
            for (SQLObject so : e.getChildren()) {
                SQLDatabase db = (SQLDatabase) so;
                List<TableProfileResult> refs = new ArrayList<TableProfileResult>(); 
                for (TableProfileResult tpr : getResults()) {
                    if (tpr.getProfiledObject().getParentDatabase() != null && tpr.getProfiledObject().getParentDatabase().equals(db)) {
                        refs.add(tpr);
                    }
                }
                if (!refs.isEmpty()) {
                    UserPromptResponse response = up.promptUser(refs.size(), db.getName());
                    if (response == UserPromptResponse.OK) {
                        logger.debug("We got the ok to delete.");
                        // disconnect those columns' source columns
                        for (TableProfileResult tpr : refs) {
                            results.remove(tpr);
                        }
                    } else if (response == UserPromptResponse.NOT_OK) {
                        e.veto();
                    } else if (response == UserPromptResponse.CANCEL) {
                        e.veto();
                    }
                }
            }
        }

    }

    private static final Logger logger = Logger.getLogger(ProfileManagerImpl.class);
    
    /**
     * The current list of listeners who want to know when the contents
     * of this profile manager change.
     */
    private final List<ProfileChangeListener> profileChangeListeners = new ArrayList<ProfileChangeListener>();

    /**
     * All profile results in this profile manager. IMPORTANT: Do not modify this list
     * directly. Always use {@link #addResults(List)},
     * {@link #removeResults(List)}, and {@link #clear()}.
     */
    private final List<TableProfileResult> results = new ArrayList<TableProfileResult>();
    
    /**
     * The defaults that new profile results will be created with.
     * XXX these are specific to the remote database profiler!
     */
    private ProfileSettings defaultProfileSettings = new ProfileSettings();

    /**
     * The Profile Executor manages the thread that actually does the work
     * of creating the profiles.
     */
    private ExecutorService profileExecutor = Executors.newSingleThreadExecutor();

    /**
     * The creator that will be used to create profiles.
     */
    private TableProfileCreator creator = new RemoteDatabaseProfileCreator(getDefaultProfileSettings());
    
    /**
     * Watches for database removals and updates the manager accordingly.
     */
    private final DatabaseRemovalWatcher databaseRemovalWatcher = new DatabaseRemovalWatcher();
    
    /**
     * A list of the different existing profile creators that can be used.
     */
    private List<TableProfileCreator> profileCreators = Arrays.asList(
            (TableProfileCreator)new RemoteDatabaseProfileCreator(getDefaultProfileSettings()),
            new LocalReservoirProfileCreator(getDefaultProfileSettings()));

    /**
     * A Callable interface which populates a single profile result then returns
     * it. Profile results don't throw the exceptions their populate() methods
     * encounter, but this callable wrapper knows about that, and digs up and
     * rethrows the exceptions encountered by populate(). This is more normal,
     * and is also compatible with the ExecutorService implementation provided
     * in the standard Java library (it handles exceptions and restarts the work
     * queue properly).
     * <p>
     * To protect the profiling from threading problems all of the objects that
     * exist in the {@link SPObject} tree are duplicated before being passed to
     * the profiler. This way the profiler can do as it desires with the objects
     * and these objects cannot be modified by outside classes. When the
     * profiling is one the profiles themselves will be updated on the
     * foreground thread so the object model stays single threaded.
     */
    private class ProfileResultCallable implements Callable<TableProfileResult> {
        
        /**
         * The table profile result this Callable populates.
         */
        private TableProfileResult actualTPR;

        /**
         * The table profile that is a copy of the real one that will be
         * populated on a background thread. This prevents any problems with
         * threading as the real profile result will not be modified until we
         * are back on the foreground thread.
         */
        private final TableProfileResult tpr;

        ProfileResultCallable(TableProfileResult actualTPR) {
            if (actualTPR == null) throw new NullPointerException("Can't populate a null profile result!");
            this.actualTPR = actualTPR;
            SQLTable table;
            TableProfileResult tempTPR;
            try {
                SQLTable profileTable = actualTPR.getProfiledObject();
                table = new SQLTable(profileTable.getParentDatabase(), true);
                table.setUUID(profileTable.getUUID());
                table.updateToMatch(profileTable);
                for (SQLColumn col : profileTable.getColumns()) {
                    SQLColumn newCol = new SQLColumn();
                    newCol.updateToMatch(col);
                    newCol.setUUID(col.getUUID());
                    table.addColumn(newCol);
                }
                tempTPR = new TableProfileResult(actualTPR, table);
                
                //Setting the profile result UUID to match which allows the
                //persister to update the actual tpr at the end.
                tempTPR.setUUID(actualTPR.getUUID());
                //Need to do the same for the parent pointer but do not want
                //to attach it to the actual profile manager.
                ProfileManager backgroundPM = new ProfileManagerImpl();
                backgroundPM.setUUID(actualTPR.getParent().getUUID());
                tempTPR.setParent(backgroundPM);
            } catch (Exception e) {
                //If an exception is thrown during setup define the profile to have an exception on
                //it and handle appropriately when doing the profile.
                tempTPR = null;
                actualTPR.setException(e);
            }
            tpr = tempTPR;
        }
        
        /**
         * Populates the profile result, throwing any exceptions encountered.
         */
        public TableProfileResult call() throws Exception {
            //Exception occurred during setup.
            if (actualTPR.getException() != null) {
                throw actualTPR.getException();
            }
            creator.doProfile(tpr);
            Runnable runner = new Runnable() {
                public void run() {
                    //None of the profiling creates or saves any data source information so an
                    //empty data source is used for the converter. If the profiling stores
                    //data source information in the future we may need the data source collection
                    //in the project.
                    DataSourceCollection<SPDataSource> dsCollection = new PlDotIni();
                    
                    SPObject root = actualTPR.getWorkspaceContainer().getWorkspace();
                    ArchitectPersisterSuperConverter converter = 
                        new ArchitectPersisterSuperConverter(dsCollection, root);
                    ArchitectSessionPersister persister = 
                        new ArchitectSessionPersister("Profiling persister", root, converter);
                    persister.setWorkspaceContainer(actualTPR.getWorkspaceContainer());
                    SPPersisterListener eventCreator = new SPPersisterListener(persister, converter);
                    eventCreator.persistObject(tpr, 
                            actualTPR.getParent().getChildren(TableProfileResult.class).indexOf(actualTPR),
                            false);
                }
            };
            try {
                actualTPR.getRunnableDispatcher().runInForeground(runner);
            } catch (SessionNotFoundException e) {
                runner.run();
            }
            
            if (tpr.getException() != null) {
                throw tpr.getException();
            }
            
            return actualTPR;
        }
    }
    
    @Constructor
    public ProfileManagerImpl() {
        defaultProfileSettings.setParent(this);
        setName("Profile Manager");
    }
    
    @Override @Mutator
    public void setParent(SPObject parent) {
        SPObject oldParent = getParent();
        if (getParent() != null) {
            ((ArchitectProject) getParent()).getRootObject().removeSQLObjectPreEventListener(databaseRemovalWatcher);
        }
        super.setParent(parent);
        final ArchitectProject architectProject = (ArchitectProject) parent;
        if (parent != null && architectProject.getRootObject() != null) {
            architectProject.getRootObject().addSQLObjectPreEventListener(databaseRemovalWatcher);
        }
        firePropertyChange("parent", oldParent, parent);
    }
    
    @Override @Accessor
    public ArchitectProject getParent() {
        return (ArchitectProject) super.getParent();
    }
    
    public void addTableProfileResult(TableProfileResult child) {
        results.add(child);
        child.setParent(this);
        fireProfilesAdded(Collections.singletonList(child));
    }
    
    /**
     * This is the method that everything which wants to add a profile result
     * must call in order to add the result. It takes care of setting SQLObject
     * client properties, firing events, and actually adding the profile to the
     * set of profile results in this profile manager.
     */
    private void addResults(List<TableProfileResult> newResults) {
        results.addAll(newResults);
        for (TableProfileResult tpr : newResults) {
            tpr.setParent(this);
        }
        fireProfilesAdded(newResults);
        for (TableProfileResult newResult : newResults) {
            SQLTable table = newResult.getProfiledObject();
            table.putClientProperty(ProfileManager.class, PROFILE_COUNT_PROPERTY, getResults(table).size());
        }
    }
    
    /* docs inherited from interface */
    public TableProfileResult createProfile(SQLTable table) throws SQLObjectException {
        TableProfileResult tpr = new TableProfileResult(table, getDefaultProfileSettings());
        addResults(Collections.singletonList(tpr));
        
        try {
            profileExecutor.submit(new ProfileResultCallable(tpr)).get();
            assert (tpr.getProgressMonitor().isFinished());
        } catch (InterruptedException ex) {
            logger.info("Profiling was interrupted (likely because this manager is being shut down)");
        } catch (ExecutionException ex) {
            throw new SQLObjectException("Profile execution failed", ex);
        }
        
        return tpr;
    }

    /* docs inherited from interface */
    public Collection<Future<TableProfileResult>> asynchCreateProfiles(Collection<SQLTable> tables) {
        
        List<TableProfileResult> profiles = new ArrayList<TableProfileResult>();
        for (SQLTable t : tables) {
            profiles.add(new TableProfileResult(t, getDefaultProfileSettings()));
        }
        
        addResults(profiles);
        
        List<Future<TableProfileResult>> results = new ArrayList<Future<TableProfileResult>>();
        for (TableProfileResult tpr : profiles) {
            results.add(scheduleProfile(tpr));
        }
        return results;
    }

    /* docs inherited from interface */
    public Future<TableProfileResult> scheduleProfile(TableProfileResult result) {
        return profileExecutor.submit(new ProfileResultCallable(result));
    }
    
    /* docs inherited from interface */
    public void clear() {
        List<TableProfileResult> oldResults = new ArrayList<TableProfileResult>(results);
        results.clear();
        fireProfilesRemoved(oldResults);
        int index = 0;
        try {
            begin("Removing all profiles");
            for (TableProfileResult oldResult : oldResults) {
                fireChildRemoved(TableProfileResult.class, oldResult, index);
                SQLTable table = oldResult.getProfiledObject();
                table.putClientProperty(ProfileManager.class, PROFILE_COUNT_PROPERTY, 0);
                index++;
            }
            commit();
        } catch (RuntimeException e) {
            rollback(e.getMessage());
            throw e;
        } catch (Throwable t) {
            rollback(t.getMessage());
            throw new RuntimeException(t);
        }
    }

    /* docs inherited from interface */
    @NonProperty
    public List<TableProfileResult> getResults() {
        // this could be optimized by caching the current result list snapshot, but enh.
        return Collections.unmodifiableList(new ArrayList<TableProfileResult>(results));
    }

    /* docs inherited from interface */
    @NonProperty
    public List<TableProfileResult> getResults(SQLTable t) {
        List<TableProfileResult> someResults = new ArrayList<TableProfileResult>();
        for (TableProfileResult tpr : results) {
            if (tpr.getProfiledObject().equals(t)) {
                someResults.add(tpr);
            }
        }
        return Collections.unmodifiableList(someResults);
    }

    /* docs inherited from interface */
    public boolean removeProfile(TableProfileResult victim) {
        int index = results.indexOf(victim);
        boolean removed = results.remove(victim);
        if (removed) {
            fireChildRemoved(TableProfileResult.class, victim, index);
            fireProfilesRemoved(Collections.singletonList(victim));
        }
        SQLTable table = victim.getProfiledObject();
        table.putClientProperty(ProfileManager.class, PROFILE_COUNT_PROPERTY, getResults(table).size());
        return removed;
    }

    /* docs inherited from interface */
    @NonProperty
    public ProfileSettings getDefaultProfileSettings() {
        return defaultProfileSettings;
    }

    /* docs inherited from interface */
    @NonProperty
    public void setDefaultProfileSettings(ProfileSettings settings) {
        ProfileSettings oldSettings = defaultProfileSettings;
        defaultProfileSettings = settings;
        defaultProfileSettings.setParent(this);
        if (oldSettings != null) {
            fireChildRemoved(ProfileSettings.class, oldSettings, 0);
        }
        if (settings != null) {
            fireChildAdded(ProfileSettings.class, settings, 0);
        }
    }

    /* docs inherited from interface */
    @NonBound
    public void setProcessingOrder(List<TableProfileResult> tpr) {
        
    }

    /**
     * This is a hook designed so the SwingUIProject can insert profile results
     * into this profile manager as it is reading in a project file.  It is
     * not appropriate to use otherwise.
     * <p>
     * This method fires an event every time it adds a table profile result, because
     * not doing so makes it necessary to create the profile manager view after
     * loading in the profile results, and it is impossible to guarantee that policy
     * from here.
     * <p>
     * The idea is, the SwingUIProject stores all profile results in a flat space
     * (table and column results are sibling elements) so it needs our help to
     * put everything back together into the original hierarchy.  This method hangs
     * onto all TableProfileResult objects given to it, and ignores all other result
     * types, assuming the client code will do the appropriate hookups.
     * <p>
     * You might be asking yourself, "why not store the profile results in the
     * same hierarchy as they had when they were originally created, so we don't
     * need any more error-prone code to recreate what we already had and then
     * threw away?  Beside reducing bugs, it would eliminate the need for this public
     * method and accompanying docs that warn you against using it."  So was I.
     * Though I fixed it, this method still has to hang around for backwards
     * compatibility reasons.
     */
    public void loadResult(ProfileResult<? extends SQLObject> pr) {
        if (pr instanceof TableProfileResult) {
            TableProfileResult tpr = (TableProfileResult) pr;
            addResults(Collections.singletonList(tpr));
        }
        // the column results will get added to the table result by
        // the project's profile result factory class
    }
    
    /**
     * Adds the given listener to this profile manager.  The listener will be notified
     * of additions and removals of results in this profile manager.
     */
    public void addProfileChangeListener(ProfileChangeListener listener) {
        profileChangeListeners.add(listener);
    }

    /**
     * Removes the given listener.  After removal, the listener will no longer be notified
     * of changes to this profile manager.
     */
    public void removeProfileChangeListener(ProfileChangeListener listener) {
        profileChangeListeners.remove(listener);
    }

    /**
     * Creates and fires a "profilesAdded" event for the given profile results.
     */
    private void fireProfilesAdded(List<TableProfileResult> results) {
        if (results == null) throw new NullPointerException("Can't fire event for null profile list");
        for (TableProfileResult tpr : results) {
            fireChildAdded(TableProfileResult.class, tpr, results.indexOf(tpr));
        }
        ProfileChangeEvent e = new ProfileChangeEvent(this, results);
        for (int i = profileChangeListeners.size() - 1; i >= 0; i--) {
            profileChangeListeners.get(i).profilesAdded(e);
        }
    }

    /**
     * Creates and fires a "profilesRemoved" event for the given list of profile results.
     */
    private void fireProfilesRemoved(List<TableProfileResult> removedList) {
        if (removedList == null) throw new NullPointerException("Null list not allowed");
        ProfileChangeEvent e = new ProfileChangeEvent(this, removedList);
        for (int i = profileChangeListeners.size() - 1; i >= 0; i--) {
            profileChangeListeners.get(i).profilesRemoved(e);
        }
    }

    public void close() {
        profileExecutor.shutdown();
    }

    @NonBound
    public List<TableProfileCreator> getProfileCreators() {
        return Collections.unmodifiableList(profileCreators);
    }

    @NonBound
    public TableProfileCreator getCreator() {
        return creator;
    }

    @NonBound
    public void setCreator(TableProfileCreator tpc) {
        this.creator = tpc;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {        
        if (child.getClass().isAssignableFrom(TableProfileResult.class)) {
            if (removeProfile((TableProfileResult)child)) {
                return true;
            } else {
                throw new IllegalArgumentException("Table does not exist in this profile manager");
            }
        } else {
            throw new IllegalArgumentException("Child must be of type TableProfileResult");
        }
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof ProfileSettings) {
            setDefaultProfileSettings((ProfileSettings) child);
        } else if (child instanceof TableProfileResult) {
            //XXX make a new add method that can add one result.
            addResults(Collections.singletonList((TableProfileResult) child));
        }
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType.isAssignableFrom(ProfileSettings.class)) {
            return 0;
        } else if (childType.isAssignableFrom(TableProfileResult.class)) {
            if (defaultProfileSettings != null) {
                return 1;
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    public List<? extends SPObject> getChildren() {
        List<SPObject> allChildren = new ArrayList<SPObject>();        
        allChildren.add(defaultProfileSettings);
        allChildren.addAll(results);
        return allChildren;
    }

    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        
    }

}
