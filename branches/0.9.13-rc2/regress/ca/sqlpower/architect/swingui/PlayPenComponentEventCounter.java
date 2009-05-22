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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PlayPenComponentEventCounter implements PropertyChangeListener {
	
    /*
     * Right now, starts and end events are not implemented. 
     */
	private int starts;
	private int ends;
	private int moved;
	private int resized;
	private int conPointsMoved;

	public int getEvents(){
		return ends+moved+resized+starts+conPointsMoved;
	}
	
	public int getEnds() {
		return ends;
	}

	public int getMoved() {
		return moved;
	}

	public int getResized() {
		return resized;
	}

	public int getStarts() {
		return starts;
	}
	
	public int getConPointsMoved() {
	    return conPointsMoved;
	}

	/**
	 * According to the type of event receives, increase corresponding
	 * counters.
	 */
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("connectionPoints")) {
            conPointsMoved++;
        }
        else if(evt.getPropertyName().equals("location")) {
            moved++;
        }
        else if(evt.getPropertyName().equals("bounds")) {
            resized++;
        }
    }

}
