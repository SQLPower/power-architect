package ca.sqlpower.architect.etl.kettle;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;

/**
 * A container for Kettle-related options.
 */
public class KettleOptions {

    private static final Logger logger = Logger.getLogger(KettleOptions.class);
    
    /**
     * The key to use in an ArchitectDataSource for the repository login
     * name.
     */
    public static final String KETTLE_REPOS_LOGIN_KEY = "ca.sqlpower.architect.etl.kettle.repos.login";

    /**
     * The key to use in an ArchitectDataSource for the repository password.
     */
    public static final String KETTLE_REPOS_PASSWORD_KEY = "ca.sqlpower.architect.etl.kettle.repos.password";
    
    /**
     * The key to use in an ArchitectDataSource for the connection type.
     */
    public static final String KETTLE_CONNECTION_TYPE_KEY = "ca.sqlpower.architect.etl.kettle.connectionType";
    
    /**
     * The string of the url parameter that defines the database name
     */
    public static final String KETTLE_DATABASE = "Database";
    
    /**
     * The key to use in an ArchitectDataSource for the database name.
     */
    public static final String KETTLE_DATABASE_KEY = "ca.sqlpower.architect.etl.kettle.database";

    /**
     * The string of the url parameter that defines the port
     */
    public static final String KETTLE_PORT = "Port";

    /**
     * The key to use in an ArchitectDataSource for the port value.
     */
    public static final String KETTLE_PORT_KEY = "ca.sqlpower.architect.etl.kettle.port";
    
    /**
     * The string of the url parameter that defines the host name
     */
    public static final String KETTLE_HOSTNAME = "Hostname";

    /**
     * The key to use in an ArchitectDataSource for the host name.
     */
    public static final String KETTLE_HOSTNAME_KEY = "ca.sqlpower.architect.etl.kettle.hostname";
        
    
    public static void testKettleDBConnection(DatabaseMeta dbMeta) throws KettleDatabaseException {
        Database db = new Database(dbMeta);
        try {
            db.connect();
        } finally {
            db.disconnect();
        }
    }

}
