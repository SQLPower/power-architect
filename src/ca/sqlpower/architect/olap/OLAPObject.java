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

package ca.sqlpower.architect.olap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class OLAPObject {

    private static final Logger logger = Logger.getLogger(OLAPObject.class);
    
    /**
     * Helper class for registering and notifying property change listeners.
     */
    protected final PropertyChangeSupport pcs;

    private final List<OLAPChildListener> childListeners = new ArrayList<OLAPChildListener>();
    
    /**
     * The number of compound edits that have started on this object but not yet
     * finished.
     */
    private int compoundEditDepth = 0;

    /**
     * The olap object this object belongs to. This pointer is maintained by the
     * parent's addChild method(s).
     */
    private OLAPObject parent;

    /**
     * All compound edit listeners registered on this object.
     */
    private final List<CompoundEditListener> compoundEditListeners = new ArrayList<CompoundEditListener>();
    
    protected OLAPObject() {
        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Returns the current OLAPObject that is this object's parent. If this
     * value is null, this object has no parent.
     */
    public OLAPObject getParent() {
        return parent;
    }

    /**
     * Returns true if this type of OLAPObject can ever return a non-empty list from
     * {@link #getChildren()}, and false if getChildren() is always empty.
     */
    public abstract boolean allowsChildren();
    
    /**
     * Returns a read-only unified list of all children of this OLAPObject.
     * If this object doesn't have any children, returns an empty list.
     */
    public abstract List<? extends OLAPObject> getChildren();

    /**
     * Adds the given child or sets the appropriate property on this OLAPObject,
     * if this type of OLAPObject has the applicable addXXX() or setXXX() method
     * for the given object's type.
     * <p>
     * This method in the abstract base class always throws the IllegalArgumentException
     * because no children or properties are defined at this level.
     * <p>
     * This method isn't compile-time type safe, so it is recommended not to use
     * it on "by-hand" usage of this API. This method is used during XML parsing,
     * which can't be made compile-time type safe anyway.
     * 
     * @param child
     *            The child to add.
     * @throws IllegalArgumentException
     *             if this object doesn't support child's type.
     */
    public void addChild(OLAPObject child) {
        throw new IllegalArgumentException(getClass().getName() + " doesn't allow children of type " + child.getClass());
    }

    /**
     * Adds the given child or sets the appropriate property on this OLAPObject,
     * allowing the overall target index to be specified.
     * <p>
     * This method in the abstract base class always throws
     * IllegalArgumentException because no children or properties are defined at
     * this level.
     * <p>
     * This method isn't compile-time type safe, so it is recommended not to use
     * it on "by-hand" usage of this API. This method is used for undo and redo,
     * which can't be compile-time type safe anyway.
     * 
     * @param index
     *            The overall index (same index as would be in
     *            {@link #getChildren()}) at which to add the child.
     * @param child
     *            The child to add.
     * @throws IllegalArgumentException
     *             if this object doesn't support child's type, or if the given
     *             index is not compatible with the given child's type (children
     *             are grouped by type, and the order is strictly enforced).
     */
    public void addChild(int index, OLAPObject child) {
        throw new IllegalArgumentException(getClass().getName() + " doesn't allow children of type " + child.getClass());
    }

    /**
     * Removes the given child on this OLAPObject if this type of OLAPObject has
     * the applicable removeXXX() or setXXX(null) method for the given object's type.
     * <p>
     * This method in the abstract base class always throws the IllegalArgumentException
     * because no children or properties are defined at this level.
     * 
     * @param child
     *             The child to remove.
     * @throws IllegalArgumentException
     *             if this object doesn't support child's type.
     */
    public boolean removeChild(OLAPObject child) {
        throw new IllegalArgumentException(getClass().getName() + " doesn't allow children of type " + child.getClass());
    }
    
    /**
     * Changes this OLAPObject's parent reference. It is the parent's
     * responsibility to manage the reference, so this method is
     * package-private.
     * <p>
     * The parent will call this method <i>after</i> the child has been added
     * or removed on the appropriate child list.
     * 
     * @param parent
     *            The new parent for this object. If the object is being removed
     *            from its former parent, this argument should be null.
     */
    void setParent(OLAPObject parent) {
        this.parent = parent;
    }
    
    public void addChildListener(OLAPChildListener listener) {
        childListeners.add(listener);
    }

    public void removeChildListener(OLAPChildListener listener) {
        childListeners.remove(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addCompoundEditListener(CompoundEditListener listener) {
        compoundEditListeners.add(listener);
    }

    public void removeCompoundEditListener(CompoundEditListener listener) {
        compoundEditListeners.remove(listener);
    }

    public void startCompoundEdit(String presentationName) {
        compoundEditDepth++;
        logger.debug("Compound edit on " + getClass().getSimpleName() + " starting. Depth=" + compoundEditDepth);
        if (compoundEditDepth == 1) {
            logger.debug("Firing compoundEditStarted to " + compoundEditListeners.size() + " listeners...");
            CompoundEditEvent evt = new CompoundEditEvent(this, presentationName);
            for (int i = compoundEditListeners.size() - 1; i >= 0; i--) {
                compoundEditListeners.get(i).compoundEditStarted(evt);
            }
        }
    }
    
    public void endCompoundEdit() {
        logger.debug("Compound edit on " + getClass().getSimpleName() + " ending. Depth=" + compoundEditDepth);
        if (compoundEditDepth <= 0) {
            throw new IllegalStateException("Compound edit depth is already " + compoundEditDepth);
        }
        compoundEditDepth--;
        if (compoundEditDepth == 0) {
            logger.debug("Firing compoundEditEnded to " + compoundEditListeners.size() + " listeners...");
            CompoundEditEvent evt = new CompoundEditEvent(this);
            for (int i = compoundEditListeners.size() - 1; i >= 0; i--) {
                compoundEditListeners.get(i).compoundEditEnded(evt);
            }
        }
    }
    
    protected void fireChildAdded(Class<? extends OLAPObject> childClass, int index, OLAPObject child) {
        OLAPChildEvent e = new OLAPChildEvent(this, childClass, child, index);
        if (logger.isDebugEnabled()) {
            logger.debug("Firing childAdded event " + e + " to " + childListeners.size() + " listeners");
        }
        for (int i = childListeners.size() - 1; i >= 0; i--) {
            childListeners.get(i).olapChildAdded(e);
        }
        logger.debug("Done firing childAdded event");
    }

    protected void fireChildRemoved(Class<? extends OLAPObject> childClass, int index, OLAPObject child) {
        OLAPChildEvent e = new OLAPChildEvent(this, childClass, child, index);
        if (logger.isDebugEnabled()) {
            logger.debug("Firing childRemoved event " + e + " to " + childListeners.size() + " listeners");
        }
        for (int i = childListeners.size() - 1; i >= 0; i--) {
            childListeners.get(i).olapChildRemoved(e);
        }
        logger.debug("Done firing childRemoved event");
    }
    
    /**
     * Default implementation. Subclasses that have actual names will override this
     * method.
     */
    public String getName() {
        String name = getClass().getName();
        name = name.substring(name.lastIndexOf('.'));
        return name + "@" + Integer.toHexString(System.identityHashCode(this));
    }
}
