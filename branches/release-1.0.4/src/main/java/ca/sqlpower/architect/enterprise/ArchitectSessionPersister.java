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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.dao.PersistedSPOProperty;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.helper.AbstractSPPersisterHelper;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLDatabase;

/**
 * An architect specific persister.
 */
public class ArchitectSessionPersister extends SPSessionPersister {

    Logger logger = Logger.getLogger(ArchitectSessionPersister.class);
    
    public ArchitectSessionPersister(String name, SPObject root, SessionPersisterSuperConverter converter) {
        super(name, root, converter);
    }

    @Override
    protected void refreshRootNode(PersistedSPObject pso) {
        root.setUUID(pso.getUUID());
        String rootObjectUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "rootObject", persistedProperties);
        
        PersistedSPObject persistedRootObject = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), rootObjectUUID, persistedObjects);
        ArchitectSwingProject architectProject = (ArchitectSwingProject) root;
        architectProject.getRootObject().setUUID(rootObjectUUID);
        persistedRootObject.setLoaded(true);
        
        String profileManagerUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "profileManager", persistedProperties);
        
        //Null for system projects.
        if (profileManagerUUID != null) {
            PersistedSPObject persistedProfileManager = AbstractSPPersisterHelper.findPersistedSPObject(
                    pso.getUUID(), profileManagerUUID, persistedObjects);
            architectProject.getProfileManager().setUUID(profileManagerUUID);
            persistedProfileManager.setLoaded(true);
        }
        
        String olapRootObjectUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "olapRootObject", persistedProperties);
        
        PersistedSPObject persistedOlapRootObject = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), olapRootObjectUUID, persistedObjects);
        architectProject.getOlapRootObject().setUUID(olapRootObjectUUID);
        persistedOlapRootObject.setLoaded(true);
        
        String kettleSettingsUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "kettleSettings", persistedProperties);
        
        PersistedSPObject persistedKettleSettings = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), kettleSettingsUUID, persistedObjects);
        persistedKettleSettings.setLoaded(true);
        architectProject.getKettleSettings().setUUID(kettleSettingsUUID);
        
        String criticManagerUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "criticManager", persistedProperties);
        
        PersistedSPObject criticManager = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), criticManagerUUID, persistedObjects);
        architectProject.getCriticManager().setUUID(criticManagerUUID);
        criticManager.setLoaded(true);
        
        architectProject.getCriticManager().clear();
        
        String snapshotCollectionUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "snapshotCollection", persistedProperties);
        
        PersistedSPObject snapshotCollectionSettings = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), snapshotCollectionUUID, persistedObjects);
        snapshotCollectionSettings.setLoaded(true);
        architectProject.getSnapshotCollection().setUUID(snapshotCollectionUUID);
        
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
