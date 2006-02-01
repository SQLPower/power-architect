package ca.sqlpower.architect.undo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.sqlpower.architect.swingui.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.TablePane;

public class TablePaneLocationEdit extends AbstractUndoableEdit {

	ArrayList<PlayPenComponentEvent> list;
	
	public TablePaneLocationEdit(PlayPenComponentEvent e) {
		list = new ArrayList();
		list.add(e);
	}
	
	public TablePaneLocationEdit(Collection<PlayPenComponentEvent> list)
	{
		this.list = new ArrayList();
		this.list.addAll(list);
	}
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(PlayPenComponentEvent componentEvent : list)
		{
			changeLocation(componentEvent, componentEvent.getOldPoint());
		}
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for(PlayPenComponentEvent componentEvent : list)
		{
			changeLocation( componentEvent, componentEvent.getNewPoint());
		}
	}
	
	private void changeLocation(PlayPenComponentEvent componentEvent,Point newPoint)
	{	
		if(componentEvent.getSource() instanceof TablePane)
		{
			((TablePane)componentEvent.getSource()).setLocation(newPoint);	
			((TablePane)componentEvent.getSource()).repaint();
			((TablePane)componentEvent.getSource()).getPlayPen().revalidate();
		}
			
	}
	
	@Override
	public String getPresentationName() {
		return "Move";
	}
	
	
	
}
