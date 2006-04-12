package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUserSettings;
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;

/**
 * Historically this file read from a configuration file named .architect-prefs in the
 * user's home directory (though the file's location could be changed with a file chooser).
 * This version uses java.util.prefs instead.
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

	// -------------------- READING THE FILE --------------------------

	public UserSettings read(ArchitectSession session) throws IOException {
		Preferences prefs = ArchitectFrame.getMainInstance().getPrefs();
		
		UserSettings userSettings = new UserSettings();
		List<String> driverJarNameList = session.getDriverJarList();
		
		int i;
		for (i = 0; i <= 99; i++) {
			String jarName = prefs.get(jarFilePrefName(i), null);
			if (jarName == null) {
				break;
			}
			// System.out.println("Getting JarName: " + jarName);
			if (!driverJarNameList.contains(jarName)) {
				driverJarNameList.add(jarName);
			}
		}
		for (; i <= 99; i++) {
			// System.out.println("Pruning dead jar entry " + i);
			prefs.remove(jarFilePrefName(i));
		}		
		
		userSettings.setPlDotIniPath(prefs.get("PL.INI.PATH", defaultHomeFile("pl.ini")));
		
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
		Preferences prefs = ArchitectFrame.getMainInstance().getPrefs();
		
		UserSettings userSettings = session.getUserSettings();
		
		List<String> driverJarList = session.getDriverJarList();
		Iterator<String> it = driverJarList.iterator();
		for (int i = 0 ; i <= 99 && it.hasNext(); i++) {
			String name = it.next();
			// System.out.println("Putting JAR " + i + " " + name);
			prefs.put(jarFilePrefName(i), name);
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
