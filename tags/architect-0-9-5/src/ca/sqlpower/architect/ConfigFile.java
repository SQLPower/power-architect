package ca.sqlpower.architect;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.qfa.QFAUserSettings;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;

/**
 * Historically this file read from a configuration file named .architect-prefs in the
 * user's home directory.
 * This version uses java.util.prefs instead; we've kept the class name for the time being...
 */
public class ConfigFile {

	private static final Logger logger = Logger.getLogger(ConfigFile.class);

	private static ConfigFile singleton;
    private Preferences prefs;

	/**
	 * The input or output file.
	 */
	protected File file;

	private ConfigFile() {

	}

	static {
		singleton = new ConfigFile();
	}

	public static ConfigFile getDefaultInstance() {
		return singleton;
	}

	// -------------------- READING THE "FILE" --------------------------

	public CoreUserSettings read(ArchitectSession session) {
		logger.debug("loading UserSettings from java.util.prefs.");
        if ( prefs == null ) {
            prefs = ArchitectFrame.getMainInstance().getPrefs();
        }
		logger.debug("Preferences class = " + prefs.getClass());

		CoreUserSettings userSettings = new CoreUserSettings();

		userSettings.setPlDotIniPath(prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null));

		UserSettings swingUserSettings = userSettings.getSwingSettings();
		swingUserSettings.setBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED,
			prefs.getBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));

		ETLUserSettings etlUserSettings = userSettings.getETLUserSettings();
		etlUserSettings.setString(ETLUserSettings.PROP_PL_ENGINE_PATH,
			prefs.get(ETLUserSettings.PROP_PL_ENGINE_PATH, ""));
		etlUserSettings.setString(ETLUserSettings.PROP_ETL_LOG_PATH,
			prefs.get(ETLUserSettings.PROP_ETL_LOG_PATH, defaultHomeFile("etl.log")));

		DDLUserSettings ddlUserSettings = userSettings.getDDLUserSettings();
		ddlUserSettings.setString(DDLUserSettings.PROP_DDL_LOG_PATH,prefs.get(DDLUserSettings.PROP_DDL_LOG_PATH, defaultHomeFile("ddl.log")));

		QFAUserSettings qfaUserSettings = userSettings.getQfaUserSettings();
        qfaUserSettings.setBoolean(QFAUserSettings.EXCEPTION_REPORTING,prefs.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true));

        PrintUserSettings printUserSettings = userSettings.getPrintUserSettings();
        printUserSettings.setDefaultPrinterName(
                prefs.get(PrintUserSettings.DEFAULT_PRINTER_NAME, ""));
		return userSettings;
	}


	// -------------------- "WRITING THE FILE" --------------------------

	public void write(ArchitectSession session) throws ArchitectException {
		logger.debug("Saving prefs to java.util.prefs");

		CoreUserSettings userSettings = session.getUserSettings();

		prefs.put(ArchitectSession.PREFS_PL_INI_PATH, userSettings.getPlDotIniPath());

		UserSettings swingUserSettings = userSettings.getSwingSettings();
		prefs.putBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED,
				swingUserSettings.getBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));

		ETLUserSettings etlUserSettings = userSettings.getETLUserSettings();
		prefs.put(ETLUserSettings.PROP_PL_ENGINE_PATH, etlUserSettings.getString(ETLUserSettings.PROP_PL_ENGINE_PATH,""));
		prefs.put(ETLUserSettings.PROP_ETL_LOG_PATH, etlUserSettings.getString(ETLUserSettings.PROP_ETL_LOG_PATH,""));

		DDLUserSettings ddlUserSettings = userSettings.getDDLUserSettings();
		prefs.put(DDLUserSettings.PROP_DDL_LOG_PATH, ddlUserSettings.getString(DDLUserSettings.PROP_DDL_LOG_PATH,""));

        QFAUserSettings qfaUserSettings = userSettings.getQfaUserSettings();
        prefs.putBoolean(QFAUserSettings.EXCEPTION_REPORTING,qfaUserSettings.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true));

		PrintUserSettings printUserSettings = userSettings.getPrintUserSettings();
		prefs.put(PrintUserSettings.DEFAULT_PRINTER_NAME, printUserSettings.getDefaultPrinterName());

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new ArchitectException("Unable to flush Java preferences", e);
		}
	}

	private String defaultHomeFile(String name) {
		return System.getProperty("user.home") + System.getProperty("file.separator") + name;
	}

    public Preferences getPrefs() {
        return prefs;
    }

    public void setPrefs(Preferences prefs) {
        this.prefs = prefs;
    }
}
