/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
