/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Library.
 *
 * SQL Power Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect;

import java.beans.PropertyChangeEvent;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.sqlobject.SPObjectSnapshotUpdateListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * Add this listener to a SQLDatabase to have its columns have correct snapshot listeners
 * attached to system types.
 */
public class SPObjectSnapshotHierarchyListener extends AbstractSPListener {

    /**
     * This project holds all of the snapshots of the {@link UserDefinedSQLType}
     * objects.
     */
    private final ArchitectClientSideSession session;

    public SPObjectSnapshotHierarchyListener(ArchitectClientSideSession session) {
        this.session = session;
    }

	@Override
	public void childAdded(SPChildEvent e) {
		if (e.getChild() instanceof SQLTable) {
			e.getChild().addSPListener(this);
		} else if (e.getChild() instanceof SQLColumn) {
			SQLColumn sqlColumn = (SQLColumn) e.getChild();
            sqlColumn.getUserDefinedSQLType().addSPListener(this);
            if (sqlColumn.getUserDefinedSQLType() != null) {
                addUpdateListener(sqlColumn.getUserDefinedSQLType());
            }
		}
	}
	
	@Override
	public void childRemoved(SPChildEvent e) {
		if (e.getChild() instanceof SQLTable) {
			e.getChild().removeSPListener(this);
			for (SQLColumn col : e.getChild().getChildren(SQLColumn.class)) {
				col.getUserDefinedSQLType().removeSPListener(this);
			}
		} else if (e.getChild() instanceof SQLColumn) {
			((SQLColumn) e.getChild()).getUserDefinedSQLType().removeSPListener(this);
		}
	}
	
	@Override
	public void propertyChanged(PropertyChangeEvent e) {
		if (e.getSource() instanceof UserDefinedSQLType 
				&& e.getPropertyName().equals("upstreamType")) {
		    //XXX Need to remove the old listener
			UserDefinedSQLType columnProxyType = (UserDefinedSQLType) e.getSource();
			addUpdateListener(columnProxyType);
		}
	}

    private void addUpdateListener(UserDefinedSQLType columnProxyType) {
        UserDefinedSQLType upstreamSnapshotType = columnProxyType.getUpstreamType();
        for (SPObjectSnapshot<?> snapshot : session.getWorkspace().getSqlTypeSnapshots()) {
            if (snapshot.getSPObject() == upstreamSnapshotType) {
                for (UserDefinedSQLType systemType : session.getSystemWorkspace().getSqlTypes()) {
                    if (systemType.getUUID().equals(snapshot.getOriginalUUID())) {
                        systemType.addSPListener(new SPObjectSnapshotUpdateListener(snapshot));
                        break;
                    }
                }
                for (DomainCategory category : session.getSystemWorkspace().getDomainCategories()) {
                    boolean typeFound = false;
                    for (UserDefinedSQLType systemType : category.getChildren(UserDefinedSQLType.class)) {
                        if (systemType.getUUID().equals(upstreamSnapshotType.getUUID())) {
                            systemType.addSPListener(new SPObjectSnapshotUpdateListener(snapshot));
                            typeFound = true;
                            break;
                        }
                    }
                    if (typeFound) break;
                }
                break;
            }
        }
    }
}
