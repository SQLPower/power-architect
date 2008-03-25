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


public class UndoCompoundEvent {

	public enum EventTypes {
		COMPOUND_EDIT_START,COMPOUND_EDIT_END;
		
		public boolean isStartEvent() {
			return (this == COMPOUND_EDIT_START);
		}
	}
	
	private EventTypes type;
	private String message;
	
	public UndoCompoundEvent(EventTypes id, String message) {
		this.message = message;
		this.type = id;
	}

	public EventTypes getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
}
