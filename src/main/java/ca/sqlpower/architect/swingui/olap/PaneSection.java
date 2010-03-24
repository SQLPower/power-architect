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

/**
 * Interface for sections of a Pane UI.
 * 
 * @param C the type of each item in this pane section
 */
public interface PaneSection<C> {

    /**
     * Returns this section's title. If the section should not have a rendered
     * title, returns null.
     */
    String getTitle();
    
    /**
     * Returns the items in this section, in the order they should be displayed.
     */
    List<C> getItems();
    
    /**
     * Returns the type of items that appear in this section.
     */
    Class<C> getItemType();

    /**
     * Inserts the given item at the given index within this section.
     * 
     * @param idx
     *            The index to add at. Must be between 0 and getItems().size()
     *            inclusive.
     * @param item
     *            The item to add.
     */
    void addItem(int idx, C item);
    
    /**
     * Inserts the given item at the end of this section.
     * 
     * @param item The item to add.
     */
    void addItem(C item);
}
