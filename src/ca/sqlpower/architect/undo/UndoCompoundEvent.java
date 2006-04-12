package ca.sqlpower.architect.undo;

import java.awt.Event;

public class UndoCompoundEvent extends Event {

	public enum EventTypes {
		DRAG_AND_DROP_START,DRAG_AND_DROP_END,
		MULTI_SELECT_START, MULTI_SELECT_END, 
		PROPERTY_CHANGE_GROUP_START,PROPERTY_CHANGE_GROUP_END, 
		MOVE_START,MOVE_END,RELATIONSHIP_START,RELATIONSHIP_END;
		
		public boolean isStartEvent() {
			return (this == DRAG_AND_DROP_START
					|| this == MULTI_SELECT_START 
					|| this == PROPERTY_CHANGE_GROUP_START 
					|| this == MOVE_START 
					|| this == RELATIONSHIP_START);
		}
		
		public boolean isEndEvent() {
			return !isStartEvent();
		}
	}
	
	private EventTypes type;
	
	public UndoCompoundEvent(Object target, EventTypes id, String message) {
		super(target, id.ordinal(), message);
		this.type = id;
	}

	public EventTypes getType() {
		return type;
	}
	
}
