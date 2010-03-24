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

import java.util.Set;

public interface UserSettings {

    /**
     * Gets the named property from the settings map.  If the value in
     * the map is a Number, the value is obtained by calling
     * <code>intValue()</code> on it.  If it is a String, it is
     * converted with <code>Integer.parseInt()</code>. Otherwise, the
     * default value is returned a warning is logged using
     * <code>logger</code>.  If there is no such value in the map,
     * the default is returned without logging a warning.
     */
    public abstract int getInt(String propName, int defaultValue);

    public abstract void setInt(String propName, int value);

    /**
     * Gets the named property from the settings map.  If the value in
     * the map is a Boolean, the value is obtained by calling
     * <code>booleanValue()</code> on it.  If it is a String, it is
     * converted with <code>Boolean.parseBoolean()</code>. Otherwise, the
     * default value is returned and a warning is logged using
     * <code>logger</code>.  If there is no such value in the map,
     * the default is returned without logging a warning.
     */
    public abstract boolean getBoolean(String propName, boolean defaultValue);

    public abstract void setBoolean(String propName, boolean value);

    public abstract void setObject(String propName, Object value);

    /**
     * Adds a new setting or updates the value of an existing setting.  This
     * method is meant to be used by the ConfigFile read method: The set&lt;Type&gt;
     * methods are an easier interface to the same thing.
     * 
     * @param propName The name of the property to add or update.
     * @param propClassName The class name of the property's value. Currently,
     * "java.lang.Integer" and "java.lang.Boolean" are supported.
     * @param propValue A string representation of the property's value.
     */
    public abstract void putSetting(String propName, String propClassName, String propValue);

    public abstract String getString(String propName, String defaultValue);
    
    public abstract void setString(String propName, String value);
    
    public abstract Object getObject(String propName, Object defaultValue);

    /**
     * Returns the names of all settings currently held by this
     * SwingUserSettings object.  They will all be Strings.
     */
    public abstract Set getSettingNames();

}