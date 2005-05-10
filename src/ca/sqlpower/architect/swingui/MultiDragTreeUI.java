package ca.sqlpower.architect.swingui;

import javax.swing.plaf.basic.BasicTreeUI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.apache.log4j.Logger;

public class MultiDragTreeUI extends BasicTreeUI {
	private static Logger logger = Logger.getLogger(MultiDragTreeUI.class);
	
	public class MouseHandler extends BasicTreeUI.MouseHandler {
		private int x1, y1;
		
		public void mousePressed(MouseEvent e) {
			logger.error("mousePressed was called");
			x1 = e.getX();
			y1 = e.getY();
			int[] rows = tree.getSelectionRows();
		
			if (rows != null) {
				for (int i = 0; i < rows.length; i++) {
					Rectangle rect3 = tree.getRowBounds(rows[i]);
					if (rect3.contains(x1, y1)) {
						logger.error("consuming click event from already selected node...");
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
     * Unlike in the example below, actually needs to be _outside_ the 
     * override of MouseHandler because all events in TreeUI are handled by a generic 
     * Handler that implements all the Listener interfaces a JTree is interested in...
     * 
     * http://forum.java.sun.com/thread.jspa?threadID=376761&messageID=1964088
	 *
     * MultiSelect DND probably used to work properly in JDK 1.4...
     * 
     */
	protected MouseListener createMouseListener() {
		return new MouseHandler();
	}
}
