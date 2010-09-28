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

package ca.sqlpower.architect.undo;

import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotUndoException;

public interface NotifyingUndoManager {

    /**
     * Adds the given change listener, which will be notified with a ChangeEvent
     * every time this undo manager's state has changed. The manager's state
     * generally changes when an undoable edit is added, or an undo or redo
     * operation is performed.
     */
    public void addChangeListener(ChangeListener l);

    /**
     * Removes the given change listener, so it will no longer be notified when
     * this manager's state changes.
     */
    public void removeChangeListener(ChangeListener l);

    
    // ----------- All methods below simply specify UndoManager methods -------------
    
    public void undo() throws CannotUndoException;
    public void redo() throws CannotUndoException;
    
    public boolean canUndo();
    public boolean canRedo();
    
    public String getUndoPresentationName();
    public String getRedoPresentationName();
    public String getUndoOrRedoPresentationName();
}
