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

package ca.sqlpower.architect.swingui.olap;

import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

/**
 * A abstract test class that can be used to help test any of the
 * OLAP swing ui components. It sets up a project for containing
 * the OLAP model, and another project containing the OLAP content pane.
 */
public abstract class TestOLAPComponent extends PersistedSPObjectTest {  
    
    protected PlayPenContentPane contentPane;
    protected Schema schema;
    protected PlayPenComponent olapComponent;
    
    public TestOLAPComponent(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        ArchitectSwingProject project = new ArchitectSwingProject();
        getRootObject().addChild(project, 0);
        OLAPSession session = new OLAPSession(new Schema());
        project.getOlapRootObject().addChild(session);
        schema = session.getSchema();
        contentPane = new PlayPenContentPane(session);
        project.addOLAPContentPane(contentPane);   
    }
    
    @Override
    public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }
    
    @Override
    public SPObject getSPObjectUnderTest() {
        return olapComponent;
    }

}
