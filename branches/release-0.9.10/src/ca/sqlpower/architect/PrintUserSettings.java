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
package ca.sqlpower.architect;

import java.util.Properties;

/**
 * The DDLUserSettings class stores project-independent DDL settings.
 *
 * @see ca.sqlpower.architect.CoreUserSettings
 * @see ca.sqlpower.architect.etl.ETLUserSettings
 * @see ca.sqlpower.architect.ddl.DDLUserSettings
 * @see ca.sqlpower.architect.swingui.ArchitectSwingUserSettings
 */
public class PrintUserSettings {

	// ------ PROPERTY LIST KEYS ------	

	public static final String DEFAULT_PRINTER_NAME
		= "DEFAULT_PRINTER_NAME";

	
	// ------ INSTANCE VARIABLES ------

	/**
	 * A copy of the property list we were constructed with (empty if
	 * created from the public no-args constructor).
	 *
	 * <p>It is necessary to retain the initial list of properties this
	 * instance was constructed with because a newer version of the
	 * architect may have written additional properties which we don't
	 * want to drop when we save back to disk.
	 */
	protected Properties props;


	// ------ CONSTRUCTORS/FACTORIES ------

	/**
	 * Creates a user settings instance initialised to the default
	 * values.
	 */
	public PrintUserSettings() {
		props = new Properties();
	}

	public static PrintUserSettings createFromPropList(Properties props) {
		PrintUserSettings settings = new PrintUserSettings();
		settings.props.putAll(props);
		return settings;
	}

	// ------- INSTANCE METHODS -------

	/**
	 * Creates a Properties list and stores all settings to it in a
	 * string representation.  This method is only intended for the
	 * UserSettings class to serialize an instance of DDLUserSettings
	 * without having to know all the properties and how to convert
	 * them.  For normal getting/setting of properties, use the
	 * getXXX/setXXX methods.
	 */
	public Properties toPropList() {
		Properties propList = new Properties();
		propList.putAll(props);
		return propList;
	}

	/**
	 * This method is only intended for the UserSettings class to
	 * deserialize an instance of ETLUserSettings without having to
	 * know how to set the properties using their individual setXXX
	 * methods.
	 */
	public void putProperty(String name, String value) {
		props.setProperty(name, value);
	}

	// ------- ACCESSORS and MUTATORS -------

	public String getDefaultPrinterName() {
		return props.getProperty(DEFAULT_PRINTER_NAME);
	}

	public void setDefaultPrinterName(String v) {
		props.setProperty(DEFAULT_PRINTER_NAME, v);
	}

}
