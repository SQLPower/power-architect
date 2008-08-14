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
package ca.sqlpower.architect.swingui.olap;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import ca.sqlpower.architect.swingui.ContainerPaneUI;
import ca.sqlpower.architect.swingui.PlayPenComponent;

/**
 * Does all of the generic painting and event handling that applies to
 * all "pane" type components in the OLAP play pen.
 * <p>
 * Our plan is to eventually move all the generic stuff up into ContainerPaneUI
 * so that BasicTablePaneUI can get simpler.
 */
public abstract class OLAPPaneUI extends ContainerPaneUI {

    /**
     * Returns this pane's list of sections.
     */
    public abstract List<PaneSection> getSections();
    
    @Override
    public int pointToItemIndex(Point p) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean contains(Point p) {
        // TODO Auto-generated method stub
        return false;
    }

    public Dimension getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void installUI(PlayPenComponent c) {
        // TODO Auto-generated method stub
        
    }

    public void paint(Graphics2D g2) {
        // TODO Auto-generated method stub
        
    }

    public void revalidate() {
        // TODO Auto-generated method stub
        
    }

    public void uninstallUI(PlayPenComponent c) {
        // TODO Auto-generated method stub
        
    }

}
