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

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPenComponent;


public class ZoomAction extends AbstractArchitectAction implements PropertyChangeListener {
	private static final Logger logger = Logger.getLogger(ZoomAction.class);

	protected double zoomStep;

	public static final String ZOOM_IN = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_IN";
	public static final String ZOOM_OUT = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_OUT";
	public static final String ZOOM_ALL = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_ALL";

	public ZoomAction(ArchitectSwingSession session, double amount) {
        super(session,
              amount > 0.0 ? "Zoom In" : "Zoom Out",
              amount > 0.0 ? "Zoom In" : "Zoom Out",
              amount > 0.0 ? "zoom_in" : "zoom_out");        
		this.zoomStep = amount;
        playpen.addPropertyChangeListener(this);
        if (amount > 0.0) {
            // According to my probing of key events on OS X 10.4.11, you can't get a VK_PLUS
            // event when modifiers in addition to SHIFT are present.. it's always VK_EQUALS.
            putValue(AbstractAction.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
        } else {
            putValue(AbstractAction.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }
	}
		
	public void actionPerformed(ActionEvent e) {
		logger.debug("oldZoom="+playpen.getZoom()+",zoomStep="+zoomStep);
		// 	zoom by a factor of sqrt(2) instead of linear so we can go below 0.1

		// playpen.setZoom(playpen.getZoom() + zoomStep); 
		playpen.setZoom(playpen.getZoom() * Math.pow(2,zoomStep));
		logger.debug("newZoom="+playpen.getZoom());
		Rectangle scrollTo = null;
		Iterator it = playpen.getSelectedItems().iterator();
		while (it.hasNext()) {
			Rectangle bounds = ((PlayPenComponent) it.next()).getBounds();
			logger.debug("new rectangle, bounds: " + bounds);
			if (scrollTo == null) {
				scrollTo = new Rectangle(bounds);
			} else {
				logger.debug("added rectangles, new bounds: " + scrollTo); 
				scrollTo.add(bounds);
			}
		}
		if (scrollTo != null && !scrollTo.isEmpty()) {
			playpen.zoomRect(scrollTo);
			playpen.scrollRectToVisible(scrollTo);
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		// this used to enable/disable zooming out when the zoom factor got lower than 0.1
	}
}
