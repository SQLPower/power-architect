package ca.sqlpower.architect;

import java.util.*;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

public abstract class SQLObject implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(SQLObject.class);
	protected boolean populated = false;
	

	private String physicalName;
	private String name;
	
	/**
	 * The children of this SQLObject (if not applicable, set to
	 * Collections.EMPTY_LIST in your constructor).
	 */
	protected List children;

	/**
	 * When this mode is true, the fireXXX methods will fire events in
	 * a "secondary change" mode, which indicates that the changes are side
	 * effects of another change.  These events should still cause UI repaints
	 * and other non-permanent effects, but should not be part of an undo history
	 * and should not cause the project to get marked "dirty."
	 */
	private boolean secondaryChangeMode = false;
	
	/**
	 * This is the name of the object.  For tables, it returns the
	 * table name; for catalogs, the catalog name, and so on.
	 */
	public String getName()
	{
		return name;
	}
	
	
	/**
	 * Sets the value of sql object name
	 *
	 * @param argName Value to assign to this.name
	 */
	public void setName(String argName) {
		String oldValue = name;
		this.name = argName;
		fireDbObjectChanged("name", oldValue, name);
	}
	

	/**
	 * when the logical name is an illegal identifier in the target
     * database, generate a legal name store it here.  Some 
     * SQLObject classes do not need to implement this, so the method
     * is declared concrete, and passes through to getName() by
     * default.  SQLObject subclasses that use this idea should
     * override this class to return the physicalName and then 
     * pass through to the getName method if one is not found.
     * 
     * <p>there is no good reason why this is final, but there is no good
     * reason to override it at this time. </p>
	 */
	public final String getPhysicalName() {
		if (physicalName != null) {
			return physicalName;
		}
		return getName(); 
	}
	public final void setPhysicalName(String argName) {
		String oldPhysicalName = this.getPhysicalName();
		this.physicalName = argName;
		fireDbObjectChanged("physicalName",oldPhysicalName,argName);
	}

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
	 * Returns a short string that should be displayed to the user for
	 * representing this SQLObject as a label.
	 */
	public abstract String getShortDisplayName();

	/**
	 * Tells if this object has already been filled with children, or
	 * if that operation is still pending.
	 */
	public boolean isPopulated() {
		return populated;
	}
	
	/**
	 * Lets outside users modify the internal flag that says whether
	 * or not the list of child objects has already been loaded from
	 * the source database.  Users of this SQLObject hierarchies should
	 * not normally call this method, but it needs to be public for the
	 * SwingUIProject load implementation.
	 */
	public void setPopulated(boolean v) {
		populated = v;
	}

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
		if (!allowsChildren()) //return null;
			return children;
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
	 * All other addChild() methods call this one.  If you want to override the addChild behaviour,
	 * override this method only.
	 * 
	 * @param index The index that the new child will have
	 * @param newChild The new child to add (must be same type as all other children)
	 * @throws ArchitectException  If you try to add a child of a different type than the existing children.
	 */
	protected void addChildImpl(int index, SQLObject newChild) throws ArchitectException {
		if ( children.size() > 0 && 
				! (children.get(0).getClass().isAssignableFrom(newChild.getClass())
					|| newChild.getClass().isAssignableFrom(children.get(0).getClass()))) {
			throw new ArchitectException("You Can't mix SQL Object Types!");
		}
		children.add(index, newChild);
		newChild.setParent(this);
		fireDbChildInserted(index, newChild);
	}
	
	/**
	 * Adds the given SQLObject to this SQLObject at index. Causes a
	 * DBChildrenInserted event.  If you want to override the
	 * behaviour of addChild, override this method.
	 * @throws ArchitectException 
	 * @throws ArchitectException 
	 */
	public void addChild(int index, SQLObject newChild) throws ArchitectException {
		addChildImpl(index, newChild);
	}

	/**
	 * Adds the given SQLObject to this SQLObject at the end of the
	 * child list by calling {@link #addChild(int,SQLObject)}. Causes
	 * a DBChildrenInserted event.  If you want to override the
	 * behaviour of addChild, do not override this method.
	 * @throws ArchitectException 
	 * @throws ArchitectException 
	 * @throws Exception 
	 */
	public void addChild(SQLObject newChild) throws ArchitectException {
		addChildImpl(children.size(), newChild);
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
        // in the general case, there is nothing to do
	}

	// ------------------- sql object event support -------------------
	private final transient List<SQLObjectListener> sqlObjectListeners = 
		new LinkedList<SQLObjectListener>();

	/*
	 * @return An immutable copy of the list of SQLObject listeners
	 */
	public List<SQLObjectListener> getSQLObjectListeners() {
			return sqlObjectListeners;
	}
	
	public void addSQLObjectListener(SQLObjectListener l) {
		if (l == null) throw new NullPointerException("You can't add a null listener");
		synchronized(sqlObjectListeners) {
			if (sqlObjectListeners.contains(l)) {
				if (logger.isDebugEnabled()) {
					logger.debug("NOT Adding duplicate listener "+l+" to SQLObject "+this);
				}
				return;
			}		
			sqlObjectListeners.add(l);
		}
	}

	public void removeSQLObjectListener(SQLObjectListener l) {
		synchronized(sqlObjectListeners) {
			sqlObjectListeners.remove(l);
		}
	}

	protected void fireDbChildrenInserted(int[] newIndices, List newChildren) {
		if (logger.isDebugEnabled()) {
			logger.debug(getClass().getName()+" "+toString()+": " +
					"firing dbChildrenInserted event " +
					"(secondary = "+isSecondaryChangeMode()+")");
		}
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 newIndices,
			 (SQLObject[]) newChildren.toArray(new SQLObject[newChildren.size()]),
			 isSecondaryChangeMode());
		synchronized(sqlObjectListeners) {
			Iterator<SQLObjectListener> it = sqlObjectListeners.iterator();
			int count = 0;
			while (it.hasNext()) {
				count ++;
				SQLObjectListener nextListener = it.next();
				(nextListener).dbChildrenInserted(e);
			}
			logger.debug(getClass().getName()+": notified "+count+" listeners");
		}
	}

	protected void fireDbChildInserted(int newIndex, SQLObject newChild) {
		int[] newIndexArray = new int[1];
		newIndexArray[0] = newIndex;
		List newChildList = new ArrayList(1);
		newChildList.add(newChild);
		fireDbChildrenInserted(newIndexArray, newChildList);
	}

	protected void fireDbChildrenRemoved(int[] oldIndices, List oldChildren) {
		if (logger.isDebugEnabled()) {
			logger.debug(getClass().getName()+" "+toString()+": " +
					"firing dbChildrenRemoved event " +
					"(secondary = "+isSecondaryChangeMode()+")");
		}
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 oldIndices,
			 (SQLObject[]) oldChildren.toArray(new SQLObject[oldChildren.size()]),
			 isSecondaryChangeMode());
		synchronized(sqlObjectListeners) {
			SQLObjectListener[] listeners = sqlObjectListeners.toArray(new SQLObjectListener[0]);
			for(int i = listeners.length-1;i>=0;i--) {
				listeners[i].dbChildrenRemoved(e);
			}
		}
	}

	protected void fireDbChildRemoved(int oldIndex, SQLObject oldChild) {
		int[] oldIndexArray = new int[1];
		oldIndexArray[0] = oldIndex;
		List oldChildList = new ArrayList(1);
		oldChildList.add(oldChild);
		fireDbChildrenRemoved(oldIndexArray, oldChildList);
	}

	protected void fireDbObjectChanged(String propertyName, Object oldValue, Object newValue) {
		SQLObjectEvent e = new SQLObjectEvent(
				this,
				propertyName,
				oldValue,
				newValue,
				isSecondaryChangeMode());
		boolean same = (oldValue == null ? oldValue == newValue : oldValue.equals(newValue));
		if (same) {
			logger.debug("Object changed event aborted, the old value '"+oldValue+"' of "
					+propertyName+" equals the new value '"+newValue+"'");
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug(getClass().getName()+" "+toString()+": " +
					"firing dbObjectChanged event " +
					"(secondary = "+isSecondaryChangeMode()+")");
		}

		int count = 0;
		synchronized(sqlObjectListeners) {
			SQLObjectListener[] listeners = sqlObjectListeners.toArray(new SQLObjectListener[0]);
			for(int i = listeners.length-1;i>=0;i--) {
				count++;
				listeners[i].dbObjectChanged(e);
			}
		}
		if (logger.isDebugEnabled()) logger.debug("Notified "+count+" listeners.");
	}

	/**
	 * Notifies listeners that a major change has occurred at or under this node.
	 *
	 * <p>Note: This method is public because the PlayPen's Objects-Adder cleanup method
	 * needs to generate these notifications.  That kind of code should be in this package instead.
	 */
	public void fireDbStructureChanged() {
		if (logger.isDebugEnabled()) {
			logger.debug(getClass().getName()+" "+toString()+": " +
					"firing dbStructureChanged event " +
					"(secondary = "+isSecondaryChangeMode()+")");
		}
		SQLObjectEvent e = new SQLObjectEvent(
				this,
				null,
				isSecondaryChangeMode());

		int count = 0;
		synchronized(sqlObjectListeners) {
			SQLObjectListener[] listeners = sqlObjectListeners.toArray(new SQLObjectListener[0]);
			for(int i = listeners.length-1;i>=0;i--) {
				count++;
				listeners[i].dbStructureChanged(e);
			}
		}
		if (logger.isDebugEnabled()) logger.debug("Notified "+count+" listeners.");
	}

	public abstract Class<? extends SQLObject> getChildType();
	
	/**
	 * The list of SQLObject property change event listeners
	 * used for undo
	 */
	protected LinkedList<UndoCompoundEventListener> undoEventListeners = new LinkedList<UndoCompoundEventListener>();

	
	public void addUndoEventListener(UndoCompoundEventListener l) {
		if (undoEventListeners.contains(l)) {
			if (logger.isDebugEnabled()) {
				logger.debug("NOT Adding duplicate Undo listener "+l+" to SQLObject "+this);
			}
			return;
		}
		undoEventListeners.add(l);
	}

	public void removeUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.remove(l);
	}
	
	protected void fireUndoCompoundEvent(UndoCompoundEvent e) {
		UndoCompoundEventListener[] listeners = sqlObjectListeners.toArray(new UndoCompoundEventListener[0]);
		if (e.getType().isStartEvent()) {
			for(int i = listeners.length-1;i>=0;i--) {
				listeners[i].compoundEditStart(e);
			}
		} else {
			for(int i = listeners.length-1;i>=0;i--) {
				listeners[i].compoundEditEnd(e);
			}
		} 
		
	}

	public LinkedList<UndoCompoundEventListener> getUndoEventListeners() {
		return undoEventListeners;
	}


	public boolean isSecondaryChangeMode() {
		return secondaryChangeMode;
	}


	public void setSecondaryChangeMode(boolean secondaryChangeMode) {
		this.secondaryChangeMode = secondaryChangeMode;
	}

}
