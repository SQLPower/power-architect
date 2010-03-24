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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;

/**
 * Renders an OLAPObject item in a list by {@link OLAPUtil#nameFor(OLAPObject)}.
 * Null objects are displayed as an empty box.
 * 
 */
public class OLAPObjectListCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(OLAPUtil.nameFor((OLAPObject) value));
        
        Font font = getFont();
        FontMetrics metrics = getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int fontWidth = getText() == null ? 100 : Math.max(100, metrics.stringWidth(getText()));
        setPreferredSize(new Dimension(fontWidth, fontHeight));
        
        return this;
    }
    
}
