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

import java.util.List;

import ca.sqlpower.architect.olap.OLAPObject;

/**
 * Generic implementation of a pane section. Should suffice for most uses.
 */
public class PaneSectionImpl implements PaneSection {

    private final List<OLAPObject> items;
    private final String title;
    
    public PaneSectionImpl(List<OLAPObject> items, String title) {
        super();
        this.items = items;
        this.title = title;
    }

    /**
     * This is the same list that was passed to the constructor. It may or may
     * not be modifiable (depending on what you provided to the constructor)
     * and its contents may or may not mirror the contents of the business model
     * (again, it depends on what you passed to the constructor).
     * <p>
     * No matter what, you won't get events from the PaneSection when the item
     * list changes. Consult the model for change notifications.
     */
    public List<OLAPObject> getItems() {
        return items;
    }

    public String getTitle() {
        return title;
    }

}
