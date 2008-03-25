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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * The PlayPenComponentUI is the superclass of all UI delegates for Play Pen components.
 * It provides a pluggable look-and-feel for PlayPenComponents exactly the same way the
 * ComponentUI does for Swing components.
 */
public interface PlayPenComponentUI {
	
    public void installUI(PlayPenComponent c);
    public void uninstallUI(PlayPenComponent c);

	public boolean contains(Point p);
	
	public void paint(Graphics2D g2);

	public void revalidate();
	public Dimension getPreferredSize();
}
