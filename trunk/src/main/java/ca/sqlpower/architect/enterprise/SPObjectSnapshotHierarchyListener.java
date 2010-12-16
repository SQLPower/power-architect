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

package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SnapshotCollection;
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
import ca.sqlpower.util.TransactionEvent;

/**
 * Add this listener to a SQLDatabase to have its columns have correct snapshot listeners
 * attached to system types.
 */
public class SPObjectSnapshotHierarchyListener extends AbstractSPListener {

    private static final Logger logger = Logger.getLogger(SPObjectSnapshotHierarchyListener.class);
    
    /**
     * This project holds all of the snapshots of the {@link UserDefinedSQLType}
     * objects.
     */
    private final ArchitectClientSideSession session;

    /**
     * How many levels of transactions the object
     * being watched is in. Currently only being used
     * if the TransactionEvent source is a UserDefinedSQLType
     */
    private int transactionCount = 0;
    
    /**
     * A reference to a property change event kept during
     * a transaction to change a UserDefinedSQLType's upstream
     * type so that the event details can be used to create a
     * new snapshot.
     */
    private Map<UserDefinedSQLType, PropertyChangeEvent> upstreamTypeChangeEventMap = new HashMap<UserDefinedSQLType, PropertyChangeEvent>();

    /**
     * Each type that needs to be cleaned up is mapped to a count of how many
     * times the clean up method should be called on it.
     * <p>
     * XXX This is a stop-gap for being able to move a column as a remove and
     * add since the column will be pointing at the snapshot and not a system
     * type. In the future we need to have the snapshot object override
     * {@link UserDefinedSQLType} so it is the same object. That change will 
     * also simplify the model.
     */
    private final Map<UserDefinedSQLType, Integer> typesToCleanup = 
        new HashMap<UserDefinedSQLType, Integer>();
    
    /**
     * True if this listener is in the middle of 
     * setting a snapshot. False otherwise. Used
     * to prevent an 'echo' event from causing
     * extra commit() calls when setting the upstream
     * type to use a snapshot.
     */
    private boolean settingSnapshot = false;
    
    /**
     * This map tracks all of the listeners that have been attached to system
     * workspace types and domains to the snapshot that they were attached for.
     * This lets us remove these listeners when the type or domain is being
     * removed from the system.
     */
    private final Map<SPObjectSnapshot<?>, SPObjectSnapshotUpdateListener> listenerMap = 
        new HashMap<SPObjectSnapshot<?>, SPObjectSnapshotUpdateListener>();

    public SPObjectSnapshotHierarchyListener(ArchitectClientSideSession session) {
        this.session = session;
    }

	@Override
    public void childAdded(SPChildEvent e) {
        if (e.getChild() instanceof SQLTable) {
            SQLTable table = (SQLTable) e.getChild();
            table.addSPListener(this);
            for (SQLColumn sqlColumn : table.getChildren(SQLColumn.class)) {
                UserDefinedSQLType upstreamType = sqlColumn.getUserDefinedSQLType().getUpstreamType();
                if (upstreamType != null) {
                    reassignType(sqlColumn);
                }
                sqlColumn.getUserDefinedSQLType().addSPListener(this);
            }
        } else if (e.getChild() instanceof SQLColumn) {
            SQLColumn sqlColumn = (SQLColumn) e.getChild();
            UserDefinedSQLType upstreamType = sqlColumn.getUserDefinedSQLType().getUpstreamType();
            if (session.getWorkspace().getSnapshotCollection().isMagicEnabled() && upstreamType != null) {
                
                // check if the upstream type is exactly an existing snapshot
                List<UserDefinedSQLTypeSnapshot> udtSnapshots = 
                    session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLTypeSnapshot.class);
                boolean isSnapshot = false;
                for (UserDefinedSQLTypeSnapshot snapshot: udtSnapshots) {
                    if (upstreamType.equals(snapshot.getSPObject())) {
                        isSnapshot = true;
                        snapshot.setSnapshotUseCount(snapshot.getSnapshotUseCount() + 1);
                        if (listenerMap.get(snapshot) == null) {
                            addUpdateListener(upstreamType);
                        }
                        break;
                    }
                }
                
                // Check if the type refers to a snapshot in another server project, or is from a local project (null parent)
                if (!isSnapshot && (upstreamType.getParent() == null || 
                        (upstreamType.getParent() instanceof SnapshotCollection &&
                                !upstreamType.getParent().equals(session.getWorkspace().getSnapshotCollection())) ||
                        (upstreamType.getParent() instanceof DomainCategory &&
                         upstreamType.getParent().getParent() instanceof SnapshotCollection &&
                        !upstreamType.getParent().getParent().equals(session.getWorkspace().getSnapshotCollection())))) {
                    reassignType(sqlColumn);
                    isSnapshot = true;
                }
                
                // If it's not a snapshot, then set the type to a snapshot
                if (!isSnapshot) {
                    UserDefinedSQLType columnProxyType = sqlColumn.getUserDefinedSQLType();
                    createSPObjectSnapshot(columnProxyType, upstreamType, 
                            session.getWorkspace().getSnapshotCollection(),
                            this);
                    addUpdateListener(columnProxyType.getUpstreamType());
                }
            }
            sqlColumn.getUserDefinedSQLType().addSPListener(this);
        }
    }
	
	@Override
	public void childRemoved(SPChildEvent e) {
		if (e.getChild() instanceof SQLTable) {
			e.getChild().removeSPListener(this);
			for (SQLColumn col : e.getChild().getChildren(SQLColumn.class)) {
				col.getUserDefinedSQLType().removeSPListener(this);
				if (session.getWorkspace().getSnapshotCollection().isMagicEnabled() &&
				        col.getUserDefinedSQLType().isMagicEnabled()) {
				    UserDefinedSQLType snapshotType = col.getUserDefinedSQLType().getUpstreamType();
				    Integer cleanupCount = typesToCleanup.get(snapshotType);
				    if (cleanupCount == null) {
				        cleanupCount = 0;
				    }
				    typesToCleanup.put(snapshotType, cleanupCount + 1);
				}
			}
		} else if (e.getChild() instanceof SQLColumn) {
		    UserDefinedSQLType colType = ((SQLColumn) e.getChild()).getUserDefinedSQLType();
			colType.removeSPListener(this);
			if (session.getWorkspace().getSnapshotCollection().isMagicEnabled() &&
			        colType.isMagicEnabled()) {
                UserDefinedSQLType snapshotType = colType.getUpstreamType();
                Integer cleanupCount = typesToCleanup.get(snapshotType);
                if (cleanupCount == null) {
                    cleanupCount = 0;
                }
                typesToCleanup.put(snapshotType, cleanupCount + 1);
			}
		}
	}
	
	@Override
	public void propertyChanged(PropertyChangeEvent e) {
		if (e.getSource() instanceof UserDefinedSQLType 
				&& e.getPropertyName().equals("upstreamType")) {
		    Object oldValue;
		    if (upstreamTypeChangeEventMap.containsKey(e.getSource())) {
		        oldValue = upstreamTypeChangeEventMap.get(e.getSource()).getOldValue();
		    } else {
		        oldValue = e.getOldValue();
		    }
		    upstreamTypeChangeEventMap.put((UserDefinedSQLType) e.getSource(), new PropertyChangeEvent(e.getSource(), e.getPropertyName(), oldValue, e.getNewValue()));
		}
	}

    /**
     * This method will remove the snapshot and the listener on the system's
     * type if the snapshot is no longer in use. Only call this method if magic
     * is enabled.
     * 
     * @param typeRemoved
     *            The snapshot type that is a child of the workspace that is/was
     *            referenced by the column's type proxy that is being removed.
     *            Either the column is being removed or the type is changing.
     */
	private void cleanupSnapshot(UserDefinedSQLType typeRemoved) {
	    
	    SnapshotCollection collection = session.getWorkspace().getSnapshotCollection();
	    
	    //The first type set will be from the system workspace and will not
	    //have a snapshot, then the snapshot will replace the one set from the system.
	    if (typeRemoved.getParent() == session.getSystemWorkspace() || 
	            (typeRemoved.getParent() instanceof DomainCategory && 
	                    typeRemoved.getParent().getParent() == session.getSystemWorkspace())) return;
	    
	    UserDefinedSQLTypeSnapshot udtSnapshot = null;
	    for (SPObjectSnapshot<?> snapshot : collection.getSPObjectSnapshots()) {
            if (snapshot.getSPObject().equals(typeRemoved)) {
                udtSnapshot = (UserDefinedSQLTypeSnapshot) snapshot;
                udtSnapshot.setSnapshotUseCount(udtSnapshot.getSnapshotUseCount() - 1);
                if (udtSnapshot.getSnapshotUseCount() > 0) return;
                break;
            }
	    }
	    if (udtSnapshot == null) throw new NullPointerException("The type " + typeRemoved + 
	            " is a snapshot type but does not have a snapshot object in the project.");
	    
	    //If we are here the snapshot is no longer in use.
	    collection.removeSPObjectSnapshot(udtSnapshot);
	    SPObjectSnapshot<?> categorySnapshot = null;
	    try {
	        if (udtSnapshot.isDomainSnapshot()) {
	            DomainCategory cat = (DomainCategory) udtSnapshot.getSPObject().getParent();
	            cat.removeChild(udtSnapshot.getSPObject());
	            
	            if (udtSnapshot.getSPObject().getUpstreamType().getUpstreamType() != null) 
	                throw new IllegalStateException("We currently do not support having a domain " +
	                		"reference an upstream type of a domain.");
	            
	            cleanupSnapshot(udtSnapshot.getSPObject().getUpstreamType());
	            
	            for (SPObjectSnapshot<?> snapshot : collection.getSPObjectSnapshots()) {
	                if (snapshot.getSPObject().equals(cat)) {
	                    categorySnapshot = snapshot;
	                    break;
	                }
	            }
	            if (cat.getChildren().size() == 0) {
	                collection.removeChild(categorySnapshot);
	                collection.removeChild(cat);
	            }
	        } else {
	            if(udtSnapshot.getSPObject().isMagicEnabled()) {
	                collection.removeChild(udtSnapshot.getSPObject());
	            }
	        }
	    } catch (Exception e) {
	        throw new RuntimeException(e);
        }
	    
	    //find its upstream type and remove its listener. Note that the system type
	    //may have been removed but the snapshot may have existed until this point.
	    UserDefinedSQLType systemType = session.findSystemTypeFromSnapshot(udtSnapshot);
	    if (systemType != null) {
	        SQLPowerUtils.unlistenToHierarchy(systemType, listenerMap.get(udtSnapshot));
	    } else if (categorySnapshot != null) {
	        UserDefinedSQLType systemCategory = session.findSystemTypeFromSnapshot(categorySnapshot);
	        if (systemCategory != null) {
	            SQLPowerUtils.unlistenToHierarchy(systemCategory, listenerMap.get(udtSnapshot));
	        } else {
	            SQLPowerUtils.unlistenToHierarchy(session.getSystemWorkspace(), listenerMap.get(udtSnapshot));
	        }
	    } else {
	        SQLPowerUtils.unlistenToHierarchy(session.getSystemWorkspace(), listenerMap.get(udtSnapshot));
	    }
        
	    //handle domain categories and domain's upstream type as well.
	}

    /**
     * Adds a listener to the correct system type based on the given type. The
     * listener on the system type will be used to update the snapshot in this
     * project when the system type changes.
     * 
     * @param columnSnapshotType
     *            This type is a type that is a snapshot of a system type used in a column.
     */
    private void addUpdateListener(UserDefinedSQLType upstreamSnapshotType) {
        if (upstreamSnapshotType == null) return; // Happens on undo.
        
        SnapshotCollection collection = session.getWorkspace().getSnapshotCollection();
        
        //check if the upstream type is actually a system type. Happens on undo.
        SPObject upstreamTypeParent = upstreamSnapshotType.getParent();
        if (upstreamTypeParent == null || upstreamTypeParent.equals(collection) ||
                (upstreamTypeParent instanceof DomainCategory && 
                    upstreamTypeParent.getParent().equals(collection))) {

            SPObjectSnapshot<?> snapshot = null;
            for (SPObjectSnapshot<?> workspaceSnapshot : collection.getSPObjectSnapshots()) {
                if (workspaceSnapshot.getSPObject() == upstreamSnapshotType) {
                    snapshot = workspaceSnapshot;
                    break;
                }
            }

            UserDefinedSQLType systemType = session.findSystemTypeFromSnapshot(snapshot);
            if (systemType != null) {
                SPObjectSnapshotUpdateListener udtSnapshotListener = new SPObjectSnapshotUpdateListener(snapshot);
                SQLPowerUtils.listenToHierarchy(systemType, udtSnapshotListener);
                listenerMap.put(snapshot, udtSnapshotListener);
                if (systemType.getParent() instanceof DomainCategory) {
                    DomainCategory category = (DomainCategory) systemType.getParent();
                    for (SPObjectSnapshot<?> categorySnapshot : collection.getSPObjectSnapshots()) {
                        if (categorySnapshot.getOriginalUUID().equals(category.getUUID())) {
                            SPObjectSnapshotUpdateListener categorySnapshotListener = new SPObjectSnapshotUpdateListener(categorySnapshot);
                            category.addSPListener(categorySnapshotListener);
                            listenerMap.put(categorySnapshot, categorySnapshotListener);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * This method will create a snapshot of the upstream type and any other
     * system types needed. Then the proxy will have its upstream type point at
     * the snapshot and the snapshot will point to the upstream type completing
     * the creation of the snapshot.
     * 
     * @param typeProxy
     *            The type that should point its upstream type to the new
     *            snapshot instead of the actual type.
     * @param upstreamType
     *            The system type that we want to create a snapshot of.
     * @param collection
     *            The collection of snapshots that the new snapshot(s) will be
     *            added to.
     * @param updateListener
     *            If not null this update listener will attach necessary
     *            listeners to the upstream type as needed to keep the snapshot
     *            correct in terms of the upstream type. If null no listeners
     *            will be added. XXX This is part of a quick fix to make the
     *            import of old projects work. It would be better to have this
     *            in a nicer fashion
     */
    public static void createSPObjectSnapshot(UserDefinedSQLType typeProxy, 
            UserDefinedSQLType upstreamType, SnapshotCollection collection,
            SPObjectSnapshotHierarchyListener updateListener) {
        if (!collection.isMagicEnabled() || !typeProxy.isMagicEnabled()) return;
        
        SPObject upstreamTypeParent = upstreamType.getParent();
        
        // Check if the upstream type is a system type by checking if it's not
        // parented by this session's workspace.
           if (upstreamTypeParent != null && !upstreamTypeParent.equals(collection) &&
                !(upstreamTypeParent instanceof DomainCategory && 
                    upstreamTypeParent.getParent().equals(collection))) {
            
            boolean snapshotExists = setUpstreamTypeToExistingSnapshot(typeProxy, upstreamType, collection);
            if (snapshotExists) return; // If snapshot already existed, then nothing else needs to be done
            
            // Otherwise, we have to create a new snapshot
            boolean isDomainSnapshot = upstreamTypeParent instanceof DomainCategory;
            UserDefinedSQLTypeSnapshot snapshot;
            if (upstreamType.getUpstreamType() != null) {
                //For domains
                UserDefinedSQLType upUpStreamType = upstreamType.getUpstreamType();
                boolean isUpstreamDomainSnapshot = upUpStreamType.getParent() instanceof DomainCategory;
                
                if (isUpstreamDomainSnapshot) throw new IllegalStateException(
                        "We currently don't handle domains that have an upstream type of another domain.");
                
                UserDefinedSQLTypeSnapshot upstreamSnapshot = null;
                boolean existingSnapshotFound = false;
                for (SPObjectSnapshot<?> workspaceSnapshot : collection.getSPObjectSnapshots()) {
                    if (workspaceSnapshot.getOriginalUUID().equals(upUpStreamType.getUUID()) && 
                            workspaceSnapshot instanceof UserDefinedSQLTypeSnapshot) {
                        upstreamSnapshot = (UserDefinedSQLTypeSnapshot) workspaceSnapshot;
                        upstreamSnapshot.setSnapshotUseCount(upstreamSnapshot.getSnapshotUseCount() + 1);
                        existingSnapshotFound = true;
                        break;
                    }
                }
                if (!existingSnapshotFound) {
                    upstreamSnapshot = new UserDefinedSQLTypeSnapshot(upUpStreamType, isUpstreamDomainSnapshot);
                    collection.addChild(upstreamSnapshot, 0);
                    collection.addChild(upstreamSnapshot.getSPObject(), 0);
                    upstreamSnapshot.setSnapshotUseCount(1);
                    if (updateListener != null) {
                        updateListener.addUpdateListener(upstreamSnapshot.getSPObject());
                    }
                }
                snapshot = new UserDefinedSQLTypeSnapshot(upstreamType, isDomainSnapshot, upstreamSnapshot);
            } else {
                snapshot = new UserDefinedSQLTypeSnapshot(upstreamType, isDomainSnapshot);
            }
            collection.addChild(snapshot, 0);
            if ((upstreamType.getParent() instanceof DomainCategory)) {
                DomainCategory parent = (DomainCategory) upstreamType.getParent();
                DomainCategorySnapshot domainSnapshot = 
                    new DomainCategorySnapshot(parent);
                collection.addChild(domainSnapshot, 0);
                collection.addChild(domainSnapshot.getSPObject(), 0);
                domainSnapshot.getSPObject().addChild(snapshot.getSPObject(), 0);
            } else {
                collection.addChild(snapshot.getSPObject(), 0);
            }
            typeProxy.setUpstreamType(snapshot.getSPObject());
            snapshot.setSnapshotUseCount(1);
        } else if (upstreamTypeParent != null && 
                (upstreamTypeParent.equals(collection) ||
                        (upstreamTypeParent instanceof DomainCategory && 
                                upstreamTypeParent.getParent().equals(collection)))) {
            for (SPObjectSnapshot<?> snapshot : collection.getSPObjectSnapshots()) {
                if (snapshot.getSPObject().equals(upstreamType)) {
                    UserDefinedSQLTypeSnapshot udtSnapshot = (UserDefinedSQLTypeSnapshot) snapshot;
                    udtSnapshot.setSnapshotUseCount(udtSnapshot.getSnapshotUseCount() + 1);
                    return;
                }
            }
        }
    }

    /**
     * If the upstreamType has an existing snapshot of it, then set the
     * typeProxy's upstream type to the snapshot of that type instead of the
     * original. This is to prevent multiple snapshots of a type to be created
     * every time that type is used.
     * 
     * @return True if an existing snapshot for the given upstream type was
     *         found Otherwise, return false.
     */
    private static boolean setUpstreamTypeToExistingSnapshot(UserDefinedSQLType typeProxy, 
            UserDefinedSQLType upstreamType, SnapshotCollection collection) {
        // Check if a snapshot for the upstreamType already exists. If so, just use that.
        boolean snapshotExists = false;
        List<UserDefinedSQLTypeSnapshot> typeSnapshots = 
            collection.getChildren(UserDefinedSQLTypeSnapshot.class);
        for (UserDefinedSQLTypeSnapshot typeSnapshot : typeSnapshots) {
            // If the snapshot is a domain snapshot, but the upstream type
            // is not a domain, then move on to the next snapshot.
            if (typeSnapshot.isDomainSnapshot() && 
                    !(upstreamType.getParent() instanceof DomainCategory)) continue;
            
            if (upstreamType.getUUID().equals(typeSnapshot.getOriginalUUID())) {
                typeProxy.setUpstreamType(typeSnapshot.getSPObject());
                typeSnapshot.setSnapshotUseCount(typeSnapshot.getSnapshotUseCount() + 1);
                snapshotExists = true;
                break;
            }
        }
        return snapshotExists;
    }
    
    @Override
    public void transactionStarted(TransactionEvent e) {
        if (settingSnapshot) {
            logger.debug("Ignoring begin");
            return;
        }
        logger.debug("Processing begin (\"" + e.getMessage() + "\")");
        transactionCount++;
        logger.debug("Incremented transaction counter to " + transactionCount);
        if (transactionCount == 1) {
            logger.debug("Firing snapshot begin");
            try {
                settingSnapshot = true;
                ((SPObject) e.getSource()).begin("setting upstream type (snapshot)");
            } finally {
                settingSnapshot = false;
            }
        }
    }
    
    @Override
    public void transactionEnded(TransactionEvent e) {
        if (settingSnapshot) {
            logger.debug("Ignoring commit");
            return;
        }
        logger.debug("Processing commit (\"" + e.getMessage() + "\")");

        transactionCount--;
        logger.debug("Decremented transaction counter to " + transactionCount);
        if (transactionCount == 0) {
            try {
                settingSnapshot = true;
                for (Entry<UserDefinedSQLType, PropertyChangeEvent> entry : upstreamTypeChangeEventMap.entrySet()) {
                    UserDefinedSQLType newValue = (UserDefinedSQLType) entry.getValue().getNewValue();
                    UserDefinedSQLType source = (UserDefinedSQLType) entry.getKey();
                    UserDefinedSQLType oldValue = (UserDefinedSQLType) entry.getValue().getOldValue();

                    logger.debug("Replacing upstreamType with snapshot!");
                    createSPObjectSnapshot(source, newValue, 
                            session.getWorkspace().getSnapshotCollection(), this);

                    if (oldValue != null &&
                    		session.getWorkspace().getSnapshotCollection().isMagicEnabled() &&
                    		source.isMagicEnabled()) {
                        cleanupSnapshot(oldValue);
                    }

                    UserDefinedSQLType columnProxyType = source;
                    addUpdateListener(columnProxyType.getUpstreamType());
                }
                upstreamTypeChangeEventMap.clear();
                ((SPObject) e.getSource()).commit("snapshot commit");
                logger.debug("Firing snapshot commit");
            } finally {
                settingSnapshot = false;
            }
            for (Map.Entry<UserDefinedSQLType, Integer> entry : typesToCleanup.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    cleanupSnapshot(entry.getKey());
                }
            }
            typesToCleanup.clear();
        }
    }

    /**
     * When copying columns from one server project to another, or local project
     * to a server project, those columns will usually be referring to
     * UserDefinedSQLTypeSnapshots that are not accessible by the target
     * project. So we have to reassign a different snapshot, either
     * pre-existing, or a new one, depending on the situation.
     * 
     * @return True if the type was successfully reassigned to an existing or
     *         new snapshot type. Return false if otherwise.
     */
    private void reassignType(SQLColumn column) {
        if (!session.getWorkspace().getSnapshotCollection().isMagicEnabled()) return;
        UserDefinedSQLType upstreamType = column.getUserDefinedSQLType().getUpstreamType();
        SPObject upstreamTypeParent = upstreamType.getParent();
        
        UserDefinedSQLTypeSnapshot sourceSnapshot = null;
        DomainCategorySnapshot sourceCategorySnapshot = null;
        
        String originalUUID = null;
        String originalCategoryUUID = null;
        if (upstreamTypeParent == null) {
            originalUUID = upstreamType.getUUID();
        } else if (upstreamTypeParent instanceof SnapshotCollection) {
            List<UserDefinedSQLTypeSnapshot> snapshots = 
                upstreamTypeParent.getChildren(UserDefinedSQLTypeSnapshot.class);
            for (UserDefinedSQLTypeSnapshot snapshot : snapshots) {
                if (upstreamType.equals(snapshot.getSPObject())) {
                    originalUUID = snapshot.getOriginalUUID();
                    sourceSnapshot = snapshot;
                    break;
                }
            }
        } else if (upstreamTypeParent instanceof DomainCategory) {
            List<SPObjectSnapshot> snapshots = 
                upstreamTypeParent.getParent().getChildren(SPObjectSnapshot.class);
            for (SPObjectSnapshot snapshot : snapshots) {
                if (upstreamType.equals(snapshot.getSPObject())) {
                    originalUUID = snapshot.getOriginalUUID();
                    sourceSnapshot = (UserDefinedSQLTypeSnapshot) snapshot;
                } else if (upstreamTypeParent.equals(snapshot.getSPObject())) {
                    originalCategoryUUID = upstreamTypeParent.getUUID();
                    sourceCategorySnapshot = (DomainCategorySnapshot) snapshot;
                }
                if (sourceSnapshot != null && sourceCategorySnapshot != null) break;
            }
        }
        
        if (originalUUID == null) {
            throw new IllegalStateException("Could not find the UUID of the original type of snapshot '" 
                    + upstreamType.getUUID() +"'");
        }
        
        for (UserDefinedSQLTypeSnapshot snapshot : 
            session.getWorkspace().getSnapshotCollection().getChildren(UserDefinedSQLTypeSnapshot.class)) {
            if (snapshot.getOriginalUUID().equals(originalUUID)) {
                column.getUserDefinedSQLType().setUpstreamType(snapshot.getSPObject());
                snapshot.setSnapshotUseCount(snapshot.getSnapshotUseCount() + 1);
                return;
            }
        }
        
        SnapshotCollection snapshotCollection = session.getWorkspace().getSnapshotCollection();
        
        DomainCategory newCategory = null;
        // If upstreamType is a domain, we need to copy the domain category snapshot as well
        if (upstreamType.getParent() instanceof DomainCategory) {
            DomainCategory upstreamCategory = (DomainCategory) upstreamType.getParent();
            newCategory = new DomainCategory(upstreamCategory.getName());
            
            DomainCategorySnapshot categorySnapshot = new DomainCategorySnapshot(newCategory, originalUUID);
            categorySnapshot.setName(sourceCategorySnapshot.getName());
            
            snapshotCollection.addSPObjectSnapshot(categorySnapshot, 0);
            snapshotCollection.addCategorySnapshot(newCategory, 0);
        }
        
        // If the upstreamType inherits from another type, then we need to make a copy of that as well
//        UserDefinedSQLType newUpUpstreamType = null;
//        if (upstreamType.getUpstreamType() != null) {
//            newUpUpstreamType = new UserDefinedSQLType();
//            UserDefinedSQLType.copyProperties(newUpUpstreamType, upstreamType.getUpstreamType());
//            UserDefinedSQLTypeSnapshot newSnapshot = 
//                new UserDefinedSQLTypeSnapshot(newUpUpstreamType, originalUUID, true);
//            newSnapshot.setName(upstreamType.getUpstreamType().getName());
//            snapshotCollection.addSPObjectSnapshot(newSnapshot, 0);
//            snapshotCollection.addUDTSnapshot(newUpUpstreamType, 0);
//            newSnapshot.setSnapshotUseCount(newSnapshot.getSnapshotUseCount() + 1);
//        }
        
        UserDefinedSQLType newType = new UserDefinedSQLType();
        UserDefinedSQLType.copyProperties(newType, upstreamType);
//        if (newUpUpstreamType != null) {
//            newType.setUpstreamType(newUpUpstreamType);
//        }
        UserDefinedSQLTypeSnapshot newSnapshot = new UserDefinedSQLTypeSnapshot(newType, originalUUID, 
                sourceSnapshot == null ? false : sourceSnapshot.isDomainSnapshot());
        newSnapshot.setName(sourceSnapshot.getName());
        snapshotCollection.addSPObjectSnapshot(newSnapshot, 0);
        if (newCategory != null) {
            newCategory.addChild(newType, 0);
        } else {
            snapshotCollection.addUDTSnapshot(newType, 0);
        }
        column.getUserDefinedSQLType().setUpstreamType(newType);
        newSnapshot.setSnapshotUseCount(newSnapshot.getSnapshotUseCount() + 1);
    }
}
