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

import ca.sqlpower.architect.enterprise.ArchitectPersisterSuperConverter;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.testutil.GenericNewValueMaker;
import ca.sqlpower.testutil.NewValueMaker;

public class ColumnProfileResultTest extends PersistedSPObjectTest {

    private ColumnProfileResult profile;
    
    public ColumnProfileResultTest(String name) {
        super(name);
    }
    
    @Override
    public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        NewValueMaker valueMaker = new GenericNewValueMaker(getRootObject());
        SQLColumn column = (SQLColumn) valueMaker.makeNewValue(SQLColumn.class, null, "column for testing");
        SQLTable table = column.getParent();
        ProfileSettings settings = new ProfileSettings();
        TableProfileResult tableProfile = new TableProfileResult(table, settings);
        getRootObject().addChild(tableProfile, 0);
        profile = new ColumnProfileResult(column);
        tableProfile.addColumnProfileResult(profile);
    }

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return ColumnValueCount.class;
    }

    @Override
    public SPObject getSPObjectUnderTest() {
        return profile;
    }
    
    @Override
    public SessionPersisterSuperConverter getConverter() {
        return new ArchitectPersisterSuperConverter(getPLIni(), getRootObject());
    }

}
