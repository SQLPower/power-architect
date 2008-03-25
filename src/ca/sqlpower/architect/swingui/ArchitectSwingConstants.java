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

/**
 * Define constants in here when one or more UI classes need to collaborate.  
 */
public class ArchitectSwingConstants {
	private ArchitectSwingConstants() {
        // never gets called
	}

	// actionCommand identifiers for actions shared by Playpen and DBTree
	public static final String ACTION_COMMAND_SRC_DBTREE = "DBTree";
	public static final String ACTION_COMMAND_SRC_PLAYPEN = "PlayPen";	
}

