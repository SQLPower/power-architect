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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.TransactionEvent;

public abstract class OLAPObject extends AbstractSPObject {

    private static final Logger logger = Logger.getLogger(OLAPObject.class);
    
    /**
     * The number of compound edits that have started on this object but not yet
     * finished.
     */
    private int compoundEditDepth = 0;

    /**
     * All compound edit listeners registered on this object.
     */
    private final List<CompoundEditListener> compoundEditListeners = new ArrayList<CompoundEditListener>();
    
    protected OLAPObject() {
    }
    
    public void addChild(SPObject child) {
        addChild(child, getChildren(child.getClass()).size());
    }
    
    protected OLAPObject(OLAPObject original) {
        setParent(original.getParent());
    }
    
    public int childPositionOffset(Class<? extends SPObject> childType) {
        throw new IllegalArgumentException("Invalid child type " + childType);
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        throw new IllegalArgumentException("Cannot add child " + child);
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        throw new IllegalArgumentException("Cannot remove child " + child);
    }
    
    public void addCompoundEditListener(CompoundEditListener listener) {
        compoundEditListeners.add(listener);
    }

    public void removeCompoundEditListener(CompoundEditListener listener) {
        compoundEditListeners.remove(listener);
    }

    @Override
    protected TransactionEvent fireTransactionStarted(String message) {
        compoundEditDepth++;
        logger.debug("Compound edit on " + getClass().getSimpleName() + " starting. Depth=" + compoundEditDepth);
        if (compoundEditDepth == 1) {
            logger.debug("Firing compoundEditStarted to " + compoundEditListeners.size() + " listeners...");
            CompoundEditEvent evt = new CompoundEditEvent(this, message);
            for (int i = compoundEditListeners.size() - 1; i >= 0; i--) {
                compoundEditListeners.get(i).compoundEditStarted(evt);
            }
        }
        return super.fireTransactionStarted(message);
    }

    @Override
    protected TransactionEvent fireTransactionEnded() {
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
        return super.fireTransactionEnded();
    }
    
    //XXX What happens on a rollback?? There is no rollback on the listeners at current
    //TODO Add in the ability to rollback or cancel a compound edit.
}
