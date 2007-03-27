package ca.sqlpower.architect.swingui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLExceptionNode;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.profile.ProfileChangeEvent;
import ca.sqlpower.architect.profile.ProfileChangeListener;
import ca.sqlpower.architect.profile.ProfileResult;

public class DBTreeModel implements TreeModel, SQLObjectListener, java.io.Serializable {

	private static Logger logger = Logger.getLogger(DBTreeModel.class);

    /**
     * Controls this model's "testing" mode.  When in testing mode,
     * the checks for whether or not events are on the Swing Event Dispatch
     * Thread are bypassed.
     */
    private boolean testMode = false;
    
	protected SQLObject root;
	
    /**
     * The session so we can get profile results and tell when they changed
     */
    private final ArchitectSession session;
	/**
	 * Creates a tree model with an empty list of databases at its
	 * root.
	 */
	public DBTreeModel(ArchitectSession session) throws ArchitectException {
		this(null,session);
	}

	/**
	 * Creates a tree model with all of the SQLDatabase objects in the
	 * given collection in its root list of databases.
	 *
	 * @param initialDatabases A collection whose items are all
	 * distinct SQLDatabase objects.
	 */
	public DBTreeModel(Collection<SQLDatabase> initialDatabases,ArchitectSession session) throws ArchitectException {
		this.root = new DBTreeRoot();
        this.session = session;
		if (initialDatabases != null) {
			Iterator<SQLDatabase> it = initialDatabases.iterator();
			while (it.hasNext()) {
				root.addChild(it.next());
			}
		}
		this.treeModelListeners = new LinkedList();
		ArchitectUtils.listenToHierarchy(this, root);
        session.getProfileManager().addProfileChangeListener(new ProfileChangeListener(){

            public void profileListChanged(ProfileChangeEvent event) {
                //This will not change the status of the profiles so ignore it
            }

            /**
             *  Note this will usually not be run from the event thread
             */
            
            public void profilesAdded(ProfileChangeEvent e) {
                for (ProfileResult pr : e.getProfileResult()) {
                    if (logger.isDebugEnabled()) logger.debug("Removing profile "+pr);
                    SQLObjectEvent soe = new SQLObjectEvent(pr.getProfiledObject(),"profile");
                    processSQLObjectChanged(soe);
                }
            }

            public void profilesRemoved(ProfileChangeEvent e) {
                for (ProfileResult pr : e.getProfileResult()) {
                    if (logger.isDebugEnabled()) logger.debug("Removing profile "+pr);
                    SQLObjectEvent soe = new SQLObjectEvent(pr.getProfiledObject(),"profile");
                    processSQLObjectChanged(soe);
                }
            }
            
        });
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
			putExceptionNodeUnder((SQLObject) parent, e);
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
		// TODO FIXME XXX Replace this with an alternate method leads to nasty behavior.  There are 3 others too
			notifier.run();
		
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
		logger.debug("firing TreeStructuredChanged. source="+e.getSource());
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
			for(int i=0; i< parent.getChildCount(); i++){
				parent.removeChild(0);
			}
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
            logger.debug("dbChildrenInserted. source="+e.getSource()
                    +" indices: "+Arrays.asList(e.getChangedIndices())
                    +" children: "+Arrays.asList(e.getChildren()));
        }
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

        if ((!SwingUtilities.isEventDispatchThread()) && (!testMode)) {
            logger.debug("Not refiring because this is not the EDT.");
            return;
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
        if (logger.isDebugEnabled()) {
            logger.debug("dbchildrenremoved. source="+e.getSource()
                    +" indices: "+Arrays.asList(e.getChangedIndices())
                    +" children: "+Arrays.asList(e.getChildren()));
        }
		if (logger.isDebugEnabled()) logger.debug("dbChildrenRemoved SQLObjectEvent: "+e);
		try {
			SQLObject[] oldEventSources = e.getChildren();
			for (int i = 0; i < oldEventSources.length; i++) {
				ArchitectUtils.unlistenToHierarchy(this, oldEventSources[i]);
			}
		} catch (ArchitectException ex) {
			logger.error("Error unlistening to removed object", ex);
		}

        if ((!SwingUtilities.isEventDispatchThread()) && (!testMode)) {
            logger.debug("Not refiring because this is not the EDT.");
            return;
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
		logger.debug("dbObjectChanged. source="+e.getSource());
        if ((!SwingUtilities.isEventDispatchThread()) && (!testMode)) {
            logger.debug("Not refiring because this is not the EDT.");
            return;
        }
		if (logger.isDebugEnabled()) logger.debug("dbObjectChanged SQLObjectEvent: "+e);
		processSQLObjectChanged(e);
	}

    /**
     * The profile manager needs to fire events from different threads.
     * So we need to get around the check.
     */
    private void processSQLObjectChanged(SQLObjectEvent e) {
        if (e.getPropertyName().equals("name") && 
				!e.getNewValue().equals(e.getSQLSource().getName()) ) {
			logger.error("Name change event has wrong new value. new="+e.getNewValue()+"; real="+e.getSQLSource().getName());
		}
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
		logger.debug("dbStructureChanged. source="+e.getSource());
		try {			
			ArchitectUtils.listenToHierarchy(this, e.getSQLSource());
		} catch (ArchitectException ex) {
			logger.error("Couldn't listen to hierarchy rooted at "+e.getSQLSource(), ex);
		}
        if ((!SwingUtilities.isEventDispatchThread()) && (!testMode)) {
            logger.debug("Not refiring because this is not the EDT.");
            return;
        }
		TreeModelEvent tme = new TreeModelEvent(this, getPathToNode(e.getSQLSource()));
		fireTreeStructureChanged(tme);
	}
    
    /**
     * Sets the {@link #testMode} flag.
     */
    public void setTestMode(boolean v) {
        testMode = v;
    }
}
