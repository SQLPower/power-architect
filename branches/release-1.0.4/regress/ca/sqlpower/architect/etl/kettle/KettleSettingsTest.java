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

package ca.sqlpower.architect.etl.kettle;

import ca.sqlpower.architect.StubArchitectSession;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

public class KettleSettingsTest extends PersistedSPObjectTest {

    KettleSettings objectUnderTest;
    
    public KettleSettingsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final ArchitectSwingProject project = (ArchitectSwingProject) new ArchitectNewValueMaker(
                getRootObject(), getPLIni()).makeNewValue(ArchitectSwingProject.class, null, "");
        project.setSession(new StubArchitectSession() {
            @Override
            public ArchitectSwingProject getWorkspace() {
                return project;
            }
        });
        objectUnderTest = project.getKettleSettings();
        getRootObject().addChild(project, 0);
    }
    
    @Override
    public void testPersisterCreatesNewObjects() throws Exception {
        // The KettleSettings object is a final field of the ArchitectProject, and as such, cannot
        // be created by persist calls. It must instead be updated. 
        // See "ArchitectSessionPersisterTest.testRefreshRootNodeWithJSONPersister()"
    }
    
    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return KettleSettings.class;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return objectUnderTest;
    }
    
    @Override
    public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }

}
