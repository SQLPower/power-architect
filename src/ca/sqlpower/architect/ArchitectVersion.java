package ca.sqlpower.architect;
/**
 * It is extremly important that this class has no dependancies outside of the standard java libraries.
 */
public class ArchitectVersion {
    
    public static final String APP_VERSION_MAJOR = "0";
    public static final String APP_VERSION_MINOR = "9";
    public static final String APP_VERSION_TINY  = "3";
    public static final String APP_VERSION = APP_VERSION_MAJOR+"."+
                                            APP_VERSION_MINOR+"." +
                                            APP_VERSION_TINY;
}
