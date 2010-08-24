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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;

/**
 * This table cell renderer works the same as the default cell renderer except
 * it shows nice error and warning icons when it renders a {@link Severity}
 * object.
 */
public class SeverityTableCellRenderer extends DefaultTableCellRenderer {

    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, 
            Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Severity) {
            return new JLabel(getIcon((Severity) value));
        } else {
            return this.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    private ImageIcon getIcon(Severity severity) {
        if (severity == Severity.ERROR) {
            return CriticSwingUtil.ERROR_ICON;
        } else if (severity == Severity.WARNING) {
            return CriticSwingUtil.WARNING_ICON;
        } else {
            return null;
        }
    }
}
