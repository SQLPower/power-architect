package ca.sqlpower.architect.undo;

import java.awt.Event;

public class UndoCompoundEvent extends Event {

	public enum EventTypes {
		COMPOUND_EDIT_START,COMPOUND_EDIT_END;
		
		public boolean isStartEvent() {
			return (this == COMPOUND_EDIT_START);
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
