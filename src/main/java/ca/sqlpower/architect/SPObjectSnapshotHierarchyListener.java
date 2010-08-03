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
import ca.sqlpower.architect.enterprise.DomainCategorySnapshot;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.sqlobject.SPObjectSnapshotUpdateListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.UserDefinedSQLTypeSnapshot;
import ca.sqlpower.util.SQLPowerUtils;

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
            if (sqlColumn.getUserDefinedSQLType() != null) {
                sqlColumn.getUserDefinedSQLType().addSPListener(this);
                createSPObjectSnapshot(sqlColumn.getUserDefinedSQLType(), sqlColumn.getUserDefinedSQLType().getUpstreamType());
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
				cleanupSnapshot();
			}
		} else if (e.getChild() instanceof SQLColumn) {
			((SQLColumn) e.getChild()).getUserDefinedSQLType().removeSPListener(this);
			cleanupSnapshot();
		}
	}
	
	@Override
	public void propertyChanged(PropertyChangeEvent e) {
		if (e.getSource() instanceof UserDefinedSQLType 
				&& e.getPropertyName().equals("upstreamType")) {
		    
		    if (e.getNewValue() instanceof UserDefinedSQLType) {
		        createSPObjectSnapshot((UserDefinedSQLType) e.getSource(), (UserDefinedSQLType) e.getNewValue());
		    }
		    
		    cleanupSnapshot();
		    
			UserDefinedSQLType columnProxyType = (UserDefinedSQLType) e.getSource();
			addUpdateListener(columnProxyType);
		}
	}

    /**
     * This method will remove the snapshot and the listener on the system's
     * type if the snapshot is no longer in use.
     */
	private void cleanupSnapshot() {
	    //find if the snapshot is being referenced by any of the columns in the workspace.
	    //if it is not referenced remove it from the project
	    //find its upstream type and remove its listener.
	    //XXX Need to remove the old listener
	}

    /**
     * Adds a listener to the correct system type based on the given type. The
     * listener on the system type will be used to update the snapshot in this
     * project when the system type changes.
     * 
     * @param columnProxyType
     *            This type is a type that is a direct child of a column. The
     *            type will have its upstreamType property pointing at a
     *            snapshot in the workspace that we can find to locate its
     *            actual system type.
     */
    private void addUpdateListener(UserDefinedSQLType columnProxyType) {
        UserDefinedSQLType upstreamSnapshotType = columnProxyType.getUpstreamType();

        SPObjectSnapshot<?> snapshot = null;
        for (SPObjectSnapshot<?> workspaceSnapshot : session.getWorkspace().getSPObjectSnapshots()) {
            if (workspaceSnapshot.getSPObject() == upstreamSnapshotType) {
                snapshot = workspaceSnapshot;
                break;
            }
        }
        
        UserDefinedSQLType systemType = session.findSystemTypeFromSnapshot(snapshot);
        SQLPowerUtils.listenToHierarchy(systemType, new SPObjectSnapshotUpdateListener(snapshot));
        if (systemType.getParent() instanceof DomainCategory) {
            DomainCategory category = (DomainCategory) systemType.getParent();
            for (SPObjectSnapshot<?> categorySnapshot : session.getWorkspace().getSPObjectSnapshots()) {
                if (categorySnapshot.getOriginalUUID().equals(category.getUUID())) {
                    category.addSPListener(new SPObjectSnapshotUpdateListener(categorySnapshot));
                    break;
                }
            }
        }
    }

    private void createSPObjectSnapshot(UserDefinedSQLType typeProxy, UserDefinedSQLType upstreamType) {
        SPObject upstreamTypeParent = upstreamType.getParent();

        if (upstreamTypeParent != null && !upstreamTypeParent.equals(session.getWorkspace()) &&
                !(upstreamTypeParent instanceof DomainCategory && 
                    upstreamTypeParent.getParent().equals(session.getWorkspace()))) {
            int systemRevision =  session.getSystemSession().getCurrentRevisionNumber();
       
            boolean isDomainSnapshot = upstreamType.getParent() instanceof DomainCategory;
            UserDefinedSQLTypeSnapshot snapshot;
            if (upstreamType.getUpstreamType() != null) {
                //For domains
                UserDefinedSQLType upUpStreamType = upstreamType.getUpstreamType();
                boolean isUpstreamDomainSnapshot = upUpStreamType.getParent() instanceof DomainCategory;
                UserDefinedSQLTypeSnapshot upstreamSnapshot = new UserDefinedSQLTypeSnapshot(upUpStreamType, systemRevision, isUpstreamDomainSnapshot);
                session.getWorkspace().addChild(upstreamSnapshot, 0);
                session.getWorkspace().addChild(upstreamSnapshot.getSPObject(), 0);
                snapshot = new UserDefinedSQLTypeSnapshot(upstreamType, systemRevision, isDomainSnapshot, upstreamSnapshot);
            } else {
                snapshot = new UserDefinedSQLTypeSnapshot(upstreamType, systemRevision, isDomainSnapshot);
            }
            session.getWorkspace().addChild(snapshot, 0);
            if ((upstreamType.getParent() instanceof DomainCategory)) {
                DomainCategory parent = (DomainCategory) upstreamType.getParent();
                DomainCategorySnapshot domainSnapshot = 
                    new DomainCategorySnapshot(parent, systemRevision);
                session.getWorkspace().addChild(domainSnapshot, 0);
                session.getWorkspace().addChild(domainSnapshot.getSPObject(), 0);
                domainSnapshot.getSPObject().addChild(snapshot.getSPObject(), 0);
            } else {
                session.getWorkspace().addChild(snapshot.getSPObject(), 0);
            }
            typeProxy.setUpstreamType(snapshot.getSPObject());
        }
    }
}
