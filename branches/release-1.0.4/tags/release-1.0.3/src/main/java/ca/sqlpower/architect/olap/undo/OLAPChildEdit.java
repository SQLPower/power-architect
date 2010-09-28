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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.sqlpower.object.SPChildEvent;

public class OLAPChildEdit extends AbstractUndoableEdit {

    /**
     * The event that this edit encapsulates. It may represent either an
     * addition or removal of a child.
     */
    private final SPChildEvent event;

    /**
     * Indicates whether the event in this edit represents an initial removal or
     * addition of the child. This influences the behaviour of undo() and redo().
     */
    private final boolean removal;

    /**
     * Creates a new edit based on the given event.
     * 
     * @param event The event that represents a child add or remove operation. This
     * event must be properly formed
     * @param removal
     */
    public OLAPChildEdit(SPChildEvent event, boolean removal) {
        if (event == null) {
            throw new NullPointerException("Null event");
        }
        if (event.getSource() == null) {
            throw new NullPointerException("Null source");
        }
        if (event.getChild() == null) {
            throw new NullPointerException("Null child");
        }
        if (event.getIndex() < 0 || event.getIndex() > event.getSource().getChildren().size()) {
            throw new IllegalArgumentException("Impossible add/remove index: " + event.getIndex());
        }
        this.event = event;
        this.removal = removal;
    }

    public String getPresentationName() {
        if (removal) {
            return "Remove child from " + event.getSource().getName();
        } else {
            return "Add child to " + event.getSource().getName();
        }
    }

    public void redo() throws CannotRedoException {
        super.redo();
        if (removal) {
            removeChild();
        } else {
            addChild();
        }
    }

    public void undo() throws CannotUndoException {
        super.undo();
        if (removal) {
            addChild();
        } else {
            removeChild();
        }
    }
    
    private void addChild() {
        event.getSource().addChild(event.getChild(), event.getIndex());
    }

    private void removeChild() {
        try {
            event.getSource().removeChild(event.getChild());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        String verb = removal ? "Remove" : "Add";
        String preposition = removal ? " from " : " to ";
        return verb + " child " + event.getChild().getName() + preposition + event.getSource().getName();
    }
}
