package ca.sqlpower.architect.swingui;

import java.util.EventObject;

public class SelectionEvent extends EventObject {

	public SelectionEvent(Object source) {
		super(source);
	}

	public Selectable getSelectedItem() {
		return (Selectable) getSource();
	}
	
	public String toString() {
		return "[SelectionEvent: source="+getSource()+"]";
	}
}
