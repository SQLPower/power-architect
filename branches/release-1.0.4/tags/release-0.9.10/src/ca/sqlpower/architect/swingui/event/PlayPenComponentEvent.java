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
import java.util.EventObject;

import ca.sqlpower.architect.swingui.PlayPenComponent;

public class PlayPenComponentEvent extends EventObject {
	
	Point oldPoint;
	Point newPoint;
	
	public PlayPenComponentEvent(PlayPenComponent source) {
		super(source);
	}
	
	public Point getNewPoint() {
		return newPoint;
	}

	public void setNewPoint(Point newPoint) {
		this.newPoint = newPoint;
	}

	public Point getOldPoint() {
		return oldPoint;
	}

	public void setOldPoint(Point oldPoint) {
		this.oldPoint = oldPoint;
	}

	public PlayPenComponentEvent(PlayPenComponent source,Point oldPoint, Point newPoint) {
		super(source);
		this.oldPoint = oldPoint;
		this.newPoint = newPoint;
	}
	
	public PlayPenComponent getPPComponent() {
		return (PlayPenComponent) getSource();
	}
	
	@Override
	public String toString() {
		return "Moving "+source+" from "+oldPoint +" to "+newPoint;
	}
}
