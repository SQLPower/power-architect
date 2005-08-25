package ca.sqlpower.architect.swingui;

import java.util.EventObject;

public class SelectionEvent extends EventObject {

	public static final int SELECTION_EVENT = 1;
	public static final int DESELECTION_EVENT = 2;

	protected int eventType;

	public SelectionEvent(Selectable source, int eventType) {
		super(source);
		this.eventType = eventType;
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
}
