package ca.sqlpower.architect.etl.kettle;

import java.util.Map;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.database.DatabaseMeta;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.swingui.ASUtils;

public class KettleUtils {

    private static final Logger logger = Logger.getLogger(KettleUtils.class);
    
    /**
     * Creates a DatabaseMeta object based on the ArchitectDataSource given to it.
     * This will return null if an error occurred and execution should stop. 
     * 
     * @param parent The parent JFrame used for showing dialog windows.
     */
    public static DatabaseMeta createDatabaseMeta(ArchitectDataSource target, JFrame parent) {
        DatabaseMeta databaseMeta;
        
        String databaseName = target.getName();
        String username = target.getUser();
        String password = target.getPass();
        ArchitectDataSourceType targetType = target.getParentType();
        String connectionType = targetType.getProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY); 
        Map<String, String> map = targetType.retrieveURLParsing(target.getUrl());
        String hostname = map.get(KettleOptions.KETTLE_HOSTNAME);
        if (hostname == null) {
            hostname = target.get(KettleOptions.KETTLE_HOSTNAME_KEY);
        }
        String port = map.get(KettleOptions.KETTLE_PORT);
        if (port == null) {
            port = target.get(KettleOptions.KETTLE_PORT_KEY);
        }
        String database = map.get(KettleOptions.KETTLE_DATABASE);
        if (database == null) {
            database = target.get(KettleOptions.KETTLE_DATABASE_KEY);
        }
        
        try {
            databaseMeta = new DatabaseMeta(databaseName
                                                  , connectionType
                                                  , "Native"
                                                  , hostname==null?"":hostname
                                                  , database==null?"":database
                                                  , port==null?"":port
                                                  , username
                                                  , password);
        } catch (RuntimeException re) {
            StringBuffer buffer = new StringBuffer();
            if (connectionType == null || connectionType.equals("")) {
                buffer.append("The Kettle connection type was not set in User Preferences.\n");
            }
            if (hostname == null || hostname.equals("")) {
                buffer.append("The host name was not set.\n");
            }
            if (database == null || database.equals("")) {
                buffer.append("The database name was not set.\n");
            }
            if (port == null || port.equals("")) {
                buffer.append("The port number was not set.\n");
            }
            if (username == null || username.equals("")) {
                buffer.append("The user name was not set.\n");
            }
            logger.error("Could not connect to the database " + databaseName + ".");
            re.printStackTrace();
            ASUtils.showExceptionDialog
                            ("Could not create the database connection for " + databaseName + "." +
                                    "\n" + buffer.toString()
                             , re);
            return null;
        }
        if (!KettleOptions.testKettleDBConnection(parent, databaseMeta)) {
            return null;
        }
        return databaseMeta;
    }
}
