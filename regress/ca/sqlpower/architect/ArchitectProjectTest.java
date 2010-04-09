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

import ca.sqlpower.architect.enterprise.ArchitectPersisterSuperConverter;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;

public class ArchitectProjectTest extends PersistedSPObjectTest {
    
    private ArchitectProject objectUnderTest;

    public ArchitectProjectTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ArchitectSession session = new StubArchitectSession();
        objectUnderTest = new ArchitectProject();
        objectUnderTest.setSession(session);
        getRootObject().addChild(objectUnderTest, 0);
    }
    
    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return null;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return objectUnderTest;
    }
    
    @Override
    public SessionPersisterSuperConverter getConverter() {
        return new ArchitectPersisterSuperConverter(getPLIni(), getRootObject());
    }
    
    /**
     * Tests the architect project's ability to add children subclassing the things in its allowedChildTypes.
     * Fails if childPositionOffset throws an IllegalArgumentException.
     */
    public void testChildPositionOffset() throws Exception {
       objectUnderTest.childPositionOffset(ProfileManagerImpl.class);
    }

    @Override
    public void testPersisterCreatesNewObjects() throws Exception {
        //The ArchitectProject is the root of the SPObject tree. This
        //object cannot be made straight from persist calls but must exist
        //as part of the session to start.
        // See "ArchitectSessionPersisterTest.testRefreshRootNodeWithJSONPersister()"
    }
    
    @Override
    public void testSPListenerPersistsNewObjects() throws Exception {
        //The ArchitectProject is the root of the SPObject tree. This
        //object cannot be made straight from persist calls but must exist
        //as part of the session to start.
        // See "ArchitectSessionPersisterTest.testRefreshRootNodeWithJSONPersister()"
    }

}
