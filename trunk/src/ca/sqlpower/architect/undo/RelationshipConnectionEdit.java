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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.event.RelationshipConnectionPointEvent;

/**
 * This class specifies the operations to be executed when we press either "undo" or "redo"
 * after changing connection points of 1 relationship.
 * @author kaiyi
 *
 */
public class RelationshipConnectionEdit extends AbstractUndoableEdit {
private static final Logger logger = Logger.getLogger(RelationshipConnectionEdit.class);
    
    ArrayList<RelationshipConnectionPointEvent> list;
    
    public RelationshipConnectionEdit(RelationshipConnectionPointEvent e) {
        list = new ArrayList<RelationshipConnectionPointEvent>();
        list.add(e);
    }
    
    public RelationshipConnectionEdit(Collection<RelationshipConnectionPointEvent> list) {
        this.list = new ArrayList<RelationshipConnectionPointEvent>();
        this.list.addAll(list);
    }
    
    /**
     * Move all the connection points back to the old positions.
     */
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for (RelationshipConnectionPointEvent componentEvent : list) {
            componentEvent.setNewPoints(componentEvent.getRelationship().getPkConnectionPoint(), componentEvent.getRelationship().getFkConnectionPoint());
            changeConnectionPoints(componentEvent, componentEvent.getPkOldPoint(), componentEvent.getFkOldPoint());
        }
    }
    
    /**
     * Return to previous edit state by relocating the connection points to newer positions
     */
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        for (RelationshipConnectionPointEvent componentEvent : list) {
            changeConnectionPoints(componentEvent, componentEvent.getPkNewPoint(), componentEvent.getFkNewPoint());
        }
    }
    
    /**
     * Helper method that sets the connection points
     * @param componentEvent
     * @param pkConnectionPoint
     * @param fkConnectionPoint
     */
    private void changeConnectionPoints(RelationshipConnectionPointEvent componentEvent, Point pkConnectionPoint, Point fkConnectionPoint) {
        logger.debug("Changing the Connection locations of " + componentEvent.getSource() + ". Pk: " + pkConnectionPoint + 
                " Fk: " + fkConnectionPoint);
        if (componentEvent.getSource() instanceof Relationship) {
            Relationship r = ((Relationship) componentEvent.getSource());
            r.updateUI();
            r.setPkConnectionPoint(pkConnectionPoint);
            r.setFkConnectionPoint(fkConnectionPoint);
        }
    }
    
    @Override
    public String getPresentationName() {
        return "ChangeConnectionPoints";
    }
    
    @Override
    public String toString() {
        return "Changing the connection points of "+list;
    }
}
