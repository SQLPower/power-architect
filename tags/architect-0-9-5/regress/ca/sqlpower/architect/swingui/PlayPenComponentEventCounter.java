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
