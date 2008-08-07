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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;

/**
 * An action that zooms the play pen's view in order to fit the current
 * selection, or the entire data model if nothing is selected.
 */
public class ZoomToFitAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(ZoomToFitAction.class);
    
    /**
     * Creates a new zoom action tied to the gicen session's play pen.
     * By default, this action gives itself the accelerator key "z" with
     * no modifiers.
     */
    public ZoomToFitAction(ArchitectSwingSession session) {
        super(session, Messages.getString("ZoomToFitAction.name"), Messages.getString("ZoomToFitAction.description"), "zoom_fit"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0));
    }
    
    /**
     * The amount to multiply the exact zoom factor by in order to come up
     * with the actual zoom factor to use.  The default value of 0.9 leaves
     * at 10% border of empty space around the zoomed region, which is
     * usually a good comfortable amount. 
     */
    private static final double OVER_ZOOM_COEFF = 0.9;

    /**
     * The maximum amount this "zoom to fit" will try to zoom in. This handles the
     * case where you select a single, small table and "zoom to fit" makes it
     * take up the whole screen.
     */
    private static final double MIN_ZOOM = 1.0;
    
    public void actionPerformed(ActionEvent e) {
        if (playpen == null) {
            // It would be best to throw the NPE here, but the old implementation just
            // silently returned with no side effects when the playpen was missing.
            logger.error("No playpen for this action!? Doing nothing..."); //$NON-NLS-1$
            return;
        }
        
        zoomToFitSelected(playpen);
    }

    /**
     * Performs a zoom-to-fit for the selected items in the given playpen.
     * 
     * @param playpen The play pen to operate on
     */
    public static void zoomToFitSelected(PlayPen playpen) {
        Rectangle zoomBounds = calcBoundingRectangle(playpen);
        if (zoomBounds == null) return;
    
        double zoom = Math.min(playpen.getViewportSize().getHeight()/zoomBounds.height,
                               playpen.getViewportSize().getWidth()/zoomBounds.width);
        zoom *= OVER_ZOOM_COEFF;
        zoom = Math.min(zoom, MIN_ZOOM);

        // When I get the playpen's animated zoom working properly, I'll change this to use it
        playpen.setZoom(zoom);
        playpen.scrollRectToVisible(zoomBounds);
    }
    
    /**
     * Calcualtes the bounding rectangle for the play pen components that are
     * currently selected, or the entire diagram if nothing is selected.
     * 
     * @return The bounding rectangle in playpen coordinates (not visible screen
     * coordinates).  If the diagram is empty (no play pen components exist),
     * returns null.
     */
    private static Rectangle calcBoundingRectangle(PlayPen playpen) {
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