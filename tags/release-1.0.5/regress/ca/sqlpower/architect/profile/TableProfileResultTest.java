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

import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

public class TableProfileResultTest extends PersistedSPObjectTest {

    private TableProfileResult tpr;

    public TableProfileResultTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        tpr = (TableProfileResult) new ArchitectNewValueMaker(
                getRootObject(), getPLIni()).makeNewValue(TableProfileResult.class, null, "");
    }

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return ColumnProfileResult.class;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return tpr;
    }
    
    @Override
    public NewValueMaker createNewValueMaker(SPObject root, 
            DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }

    /**
     * Regression test to ensure profiling a table does not null out the
     * profile's parent pointer.
     */
//    public void testParentSetCorrectly() throws Exception {
//        sqlx("Create table testProfileParent (col1 varchar(50), col2 varchar(50))");
//        SQLTable testTable = db.getTableByName("testProfileParent");
//        testTable.populate();
//        assertNotNull(testTable);
//        assertEquals(2, testTable.getColumns().size());
//        
//        ArchitectSession session = new ArchitectSessionImpl(new ArchitectSessionContextImpl(), "testing session");
//        ProfileManager pm = new ProfileManagerImpl();
//        session.getWorkspace().setProfileManager(pm);
//        session.getWorkspace().getRootObject().addDatabase(db, 0);
//        TableProfileResult tpr = pm.createProfile(testTable);
//        
//        assertEquals(pm, tpr.getParent());
//        assertTrue(tpr.getCreateStartTime() >= 0);
//        assertTrue(tpr.getCreateEndTime() >= 1);
//    }

}
