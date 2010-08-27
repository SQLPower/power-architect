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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.swingui.dbtree.IconFilter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.SPSUtils;

/**
 * Replaces the table icon with the "profiled table" icon for tables that
 * have been profiled.
 */
public class ProfiledTableIconFilter implements IconFilter {

    public static final ImageIcon PROFILED_DATABASE_ICON = SPSUtils.createIcon("Database_profiled", "SQL Database", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon PROFILED_TABLE_ICON = SPSUtils.createIcon("Table_profiled", "SQL Table", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$

    public Icon filterIcon(Icon original, SPObject node) {
        if (node instanceof SQLTable) {
            Object profileCount = ((SQLTable) node).getClientProperty(ProfileManager.class, ProfileManager.PROFILE_COUNT_PROPERTY);
            if (profileCount != null && ((Integer) profileCount).intValue() > 0) {
                return PROFILED_TABLE_ICON;
            }
        }
        return original;
    }

    
}
