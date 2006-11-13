package ca.sqlpower.architect;

import java.util.List;

public interface ArchitectSession {

    public static final String PREFS_PL_INI_PATH = "PL.INI.PATH";

    /**
     * See {@link #userSettings}.
     *
     * @return the value of userSettings
     */
    public CoreUserSettings getUserSettings();

    /**
     * See {@link #userSettings}.
     *
     * @param argUserSettings Value to assign to this.userSettings
     */
    public void setUserSettings(CoreUserSettings argUserSettings);

    public List<String> getDriverJarList();

    /**
     * Adds an entry to the list of JDBC driver JAR files.  The
     * ConfigFile class uses this when loading the user settings file.
     */
    public boolean addDriverJar(String fullPath);

    public boolean removeDriverJar(String fullPath);

    public void removeAllDriverJars();

    public void clearDriverJarList();

    public JDBCClassLoader getJDBCClassLoader();

}