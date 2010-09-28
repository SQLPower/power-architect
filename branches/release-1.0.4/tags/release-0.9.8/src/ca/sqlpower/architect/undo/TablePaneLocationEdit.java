/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
