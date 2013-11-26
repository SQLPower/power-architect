/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect;

import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.BasicSQLType;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

public class SPObjectSnapshotHierarchyListenerTest extends TestCase {

    private ArchitectClientSideSession session;
    private UserDefinedSQLType systemType1;
    private UserDefinedSQLType systemType2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        ProjectLocation dummyLocation = new ProjectLocation("abc", "test location", 
                new SPServerInfo("stub info", "localhost", 8080, "/no pass", "", ""));
        
        final ArchitectSessionContext context = new ArchitectSessionContextImpl();
        
        final ArchitectClientSideSession systemSession = new ArchitectClientSideSession(context, "system test session", dummyLocation) {
            
            //Overridden to prevent the session from actually trying to connect to the database.
            @Override
            public DataSourceCollection<JDBCDataSource> getDataSources() {
                return new SpecificDataSourceCollection<JDBCDataSource>(new PlDotIni(), JDBCDataSource.class);
            }
            
            //Overridden to prevent the session from actually trying to connect to the database.
            @Override
            protected void verifyServerLicense(ProjectLocation projectLocation) throws AssertionError {
                //do nothing, server is verified for this test.
            }
        };
        final ArchitectSwingProject systemProject = systemSession.getWorkspace();
        systemType1 = new UserDefinedSQLType();
        systemType1.setName("Test system type 1");
        systemType1.setBasicType(BasicSQLType.TEXT);
        systemProject.addChild(systemType1, 0);
        
        systemType2 = new UserDefinedSQLType();
        systemType2.setName("Test system type 2");
        systemType2.setBasicType(BasicSQLType.NUMBER);
        systemProject.addChild(systemType2, 0);
        
        session = new ArchitectClientSideSession(context, "Test session", dummyLocation) {
            
            //Overridden to prevent the session from actually trying to connect to the database.
            @Override
            public DataSourceCollection<JDBCDataSource> getDataSources() {
                return new SpecificDataSourceCollection<JDBCDataSource>(new PlDotIni(), JDBCDataSource.class);
            }
            
            //Overridden to prevent the session from actually trying to connect to the database.
            @Override
            protected void verifyServerLicense(ProjectLocation projectLocation) throws AssertionError {
                //do nothing, server is verified for this test.
            }
            
            @Override
            public ArchitectClientSideSession getSystemSession() {
                return systemSession;
            }
            
            @Override
            public ArchitectSwingProject getSystemWorkspace() {
                return systemSession.getWorkspace();
            }
        };
    }
    
    /**
     * Simple test that adds a column and expects a type to be created for it.
     */
    public void testAddingUDT() throws Exception {
        SQLDatabase db = session.getTargetDatabase();
        SQLTable table = new SQLTable(db, true);
        table.setName("Test table");
        db.addTable(table);
        SQLColumn col = new SQLColumn(table, "test col", Types.VARCHAR, 10, 0);
        col.getUserDefinedSQLType().setUpstreamType(systemType1);
        table.addColumn(col);
        
        assertEquals(1, session.getWorkspace().getSnapshotCollection().getSPObjectSnapshots().size());
        assertEquals(1, session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLType.class).size());
        assertTrue(session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLType.class).get(0) == 
            session.getWorkspace().getSnapshotCollection().getSPObjectSnapshots().get(0).getSPObject());
    }
    
    /**
     * Test that re-adds a UDT and expects only one type to be in the system at the end.
     */
    public void testReaddingUDT() throws Exception {
        SQLDatabase db = session.getTargetDatabase();
        SQLTable table = new SQLTable(db, true);
        table.setName("Test table");
        db.addTable(table);
        SQLColumn col = new SQLColumn(table, "test col", Types.VARCHAR, 10, 0);
        col.getUserDefinedSQLType().setUpstreamType(systemType1);
        table.addColumn(col);
        
        assertEquals(1, session.getWorkspace().getSnapshotCollection().getSPObjectSnapshots().size());
        assertEquals(1, session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLType.class).size());
        UserDefinedSQLType snapshotUDT = session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLType.class).get(0);
        SPObjectSnapshot<?> snapshot = session.getWorkspace().getSnapshotCollection().getSPObjectSnapshots().get(0);
        assertTrue(snapshotUDT == snapshot.getSPObject());
        assertEquals(systemType1.getUUID(), snapshot.getOriginalUUID());
        assertEquals(systemType1.getName(), snapshotUDT.getName());
        
        col.getUserDefinedSQLType().setUpstreamType(systemType2);
        
        assertEquals(1, session.getWorkspace().getSnapshotCollection().getSPObjectSnapshots().size());
        assertEquals(1, session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLType.class).size());
        snapshotUDT = session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLType.class).get(0);
        snapshot = session.getWorkspace().getSnapshotCollection().getSPObjectSnapshots().get(0);
        assertTrue(snapshotUDT == snapshot.getSPObject());
        
        assertEquals(systemType2.getUUID(), snapshot.getOriginalUUID());
        assertEquals(systemType2.getName(), snapshotUDT.getName());
    }
    
}
