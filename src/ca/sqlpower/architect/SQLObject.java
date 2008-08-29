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
package ca.sqlpower.architect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

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
	 * When this counter is > 0, the fireXXX methods will ignore secondary changes.
	 */
	protected int magicDisableCount = 0;
	 
	public synchronized void setMagicEnabled(boolean enable) {
		if (magicDisableCount < 0) {
			throw new IllegalStateException("magicDisableCount < 0");
		}
		if (enable) {
			if (magicDisableCount == 0) {
				throw new IllegalArgumentException("Sorry, you asked me to enable, but disable count already 0");
				// return;
			}
			--magicDisableCount;
		} else { // disable
			++magicDisableCount;
		}
	}
	
	public boolean isMagicEnabled() {
		if (magicDisableCount > 0) {
			return false;
		}
		if (getParent() != null) {
			return getParent().isMagicEnabled();
		}
		return true;
	}
	
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
		String oldPhysicalName = this.physicalName;
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
		if (!allowsChildren()) //never return null;
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
            
            ArchitectException ex;
            if (newChild instanceof SQLExceptionNode) {

                // long term, we want to dispose of SQLExceptionNode altogether. This is a temporary workaround.
                SQLExceptionNode sen = (SQLExceptionNode) newChild;
                ex = new ArchitectException(
                        "Can't add exception node here because there are already other children. " +
                        "See exception cause for the original exception.",
                        sen.getException());
            } else {
                ex = new ArchitectException(
                        "You Can't mix SQL Object Types! You gave: " +
                        newChild.getClass().getName() +
                        "; I need " + children.get(0).getClass());
            }
			throw ex;
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
	
    /**
     * This implementation calls {@link#removeImpl(int)}.
     */
	public SQLObject removeChild(int index) {
	    return removeImpl(index);
	}

	/**
	 * This method is implemented in terms of {@link #removeImpl(int)}.
	 */
	public boolean removeChild(SQLObject child) {
		int childIdx = children.indexOf(child);
		if (childIdx >= 0) {
			removeChild(childIdx);
		}
		return childIdx >= 0;
	}

    /**
     * The implementation that all remove methods delegate to.  If you want
     * to override the behaviour of removeChild, override this method.
     */
    protected SQLObject removeImpl(int index) {
        boolean shouldProceed = fireDbChildPreRemove(index, (SQLObject) children.get(index));

        if (shouldProceed) {
            try {
                startCompoundEdit("Remove child of " + getName());
                SQLObject removedChild = (SQLObject) children.remove(index);
                if (removedChild != null) {
                    removedChild.setParent(null);
                    fireDbChildRemoved(index, removedChild);
                }
                return removedChild;
            } finally {
                endCompoundEdit("Remove child of " + getName());
            }
        } else {
            return null;
        }
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
					"firing dbChildrenInserted event");
		}
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 newIndices,
			 (SQLObject[]) newChildren.toArray(new SQLObject[newChildren.size()]));
		synchronized(sqlObjectListeners) {
		    int count = 0;
            for (SQLObjectListener l : new ArrayList<SQLObjectListener>(sqlObjectListeners)) {
				count++;
				l.dbChildrenInserted(e);
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
					"firing dbChildrenRemoved event");
			logger.debug("Removing children " + oldChildren + " from " + this);
		}
		SQLObjectEvent e = new SQLObjectEvent
			(this,
			 oldIndices,
			 (SQLObject[]) oldChildren.toArray(new SQLObject[oldChildren.size()]));
		int count =0;
		synchronized(sqlObjectListeners) {
			SQLObjectListener[] listeners = sqlObjectListeners.toArray(new SQLObjectListener[0]);
			for(int i = listeners.length-1;i>=0;i--) {
				listeners[i].dbChildrenRemoved(e);
				count++;
			}
		}
		if (logger.isDebugEnabled()) logger.debug("Notified "+count+" listeners.");
	}

	protected void fireDbChildRemoved(int oldIndex, SQLObject oldChild) {
		int[] oldIndexArray = new int[1];
		oldIndexArray[0] = oldIndex;
		List oldChildList = new ArrayList(1);
		oldChildList.add(oldChild);
		fireDbChildrenRemoved(oldIndexArray, oldChildList);
		
	}

	protected void fireDbObjectChanged(String propertyName, Object oldValue, Object newValue) {
		boolean same = (oldValue == null ? oldValue == newValue : oldValue.equals(newValue));
		if (same) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not firing property change: "+getClass().getName()+"."+propertyName+
                        " '"+oldValue+"' == '"+newValue+"'");
            }
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Firing Property Change: "+getClass().getName()+"."+propertyName+
                    " '"+oldValue+"' -> '"+newValue+"'");
		}

        SQLObjectEvent e = new SQLObjectEvent(
                this,
                propertyName,
                oldValue,
                newValue);

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
					"firing dbStructureChanged event");
		}
		SQLObjectEvent e = new SQLObjectEvent(
				this,
				null);

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

    // ------------------- sql object Pre-event support -------------------
    private final transient List<SQLObjectPreEventListener> sqlObjectPreEventListeners = 
        new ArrayList<SQLObjectPreEventListener>();

    /**
     * @return An immutable copy of the list of SQLObject pre-event listeners
     */
    public List<SQLObjectPreEventListener> getSQLObjectPreEventListeners() {
            return sqlObjectPreEventListeners;
    }
    
    public void addSQLObjectPreEventListener(SQLObjectPreEventListener l) {
        if (l == null) throw new NullPointerException("You can't add a null listener");
        synchronized(sqlObjectPreEventListeners) {
            if (sqlObjectPreEventListeners.contains(l)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("NOT Adding duplicate pre-event listener "+l+" to SQLObject "+this);
                }
                return;
            }       
            sqlObjectPreEventListeners.add(l);
        }
    }

    public void removeSQLObjectPreEventListener(SQLObjectPreEventListener l) {
        synchronized(sqlObjectPreEventListeners) {
            sqlObjectPreEventListeners.remove(l);
        }
    }

    /**
     * Fires a pre-remove event, and returns the status of whether or not the
     * operation should proceed.
     * 
     * @param oldIndices The child indices that might be removed
     * @param oldChildren The children that might be removed
     * @return  True if the operation should proceed; false if it should not. 
     */
    protected boolean fireDbChildrenPreRemove(int[] oldIndices, List oldChildren) {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getName()+" "+toString()+": " +
                    "firing dbChildrenPreRemove event");
        }
        SQLObjectPreEvent e = new SQLObjectPreEvent
            (this,
             oldIndices,
             (SQLObject[]) oldChildren.toArray(new SQLObject[oldChildren.size()]));
        int count = 0;
        synchronized (sqlObjectPreEventListeners) {
            SQLObjectPreEventListener[] listeners =
                sqlObjectPreEventListeners.toArray(new SQLObjectPreEventListener[0]);
            for (SQLObjectPreEventListener l : listeners) {
                l.dbChildrenPreRemove(e);
                count++;
            }
        }
        if (logger.isDebugEnabled()) logger.debug("Notified "+count+" listeners. Veto="+e.isVetoed());
        return !e.isVetoed();
    }

    /**
     * Convenience method for {@link #fireDbChildrenPreRemove(int[], List)} when there
     * is only one child being removed.
     * 
     * @param oldIndex The index of the child to be removed
     * @param oldChild The child to be removed
     */
    protected boolean fireDbChildPreRemove(int oldIndex, SQLObject oldChild) {
        int[] oldIndexArray = new int[1];
        oldIndexArray[0] = oldIndex;
        List oldChildList = new ArrayList(1);
        oldChildList.add(oldChild);
        return fireDbChildrenPreRemove(oldIndexArray, oldChildList);
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
	
	private void fireUndoCompoundEvent(UndoCompoundEvent e) {
		UndoCompoundEventListener[] listeners = undoEventListeners.toArray(new UndoCompoundEventListener[0]);
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
	
	public void startCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START,message));
	}
	
	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END,message));
	}

	public LinkedList<UndoCompoundEventListener> getUndoEventListeners() {
		return undoEventListeners;
	}

    /**
     * Returns the first child (in the sequence of the getChildren() list) which has the
     * given name (case sensitive).
     *  
     * @param name The name of the child to look for (case sensitive).
     * @return The first child with the given name, or null if there is no such child.
     * @throws ArchitectException If the moon is waxing gibbous.
     */
    public SQLObject getChildByName(String name) throws ArchitectException {
        return getChildByNameImpl(name, false);
    }
    
    /**
     * Returns the first child (in the sequence of the getChildren() list) which has the
     * given name (case insensitive).
     *  
     * @param name The name of the child to look for (case insensitive).
     * @return The first child with the given name, or null if there is no such child.
     * @throws ArchitectException If the moon is waxing gibbous.
     */
    public SQLObject getChildByNameIgnoreCase(String name) throws ArchitectException {
        return getChildByNameImpl(name, true);
    }
    
    /**
     * Common implementation for the two getChildByName methods.
     */
    private SQLObject getChildByNameImpl(String name, boolean ignoreCase) throws ArchitectException {
        for (SQLObject o : (List<SQLObject>) getChildren()) {
            if ( (ignoreCase && o.getName().equalsIgnoreCase(name))
                  || ( (!ignoreCase) && o.getName().equals(name)) ) {
                return o;
            }
        }
        return null;
    }
    /**
     * Returns the index of the named child, or -1 if there is no child with
     * that name.
     * 
     * @param name The name of the child to look for (case sensitive)
     * @return The index of the named child in the child list, or -1 if there
     * is no such child.
     * @throws ArchitectException if the child list can't be populated
     */
    public int getIndexOfChildByName(String name) throws ArchitectException {
        int i = 0;
        for (Object o : getChildren()) {
            SQLObject so = (SQLObject) o;
            if (so.getName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }
}