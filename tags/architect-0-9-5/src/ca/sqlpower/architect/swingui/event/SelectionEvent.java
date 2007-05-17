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
