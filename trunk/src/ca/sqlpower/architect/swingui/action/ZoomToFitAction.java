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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPenComponent;

/**
 * An action that zooms the play pen's view in order to fit the current
 * selection, or the entire data model if nothing is selected.
 */
public class ZoomToFitAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(ZoomToFitAction.class);
    
    /**
     * Creates a new zoom action tied to the gicen session's play pen.
     */
    public ZoomToFitAction(ArchitectSwingSession session) {
        super(session, "Zoom to fit", "Zoom to fit", "zoom_fit");
    }
    
    /**
     * The amount to multiply the exact zoom factor by in order to come up
     * with the actual zoom factor to use.  The default value of 0.9 leaves
     * at 10% border of empty space around the zoomed region, which is
     * usually a good comfortable amount. 
     */
    private double overZoomCoefficient = 0.9;

    public void actionPerformed(ActionEvent e) {
        if (playpen == null) {
            // It would be best to throw the NPE here, but the old implementation just
            // silently returned with no side effects when the playpen was missing.
            logger.error("No playpen for this action!? Doing nothing...");
            return;
        }
        
        Rectangle zoomBounds = calcBoundingRectangle();
        if (zoomBounds == null) return;
    
        double zoom = Math.min(playpen.getViewportSize().getHeight()/zoomBounds.height,
                               playpen.getViewportSize().getWidth()/zoomBounds.width);
        zoom *= overZoomCoefficient;
    
        playpen.setZoom(zoom);
        playpen.scrollRectToVisible(playpen.zoomRect(zoomBounds));
    }

    /**
     * Calcualtes the bounding rectangle for the play pen components that are
     * currently selected, or the entire diagram if nothing is selected.
     * 
     * @return The bounding rectangle in playpen coordinates (not visible screen
     * coordinates).  If the diagram is empty (no play pen components exist),
     * returns null.
     */
    private Rectangle calcBoundingRectangle() {
        Rectangle rect = null;
        List<PlayPenComponent> fitThese = playpen.getSelectedItems();
        if (fitThese.isEmpty()) {
            fitThese = new ArrayList<PlayPenComponent>();
            for (int i = 0; i < playpen.getContentPane().getComponentCount(); i++) {
                fitThese.add(playpen.getContentPane().getComponent(i));
            }
        }
        
        for (PlayPenComponent ppc : fitThese) {
            if ( rect == null ) {
                rect = new Rectangle(ppc.getLocation(),ppc.getSize());
            } else {
                rect.add(ppc.getBounds());
            }
        }
        return rect;
    }
}