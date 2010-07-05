/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.critic;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLObject;

/**
 * Renderer for Objects displayed in the critic table. 
 */
public class CriticismObjectRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        return new JLabel(getVisibleText(value));
    }

    /**
     * Returns the best guess at decent user-readable text for a criticized
     * object.
     */
    public static String getVisibleText(Object value) {
        if (value instanceof SQLObject) {
            String name = ((SQLObject) value).getShortDisplayName();
            if (name == null || name.trim().length() == 0) {
                ((SQLObject) value).getName();
            }
            return name;
        } else if (value instanceof SPObject) {
            return ((SPObject) value).getName();
        } else {
            return value.toString();
        }
    }
}
