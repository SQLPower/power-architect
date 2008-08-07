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

import javax.swing.JPopupMenu;

/**
 * Simple interface that can be used in components that want to show a popup
 * menu but don't know what items the menu should actually contain. For example,
 * the PlayPen can be used for relational modeling or OLAP modeling, and it
 * displays completely different popups in those situations because it's got
 * different implementations of this factory in those two scenarios.
 */
public interface PopupMenuFactory {

    /**
     * Creates a popup menu appropriate to this factory's settings and current state.
     * 
     * @return The popup menu that should be displayed now. Will not be null.
     */
    JPopupMenu createPopupMenu();
}
