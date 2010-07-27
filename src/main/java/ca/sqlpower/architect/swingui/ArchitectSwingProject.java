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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ProjectSettings;
import ca.sqlpower.architect.ddl.critic.CriticManager;
import ca.sqlpower.architect.enterprise.BusinessDefinition;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.architect.enterprise.FormulaMetricCalculation;
import ca.sqlpower.architect.etl.kettle.KettleSettings;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.MappedSPTree;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.ConstructorParameter.ParameterType;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.util.RunnableDispatcher;
import ca.sqlpower.util.WorkspaceContainer;

/**
 * 
 * This class is the root object of an ArchitectSession. There is an ArchitectProject
 * for every ArchitectSession. The ArchitectProject, and all its children, will be
 * listened to and persisted to the JCR. This includes the SQL object tree,
 * the profile manager.
 *
 */

public class ArchitectSwingProject extends ArchitectProject implements MappedSPTree {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections
            .unmodifiableList(new ArrayList<Class<? extends SPObject>>(Arrays.asList(UserDefinedSQLType.class, 
                    DomainCategory.class, SPObjectSnapshot.class, SQLObjectRoot.class,
                    OLAPRootObject.class, PlayPenContentPane.class, ProfileManager.class, ProjectSettings.class,
                    CriticManager.class, KettleSettings.class, User.class, Group.class, 
                    BusinessDefinition.class, FormulaMetricCalculation.class)));
    
    /**
     * A hash map mapping all the descendants of this project.
     * It must be kept up to date by listening to all its descendant nodes
     * for child added and child removed events.
     */
    private final HashMap<String, SPObject> projectMap;
    
    /**
     * The listener used to keep the projectMap up to date.
     */
    private final SPListener projectMapListener = new AbstractSPListener() {
        public void childAdded(SPChildEvent e) {
            populateTreeMap(e.getChild());            
        }      

        public void childRemoved(SPChildEvent e) {           
            unpopulateTreeMap(e.getChild());                  
        }
        
        public void propertyChanged(PropertyChangeEvent e) {
            if (e.getPropertyName().toUpperCase().equals("UUID")) {
                projectMap.put((String) e.getNewValue(), projectMap.remove(e.getOldValue()));
            }
        }
        
        private void populateTreeMap(SPObject addedChild) {
            if (projectMap.put(addedChild.getUUID(), addedChild) != null) {
                throw new IllegalStateException("Object added under project with same UUID! Adding " + addedChild);
            }
            addedChild.addSPListener(this);
            //Be careful here. If calling getChildren adds children to the object this
            //listener will get called twice, once because the listener is now on the parent
            //and again for the loop.
            if (addedChild instanceof SQLObject) {
                for (SPObject o : ((SQLObject) addedChild).getChildrenWithoutPopulating()) {
                    populateTreeMap(o);
                }
            } else {
                for (SPObject o : addedChild.getChildren()) {
                    populateTreeMap(o);
                }
            }
        }
        
        private void unpopulateTreeMap(SPObject removedChild) {
            if (projectMap.remove(removedChild.getUUID()) != removedChild) {
                throw new IllegalStateException("Inconsistent project map: " +
                        "removed child's entry in map was either null, or different object.");
            }
            removedChild.removeSPListener(this);
            
            //Removing a listener should not cause a SQLObject to populate but we need to
            //remove the listener from all descendants and clear the map .
            if (removedChild instanceof SQLObject) {
                for (SPObject o : ((SQLObject) removedChild).getChildrenWithoutPopulating()) {
                    unpopulateTreeMap(o);
                }
            } else {
                for (SPObject o : removedChild.getChildren()) {
                    unpopulateTreeMap(o);
                }
            }
        }
    };

    private PlayPenContentPane playPenContentPane;
    
    /**
     * This OLAP object contains the OLAP session.
     */
    private final OLAPRootObject olapRootObject;
    
    private final List<PlayPenContentPane> olapContentPaneList = new ArrayList<PlayPenContentPane>();
    
    /**
     * The OLAP content panes (one for each OLAPSession)
     */
    private final Map<OLAPSession, PlayPenContentPane> olapContentPaneMap = new HashMap<OLAPSession, PlayPenContentPane>();
    
    private final KettleSettings kettleSettings;
    
    private final CriticManager criticManager;
    
    /**
     * Constructs an architect project. The init method must be called immediately
     * after creating a project.
     * @throws SQLObjectException
     */
    public ArchitectSwingProject() throws SQLObjectException {
        //must call super with no args to create the target database as required.
        super();
        this.olapRootObject = new OLAPRootObject();
        olapRootObject.setParent(this);
        this.kettleSettings = new KettleSettings();
        kettleSettings.setParent(this);
        
        setName("Architect Project");
        projectMap = new HashMap<String, SPObject>();
        criticManager = new CriticManager();
        criticManager.setParent(this);
        projectMapListener.childAdded(new SPChildEvent(this, null, this, 0, null));
    }
    
    /**
     * Constructs an architect project. The init method must be called immediately
     * after creating a project.
     */
    @Constructor
    public ArchitectSwingProject(
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="rootObject") SQLObjectRoot rootObject,
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="olapRootObject") OLAPRootObject olapRootObject,
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="kettleSettings") KettleSettings kettleSettings,
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="profileManager") ProfileManager profileManager,
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="criticManager") CriticManager criticManager
            ) throws SQLObjectException {
        super(rootObject, profileManager);
        this.olapRootObject = olapRootObject;
        olapRootObject.setParent(this);
        this.kettleSettings = kettleSettings;
        kettleSettings.setParent(this);
        this.criticManager = criticManager;
        criticManager.setParent(this);
        
        setName("Architect Project");
        projectMap = new HashMap<String, SPObject>();
        projectMapListener.childAdded(new SPChildEvent(this, null, this, 0, null));
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child instanceof PlayPenContentPane) {
            return removeOLAPContentPane((PlayPenContentPane) child);
        } else {
            return super.removeChildImpl(child);
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
        allChildren.addAll(getSqlTypes());
        allChildren.addAll(getDomainCategories());
        allChildren.addAll(getSqlTypeSnapshots());
        allChildren.add(getRootObject());
        allChildren.add(olapRootObject);
        if (playPenContentPane != null) {
            allChildren.add(playPenContentPane);
        }
        allChildren.addAll(olapContentPaneList);
        if (getProfileManager() != null) {
            allChildren.add(getProfileManager());
        }
        allChildren.add(getProjectSettings());
        allChildren.add(criticManager);
        allChildren.add(kettleSettings);
        //TODO make specific getters for these types.
        allChildren.addAll(getUsers());
        allChildren.addAll(getGroups());
        allChildren.addAll(getBusinessDefinitions());
        allChildren.addAll(getFormulas());
        return Collections.unmodifiableList(allChildren);
    }
    
    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        super.removeDependency(dependency);
        getOlapRootObject().removeDependency(dependency);
        getCriticManager().removeDependency(dependency);
        getKettleSettings().removeDependency(dependency);
        getPlayPenContentPane().removeDependency(dependency);
        for (PlayPenContentPane ppcp : getOlapContentPanes()) {
            ppcp.removeDependency(dependency);
        }
        //XXX Need to cover the remaining child types
    }
    
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof PlayPenContentPane) {
            PlayPenContentPane pane = (PlayPenContentPane) child;
            if (index == 0) {
                setPlayPenContentPane(pane);
            } else {
                addOLAPContentPane(pane);
            }
        } else {
            super.addChildImpl(child, index);
        }
    }


    @NonProperty
    public void setPlayPenContentPane(PlayPenContentPane pane) {
        PlayPenContentPane oldPane = playPenContentPane;
        playPenContentPane = pane;      
        if (oldPane != null) {
            if (pane.getPlayPen() == null) {
                // This is the usual scenario, where we have a PlayPenContentPane
                // in the project initially, containing the PlayPen, and the
                // server is trying to persist its PlayPenContentPane
                // which does not have a PlayPen.
                PlayPen pp = oldPane.getPlayPen();
                pp.setContentPane(pane);
            }            
            pane.setComponentListeners(oldPane.getComponentListeners());
            fireChildRemoved(oldPane.getClass(), oldPane, 0);
        }        
        fireChildAdded(pane.getClass(), playPenContentPane, 0);
        pane.setParent(this);
    }

    @NonProperty
    public PlayPenContentPane getPlayPenContentPane() {
        return playPenContentPane;
    }
    
    @NonProperty
    public OLAPRootObject getOlapRootObject() {
        return olapRootObject;
    }
    
    @NonProperty
    public KettleSettings getKettleSettings() {
        return kettleSettings;
    }
    
    @NonProperty
    public List<PlayPenContentPane> getOlapContentPanes() {
        return Collections.unmodifiableList(olapContentPaneList);
    }
    
    @NonBound
    public PlayPenContentPane getOlapContentPane(OLAPSession session) {
        return olapContentPaneMap.get(session);
    }
    
    public void addOLAPContentPane(PlayPenContentPane olapContentPane) {
        if (!(olapContentPane.getModelContainer() instanceof OLAPSession)) {
            throw new IllegalArgumentException(
                    "PlayPenContentPane is not modelling an OLAPSession");
        }
        olapContentPaneList.add(olapContentPane);
        olapContentPaneMap.put((OLAPSession) olapContentPane.getModelContainer(), olapContentPane);
        int index = olapContentPaneList.indexOf(olapContentPane);
        if (playPenContentPane != null) index++;
        olapContentPane.setParent(this);
        fireChildAdded(PlayPenContentPane.class, olapContentPane, index);        
    }
    
    public boolean removeOLAPContentPane(PlayPenContentPane olapContentPane) {
        int index = olapContentPaneList.indexOf(olapContentPane);
        if (!olapContentPaneList.remove(olapContentPane)) return false;
        if (olapContentPaneMap.remove(olapContentPane.getModelContainer()) == null) {
            throw new IllegalStateException("Tried removing OLAP PlayPenContentPane from " + 
                    " project mapping but could not find it from its OLAPSession");
        }
        if (playPenContentPane != null) index++;
        fireChildRemoved(PlayPenContentPane.class, olapContentPane, index);
        olapContentPane.setParent(null);
        return true;
    }
    
    @NonBound
    public SPObject getObjectInTree(String uuid) {
        return projectMap.get(uuid);
    }
    
    /**
     * Locates the SPObject which has the given UUID, under this project,
     * returning null if the item is not found. Throws ClassCastException
     * if in item is found, but it is not of the expected type.
     */
    @NonBound
    public <T extends SPObject> T getObjectInTree(String uuid, Class<T> expectedType) {
        return expectedType.cast(getObjectInTree(uuid));
    }
    
    @NonProperty
    public CriticManager getCriticManager() {
        return criticManager;
    }
}
