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

package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import ca.sqlpower.architect.undo.ArchitectUndoManager;
import ca.sqlpower.object.undo.PropertyChangeEdit;

/**
 * An undoable edit that absorbs PropertyChangeEdits from PlayPenComponents. The
 * way it is used is to add it to the undo manager using
 * {@link ArchitectUndoManager#addEdit(UndoableEdit)} before any components start moving.
 * Then as each successive {@link PropertyChangeEdit} is added to the undo
 * manager, this edit will absorb it if it represents a change in location of a
 * PlayPenComponent. As soon as any other type of edit is added to the undo
 * manager, this edit will refuse to absorb it, and new edits will be
 * accumulated by the undo manager as usual.
 */
public class PlayPenComponentLocationEdit extends AbstractUndoableEdit {

    private final Map<PlayPenComponent, Point> initialBounds =
        new HashMap<PlayPenComponent, Point>();

    private final Map<PlayPenComponent, Point> finalBounds =
        new HashMap<PlayPenComponent, Point>();
    
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (anEdit instanceof PropertyChangeEdit) {

            PropertyChangeEdit pce = (PropertyChangeEdit) anEdit;
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    pce.getSource(), pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
            
            if (pce.getPropertyName().equals("topLeftCorner")
                    && pce.getSource() instanceof PlayPenComponent) {
                PlayPenComponent ppc = (PlayPenComponent) pce.getSource();
                if (!initialBounds.containsKey(pce.getSource())) {
                    initialBounds.put(ppc, new Point((Point) pce.getOldValue()));
                }
                finalBounds.put(ppc, new Point((Point) pce.getNewValue()));
                anEdit.die();
                return true;
            }
            
        }
        return false;
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for (Map.Entry<PlayPenComponent, Point> entry : initialBounds.entrySet()) {
            entry.getKey().setTopLeftCorner(entry.getValue());
        }
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        for (Map.Entry<PlayPenComponent, Point> entry : finalBounds.entrySet()) {
            entry.getKey().setTopLeftCorner(entry.getValue());
        }
    }
    
    @Override
    public String getUndoPresentationName() {
        return "Undo moving " + initialBounds.size() + " objects";
    }

    @Override
    public String getRedoPresentationName() {
        return "Redo moving " + finalBounds.size() + " objects";
    }
    
    @Override
    public boolean isSignificant() {
        return finalBounds.size() > 0;
    }
}
