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
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.swingui.SPSUtils;

/**
 * This table cell renderer will render lists of quick fix objects.
 */
public class QuickFixListCellRenderer extends DefaultTableCellRenderer {

    /**
     * An image that represents quick fixes are available to use for fixing the
     * current error.
     */
    public static final ImageIcon QUICK_FIX_IMAGE = SPSUtils.createIcon("lightbulb", "quick fix");
    
    /**
     * An image that represents there are no quick fixes available to use for fixing
     * the current error even though the error exists.
     */
    public static final ImageIcon NO_QUICK_FIX_IMAGE = SPSUtils.createIcon("lightbulb_off", "no quick fixes");

    @Override
    public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component originalComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof List<?>) {
            if (((List<?>) value).isEmpty()) {
                JLabel label = new JLabel();
                label.setIcon(NO_QUICK_FIX_IMAGE);
                return label;
            } else {
                JLabel label = new JLabel();
                label.setIcon(QUICK_FIX_IMAGE);
                label.setText("Fix");
                label.setFont(originalComponent.getFont());
                return label;
            }
        } else {
            return originalComponent;
        }
    }
    
}
