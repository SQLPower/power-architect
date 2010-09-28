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
package ca.sqlpower.architect.swingui.action;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

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
