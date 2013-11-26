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

package ca.sqlpower.architect.olap;

import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

public class OLAPSessionTest extends PersistedSPObjectTest {
    
    private OLAPSession session;

    public OLAPSessionTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        session = new OLAPSession(new Schema());
        OLAPRootObject olapRoot = new OLAPRootObject();
        olapRoot.addChild(session);
        getRootObject().addChild(olapRoot, 0);
    }

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return null;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return session;
    }
    
    @Override
    public NewValueMaker createNewValueMaker(SPObject root, 
            DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }

}
