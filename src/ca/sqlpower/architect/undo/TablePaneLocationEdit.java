/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
