package ca.sqlpower.architect.swingui.event;

import java.awt.Point;
import java.util.EventObject;

import ca.sqlpower.architect.swingui.PlayPenComponent;

public class PlayPenComponentEvent extends EventObject {
	
	Point oldPoint;
	Point newPoint;
	
	public PlayPenComponentEvent(PlayPenComponent source) {
		super(source);
	}
	
	public Point getNewPoint() {
		return newPoint;
	}

	public void setNewPoint(Point newPoint) {
		this.newPoint = newPoint;
	}

	public Point getOldPoint() {
		return oldPoint;
	}

	public void setOldPoint(Point oldPoint) {
		this.oldPoint = oldPoint;
	}

	public PlayPenComponentEvent(PlayPenComponent source,Point oldPoint, Point newPoint) {
		super(source);
		this.oldPoint = oldPoint;
		this.newPoint = newPoint;
	}
	
	public PlayPenComponent getPPComponent() {
		return (PlayPenComponent) getSource();
	}
}
