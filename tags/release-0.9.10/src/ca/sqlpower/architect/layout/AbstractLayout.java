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
package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractLayout implements ArchitectLayout {

    protected Rectangle frame;

    public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle rect) {
        frame = rect;
    }

    HashMap<String, Object> properties;

    protected AbstractLayout() {
        properties = new HashMap<String, Object>();
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);

    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Dimension getNewArea(List<? extends LayoutNode> nodes) {
        Dimension d = new Dimension();
        long area = 0;
        for (LayoutNode tp : nodes) {
            Rectangle b = tp.getBounds();
            area += b.width * b.height;
        }

        final double areaFudgeFactor = 16.0;
        double newWidth = Math.sqrt((11.0 / 8.5) * area * areaFudgeFactor);
        double newHeight = (8.5 / 11.0) * newWidth;
        d.setSize((int) newWidth, (int) newHeight);
        return d;
    }

}
