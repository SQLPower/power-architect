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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;

public class SelectAllAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(PrintAction.class);

	/**
	 * The PlayPen instance that this Action operates on.
	 */
	private final PlayPen pp;

	public SelectAllAction(ArchitectSwingSession session) {
		super("Select All");
		putValue(SHORT_DESCRIPTION, "Select All");
        pp = session.getPlayPen();
	}

	public void actionPerformed(ActionEvent evt) {
		logger.debug(getValue(SHORT_DESCRIPTION) + ": started");
		pp.selectAll();
	}
	
}
