/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.PlayPen.FloatingContainerPaneListener;

public abstract class DraggablePlayPenComponent extends PlayPenComponent {

    private static final Logger logger = Logger.getLogger(DraggablePlayPenComponent.class);
    
    protected DraggablePlayPenComponent(DraggablePlayPenComponent copyMe, PlayPenContentPane parent) {
        super(copyMe, parent);
    }

    public DraggablePlayPenComponent(String name) {
        super(name);
    }
    
    public DraggablePlayPenComponent(String name, PlayPenContentPane parent) {
        super(name, parent);
    }

    /**
     * Sets up the PlayPen's drag listener for all selected items. The drag
     * listener will keep them at an offset from the mouse position.
     * 
     * @param p
     *            The relative location of the mouse event, from the top left
     *            corner of this component.
     */
    protected void setupDrag(Point p) {
        PlayPen pp = getPlayPen();
        Iterator<DraggablePlayPenComponent> it = pp.getSelectedDraggableComponents().iterator();
        logger.debug("event point: " + p); //$NON-NLS-1$
        logger.debug("zoomed event point: " + pp.zoomPoint(new Point(p))); //$NON-NLS-1$
        pp.setDraggingContainerPanes(true);
        startedDragging();
        Map<DraggablePlayPenComponent, Point> ppcToHandleMap = new HashMap<DraggablePlayPenComponent, Point>();

        while (it.hasNext()) {
            // create FloatingContainerPaneListener for each selected item
            DraggablePlayPenComponent ppc = it.next();
            logger.debug("(" + ppc.getModel() + ") zoomed selected containerPane's point: " + ppc.getLocationOnScreen()); //$NON-NLS-1$ //$NON-NLS-2$
            logger.debug("(" + ppc.getModel() + ") unzoomed selected containerPane's point: " + pp.unzoomPoint(ppc.getLocationOnScreen())); //$NON-NLS-1$ //$NON-NLS-2$
            /* the floating ContainerPane listener expects zoomed handles which are relative to
                       the location of the ContainerPane column which was clicked on.  */
            Point clickedItem = getLocationOnScreen();
            Point otherContainer = ppc.getLocationOnScreen();
            logger.debug("(" + ppc.getModel() + ") translation x=" //$NON-NLS-1$ //$NON-NLS-2$
                    + (otherContainer.getX() - clickedItem.getX()) + ",y=" //$NON-NLS-1$
                    + (otherContainer.getY() - clickedItem.getY()));
            Point handle = pp.zoomPoint(new Point(p));
            handle.translate((int)(clickedItem.getX() - otherContainer.getX()), (int) (clickedItem.getY() - otherContainer.getY()));
            
            ppcToHandleMap.put(ppc, handle);
        }
        new FloatingContainerPaneListener(pp, ppcToHandleMap);
    }
    
}
