package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.qfa.QFAUserSettings;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class CoreUserSettings {
    private static final Logger logger = Logger.getLogger(CoreUserSettings.class);

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
	
	public CoreUserSettings() {
		super();
		printUserSettings = new PrintUserSettings();
		swingSettings = new SwingUserSettings();
		etlUserSettings = new ETLUserSettings();
		ddlUserSettings = new DDLUserSettings();
        qfaUserSettings = new QFAUserSettings();
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
    
    public List<ArchitectDataSource> getConnections() {
        return getPlDotIni().getConnections();
    }
}
