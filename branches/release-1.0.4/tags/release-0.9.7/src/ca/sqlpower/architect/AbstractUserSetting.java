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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class AbstractUserSetting implements UserSettings {
    private static Logger logger = Logger.getLogger(AbstractUserSetting.class);

    protected Map settings;

    public AbstractUserSetting() {
        super();
        settings = new HashMap();
    }

    /**
     * Gets the named property from the settings map.  If the value in
     * the map is a Number, the value is obtained by calling
     * <code>intValue()</code> on it.  If it is a String, it is
     * converted with <code>Integer.parseInt()</code>. Otherwise, the
     * default value is returned a warning is logged using
     * <code>logger</code>.  If there is no such value in the map,
     * the default is returned without logging a warning.
     */
    public int getInt(String propName, int defaultValue) {
        Object result = settings.get(propName);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).intValue();
        } else if (result instanceof String) {
            try {
                return Integer.parseInt((String) result);
            } catch (NumberFormatException e) {
                logger.warn("Couldn't parse value '"+result
                            +"' as integer for property '"+propName+"'");
            }
        }
        logger.warn("Using default value for integer property '"+propName+"' because of unknown data type for existing value");
        return defaultValue;
    }

    public void setInt(String propName, int value) {
        settings.put(propName, new Integer(value));
    }

    /**
     * Gets the named property from the settings map.  If the value in
     * the map is a Boolean, the value is obtained by calling
     * <code>booleanValue()</code> on it.  If it is a String, it is
     * converted with <code>Boolean.parseBoolean()</code>. Otherwise, the
     * default value is returned and a warning is logged using
     * <code>logger</code>.  If there is no such value in the map,
     * the default is returned without logging a warning.
     */
    public boolean getBoolean(String propName, boolean defaultValue) {
        Object result = settings.get(propName);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Boolean) {
            return ((Boolean) result).booleanValue();
        } else if (result instanceof String) {
            return Boolean.valueOf((String) result).booleanValue();
        }
        logger.warn("Using default value for boolean property '"+propName+"' because of unknown data type for existing value");
        return defaultValue;
    }

    public void setBoolean(String propName, boolean value) {
        settings.put(propName,  Boolean.valueOf(value));
    }

    public void setObject(String propName, Object value) {
        settings.put(propName, value);
    }

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
    public void putSetting(String propName, String propClassName, String propValue) {
        Object prop = null;
        if (propClassName.equals("java.lang.Integer")) {
            try {
                prop = Integer.valueOf(propValue);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer setting "+propName+"="+propValue);
                return;
            }
        } else if (propClassName.equals("java.lang.Boolean")) {
            prop = Boolean.valueOf(propValue);
        } else {
            logger.warn("Unknown property class "+propClassName);
            return;
        }
        settings.put(propName, prop);
    }

    public String getString(String propName, String defaultValue) {
        Object result = settings.get(propName);
        if (result == null) {
            return defaultValue;
        } else {
            return result.toString();
        }
    }

    public void setString(String propName, String value) {
        settings.put(propName,value);
    }

    public Object getObject(String propName, Object defaultValue) {
        Object result = settings.get(propName);
        if (result == null) {
            return defaultValue;
        } else {
            return result;
        }
    }

    /**
     * Returns the names of all settings currently held by this
     * SwingUserSettings object.  They will all be Strings.
     */
    public Set getSettingNames() {
        return settings.keySet();
    }
}
