package ca.sqlpower.architect.etl.kettle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.database.DatabaseInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;

public class KettleUtils {

    private static final Logger logger = Logger.getLogger(KettleUtils.class);
    
    public static List<String> retrieveKettleConnectionTypes() {
        List<String> list = new ArrayList<String>();
        DatabaseInterface[] dbConnectionArray = DatabaseMeta.getDatabaseInterfaces();
        for (int i = 0; i < dbConnectionArray.length; i++) {
            list.add(dbConnectionArray[i].getDatabaseTypeDescLong());
        }
        return list;
    }
    
    /**
     * Creates a DatabaseMeta object based on the ArchitectDataSource given to it.
     * This will return null if an error occurred and execution should stop. 
     * 
     * @param parent The parent JFrame used for showing dialog windows.
     */
    public static DatabaseMeta createDatabaseMeta(ArchitectDataSource target) throws RuntimeException {
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
        
        databaseMeta = new DatabaseMeta(databaseName
                                              , connectionType
                                              , "Native"
                                              , hostname==null?"":hostname
                                              , database==null?"":database
                                              , port==null?"":port
                                              , username
                                              , password);
        

        return databaseMeta;
    }
}
