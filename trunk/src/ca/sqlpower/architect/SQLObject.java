package ca.sqlpower.architect;

import java.util.*;

public abstract class SQLObject implements java.io.Serializable {

	/**
	 * The children of this SQLObject (if not applicable, set to
	 * Collections.EMPTY_LIST in your constructor).
	 */
	protected List children;

	/**
	 * Returns the parent of this SQLObject or <code>null</code> if it
	 * is a root object such as SQLDatabase.
	 */
	public abstract SQLObject getParent();

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
		populate();
		return Collections.unmodifiableList(children);
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
	 * Adds the given SQLObject to this SQLObject at index. Causes a DBChildrenInserted event.
	 */
	public void addChild(int index, SQLObject newChild) {
		children.add(index, newChild);
		fireDbChildInserted(index, newChild);
	}

	/**
	 * Adds the given SQLObject to this SQLObject at the end of the
	 * child list. Causes a DBChildrenInserted event.
	 */
	public void addChild(SQLObject newChild) {
		children.add(newChild);
		fireDbChildInserted(children.size() - 1, newChild);
	}
	
	public SQLObject removeChild(int index) {
		SQLObject removedChild = (SQLObject) children.remove(index);
		if (removedChild != null) fireDbChildRemoved(index, removedChild);
		return removedChild;
	}

	public boolean removeChild(SQLObject child) {
		int childIdx = children.indexOf(child);
		if (childIdx >= 0) {
			children.remove(childIdx);
			fireDbChildRemoved(childIdx, child);
		}
		return childIdx >= 0;
	}

	// ------------------- sql object event support -------------------
	private transient List sqlObjectListeners = new LinkedList();

	private List getSqlObjectListeners() {
		if (sqlObjectListeners == null) {
			sqlObjectListeners = new LinkedList();
		}
		return sqlObjectListeners;
	}

	public void addSQLObjectListener(SQLObjectListener l) {
		getSqlObjectListeners().add(l);
	}

	public void removeSQLObjectListener(SQLObjectListener l) {
		getSqlObjectListeners().remove(l);
	}

	protected void fireDbChildrenInserted(int[] newIndices, List newChildren) {
		System.out.println(getClass().getName()+": firing dbChildrenInserted event");
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 newIndices,
			 (SQLObject[]) newChildren.toArray(new SQLObject[newChildren.size()]));
		Iterator it = getSqlObjectListeners().iterator();
		int count = 0;
		while (it.hasNext()) {
			count ++;
			((SQLObjectListener) it.next()).dbChildrenInserted(e);
		}
		System.out.println(getClass().getName()+": notified "+count+" listeners");
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
		Iterator it = getSqlObjectListeners().iterator();
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
		Iterator it = getSqlObjectListeners().iterator();
		while (it.hasNext()) {
			((SQLObjectListener) it.next()).dbObjectChanged(e);
		}
		
	}
}
