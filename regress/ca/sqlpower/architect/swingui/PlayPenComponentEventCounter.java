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

import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentListener;

public class PlayPenComponentEventCounter implements PlayPenComponentListener {
	
	private int starts;
	private int ends;
	private int moved;
	private int resized;

	public int getEvents(){
		return ends+moved+resized+starts;
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

	

	public void componentMoveStart(PlayPenComponentEvent e) {
		starts++;

	}

	public void componentMoveEnd(PlayPenComponentEvent e) {
		ends++;

	}

	public void componentMoved(PlayPenComponentEvent e) {
		moved++;

	}

	public void componentResized(PlayPenComponentEvent e) {
		resized++;

	}

}
