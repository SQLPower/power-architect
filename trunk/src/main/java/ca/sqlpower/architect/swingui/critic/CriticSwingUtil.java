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

import ca.sqlpower.swingui.SPSUtils;

public class CriticSwingUtil {
    
    /**
     * Error icon to go along with criticisms that are flagged to be errors.
     */
    public static final ImageIcon ERROR_ICON = SPSUtils.createIcon("error", "error badge");
    
    /**
     * Warning icon to go along with criticisms that are flagged to be warnings.
     */
    public static final ImageIcon WARNING_ICON = SPSUtils.createIcon("warning", "warning badge");

    private CriticSwingUtil() {
        //utility class
    }
}
