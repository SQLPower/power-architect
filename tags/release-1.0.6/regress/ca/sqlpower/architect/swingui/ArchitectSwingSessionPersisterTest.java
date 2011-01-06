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

import ca.sqlpower.architect.SnapshotCollection;
import ca.sqlpower.architect.ddl.critic.CriticManager;
import ca.sqlpower.architect.enterprise.ArchitectPersisterSuperConverter;
import ca.sqlpower.architect.enterprise.ArchitectSessionPersister;
import ca.sqlpower.architect.enterprise.DirectJsonMessageSender;
import ca.sqlpower.architect.etl.kettle.KettleSettings;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.sqlobject.DatabaseConnectedTestCase;
import ca.sqlpower.sqlobject.SQLObjectRoot;

public class ArchitectSwingSessionPersisterTest extends DatabaseConnectedTestCase {
    
    private ArchitectSessionPersister persister;
    private ArchitectSwingSession session;
    
    protected void setUp() throws Exception {
        super.setUp();
        session = new ArchitectSwingSessionImpl(new TestingArchitectSwingSessionContext(), "test");
        SessionPersisterSuperConverter converter = new ArchitectPersisterSuperConverter(getPLIni(), session.getWorkspace());
        persister = new ArchitectSessionPersister("test", session.getWorkspace(), converter);
        persister.setWorkspaceContainer(session);
    }
    
    /**
     * The purpose of this test is to ensure that the root node (ArchitectProject) is 
     * correctly updated by persist calls: That it contains the correct SPObjectRoot, 
     * OlapObjectRoot, and KettleSettings.
     */
    public void testRefreshRootNodeWithJSONPersister() throws Exception {
        SPJSONMessageDecoder decoder = new SPJSONMessageDecoder(persister);
        SPJSONPersister jsonPersister = new SPJSONPersister(new DirectJsonMessageSender(decoder));
        
        session.getUndoManager().setLoading(true);
        
        jsonPersister.begin();
        jsonPersister.persistObject(null, ArchitectSwingProject.class.getName(), "ArchitectProjectUUID", 0);
        jsonPersister.persistObject("ArchitectProjectUUID", KettleSettings.class.getName(), "KettleSettingsUUID", 0);
        jsonPersister.persistObject("ArchitectProjectUUID", SQLObjectRoot.class.getName(), "SQLObjectRootUUID", 0);
        jsonPersister.persistObject("ArchitectProjectUUID", OLAPRootObject.class.getName(), "OLAPRootObjectUUID", 0);
        jsonPersister.persistObject("ArchitectProjectUUID", CriticManager.class.getName(), "CriticManagerUUID", 0);
        jsonPersister.persistObject("ArchitectProjectUUID", SnapshotCollection.class.getName(), "SnapshotCollectionUUID", 0);
        jsonPersister.persistProperty("ArchitectProjectUUID", "rootObject", DataType.STRING, "SQLObjectRootUUID");
        jsonPersister.persistProperty("ArchitectProjectUUID", "olapRootObject", DataType.STRING, "OLAPRootObjectUUID");
        jsonPersister.persistProperty("ArchitectProjectUUID", "kettleSettings", DataType.STRING, "KettleSettingsUUID");
        jsonPersister.persistProperty("ArchitectProjectUUID", "criticManager", DataType.STRING, "CriticManagerUUID");
        jsonPersister.persistProperty("ArchitectProjectUUID", "snapshotCollection", DataType.STRING, "SnapshotCollectionUUID");
        jsonPersister.commit();
        
        assertEquals("KettleSettingsUUID", session.getWorkspace().getKettleSettings().getUUID());
        assertEquals("SQLObjectRootUUID", session.getWorkspace().getRootObject().getUUID());
        assertEquals("OLAPRootObjectUUID", session.getWorkspace().getOlapRootObject().getUUID());
        assertEquals("CriticManagerUUID", session.getWorkspace().getCriticManager().getUUID());
        assertEquals("SnapshotCollectionUUID", session.getWorkspace().getSnapshotCollection().getUUID());

        session.getUndoManager().setLoading(false);
    }

}
