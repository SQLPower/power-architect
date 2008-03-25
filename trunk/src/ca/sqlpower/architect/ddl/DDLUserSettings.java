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
package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.AbstractUserSetting;

/**
 * The DDLUserSettings class stores project-independent DDL settings.
 *
 * @see ca.sqlpower.architect.CoreUserSettings
 * @see ca.sqlpower.architect.etl.ETLUserSettings
 * @see ca.sqlpower.architect.swingui.ArchitectSwingUserSettings
 */
public class DDLUserSettings extends AbstractUserSetting{

	// ------ PROPERTY LIST KEYS ------	

	public static final String PROP_DDL_LOG_PATH
		= "DDLUserSettings.PROP_DDL_LOG_PATH";

	

	// ------ CONSTRUCTORS/FACTORIES ------

	/**
	 * Creates a user settings instance initialised to the default
	 * values.
	 */
	public DDLUserSettings() {
		super();
	}
}
