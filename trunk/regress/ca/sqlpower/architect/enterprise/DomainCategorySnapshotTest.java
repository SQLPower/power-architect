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

package ca.sqlpower.architect.enterprise;

import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

public class DomainCategorySnapshotTest extends PersistedSPObjectTest {
    public DomainCategorySnapshotTest(String name) throws Exception {
        super(name);
    }

    private DomainCategory category;
    private DomainCategorySnapshot categorySnapshot;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        category = new DomainCategory("test");
        categorySnapshot = new DomainCategorySnapshot(category);
        
        getRootObject().addChild(category, 0);
        getRootObject().addChild(categorySnapshot, 0);
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return categorySnapshot;
    }

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return DomainCategory.class;
    }
    
    public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }
}
