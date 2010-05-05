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

package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.etl.kettle.KettleSettings;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.dao.PersistedSPOProperty;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.helper.AbstractSPPersisterHelper;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.util.SQLPowerUtils;

/**
 * An architect specific persister.
 */
public class ArchitectSessionPersister extends SPSessionPersister {

    /**
     * This listener is only attached to the {@link PlayPenContentPane} and it's
     * children. It will cause the viewable components to revalidate after
     * changes from the server since the persister has magic disabled while
     * making changes and the components only revalidate if magic is enabled.
     */
    private final SPListener ppcRevalidateListener = new AbstractSPListener() {
    
        public void childAdded(SPChildEvent e) {
            if (e.getChild() instanceof PlayPenContentPane) {
                e.getChild().addSPListener(this);
                for (PlayPenComponent ppc : ((PlayPenContentPane) e.getChild()).getChildren(PlayPenComponent.class)) {
                    ppc.addSPListener(this);
                }
            } else if (e.getChild() instanceof PlayPenComponent) {
                e.getChild().addSPListener(this);
            }
        }
        
        public void childRemoved(SPChildEvent e) {
            e.getChild().removeSPListener(this);
        }
        
        public void propertyChanged(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof PlayPenComponent) {
                componentsToRevalidate.add(((PlayPenComponent) evt.getSource()));
            }
        }
    
    };
    
    /**
     * This is the set of components that need to be revalidated at the end of
     * a commit so they are revalidated when magic is enabled.
     */
    private final Set<PlayPenComponent> componentsToRevalidate = new HashSet<PlayPenComponent>();

    public ArchitectSessionPersister(String name, SPObject root, SessionPersisterSuperConverter converter) {
        super(name, root, converter);
        SQLPowerUtils.listenToHierarchy(root, ppcRevalidateListener);
    }

    @Override
    public void commit() throws SPPersistenceException {
        synchronized(getWorkspaceContainer().getWorkspace()) {
            componentsToRevalidate.clear();
            super.commit();
            for (PlayPenComponent ppc : componentsToRevalidate) {
                ppc.revalidate();
            }
            componentsToRevalidate.clear();
        }
    }
    
    @Override
    protected void refreshRootNode(PersistedSPObject pso) {
        root.setUUID(pso.getUUID());
        String rootObjectUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "rootObject", persistedProperties);
        
        PersistedSPObject persistedRootObject = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), SQLObjectRoot.class.getName(), rootObjectUUID, persistedObjects);
        ArchitectProject architectProject = (ArchitectProject) root;
        architectProject.getRootObject().setUUID(rootObjectUUID);
        persistedRootObject.setLoaded(true);
        
        String profileManagerUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "profileManager", persistedProperties);
        
        //Null for system projects.
        if (profileManagerUUID != null) {
            PersistedSPObject persistedProfileManager = AbstractSPPersisterHelper.findPersistedSPObject(
                    pso.getUUID(), ProfileManagerImpl.class.getName(), profileManagerUUID, persistedObjects);
            architectProject.getProfileManager().setUUID(profileManagerUUID);
            persistedProfileManager.setLoaded(true);
        }
        
        String olapRootObjectUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "olapRootObject", persistedProperties);
        
        PersistedSPObject persistedOlapRootObject = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), OLAPRootObject.class.getName(), olapRootObjectUUID, persistedObjects);
        architectProject.getOlapRootObject().setUUID(olapRootObjectUUID);
        persistedOlapRootObject.setLoaded(true);
        
        String kettleSettingsUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "kettleSettings", persistedProperties);
        
        PersistedSPObject persistedKettleSettings = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), KettleSettings.class.getName(), kettleSettingsUUID, persistedObjects);
        persistedKettleSettings.setLoaded(true);
        architectProject.getKettleSettings().setUUID(kettleSettingsUUID);
        
        List<PersistedSPObject> databases = new ArrayList<PersistedSPObject>();
        for (PersistedSPObject o : persistedObjects) {
            if (o.getParentUUID().equals(architectProject.getRootObject().getUUID()) &&
                    o.getType().equals(SQLDatabase.class.getName())) {
                databases.add(o);
            }
        }
        
        boolean found = false;
        for (PersistedSPObject db : databases) {
            for (PersistedSPOProperty prop : persistedProperties.get(db.getUUID())) {
                if (prop.getPropertyName().equals("playPenDatabase") && 
                        (Boolean) converter.convertToComplexType(prop.getNewValue(), Boolean.class)) {
                    architectProject.getTargetDatabase().setUUID(db.getUUID());
                    db.setLoaded(true);
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
    }

}
