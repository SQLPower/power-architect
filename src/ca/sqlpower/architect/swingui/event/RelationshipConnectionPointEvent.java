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

package ca.sqlpower.architect.swingui.event;

import java.awt.Point;
import java.beans.PropertyChangeEvent;

import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.Relationship;

/**
 * This event is triggered when a user changes connection points of a relationship.
 * @author kaiyi
 *
 */
public class RelationshipConnectionPointEvent extends PropertyChangeEvent {
    
    public RelationshipConnectionPointEvent(PlayPenComponent source) {
        super(source, "Changed connection point", null, null);
    }
    
    public RelationshipConnectionPointEvent(PlayPenComponent source, Point[] oldConnectionPoints, Point[] newConnectionPoints) {
        super(source, "Changed connection point", oldConnectionPoints, newConnectionPoints);
    }
    
    public Relationship getRelationship() {
        return (Relationship) getSource();
    }
    
    @Override
    public String toString() {
        return "Changing connection points of "+source+" Pk point from "+((Point[])getOldValue())[0] +" to "
        +((Point[])getNewValue())[0]+" Fk point from "+((Point[])getOldValue())[1]+" to "+((Point[])getNewValue())[1];
    }
}
