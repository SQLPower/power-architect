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

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

public class DBTreeModel extends AbstractSPListener implements TreeModel, java.io.Serializable {

	private static Logger logger = Logger.getLogger(DBTreeModel.class);

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
	 * Map of parents to children and indices of nodes inserted during the transaction
	 */
	private Map<SPObject, HashMap<Integer, SPObject>> insertedNodes = new HashMap<SPObject, HashMap<Integer, SPObject>>();
	
	/**
	 * Map of parents to children and indices of nodes removed during the transaction 
	 */
	private Map<SPObject, HashMap<Integer, SPObject>> removedNodes = new HashMap<SPObject, HashMap<Integer, SPObject>>();
	
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
		SQLPowerUtils.listenToHierarchy(root, this);
	}

	public Object getRoot() {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getRoot: returning "+root); //$NON-NLS-1$
		return root;
	}

	public Object getChild(Object parent, int index) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getChild("+parent+","+index+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		SQLObject sqlParent = (SQLObject) parent;
		try {
            if (logger.isDebugEnabled()) logger.debug("returning "+sqlParent.getChildCount()); //$NON-NLS-1$
			return sqlParent.getChildCount();
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	}

	public boolean isLeaf(Object parent) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.isLeaf("+parent+"): returning "+!((SQLObject) parent).allowsChildren()); //$NON-NLS-1$ //$NON-NLS-2$
		return !((SQLObject) parent).allowsChildren();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("model doesn't support editting yet"); //$NON-NLS-1$
	}

	public int getIndexOfChild(Object parent, Object child) {
		try {
			if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getIndexOfChild("+parent+","+child+"): returning "+((SQLObject) parent).getChildren().indexOf(child)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return ((SQLObject) parent).getChildren().indexOf(child);
		} catch (SQLObjectException e) {
			//logger.error("Couldn't get index of child "+child, e);
			//return -1;
			throw new SQLObjectRuntimeException(e);
		}
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
			path.add(0, node);
			node = node.getParent();
		}
		path.add(0, root);
		return (SQLObject[]) path.toArray(new SQLObject[path.size()]);
	}

	public SQLObject[] getPkPathToRelationship(SQLRelationship rel) {
		SQLObject[] pathToPkTable = getPathToNode(rel.getPkTable());
		SQLObject[] path = new SQLObject[pathToPkTable.length + 2];
		System.arraycopy(pathToPkTable, 0, path, 0, pathToPkTable.length);
        path[path.length - 2] = rel.getPkTable().getExportedKeysFolder();
        path[path.length - 1] = rel;
		return path;
	}

	public SQLObject[] getFkPathToRelationship(SQLRelationship rel) {
		SQLObject[] pathToFkTable = getPathToNode(rel.getFkTable());
		SQLObject[] path = new SQLObject[pathToFkTable.length + 2];
		System.arraycopy(pathToFkTable, 0, path, 0, pathToFkTable.length);
        path[path.length - 2] = rel.getFkTable().getImportedKeysFolder();
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


	// --------------------- SQLObject listener support -----------------------
	@Override
	public void childAddedImpl(SPChildEvent e) {
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

        if (!insertedNodes.containsKey(e.getSource())) {
            insertedNodes.put(e.getSource(), new HashMap<Integer, SPObject>());
        }
        insertedNodes.get(e.getSource()).put(e.getIndex(), e.getChild());
	}

	@Override
	public void childRemovedImpl(SPChildEvent e) {
        if (logger.isDebugEnabled()) {
            logger.debug("dbchildrenremoved. source="+e.getSource() //$NON-NLS-1$
                    +" index: "+e.getIndex() //$NON-NLS-1$
                    +" child: "+e.getChild()); //$NON-NLS-1$
        }
		if (logger.isDebugEnabled()) logger.debug("dbChildrenRemoved SQLObjectEvent: "+e); //$NON-NLS-1$
		SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
		
		if (!removedNodes.containsKey(e.getSource())) {
		    removedNodes.put(e.getSource(), new HashMap<Integer, SPObject>());
		}
		removedNodes.get(e.getSource()).put(e.getIndex(), e.getChild());
	}
	
	@Override
	public void propertyChangeImpl(PropertyChangeEvent e) {
		logger.debug("dbObjectChanged. source="+e.getSource()); //$NON-NLS-1$
        if ((!SwingUtilities.isEventDispatchThread()) && (!refireOnAnyThread)) {
            logger.debug("Not refiring because this is not the EDT. You will need to call refreshTreeStructure() at some point in the future."); //$NON-NLS-1$
            return;
        }
		if (logger.isDebugEnabled()) logger.debug("dbObjectChanged SQLObjectEvent: "+e); //$NON-NLS-1$
		processSQLObjectChanged(e);
	}
	
	@Override
	protected void finalCommitImpl(TransactionEvent e) {
	    if ((!SwingUtilities.isEventDispatchThread()) && (!refireOnAnyThread)) {
            logger.debug("Not refiring because this is not the EDT. You will need to call refreshTreeStructure() at some point in the future."); //$NON-NLS-1$
            return;
        }
	    
	    for (TreeModelEvent insert : createTreeEvents(insertedNodes)) {
	        fireTreeNodesInserted(insert);
	    }
	    insertedNodes.clear();
	    
	    for (TreeModelEvent remove : createTreeEvents(removedNodes)) {
            fireTreeNodesRemoved(remove);
        }
	    removedNodes.clear();
	}
	
	/**
	 * Creates a set of TreeModelEvents, one for each parent, based on the given map.
	 */
	private Set<TreeModelEvent> createTreeEvents(Map<SPObject, HashMap<Integer, SPObject>> changes) {
	    Set<TreeModelEvent> events = new HashSet<TreeModelEvent>();
	    for (Map.Entry<SPObject, HashMap<Integer, SPObject>> e : changes.entrySet()) {
	        if (e.getKey() instanceof SQLRelationship) {
	            // SQLRelationships are in the tree twice, must fire two events
	            events.add(createTreeModelEvent(getPkPathToRelationship((SQLRelationship) e.getKey()), e.getValue()));
	            events.add(createTreeModelEvent(getFkPathToRelationship((SQLRelationship) e.getKey()), e.getValue()));
	        } else {
	            events.add(createTreeModelEvent(getPathToNode(e.getKey()), e.getValue()));
	        }
	    }
	    return events;
	}

	/**
	 * Creates a single TreeModelEvent for the given path and Map of children.
	 */
	private TreeModelEvent createTreeModelEvent(SPObject[] path, HashMap<Integer, SPObject> changes) {
	    int[] indexes = new int[changes.size()];
	    SPObject[] children = new SPObject[changes.size()];
	    Iterator<Map.Entry <Integer, SPObject>> it = changes.entrySet().iterator();
	    for (int i = 0; it.hasNext(); i++) {
	        Map.Entry<Integer, SPObject> e = it.next();
	        indexes[i] = e.getKey();
	        children[i] = e.getValue();
	    }
	    return new TreeModelEvent(this, path, indexes, children);
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
}
