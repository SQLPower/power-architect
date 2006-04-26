package ca.sqlpower.architect.undo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;

public class TablePaneLocationEdit extends AbstractUndoableEdit {
	private static final Logger logger = Logger.getLogger(TablePaneLocationEdit.class);
	
	ArrayList<PlayPenComponentEvent> list;
	
	public TablePaneLocationEdit(PlayPenComponentEvent e) {
		list = new ArrayList<PlayPenComponentEvent>();
		list.add(e);
	}
	
	public TablePaneLocationEdit(Collection<PlayPenComponentEvent> list) {
		this.list = new ArrayList<PlayPenComponentEvent>();
		this.list.addAll(list);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (PlayPenComponentEvent componentEvent : list) {
			changeLocation(componentEvent, componentEvent.getOldPoint());
		}
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (PlayPenComponentEvent componentEvent : list) {
			changeLocation(componentEvent, componentEvent.getNewPoint());
		}
	}
	
	private void changeLocation(PlayPenComponentEvent componentEvent, Point newPoint) {
		logger.debug("Changing the location of "+componentEvent.getSource()+" to " + newPoint);
		if (componentEvent.getSource() instanceof TablePane) {
			((TablePane) componentEvent.getSource()).setLocation(newPoint);
			((TablePane) componentEvent.getSource()).repaint();
			((TablePane) componentEvent.getSource()).getPlayPen().revalidate();
		}

	}
	
	@Override
	public String getPresentationName() {
		return "Move";
	}
	
	@Override
	public String toString() {
		return "Changing the location of "+list;
	}
	
}
