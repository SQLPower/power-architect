/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * This {@link TreeCellRenderer} is used for rendering {@link JTree}s that used
 * the {@link SQLTypeTreeModel}.
 */
public class SQLTypeTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final Logger logger = Logger.getLogger(SQLTypeTreeCellRenderer.class);
    
    public static final ImageIcon CATEGORY_ICON = new ImageIcon(SQLTypeTreeCellRenderer.class.getResource("icons/category.png"));
    public static final ImageIcon DOMAIN_ICON = new ImageIcon(SQLTypeTreeCellRenderer.class.getResource("icons/domain.png"));
    public static final ImageIcon TYPE_ICON = new ImageIcon(SQLTypeTreeCellRenderer.class.getResource("icons/type.png"));
    
    public SQLTypeTreeCellRenderer() {
        super();
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof ArchitectSwingProject) {
            setIcon(null);
            
            if (((ArchitectSwingProject) value).getSession().isEnterpriseSession()) {
                setText("Domains & Data Types");
            } else {
                setText("Data Types");
            }
        } else if (value instanceof UserDefinedSQLType) {
            UserDefinedSQLType type = (UserDefinedSQLType) value;
            
            if (type.getParent() instanceof DomainCategory) {
                setIcon(DOMAIN_ICON);
            } else {
                setIcon(TYPE_ICON);
            }
            setText(type.getName());
        } else if (value instanceof DomainCategory) {
            setIcon(CATEGORY_ICON);
            setText(((DomainCategory) value).getName());
        }
        setToolTipText(getText());
        return this;
    }

}
