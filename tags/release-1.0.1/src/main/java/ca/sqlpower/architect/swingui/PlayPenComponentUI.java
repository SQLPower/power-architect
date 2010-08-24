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

import javax.annotation.Nonnull;

import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Transient;

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
	
	/**
     * Returns a point on the UI object that is most reasonable to attach
     * additional text or objects to for the given model object or a part of the
     * model. If the component is made up of multiple parts the object passed in
     * may change the desired location to put an icon near the part.
     * <p>
     * Default is normally the top left point of the component returned by getLocation().
     * 
     * @param modelObject
     *            The model of this component or an object that is part of the
     *            model, normally a descendant.
     * @return A point that is the best location to place an icon or text at. If
     *         the given modelObject does not belong to this component a
     *         reasonable point for the component will still be returned.
     */
    @Transient @Accessor
    public Point getPointForModelObject(@Nonnull Object modelObject);
}
