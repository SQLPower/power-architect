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

import org.json.JSONArray;
import org.json.JSONObject;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContextImpl;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.SPPersistenceException;
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
        session = new ArchitectSessionImpl(new ArchitectSessionContextImpl(), "test");
        SessionPersisterSuperConverter converter = new ArchitectPersisterSuperConverter(getPLIni(), session.getWorkspace());
        persister = new ArchitectSessionPersister("test", session.getWorkspace(), converter);
        persister.setSession(session);
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
     * Sends JSON messages directly to the JSON decoder
     */
    private class DirectJsonMessageSender implements MessageSender<JSONObject> {

        private final SPJSONMessageDecoder decoder;

        private JSONArray array;

        public DirectJsonMessageSender(SPJSONMessageDecoder decoder) {
            this.decoder = decoder;
            this.array = new JSONArray();
        }

        public void clear() {
            // messages get sent directly so no need to 'clear'
        }

        public void flush() throws SPPersistenceException {
            decoder.decode(array.toString());
            array = new JSONArray();
            // I wish JSONArray had a clear, but since this is just a test
            // class, I'm not that concerned of any performance hit from
            // creating a new object at this point.
        }

        public void send(JSONObject content) throws SPPersistenceException {
            array.put(content);
        }

    }
}
