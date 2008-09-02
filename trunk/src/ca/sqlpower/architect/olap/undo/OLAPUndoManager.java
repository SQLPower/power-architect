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
import java.beans.PropertyChangeListener;
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
import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.undo.NotifyingUndoManager;
import ca.sqlpower.architect.undo.PropertyChangeEdit;

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
    
    public OLAPUndoManager(OLAPObject root) {
        OLAPUtil.listenToHierarchy(root, eventHandler, eventHandler, eventHandler);
        // TODO need to track the playpen components as well
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
    
    private class UndoableEventHandler implements OLAPChildListener, PropertyChangeListener, CompoundEditListener {

        public void olapChildAdded(OLAPChildEvent e) {
            addEdit(new OLAPChildEdit(e, false));
            OLAPUtil.listenToHierarchy(e.getChild(), this, this, this);
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            addEdit(new OLAPChildEdit(e, true));
            OLAPUtil.unlistenToHierarchy(e.getChild(), this, this, this);
        }

        public void propertyChange(PropertyChangeEvent e) {
            addEdit(new PropertyChangeEdit(e));
        }

        public void compoundEditStarted(CompoundEditEvent evt) {
            compoundEditDepth++;
            logger.debug("Got start compound event. My depth: " + compoundEditDepth);
            if (compoundEditDepth == 1) {
                final String presentationName = evt.getPresentationName();
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
        
        public void compoundEditEnded(CompoundEditEvent evt) {
            logger.debug("Got end compound event. My depth: " + compoundEditDepth);
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
