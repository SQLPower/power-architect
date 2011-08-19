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

package ca.sqlpower.architect.olap.undo;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.CompoundEditEvent;
import ca.sqlpower.architect.olap.CompoundEditListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.undo.NotifyingUndoManager;
import ca.sqlpower.object.undo.PropertyChangeEdit;
import ca.sqlpower.util.TransactionEvent;

/**
 * A customized undo manager that works well with the OLAP business model and GUI.
 */
public class OLAPUndoManager extends UndoManager implements NotifyingUndoManager {

    private static final Logger logger = Logger.getLogger(OLAPUndoManager.class);
    
    private final UndoableEventHandler eventHandler = new UndoableEventHandler();
    
    private boolean undoing = false;
    
    /**
     * Will be non-null when this undo manager is in the middle of receiving a
     * series of edits that are supposed to be undone and redone as one step.
     */
    private CompoundEdit currentCompoundEdit;
    
    /**
     * The number of times we've started a compound edit minus the number of
     * times we've finished one. If greater than 0, we are in a compound edit
     * and {@link #currentCompoundEdit} will be non-null.
     * <p>
     * This value will never be less than 0.
     */
    private int compoundEditDepth = 0;
    
    private WeakReference<UndoableEdit> rememberedPosition;
    
    public OLAPUndoManager(OLAPObject root) {
        OLAPUtil.listenToHierarchy(root, eventHandler, eventHandler);
        // TODO need to track the playpen components as well
    }
    
    /**
     * Marks this undo manager's current position.
     */
    public void rememberPosition() {
        // this might be a weak reference to null, for instance if there are no edits yet
        // (but that's ok; it will still work)
        rememberedPosition = new WeakReference<UndoableEdit>(editToBeUndone());
        fireChangeEvent();
    }
    
    public boolean isAtRememberedPosition() {
        return rememberedPosition != null && rememberedPosition.get() == editToBeUndone();
    }

    /**
     * This override is just a hook for the "remembered position" system. If we
     * are trimming off the first edit, and our remembered undoable edit was
     * null (which only happens when the remembered position is before the first
     * edit), we "unremember" the position because it is now impossible to get
     * back there. If we didn't do this, if the user undoes back to the
     * beginning of the undo vector, we would report that we're at the
     * remembered position, but it would be a lie.
     */
    @Override
    protected void trimEdits(int from, int to) {
        super.trimEdits(from, to);
        if (from == 0 && to > 0 && rememberedPosition.get() == null) {
            rememberedPosition = null;
        }
    }
    
    @Override
    protected void undoTo(UndoableEdit edit) throws CannotUndoException {
        try {
            undoing = true;
            logger.debug("Undoing to " + edit + "...");
            super.undoTo(edit);
            logger.debug("Finished undoing. canUndo=" + canUndo() + " canRedo=" + canRedo());
        } finally {
            undoing = false;
        }
        fireChangeEvent();
    }

    @Override
    protected void redoTo(UndoableEdit edit) throws CannotRedoException {
        try {
            undoing = true;
            super.redoTo(edit);
        } finally {
            undoing = false;
        }
        fireChangeEvent();
    }

    @Override
    public synchronized boolean addEdit(UndoableEdit anEdit) {
        if (!undoing) {
            boolean added = super.addEdit(anEdit);
            logger.debug("Added new edit: " + anEdit);
            fireChangeEvent();
            return added;
        } else {
            // Ignore event but still return true because everything is copasetic
            return true;
        }
    }
    
    public void startCompoundEdit(final String presentationName) {
        compoundEditDepth++;
        logger.debug("Starting compound edit. My depth: " + compoundEditDepth);
        if (compoundEditDepth == 1) {
            logger.debug("Beginning compound edit \"" + presentationName + "\"");
            currentCompoundEdit = new CompoundEdit() {
                @Override
                public String getPresentationName() {
                    return presentationName;
                }
            };
            addEdit(currentCompoundEdit);
        }
    }

    public void endCompoundEdit() {
        logger.debug("Ending compound event. My depth: " + compoundEditDepth);
        if (currentCompoundEdit == null || compoundEditDepth <= 0) {
            throw new IllegalStateException(
                    "Received a compoundEditEnded but was not " +
                    "in a compound edit state: depth=" + compoundEditDepth +
                    "; currentEdit=" + currentCompoundEdit);
        }
        compoundEditDepth--;
        if (compoundEditDepth == 0) {
            logger.debug("Ending compound edit \"" + currentCompoundEdit.getPresentationName() + "\"");
            currentCompoundEdit.end();
            currentCompoundEdit = null;
            fireChangeEvent();
        }
    }
    
    private class UndoableEventHandler implements SPListener, CompoundEditListener {

        public void childAdded(SPChildEvent e) {
            addEdit(new OLAPChildEdit(e, false));
            OLAPUtil.listenToHierarchy((OLAPObject) e.getChild(), this, this);
        }

        public void childRemoved(SPChildEvent e) {
            addEdit(new OLAPChildEdit(e, true));
            OLAPUtil.unlistenToHierarchy((OLAPObject) e.getChild(), this, this);
        }

        public void propertyChanged(PropertyChangeEvent e) {
            addEdit(new PropertyChangeEdit(e));
        }

        public void compoundEditStarted(CompoundEditEvent evt) {
            startCompoundEdit(evt.getPresentationName());
        }

        public void compoundEditEnded(CompoundEditEvent evt) {
            endCompoundEdit();
        }

        public void transactionEnded(TransactionEvent e) {
            //no-op            
        }

        public void transactionRollback(TransactionEvent e) {
            //no-op            
        }

        public void transactionStarted(TransactionEvent e) {
            //no-op            
        }
        
    }
    
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }
    
    private void fireChangeEvent() {
        ChangeEvent e = new ChangeEvent(this);
        for (int i = changeListeners.size() - 1; i >= 0; i--) {
            changeListeners.get(i).stateChanged(e);
        }
    }
}
