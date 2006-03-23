package ca.sqlpower.architect.swingui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
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
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getRoot: returning "+root);
		return root;
	}

	public Object getChild(Object parent, int index) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getChild("+parent+","+index+")");
		try {
			if (logger.isDebugEnabled()) logger.debug("returning "+((SQLObject) parent).getChild(index));
			return ((SQLObject) parent).getChild(index);
		} catch (Exception e) {
			SQLExceptionNode fakeChild = putExceptionNodeUnder((SQLObject) parent, e);
			return fakeChild;
		}
	}

	public int getChildCount(Object parent) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getChildCount("+parent+")");
		try {
			if (logger.isDebugEnabled()) logger.debug("returning "+((SQLObject) parent).getChildCount());
			return ((SQLObject) parent).getChildCount();
		} catch (Exception e) {
			SQLExceptionNode fakeChild = putExceptionNodeUnder((SQLObject) parent, e);
			return 1; // XXX: could be incorrect if exception was not a populate problem!
		}
	}

	public boolean isLeaf(Object parent) {
		if (logger.isDebugEnabled()) logger.debug("DBTreeModel.isLeaf("+parent+"): returning "+!((SQLObject) parent).allowsChildren());
		return !((SQLObject) parent).allowsChildren();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("model doesn't support editting yet");
	}

	public int getIndexOfChild(Object parent, Object child) {
		try {
			if (logger.isDebugEnabled()) logger.debug("DBTreeModel.getIndexOfChild("+parent+","+child+"): returning "+((SQLObject) parent).getChildren().indexOf(child));
			return ((SQLObject) parent).getChildren().indexOf(child);
		} catch (ArchitectException e) {
			//logger.error("Couldn't get index of child "+child, e);
			//return -1;
			throw new ArchitectRuntimeException(e);
		}
	}

	/**
	 * The backing class for an invisible root node that contains
	 * SQLDatabase objects.
	 */
	public static class DBTreeRoot extends SQLObject {
		public DBTreeRoot() {
			children = new LinkedList();
		}

		public SQLObject getParent() {
			return null;
		}

		protected void setParent(SQLObject newParent) {
			// no parent
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

		@Override
		public Class<? extends SQLObject> getChildType() {
			return SQLDatabase.class;
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
		if (logger.isDebugEnabled()) logger.debug("Firing treeNodesInserted event: "+e);
		final TreeModelEvent ev =e; 
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeNodesInserted(ev);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			notifier.run();
		}
		else
		{
			SwingUtilities.invokeLater(notifier);
		}
	}
	
	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		if (logger.isDebugEnabled()) logger.debug("Firing treeNodesRemoved event "+e);
		final TreeModelEvent ev =e; 
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeNodesRemoved(ev);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			notifier.run();
		}
		else
		{
			SwingUtilities.invokeLater(notifier);
		}
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
		if (SwingUtilities.isEventDispatchThread()) {
			notifier.run();
		}
		else
		{
			SwingUtilities.invokeLater(notifier);
		}
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {
		final TreeModelEvent ev =e; 
		Runnable notifier = new Runnable(){
			public void run() {
				Iterator it = treeModelListeners.iterator();
				while (it.hasNext()) {
					((TreeModelListener) it.next()).treeStructureChanged(ev);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			notifier.run();
		}
		else
		{
			SwingUtilities.invokeLater(notifier);
		}
	}

	/**
	 * Returns the path from the conceptual, hidden root node (of type
	 * DBTreeRoot) to the given node.
	 * 
	 * <p>NOTE: This method doesn't work for SQLRelationship objects,
	 * because they have two parents! Use getPkPathToRelationship and
	 * getFkPathToRelationship instead.
	 *
	 * <p>XXX: getPathToNode and get(Pk|Fk)PathToRelationship should
	 * be merged into a new getPathsToNode method that returns a List
	 * or array of paths.  Then all methods that call it (currently
	 * they are only here and in DBTree) should be adapted to allow
	 * multiple returned paths.  getPathToNode is not part of the
	 * javax.swing.tree.TreeModel interface.
	 *
	 * @throws IllegalArgumentException if <code>node</code> is of class SQLRelationship.
	 */
	public SQLObject[] getPathToNode(SQLObject node) {
		if (node instanceof SQLRelationship) {
			throw new IllegalArgumentException("This method does not work for SQLRelationship. Use getPkPathToRelationship() and getFkPathToRelationship() instead.");
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
	 * Creates a SQLExceptionNode with the given Throwable and places
	 * it under parent.
	 *
	 * @return the node that has been added to parent.
	 */
	protected SQLExceptionNode putExceptionNodeUnder(final SQLObject parent, Throwable ex) {
		// dig for root cause and message
		logger.info("Adding exception node under "+parent, ex);
		String message = ex.getMessage();
		Throwable cause = ex;
		while (cause.getCause() != null) {
			cause = cause.getCause();
			if (cause.getMessage() != null && cause.getMessage().length() > 0) {
				message = cause.getMessage();
			}
		}
		
		if (message == null || message.length() == 0) {
			message = "Check application log for details";
		}
		
		final SQLExceptionNode excNode = new SQLExceptionNode(ex, message);
		excNode.setParent((SQLObject) parent);

		/* This is likely to fail, but it should convince the parent that it is populated */
		try {
			parent.getChildCount();
		} catch (ArchitectException e) {
			logger.error("Couldn't populate parent node of exception");
		}

		try {
			parent.addChild(excNode);
		} catch (ArchitectException e) {
			logger.error("Couldn't add SQLExceptionNode \""+excNode.getName()+"\" to tree model:", e);
			JOptionPane.showMessageDialog(null, "Failed to add SQLExceptionNode:\n"+e.getMessage());
		}
		return excNode;
	}

	// --------------------- SQLObject listener support -----------------------
	public void dbChildrenInserted(SQLObjectEvent e) {
		if (logger.isDebugEnabled()) {
			if (e.getSQLSource() instanceof SQLRelationship) {
				SQLRelationship r = (SQLRelationship) e.getSQLSource();
				logger.debug("dbChildrenInserted SQLObjectEvent: "+e
							 +"; pk path="+Arrays.asList(getPkPathToRelationship(r))
							 +"; fk path="+Arrays.asList(getFkPathToRelationship(r)));
			} else {
				logger.debug("dbChildrenInserted SQLObjectEvent: "+e
							 +"; tree path="+Arrays.asList(getPathToNode(e.getSQLSource())));
			}
		}
		try {
			SQLObject[] newEventSources = e.getChildren();
			for (int i = 0; i < newEventSources.length; i++) {
				ArchitectUtils.listenToHierarchy(this, newEventSources[i]);
			}
		} catch (ArchitectException ex) {
			logger.error("Error listening to added object", ex);
		}

		// relationships have two parents (pktable and fktable) so we need to fire two TMEs
		if (e.getSQLSource() instanceof SQLRelationship) {
			TreeModelEvent tme = new TreeModelEvent
				(this,
				 getPkPathToRelationship((SQLRelationship) e.getSQLSource()),
				 e.getChangedIndices(),
				 e.getChildren());
			fireTreeNodesInserted(tme);

			tme = new TreeModelEvent
				(this,
				 getFkPathToRelationship((SQLRelationship) e.getSQLSource()),
				 e.getChangedIndices(),
				 e.getChildren());
			fireTreeNodesInserted(tme);
		} else {
			TreeModelEvent tme = new TreeModelEvent
				(this,
				 getPathToNode(e.getSQLSource()),
				 e.getChangedIndices(),
				 e.getChildren());
			fireTreeNodesInserted(tme);
		}
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		if (logger.isDebugEnabled()) logger.debug("dbChildrenRemoved SQLObjectEvent: "+e);
		try {
			SQLObject[] oldEventSources = e.getChildren();
			for (int i = 0; i < oldEventSources.length; i++) {
				ArchitectUtils.unlistenToHierarchy(this, oldEventSources[i]);
			}
		} catch (ArchitectException ex) {
			logger.error("Error unlistening to removed object", ex);
		}

		if (e.getSQLSource() instanceof SQLRelationship) {
			TreeModelEvent tme = new TreeModelEvent
				(this,
				 getPkPathToRelationship((SQLRelationship) e.getSQLSource()),
				 e.getChangedIndices(),
				 e.getChildren());
			fireTreeNodesRemoved(tme);

			tme = new TreeModelEvent
				(this,
				 getFkPathToRelationship((SQLRelationship) e.getSQLSource()),
				 e.getChangedIndices(),
				 e.getChildren());
			fireTreeNodesRemoved(tme);
		} else {
			TreeModelEvent tme = new TreeModelEvent
				(this,
				 getPathToNode(e.getSQLSource()),
				 e.getChangedIndices(),
				 e.getChildren());
			fireTreeNodesRemoved(tme);
		}
	}
	
	public void dbObjectChanged(SQLObjectEvent e) {
		if (logger.isDebugEnabled()) logger.debug("dbObjectChanged SQLObjectEvent: "+e);
		SQLObject source = e.getSQLSource();
		if (source instanceof SQLRelationship) {
			SQLRelationship r = (SQLRelationship) source;
			fireTreeNodesChanged(new TreeModelEvent(this, getPkPathToRelationship(r)));
			fireTreeNodesChanged(new TreeModelEvent(this, getFkPathToRelationship(r)));
		} else {
			fireTreeNodesChanged(new TreeModelEvent(this, getPathToNode(source)));
		}
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		throw new UnsupportedOperationException("not yet");
	}
}
