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
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

public class DBTreeModel implements TreeModel, java.io.Serializable {

	private static Logger logger = Logger.getLogger(DBTreeModel.class);

	/**
	 * A visual node for the tree that groups children of the table together.
	 */
	private static class FolderNode extends SQLObject {
	    
	    private final SQLTable parentTable;
        private final Class<? extends SQLObject> containingChildType;

        /**
         * @param parentTable
         *            The SQLTable that this folder is to appear under
         * @param containingChildType
         *            The type of child of the SQLTable this folder is to
         *            contain. Must be a valid type of child in the table.
         */
	    public FolderNode(SQLTable parentTable, Class<? extends SQLObject> containingChildType) {
	        this.parentTable = parentTable;
	        if (!parentTable.getAllowedChildTypes().contains(containingChildType)) 
	            throw new IllegalArgumentException(containingChildType + " is not a valid child type of " + parentTable);
            this.containingChildType = containingChildType;
	    }
	    
	    public SQLTable getParentTable() {
            return parentTable;
        }
	    
	    public List<? extends SQLObject> getChildren() {
	        return parentTable.getChildrenWithoutPopulating(containingChildType);
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
            return "Folder for " + containingChildType.getSimpleName();
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
	    
	}
	
	/**
	 * A {@link SPListener} implementation that will fire tree events as the underlying
	 * objects change.
	 */
	private class DBTreeSPListener implements SPListener {
	    
        public void childAdded(SPChildEvent e) {
            if (logger.isDebugEnabled()) {
                logger.debug("dbChildrenInserted. source="+e.getSource() //$NON-NLS-1$
                        +" index: "+e.getIndex() //$NON-NLS-1$
                        +" child: "+e.getChild()); //$NON-NLS-1$
                if (e.getSource() instanceof SQLRelationship) {
                    SQLRelationship r = (SQLRelationship) e.getSource();
                    logger.debug("dbChildrenInserted SQLObjectEvent: "+e //$NON-NLS-1$
                                 +"; pk path="+Arrays.asList(getPkPathToRelationship(r)) //$NON-NLS-1$
                                 +"; fk path="+Arrays.asList(getFkPathToRelationship(r))); //$NON-NLS-1$
                } else {
                    logger.debug("dbChildrenInserted SQLObjectEvent: "+e //$NON-NLS-1$
                                 +"; tree path="+Arrays.asList(getPathToNode(e.getSource()))); //$NON-NLS-1$
                }
            }
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
            
            Set<TreeModelEvent> events = createTreeEvents(e);
            for (TreeModelEvent evt : events) {
                fireTreeNodesInserted(evt);
            }

            if (e.getChild() instanceof SQLTable && foldersInTables.get(e.getChild()) == null) {
                SQLTable table = (SQLTable) e.getChild();
                createFolders(table);
                List<FolderNode> folderList = foldersInTables.get(table);
                int[] positions = new int[folderList.size()];
                for (int i = 0; i < folderList.size(); i++) {
                    positions[i] = i;
                }
                fireTreeNodesInserted(new TreeModelEvent(table, getPathToNode(table), 
                        positions, folderList.toArray()));
            } else {
                setupTreeForNode((SQLObject) e.getChild());
            }
            
        }

        public void childRemoved(SPChildEvent e) {
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
                fireTreeNodesRemoved(evt);
            }
        }

        public void transactionEnded(TransactionEvent e) {
            //do nothing
        }

        public void transactionRollback(TransactionEvent e) {
            //do nothing            
        }

        public void transactionStarted(TransactionEvent e) {
            //do nothing            
        }

        public void propertyChange(PropertyChangeEvent e) {
            logger.debug("dbObjectChanged. source="+e.getSource()); //$NON-NLS-1$
            if ((!SwingUtilities.isEventDispatchThread()) && (!refireOnAnyThread)) {
                logger.debug("Not refiring because this is not the EDT. You will need to call refreshTreeStructure() at some point in the future."); //$NON-NLS-1$
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
            if (parent instanceof SQLRelationship) {
                // SQLRelationships are in the tree twice, must fire two events
                events.add(new TreeModelEvent(DBTreeModel.this, getPkPathToRelationship((SQLRelationship) parent), new int[]{change.getIndex()}, new Object[]{child}));
                events.add(new TreeModelEvent(DBTreeModel.this, getFkPathToRelationship((SQLRelationship) parent), new int[]{change.getIndex()}, new Object[]{child}));
            } else {
                if (parent instanceof SQLTable) {
                    for (FolderNode folder : foldersInTables.get(parent)) {
                        if (folder.getContainingChildType().isAssignableFrom(child.getClass())) {
                            parent = folder;
                            break;
                        }
                    }
                }
                events.add(new TreeModelEvent(DBTreeModel.this, getPathToNode(parent), new int[]{change.getIndex()}, new Object[]{child}));
            }
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
            if (source instanceof SQLRelationship) {
                SQLRelationship r = (SQLRelationship) source;
                fireTreeNodesChanged(new TreeModelEvent(this, getPkPathToRelationship(r)));
                fireTreeNodesChanged(new TreeModelEvent(this, getFkPathToRelationship(r)));
            } else {
                fireTreeNodesChanged(new TreeModelEvent(this, getPathToNode(source)));
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
            if (logger.isDebugEnabled()) logger.debug("returning "+sqlParent.getChildrenWithoutPopulating().size()); //$NON-NLS-1$
			return sqlParent.getChildrenWithoutPopulating().size();
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
		throw new UnsupportedOperationException("model doesn't support editting yet"); //$NON-NLS-1$
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getIndexOfChild("+parent+","+child+"): returning "+((SQLObject) parent).getChildren().indexOf(child)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if (parent instanceof FolderNode) {
            return ((FolderNode) parent).getChildren().indexOf(child);
        } else if (parent instanceof SQLTable) {
            if (foldersInTables.get((SQLTable) parent) == null) return -1;
            return foldersInTables.get((SQLTable) parent).indexOf(child);
        }
		
        return ((SQLObject) parent).getChildren().indexOf(child);
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
		if (node instanceof SQLRelationship) {
			throw new IllegalArgumentException("This method does not work for SQLRelationship. Use getPkPathToRelationship() and getFkPathToRelationship() instead."); //$NON-NLS-1$
		}
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

	public SQLObject[] getPkPathToRelationship(SQLRelationship rel) {
		SQLObject[] pathToPkTable = getPathToNode(rel.getPkTable());
		SQLObject[] path = new SQLObject[pathToPkTable.length + 1];
		System.arraycopy(pathToPkTable, 0, path, 0, pathToPkTable.length);
        path[path.length - 1] = rel;
		return path;
	}

	public SQLObject[] getFkPathToRelationship(SQLRelationship rel) {
		SQLObject[] pathToFkTable = getPathToNode(rel.getFkTable());
		SQLObject[] path = new SQLObject[pathToFkTable.length + 1];
		System.arraycopy(pathToFkTable, 0, path, 0, pathToFkTable.length);
		path[path.length - 1] = rel;
		return path;
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
	    if (node instanceof SQLRelationship) {
	        SQLRelationship rel = (SQLRelationship) node;
	        nodePaths.add(getPkPathToRelationship(rel));
	        nodePaths.add(getFkPathToRelationship(rel));
	    } else {
	        nodePaths.add(getPathToNode(node));
	    }
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
    private void createFolders(SQLTable table) {
        if (foldersInTables.get(table) == null) {
            List<FolderNode> folderList = new ArrayList<FolderNode>();
            foldersInTables.put(table, folderList);
            FolderNode SQLColumnFolder = new FolderNode(table, SQLColumn.class);
            folderList.add(SQLColumnFolder);
            FolderNode SQLRelationshipFolder = new FolderNode(table, SQLRelationship.class);
            folderList.add(SQLRelationshipFolder);
            FolderNode SQLIndexFolder = new FolderNode(table, SQLIndex.class);
            folderList.add(SQLIndexFolder);
        }
    }
}
