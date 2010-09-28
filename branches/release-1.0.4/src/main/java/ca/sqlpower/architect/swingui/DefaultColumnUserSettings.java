/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import java.util.prefs.Preferences;

import ca.sqlpower.architect.AbstractUserSetting;
import ca.sqlpower.architect.ArchitectSessionContextImpl;
import ca.sqlpower.sqlobject.SQLColumn;
import java.sql.Types;

public class DefaultColumnUserSettings extends AbstractUserSetting {
    
    // ----------PROPERTY LIST KEYS-------------
    
    private static final Preferences prefs = Preferences.userNodeForPackage(ArchitectSessionContextImpl.class);
    
    public static final String DEFAULT_COLUMN_NAME = "DefaultColumnUserSettings.DEFAULT_COLUMN_NAME";
    
    public static final String DEFAULT_COLUMN_TYPE = "DefaultColumnUserSettings.DEFAULT_COLUMN_TYPE";
    
    public static final String DEFAULT_COLUMN_PREC = "DefaultColumnUserSettings.DEFAULT_COLUMN_PREC";
    
    public static final String DEFAULT_COLUMN_SCALE = "DefaultColumnUserSettings.DEFAULT_COLUMN_SCALE";
    
    public static final String DEFAULT_COLUMN_INPK = "DefaultColumnUserSettings.DEFAULT_COLUMN_INPK";
    
    public static final String DEFAULT_COLUMN_NULLABLE = "DefaultColumnUserSettings.DEFAULT_COLUMN_NULLABLE";
    
    public static final String DEFAULT_COLUMN_AUTOINC = "DefaultColumnUserSettings.DEFAULT_COLUMN_AUTOINC";
    
    public static final String DEFAULT_COLUMN_REMARKS = "DefaultColumnUserSettings.DEFAULT_COLUMN_REMARKS";
    
    public static final String DEFAULT_COLUMN_DEFAULT_VALUE = "DefaultColumnUserSettings.DEFAULT_COLUMN_DEFAULT_VALUE";
    
    // ----------CONSTRUCTORS/FACTORIES--------------
    
    public DefaultColumnUserSettings() {
        super();
    }
    
    public static void setColumnDefaults()
    {
        SQLColumn.setDefaultName(prefs.get(DefaultColumnUserSettings.DEFAULT_COLUMN_NAME, "New Column"));
        SQLColumn.setDefaultType(prefs.getInt(DefaultColumnUserSettings.DEFAULT_COLUMN_TYPE, Types.VARCHAR));
        SQLColumn.setDefaultPrec(prefs.getInt(DefaultColumnUserSettings.DEFAULT_COLUMN_PREC, 10));
        SQLColumn.setDefaultScale(prefs.getInt(DefaultColumnUserSettings.DEFAULT_COLUMN_SCALE, 0));
        SQLColumn.setDefaultInPK(prefs.getBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_INPK, false));
        SQLColumn.setDefaultNullable(prefs.getBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_NULLABLE, false));
        SQLColumn.setDefaultAutoInc(prefs.getBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_AUTOINC, false));
        SQLColumn.setDefaultRemarks(prefs.get(DefaultColumnUserSettings.DEFAULT_COLUMN_REMARKS, ""));
        SQLColumn.setDefaultForDefaultValue(prefs.get(DefaultColumnUserSettings.DEFAULT_COLUMN_DEFAULT_VALUE, ""));
    }
}
