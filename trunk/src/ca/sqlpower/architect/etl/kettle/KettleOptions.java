package ca.sqlpower.architect.etl.kettle;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
    
    public static boolean connectToDB(JPanel panel, DatabaseMeta dbMeta) {
        
        
        Database db = new Database(dbMeta);
        try {
            db.connect();
            return true;
        } catch (KettleDatabaseException e) {
            logger.error("Could not connect to the database.");
            e.printStackTrace();
            int response = JOptionPane.showConfirmDialog
                            (panel, "Could not connect to the database " + dbMeta.getName() + "." +
                                    "\nThe properties Hostname, Database, and Port must be defined in the URL." +
                                    "\nWould you like to continue?");
            return response == JOptionPane.OK_OPTION;
        } finally {
            db.disconnect();
        }
    }

}
