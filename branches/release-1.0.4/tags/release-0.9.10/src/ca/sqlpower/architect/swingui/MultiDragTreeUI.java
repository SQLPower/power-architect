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
package ca.sqlpower.architect.swingui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.plaf.basic.BasicTreeUI;

import org.apache.log4j.Logger;

public class MultiDragTreeUI extends BasicTreeUI {
	private static Logger logger = Logger.getLogger(MultiDragTreeUI.class);
	
	public class MouseHandler extends BasicTreeUI.MouseHandler {
		private int x1, y1;
		
		public void mousePressed(MouseEvent e) {
			logger.debug("mousePressed was called");
			x1 = e.getX();
			y1 = e.getY();
			int[] rows = tree.getSelectionRows();
		
			if (rows != null) {
				for (int i = 0; i < rows.length; i++) {
					Rectangle rect3 = tree.getRowBounds(rows[i]);
					if (rect3.contains(x1, y1)) {
						logger.debug("consuming click event from already selected node...");
						e.consume();
						break;
					}
				}
			}
			super.mousePressed(e);
		}		
	}
	
	/** 
     *	
     * Unlike in the example below, this actually needs to be _outside_ the 
     * override of MouseHandler because all events in TreeUI are handled by a generic 
     * Handler that implements all the Listener interfaces a JTree is interested in...
     * 
     * http://forum.java.sun.com/thread.jspa?threadID=376761&messageID=1964088
	 *
     * MultiSelect DND probably used to work properly in previous versions of the JDK...
     * 
     */
	protected MouseListener createMouseListener() {
		return new MouseHandler();
	}
}
