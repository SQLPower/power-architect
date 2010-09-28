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

package ca.sqlpower.architect.util;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileSettings;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.testutil.GenericNewValueMaker;
import ca.sqlpower.util.SPSession;

public class ArchitectNewValueMaker extends GenericNewValueMaker {

    public ArchitectNewValueMaker(SPObject root) {
        super(root);
    }
    
    public ArchitectNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        super(root, dsCollection);
    }
    
    @Override
    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        if (valueType == ProfileSettings.class) {
            ProfileSettings settings = new ProfileSettings();
            getRootObject().addChild(settings, 0);
            return settings;
        } else if (valueType == TableProfileResult.class) {
            TableProfileResult tpr = new TableProfileResult(
                    (SQLTable) makeNewValue(SQLTable.class, null, ""), 
                    (ProfileSettings) makeNewValue(ProfileSettings.class, null, ""));
            getRootObject().addChild(tpr, 0);
            return tpr;
        } else if (valueType == ColumnProfileResult.class) {
            TableProfileResult tpr = (TableProfileResult) makeNewValue(TableProfileResult.class, null, "");
            ColumnProfileResult cpr = new ColumnProfileResult(
                    (SQLColumn) makeNewValue(SQLColumn.class, null, ""), tpr);
            cpr.setParent(tpr);
            return cpr;
        } else if (valueType == ColumnValueCount.class) {
            ColumnValueCount cvc = new ColumnValueCount(Integer.MAX_VALUE, 2, 42);
            getRootObject().addChild(cvc, 0);
            return cvc;
        } else if (ArchitectProject.class.isAssignableFrom(valueType)) {
            ArchitectProject project;
            final SPObject rootObject = getRootObject();
            try {
                project = new ArchitectProject() {
                    @Override
                    public SPSession getSession() {
                        return rootObject.getSession();
                    }
                };
            } catch (SQLObjectException e) {
                throw new RuntimeException(e);
            }
            getRootObject().addChild(project, 0);
            return project;
        } else {
            return super.makeNewValue(valueType, oldVal, propName);
        }
    }

}
