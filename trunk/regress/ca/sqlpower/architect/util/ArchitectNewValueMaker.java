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
import ca.sqlpower.architect.ProjectSettings;
import ca.sqlpower.architect.ProjectSettings.ColumnVisibility;
import ca.sqlpower.architect.etl.kettle.KettleSettings;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileSettings;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.testutil.GenericNewValueMaker;

public class ArchitectNewValueMaker extends GenericNewValueMaker {      

    private final ArchitectProject valueMakerProject;
    
    public ArchitectNewValueMaker(SPObject root) {
        this(root, new PlDotIni());
    }
    
    public ArchitectNewValueMaker(final SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        super(root, dsCollection);
        valueMakerProject = (ArchitectProject) makeNewValue(ArchitectProject.class, null, null);
        valueMakerProject.setPlayPenContentPane(new PlayPenContentPane());
    }    
    
    @Override
    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        if (valueType == ProfileSettings.class) {
            ProfileSettings settings = new ProfileSettings();
            getRootObject().addChild(settings, 0);
            return settings;
        } else if (valueType == ProjectSettings.class) {
            ProjectSettings settings = new ProjectSettings();
            root.addChild(settings, 0);
            return settings;
        } else if (valueType == KettleSettings.class) {
            return ((ArchitectProject) makeNewValue(ArchitectProject.class, null, null)).getKettleSettings();
        } else if (valueType == ColumnVisibility.class) { 
            if (oldVal != ColumnVisibility.ALL) {
                return ColumnVisibility.ALL;
            } else {
                return ColumnVisibility.PK;
            }
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
        } else if (valueType == ArchitectProject.class) {
            ArchitectProject project;
            try {
                project = new ArchitectProject();
            } catch (SQLObjectException e) {
                throw new RuntimeException(e);
            }
            getRootObject().addChild(project, 0);
            return project;
        } else if (valueType == PlayPenContentPane.class) {
            PlayPenContentPane pane = new PlayPenContentPane();
            ((ArchitectProject) makeNewValue(ArchitectProject.class, null, null)).setPlayPenContentPane(pane);
            return pane;
        } else if (valueType == PlayPenComponent.class) {            
            return makeNewValue(TablePane.class, null, null);
        } else if (valueType == TablePane.class) {
            PlayPenContentPane p = valueMakerProject.getPlayPenContentPane();
            TablePane tp = new TablePane((SQLTable) super.makeNewValue(SQLTable.class, null, null), p);            
            p.addChild(tp, p.getChildren(TablePane.class).size());
            return tp;
        } else if (valueType == OLAPSession.class) {
            OLAPSession session = new OLAPSession(new Schema());
            OLAPRootObject root = new OLAPRootObject();
            root.addChild(session);
            getRootObject().addChild(root, 0);
            return session;
        } else if (valueType == OLAPRootObject.class) {
            return ((ArchitectProject) makeNewValue(ArchitectProject.class, null, null)).getOlapRootObject();
        } else {
            return super.makeNewValue(valueType, oldVal, propName);
        }
    }

}
