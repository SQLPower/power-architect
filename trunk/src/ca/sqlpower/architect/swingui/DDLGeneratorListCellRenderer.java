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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;

/**
 * Renders a list item which is of type <tt>Class&lt;? extends {@link DDLGenerator}&gt;</tt>.
 * The rendered value is the database platform name the DDL Generator targets.
 * This generator is safe for use on null values, and has the same rendering behaviour
 * as the default list cell renderer for them.
 */
public class DDLGeneratorListCellRenderer extends DefaultListCellRenderer {

    private static final Logger logger = Logger.getLogger(DDLGeneratorListCellRenderer.class);
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value != null) {
            try {
                Class<?> c = (Class<?>) value;
                Class<? extends DDLGenerator> ddlgClass = c.asSubclass(DDLGenerator.class);
                DDLGenerator g = ddlgClass.newInstance();
                value = g.getName();
            } catch (Exception e) {
                logger.warn("Couldn't determine DDL Generator name. Just using class name...", e);
                value = value.getClass().getName();
            }
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
