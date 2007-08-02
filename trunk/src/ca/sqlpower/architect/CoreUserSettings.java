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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.swingui.ArchitectSwingUserSettings;
import ca.sqlpower.architect.swingui.QFAUserSettings;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

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
     * The parsed list of connections.
     */
    private DataSourceCollection plDotIni;
    
    /**
     * The location of the PL.INI file.
     */
    private String plDotIniPath;
    
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

        setPlDotIniPath(prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null));

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

        prefs.put(ArchitectSession.PREFS_PL_INI_PATH, getPlDotIniPath());

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

    public boolean isPlDotIniPathValid() {
        logger.debug("Checking pl.ini path: "+getPlDotIniPath());
        String path = getPlDotIniPath();
        if (path == null) {
            return false;
        } else {
            File f = new File(path);
            return (f.canRead() && f.isFile());
        }
    }
    
    /**
     * Tries to read the plDotIni if it hasn't been done already.  If it can't be read,
     * returns null and leaves the plDotIni property as null as well. See {@link #plDotIni}.
     */
    public DataSourceCollection getPlDotIni() {
        
        String path = getPlDotIniPath();
        if (path == null) return null;
        
        if (plDotIni == null) {
            plDotIni = new PlDotIni();
            try {
                logger.debug("Reading PL.INI defaults");
                plDotIni.read(getClass().getClassLoader().getResourceAsStream("default_database_types.ini"));
            } catch (IOException e) {
                throw new ArchitectRuntimeException(new ArchitectException("Failed to read system resource default_database_types.ini",e));
            }
            try {
                if (plDotIni != null) {
                    logger.debug("Reading new PL.INI instance");
                    plDotIni.read(new File(path));
                }
            } catch (IOException e) {
                throw new ArchitectRuntimeException(new ArchitectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
        }
        return plDotIni;
    }
    
    public void setPlDotIni(DataSourceCollection ini) {
        logger.debug("got new pl.ini \""+ini+"\"");
        plDotIni = ini;
    }
    
    /**
     * See {@link #plDotIniPath}.
     */
    public String getPlDotIniPath() {
        return plDotIniPath;
    }
    
    /**
     * Sets the plDotIniPath property, and nulls out the current plDotIni
     * if the given value differs from the existing one.  See {@link #plDotIniPath}.
     */
    public void setPlDotIniPath(String plDotIniPath) {
        logger.debug("PlDotIniPath changing from \""+this.plDotIniPath+"\" to \""+plDotIniPath+"\"");

        // important to short-circuit when the value is not different
        // (if we don't, the prefs panel doesn't save properly)
        if (this.plDotIniPath != null && this.plDotIniPath.equals(plDotIniPath)) {
            return;
        }
        this.plDotIniPath = plDotIniPath;
        this.plDotIni = null;
    }
    
    public List<SPDataSource> getConnections() {
        return getPlDotIni().getConnections();
    }
}
