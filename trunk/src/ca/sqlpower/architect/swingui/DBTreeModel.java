package ca.sqlpower.architect.swingui;

import javax.swing.tree.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;

public class DBTreeModel implements TreeModel, SQLObjectListener, java.io.Serializable {

	private static Logger logger = Logger.getLogger(DBTreeModel.class);

	protected SQLObject root;

	/**
	 * Creates a tree model with an empty list of databases at its
	 * root.
	 */
	public DBTreeModel() throws ArchitectException {
		this(null);
	}

	/**
	 * Creates a tree model with all of the SQLDatabase objects in the
	 * given collection in its root list of databases.
	 *
	 * @param initialDatabases A collection whose items are all
	 * distinct SQLDatabase objects.
	 */
	public DBTreeModel(Collection initialDatabases) throws ArchitectException {
		this.root = new DBTreeRoot();
		if (initialDatabases != null) {
			Iterator it = initialDatabases.iterator();
			while (it.hasNext()) {
				root.addChild((SQLDatabase) it.next());
			}
		}
		this.treeModelListeners = new LinkedList();
		ArchitectUtils.listenToHierarchy(this, root);
	}

	public Object getRoot() {
		logger.debug("DBTreeModel.getRoot: returning "+root);
		return root;
	}

	public Object getChild(Object parent, int index) {
		try {
			logger.debug("DBTreeModel.getChild("+parent+","+index+"): returning "+((SQLObject) parent).getChild(index));
			return ((SQLObject) parent).getChild(index);
		} catch (ArchitectException e) {
			logger.error("Couldn't get child "+index+" of "+parent, e);
			return null;
		}
	}

	public int getChildCount(Object parent) {
		try {
			logger.debug("DBTreeModel.getChildCount("+parent+"): returning "+((SQLObject) parent).getChildCount());
			return ((SQLObject) parent).getChildCount();
		} catch (ArchitectException e) {
			logger.error("Couldn't get child count of "+parent, e);
			return -1;
		}
	}

	public boolean isLeaf(Object parent) {
		logger.debug("DBTreeModel.isLeaf("+parent+"): returning "+!((SQLObject) parent).allowsChildren());
		return !((SQLObject) parent).allowsChildren();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("model doesn't support editting yet");
	}

	public int getIndexOfChild(Object parent, Object child) {
		try {
			logger.debug("DBTreeModel.getIndexOfChild("+parent+","+child+"): returning "+((SQLObject) parent).getChildren().indexOf(child));
			return ((SQLObject) parent).getChildren().indexOf(child);
		} catch (ArchitectException e) {
			logger.error("Couldn't get index of child "+child, e);
			return -1;
		}
	}

	/**
	 * The backing class for an invisible root node that contains
	 * SQLDatabase objects.
	 */
	protected class DBTreeRoot extends SQLObject {
		public DBTreeRoot() {
			children = new LinkedList();
		}

		public SQLObject getParent() {
			return null;
		}

		public String getName() {
			return getShortDisplayName();
		}

		public String getShortDisplayName() {
			return "Database Connections";
		}
		
		public boolean allowsChildren() {
			return true;
		}
		
		public void populate() throws ArchitectException {
			return;
		}
		
		public boolean isPopulated() {
			return true;
		}

		public String toString() {
			return getShortDisplayName();
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
		logger.debug("Firing treeNodesInserted event: "+e);
		Iterator it = treeModelListeners.iterator();
		while (it.hasNext()) {
			((TreeModelListener) it.next()).treeNodesInserted(e);
		}
	}
	
	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		logger.debug("Firing treeNodesRemoved event "+e);
		Iterator it = treeModelListeners.iterator();
		while (it.hasNext()) {
			((TreeModelListener) it.next()).treeNodesRemoved(e);
			logger.debug("Sent a copy");
		}
	}

	protected void fireTreeNodesChanged(TreeModelEvent e) {
		Iterator it = treeModelListeners.iterator();
		while (it.hasNext()) {
			((TreeModelListener) it.next()).treeNodesChanged(e);
		}
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {
		Iterator it = treeModelListeners.iterator();
		while (it.hasNext()) {
			((TreeModelListener) it.next()).treeStructureChanged(e);
		}
	}

	public SQLObject[] getPathToNode(SQLObject node) {
		LinkedList path = new LinkedList();
		while (node != null) {
			path.add(0, node);
			node = node.getParent();
		}
		path.add(0, root);
		return (SQLObject[]) path.toArray(new SQLObject[path.size()]);
	}

	// --------------------- SQLObject listener support -----------------------
	public void dbChildrenInserted(SQLObjectEvent e) {
		logger.debug("dbChildrenInserted SQLObjectEvent: "+e);
		try {
			SQLObject[] newEventSources = e.getChildren();
			for (int i = 0; i < newEventSources.length; i++) {
				ArchitectUtils.listenToHierarchy(this, newEventSources[i]);
			}
		} catch (ArchitectException ex) {
			logger.error("Error listening to added object", ex);
		}
		TreeModelEvent tme = new TreeModelEvent(this,
												getPathToNode(e.getSQLSource()),
												e.getChangedIndices(),
												e.getChildren());
		fireTreeNodesInserted(tme);
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		logger.debug("dbChildrenRemoved SQLObjectEvent: "+e);
		try {
			SQLObject[] oldEventSources = e.getChildren();
			for (int i = 0; i < oldEventSources.length; i++) {
				ArchitectUtils.unlistenToHierarchy(this, oldEventSources[i]);
			}
		} catch (ArchitectException ex) {
			logger.error("Error unlistening to removed object", ex);
		}
		TreeModelEvent tme = new TreeModelEvent(this,
												getPathToNode(e.getSQLSource()),
												e.getChangedIndices(),
												e.getChildren());
		fireTreeNodesRemoved(tme);
	}
	
	public void dbObjectChanged(SQLObjectEvent e) {
		logger.debug("dbObjectChanged SQLObjectEvent: "+e);
		SQLObject source = e.getSQLSource();
		fireTreeNodesChanged(new TreeModelEvent(this, getPathToNode(source)));
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		throw new UnsupportedOperationException("not yet");
	}
}
