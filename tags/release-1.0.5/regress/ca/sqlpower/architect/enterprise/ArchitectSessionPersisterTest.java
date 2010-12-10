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


import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.TestingArchitectSessionContext;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.sqlobject.DatabaseConnectedTestCase;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectRoot;

/**
 * A test suite to ensure the proper functionality of the {@link ArchitectSessionPersister}.
 */
public class ArchitectSessionPersisterTest extends DatabaseConnectedTestCase {

    private ArchitectSessionPersister persister;
    private ArchitectSession session;
    
    protected void setUp() throws Exception {
        super.setUp();
        session = new ArchitectSessionImpl(new TestingArchitectSessionContext(), "test");
        SessionPersisterSuperConverter converter = new ArchitectPersisterSuperConverter(getPLIni(), session.getWorkspace());
        persister = new ArchitectSessionPersister("test", session.getWorkspace(), converter);
        persister.setWorkspaceContainer(session);
    }

    /**
     * Tests adding a SQLDatabase to an ArchitectSession through an
     * {@link SPJSONPersister} hooked up to n {@link ArchitectSessionPersister}
     */
    public void testPersistSQLDatabaseWithJSONPersister() throws Exception {
        SPJSONMessageDecoder decoder = new SPJSONMessageDecoder(persister);
        SPJSONPersister jsonPersister = new SPJSONPersister(new DirectJsonMessageSender(decoder));
        
        jsonPersister.begin();
        jsonPersister.persistObject(session.getRootObject().getUUID(), SQLDatabase.class.getName(), "database", 0);
        jsonPersister.persistProperty("database", "dataSource", DataType.STRING, "regression_test");
        jsonPersister.commit();
        
        SQLObjectRoot root = session.getRootObject();
        SQLObject rootChild = root.getChild(0);
        assertTrue(rootChild instanceof SQLDatabase);
        assertEquals("database", rootChild.getUUID());
        assertEquals("regression_test", rootChild.getName());
    }

    /**
     * Test changing the project name through the {@link SPJSONPersister}
     */
    public void testChangePropertyWithJSONPersister() throws Exception {
        SPJSONMessageDecoder decoder = new SPJSONMessageDecoder(persister);
        SPJSONPersister jsonPersister = new SPJSONPersister(new DirectJsonMessageSender(decoder));
        
        jsonPersister.begin();
        jsonPersister.persistProperty(session.getWorkspace().getUUID(), "name", DataType.STRING, "", "newProjectName");
        jsonPersister.commit();
        
        ArchitectProject project = session.getWorkspace();
        assertEquals("newProjectName", project.getName());
    }
    
    /**
     * Test adding and then removing the database through the {@link SPJSONPersister}
     */
    public void testRemoveObjectWithJSONPersister() throws Exception {
        SPJSONMessageDecoder decoder = new SPJSONMessageDecoder(persister);
        SPJSONPersister jsonPersister = new SPJSONPersister(new DirectJsonMessageSender(decoder));
        
        jsonPersister.begin();
        String rootUUID = session.getRootObject().getUUID();
        jsonPersister.persistObject(rootUUID, SQLDatabase.class.getName(), "database", 0);
        jsonPersister.persistProperty("database", "dataSource", DataType.STRING, "regression_test");
        jsonPersister.commit();
        
        // Ensure database was added
        SQLObjectRoot root = session.getRootObject();
        SQLObject rootChild = root.getChild(0);
        assertTrue(rootChild instanceof SQLDatabase);
        assertEquals("database", rootChild.getUUID());
        
        // Now remove it
        int oldChildCount = root.getChildCount();
        jsonPersister.begin();
        jsonPersister.removeObject(rootUUID, "database");
        jsonPersister.commit();
        assertEquals(oldChildCount - 1, root.getChildCount());
        assertNull(root.getChildByName("regression_test", SQLDatabase.class));
    }
    
    /**
     * Tests adding a SQLDatabase to an ArchitectSession through an
     * {@link SPJSONPersister} hooked up to n {@link ArchitectSessionPersister}
     */
    public void testRollbackWithJSONPersister() throws Exception {
        SPJSONMessageDecoder decoder = new SPJSONMessageDecoder(persister);
        SPJSONPersister jsonPersister = new SPJSONPersister(new DirectJsonMessageSender(decoder));
        
        int oldChildCount = session.getRootObject().getChildCount();
        
        jsonPersister.begin();
        jsonPersister.persistObject(session.getRootObject().getUUID(), SQLDatabase.class.getName(), "database", 0);
        jsonPersister.persistProperty("database", "dataSource", DataType.STRING, "regression_test");
        jsonPersister.rollback();
        
        SQLObjectRoot rootObject = session.getRootObject();
        assertEquals(oldChildCount, rootObject.getChildCount());
        assertNull(rootObject.getChildByName("regression_test", SQLDatabase.class));
    }
}
