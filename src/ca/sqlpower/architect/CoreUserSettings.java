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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.swingui.ArchitectSwingUserSettings;
import ca.sqlpower.architect.swingui.QFAUserSettings;

/**
 * This class is ill-conceived. It's part of the core API, but it has direct references
 * to all the various subsystems of the Architect.  We should change it so that the
 * subsystems can register themselves somehow, or even just get the subsystems to use
 * the java prefs API directly.  Either way, the knowledge that this class has of the
 * other parts of the API is highly undersirable.
 */
public class CoreUserSettings {
    private static final Logger logger = Logger.getLogger(CoreUserSettings.class);

    /**
     * The prefs node we read and write all the settings in.
     */
    private final Preferences prefs;
    
	/**
	 * For now, this just holds the preferred printer.  
	 */
    private PrintUserSettings printUserSettings;

	/**
	 * GUI-related settings.  This technically shouldn't be here
	 * (model is referencing view stuff) but it didn't seem right to
	 * make the settings file primarily swing-specific with a
	 * reference to the general architect prefs.
	 */
    private UserSettings swingSettings;

	/**
	 * ETL-related settings.  This is not a design problem like
	 * swingSettings is, since ETL is part of the app's core
	 * functionality.
	 */
    private ETLUserSettings etlUserSettings;

	/**
	 * DDL-related settings.  
	 */
    private DDLUserSettings ddlUserSettings;

    private QFAUserSettings qfaUserSettings;
	
	public CoreUserSettings(Preferences prefs) {
		super();
		printUserSettings = new PrintUserSettings();
		swingSettings = new ArchitectSwingUserSettings();
		etlUserSettings = new ETLUserSettings();
		ddlUserSettings = new DDLUserSettings();
        qfaUserSettings = new QFAUserSettings();
        this.prefs = prefs;
        loadFromPrefs();
	}
	
    /**
     * Sets up the various prefs objects based on values stored in the java.util.prefs
     * preferences node.
     */
    private final void loadFromPrefs() {
        logger.debug("loading UserSettings from java.util.prefs.");
        logger.debug("Preferences class = " + prefs.getClass());

        swingSettings.setBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED,
            prefs.getBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));

        etlUserSettings.setString(ETLUserSettings.PROP_PL_ENGINE_PATH,
            prefs.get(ETLUserSettings.PROP_PL_ENGINE_PATH, ""));
        etlUserSettings.setString(ETLUserSettings.PROP_ETL_LOG_PATH,
            prefs.get(ETLUserSettings.PROP_ETL_LOG_PATH, defaultHomeFile("etl.log")));

        ddlUserSettings.setString(DDLUserSettings.PROP_DDL_LOG_PATH,prefs.get(DDLUserSettings.PROP_DDL_LOG_PATH, defaultHomeFile("ddl.log")));

        qfaUserSettings.setBoolean(QFAUserSettings.EXCEPTION_REPORTING,prefs.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true));

        printUserSettings.setDefaultPrinterName(
                prefs.get(PrintUserSettings.DEFAULT_PRINTER_NAME, ""));
    }
    
    /**
     * Saves all the preferences that this class knows about back into the
     * prefs node.
     */
    public void write() throws ArchitectException {
        logger.debug("Saving user settings to java.util.prefs");

        prefs.putBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED,
                swingSettings.getBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));

        prefs.put(ETLUserSettings.PROP_PL_ENGINE_PATH, etlUserSettings.getString(ETLUserSettings.PROP_PL_ENGINE_PATH,""));
        prefs.put(ETLUserSettings.PROP_ETL_LOG_PATH, etlUserSettings.getString(ETLUserSettings.PROP_ETL_LOG_PATH,""));

        prefs.put(DDLUserSettings.PROP_DDL_LOG_PATH, ddlUserSettings.getString(DDLUserSettings.PROP_DDL_LOG_PATH,""));

        prefs.putBoolean(QFAUserSettings.EXCEPTION_REPORTING,qfaUserSettings.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true));

        prefs.put(PrintUserSettings.DEFAULT_PRINTER_NAME, printUserSettings.getDefaultPrinterName());

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            throw new ArchitectException("Unable to flush Java preferences", e);
        }
    }
    
    /**
     * Creates a full pathname for a file of the given name inside the user's
     * home directory.
     *  
     * @param name The file name.
     */
    private String defaultHomeFile(String name) {
        return System.getProperty("user.home") + System.getProperty("file.separator") + name;
    }
    
	public void setPrintUserSettings (PrintUserSettings printUserSettings) {
		this.printUserSettings = printUserSettings;
	}

	public PrintUserSettings getPrintUserSettings () {
		return this.printUserSettings;
	}

	public UserSettings getSwingSettings() {
		return swingSettings;
	}

	public void setSwingSettings(UserSettings sprefs) {
		swingSettings = sprefs;
	}

	public ETLUserSettings getETLUserSettings() {
		return etlUserSettings;
	}

	public void setETLUserSettings(ETLUserSettings v) {
		etlUserSettings = v;
	}

	public QFAUserSettings getQfaUserSettings() {
        return qfaUserSettings;
    }

    public void setQfaUserSettings(QFAUserSettings qfaUserSettings) {
        this.qfaUserSettings = qfaUserSettings;
    }

    public DDLUserSettings getDDLUserSettings() {
		return ddlUserSettings;
	}

	public void setDDLUserSettings(DDLUserSettings v) {
		ddlUserSettings = v;
	}
}
