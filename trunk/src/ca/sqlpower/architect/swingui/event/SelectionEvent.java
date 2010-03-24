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

import java.util.EventObject;

import ca.sqlpower.architect.swingui.Selectable;

public class SelectionEvent extends EventObject {

	public static final int SELECTION_EVENT = 1;
	public static final int DESELECTION_EVENT = 2;

    public static final int SINGLE_SELECT=4;
    public static final int CTRL_MULTISELECT=8;
    public static final int SHIFT_MULTISELECT=16;
    
    
	protected int eventType;
	private final int multiselectType;
    
	public SelectionEvent(Selectable source, int eventType, int multiselect) {
		super(source);
		this.eventType = eventType;
        this.multiselectType = multiselect;
	}

	public Selectable getSelectableSource() {
		return (Selectable) getSource();
	}
	
	public String toString() {
		return "[SelectionEvent: type="+(eventType==SELECTION_EVENT?"selection":eventType==DESELECTION_EVENT?"deselection":("unknown code "+eventType))+", source="+getSource()+"]";
	}

	/**
	 * Returns the event type (SELECTION_EVENT or DESELECTION_EVENT).
	 */
	public int getType() {
		return eventType;
	}

	/**
	 * returns the multiselect type (SINGLE_SELECT,CTRL_MULTISELECT,SHIFT_MULTISELECT)
	 */
    public int getMultiselectType() {
        return multiselectType;
    }  
}
