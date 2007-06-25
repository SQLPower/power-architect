package ca.sqlpower.architect.etl;

import java.util.List;

import junit.framework.TestCase;
import be.ibridge.kettle.core.database.DatabaseInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.etl.kettle.KettleUtils;

public class KettleUtilsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testRetrieveKettleConnectionTypes() {
        List<String> utilsConnectionTypes = KettleUtils.retrieveKettleConnectionTypes();
        DatabaseInterface[] dbConnectionArray = DatabaseMeta.getDatabaseInterfaces();
        for (int i = 0; i < dbConnectionArray.length; i++) {
            assertEquals(utilsConnectionTypes.get(i), dbConnectionArray[i].getDatabaseTypeDescLong());
        }
    }
    
    public void testCreateDatabaseMetaUsingURL() {
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ArchitectDataSourceType dsType = ds.getParentType();
        dsType.setJdbcUrl("<Hostname>:<Port>:<Database>");
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        ds.setUrl("hostname:1234:database");
        DatabaseMeta dbMeta = null;
        // If something goes wrong this will throw a RuntimeException
        dbMeta = KettleUtils.createDatabaseMeta(ds);
        assertEquals(ds.getName(), dbMeta.getName());
        assertEquals(ds.getUser(), dbMeta.getUsername());
        assertEquals(ds.getPass(), dbMeta.getPassword());
        assertEquals("hostname", dbMeta.getHostname());
        assertEquals("database", dbMeta.getDatabaseName());
        assertEquals("oracle", dbMeta.getDatabaseTypeDesc().toLowerCase());
        assertEquals("Native", dbMeta.getAccessTypeDesc());
    }
    
    public void testCreateDatabaseMetaUsingProperties() {
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ds.put(KettleOptions.KETTLE_HOSTNAME_KEY, "Hostname");
        ds.put(KettleOptions.KETTLE_PORT_KEY, "1234");
        ds.put(KettleOptions.KETTLE_DATABASE_KEY, "Database");
        ArchitectDataSourceType dsType = ds.getParentType();
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        DatabaseMeta dbMeta = null;
        // If something goes wrong this will throw a RuntimeException
        dbMeta = KettleUtils.createDatabaseMeta(ds);
        assertEquals(ds.getName(), dbMeta.getName());
        assertEquals(ds.getUser(), dbMeta.getUsername());
        assertEquals(ds.getPass(), dbMeta.getPassword());
        assertEquals("Hostname", dbMeta.getHostname());
        assertEquals("Database", dbMeta.getDatabaseName());
        assertEquals("oracle", dbMeta.getDatabaseTypeDesc().toLowerCase());
        assertEquals("Native", dbMeta.getAccessTypeDesc());
    }
    
    public void testCreateDatabaseMetaUsingNulls() {
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ArchitectDataSourceType dsType = ds.getParentType();
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        DatabaseMeta dbMeta = null;
        // If something goes wrong this will throw a RuntimeException
        dbMeta = KettleUtils.createDatabaseMeta(ds);
        assertEquals(ds.getName(), dbMeta.getName());
        assertEquals(ds.getUser(), dbMeta.getUsername());
        assertEquals(ds.getPass(), dbMeta.getPassword());
        assertEquals("", dbMeta.getHostname());
        assertEquals("", dbMeta.getDatabaseName());
        assertEquals("oracle", dbMeta.getDatabaseTypeDesc().toLowerCase());
        assertEquals("Native", dbMeta.getAccessTypeDesc());
    }
}
