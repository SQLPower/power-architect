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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.PlayPenContentPane;

/**
 * A class that provides all the generic behaviour applicable to OLAP
 * playpen components that have titles and sections of selectable items.
 *
 * @param <T> The model's type 
 * @param <C> The item type. If there are mixed item types, this will be OLAPObject.
 */
public abstract class OLAPPane<T extends OLAPObject, C extends OLAPObject> extends ContainerPane<T, C> {

    /**
     * The sections of this OLAP Pane. There must always be at least one section.
     * The set of sections is allowed to change at any time, but an appropriate
     * event will be fired when it does change.
     */
    protected final List<PaneSection<C>> sections = new ArrayList<PaneSection<C>>();


    protected OLAPPane(PlayPenContentPane parent) {
        super(parent);
    }
    
    /**
     * Returns this pane's list of sections.
     */
    public List<PaneSection<C>> getSections() {
        return sections;
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public int pointToItemIndex(Point p) {
        return getUI().pointToItemIndex(p);
    }
    
    @Override
    public OLAPPaneUI<T, C> getUI() {
        return (OLAPPaneUI<T, C>) super.getUI();
    }
}
