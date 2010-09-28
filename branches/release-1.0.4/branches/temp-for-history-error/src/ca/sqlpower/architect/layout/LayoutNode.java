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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

public interface LayoutNode {

    String getNodeName();
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    Rectangle getBounds(Rectangle b);
    Rectangle getBounds();
    void setBounds(int x, int i, int width, int height);
    Point getLocation();
    void setLocation(int i, int j);
    void setLocation(Point pos);
    List<LayoutEdge> getOutboundEdges();
    List<LayoutEdge> getInboundEdges();

}
