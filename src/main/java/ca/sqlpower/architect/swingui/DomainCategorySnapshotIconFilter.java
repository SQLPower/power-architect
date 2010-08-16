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

package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.architect.enterprise.DomainCategorySnapshot;
import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.IconFilter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.ComposedIcon;

public class DomainCategorySnapshotIconFilter implements IconFilter {
    
    public static final ImageIcon DOMAIN_CATEGORY_ICON = 
        new ImageIcon(DomainCategorySnapshotIconFilter.class.getResource("icons/category.png"));

    @Override
    public Icon filterIcon(Icon original, SPObject node) {
        if (node instanceof DomainCategorySnapshot) {
            Icon icon = DOMAIN_CATEGORY_ICON;
            
            if (((DomainCategorySnapshot) node).isDeleted()) {
                icon = ComposedIcon.getInstance(icon, 
                        DBTreeCellRenderer.ERROR_BADGE);
            } else if (((DomainCategorySnapshot) node).isObsolete()) {
                final BufferedImage bufferedImage = 
                    new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                
                Graphics2D g = bufferedImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(DBTreeCellRenderer.REFRESH_ICON.getImage(), 8, 8, 8, 8, 
                        new Color(0xffffffff, true), null);
                g.dispose();
                
                icon = ComposedIcon.getInstance(icon, new ImageIcon(bufferedImage));
            }
            return icon;
        }
        return original;
    }

}
