/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
package ca.sqlpower.architect.swingui.dbtree;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

/**
 * A tree model that displays {@link SQLObject}s contained in a {@link SQLObjectRoot}.
 */
public class DBTreeModel implements TreeModel, java.io.Serializable {

	private static Logger logger = Logger.getLogger(DBTreeModel.class);

	/**
	 * A visual node for the tree that groups children of the table together.
	 */
	private static class FolderNode extends SQLObject {
	    
	    private final SQLTable parentTable;
        private final Class<? extends SQLObject> containingChildType;
        private final Callable<Boolean> isPopulatedRunnable;

        /**
         * @param parentTable
         *            The SQLTable that this folder is to appear under
         * @param containingChildType
         *            The type of child of the SQLTable this folder is to
         *            contain. Must be a valid type of child in the table.
         */
	    public FolderNode(SQLTable parentTable, Class<? extends SQLObject> containingChildType,
	            Callable<Boolean> isPopulatedRunnable) {
	        this.parentTable = parentTable;
	        if (!parentTable.getAllowedChildTypes().contains(containingChildType)) 
	            throw new IllegalArgumentException(containingChildType + " is not a valid child type of " + parentTable);
            this.containingChildType = containingChildType;
            this.isPopulatedRunnable = isPopulatedRunnable;
	    }
	    
	    public SQLTable getParentTable() {
            return parentTable;
        }
	    
	    public List<? extends SQLObject> getChildren() {
	        return parentTable.getChildren(containingChildType);
	    }
	    
	    public Class<? extends SPObject> getContainingChildType() {
            return containingChildType;
        }
	    
	    @Override
	    public SQLObject getParent() {
	        return parentTable;
	    }

        @Override
        public boolean allowsChildren() {
            return true;
        }

        @Override
        public List<? extends SQLObject> getChildrenWithoutPopulating() {
            return parentTable.getChildrenWithoutPopulating(containingChildType);
        }

        @Override
        public String getShortDisplayName() {
            
            if (containingChildType.isAssignableFrom(SQLColumn.class)) {
                return "Columns folder for " + parentTable.getName();
            }
            
            if (containingChildType.isAssignableFrom(SQLIndex.class)) {
                return "Indices folder for " + parentTable.getName();
            }
            
            if (containingChildType.isAssignableFrom(SQLRelationship.class)) {
                return "Exported keys folder for " + parentTable.getName();
            }
            
            if (containingChildType.isAssignableFrom(SQLRelationship.SQLImportedKey.class)) {
                return "Imported keys folder for " + parentTable.getName();
            }
            
            return containingChildType.getSimpleName() + "s folder for " + parentTable.getName();
        }
        
        @Override
        public String toString() {
            return getShortDisplayName();
        }

        @Override
        protected void populateImpl() throws SQLObjectException {
            //do nothing
        }

        @Override
        protected boolean removeChildImpl(SPObject child) {
            throw new IllegalStateException("Cannot remove children from a folder, " +
            		"remove them from the table the folder is contained by.");
        }

        public int childPositionOffset(Class<? extends SPObject> childType) {
            return 0;
        }

        public List<Class<? extends SPObject>> getAllowedChildTypes() {
            List<Class<? extends SPObject>> allowedTypes = new ArrayList<Class<? extends SPObject>>();
            allowedTypes.add(containingChildType);
            return allowedTypes;
        }

        public List<? extends SPObject> getDependencies() {
            return Collections.emptyList();
        }

        public void removeDependency(SPObject dependency) {
            //do nothing
        }
        
        @Override
        public boolean isPopulated() {
            try {
                return isPopulatedRunnable.call().booleanValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public Throwable getChildrenInaccessibleReason(Class<? extends SQLObject> childType) {
            if (childType == containingChildType || childType == SQLObject.class) {
                return parentTable.getChildrenInaccessibleReason(containingChildType);
            } else {
                return null;
            }
        }
	    
	}
	
	/**
	 * For use in the {@link DBTreeSPListener}.
	 */
	private enum EventType {
	    INSERT,
	    REMOVE,
	    CHANGE
	}
	
	/**
	 * A {@link SPListener} implementation that will fire tree events as the underlying
	 * objects change.
	 */
	private class DBTreeSPListener implements SPListener {
	    
	    /**
	     * Small inner class to group the tree event with the type of event it is.
	     */
	    private class TreeEventWithType {
	        private final TreeModelEvent evt;
	        private final EventType type;
	        
	        public TreeEventWithType(TreeModelEvent evt, EventType type) {
                this.evt = evt;
                this.type = type;
	        }
	        
	        public TreeModelEvent getEvt() {
                return evt;
            }
	        
	        public EventType getType() {
                return type;
            }
	        
	        @Override
	        public String toString() {
	            return evt.toString() + ", " + type;
	        }
	    }

        /**
         * List to hold all of the events. The events are to be pooled during a
         * transaction and only acted upon when the transaction is complete.
         * This gives methods like populate the ability to fire multiple child
         * events but not cause the tree model to get the children of the
         * objects it is looking at which would cause populate to start over
         * again when it is already in the middle of populating.
         */
	    private List<TreeEventWithType> allEvents = new ArrayList<TreeEventWithType>();
	    
	    private int transactionCount = 0;
	    
        public void childAdded(SPChildEvent e) {
            if (!SQLPowerUtils.getAncestorList(e.getSource()).contains(root) && !e.getSource().equals(root)) return;
            
            if (!root.getSession().isForegroundThread()) 
                throw new IllegalStateException("Adding a child " + e.getChild() + " to " + e.getSource() + 
                        " not on the foreground thread.");
            if (logger.isDebugEnabled()) {
                logger.debug("dbChildrenInserted. source="+e.getSource() //$NON-NLS-1$
                        +" index: "+e.getIndex() //$NON-NLS-1$
                        +" child: "+e.getChild()); //$NON-NLS-1$
                logger.debug("dbChildrenInserted SQLObjectEvent: "+e //$NON-NLS-1$
                        +"; tree path="+Arrays.asList(getPathToNode(e.getSource()))); //$NON-NLS-1$
            }
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
            
            Set<TreeModelEvent> events = createTreeEvents(e);
            for (TreeModelEvent evt : events) {
                if (transactionCount > 0) {
                    allEvents.add(new TreeEventWithType(evt, EventType.INSERT));
                } else {
                    fireTreeNodesInserted(evt);
                }
            }

            if (e.getChild() instanceof SQLTable && foldersInTables.get(e.getChild()) == null) {
                SQLTable table = (SQLTable) e.getChild();
                createFolders(table);
                List<FolderNode> folderList = foldersInTables.get(table);
                int[] positions = new int[folderList.size()];
                for (int i = 0; i < folderList.size(); i++) {
                    positions[i] = i;
                }
                final TreeModelEvent evt = new TreeModelEvent(table, getPathToNode(table), 
                        positions, folderList.toArray());
                if (transactionCount > 0) {
                    allEvents.add(new TreeEventWithType(evt, EventType.INSERT));
                } else {
                    fireTreeNodesInserted(evt);
                }
            } else {
                setupTreeForNode((SQLObject) e.getChild());
            }
            
        }

        public void childRemoved(SPChildEvent e) {
            if (!SQLPowerUtils.getAncestorList(e.getSource()).contains(root) && !e.getSource().equals(root)) return;
            
            if (!root.getSession().isForegroundThread()) 
                throw new IllegalStateException("Removing a child " + e.getChild() + " to " + e.getSource() + 
                        " not on the foreground thread.");
            if (logger.isDebugEnabled()) {
                logger.debug("dbchildrenremoved. source="+e.getSource() //$NON-NLS-1$
                        +" index: "+e.getIndex() //$NON-NLS-1$
                        +" child: "+e.getChild()); //$NON-NLS-1$
            }
            if (logger.isDebugEnabled()) logger.debug("dbChildrenRemoved SQLObjectEvent: "+e); //$NON-NLS-1$
            SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
            if (e.getChild() instanceof SQLTable) {
                foldersInTables.remove(e.getChild());
            }
            
            Set<TreeModelEvent> events = createTreeEvents(e);
            for (TreeModelEvent evt : events) {
                if (transactionCount > 0) {
                    allEvents.add(new TreeEventWithType(evt, EventType.REMOVE));
                } else {
                    fireTreeNodesRemoved(evt);
                }
            }
        }

        public void transactionEnded(TransactionEvent e) {
            if (!root.getSession().isForegroundThread()) 
                throw new IllegalStateException("Transaction ended for " + e.getSource() + 
                        " while not on the foreground thread.");
            if (transactionCount == 0) {
                throw new IllegalStateException("Transaction ended outside of a transaction.");
            }
            transactionCount--;
            if (transactionCount == 0) {
                List<TreeEventWithType> currentEvents = new ArrayList<TreeEventWithType>(allEvents);
                for (TreeEventWithType evt : currentEvents) {
                    if (evt.getType() == EventType.INSERT) {
                        fireTreeNodesInserted(evt.getEvt());
                    } else if (evt.getType() == EventType.REMOVE) {
                        fireTreeNodesRemoved(evt.getEvt());
                    } else if (evt.getType() == EventType.CHANGE) {
                        fireTreeNodesChanged(evt.getEvt());
                    } else {
                        throw new IllegalStateException("Unknown event type " + evt.getType());
                    }
                }
                allEvents.clear();
            }
        }

        public void transactionRollback(TransactionEvent e) {
            if (!root.getSession().isForegroundThread()) 
                throw new IllegalStateException("Transaction rolled back for " + e.getSource() + 
                        " while not on the foreground thread.");
            transactionCount = 0;
            allEvents.clear();
        }

        public void transactionStarted(TransactionEvent e) {
            if (!root.getSession().isForegroundThread()) 
                throw new IllegalStateException("Transaction started for " + e.getSource() + 
                        " while not on the foreground thread.");
            transactionCount++;
        }

        public void propertyChanged(PropertyChangeEvent e) {
            if (!SQLPowerUtils.getAncestorList(((SPObject) e.getSource())).contains(root) && !e.getSource().equals(root)) return;
            
            
            if (!root.getSession().isForegroundThread()) 
                throw new IllegalStateException("Changing the property" + e.getPropertyName() + " on " + e.getSource() + 
                        " not on the foreground thread.");
            logger.debug("dbObjectChanged. source="+e.getSource()); //$NON-NLS-1$
            if ((!SwingUtilities.isEventDispatchThread()) && (!refireOnAnyThread)) {
                logger.warn("Not refiring because this is not the EDT. You will need to call refreshTreeStructure() at some point in the future."); //$NON-NLS-1$
                return;
            }
            if (logger.isDebugEnabled()) logger.debug("dbObjectChanged SQLObjectEvent: "+e); //$NON-NLS-1$
            processSQLObjectChanged(e);            
        }
        
        /**
         * Creates a TreeModelEvent for a SPChildEvent.
         */
        private Set<TreeModelEvent> createTreeEvents(SPChildEvent change) {
            Set<TreeModelEvent> events = new HashSet<TreeModelEvent>();
            SPObject parent = change.getSource();
            SPObject child = change.getChild();
            if (parent instanceof SQLTable) {
                for (FolderNode folder : foldersInTables.get(parent)) {
                    if (folder.getContainingChildType().isAssignableFrom(child.getClass())) {
                        parent = folder;
                        break;
                    }
                }
            }
            events.add(new TreeModelEvent(DBTreeModel.this, getPathToNode(parent), new int[]{change.getIndex()}, new Object[]{child}));
            return events;
        }

        /**
         * The profile manager needs to fire events from different threads.
         * So we need to get around the check.
         */
        private void processSQLObjectChanged(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("name") &&  //$NON-NLS-1$
                    !e.getNewValue().equals(((SPObject) e.getSource()).getName()) ) {
                logger.error("Name change event has wrong new value. new="+e.getNewValue()+"; real="+((SPObject) e.getSource()).getName()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            SPObject source = (SPObject) e.getSource();
            //The UUID should only change during loading. If it changes at a different time it is likely an error.
            if (e.getPropertyName().equals("UUID")) {
                refreshTreeStructure();
                logger.info("Changing a UUID. This should only be done during load.");
            } else {
                final TreeModelEvent evt = new TreeModelEvent(this, getPathToNode(source));
                if (transactionCount > 0) {
                    allEvents.add(new TreeEventWithType(evt, EventType.CHANGE));
                } else {
                    fireTreeNodesChanged(evt);
                }
            }
        }
	    
	}
	
	private final DBTreeSPListener treeListener = new DBTreeSPListener();

    /**
     * When this flag is true, the DBTreeModel's protection against firing
     * TreeModelEvents on the wrong thread are disabled. The only legitimate
     * use is in the test suite, where we are not on the EDT but we want to
     * test that the events are being fired correctly. Other uses are highly
     * suspect, since they will be breaking Swing's rule of only manipulating
     * components from the designated thread.
     */
    private boolean refireOnAnyThread = false;
    
	protected SQLObject root;
	
	/**
	 * Each table in the tree model is entered in the map when it is added to the tree
	 * and is mapped to folders that contains the children of the table broken into their
	 * types.
	 */
	protected final Map<SQLTable, List<FolderNode>> foldersInTables = 
	    new HashMap<SQLTable, List<FolderNode>>();
	
	/**
	 * Creates a tree model with all of the SQLDatabase objects in the
	 * given session's root object in its root list of databases.
	 *
	 * @param root A SQLObject that contains all the databases you
	 * want in the tree.  This does not necessarily have to be the
	 * root object associated with the given session, but it normally
	 * will be.
	 */
	public DBTreeModel(SQLObjectRoot root) throws SQLObjectException {
		this.root = root;
		this.treeModelListeners = new LinkedList();
		SQLPowerUtils.listenToHierarchy(root, treeListener); 
		
 		for (SPObject ancestor : SQLPowerUtils.getAncestorList(root)) {
		    ancestor.addSPListener(treeListener);
		}
		
		setupTreeForNode(root);
	}
	
	/**
	 * Recursively walks the tree doing any necessary setup for each node.
	 * At current this just adds folders for {@link SQLTable} objects.
	 */
	private void setupTreeForNode(SQLObject node) {
	    if (node instanceof SQLTable) {
	        createFolders((SQLTable) node);
	    }
	    for (SQLObject child : node.getChildrenWithoutPopulating()) {
	        setupTreeForNode(child);
	    }
	}

	public Object getRoot() {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getRoot: returning "+root); //$NON-NLS-1$
		return root;
	}

	public Object getChild(Object parent, int index) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getChild("+parent+","+index+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if (parent instanceof FolderNode) {
		    return ((FolderNode) parent).getChildren().get(index);
		} else if (parent instanceof SQLTable) {
		    return foldersInTables.get((SQLTable) parent).get(index);
		}
		
		SQLObject sqlParent = (SQLObject) parent;
		try {
            if (logger.isDebugEnabled()) logger.debug("returning "+sqlParent.getChild(index)); //$NON-NLS-1$
			return sqlParent.getChild(index);
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	}

	public int getChildCount(Object parent) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getChildCount("+parent+")"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (parent instanceof FolderNode) {
		    return ((FolderNode) parent).getChildren().size();
        } else if (parent instanceof SQLTable) {
            return foldersInTables.get((SQLTable) parent).size();
        }
		
		SQLObject sqlParent = (SQLObject) parent;
		try {
            if (logger.isDebugEnabled()) logger.debug("returning "+sqlParent.getChildren().size()); //$NON-NLS-1$
			return sqlParent.getChildren().size();
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	}

	public boolean isLeaf(Object parent) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.isLeaf("+parent+"): returning "+!((SQLObject) parent).allowsChildren()); //$NON-NLS-1$ //$NON-NLS-2$
		if (parent instanceof FolderNode) {
		    return false;
		}
		return !((SQLObject) parent).allowsChildren();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("model doesn't support editing yet"); //$NON-NLS-1$
	}

	public int getIndexOfChild(Object parent, Object child) {
	    SPObject spChild = (SPObject) child;
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getIndexOfChild("+parent+","+child+"): returning "+((SQLObject) parent).getChildren(spChild.getClass()).indexOf(child)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if (parent instanceof FolderNode) {
            return ((FolderNode) parent).getChildren(spChild.getClass()).indexOf(child);
        } else if (parent instanceof SQLTable) {
            if (foldersInTables.get((SQLTable) parent) == null) return -1;
            return foldersInTables.get((SQLTable) parent).indexOf(child);
        }
		
        return ((SQLObject) parent).getChildren(spChild.getClass()).indexOf(child);
	}

	// -------------- treeModel event source support -----------------
	protected LinkedList treeModelListeners;

	public void addTreeModelListener(TreeModelListener l) {		
		treeModelListeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	protected void fireTreeNodesInserted(TreeModelEvent e) {
		if (logger.isDebugEnabled()) logger.debug("Firing treeNodesInserted event: "+e); //$NON-NLS-1$
		final TreeModelEvent ev =e; 
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeNodesInserted(ev);
				}
			}
		};
		// TODO FIXME XXX Replace this with an alternate method leads to nasty behavior.  There are 3 others too
			notifier.run();
		
	}
	
	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		if (logger.isDebugEnabled()) logger.debug("Firing treeNodesRemoved event "+e); //$NON-NLS-1$
		final TreeModelEvent ev =e; 
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeNodesRemoved(ev);
				}
			}
		};
//		 TODO FIXME XXX Replace this with an alternate method leads to nasty behavior.  There are 3 others too
		notifier.run();
		
	}

	protected void fireTreeNodesChanged(TreeModelEvent e) {
		final TreeModelEvent ev =e; 
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeNodesChanged(ev);
				}
			}
		};
//		 TODO FIXME XXX Replace this with an alternate method leads to nasty behavior.  There are 3 others too
		notifier.run();
		
		
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {
		logger.debug("firing TreeStructuredChanged. source="+e.getSource()); //$NON-NLS-1$
		final TreeModelEvent ev =e; 		
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeStructureChanged(ev);
				}
			}
		};
//		 TODO FIXME XXX Replace this with an alternate method leads to nasty behavior.  There are 3 others too
			notifier.run();
		
	}
	
	/**
	 * This method will update the view of the tree by firing a structure change
	 * on the root. This is necessary to update the tree after loading the project
	 * on a separate thread.
	 * 
	 * NOTE: This method fires a TreeStructureChanged event which is not undoable
	 * and can be a heavy operation!
	 */
	public void refreshTreeStructure() {
        fireTreeStructureChanged(new TreeModelEvent(root, new Object[]{root}));
	}

	/**
	 * Returns the path from the conceptual, hidden root node (of type
	 * DBTreeRoot) to the given node.
	 * 
	 * <p>NOTE: This method doesn't work for SQLRelationship objects,
	 * because they have two parents! Use getPkPathToRelationship and
	 * getFkPathToRelationship instead.
	 *
	 * @throws IllegalArgumentException if <code>node</code> is of class SQLRelationship.
	 */
	public SQLObject[] getPathToNode(SPObject node) {
		LinkedList path = new LinkedList();
		while (node != null && node != root) {
		    if (path.size() > 0 && node instanceof SQLTable) {
		        for (FolderNode folder : foldersInTables.get(node)) {
		            if (folder.getContainingChildType().isAssignableFrom(path.get(0).getClass())) {
		                path.add(0, folder);
		                break;
		            }
		        }
		    }
			path.add(0, node);
			node = node.getParent();
		}
		path.add(0, root);
		return (SQLObject[]) path.toArray(new SQLObject[path.size()]);
	}

	/**
     * Returns the path from the conceptual, hidden root node (of type
     * DBTreeRoot) to the given node.
     * 
     * If the node is not a relationship then the list will only contain
     * one path to the object. Otherwise the list will contain the path
     * to the primary key then the path to the foreign key.
	 */
	public List<SQLObject[]> getPathsToNode(SQLObject node) {
	    List<SQLObject[]> nodePaths = new ArrayList<SQLObject[]>();
	    nodePaths.add(getPathToNode(node));
	    return nodePaths;
	}

    /**
     * When this flag is true, the DBTreeModel's protection against firing
     * TreeModelEvents on the wrong thread are disabled. The only legitimate
     * use is in the test suite, where we are not on the EDT but we want to
     * test that the events are being fired correctly. Other uses are highly
     * suspect, since they will be breaking Swing's rule of only manipulating
     * components from the designated thread.
     */
    public void setRefireEventsOnAnyThread(boolean v) {
        refireOnAnyThread = v;
    }
    
    /**
     * Creates all of the folders the given table should contain for its children
     * and adds them to the {@link #foldersInTables} map.
     */
    private void createFolders(final SQLTable table) {
        if (foldersInTables.get(table) == null) {
            List<FolderNode> folderList = new ArrayList<FolderNode>();
            foldersInTables.put(table, folderList);
            FolderNode SQLColumnFolder = new FolderNode(table, SQLColumn.class, new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return table.isColumnsPopulated();
                }
            });
            folderList.add(SQLColumnFolder);
            FolderNode SQLRelationshipFolder = new FolderNode(table, SQLRelationship.class, new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return table.isExportedKeysPopulated();
                }
            });
            folderList.add(SQLRelationshipFolder);
            FolderNode SQLImportedKeys = new FolderNode(table, SQLImportedKey.class, new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return table.isImportedKeysPopulated();
                }
            });
            folderList.add(SQLImportedKeys);
            FolderNode SQLIndexFolder = new FolderNode(table, SQLIndex.class, new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return table.isIndicesPopulated();
                }
            });
            folderList.add(SQLIndexFolder);
        }
    }
}
