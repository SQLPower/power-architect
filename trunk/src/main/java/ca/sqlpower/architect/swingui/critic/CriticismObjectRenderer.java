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

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;

/**
 * Renderer for Objects displayed in the critic table. 
 */
public class CriticismObjectRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        String visibleText = getVisibleText(value);
        Component originalComponent = super.getTableCellRendererComponent(table, visibleText, 
                isSelected, hasFocus, row, column);
        JLabel label = new JLabel();
        label.setFont(originalComponent.getFont());
        label.setText(visibleText);
        if (value instanceof SQLTable) {
            label.setIcon(DBTreeCellRenderer.TABLE_ICON);
            return label;
        } else if (value instanceof SQLIndex) {
            SQLIndex index = (SQLIndex) value;
            if (index.isPrimaryKeyIndex()) {
                label.setIcon(DBTreeCellRenderer.PK_ICON);
            } else {
                label.setIcon(DBTreeCellRenderer.INDEX_ICON);
            }
            label.setText(visibleText);
            return label;
        } else if (value instanceof SQLColumn) {
            label.setIcon(DBTreeCellRenderer.COLUMN_ICON);
            return label;
        } else if (value instanceof SQLRelationship) {
            label.setIcon(DBTreeCellRenderer.EXPORTED_KEY_ICON);
            return label;
        } else if (value instanceof SQLImportedKey) {
            label.setIcon(DBTreeCellRenderer.IMPORTED_KEY_ICON);
            return label;
        } else {
            return originalComponent;
        }
    }

    /**
     * Returns the best guess at decent user-readable text for a criticized
     * object.
     */
    public static String getVisibleText(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof SQLTable) {
            return "<html><b>" + ((SQLTable) value).getName() + "</b></html>";
        } else if (value instanceof SQLIndex) {
            SQLIndex index = (SQLIndex) value;
            StringBuffer text = new StringBuffer();
            text.append("<html>");
            if (index.getParent() != null) {
                text.append(index.getParent().getName() + ".");
            } else {
                text.append("(no parent).");
            }
            text.append("<b>");
            text.append(index.getName());
            text.append("</b></html>");
            return text.toString();
        } else if (value instanceof SQLColumn) {
            SQLColumn sqlColumn = (SQLColumn) value;
            StringBuffer text = new StringBuffer();
            text.append("<html>");
            if (sqlColumn.getParent() != null) {
                text.append(sqlColumn.getParent().getName() + ".");
            } else {
                text.append("(no parent).");
            }
            text.append("<b>");
            text.append(sqlColumn.getName());
            text.append("</b></html>");
            return text.toString();
        } else if (value instanceof SQLRelationship) {
            SQLRelationship relation = (SQLRelationship) value;
            StringBuffer text = new StringBuffer();
            text.append("<html>");
            if (relation.getParent() != null) {
                text.append(relation.getParent().getName() + ".");
            } else {
                text.append("(no parent).");
            }
            text.append("<b>");
            text.append(relation.getName());
            text.append("</b></html>");
            return text.toString();
        } else if (value instanceof SQLImportedKey) {
            SQLRelationship relation = (SQLRelationship) value;
            StringBuffer text = new StringBuffer();
            text.append("<html>");
            if (relation.getParent() != null) {
                text.append(relation.getParent().getName() + ".");
            } else {
                text.append("(no parent).");
            }
            text.append("<b>");
            text.append(relation.getName());
            text.append("</b></html>");
            return text.toString();
        } else if (value instanceof SQLObject) {
            String name = ((SQLObject) value).getShortDisplayName();
            if (name == null || name.trim().length() == 0) {
                ((SQLObject) value).getName();
            }
            return name;
        } else if (value instanceof SPObject) {
            return ((SPObject) value).getName();
        } else if (value instanceof JDBCDataSourceType) {
            return ((JDBCDataSourceType) value).getName();
        } else {
            return value.toString();
        }
    }
}
