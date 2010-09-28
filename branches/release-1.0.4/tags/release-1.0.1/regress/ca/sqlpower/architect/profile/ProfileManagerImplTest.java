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

package ca.sqlpower.architect.profile;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.StubArchitectSession;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

public class ProfileManagerImplTest extends PersistedSPObjectTest {
    
    private ProfileManagerImpl profileManager;

    public ProfileManagerImplTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    
        profileManager = new ProfileManagerImpl();
        final ArchitectProject project = (ArchitectProject) new ArchitectNewValueMaker(
                getRootObject(), getPLIni()).makeNewValue(ArchitectProject.class, null, "");
        project.setSession(new StubArchitectSession() {
            @Override
            public ArchitectProject getWorkspace() {
                return project;
            }
        });
        project.setProfileManager(profileManager);
        
        getRootObject().addChild(project, 0);
    }
    
    @Override
    public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return TableProfileResult.class;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return profileManager;
    }

}
