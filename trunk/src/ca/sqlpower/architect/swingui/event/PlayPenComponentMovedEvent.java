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

public class PlayPenComponentMovedEvent extends PropertyChangeEvent {

	public PlayPenComponentMovedEvent(PlayPenComponent source) {
		super(source, "Playpen Component moved", null, null);
	}
	
	public PlayPenComponentMovedEvent(PlayPenComponent source, Point oldPoint, Point newPoint) {
	    super(source, "Playpen Component moved", oldPoint, newPoint);
	}
	
	public Point getNewPoint() {
		return (Point)this.getNewValue();
	}


	public Point getOldPoint() {
		return (Point)this.getOldValue();
	}

	
	public PlayPenComponent getPPComponent() {
		return (PlayPenComponent) getSource();
	}
	
	@Override
	public String toString() {
		return "Moving "+source+" from "+getOldPoint() +" to "+getNewPoint();
	}
}
