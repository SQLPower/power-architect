package ca.sqlpower.architect;

import java.util.*;
import org.apache.log4j.Logger;

public abstract class SQLObject implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(SQLObject.class);

	/**
	 * The children of this SQLObject (if not applicable, set to
	 * Collections.EMPTY_LIST in your constructor).
	 */
	protected List children;

	/**
	 * This is the name of the object.  For tables, it returns the
	 * table name; for catalogs, the catalog name, and so on.
	 */
	public abstract String getName();

	/**
	 * Returns the parent of this SQLObject or <code>null</code> if it
	 * is a root object such as SQLDatabase.
	 */
	public abstract SQLObject getParent();

	/**
	 * Parents call this on their children to update parent pointers
	 * during addChild and removeChild requests.
	 */
	protected abstract void setParent(SQLObject parent);

	/**
	 * Causes this SQLObject to load its children (if any exist).
	 * This method will be called lots of times, so track whether or
	 * not you need to do anything and return right away whenever
	 * possible.
	 */
	protected abstract void populate() throws ArchitectException;

	/**
	 * Must return false if and only if <code>populate()</code> still needs to be called.
	 */
	public abstract boolean isPopulated();

	/**
	 * Returns a short string that should be displayed to the user for
	 * representing this SQLObject as a label.
	 */
	public abstract String getShortDisplayName();

	/**
	 * Returns true if and only if this object can have child
	 * SQLObjects.  Your implementation of this method <b>must not</b>
	 * cause JDBC activity, or the lazy loading properties of your
	 * SQLObjects will be wasted!  Typically, you will implement this
	 * with a hardcoded "<code>return true</code>" or 
	 * "<code>return false</code>" depending on object type.
	 */
	public abstract boolean allowsChildren();

	/**
	 * Returns an unmodifiable view of the child list.  All list
	 * members will be SQLObject subclasses (SQLTable,
	 * SQLRelationship, SQLColumn, etc.) which are directly contained
	 * within this SQLObject.
	 */
	public List getChildren() throws ArchitectException {
		if (!allowsChildren()) return null;
		populate();
		return Collections.unmodifiableList(children);
	}

	/**
	 * Replaces the children of this SQLObject with those in the given
	 * list.
	 *
	 * <p>XXX: this is a very inefficient implementation!
	 *
	 * @param newChildren A List of SQLObject objects.
	 */
	public void setChildren(List newChildren) throws ArchitectException {
		if (!allowsChildren()) return;
		while (getChildCount() > 0) {
			removeChild(0);
		}
		Iterator it = newChildren.iterator();
		while (it.hasNext()) {
			addChild((SQLObject) it.next());
		}
	}
	
	public SQLObject getChild(int index) throws ArchitectException {
		populate();
		return (SQLObject) children.get(index);
	}

	public int getChildCount() throws ArchitectException {
		populate();
		return children.size();
	}

	/**
	 * Adds the given SQLObject to this SQLObject at index. Causes a
	 * DBChildrenInserted event.  If you want to override the
	 * behaviour of addChild, override this method.
	 */
	public void addChild(int index, SQLObject newChild) {
		children.add(index, newChild);
		newChild.setParent(this);
		fireDbChildInserted(index, newChild);
	}

	/**
	 * Adds the given SQLObject to this SQLObject at the end of the
	 * child list by calling {@link #addChild(int,SQLObject)}. Causes
	 * a DBChildrenInserted event.  If you want to override the
	 * behaviour of addChild, do not override this method.
	 */
	public void addChild(SQLObject newChild) {
		addChild(children.size(), newChild);
	}
	
	public SQLObject removeChild(int index) {
		SQLObject removedChild = (SQLObject) children.remove(index);
		if (removedChild != null) {
			removedChild.removeDependencies();
			removedChild.setParent(null);
			fireDbChildRemoved(index, removedChild);
		}
		return removedChild;
	}

	/**
	 * This method is implemented in terms of {@link #removeChild(int)}.
	 */
	public boolean removeChild(SQLObject child) {
		int childIdx = children.indexOf(child);
		if (childIdx >= 0) {
			removeChild(childIdx);
		}
		return childIdx >= 0;
	}

	/**
	 * Override this method if your SQLObject has cross-dependant
	 * children (such as relationships between tables) that have to be
	 * specifically removed when your object is removed from its
	 * parent.  It is not necessary to remove direct children (like
	 * columns of tables), but it is necessary to remove cross-linking
	 * children as mentioned above.
	 */
	public void removeDependencies() {
	}

	// ------------------- sql object event support -------------------
	private transient List sqlObjectListeners = new LinkedList();

	public List getSQLObjectListeners() {
		if (sqlObjectListeners == null) {
			sqlObjectListeners = new LinkedList();
		}
		return sqlObjectListeners;
	}

	public void addSQLObjectListener(SQLObjectListener l) {
		if (getSQLObjectListeners().contains(l)) {
			logger.warn("NOT Adding duplicate listener "+l+" to SQLObject "+this);
			return;
		}
		getSQLObjectListeners().add(l);
	}

	public void removeSQLObjectListener(SQLObjectListener l) {
		getSQLObjectListeners().remove(l);
	}

	protected void fireDbChildrenInserted(int[] newIndices, List newChildren) {
		logger.debug(getClass().getName()+": firing dbChildrenInserted event");
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 newIndices,
			 (SQLObject[]) newChildren.toArray(new SQLObject[newChildren.size()]));
		Iterator it = getSQLObjectListeners().iterator();
		int count = 0;
		while (it.hasNext()) {
			count ++;
			((SQLObjectListener) it.next()).dbChildrenInserted(e);
		}
		logger.debug(getClass().getName()+": notified "+count+" listeners");
	}

	protected void fireDbChildInserted(int newIndex, SQLObject newChild) {
		int[] newIndexArray = new int[1];
		newIndexArray[0] = newIndex;
		List newChildList = new ArrayList(1);
		newChildList.add(newChild);
		fireDbChildrenInserted(newIndexArray, newChildList);
	}

	protected void fireDbChildrenRemoved(int[] oldIndices, List oldChildren) {
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 oldIndices,
			 (SQLObject[]) oldChildren.toArray(new SQLObject[oldChildren.size()]));
		Iterator it = getSQLObjectListeners().iterator();
		while (it.hasNext()) {
			((SQLObjectListener) it.next()).dbChildrenRemoved(e);
		}
	}

	protected void fireDbChildRemoved(int oldIndex, SQLObject oldChild) {
		int[] oldIndexArray = new int[1];
		oldIndexArray[0] = oldIndex;
		List oldChildList = new ArrayList(1);
		oldChildList.add(oldChild);
		fireDbChildrenRemoved(oldIndexArray, oldChildList);
	}

	protected void fireDbObjectChanged(String propertyName) {
		SQLObjectEvent e = new SQLObjectEvent(this, propertyName);

		if (logger.isDebugEnabled()) {
			logger.debug("Sending dbObjectChanged event "+e+" to "
						 +getSQLObjectListeners().size()+" listeners: "
						 +getSQLObjectListeners());
		}

		Iterator it = getSQLObjectListeners().iterator();
		while (it.hasNext()) {
			((SQLObjectListener) it.next()).dbObjectChanged(e);
		}
		
	}

	protected void fireDbStructureChanged(String propertyName) {
		SQLObjectEvent e = new SQLObjectEvent(this, propertyName);
		Iterator it = getSQLObjectListeners().iterator();
		while (it.hasNext()) {
			((SQLObjectListener) it.next()).dbStructureChanged(e);
		}
		
	}
}
