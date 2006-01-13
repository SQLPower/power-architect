package ca.sqlpower.architect.swingui;

import java.util.EventObject;

public class PlayPenComponentEvent extends EventObject {
	public PlayPenComponentEvent(PlayPenComponent source) {
		super(source);
	}
	
	public PlayPenComponent getPPComponent() {
		return (PlayPenComponent) getSource();
	}
}
