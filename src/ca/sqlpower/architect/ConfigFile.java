package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;

/**
 * Historically this file read from a configuration file named .architect-prefs in the
 * user's home directory (though the file's location could be changed with a file chooser).
 * This version uses java.util.prefs instead; we've kept the class name for the time being...
 */
public class ConfigFile {

	private static final Logger logger = Logger.getLogger(ConfigFile.class);

	private static ConfigFile singleton;
	
	/**
	 * The input or output file.
	 */
	protected File file;

	/**
	 * Where to write xml output to, or null if we're not in write
	 * mode.
	 */
	protected PrintWriter out;

	/**
	 * The current amount of indentation (xml nesting level) in the
	 * output file.
	 */
	protected int indent;

	private ConfigFile() {
		
	}
	
	static {
		singleton = new ConfigFile();
	}

	public static ConfigFile getDefaultInstance() {
		return singleton;
	}

	// -------------------- READING THE "FILE" --------------------------

	public UserSettings read(ArchitectSession session) throws IOException {
		logger.debug("reading UserSettings from java.util.prefs.");
		Preferences prefs = ArchitectFrame.getMainInstance().getPrefs();
		logger.debug("Preferences class = " + prefs.getClass());
		
		UserSettings userSettings = new UserSettings();
		
		int i;
		for (i = 0; i <= 99; i++) {
			String jarName = prefs.get(jarFilePrefName(i), null);
			logger.debug("read Jar File entry: " + jarName);
			if (jarName == null) {
				break;
			}
			
			logger.debug("Adding JarName: " + jarName);
			session.addDriverJar(jarName);
			
		}
		// XXX Put prefs in sub-node, just delete it before you start.
		for (; i <= 99; i++) {
			prefs.remove(jarFilePrefName(i));
		}		
		
		userSettings.setPlDotIniPath(prefs.get("PL.INI.PATH", null));
		
		SwingUserSettings swingUserSettings = userSettings.getSwingSettings();
		swingUserSettings.setBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED,
			prefs.getBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));
		
		ETLUserSettings etlUserSettings = userSettings.getETLUserSettings();
		etlUserSettings.setPowerLoaderEnginePath(
			prefs.get(ETLUserSettings.PROP_PL_ENGINE_PATH, ""));
		etlUserSettings.setETLLogPath(
			prefs.get(ETLUserSettings.PROP_ETL_LOG_PATH, defaultHomeFile("etl.log")));
		
		DDLUserSettings ddlUserSettings = userSettings.getDDLUserSettings();
		ddlUserSettings.setDDLLogPath(prefs.get(DDLUserSettings.PROP_DDL_LOG_PATH, defaultHomeFile("ddl.log")));
		PrintUserSettings printUserSettings = userSettings.getPrintUserSettings();
		printUserSettings.setDefaultPrinterName(
				prefs.get(PrintUserSettings.DEFAULT_PRINTER_NAME, ""));
		
		return userSettings;
	}
	

	// -------------------- "WRITING THE FILE" --------------------------

	public void write(ArchitectSession session) throws ArchitectException {
		logger.debug("Saving prefs to java.util.prefs");
		Preferences prefs = ArchitectFrame.getMainInstance().getPrefs();
		
		UserSettings userSettings = session.getUserSettings();
		
		List<String> driverJarList = session.getDriverJarList();
		Iterator<String> it = driverJarList.iterator();
		for (int i = 0 ; i <= 99; i++) {
			if (it.hasNext()) {
				String name = it.next();
				logger.debug("Putting JAR " + i + " " + name);
				prefs.put(jarFilePrefName(i), name);
			} else {
				// XXX optimize this - make jar file be its own node, just delete the node before starting.
				prefs.remove(jarFilePrefName(i));
			}
		}
		
		
		prefs.put("PL.INI.PATH", userSettings.getPlDotIniPath());
		
		SwingUserSettings swingUserSettings = userSettings.getSwingSettings();
		prefs.putBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED,
				swingUserSettings.getBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));
		
		ETLUserSettings etlUserSettings = userSettings.getETLUserSettings();
		prefs.put(ETLUserSettings.PROP_PL_ENGINE_PATH, etlUserSettings.getPowerLoaderEnginePath());
		prefs.put(ETLUserSettings.PROP_ETL_LOG_PATH, etlUserSettings.getETLLogPath());
		
		DDLUserSettings ddlUserSettings = userSettings.getDDLUserSettings();
		prefs.put(DDLUserSettings.PROP_DDL_LOG_PATH, ddlUserSettings.getDDLLogPath());

		PrintUserSettings printUserSettings = userSettings.getPrintUserSettings();
		prefs.put(PrintUserSettings.DEFAULT_PRINTER_NAME, printUserSettings.getDefaultPrinterName());
		
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new ArchitectException("Unable to flush Java preferences", e);
		}
	}

	/**
	 * @param i
	 * @return
	 */
	private String jarFilePrefName(int i) {
		return "JDBCJarFile." + String.format("%02d", i);
	}
	
	private String defaultHomeFile(String name) {
		return System.getProperty("user.home") + System.getProperty("file.separator") + name;
	}
}
