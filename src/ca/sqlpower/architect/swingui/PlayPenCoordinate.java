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

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.olap.OLAPPane;
import ca.sqlpower.architect.swingui.olap.PaneSection;

/**
 * Used to represent a location within a container pane.
 *  
 * @param <T> The type of the model of the pane.
 * @param <I> The type of the items of the pane.
 */
public class PlayPenCoordinate <T extends OLAPObject, I extends OLAPObject> {
    
    /**
     * A special item index that represents the titlebar area.
     */
    public static final int ITEM_INDEX_TITLE = -1;

    /**
     * A special item index that means "no location."
     */
    public static final int ITEM_INDEX_NONE = -2;
    
    /**
     * A special item index that represents a section titlebar area.
     */
    public static final int ITEM_INDEX_SECTION_TITLE = -3;
    
    /**
     * The pane that the represented location is within.
     */
    private final OLAPPane<T, I> pane;
    
    /**
     * The section of the represented location.
     */
    private final PaneSection<? extends I> section;
    
    /**
     * The item of the represented location.
     */
    private final I item;
    
    /**
     * The index of the item within the section.
     */
    private final int index;
    
    public PlayPenCoordinate(OLAPPane<T, I> pane, PaneSection<? extends I> section, int index, I item) {
        this.pane = pane;
        this.section = section;
        this.index = index;
        this.item = item;
    }

    public OLAPPane<T, I> getPane() {
        return pane;
    }

    public PaneSection<? extends I> getSection() {
        return section;
    }

    public int getIndex() {
        return index;
    }

    public I getItem() {
        return item;
    }
    
    @Override
    public String toString() {
        return "pane = " + pane + ", section = " + section + ", index = " + index + ", item = " + item;
    }
}
