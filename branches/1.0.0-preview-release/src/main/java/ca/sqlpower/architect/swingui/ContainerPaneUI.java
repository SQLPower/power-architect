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

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serializable;

import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.object.AbstractSPListener;

public abstract class ContainerPaneUI extends AbstractSPListener
implements PlayPenComponentUI, Serializable {
    
    /**
     * This delegate method is specified by {@link DimensionPane#pointToItemIndex(Point)}
     * Subclass of DimensionPaneUI will implement this method to calculate column index
     * given a point on the playpenComponent.
     */
    @Deprecated
    public abstract int pointToItemIndex(Point p);

    protected void paintInsertionPoint(Graphics2D g2, int y, int width) {
        g2.drawLine(5, y, width - 6, y);
        g2.drawLine(2, y-3, 5, y);
        g2.drawLine(2, y+3, 5, y);
        g2.drawLine(width - 3, y-3, width - 6, y);
        g2.drawLine(width - 3, y+3, width - 6, y);
    }
}
