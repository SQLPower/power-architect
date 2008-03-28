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
