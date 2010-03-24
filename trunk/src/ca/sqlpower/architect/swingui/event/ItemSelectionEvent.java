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

package ca.sqlpower.architect.swingui.event;

import java.util.Collections;
import java.util.Set;

import ca.sqlpower.architect.swingui.ContainerPane;

public class ItemSelectionEvent<T, C> {

    private final ContainerPane<T, C> source;
    private final Set<C> items;

    /**
     * Creates a new event object that holds information about the
     * items in a container pane becoming selected or deselected.
     * 
     * @param source the container pane whose item selection state changed.
     * @param items the items that were selected or deselected.
     */
    public ItemSelectionEvent(ContainerPane<T, C> source, Set<C> items) {
        this.source = source;
        this.items = Collections.unmodifiableSet(items);
    }

    /**
     * Returns the container pane whose item selection state changed.
     */
    public ContainerPane<T, C> getSource() {
        return source;
    }

    /**
     * Returns an unmodifiable set of the items that were selected or deselected.
     */
    public Set<C> getItems() {
        return items;
    }
    
}
