package ca.sqlpower.architect;

import ca.sqlpower.architect.profile.ProfileManager;

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

    public ProfileManager getProfileManager();

}